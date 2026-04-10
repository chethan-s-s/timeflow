package com.chethans.timeflow.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.chethans.timeflow.R
import com.chethans.timeflow.data.CountdownEntity
import com.chethans.timeflow.ui.getCountdownGradient
import com.chethans.timeflow.util.formatLocalizedDateTime
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun CountdownDetailsDialog(
    item: CountdownEntity,
    activeWidgetId: Int,
    currentTime: Long,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onSetWidget: () -> Unit,
    onEdit: () -> Unit,
    largeText: Boolean = false
) {
    val context = LocalContext.current
    val gradient = remember(item.colorIndex) { getCountdownGradient(item.colorIndex) }
    val imageButtonGradient = produceState<List<Color>?>(initialValue = null, key1 = item.imageUri) {
        value = extractGradientFromImage(context, item.imageUri)
    }.value
    val actionGradientSource = imageButtonGradient ?: gradient
    val remaining = item.targetTime - currentTime
    val actionButtonBrush = remember(actionGradientSource) {
        Brush.horizontalGradient(
            colors = listOf(
                lightenColor(actionGradientSource.first(), 0.25f).copy(alpha = 0.72f),
                lightenColor(actionGradientSource.last(), 0.25f).copy(alpha = 0.72f)
            )
        )
    }
    val actionButtonColors = ButtonDefaults.filledTonalButtonColors(
        containerColor = Color.Transparent,
        contentColor = Color.White,
        disabledContainerColor = Color.Transparent,
        disabledContentColor = Color.White.copy(alpha = 0.95f)
    )
    val modalButtonShape = RoundedCornerShape(25.dp)
    val endDateStr = remember(item.targetTime) { formatLocalizedDateTime(item.targetTime) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .heightIn(max = if (largeText) 800.dp else 600.dp)
                .background(Color.Black.copy(alpha = 0.28f))
                .padding(horizontal = 14.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 228.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Background: Image if available, otherwise gradient
                    if (!item.imageUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = item.imageUri,
                            contentDescription = item.title,
                            modifier = Modifier.matchParentSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Semi-transparent overlay for readability
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Brush.verticalGradient(gradient))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(if (largeText) 28.dp else 24.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = categoryIconFor(item.category),
                                    contentDescription = item.category,
                                    tint = Color.White,
                                    modifier = Modifier.size(if (largeText) 30.dp else 26.dp)
                                )
                                Text(
                                    text = item.category,
                                    color = Color.White.copy(alpha = 0.9f),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = if (largeText) 12.sp else 11.sp
                                    ),
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = item.title,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = if (largeText) 28.sp else 24.sp
                                ),
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.height(if (largeText) 6.dp else 4.dp))

                        Text(
                            text = stringResource(R.string.ends_on, endDateStr),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = if (largeText) 16.sp else 14.sp
                            ),
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(if (largeText) 16.dp else 12.dp))

                        val isFinished = remaining <= 0
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = if (largeText) 220.dp else 180.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .padding(horizontal = if (largeText) 20.dp else 16.dp, vertical = if (largeText) 22.dp else 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ModalCountdownText(ms = remaining, largeText = largeText)
                        }

                        Spacer(modifier = Modifier.height(if (largeText) 16.dp else 12.dp))

                        // Circular progress ring
                        val totalDuration = (item.targetTime - item.createdAt).coerceAtLeast(1L)
                        val elapsed = (currentTime - item.createdAt).coerceIn(0L, totalDuration)
                        val progress = elapsed.toFloat() / totalDuration.toFloat()
                        CircularCountdownProgress(
                            progress = progress,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = if (largeText) 6.dp else 4.dp),
                            largeText = largeText
                        )

                        Spacer(modifier = Modifier.height(if (largeText) 16.dp else 12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(if (largeText) 56.dp else 48.dp)
                                    .clip(modalButtonShape)
                                    .background(actionButtonBrush),
                                colors = actionButtonColors,
                                shape = modalButtonShape
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                                Spacer(Modifier.width(2.dp))
                                Text(stringResource(R.string.delete), style = MaterialTheme.typography.labelSmall.copy(fontSize = if (largeText) 14.sp else 12.sp))
                            }
                            if (!isFinished) {
                                if (item.id == activeWidgetId) {
                                    FilledTonalButton(
                                        onClick = { },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(if (largeText) 56.dp else 48.dp)
                                            .clip(modalButtonShape)
                                            .background(actionButtonBrush),
                                        enabled = false,
                                        colors = actionButtonColors,
                                        shape = modalButtonShape
                                    ) {
                                        Icon(Icons.Filled.Widgets, contentDescription = stringResource(R.string.current_widget))
                                        Spacer(Modifier.width(2.dp))
                                        Text(stringResource(R.string.active), style = MaterialTheme.typography.labelSmall.copy(fontSize = if (largeText) 14.sp else 12.sp))
                                    }
                                } else {
                                    FilledTonalButton(
                                        onClick = onSetWidget,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(if (largeText) 56.dp else 48.dp)
                                            .clip(modalButtonShape)
                                            .background(actionButtonBrush),
                                        enabled = true,
                                        colors = actionButtonColors,
                                        shape = modalButtonShape
                                    ) {
                                        Icon(Icons.Filled.Widgets, contentDescription = stringResource(R.string.set_widget))
                                        Spacer(Modifier.width(2.dp))
                                        Text(stringResource(R.string.set_active), style = MaterialTheme.typography.labelSmall.copy(fontSize = if (largeText) 14.sp else 12.sp))
                                    }
                                }
                                FilledTonalButton(
                                    onClick = onEdit,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(if (largeText) 56.dp else 48.dp)
                                        .clip(modalButtonShape)
                                        .background(actionButtonBrush),
                                    colors = actionButtonColors,
                                    shape = modalButtonShape
                                ) {
                                    Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
                                    Spacer(Modifier.width(2.dp))
                                    Text(stringResource(R.string.edit), style = MaterialTheme.typography.labelSmall.copy(fontSize = if (largeText) 14.sp else 12.sp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (largeText) 56.dp else 48.dp),
                            shape = modalButtonShape,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(stringResource(R.string.close), fontSize = if (largeText) 16.sp else 14.sp)
                        }
                    }
                }
            }
        }
    }
}

