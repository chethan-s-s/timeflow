# Import Guide - Refactored Components

## How to Use New Components in Other Files

### 1. **Theme System** - Import from `ui.theme`

```kotlin
import com.example.countdowntimer.ui.theme.ThemePreset
import com.example.countdowntimer.ui.theme.getThemeColors
import com.example.countdowntimer.ui.theme.getOverlayAlphas

// Usage
val isDark = isSystemInDarkTheme()
val themeColors = getThemeColors(ThemePreset.CLASSIC)
val (activeTabAlpha, fabAlpha) = getOverlayAlphas(themePreset)
```

### 2. **Menu Components** - Import from `ui.components`

```kotlin
import com.example.countdowntimer.ui.components.HamburgerMenuContent
import com.example.countdowntimer.ui.components.SelectionModeActions

// Usage - Hamburger Menu
HamburgerMenuContent(
    showMenu = showOptionsMenu,
    onDismiss = { showOptionsMenu = false },
    themePreset = themePreset,
    largeText = largeText,
    highContrast = highContrast,
    onThemeChange = { themePreset = it },
    onLargeTextChange = { largeText = it },
    onHighContrastChange = { highContrast = it },
    prefs = prefs
)

// Usage - Selection Mode Actions
SelectionModeActions(
    selectedIds = selectedIds,
    filteredItems = filteredItems,
    vm = vm,
    showCategoryMenu = remember { mutableStateOf(false) },
    onClearSelection = { selectedIds.clear() }
)
```

### 3. **Sort Menu** - Import from `ui.components`

```kotlin
import com.example.countdowntimer.ui.components.SortMenuDropdown

// Usage
SortMenuDropdown(
    expanded = showSortMenu,
    onDismiss = { showSortMenu = false },
    sortAscending = sortAscending,
    onSortAscending = { sortAscending = it },
    isDark = isDark,
    prefs = prefs
)
```

### 4. **Sort Menu Items** - For custom menus

```kotlin
import com.example.countdowntimer.ui.components.SortMenuItemContent
import com.example.countdowntimer.ui.components.SortMenuItemBackgroundModifier

// Usage
DropdownMenuItem(
    text = {
        SortMenuItemContent(
            label = "Option",
            description = "Description",
            isSelected = true,
            isDark = isDark
        )
    },
    onClick = { /* ... */ },
    modifier = SortMenuItemBackgroundModifier(
        isSelected = true,
        isDark = isDark
    )
)
```

### 5. **Constants** - Import from `ui.components`

```kotlin
import com.example.countdowntimer.ui.components.CategoryOptions

// Usage
CategoryOptions.forEach { category ->
    // "General", "Work", "Personal", "Birthday", "Travel", "Exam"
}
```

## Current Usage in CountdownScreen.kt

```kotlin
// Theme imports
import com.example.countdowntimer.ui.theme.ThemePreset
import com.example.countdowntimer.ui.theme.getOverlayAlphas
import com.example.countdowntimer.ui.theme.getThemeColors

// Component imports
import com.example.countdowntimer.ui.components.CategoryOptions
import com.example.countdowntimer.ui.components.HamburgerMenuContent
import com.example.countdowntimer.ui.components.SelectionModeActions
import com.example.countdowntimer.ui.components.SortMenuDropdown
import com.example.countdowntimer.ui.components.TabItem
```

## File Dependencies

```
CountdownScreen.kt
├── depends on → ThemePreset.kt
├── depends on → HamburgerMenuContent (MenuComposables.kt)
├── depends on → SortMenuDropdown.kt
├── depends on → SortMenuComposables.kt
└── depends on → Constants.kt

MenuComposables.kt
├── depends on → ThemePreset.kt
└── uses → Icons, Material3 components

SortMenuDropdown.kt
├── depends on → SortMenuComposables.kt
└── uses → Material3 components

SortMenuComposables.kt
└── uses → Material3 components

ThemePreset.kt
└── uses → Compose Foundation & Graphics
```

## Testing & Integration Guide

### Adding New Theme
```kotlin
// 1. Add to ThemePreset enum
enum class ThemePreset {
    CLASSIC,
    MINIMAL,
    NEW_THEME  // ← Add here
}

// 2. Add colors in getThemeColors()
ThemePreset.NEW_THEME -> ThemeColors(...)
```

### Adding New Menu Item
```kotlin
// 1. Update MenuComposables.kt HamburgerMenuContent()
// 2. Add new DropdownMenuItem block
// 3. Handle onClick callback
```

### Adding New Sort Option
```kotlin
// 1. Update CountdownScreen.kt filteredItems logic
// 2. Add DropdownMenuItem to SortMenuDropdown()
// 3. Update sort state handling
```

## Best Practices

1. **Always pass callbacks instead of direct state mutations**
   - ❌ Don't: `onThemeChange = { themePreset = it }`
   - ✅ Do: Pass callback to update state in parent

2. **Use composition over large files**
   - Extract components when file exceeds 400 lines
   - Keep related logic together

3. **Centralize theme/style logic**
   - Use `getThemeColors()` for colors
   - Use `getOverlayAlphas()` for transparency

4. **Make components reusable**
   - Accept required parameters
   - Don't assume parent state management

5. **Document component contracts**
   - Clearly define required vs optional params
   - Document callback behaviors

