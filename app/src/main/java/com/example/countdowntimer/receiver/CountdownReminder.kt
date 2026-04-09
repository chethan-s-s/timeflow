package com.example.countdowntimer.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Locale

private const val REQUEST_CODE_MULTIPLIER = 10
private const val ACTION_PREFIX = ".action.COUNTDOWN_REMINDER."

const val EXTRA_COUNTDOWN_ID = "countdown_id"
const val EXTRA_COUNTDOWN_TITLE = "countdown_title"
const val EXTRA_COUNTDOWN_CATEGORY = "countdown_category"
const val EXTRA_COUNTDOWN_TARGET_TIME = "countdown_target_time"
const val EXTRA_REMINDER_TYPE = "countdown_reminder_type"

const val ONE_DAY_MILLIS = 24L * 60L * 60L * 1000L
const val ONE_WEEK_MILLIS = 7L * ONE_DAY_MILLIS

enum class CountdownReminderType(val code: Int) {
    WEEK_BEFORE(1),
    DAY_BEFORE(2),
    END(3);

    fun requestCodeFor(countdownId: Int): Int = countdownId * REQUEST_CODE_MULTIPLIER + code

    companion object {
        fun fromCode(code: Int): CountdownReminderType {
            return entries.firstOrNull { it.code == code } ?: END
        }
    }
}

private fun reminderAction(context: Context, type: CountdownReminderType): String {
    return "${context.packageName}$ACTION_PREFIX${type.name}"
}

fun createCountdownReminderIntent(
    context: Context,
    countdownId: Int,
    title: String,
    category: String,
    targetTime: Long,
    reminderType: CountdownReminderType
): Intent {
    return Intent(context, CountdownReceiver::class.java).apply {
        action = reminderAction(context, reminderType)
        putExtra(EXTRA_COUNTDOWN_ID, countdownId)
        putExtra(EXTRA_COUNTDOWN_TITLE, title)
        putExtra(EXTRA_COUNTDOWN_CATEGORY, category)
        putExtra(EXTRA_COUNTDOWN_TARGET_TIME, targetTime)
        putExtra(EXTRA_REMINDER_TYPE, reminderType.code)
    }
}

private fun createReminderIdentityIntent(
    context: Context,
    countdownId: Int,
    reminderType: CountdownReminderType
): Intent {
    return Intent(context, CountdownReceiver::class.java).apply {
        action = reminderAction(context, reminderType)
        putExtra(EXTRA_COUNTDOWN_ID, countdownId)
    }
}

fun scheduleCountdownReminderAlarm(
    context: Context,
    countdownId: Int,
    title: String,
    category: String,
    targetTime: Long,
    triggerAtMillis: Long,
    reminderType: CountdownReminderType
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminderType.requestCodeFor(countdownId),
        createCountdownReminderIntent(
            context = context,
            countdownId = countdownId,
            title = title,
            category = category,
            targetTime = targetTime,
            reminderType = reminderType
        ),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    } else {
        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            pendingIntent
        )
    }
}

fun cancelCountdownReminderAlarm(
    context: Context,
    countdownId: Int,
    reminderType: CountdownReminderType
) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        reminderType.requestCodeFor(countdownId),
        createReminderIdentityIntent(context, countdownId, reminderType),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
    pendingIntent.cancel()
}

fun categoryNotificationTag(category: String): String {
    val normalized = category
        .trim()
        .ifBlank { "general" }
        .lowercase(Locale.US)
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')

    return "countdown_category_${normalized.ifBlank { "general" }}"
}

