package com.chethans.timeflow.ui

fun formatTime(ms: Long): String {

    if (ms <= 0) return "Countdown Finished"

    val totalMinutes = ms / 1000 / 60
    val totalHours = totalMinutes / 60
    val totalDays = totalHours / 24
    val totalWeeks = totalDays / 7

    val weeks = totalWeeks
    val days = totalDays % 7
    val hours = totalHours % 24
    val minutes = totalMinutes % 60

    val parts = mutableListOf<String>()
    if (weeks > 0) parts.add("%02d Week".format(weeks))
    if (days > 0) parts.add("%02d Day".format(days))
    if (hours > 0) parts.add("%02d Hour".format(hours))
    if (minutes > 0) parts.add("%02d Minutes".format(minutes))

    return if (parts.isEmpty()) "00 Minutes" else parts.joinToString(" ")
}
