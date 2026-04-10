package com.chethans.timeflow.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width as spaceWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.MutableState
import com.chethans.timeflow.data.CountdownEntity
import com.chethans.timeflow.R
import com.chethans.timeflow.ui.theme.ThemePreset
import com.chethans.timeflow.util.deleteImageFromInternalStorage
import com.chethans.timeflow.vm.CountdownViewModel
import com.chethans.timeflow.widget.WidgetBackgroundMode
import android.content.SharedPreferences
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource

@Composable
fun HamburgerMenuContent(
    showMenu: Boolean,
    onDismiss: () -> Unit,
    themePreset: ThemePreset,
    largeText: Boolean,
    highContrast: Boolean,
    cardLayoutMode: CardLayoutMode,
    widgetBackgroundMode: WidgetBackgroundMode,
    includeImagesInBackup: Boolean,
    replaceOnImport: Boolean,
    onThemeChange: (ThemePreset) -> Unit,
    onLargeTextChange: (Boolean) -> Unit,
    onHighContrastChange: (Boolean) -> Unit,
    onCardLayoutModeChange: (CardLayoutMode) -> Unit,
    onWidgetBackgroundModeChange: (WidgetBackgroundMode) -> Unit,
    onIncludeImagesInBackupChange: (Boolean) -> Unit,
    onReplaceOnImportChange: (Boolean) -> Unit,
    onExportBackup: () -> Unit,
    onImportBackup: () -> Unit,
    prefs: SharedPreferences
) {
    val isDark = isSystemInDarkTheme()
    val configuration = LocalConfiguration.current
    val drawerWidth = (configuration.screenWidthDp.dp * 0.86f).coerceIn(260.dp, 340.dp)

    // Track which legal dialog (if any) is open
    var legalTab by remember { mutableStateOf<LegalTab?>(null) }

    // Show legal dialog when requested
    legalTab?.let { tab ->
        LegalInfoDialog(tab = tab, onDismiss = { legalTab = null })
    }

    // Drawer background - match theme
    val drawerBackground = when (themePreset) {
        ThemePreset.CLASSIC -> if (isDark)
            Color(0xFF1A1A2E).copy(alpha = 0.98f)
        else
            Color(0xFFE2D1C3).copy(alpha = 0.98f)
        ThemePreset.MINIMAL -> if (isDark)
            Color(0xFF121212).copy(alpha = 0.98f)
        else
            Color(0xFFF2F2F2).copy(alpha = 0.98f)
    }

    val textColor = if (isDark) Color(0xFFE0E0E0) else Color(0xFF333333)
    val dividerColor = if (isDark) Color(0xFF333333) else Color(0xFFDDDDDD)

    // Scrim overlay — fades in/out
    AnimatedVisibility(
        visible = showMenu,
        enter = fadeIn(animationSpec = tween(durationMillis = 320)),
        exit = fadeOut(animationSpec = tween(durationMillis = 280))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() }
        )
    }

    // Drawer panel — slides in from the left
    AnimatedVisibility(
        visible = showMenu,
        enter = slideInHorizontally(
            animationSpec = tween(durationMillis = 380, easing = FastOutSlowInEasing)
        ) { -it } + fadeIn(animationSpec = tween(durationMillis = 320)),
        exit = slideOutHorizontally(
            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
        ) { -it } + fadeOut(animationSpec = tween(durationMillis = 240))
    ) {
        Column(
            modifier = Modifier
                .width(drawerWidth)
                .fillMaxHeight()
                .background(drawerBackground)
                .statusBarsPadding()
                .padding(top = 12.dp)
                .padding(20.dp)
                .clickable { } // Prevent clicks from closing drawer
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
        // Top section - scrollable
        Column {
            // App Name
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = androidx.compose.ui.unit.TextUnit(32f, androidx.compose.ui.unit.TextUnitType.Sp),
                    color = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Horizontal line
            HorizontalDivider(
                modifier = Modifier.padding(bottom = 24.dp),
                color = dividerColor,
                thickness = 1.dp
            )

            // Theme Section
            Text(
                text = stringResource(R.string.theme),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold,
                    fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Theme Options
            ThemePreset.entries.forEach { preset ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onThemeChange(preset)
                            prefs.edit().putInt("theme_preset", preset.ordinal).apply()
                            onDismiss()
                        }
                        .padding(vertical = 10.dp)
                        .background(
                            color = if (themePreset == preset)
                                (if (isDark) Color(0xFF263238).copy(alpha = 0.4f) else Color(0xFFE3F2FD).copy(alpha = 0.5f))
                            else Color.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (themePreset == preset)
                                    (if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2))
                                else Color.Transparent,
                                shape = CircleShape
                            )
                            .border(
                                width = if (themePreset == preset) 0.dp else 1.5.dp,
                                color = if (isDark) Color(0xFF666666) else Color(0xFFCCCCCC),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.spaceWidth(12.dp))
                    Text(
                        text = preset.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = textColor,
                        fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = dividerColor,
                thickness = 1.dp
            )

            // Preferences Section
            Text(
                text = stringResource(R.string.preferences),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold,
                    fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Larger Text Option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onLargeTextChange(!largeText)
                        prefs.edit().putBoolean("large_text", !largeText).apply()
                    }
                    .padding(vertical = 12.dp)
                    .background(
                        color = if (largeText)
                            (if (isDark) Color(0xFF263238).copy(alpha = 0.4f) else Color(0xFFE3F2FD).copy(alpha = 0.5f))
                        else Color.Transparent,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(
                    checked = largeText,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2)
                    ),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.larger_text),
                    color = textColor,
                    fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            }

            // High Contrast Option
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onHighContrastChange(!highContrast)
                        prefs.edit().putBoolean("high_contrast", !highContrast).apply()
                    }
                    .padding(vertical = 12.dp)
                    .background(
                        color = if (highContrast)
                            (if (isDark) Color(0xFF263238).copy(alpha = 0.4f) else Color(0xFFE3F2FD).copy(alpha = 0.5f))
                        else Color.Transparent,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Checkbox(
                    checked = highContrast,
                    onCheckedChange = null,
                    colors = CheckboxDefaults.colors(
                        checkedColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2)
                    ),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    stringResource(R.string.high_contrast),
                    color = textColor,
                    fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp)
                )
            }

            Text(
                text = stringResource(R.string.card_layout_mode),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold,
                    fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)
            )

            CardLayoutMode.entries.forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCardLayoutModeChange(mode) }
                        .padding(vertical = 8.dp)
                        .background(
                            color = if (cardLayoutMode == mode)
                                (if (isDark) Color(0xFF263238).copy(alpha = 0.4f) else Color(0xFFE3F2FD).copy(alpha = 0.5f))
                            else Color.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (cardLayoutMode == mode)
                                    (if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2))
                                else Color.Transparent,
                                shape = CircleShape
                            )
                            .border(
                                width = if (cardLayoutMode == mode) 0.dp else 1.5.dp,
                                color = if (isDark) Color(0xFF666666) else Color(0xFFCCCCCC),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.spaceWidth(12.dp))
                    Text(
                        text = when (mode) {
                            CardLayoutMode.SINGLE_COLUMN -> stringResource(R.string.card_layout_single)
                            CardLayoutMode.TWO_COLUMN -> stringResource(R.string.card_layout_double)
                        },
                        color = textColor,
                        fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 6.dp),
                color = dividerColor,
                thickness = 1.dp
            )

            Text(
                text = stringResource(R.string.widget_background),
                style = MaterialTheme.typography.labelMedium.copy(
                    color = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold,
                    fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
                ),
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            WidgetBackgroundMode.entries
                .filter { it != WidgetBackgroundMode.COLOR }
                .forEach { mode ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onWidgetBackgroundModeChange(mode) }
                        .padding(vertical = 10.dp)
                        .background(
                            color = if (widgetBackgroundMode == mode)
                                (if (isDark) Color(0xFF263238).copy(alpha = 0.4f) else Color(0xFFE3F2FD).copy(alpha = 0.5f))
                            else Color.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (widgetBackgroundMode == mode)
                                    (if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2))
                                else Color.Transparent,
                                shape = CircleShape
                            )
                            .border(
                                width = if (widgetBackgroundMode == mode) 0.dp else 1.5.dp,
                                color = if (isDark) Color(0xFF666666) else Color(0xFFCCCCCC),
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.spaceWidth(12.dp))
                    Text(
                        text = when (mode) {
                            WidgetBackgroundMode.TRANSPARENT -> stringResource(R.string.widget_bg_transparent)
                            WidgetBackgroundMode.COLOR -> stringResource(R.string.widget_bg_color)
                            WidgetBackgroundMode.IMAGE_OR_COLOR -> stringResource(R.string.widget_bg_image_or_color)
                        },
                        color = textColor,
                        fontSize = androidx.compose.ui.unit.TextUnit(14f, androidx.compose.ui.unit.TextUnitType.Sp)
                    )
                }
            }

            // TODO: Data Backup section temporarily commented out
            // HorizontalDivider(
            //     modifier = Modifier.padding(vertical = 16.dp),
            //     color = dividerColor,
            //     thickness = 1.dp
            // )
            //
            // Text(
            //     text = stringResource(R.string.data_backup),
            //     style = MaterialTheme.typography.labelMedium.copy(
            //         color = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2),
            //         fontWeight = FontWeight.Bold,
            //         fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp)
            //     ),
            //     modifier = Modifier.padding(bottom = 10.dp)
            // )
            //
            // Row(
            //     verticalAlignment = Alignment.CenterVertically,
            //     modifier = Modifier
            //         .fillMaxWidth()
            //         .clickable { onIncludeImagesInBackupChange(!includeImagesInBackup) }
            //         .padding(vertical = 8.dp),
            //     horizontalArrangement = Arrangement.spacedBy(10.dp)
            // ) {
            //     Checkbox(
            //         checked = includeImagesInBackup,
            //         onCheckedChange = { onIncludeImagesInBackupChange(it) },
            //         colors = CheckboxDefaults.colors(
            //             checkedColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2)
            //         ),
            //         modifier = Modifier.size(20.dp)
            //     )
            //     Text(stringResource(R.string.include_images_in_backup), color = textColor)
            // }
            //
            // Row(
            //     verticalAlignment = Alignment.CenterVertically,
            //     modifier = Modifier
            //         .fillMaxWidth()
            //         .clickable { onReplaceOnImportChange(!replaceOnImport) }
            //         .padding(vertical = 8.dp),
            //     horizontalArrangement = Arrangement.spacedBy(10.dp)
            // ) {
            //     Checkbox(
            //         checked = replaceOnImport,
            //         onCheckedChange = { onReplaceOnImportChange(it) },
            //         colors = CheckboxDefaults.colors(
            //             checkedColor = if (isDark) Color(0xFF90CAF9) else Color(0xFF1976D2)
            //         ),
            //         modifier = Modifier.size(20.dp)
            //     )
            //     Text(stringResource(R.string.replace_data_on_import), color = textColor)
            // }
            //
            // Row(
            //     modifier = Modifier.fillMaxWidth(),
            //     horizontalArrangement = Arrangement.spacedBy(8.dp)
            // ) {
            //     OutlinedButton(onClick = onExportBackup, modifier = Modifier.weight(1f)) {
            //         Text(stringResource(R.string.export_backup))
            //     }
            //     OutlinedButton(onClick = onImportBackup, modifier = Modifier.weight(1f)) {
            //         Text(stringResource(R.string.import_backup))
            //     }
            // }
        }


        // Bottom section - footer links
        Column {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = dividerColor,
                thickness = 1.dp
            )

            // Privacy Policy
            Text(
                text = stringResource(R.string.privacy_policy),
                color = textColor,
                fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { legalTab = LegalTab.PRIVACY_POLICY }
                    .padding(vertical = 10.dp)
            )

            // Terms of Service
            Text(
                text = stringResource(R.string.terms_of_service),
                color = textColor,
                fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { legalTab = LegalTab.TERMS_OF_SERVICE }
                    .padding(vertical = 10.dp)
            )

            // About Developer
            Text(
                text = stringResource(R.string.about_developer),
                color = textColor,
                fontSize = androidx.compose.ui.unit.TextUnit(12f, androidx.compose.ui.unit.TextUnitType.Sp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { legalTab = LegalTab.ABOUT_DEVELOPER }
                    .padding(vertical = 10.dp)
            )

            HorizontalDivider(
                modifier = Modifier.padding(top = 12.dp),
                color = dividerColor,
                thickness = 1.dp
            )
        }
        } // end outer Column (drawer panel)
    } // end AnimatedVisibility (drawer panel)
}

@Composable
fun SelectionModeActions(
    selectedIds: List<Int>,
    filteredItems: List<CountdownEntity>,
    vm: CountdownViewModel,
    showCategoryMenu: MutableState<Boolean>,
    onClearSelection: () -> Unit
) {
    val deleteSelected = stringResource(R.string.delete_selected)
    val archiveSelected = stringResource(R.string.archive_selected)
    val setCategory = stringResource(R.string.set_category_for_selected)

    IconButton(onClick = {
        val selectedItems = filteredItems.filter { it.id in selectedIds }
        selectedItems.forEach { deleteImageFromInternalStorage(it.imageUri) }
        vm.bulkDelete(selectedIds.toList())
        onClearSelection()
    }) {
        Icon(Icons.Default.Delete, contentDescription = deleteSelected)
    }

    IconButton(onClick = {
        vm.bulkArchive(selectedIds.toList(), true)
        onClearSelection()
    }) {
        Icon(Icons.Default.Archive, contentDescription = archiveSelected)
    }

    IconButton(onClick = { showCategoryMenu.value = true }) {
        Icon(Icons.Default.Category, contentDescription = setCategory)
    }
}
