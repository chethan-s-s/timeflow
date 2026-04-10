package com.chethans.timeflow.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.chethans.timeflow.MainActivity
import com.chethans.timeflow.R
import com.chethans.timeflow.data.AppDatabase
import com.chethans.timeflow.util.nextYearlyOccurrence
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CountdownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra(EXTRA_COUNTDOWN_ID, 0)
        val reminderType = CountdownReminderType.fromCode(
            intent.getIntExtra(EXTRA_REMINDER_TYPE, CountdownReminderType.END.code)
        )

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getDatabase(context).dao()
                val item = dao.getCountdownById(id)
                if (item == null) {
                    return@launch
                }

                if (reminderType != CountdownReminderType.END && item.targetTime <= System.currentTimeMillis()) {
                    return@launch
                }

                showNotification(
                    context = context,
                    title = item.title,
                    category = item.category,
                    countdownId = item.id,
                    reminderType = reminderType
                )

                if (item.repeatYearly && reminderType == CountdownReminderType.END) {
                    val nextTime = nextYearlyOccurrence(item.targetTime)
                    dao.update(item.copy(targetTime = nextTime))
                    scheduleNext(context, item.id, item.title, item.category, nextTime)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun scheduleNext(
        context: Context,
        id: Int,
        title: String,
        category: String,
        timeMillis: Long
    ) {
        val now = System.currentTimeMillis()

        if (timeMillis - now > ONE_WEEK_MILLIS) {
            scheduleCountdownReminderAlarm(
                context = context,
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
                context = context,
                countdownId = id,
                title = title,
                category = category,
                targetTime = timeMillis,
                triggerAtMillis = timeMillis - ONE_DAY_MILLIS,
                reminderType = CountdownReminderType.DAY_BEFORE
            )
        }

        scheduleCountdownReminderAlarm(
            context = context,
            countdownId = id,
            title = title,
            category = category,
            targetTime = timeMillis,
            triggerAtMillis = timeMillis,
            reminderType = CountdownReminderType.END
        )
    }

    private fun showNotification(
        context: Context,
        title: String,
        category: String,
        countdownId: Int,
        reminderType: CountdownReminderType
    ) {
        val channelId = "countdown_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val isGeneralCategory = category.isBlank() || category.equals("General", ignoreCase = true)
        val notificationId = reminderType.requestCodeFor(countdownId)
        val notificationTag = categoryNotificationTag(category)

        val contentTitle = when (reminderType) {
            CountdownReminderType.WEEK_BEFORE -> {
                if (isGeneralCategory) {
                    context.getString(R.string.countdown_week_reminder_title_generic)
                } else {
                    context.getString(R.string.countdown_week_reminder_title_category, category)
                }
            }
            CountdownReminderType.DAY_BEFORE -> {
                if (isGeneralCategory) {
                    context.getString(R.string.countdown_day_reminder_title_generic)
                } else {
                    context.getString(R.string.countdown_day_reminder_title_category, category)
                }
            }
            CountdownReminderType.END -> {
                if (isGeneralCategory) {
                    context.getString(R.string.countdown_finished_title_generic)
                } else {
                    context.getString(R.string.countdown_finished_title_category, category)
                }
            }
        }

        val contentText = when (reminderType) {
            CountdownReminderType.WEEK_BEFORE -> {
                if (isGeneralCategory) {
                    context.getString(R.string.countdown_week_reminder_message_generic, title)
                } else {
                    context.getString(R.string.countdown_week_reminder_message_category, category, title)
                }
            }
            CountdownReminderType.DAY_BEFORE -> {
                if (isGeneralCategory) {
                    context.getString(R.string.countdown_day_reminder_message_generic, title)
                } else {
                    context.getString(R.string.countdown_day_reminder_message_category, category, title)
                }
            }
            CountdownReminderType.END -> {
                if (isGeneralCategory) {
                    context.getString(R.string.countdown_finished_message_generic, title)
                } else {
                    context.getString(R.string.countdown_finished_message_category, category, title)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                context.getString(R.string.countdown_notifications_channel),
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, notificationId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setGroup(notificationTag)
            .setSubText(category.ifBlank { context.getString(R.string.app_name) })
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationTag, notificationId, notification)
    }
}
