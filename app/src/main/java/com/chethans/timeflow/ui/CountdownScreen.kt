package com.chethans.timeflow.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.livedata.observeAsState
import com.chethans.timeflow.R
import com.chethans.timeflow.data.CountdownEntity
import com.chethans.timeflow.ui.components.AddTimerDialog
import com.chethans.timeflow.ui.components.CategoryOptions
import com.chethans.timeflow.ui.components.CountdownDetailsDialog
import com.chethans.timeflow.ui.components.CardLayoutMode
import com.chethans.timeflow.ui.components.EditTimerDialog
import com.chethans.timeflow.ui.components.HamburgerMenuContent
import com.chethans.timeflow.ui.components.SortMenuDropdown
import com.chethans.timeflow.ui.components.TabItem
import com.chethans.timeflow.ui.components.timerDialogColors
import com.chethans.timeflow.ui.theme.ThemePreset
import com.chethans.timeflow.util.copyImageToInternalStorage
import com.chethans.timeflow.util.deleteImageFromInternalStorage
import com.chethans.timeflow.vm.CountdownViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountdownScreen(vm: CountdownViewModel = viewModel()) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val widgetAddManuallyText = stringResource(R.string.widget_add_manually)
    val list by vm.countdowns.observeAsState(emptyList())
    val activeWidgetId by vm.activeWidgetId.collectAsState()
    val hasAnyWidget by vm.hasWidgetInstance.collectAsState()
    val widgetBackgroundMode by vm.widgetBackgroundMode.collectAsState()

    val prefs = remember { context.getSharedPreferences("ui_prefs", android.content.Context.MODE_PRIVATE) }

    var currentTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var selectedTab by remember { mutableStateOf("Active") }
    val swipeThresholdPx = with(LocalDensity.current) { 72.dp.toPx() }
    var horizontalDragAccum by remember { mutableStateOf(0f) }

    var showDialog by remember { mutableStateOf(false) }
    var selectedCountdown by remember { mutableStateOf<CountdownEntity?>(null) }
    var pendingDeleteItem by remember { mutableStateOf<CountdownEntity?>(null) }
    var pendingDeleteFromDetails by remember { mutableStateOf(false) }
    var editingCountdown by remember { mutableStateOf<CountdownEntity?>(null) }
    var countdownBeforeEdit by remember { mutableStateOf<CountdownEntity?>(null) }
    var title by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf("") }
    var selectedDateTime by remember { mutableStateOf(System.currentTimeMillis() + 60_000L) }
    var selectedColorIndex by remember { mutableStateOf<Int?>(null) }
    var repeatYearly by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf(prefs.getString("search_query", "") ?: "") }
    var showArchived by remember { mutableStateOf(prefs.getBoolean("show_archived", false)) }
    var sortAscending by remember { mutableStateOf(prefs.getBoolean("sort_ascending", true)) }
    var largeText by remember { mutableStateOf(prefs.getBoolean("large_text", false)) }
    var highContrast by remember { mutableStateOf(prefs.getBoolean("high_contrast", false)) }
    var cardLayoutMode by remember {
        mutableStateOf(CardLayoutMode.fromStorage(prefs.getString("card_layout_mode", CardLayoutMode.SINGLE_COLUMN.name)))
    }
    var includeImagesInBackup by remember { mutableStateOf(prefs.getBoolean("backup_include_images", false)) }
    var replaceDataOnImport by remember { mutableStateOf(prefs.getBoolean("backup_replace_on_import", true)) }
    var themePreset by remember {
        mutableStateOf(
            ThemePreset.entries.getOrElse(prefs.getInt("theme_preset", 0)) { ThemePreset.CLASSIC }
        )
    }

    var showOptionsMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val selectedIds = remember { mutableStateListOf<Int>() }
    val selectionMode = selectedIds.isNotEmpty()
    val scope = rememberCoroutineScope()

    val exportBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        vm.exportBackup(uri, includeImagesInBackup) { result ->
            Toast.makeText(
                context,
                if (result.success) "${result.message} (${result.count})" else result.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        }
        vm.importBackup(uri, replaceDataOnImport) { result ->
            Toast.makeText(
                context,
                if (result.success) "${result.message} (${result.count})" else result.message,
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun clearSelection() = selectedIds.clear()

    fun requestDelete(item: CountdownEntity, fromDetails: Boolean) {
        pendingDeleteItem = item
        pendingDeleteFromDetails = fromDetails
    }

    fun confirmDelete() {
        val itemToDelete = pendingDeleteItem ?: return
        deleteImageFromInternalStorage(itemToDelete.imageUri)
        vm.delete(itemToDelete)
        if (pendingDeleteFromDetails) {
            selectedCountdown = null
        }
        pendingDeleteItem = null
        pendingDeleteFromDetails = false
    }

    fun dismissDeleteDialog() {
        pendingDeleteItem = null
        pendingDeleteFromDetails = false
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                currentTime = System.currentTimeMillis()
                vm.syncWidgetPresence()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = System.currentTimeMillis()
            val delayMillis = (1000L - (currentTime % 1000L)).coerceAtLeast(1L)
            delay(delayMillis)
        }
    }

    val isDark = isSystemInDarkTheme()
    val (sandGradient, backgroundGradient, tabContainerColor) = when (themePreset) {
        ThemePreset.CLASSIC -> Triple(
            if (isDark) Brush.horizontalGradient(listOf(Color(0xFF1A1A2E), Color(0xFF16213E)))
            else Brush.horizontalGradient(listOf(Color(0xFFE2D1C3), Color(0xFFC9B09A))),
            if (isDark) Brush.verticalGradient(listOf(Color(0xFF2A2A3E), Color(0xFF1A1A2E)))
            else Brush.verticalGradient(listOf(Color(0xFFC6AA92), Color(0xFF9D7F66))),
            if (isDark) Color(0xFF2A2A3E).copy(alpha = 0.9f) else Color(0xFF7F6654).copy(alpha = 0.55f)
        )
        ThemePreset.MINIMAL -> Triple(
            if (isDark) Brush.horizontalGradient(listOf(Color(0xFF121212), Color(0xFF1E1E1E)))
            else Brush.horizontalGradient(listOf(Color(0xFFF2F2F2), Color(0xFFE6E6E6))),
            if (isDark) Brush.verticalGradient(listOf(Color(0xFF2C2C2C), Color(0xFF1A1A1A)))
            else Brush.verticalGradient(listOf(Color(0xFF666666), Color(0xFF555555))),
            if (isDark) Color(0xFF2B2B2B).copy(alpha = 0.92f) else Color(0xFF777777).copy(alpha = 0.75f)
        )
    }

    val classicLightMode = !isDark && themePreset == ThemePreset.CLASSIC
    val activeTabOverlayAlpha = if (classicLightMode) 0.32f else 0.5f
    val addFabOverlayAlpha = if (classicLightMode) 0.22f else 0.35f

    val allGradients = remember { getAllCountdownGradients() }

    val filteredItems = remember(list, searchQuery, showArchived, sortAscending) {
        val filtered = list.filter { item ->
            val searchMatches = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
            val archiveMatches = showArchived || !item.isArchived
            searchMatches && archiveMatches
        }
        if (sortAscending) filtered.sortedBy { it.targetTime } else filtered.sortedByDescending { it.targetTime }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(sandGradient)
    ) {
        Scaffold(
            modifier = Modifier.blur(if (selectedCountdown != null) 14.dp else 0.dp),
            containerColor = Color.Transparent,
            floatingActionButton = {
                if (!selectionMode) {
                    // ...existing code...
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(backgroundGradient)
                            .border(
                                width = 1.5.dp,
                                color = Color.Black.copy(alpha = 0.35f),
                                shape = CircleShape
                            )
                            .background(Color.White.copy(alpha = addFabOverlayAlpha)),
                        contentAlignment = Alignment.Center
                    ) {
                        FloatingActionButton(
                            onClick = {
                                editingCountdown = null
                                title = ""
                                imageUri = ""
                                selectedDateTime = System.currentTimeMillis() + 60_000L
                                selectedColorIndex = null
                                repeatYearly = false
                                showDialog = true
                            },
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            shape = CircleShape,
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 0.dp,
                                pressedElevation = 0.dp
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.add_new_timer),
                                modifier = Modifier.size(30.dp),
                                tint = Color.White
                            )
                        }
                    }
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            bottomBar = {
                Surface(
                    color = tabContainerColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TabItem(
                            title = stringResource(R.string.tab_active),
                            icon = Icons.Default.Timer,
                            isSelected = selectedTab == "Active",
                            activeOverlayAlpha = activeTabOverlayAlpha,
                            onClick = {
                                selectedTab = "Active"
                            },
                            gradient = backgroundGradient,
                            modifier = Modifier.weight(1f)
                        )
                        TabItem(
                            title = stringResource(R.string.tab_finished),
                            icon = Icons.Default.History,
                            isSelected = selectedTab == "Finished",
                            activeOverlayAlpha = activeTabOverlayAlpha,
                            onClick = {
                                selectedTab = "Finished"
                            },
                            gradient = backgroundGradient,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        if (selectionMode) {
                            Text(
                                text = stringResource(R.string.selected_count, selectedIds.size),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDark) Color(0xFFE0D6CC) else Color(0xFF333333)
                                )
                            )
                        } else {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = {
                                    searchQuery = it
                                    prefs.edit().putString("search_query", it).apply()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                placeholder = { Text(stringResource(R.string.search_by_title_or_category), color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF777777)) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = if (isDark) Color(0xFFE0E0E0) else Color(0xFF333333)) },
                                singleLine = true,
                                shape = RoundedCornerShape(20.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = if (isDark) Color(0xFFE0E0E0) else Color(0xFF222222),
                                    unfocusedTextColor = if (isDark) Color(0xFFE0E0E0) else Color(0xFF222222),
                                    focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.Black.copy(alpha = 0.08f),
                                    unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.06f),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { showOptionsMenu = !showOptionsMenu }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = stringResource(R.string.menu),
                                modifier = Modifier.size(28.dp),
                                tint = if (isDark) Color(0xFFE0E0E0) else Color(0xFF222222)
                            )
                        }
                    },
                    actions = {
                        if (selectionMode) {
                            IconButton(onClick = {
                                val selectedItems = filteredItems.filter { it.id in selectedIds }
                                selectedItems.forEach { deleteImageFromInternalStorage(it.imageUri) }
                                vm.bulkDelete(selectedIds.toList())
                                clearSelection()
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete_selected))
                            }
                            IconButton(onClick = {
                                vm.bulkArchive(selectedIds.toList(), true)
                                clearSelection()
                            }) {
                                Icon(Icons.Default.Archive, contentDescription = stringResource(R.string.archive_selected))
                            }
                            IconButton(onClick = { showCategoryMenu = true }) {
                                Icon(Icons.Default.Category, contentDescription = stringResource(R.string.set_category_for_selected))
                            }
                            DropdownMenu(
                                expanded = showCategoryMenu,
                                onDismissRequest = { showCategoryMenu = false }
                            ) {
                                CategoryOptions.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text(category) },
                                        onClick = {
                                            vm.bulkSetCategory(selectedIds.toList(), category)
                                            showCategoryMenu = false
                                            clearSelection()
                                        }
                                    )
                                }
                            }
                        } else {
                            Box {
                                IconButton(
                                    onClick = {
                                        showSortMenu = !showSortMenu
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Sort,
                                        contentDescription = stringResource(R.string.sort_options),
                                        tint = if (isDark) Color(0xFFE0E0E0) else Color(0xFF222222)
                                    )
                                }
                                SortMenuDropdown(
                                    expanded = showSortMenu,
                                    onDismiss = { showSortMenu = false },
                                    sortAscending = sortAscending,
                                    onSortAscending = { sortAscending = it },
                                    isDark = isDark,
                                    prefs = prefs
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
            ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .pointerInput(selectedTab) {
                        detectHorizontalDragGestures(
                            onDragStart = { horizontalDragAccum = 0f },
                            onHorizontalDrag = { _, dragAmount ->
                                horizontalDragAccum += dragAmount
                            },
                            onDragEnd = {
                                when {
                                    selectedTab == "Active" && horizontalDragAccum < -swipeThresholdPx -> {
                                        selectedTab = "Finished"
                                    }

                                    selectedTab == "Finished" && horizontalDragAccum > swipeThresholdPx -> {
                                        selectedTab = "Active"
                                    }
                                }
                                horizontalDragAccum = 0f
                            },
                            onDragCancel = { horizontalDragAccum = 0f }
                        )
                    }
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (initialState == "Active" && targetState == "Finished") {
                            slideInHorizontally(animationSpec = tween(260)) { it } togetherWith
                                slideOutHorizontally(animationSpec = tween(260)) { -it }
                        } else {
                            slideInHorizontally(animationSpec = tween(260)) { -it } togetherWith
                                slideOutHorizontally(animationSpec = tween(260)) { it }
                        }
                    },
                    label = "tabContent"
                ) { tab ->
                    val tabItems = if (tab == "Active") {
                        filteredItems.filter { it.targetTime > currentTime }
                    } else {
                        filteredItems.filter { it.targetTime <= currentTime }
                    }

                    if (tabItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 36.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (tab == "Active") {
                                        stringResource(R.string.no_active_countdowns)
                                    } else {
                                        stringResource(R.string.no_finished_countdowns)
                                    },
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isDark)
                                            Color(0xFF8899AA).copy(alpha = 0.8f)
                                        else
                                            Color(0xFF6B4E3C).copy(alpha = 0.65f)
                                    ),
                                    textAlign = TextAlign.Center
                                )
                                if (tab == "Active" && searchQuery.isBlank()) {
                                    TextButton(
                                        onClick = {
                                            showDialog = true
                                            editingCountdown = null
                                            title = ""
                                            imageUri = ""
                                            selectedColorIndex = null
                                            repeatYearly = false
                                            selectedDateTime = System.currentTimeMillis() + 60_000L
                                        }
                                    ) {
                                        Text(stringResource(R.string.create_your_first_countdown))
                                    }
                                }
                            }
                        }
                    } else {
                        val cardContent: @Composable (CountdownEntity) -> Unit = { item ->
                            AnimatedVisibility(
                                visible = true,
                                enter = slideInVertically(animationSpec = tween(260)) { it / 6 } + fadeIn(tween(220)),
                                exit = fadeOut(tween(140))
                            ) {
                                CountdownCard(
                                    item = item,
                                    activeWidgetId = activeWidgetId,
                                    currentTime = currentTime,
                                    onDelete = {
                                        requestDelete(item, fromDetails = false)
                                    },
                                    onSetWidget = {
                                        vm.setActiveWidget(item.id)
                                        if (!vm.hasAnyWidgetInstance()) {
                                            if (!vm.requestPinWidgetIfNeeded()) {
                                                Toast.makeText(context, widgetAddManuallyText, Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (selectionMode) {
                                            if (item.id in selectedIds) selectedIds.remove(item.id) else selectedIds.add(item.id)
                                        } else {
                                            selectedCountdown = item
                                        }
                                    },
                                    onLongClick = {
                                        if (item.id !in selectedIds) {
                                            selectedIds.add(item.id)
                                        }
                                    },
                                    isSelected = item.id in selectedIds,
                                    textScale = if (largeText) 1.12f else 1f,
                                    highContrast = highContrast,
                                    hasAnyWidgetInstance = hasAnyWidget,
                                    isGridMode = cardLayoutMode == CardLayoutMode.TWO_COLUMN
                                )
                            }
                        }

                        if (cardLayoutMode == CardLayoutMode.TWO_COLUMN) {
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = 170.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                gridItems(tabItems, key = { it.id }) { item ->
                                    cardContent(item)
                                }
                            }
                        } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(tabItems, key = { it.id }) { item ->
                                cardContent(item)
                            }
                        }
                        }
                    }
                }
            }
        }

        // Drawer overlay - rendered on top of everything
        HamburgerMenuContent(
            showMenu = showOptionsMenu,
            onDismiss = { showOptionsMenu = false },
            themePreset = themePreset,
            largeText = largeText,
            highContrast = highContrast,
            cardLayoutMode = cardLayoutMode,
            widgetBackgroundMode = widgetBackgroundMode,
            includeImagesInBackup = includeImagesInBackup,
            replaceOnImport = replaceDataOnImport,
            onThemeChange = { themePreset = it },
            onLargeTextChange = { largeText = it },
            onHighContrastChange = { highContrast = it },
            onCardLayoutModeChange = { mode ->
                cardLayoutMode = mode
                prefs.edit().putString("card_layout_mode", mode.name).apply()
            },
            onWidgetBackgroundModeChange = { mode -> vm.setWidgetBackgroundMode(mode) },
            onIncludeImagesInBackupChange = {
                includeImagesInBackup = it
                prefs.edit().putBoolean("backup_include_images", it).apply()
            },
            onReplaceOnImportChange = {
                replaceDataOnImport = it
                prefs.edit().putBoolean("backup_replace_on_import", it).apply()
            },
            onExportBackup = {
                exportBackupLauncher.launch("countdown-backup-${System.currentTimeMillis()}.json")
            },
            onImportBackup = {
                importBackupLauncher.launch(arrayOf("application/json", "text/json", "text/plain"))
            },
            prefs = prefs
        )
    }

    selectedCountdown?.let { item ->
        CountdownDetailsDialog(
            item = item,
            activeWidgetId = activeWidgetId,
            currentTime = currentTime,
            onDismiss = { selectedCountdown = null },
            onDelete = {
                requestDelete(item, fromDetails = true)
            },
            onSetWidget = {
                vm.setActiveWidget(item.id)
                if (!vm.hasAnyWidgetInstance()) {
                    if (!vm.requestPinWidgetIfNeeded()) {
                        Toast.makeText(context, widgetAddManuallyText, Toast.LENGTH_LONG).show()
                    }
                }
            },
            onEdit = {
                countdownBeforeEdit = item
                editingCountdown = item
                title = item.title
                imageUri = item.imageUri ?: ""
                selectedDateTime = item.targetTime
                selectedColorIndex = item.colorIndex
                repeatYearly = item.repeatYearly
                selectedCountdown = null
                showDialog = true
            },
            largeText = largeText
        )
    }

    val addDialogColors = remember(isDark) {
        timerDialogColors(themePreset = ThemePreset.MINIMAL, isDark = isDark)
    }

    val editDialogColors = remember(themePreset, isDark) {
        timerDialogColors(themePreset = themePreset, isDark = isDark)
    }

    val addDialogDefaultGradient = remember(isDark) {
        if (isDark) listOf(Color(0xFF2F353F), Color(0xFF272D36))
        else listOf(Color(0xFFF6F7F9), Color(0xFFE6EAF0))
    }

    if (editingCountdown == null) {
        AddTimerDialog(
            showDialog = showDialog,
            title = title,
            selectedDateTime = selectedDateTime,
            selectedColorIndex = selectedColorIndex,
            gradientColors = if (selectedColorIndex != null) {
                getCountdownGradient(selectedColorIndex ?: 0)
            } else {
                addDialogDefaultGradient
            },
            allGradients = allGradients,
            imageUri = imageUri,
            onTitleChange = { title = it },
            onPickDate = {
                showDatePicker(context, selectedDateTime) { pickedDateMillis ->
                    selectedDateTime = pickedDateMillis
                }
            },
            onPickTime = {
                showTimePicker(context, selectedDateTime, selectedDateTime) { pickedDateTimeMillis ->
                    selectedDateTime = pickedDateTimeMillis
                }
            },
            onImageSelected = { pickedImageUri ->
                if (imageUri.isNotBlank()) deleteImageFromInternalStorage(imageUri)
                imageUri = if (pickedImageUri != null) {
                    selectedColorIndex = null
                    copyImageToInternalStorage(context, pickedImageUri)
                } else {
                    ""
                }
            },
            onSelectColor = { pickedColorIndex ->
                selectedColorIndex = pickedColorIndex
            },
            repeatYearly = repeatYearly,
            onRepeatYearlyChange = { repeatYearly = it },
            onDismiss = {
                showDialog = false
                selectedColorIndex = null
                repeatYearly = false
            },
            onConfirm = {
                vm.add(
                    title,
                    selectedDateTime,
                    if (imageUri.isBlank()) null else imageUri,
                    selectedColorIndex,
                    repeatYearly
                )
                title = ""
                imageUri = ""
                selectedColorIndex = null
                repeatYearly = false
                showDialog = false
            },
            largeText = largeText,
            dialogColors = addDialogColors
        )
    } else {
        val editingItem = editingCountdown
        EditTimerDialog(
            showDialog = showDialog,
            title = title,
            selectedDateTime = selectedDateTime,
            selectedColorIndex = selectedColorIndex,
            gradientColors = getCountdownGradient(selectedColorIndex ?: (editingItem?.colorIndex ?: 0)),
            allGradients = allGradients,
            imageUri = imageUri,
            onTitleChange = { title = it },
            onPickDate = {
                showDatePicker(context, selectedDateTime) { pickedDateMillis ->
                    selectedDateTime = pickedDateMillis
                }
            },
            onPickTime = {
                showTimePicker(context, selectedDateTime, selectedDateTime) { pickedDateTimeMillis ->
                    selectedDateTime = pickedDateTimeMillis
                }
            },
            onImageSelected = { pickedImageUri ->
                if (imageUri.isNotBlank()) deleteImageFromInternalStorage(imageUri)
                imageUri = if (pickedImageUri != null) {
                    selectedColorIndex = null
                    copyImageToInternalStorage(context, pickedImageUri)
                } else {
                    ""
                }
            },
            onSelectColor = { pickedColorIndex ->
                selectedColorIndex = pickedColorIndex
            },
            repeatYearly = repeatYearly,
            onRepeatYearlyChange = { repeatYearly = it },
            onDismiss = {
                showDialog = false
                editingCountdown = null
                selectedColorIndex = null
                repeatYearly = false
                selectedCountdown = countdownBeforeEdit
                countdownBeforeEdit = null
            },
            onConfirm = {
                val safeEditingItem = editingItem ?: return@EditTimerDialog
                if (safeEditingItem.imageUri != null && safeEditingItem.imageUri != imageUri) {
                    deleteImageFromInternalStorage(safeEditingItem.imageUri)
                }
                val updatedItem = safeEditingItem.copy(
                    title = title,
                    targetTime = selectedDateTime,
                    imageUri = if (imageUri.isBlank()) null else imageUri,
                    colorIndex = selectedColorIndex ?: safeEditingItem.colorIndex,
                    repeatYearly = repeatYearly
                )
                vm.update(updatedItem)
                title = ""
                imageUri = ""
                editingCountdown = null
                selectedColorIndex = null
                repeatYearly = false
                showDialog = false
                selectedCountdown = updatedItem
                countdownBeforeEdit = null
            },
            largeText = largeText,
            dialogColors = editDialogColors
        )
    }

    pendingDeleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { dismissDeleteDialog() },
            containerColor = if (isDark) Color(0xFF2F353F) else Color(0xFFF6F7F9),
            titleContentColor = if (isDark) Color(0xFFF2F3F5) else Color(0xFF1F2329),
            textContentColor = if (isDark) Color(0xFFB9BDC5) else Color(0xFF616A76),
            title = { Text(stringResource(R.string.delete_countdown_title)) },
            text = {
                Text(stringResource(R.string.delete_countdown_message, item.title))
            },
            confirmButton = {
                TextButton(onClick = { confirmDelete() }) {
                    Text(stringResource(R.string.delete), color = Color(0xFFD64A4A))
                }
            },
            dismissButton = {
                TextButton(onClick = { dismissDeleteDialog() }) {
                    Text(stringResource(R.string.cancel), color = if (isDark) Color(0xFFB9BDC5) else Color(0xFF616A76))
                }
            }
        )
    }
}
