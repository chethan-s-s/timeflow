package com.example.countdowntimer.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat

/**
 * A full-screen [Box] that lives inside a [androidx.compose.ui.window.Dialog].
 *
 * When the soft keyboard opens the bottom padding smoothly grows (spring animation)
 * so the card slides upward and nothing is hidden behind the keyboard.
 * When the keyboard closes the padding shrinks back with the same animation.
 */
@Composable
fun KeyboardAwareDialogBox(
    scrimColor: Color,
    horizontalPadding: Dp = 14.dp,
    topPadding: Dp = 24.dp,
    bottomPadding: Dp = 24.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val view = LocalView.current
    val density = LocalDensity.current

    // Tell the dialog window not to handle system windows itself so that
    // WindowInsets.ime delivers the keyboard height to Compose.
    DisposableEffect(view) {
        val window = (view.parent as? DialogWindowProvider)?.window
        window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
        onDispose {}
    }

    // Read current keyboard height (px) and convert to dp.
    val imeBottomPx = WindowInsets.ime.getBottom(density)

    // Animate with a natural spring: no bounce, medium stiffness (~300 ms feel).
    val animatedImeBottom by animateDpAsState(
        targetValue = with(density) { imeBottomPx.toDp() },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "keyboard_aware_bottom"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(scrimColor)
            .padding(horizontal = horizontalPadding)
            .padding(
                top = topPadding,
                // When keyboard is open animatedImeBottom > 0, pushing the card up.
                bottom = bottomPadding + animatedImeBottom
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

