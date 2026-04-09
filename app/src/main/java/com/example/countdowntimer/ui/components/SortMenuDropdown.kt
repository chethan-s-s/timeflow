package com.example.countdowntimer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.width
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import android.content.SharedPreferences
import com.example.countdowntimer.R

@Composable
fun SortMenuDropdown(
    expanded: Boolean,
    onDismiss: () -> Unit,
    sortAscending: Boolean,
    onSortAscending: (Boolean) -> Unit,
    isDark: Boolean,
    prefs: SharedPreferences
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier
            .background(
                brush = Brush.verticalGradient(
                    listOf(
                        if (isDark) Color(0xFF1E1E1E).copy(alpha = 0.98f) else Color(0xFFF5F5F5).copy(alpha = 0.98f),
                        if (isDark) Color(0xFF252525).copy(alpha = 0.98f) else Color(0xFFE8E8E8).copy(alpha = 0.98f)
                    )
                )
            )
            .width(220.dp),
        offset = DpOffset(x = (-40).dp, y = 8.dp)
    ) {
        DropdownMenuItem(
            text = {
                SortMenuItemContent(
                    label = stringResource(R.string.oldest_first),
                    description = stringResource(R.string.nearest_date_first),
                    isSelected = sortAscending,
                    isDark = isDark
                )
            },
            onClick = {
                onSortAscending(true)
                prefs.edit().putBoolean("sort_ascending", true).apply()
                onDismiss()
            },
            modifier = SortMenuItemBackgroundModifier(
                isSelected = sortAscending,
                isDark = isDark
            )
        )

        DropdownMenuItem(
            text = {
                SortMenuItemContent(
                    label = stringResource(R.string.newest_first),
                    description = stringResource(R.string.farthest_date_first),
                    isSelected = !sortAscending,
                    isDark = isDark
                )
            },
            onClick = {
                onSortAscending(false)
                prefs.edit().putBoolean("sort_ascending", false).apply()
                onDismiss()
            },
            modifier = SortMenuItemBackgroundModifier(
                isSelected = !sortAscending,
                isDark = isDark
            )
        )
    }
}
