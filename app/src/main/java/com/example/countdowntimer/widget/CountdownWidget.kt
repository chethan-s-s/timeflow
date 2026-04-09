package com.example.countdowntimer.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.os.Build
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.RemoteViews
import com.example.countdowntimer.MainActivity
import com.example.countdowntimer.R
import com.example.countdowntimer.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.min

class CountdownWidget : AppWidgetProvider() {

    companion object {
        private const val ACTION_UPDATE_WIDGET = "com.example.countdowntimer.ACTION_UPDATE_WIDGET"
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                updateWidgets(context, appWidgetManager, appWidgetIds)
                scheduleNextUpdate(context)
            } finally {
                pendingResult.finish()
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_UPDATE_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, CountdownWidget::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    updateWidgets(context, appWidgetManager, appWidgetIds)
                    scheduleNextUpdate(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        appWidgetIds.forEach { id ->
            editor.remove("active_id_$id")
            editor.remove("widget_text_scale_$id")
            editor.remove("widget_high_contrast_$id")
            editor.remove("widget_background_mode_$id")
        }
        editor.apply()
    }

    private suspend fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val database = AppDatabase.getDatabase(context)
        val dao = database.dao()
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val activeId = prefs.getInt("active_id", -1)
        val backgroundMode = runCatching {
            WidgetBackgroundMode.valueOf(
                prefs.getString("widget_background_mode", WidgetBackgroundMode.TRANSPARENT.name)
                    ?: WidgetBackgroundMode.TRANSPARENT.name
            )
        }.getOrDefault(WidgetBackgroundMode.TRANSPARENT)

        for (id in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            val widgetActiveId = prefs.getInt("active_id_$id", activeId)
            val widgetBackgroundMode = runCatching {
                WidgetBackgroundMode.valueOf(
                    prefs.getString("widget_background_mode_$id", backgroundMode.name) ?: backgroundMode.name
                )
            }.getOrDefault(backgroundMode)
            val widgetTextScale = prefs.getFloat("widget_text_scale_$id", 1f).coerceIn(0.85f, 1.4f)
            val widgetHighContrast = prefs.getBoolean("widget_high_contrast_$id", false)

            val activeCountdown = if (widgetActiveId != -1) {
                dao.getCountdownById(widgetActiveId)
            } else {
                null
            }

            // Create an Intent to launch MainActivity
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Set the click listener to the root layout of the widget
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)
            applyBackgroundForMode(
                context = context,
                views = views,
                activeCountdown = activeCountdown,
                mode = widgetBackgroundMode
            )
            applyWidgetTextStyle(views, widgetTextScale, widgetHighContrast)

            if (activeCountdown != null) {
                val remaining = activeCountdown.targetTime - System.currentTimeMillis()
                val timeText = formatWidgetTime(context, remaining)

                views.setTextViewText(R.id.widget_title, activeCountdown.title)
                views.setTextViewText(R.id.widget_time, timeText)
            } else {
                views.setTextViewText(R.id.widget_title, context.getString(R.string.widget_no_countdown_set))
                views.setTextViewText(R.id.widget_time, "")
            }

            appWidgetManager.updateAppWidget(id, views)
        }
    }

    private fun applyWidgetTextStyle(views: RemoteViews, textScale: Float, highContrast: Boolean) {
        views.setFloat(R.id.widget_title, "setTextSize", 18f * textScale)
        views.setFloat(R.id.widget_time, "setTextSize", 16f * textScale)
        if (highContrast) {
            views.setTextColor(R.id.widget_title, android.graphics.Color.WHITE)
            views.setTextColor(R.id.widget_time, android.graphics.Color.WHITE)
            views.setInt(R.id.widget_image_scrim, "setAlpha", 190)
        } else {
            views.setTextColor(R.id.widget_title, android.graphics.Color.parseColor("#FFFFFF"))
            views.setTextColor(R.id.widget_time, android.graphics.Color.parseColor("#E5E7EB"))
            views.setInt(R.id.widget_image_scrim, "setAlpha", 120)
        }
    }

    private fun applyBackgroundForMode(
        context: Context,
        views: RemoteViews,
        activeCountdown: com.example.countdowntimer.data.CountdownEntity?,
        mode: WidgetBackgroundMode
    ) {
        views.setViewVisibility(R.id.widget_background_image, View.GONE)
        views.setViewVisibility(R.id.widget_image_scrim, View.GONE)
        views.setImageViewBitmap(R.id.widget_background_image, null)

        when (mode) {
            WidgetBackgroundMode.TRANSPARENT -> {
                views.setInt(R.id.widget_root, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
            }

            WidgetBackgroundMode.COLOR -> {
                views.setInt(R.id.widget_root, "setBackgroundColor", colorForIndex(activeCountdown?.colorIndex))
            }

            WidgetBackgroundMode.IMAGE_OR_COLOR -> {
                val bitmap = decodeWidgetBitmap(context, activeCountdown?.imageUri)
                if (bitmap != null) {
                    views.setInt(R.id.widget_root, "setBackgroundColor", android.graphics.Color.TRANSPARENT)
                    views.setImageViewBitmap(R.id.widget_background_image, bitmap)
                    views.setViewVisibility(R.id.widget_background_image, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_image_scrim, View.VISIBLE)
                } else {
                    views.setInt(R.id.widget_root, "setBackgroundColor", colorForIndex(activeCountdown?.colorIndex))
                }
            }
        }
    }

    private fun colorForIndex(colorIndex: Int?): Int {
        val baseColors = intArrayOf(
            0xFF1B2631.toInt(),
            0xFF145A32.toInt(),
            0xFF512E5F.toInt(),
            0xFF641E16.toInt(),
            0xFF154360.toInt(),
            0xFF4A235A.toInt(),
            0xFF0E6251.toInt(),
            0xFF78281F.toInt(),
            0xFF1B4F72.toInt(),
            0xFF1E8449.toInt()
        )
        return if (colorIndex == null) 0xFF2F353F.toInt() else baseColors.getOrElse(colorIndex) { baseColors.first() }
    }

    private fun decodeWidgetBitmap(context: Context, uriString: String?): Bitmap? {
        if (uriString.isNullOrBlank()) return null

        return try {
            val parsed = Uri.parse(uriString)
            val decodeBounds: (BitmapFactory.Options) -> Unit = { opts ->
                when (parsed.scheme) {
                    "file", null -> BitmapFactory.decodeFile(parsed.path ?: uriString, opts)
                    else -> context.contentResolver.openInputStream(parsed)?.use { input ->
                        BitmapFactory.decodeStream(input, null, opts)
                    }
                }
            }
            val decodeBitmap: (BitmapFactory.Options) -> Bitmap? = { opts ->
                when (parsed.scheme) {
                    "file", null -> BitmapFactory.decodeFile(parsed.path ?: uriString, opts)
                    else -> context.contentResolver.openInputStream(parsed)?.use { input ->
                        BitmapFactory.decodeStream(input, null, opts)
                    }
                }
            }

            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            decodeBounds(bounds)
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val targetWidth = 720
            val targetHeight = 360
            val sample = calculateInSampleSize(bounds, targetWidth, targetHeight)

            val options = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.RGB_565
            }

            val decoded = decodeBitmap(options) ?: return null
            val scale = min(targetWidth / decoded.width.toFloat(), targetHeight / decoded.height.toFloat())
            if (scale >= 1f) {
                decoded
            } else {
                val scaled = Bitmap.createScaledBitmap(
                    decoded,
                    (decoded.width * scale).toInt().coerceAtLeast(1),
                    (decoded.height * scale).toInt().coerceAtLeast(1),
                    true
                )
                if (scaled !== decoded) decoded.recycle()
                scaled
            }
        } catch (_: Throwable) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height, width) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            var halfHeight = height / 2
            var halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }

    private fun scheduleNextUpdate(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, CountdownWidget::class.java).apply {
            action = ACTION_UPDATE_WIDGET
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Align updates to wall-clock minute boundaries to keep time text in sync.
        val now = System.currentTimeMillis()
        val triggerAt = now - (now % 60000L) + 60000L

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    }

    private fun formatWidgetTime(context: Context, ms: Long): CharSequence {
        if (ms <= 0) return context.getString(R.string.countdown_finished)

        val totalMinutes = ms / 1000 / 60
        val totalHours = totalMinutes / 60
        val totalDays = totalHours / 24

        val days = totalDays
        val hours = totalHours % 24
        val minutes = totalMinutes % 60

        val ssb = SpannableStringBuilder()

        fun appendStyled(value: Long, unit: String) {
            val start = ssb.length
            ssb.append(value.toString())
            ssb.setSpan(StyleSpan(Typeface.BOLD), start, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            ssb.setSpan(AbsoluteSizeSpan(24, true), start, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            
            val unitStart = ssb.length
            ssb.append(" $unit ")
            ssb.setSpan(AbsoluteSizeSpan(14, true), unitStart, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        if (days > 0) {
            appendStyled(days, context.resources.getQuantityString(R.plurals.countdown_days_unit, days.toInt()))
        }
        if (hours > 0 || days > 0) {
            appendStyled(hours, context.resources.getQuantityString(R.plurals.countdown_hours_unit, hours.toInt()))
        }
        appendStyled(minutes, context.resources.getQuantityString(R.plurals.countdown_minutes_unit, minutes.toInt()))

        return ssb
    }
}