internal fun lightenColor(color: Color, amount: Float): Color {
    val t = amount.coerceIn(0f, 1f)
    return Color(
        red = color.red + (1f - color.red) * t,
        green = color.green + (1f - color.green) * t,
        blue = color.blue + (1f - color.blue) * t,
        alpha = color.alpha
    )
}

@Composable
private fun ModalCountdownText(ms: Long, largeText: Boolean = false) {    if (ms <= 0) {
        Text(
            text = stringResource(R.string.countdown_finished),
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (largeText) 30.sp else 24.sp
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        return
    }

    val totalSeconds = ms / 1000
    val totalMinutes = totalSeconds / 60
    val totalHours = totalMinutes / 60
    val days = totalHours / 24
    val hours = totalHours % 24
    val minutes = totalMinutes % 60
    val seconds = totalSeconds % 60

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(if (largeText) 8.dp else 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (days > 0 || hours > 0) {
            Text(
                text = "${days} ${pluralStringResource(R.plurals.countdown_days_unit, days.toInt())}  ${hours} ${pluralStringResource(R.plurals.countdown_hours_unit, hours.toInt())}",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = if (largeText) 34.sp else 28.sp
                ),
                maxLines = 1
            )
        }

        Text(
            text = "${minutes} ${pluralStringResource(R.plurals.countdown_minutes_unit, minutes.toInt())}  ${seconds.toString().padStart(2, '0')} ${pluralStringResource(R.plurals.countdown_seconds_unit, seconds.toInt())}",
            color = Color.White,
            style = MaterialTheme.typography.displaySmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (largeText) 46.sp else 40.sp
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun CircularCountdownProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    largeText: Boolean = false
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(if (largeText) 100.dp else 80.dp)) {
            val strokeWidth = 8.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(diameter, diameter)

            // Track (background ring)
            drawArc(
                color = Color.White.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Progress arc
            drawArc(
                color = Color.White.copy(alpha = 0.9f),
                startAngle = -90f,
                sweepAngle = 360f * progress.coerceIn(0f, 1f),
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        // Percentage label in the centre
        Text(
            text = "${(progress * 100).toInt()}%",
            color = Color.White,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = if (largeText) 16.sp else 13.sp
            )
        )
    }
}

internal suspend fun extractGradientFromImage(
    context: android.content.Context,
    uriString: String?
): List<Color>? = withContext(Dispatchers.IO) {
    if (uriString.isNullOrBlank()) return@withContext null

    try {
        val uri = Uri.parse(uriString)
        val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: return@withContext null

        // Downscale to reduce work and sample dominant tones from top/bottom halves.
        val scaled = Bitmap.createScaledBitmap(bitmap, 24, 24, true)
        val topColor = averageRegionColor(scaled, 0, 0, 24, 12)
        val bottomColor = averageRegionColor(scaled, 0, 12, 24, 24)

        if (scaled != bitmap) scaled.recycle()
        bitmap.recycle()

        listOf(Color(topColor), Color(bottomColor))
    } catch (_: Exception) {
        null
    }
}

private fun averageRegionColor(
    bitmap: Bitmap,
    startX: Int,
    startY: Int,
    endX: Int,
    endY: Int
): Int {
    var r = 0L
    var g = 0L
    var b = 0L
    var count = 0L

    for (x in startX until endX) {
        for (y in startY until endY) {
            val p = bitmap.getPixel(x, y)
            r += android.graphics.Color.red(p)
            g += android.graphics.Color.green(p)
            b += android.graphics.Color.blue(p)
            count++
        }
    }

    if (count == 0L) return android.graphics.Color.WHITE

    return android.graphics.Color.rgb(
        (r / count).toInt(),
        (g / count).toInt(),
        (b / count).toInt()
    )
}
