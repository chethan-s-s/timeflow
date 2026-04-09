package com.example.countdowntimer.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.countdowntimer.R
import java.text.DateFormat
import java.util.Date
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@Composable
fun EditTimerDialog(
    showDialog: Boolean,
    title: String,
    selectedDateTime: Long,
    selectedColorIndex: Int?,
    gradientColors: List<Color>,
    allGradients: List<List<Color>>,
    imageUri: String = "",
    onTitleChange: (String) -> Unit,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    onImageSelected: (String?) -> Unit,
    onSelectColor: (Int) -> Unit,
    repeatYearly: Boolean,
    onRepeatYearlyChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    largeText: Boolean = false,
    dialogColors: TimerDialogColors
) {
    if (!showDialog) return

    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        onImageSelected(uri?.toString())
    }

    val dateText = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(selectedDateTime))
    val timeText = DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(selectedDateTime))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        KeyboardAwareDialogBox(
            scrimColor = dialogColors.scrimColor,
            topPadding = 32.dp,
            bottomPadding = 32.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
            ) {
                // Background: Image if available, otherwise gradient
                if (imageUri.isNotEmpty()) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.matchParentSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Semi-transparent overlay for readability
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(dialogColors.imageOverlayColor)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Brush.verticalGradient(gradientColors))
                    )
                }

                // Derive button colors: if an image is present, extract dominant colors from it
                // (same technique as CountdownDetailsDialog); otherwise fall back to gradient color.
                val imageButtonGradient = produceState<List<Color>?>(initialValue = null, key1 = imageUri) {
                    value = if (imageUri.isNotEmpty()) extractGradientFromImage(context, imageUri) else null
                }.value

                val actionGradientSource = imageButtonGradient ?: gradientColors
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
                    contentColor = Color.White
                )
                val actionButtonShape = RoundedCornerShape(25.dp)

                // Legacy flat color for non-image buttons (Upload Image button)
                val cardButtonColor = gradientColors.first().copy(alpha = 0.82f)
                val cardButtonContentColor = if (cardButtonColor.luminance() > 0.55f) Color(0xFF1F2329) else Color.White

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(if (largeText) 24.dp else 20.dp),
                    verticalArrangement = Arrangement.spacedBy(if (largeText) 20.dp else 18.dp)
                ) {
                    Text(
                        text = stringResource(R.string.edit_countdown),
                        color = dialogColors.contentColor,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = if (largeText) 24.sp else 20.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        label = { Text(stringResource(R.string.event_title)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = dialogColors.contentColor,
                            unfocusedTextColor = dialogColors.contentColor,
                            focusedLabelColor = dialogColors.contentColor,
                            unfocusedLabelColor = dialogColors.mutedContentColor,
                            focusedIndicatorColor = dialogColors.outlinedBorderColor,
                            unfocusedIndicatorColor = dialogColors.outlinedBorderColor.copy(alpha = 0.85f),
                            cursorColor = dialogColors.contentColor,
                            focusedContainerColor = dialogColors.fieldContainerColor,
                            unfocusedContainerColor = dialogColors.fieldContainerColor,
                            disabledContainerColor = dialogColors.fieldContainerColor
                        )
                    )


                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onPickDate,
                            modifier = Modifier
                                .weight(1f)
                                .height(if (largeText) 56.dp else 48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = dialogColors.contentColor),
                            border = BorderStroke(1.5.dp, dialogColors.outlinedBorderColor)
                        ) {
                            Text(dateText, fontSize = if (largeText) 16.sp else 14.sp)
                        }
                        OutlinedButton(
                            onClick = onPickTime,
                            modifier = Modifier
                                .weight(1f)
                                .height(if (largeText) 56.dp else 48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = dialogColors.contentColor),
                            border = BorderStroke(1.5.dp, dialogColors.outlinedBorderColor)
                        ) {
                            Text(timeText, fontSize = if (largeText) 16.sp else 14.sp)
                        }
                    }


                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (imageUri.isNotEmpty()) {
                            FilledTonalButton(
                                onClick = { onImageSelected(null) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (largeText) 56.dp else 48.dp)
                                    .clip(actionButtonShape)
                                    .background(actionButtonBrush),
                                colors = actionButtonColors,
                                shape = actionButtonShape
                            ) {
                                Text(stringResource(R.string.remove_image), fontSize = if (largeText) 16.sp else 14.sp)
                            }
                        } else {
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(if (largeText) 56.dp else 48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = cardButtonColor,
                                    contentColor = cardButtonContentColor
                                )
                            ) {
                                Text(stringResource(R.string.add_image), fontSize = if (largeText) 16.sp else 14.sp)
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.or_choose_color),
                        color = dialogColors.mutedContentColor,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontSize = if (largeText) 16.sp else 14.sp
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(if (largeText) 10.dp else 8.dp)
                    ) {
                        allGradients.forEachIndexed { index, colors ->
                                Box(
                                    modifier = Modifier
                                        .size(if (largeText) 40.dp else 30.dp)
                                        .clip(CircleShape)
                                        .background(Brush.verticalGradient(colors))
                                        .border(
                                            width = if (selectedColorIndex == index && imageUri.isEmpty()) 3.dp else 0.dp,
                                            color = dialogColors.selectionBorderColor,
                                            shape = CircleShape
                                        )
                                        .clickable { onSelectColor(index) }
                                )
                            }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(dialogColors.fieldContainerColor)
                            .clickable { onRepeatYearlyChange(!repeatYearly) }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.repeat_every_year),
                            color = dialogColors.contentColor,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = if (largeText) 16.sp else 14.sp
                            )
                        )
                        Checkbox(
                            checked = repeatYearly,
                            onCheckedChange = { onRepeatYearlyChange(it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = dialogColors.filledButtonContentColor,
                                uncheckedColor = dialogColors.mutedContentColor,
                                checkmarkColor = dialogColors.filledButtonColor
                            )
                        )
                    }



                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(if (largeText) 56.dp else 48.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = dialogColors.contentColor),
                            border = BorderStroke(1.5.dp, dialogColors.outlinedBorderColor)
                        ) {
                            Text(stringResource(R.string.cancel), fontSize = if (largeText) 16.sp else 14.sp)
                        }
                        FilledTonalButton(
                            onClick = onConfirm,
                            enabled = title.isNotBlank(),
                            modifier = Modifier
                                .weight(1f)
                                .height(if (largeText) 56.dp else 48.dp)
                                .clip(actionButtonShape)
                                .background(actionButtonBrush),
                            colors = actionButtonColors,
                            shape = actionButtonShape
                        ) {
                            Text(stringResource(R.string.save_changes), fontSize = if (largeText) 16.sp else 14.sp)
                        }
                    }
                }
            }
        }
    }
}
