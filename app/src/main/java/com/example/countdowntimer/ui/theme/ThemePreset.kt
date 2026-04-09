package com.example.countdowntimer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class ThemePreset {
    CLASSIC,
    MINIMAL
}

data class ThemeColors(
    val sandGradient: Brush,
    val backgroundGradient: Brush,
    val tabContainerColor: Color
)

@Composable
fun getThemeColors(themePreset: ThemePreset): ThemeColors {
    val isDark = isSystemInDarkTheme()

    return when (themePreset) {
        ThemePreset.CLASSIC -> ThemeColors(
            sandGradient = if (isDark)
                Brush.horizontalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
            else
                Brush.horizontalGradient(listOf(Color(0xFFE2D1C3), Color(0xFFC9B09A))),

            backgroundGradient = if (isDark)
                Brush.verticalGradient(listOf(Color(0xFF2A2A3E), Color(0xFF1A1A2E)))
            else
                Brush.verticalGradient(listOf(Color(0xFFC6AA92), Color(0xFF9D7F66))),

            tabContainerColor = if (isDark)
                Color(0xFF2A2A3E).copy(alpha = 0.9f)
            else
                Color(0xFF7F6654).copy(alpha = 0.55f)
        )

        ThemePreset.MINIMAL -> ThemeColors(
            sandGradient = if (isDark)
                Brush.horizontalGradient(listOf(Color(0xFF121212), Color(0xFF1E1E1E)))
            else
                Brush.horizontalGradient(listOf(Color(0xFFF2F2F2), Color(0xFFE6E6E6))),

            backgroundGradient = if (isDark)
                Brush.verticalGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A)))
            else
                Brush.verticalGradient(listOf(Color(0xFF666666), Color(0xFF555555))),

            tabContainerColor = if (isDark)
                Color(0xFF2B2B2B).copy(alpha = 0.92f)
            else
                Color(0xFF777777).copy(alpha = 0.75f)
        )
    }
}

@Composable
fun getOverlayAlphas(themePreset: ThemePreset): Pair<Float, Float> {
    val isDark = isSystemInDarkTheme()
    val classicLightMode = !isDark && themePreset == ThemePreset.CLASSIC

    val activeTabOverlayAlpha = if (classicLightMode) 0.32f else 0.5f
    val addFabOverlayAlpha = if (classicLightMode) 0.22f else 0.35f

    return Pair(activeTabOverlayAlpha, addFabOverlayAlpha)
}

