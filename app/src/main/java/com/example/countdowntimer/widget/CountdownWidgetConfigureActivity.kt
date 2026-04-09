package com.example.countdowntimer.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.countdowntimer.R
import com.example.countdowntimer.data.AppDatabase
import com.example.countdowntimer.data.CountdownEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CountdownWidgetConfigureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finishWithResult(Activity.RESULT_CANCELED, appWidgetId)
            return
        }

        setResult(Activity.RESULT_CANCELED)

        val widgetPrefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val preferredCountdownId = widgetPrefs.getInt("active_id", -1)

        setContent {
            val items = remember { mutableStateListOf<CountdownEntity>() }
            var selectedId by remember { mutableIntStateOf(-1) }
            var textScale by remember { mutableFloatStateOf(1f) }
            var highContrast by remember { mutableStateOf(false) }
            var backgroundMode by remember { mutableStateOf(WidgetBackgroundMode.TRANSPARENT) }

            androidx.compose.runtime.LaunchedEffect(Unit) {
                lifecycleScope.launch {
                    val dao = AppDatabase.getDatabase(this@CountdownWidgetConfigureActivity).dao()
                    val loaded = withContext(Dispatchers.IO) { dao.getAllList() }
                    items.clear()
                    items.addAll(loaded)
                    selectedId = when {
                        loaded.isEmpty() -> -1
                        preferredCountdownId != -1 && loaded.any { it.id == preferredCountdownId } -> preferredCountdownId
                        else -> loaded.first().id
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1F2329))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.widget_setup_title),
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = stringResource(R.string.widget_pick_countdown),
                    color = Color(0xFFB9BDC5)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF2F353F), RoundedCornerShape(10.dp))
                                .clickable { selectedId = item.id }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = selectedId == item.id, onClick = { selectedId = item.id })
                            Text(
                                text = item.title,
                                color = Color.White,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                }

                Text(text = stringResource(R.string.widget_text_size), color = Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0.9f, 1.0f, 1.2f).forEach { scale ->
                        OutlinedButton(onClick = { textScale = scale }) {
                            Text(if (scale == 1.0f) stringResource(R.string.widget_text_size_default) else "${scale}x")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = stringResource(R.string.high_contrast), color = Color.White)
                    Switch(checked = highContrast, onCheckedChange = { highContrast = it })
                }

                Text(text = stringResource(R.string.widget_background), color = Color.White)
                WidgetBackgroundMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { backgroundMode = mode }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = backgroundMode == mode, onClick = { backgroundMode = mode })
                        Text(
                            text = when (mode) {
                                WidgetBackgroundMode.TRANSPARENT -> stringResource(R.string.widget_bg_transparent)
                                WidgetBackgroundMode.COLOR -> stringResource(R.string.widget_bg_color)
                                WidgetBackgroundMode.IMAGE_OR_COLOR -> stringResource(R.string.widget_bg_image_or_color)
                            },
                            color = Color.White,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { finishWithResult(Activity.RESULT_CANCELED, appWidgetId) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = {
                            saveWidgetPrefs(
                                context = this@CountdownWidgetConfigureActivity,
                                appWidgetId = appWidgetId,
                                countdownId = selectedId,
                                textScale = textScale,
                                highContrast = highContrast,
                                mode = backgroundMode
                            )
                            val updateIntent = Intent(this@CountdownWidgetConfigureActivity, CountdownWidget::class.java).apply {
                                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                            }
                            sendBroadcast(updateIntent)
                            finishWithResult(Activity.RESULT_OK, appWidgetId)
                        },
                        enabled = selectedId != -1,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }
    }

    private fun saveWidgetPrefs(
        context: Context,
        appWidgetId: Int,
        countdownId: Int,
        textScale: Float,
        highContrast: Boolean,
        mode: WidgetBackgroundMode
    ) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("active_id_$appWidgetId", countdownId)
            .putFloat("widget_text_scale_$appWidgetId", textScale)
            .putBoolean("widget_high_contrast_$appWidgetId", highContrast)
            .putString("widget_background_mode_$appWidgetId", mode.name)
            .apply()
    }

    private fun finishWithResult(resultCode: Int, appWidgetId: Int) {
        val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        setResult(resultCode, resultValue)
        finish()
    }
}
