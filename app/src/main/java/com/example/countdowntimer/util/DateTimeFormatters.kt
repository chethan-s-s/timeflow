package com.example.countdowntimer.util

import java.text.DateFormat
import java.util.Date
import java.util.Locale

fun formatLocalizedDateTime(
    timeMillis: Long,
    locale: Locale = Locale.getDefault()
): String {
    return DateFormat.getDateTimeInstance(
        DateFormat.FULL,
        DateFormat.SHORT,
        locale
    ).format(Date(timeMillis))
}

