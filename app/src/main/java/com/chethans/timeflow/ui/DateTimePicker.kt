package com.chethans.timeflow.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import com.chethans.timeflow.R
import java.util.Calendar

fun showDatePicker(
    context: Context,
    initialDateMillis: Long = System.currentTimeMillis(),
    onDateSelected: (Long) -> Unit
) {
    val now = Calendar.getInstance()
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = initialDateMillis.coerceAtLeast(now.timeInMillis)

    val dialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            onDateSelected(calendar.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    // Block any date before today
    dialog.datePicker.minDate = now.timeInMillis
    dialog.show()
}

fun showTimePicker(
    context: Context,
    baseDateMillis: Long,
    initialDateTimeMillis: Long = baseDateMillis,
    onDateTimeSelected: (Long) -> Unit
) {
    val now = Calendar.getInstance()
    val baseDay = Calendar.getInstance().apply { timeInMillis = baseDateMillis }
    val isSameDay = baseDay.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
        baseDay.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)

    val calendar = Calendar.getInstance()
    calendar.timeInMillis = baseDateMillis

    val initialTimeCalendar = Calendar.getInstance().apply {
        timeInMillis = initialDateTimeMillis
    }

    TimePickerDialog(
        context,
        { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)

            val selected = calendar.timeInMillis
            if (selected <= System.currentTimeMillis()) {
                Toast.makeText(
                    context,
                    context.getString(R.string.select_future_time),
                    Toast.LENGTH_SHORT
                ).show()
                // Re-open so user can pick again
                showTimePicker(context, baseDateMillis, initialDateTimeMillis, onDateTimeSelected)
            } else {
                onDateTimeSelected(selected)
            }
        },
        // On same day, default to current hour/min so user can't accidentally pick past time
        if (isSameDay) now.get(Calendar.HOUR_OF_DAY) else initialTimeCalendar.get(Calendar.HOUR_OF_DAY),
        if (isSameDay) now.get(Calendar.MINUTE) else initialTimeCalendar.get(Calendar.MINUTE),
        true
    ).show()
}
