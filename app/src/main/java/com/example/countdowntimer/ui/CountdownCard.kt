package com.example.countdowntimer.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.*
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.countdowntimer.R
import com.example.countdowntimer.data.CountdownEntity
import com.example.countdowntimer.ui.components.categoryIconFor
import com.example.countdowntimer.util.formatLocalizedDateTime

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
    highContrast: Boolean = false
) {
    val remaining = item.targetTime - currentTime
    val endDateStr = remember(item.targetTime) {
        formatLocalizedDateTime(item.targetTime)
    }

    val selectedGradient = remember(item.colorIndex) {
        getCountdownGradient(item.colorIndex)
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .pointerInput(item.id) {
                detectTapGestures(onLongPress = { onLongClick() })
            }
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.4f)
            )
            .height((180.dp * textScale).coerceAtLeast(180.dp)),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Image (if exists)
            if (item.imageUri != null) {
                AsyncImage(
                    model = item.imageUri,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Gradient background if no image
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Brush.verticalGradient(selectedGradient))
                )
            }

            // Dark overlay for better text readability
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = if (highContrast) 0.52f else 0.38f))
            )

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.16f))
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                // Row 1: Title & Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = categoryIconFor(item.category),
                                contentDescription = item.category,
                                tint = Color.White.copy(alpha = 0.96f),
                                modifier = Modifier.size((24f * textScale).dp)
                            )
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White.copy(alpha = 0.95f),
                                    fontSize = (10f * textScale).sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = (28f * textScale).sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
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

                // Row 2: Countdown Timer
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = if (highContrast) 0.28f else 0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    FormattedCountdownText(
                        ms = remaining,
                        valueFontSize = (28f * textScale).sp,
                        unitFontSize = (16f * textScale).sp
                    )
                }

                // Row 3: End Date
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = endDateStr,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = (14f * textScale).sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun FormattedCountdownText(
    ms: Long,
    valueFontSize: androidx.compose.ui.unit.TextUnit = 28.sp,
    unitFontSize: androidx.compose.ui.unit.TextUnit = 16.sp,
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
                    append(" ${pluralStringResource(R.plurals.countdown_days_unit, days.toInt())} ")
                }
            }
            if (hours > 0 || days > 0) {
                withStyle(style = SpanStyle(fontSize = valueFontSize, fontWeight = FontWeight.Bold)) {
                    append(hours.toString())
                }
                withStyle(style = SpanStyle(fontSize = unitFontSize)) {
                    append(" ${pluralStringResource(R.plurals.countdown_hours_unit, hours.toInt())} ")
                }
            }
            withStyle(style = SpanStyle(fontSize = valueFontSize, fontWeight = FontWeight.Bold)) {
                append(minutes.toString())
            }
            withStyle(style = SpanStyle(fontSize = unitFontSize)) {
                append(" ${pluralStringResource(R.plurals.countdown_minutes_unit, minutes.toInt())}")
            }
            withStyle(style = SpanStyle(fontSize = unitFontSize)) {
                append(" ")
            }
            withStyle(style = SpanStyle(fontSize = valueFontSize, fontWeight = FontWeight.Bold)) {
                append(seconds.toString().padStart(2, '0'))
            }
            withStyle(style = SpanStyle(fontSize = unitFontSize)) {
                append(" ${pluralStringResource(R.plurals.countdown_seconds_unit, seconds.toInt())}")
            }
        },
        color = color
    )
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

