package com.chethans.timeflow.util

import java.util.Calendar

fun nextYearlyOccurrence(baseTimeMillis: Long, nowMillis: Long = System.currentTimeMillis()): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = baseTimeMillis }
    while (calendar.timeInMillis <= nowMillis) {
        calendar.add(Calendar.YEAR, 1)
    }
    return calendar.timeInMillis
}

