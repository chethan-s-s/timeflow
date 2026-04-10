package com.chethans.timeflow.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.chethans.timeflow.R
import com.chethans.timeflow.data.CountdownEntity
import com.chethans.timeflow.ui.components.categoryIconFor
import com.chethans.timeflow.util.formatLocalizedDateTime
import kotlinx.coroutines.delay

@Composable
fun CountdownCard(
    item: CountdownEntity,
    activeWidgetId: Int,
    currentTime: Long,
    onDelete: () -> Unit,
    onSetWidget: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    isSelected: Boolean = false,
    textScale: Float = 1f,
    highContrast: Boolean = false,
    isGridMode: Boolean = false
) {
    val remaining = item.targetTime - currentTime
    val endDateStr = remember(item.targetTime) { formatLocalizedDateTime(item.targetTime) }
    val selectedGradient = remember(item.colorIndex) { getCountdownGradient(item.colorIndex) }
    val countdownParts = remember(remaining) { extractCountdownParts(remaining) }
    val progress = remember(item.createdAt, item.targetTime, currentTime) {
        calculateCountdownProgress(item.createdAt, item.targetTime, currentTime)
    }
    val isActiveWidget = item.id == activeWidgetId
    var widgetPulse by remember(item.id) { mutableStateOf(false) }
    LaunchedEffect(activeWidgetId) {
        if (isActiveWidget) {
            widgetPulse = true
            delay(420)
            widgetPulse = false
        }
    }
    val widgetGlowAlpha by animateFloatAsState(
        targetValue = when {
            widgetPulse -> 0.28f
            isActiveWidget -> 0.14f
            else -> 0f
        },
        animationSpec = tween(240),
        label = "widgetGlowAlpha"
    )
    val widgetBorderWidth by animateDpAsState(
        targetValue = if (widgetPulse || isActiveWidget) 2.dp else 0.dp,
        animationSpec = tween(240),
        label = "widgetBorderWidth"
    )

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                vertical = 8.dp,
                horizontal = if (isGridMode) 6.dp else 16.dp
            )
            .pointerInput(item.id) { detectTapGestures(onLongPress = { onLongClick() }) }
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .height(
                if (isGridMode) (240.dp * textScale).coerceAtLeast(220.dp)
                else (180.dp * textScale).coerceAtLeast(180.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image (if exists)
            if (item.imageUri != null) {
                AsyncImage(
                    model = item.imageUri,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Gradient background if no image
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(selectedGradient))
                )
            }

            // Dark overlay for better text readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = if (highContrast) 0.58f else 0.44f))
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.16f))
                )
            }

            if (widgetPulse || isActiveWidget) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = if (widgetPulse) 0.08f else 0f))
                        .border(
                            width = widgetBorderWidth,
                            color = Color.White.copy(alpha = widgetGlowAlpha),
                            shape = RoundedCornerShape(24.dp)
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (isGridMode) 12.dp else 16.dp),
                verticalArrangement = if (isGridMode) Arrangement.SpaceBetween else Arrangement.spacedBy(6.dp)
            ) {
                // Row 1: Title & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = categoryIconFor(item.category),
                            contentDescription = item.category,
                            tint = Color.White,
                            modifier = Modifier.size(if (isGridMode) 18.dp else 24.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.category,
                                color = Color.White.copy(alpha = 0.88f),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = if (isGridMode) 11.sp else (13f * textScale).sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = item.title,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = if (isGridMode) (16f * textScale).sp else (24f * textScale).sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Row {
                        if (item.id != activeWidgetId && remaining > 0) {
                            IconButton(onClick = onSetWidget) {
                                Icon(
                                    imageVector = Icons.Default.Widgets,
                                    contentDescription = stringResource(R.string.set_as_widget, item.title),
                                    tint = Color.White.copy(alpha = 0.95f)
                                )
                            }
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete_countdown, item.title),
                                tint = Color.White.copy(alpha = 0.95f)
                            )
                        }
                    }
                }

                if (isGridMode) {
                    // Row 2: Countdown Timer (Circular Progress)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val circleSize = 132.dp
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.size(circleSize),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.22f),
                                strokeWidth = 9.dp
                            )

                            if (remaining <= 0) {
                                Text(
                                    text = stringResource(R.string.countdown_finished),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    TimeUnitRow(
                                        leftValue = countdownParts.days,
                                        leftUnit = "D",
                                        rightValue = countdownParts.hours,
                                        rightUnit = "H",
                                        textScale = textScale,
                                        isGridMode = true
                                    )
                                    TimeUnitRow(
                                        leftValue = countdownParts.minutes,
                                        leftUnit = "M",
                                        rightValue = countdownParts.seconds,
                                        rightUnit = "S",
                                        textScale = textScale,
                                        isGridMode = true
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = endDateStr,
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (12f * textScale).sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                } else {
                    // Old single-card style: full time text in one line and end date below it.
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = if (highContrast) 0.28f else 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        FormattedCountdownText(
                            ms = remaining,
                            valueFontSize = (32f * textScale).sp,
                            unitFontSize = (18f * textScale).sp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = stringResource(R.string.ends_on, endDateStr),
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = (12f * textScale).sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun FormattedCountdownText(
    ms: Long,
    valueFontSize: androidx.compose.ui.unit.TextUnit,
    unitFontSize: androidx.compose.ui.unit.TextUnit,
    color: Color = Color.White
) {
    if (ms <= 0) {
        Text(stringResource(R.string.countdown_finished), color = color, fontWeight = FontWeight.Bold)
        return
    }

    val totalSeconds = ms / 1000
    val totalMinutes = totalSeconds / 60
    val totalHours = totalMinutes / 60
    val totalDays = totalHours / 24

    val days = totalDays
    val hours = totalHours % 24
    val minutes = totalMinutes % 60
    val seconds = totalSeconds % 60

    Text(
        text = buildAnnotatedString {
            if (days > 0) {
                withStyle(style = SpanStyle(fontSize = valueFontSize, fontWeight = FontWeight.Bold)) {
                    append(days.toString())
                }
                withStyle(style = SpanStyle(fontSize = unitFontSize)) {
                    append(" ${stringResource(R.string.countdown_unit_day)} ")
                }
            }
            if (hours > 0 || days > 0) {
                withStyle(style = SpanStyle(fontSize = valueFontSize, fontWeight = FontWeight.Bold)) {
                    append(hours.toString())
                }
                withStyle(style = SpanStyle(fontSize = unitFontSize)) {
                    append(" ${stringResource(R.string.countdown_unit_hour)} ")
                }
            }
            withStyle(style = SpanStyle(fontSize = valueFontSize, fontWeight = FontWeight.Bold)) {
                append(minutes.toString())
            }
            withStyle(style = SpanStyle(fontSize = unitFontSize)) {
                append(" ${stringResource(R.string.countdown_unit_min)} ")
            }
            withStyle(style = SpanStyle(fontSize = valueFontSize, fontWeight = FontWeight.Bold)) {
                append(seconds.toString().padStart(2, '0'))
            }
            withStyle(style = SpanStyle(fontSize = unitFontSize)) {
                append(" ${stringResource(R.string.countdown_unit_sec)}")
            }
        },
        color = color
    )
}

@Composable
private fun TimeUnitRow(
    leftValue: Long,
    leftUnit: String,
    rightValue: Long,
    rightUnit: String,
    textScale: Float,
    isGridMode: Boolean
) {
    val valueSize = if (isGridMode) (21f * textScale).sp else (20f * textScale).sp
    val unitSize = if (isGridMode) (13f * textScale).sp else (12f * textScale).sp

    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        UnitValue(value = leftValue, unit = leftUnit, valueSize = valueSize, unitSize = unitSize)
        UnitValue(value = rightValue, unit = rightUnit, valueSize = valueSize, unitSize = unitSize)
    }
}

@Composable
private fun UnitValue(value: Long, unit: String, valueSize: androidx.compose.ui.unit.TextUnit, unitSize: androidx.compose.ui.unit.TextUnit) {
    Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            text = value.toString().padStart(2, '0'),
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = valueSize
        )
        Text(
            text = unit,
            color = Color.White.copy(alpha = 0.9f),
            fontWeight = FontWeight.Medium,
            fontSize = unitSize
        )
    }
}

private data class CountdownParts(
    val days: Long,
    val hours: Long,
    val minutes: Long,
    val seconds: Long
)

private fun extractCountdownParts(ms: Long): CountdownParts {
    if (ms <= 0) return CountdownParts(0, 0, 0, 0)

    val totalSeconds = ms / 1000
    val totalMinutes = totalSeconds / 60
    val totalHours = totalMinutes / 60
    val totalDays = totalHours / 24

    return CountdownParts(
        days = totalDays,
        hours = totalHours % 24,
        minutes = totalMinutes % 60,
        seconds = totalSeconds % 60
    )
}

private fun calculateCountdownProgress(createdAt: Long, targetTime: Long, now: Long): Float {
    val duration = (targetTime - createdAt).coerceAtLeast(1L)
    val elapsed = (now - createdAt).coerceIn(0L, duration)
    return (elapsed.toFloat() / duration.toFloat()).coerceIn(0f, 1f)
}

private val countdownGradients = listOf(
    listOf(Color(0xFF1B2631), Color(0xFF0B1217)),
    listOf(Color(0xFF145A32), Color(0xFF082D19)),
    listOf(Color(0xFF512E5F), Color(0xFF2E1A36)),
    listOf(Color(0xFF641E16), Color(0xFF3D120D)),
    listOf(Color(0xFF154360), Color(0xFF0C2637)),
    listOf(Color(0xFF4A235A), Color(0xFF2A1433)),
    listOf(Color(0xFF0E6251), Color(0xFF08392F)),
    listOf(Color(0xFF78281F), Color(0xFF451712)),
    listOf(Color(0xFF1B4F72), Color(0xFF0F2D41)),
    listOf(Color(0xFF1E8449), Color(0xFF114C2A)),
)

const val COUNTDOWN_GRADIENT_COUNT = 10

fun getCountdownGradient(colorIndex: Int): List<Color> {
    return countdownGradients.getOrElse(colorIndex) { countdownGradients.first() }
}

fun getAllCountdownGradients(): List<List<Color>> = countdownGradients

