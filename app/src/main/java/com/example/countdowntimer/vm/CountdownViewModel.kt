package com.example.countdowntimer.vm

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.countdowntimer.R
import com.example.countdowntimer.data.AppDatabase
import com.example.countdowntimer.data.CountdownEntity
import com.example.countdowntimer.data.CountdownRepository
import com.example.countdowntimer.receiver.CountdownReminderType
import com.example.countdowntimer.receiver.ONE_DAY_MILLIS
import com.example.countdowntimer.receiver.ONE_WEEK_MILLIS
import com.example.countdowntimer.receiver.cancelCountdownReminderAlarm
import com.example.countdowntimer.receiver.createCountdownReminderIntent
import com.example.countdowntimer.receiver.scheduleCountdownReminderAlarm
import com.example.countdowntimer.util.BackupResult
import com.example.countdowntimer.util.classifyCategoryFromTitle
import com.example.countdowntimer.util.exportCountdownsToJson
import com.example.countdowntimer.util.importCountdownsFromJson
import com.example.countdowntimer.util.nextYearlyOccurrence
import com.example.countdowntimer.widget.CountdownWidget
import com.example.countdowntimer.widget.WidgetBackgroundMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class CountdownViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repo = CountdownRepository(db.dao())

    val countdowns = repo.getAll().asLiveData()

    private val prefs = application.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
    private val _activeWidgetId = MutableStateFlow(prefs.getInt("active_id", -1))
    val activeWidgetId = _activeWidgetId.asStateFlow()
    private val _widgetBackgroundMode = MutableStateFlow(
        runCatching {
            WidgetBackgroundMode.valueOf(
                prefs.getString("widget_background_mode", WidgetBackgroundMode.TRANSPARENT.name)
                    ?: WidgetBackgroundMode.TRANSPARENT.name
            )
        }.getOrDefault(WidgetBackgroundMode.TRANSPARENT)
    )
    val widgetBackgroundMode = _widgetBackgroundMode.asStateFlow()
    private var lastGeneratedColorIndex = -1

    init {
        backfillColorsIfAllSame()
        reconcileRecurringTimers()
        autoCategorizeGeneralTimers()
        rescheduleUpcomingNotifications()
    }

    fun add(
        title: String,
        time: Long,
        imageUri: String? = null,
        colorIndex: Int? = null,
        repeatYearly: Boolean = false
    ) {
        viewModelScope.launch {
            val resolvedColorIndex = colorIndex ?: nextRandomColorIndex()
            val scheduledTime = if (repeatYearly) nextYearlyOccurrence(time) else time
            val autoCategory = classifyCategoryFromTitle(title)
            val id = repo.add(
                title = title,
                time = scheduledTime,
                imageUri = imageUri,
                colorIndex = resolvedColorIndex,
                repeatYearly = repeatYearly,
                category = autoCategory
            )
            scheduleCountdownNotifications(id.toInt(), title, autoCategory, scheduledTime)
        }
    }

    private fun nextRandomColorIndex(): Int {
        val maxExclusive = 10
        var picked = Random.nextInt(maxExclusive)
        while (picked == lastGeneratedColorIndex) {
            picked = Random.nextInt(maxExclusive)
        }
        lastGeneratedColorIndex = picked
        return picked
    }

    private fun backfillColorsIfAllSame() {
        viewModelScope.launch {
            val items = repo.getAllList()
            if (items.size <= 1) return@launch

            val distinctColors = items.map { it.colorIndex }.toSet()
            if (distinctColors.size <= 1) {
                items.forEach { item ->
                    repo.update(item.copy(colorIndex = nextRandomColorIndex()))
                }
            }
        }
    }

    fun delete(item: CountdownEntity) {
        viewModelScope.launch {
            repo.delete(item)
            cancelCountdownNotifications(item.id)
            if (item.id == _activeWidgetId.value) {
                setActiveWidget(-1)
            }
        }
    }

    fun update(item: CountdownEntity) {
        viewModelScope.launch {
            val normalized = if (item.repeatYearly) {
                item.copy(targetTime = nextYearlyOccurrence(item.targetTime))
            } else {
                item
            }

            val recategorized = if (normalized.category == "General") {
                normalized.copy(category = classifyCategoryFromTitle(normalized.title))
            } else {
                normalized
            }

            repo.update(recategorized)
            cancelCountdownNotifications(recategorized.id)
            scheduleCountdownNotifications(
                recategorized.id,
                recategorized.title,
                recategorized.category,
                recategorized.targetTime
            )
            if (item.id == _activeWidgetId.value) {
                updateWidget()
            }
        }
    }

    private fun reconcileRecurringTimers() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val items = repo.getAllList()
            items.filter { it.repeatYearly && it.targetTime <= now }.forEach { item ->
                val nextTime = nextYearlyOccurrence(item.targetTime, now)
                val updated = item.copy(targetTime = nextTime)
                repo.update(updated)
                cancelCountdownNotifications(updated.id)
                scheduleCountdownNotifications(updated.id, updated.title, updated.category, nextTime)
            }
        }
    }

    private fun rescheduleUpcomingNotifications() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repo.getAllList()
                .filter { it.targetTime > now }
                .forEach { item ->
                    cancelCountdownNotifications(item.id)
                    scheduleCountdownNotifications(item.id, item.title, item.category, item.targetTime)
                }
        }
    }

    fun setActiveWidget(id: Int) {
        prefs.edit { putInt("active_id", id) }
        _activeWidgetId.value = id
        updateWidget()
    }

    /**
     * If no widget instance is currently pinned to the homescreen, asks the launcher to
     * pin one (Android 8.0+). Should be called right after [setActiveWidget] so the newly
     * selected countdown is already stored before the widget appears.
     *
     * @return `true` when no manual action is needed (widget already exists, or pin request accepted).
     *         Returns `false` when the launcher cannot/does not accept a pin request and the UI
     *         should instruct the user to add the widget manually.
     */
    fun requestPinWidgetIfNeeded(): Boolean {
        val app = getApplication<Application>()
        val manager = AppWidgetManager.getInstance(app)
        val component = ComponentName(app, CountdownWidget::class.java)

        // If at least one widget already exists, setting active_id is enough.
        if (manager.getAppWidgetIds(component).isNotEmpty()) return true
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return false
        if (!manager.isRequestPinAppWidgetSupported) return false

        return manager.requestPinAppWidget(component, null, null)
    }

    fun setWidgetBackgroundMode(mode: WidgetBackgroundMode) {
        prefs.edit { putString("widget_background_mode", mode.name) }
        _widgetBackgroundMode.value = mode
        updateWidget()
    }

    private fun updateWidget() {
        val intent = Intent(getApplication(), CountdownWidget::class.java).apply {
            action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(getApplication())
                .getAppWidgetIds(ComponentName(getApplication(), CountdownWidget::class.java))
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        }
        getApplication<Application>().sendBroadcast(intent)
    }

    private fun scheduleCountdownNotifications(
        id: Int,
        title: String,
        category: String,
        timeMillis: Long
    ) {
        val app = getApplication<Application>()
        val now = System.currentTimeMillis()
        if (timeMillis <= now) {
            app.sendBroadcast(
                createCountdownReminderIntent(
                    context = app,
                    countdownId = id,
                    title = title,
                    category = category,
                    targetTime = timeMillis,
                    reminderType = CountdownReminderType.END
                )
            )
            return
        }

        if (timeMillis - now > ONE_WEEK_MILLIS) {
            scheduleCountdownReminderAlarm(
                context = app,
                countdownId = id,
                title = title,
                category = category,
                targetTime = timeMillis,
                triggerAtMillis = timeMillis - ONE_WEEK_MILLIS,
                reminderType = CountdownReminderType.WEEK_BEFORE
            )
        }

        if (timeMillis - now > ONE_DAY_MILLIS) {
            scheduleCountdownReminderAlarm(
                context = app,
                countdownId = id,
                title = title,
                category = category,
                targetTime = timeMillis,
                triggerAtMillis = timeMillis - ONE_DAY_MILLIS,
                reminderType = CountdownReminderType.DAY_BEFORE
            )
        }

        scheduleCountdownReminderAlarm(
            context = app,
            countdownId = id,
            title = title,
            category = category,
            targetTime = timeMillis,
            triggerAtMillis = timeMillis,
            reminderType = CountdownReminderType.END
        )
    }

    private fun cancelCountdownNotifications(id: Int) {
        val app = getApplication<Application>()
        CountdownReminderType.entries.forEach { type ->
            cancelCountdownReminderAlarm(app, id, type)
        }
    }

    fun bulkDelete(ids: List<Int>) {
        viewModelScope.launch {
            val items = repo.getByIds(ids)
            items.forEach { cancelCountdownNotifications(it.id) }
            repo.deleteByIds(ids)

            if (_activeWidgetId.value in ids) {
                setActiveWidget(-1)
            } else {
                updateWidget()
            }
        }
    }

    fun bulkArchive(ids: List<Int>, archived: Boolean = true) {
        viewModelScope.launch {
            repo.updateArchived(ids, archived)
            if (archived && _activeWidgetId.value in ids) {
                setActiveWidget(-1)
            } else {
                updateWidget()
            }
        }
    }

    fun bulkSetCategory(ids: List<Int>, category: String) {
        viewModelScope.launch {
            val items = repo.getByIds(ids)
            repo.updateCategory(ids, category)
            val now = System.currentTimeMillis()
            items.forEach { item ->
                cancelCountdownNotifications(item.id)
                if (item.targetTime > now) {
                    scheduleCountdownNotifications(item.id, item.title, category, item.targetTime)
                }
            }
            updateWidget()
        }
    }

    private fun autoCategorizeGeneralTimers() {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repo.getAllList().forEach { item ->
                if (item.category == "General") {
                    val autoCategory = classifyCategoryFromTitle(item.title)
                    if (autoCategory != "General") {
                        val updated = item.copy(category = autoCategory)
                        repo.update(updated)
                        if (updated.targetTime > now) {
                            cancelCountdownNotifications(updated.id)
                            scheduleCountdownNotifications(
                                updated.id,
                                updated.title,
                                updated.category,
                                updated.targetTime
                            )
                        }
                    }
                }
            }
        }
    }

    fun exportBackup(uri: Uri, includeImages: Boolean, onDone: (BackupResult) -> Unit) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val result = exportCountdownsToJson(
                context = app,
                uri = uri,
                items = repo.getAllList(),
                includeImages = includeImages
            )
            onDone(result)
        }
    }

    fun importBackup(uri: Uri, replaceExisting: Boolean, onDone: (BackupResult) -> Unit) {
        viewModelScope.launch {
            val app = getApplication<Application>()
            val restored = importCountdownsFromJson(app, uri)

            restored.fold(
                onSuccess = { items ->
                    if (replaceExisting) {
                        repo.getAllList().forEach { cancelCountdownNotifications(it.id) }
                        repo.clearAll()
                    }

                    val now = System.currentTimeMillis()
                    val normalizedItems = items.map { item ->
                        if (item.repeatYearly && item.targetTime <= now) {
                            item.copy(targetTime = nextYearlyOccurrence(item.targetTime, now))
                        } else {
                            item
                        }
                    }

                    val ids = repo.insertAll(normalizedItems)
                    normalizedItems.zip(ids).forEach { (item, id) ->
                        if (item.targetTime > now) {
                            scheduleCountdownNotifications(
                                id = id.toInt(),
                                title = item.title,
                                category = item.category,
                                timeMillis = item.targetTime
                            )
                        }
                    }
                    updateWidget()
                    onDone(
                        BackupResult(
                            success = true,
                            message = app.getString(R.string.backup_import_success),
                            count = items.size
                        )
                    )
                },
                onFailure = { error ->
                    onDone(
                        BackupResult(
                            success = false,
                            message = error.message ?: app.getString(R.string.backup_import_failed)
                        )
                    )
                }
            )
        }
    }
}