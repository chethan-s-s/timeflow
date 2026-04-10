package com.chethans.timeflow.ui.components

import androidx.compose.ui.graphics.Color
import com.chethans.timeflow.ui.theme.ThemePreset

data class TimerDialogColors(
    val scrimColor: Color,
    val contentColor: Color,
    val mutedContentColor: Color,
    val fieldContainerColor: Color,
    val outlinedBorderColor: Color,
    val filledButtonColor: Color,
    val filledButtonContentColor: Color,
    val imageOverlayColor: Color,
    val selectionBorderColor: Color
)

fun timerDialogColors(themePreset: ThemePreset, isDark: Boolean): TimerDialogColors {
    return when (themePreset) {
        ThemePreset.CLASSIC -> {
            if (isDark) {
                TimerDialogColors(
                    scrimColor = Color.Black.copy(alpha = 0.35f),
                    contentColor = Color(0xFFF2EDE8),
                    mutedContentColor = Color(0xFFE0D4C9),
                    fieldContainerColor = Color.White.copy(alpha = 0.16f),
                    outlinedBorderColor = Color(0xFFE6D5C7).copy(alpha = 0.85f),
                    filledButtonColor = Color(0xFF9D7F66).copy(alpha = 0.45f),
                    filledButtonContentColor = Color(0xFFF2EDE8),
                    imageOverlayColor = Color.Black.copy(alpha = 0.5f),
                    selectionBorderColor = Color(0xFFF5E7DA)
                )
            } else {
                TimerDialogColors(
                    scrimColor = Color.Black.copy(alpha = 0.22f),
                    contentColor = Color(0xFF2F241D),
                    mutedContentColor = Color(0xFF4A3A30),
                    fieldContainerColor = Color.Black.copy(alpha = 0.08f),
                    outlinedBorderColor = Color(0xFF6B4E3C).copy(alpha = 0.45f),
                    filledButtonColor = Color(0xFF7F6654).copy(alpha = 0.28f),
                    filledButtonContentColor = Color(0xFF2F241D),
                    imageOverlayColor = Color.Black.copy(alpha = 0.3f),
                    selectionBorderColor = Color(0xFF5A4233)
                )
            }
        }

        ThemePreset.MINIMAL -> {
            if (isDark) {
                TimerDialogColors(
                    scrimColor = Color.Black.copy(alpha = 0.5f),
                    contentColor = Color(0xFFF2F3F5),
                    mutedContentColor = Color(0xFFB9BDC5),
                    fieldContainerColor = Color(0xFF2B2F36).copy(alpha = 0.92f),
                    outlinedBorderColor = Color(0xFF515864).copy(alpha = 0.9f),
                    filledButtonColor = Color(0xFF3A404A).copy(alpha = 0.95f),
                    filledButtonContentColor = Color(0xFFF2F3F5),
                    imageOverlayColor = Color.Black.copy(alpha = 0.45f),
                    selectionBorderColor = Color(0xFFD7DBE2)
                )
            } else {
                TimerDialogColors(
                    scrimColor = Color.Black.copy(alpha = 0.22f),
                    contentColor = Color(0xFF1F2329),
                    mutedContentColor = Color(0xFF616A76),
                    fieldContainerColor = Color(0xFFF0F2F5),
                    outlinedBorderColor = Color(0xFFC8CED8),
                    filledButtonColor = Color(0xFFE2E6EC),
                    filledButtonContentColor = Color(0xFF1F2329),
                    imageOverlayColor = Color.Black.copy(alpha = 0.28f),
                    selectionBorderColor = Color(0xFF3C4350)
                )
            }
        }
    }
}
