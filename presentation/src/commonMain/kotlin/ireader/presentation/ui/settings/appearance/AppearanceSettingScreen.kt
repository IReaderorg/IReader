package ireader.presentation.ui.settings.appearance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.theme.Theme
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.Build
import ireader.presentation.ui.component.components.ChoicePreference
import ireader.presentation.ui.component.components.ColorPickerDialog
import ireader.presentation.ui.component.components.ColorPickerInfo
import ireader.presentation.ui.component.components.ColorPreference
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.Divider
import ireader.presentation.ui.component.components.LazyColumnWithInsets
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.theme.isLight
import kotlinx.coroutines.launch

@Composable
fun AppearanceSettingScreen(
        modifier: Modifier = Modifier,
        onPopBackStack: () -> Unit,
        saveDarkModePreference: (PreferenceValues.ThemeMode) -> Unit,
        vm: AppearanceViewModel,
        scaffoldPaddingValues: PaddingValues,
        onColorChange: () -> Unit,
        onColorReset: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val customizedColors = vm.getCustomizedColors()
    val systemTheme = isSystemInDarkTheme()
    val isLight = remember(vm.themeMode.value) {
        if (vm.themeMode.value == PreferenceValues.ThemeMode.System) {
            !systemTheme
        } else {
            vm.themeMode.value == PreferenceValues.ThemeMode.Light
        }
    }

    val scope = rememberCoroutineScope()
    val themesForCurrentMode = remember(vm.themeMode.value, vm.vmThemes.size, isLight) {
        if (isLight)
            vm.vmThemes.filter { !it.isDark }
        else
            vm.vmThemes.filter { it.isDark }
    }
    var showColorDialog = remember {
        mutableStateOf(false)
    }
    var colorPickerInfo by remember {
        mutableStateOf(ColorPickerInfo())
    }
    
    // Theme export state
    var showThemeExport by remember { mutableStateOf(false) }
    var themeToExport by remember { mutableStateOf("") }
    
    OnShowThemeExport(
        show = showThemeExport,
        themeJson = themeToExport,
        onFileSelected = { success ->
            showThemeExport = false
            if (success) {
                vm.showSnackBar(UiText.DynamicString("Theme exported successfully"))
            } else {
                vm.showSnackBar(UiText.DynamicString("Failed to export theme"))
            }
        }
    )

    // Calculate responsive grid size based on screen width
    // Using a default size that works well across platforms
    val gridMinSize = 180.dp
    
    // Separate themes by light and dark
    val lightThemes = remember(vm.vmThemes.size) {
        vm.vmThemes.filter { !it.isDark }
    }
    val darkThemes = remember(vm.vmThemes.size) {
        vm.vmThemes.filter { it.isDark }
    }

    LazyColumnWithInsets(scaffoldPaddingValues) {
        // Dynamic Colors Section
        item {
            Components.Switch(
                preference = vm.dynamicColorMode,
                title = "Material You (Dynamic Colors)",
                subtitle = "Adapt colors from your wallpaper (Android 12+)",
            ).Build()
        }
        item {
            Components.Switch(
                preference = vm.useTrueBlack,
                title = "Use True Black (AMOLED)",
                subtitle = "Pure black backgrounds for power saving on AMOLED screens",
            ).Build()
        }
        
        // Section Divider
        item {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        
        // Font Customization Section
        item {
            Components.Header(
                    text = "Font Customization",
            ).Build()
        }
        item {
            Components.Dynamic {
                ChoicePreference<String>(
                        preference = vm.appUiFont,
                        choices = vm.availableFonts,
                        title = "App UI Font",
                        subtitle = "Font used for all app interface elements (not reader)",
                )
            }.Build()
        }
        
        // Section Divider
        item {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        
        // Light Themes Section
        item {
            Components.Dynamic {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Light Themes",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Text(
                        text = "${lightThemes.size} themes available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }.Build()
        }
        item {
            Components.Dynamic {
                // Calculate required height based on number of items and grid configuration
                val itemHeight = gridMinSize + 16.dp // item height + spacing
                val columns = 2 // Minimum columns for adaptive grid
                val rows = (lightThemes.size + columns - 1) / columns
                val gridHeight = (itemHeight * rows) + 16.dp // Add padding
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = gridMinSize),
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(gridHeight)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(8.dp),
                    userScrollEnabled = false
                ) {
                    items(items = lightThemes, key = { it.id }) { theme ->
                        ThemeCard(
                            theme = theme,
                            onClick = { selectedTheme ->
                                vm.colorTheme.value = selectedTheme.id
                                customizedColors.primaryState.value = selectedTheme.materialColors.primary
                                customizedColors.secondaryState.value = selectedTheme.materialColors.secondary
                                customizedColors.barsState.value = selectedTheme.extraColors.bars
                                vm.isSavable = false
                                // Auto-switch to light mode
                                vm.saveNightModePreferences(PreferenceValues.ThemeMode.Light)
                            },
                            isSelected = vm.colorTheme.value == theme.id,
                            gridMinSize = gridMinSize
                        )
                    }
                }
            }.Build()
        }
        
        // Section Divider
        item {
            Divider(modifier = Modifier.padding(vertical = 24.dp))
        }
        
        // Dark Themes Section
        item {
            Components.Dynamic {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Dark Themes",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Text(
                        text = "${darkThemes.size} themes available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }.Build()
        }
        item {
            Components.Dynamic {
                // Calculate required height based on number of items and grid configuration
                val itemHeight = gridMinSize + 16.dp // item height + spacing
                val columns = 2 // Minimum columns for adaptive grid
                val rows = (darkThemes.size + columns - 1) / columns
                val gridHeight = (itemHeight * rows) + 16.dp // Add padding
                
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = gridMinSize),
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(gridHeight)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(8.dp),
                    userScrollEnabled = false
                ) {
                    items(items = darkThemes, key = { it.id }) { theme ->
                        ThemeCard(
                            theme = theme,
                            onClick = { selectedTheme ->
                                vm.colorTheme.value = selectedTheme.id
                                customizedColors.primaryState.value = selectedTheme.materialColors.primary
                                customizedColors.secondaryState.value = selectedTheme.materialColors.secondary
                                customizedColors.barsState.value = selectedTheme.extraColors.bars
                                vm.isSavable = false
                                // Auto-switch to dark mode
                                vm.saveNightModePreferences(PreferenceValues.ThemeMode.Dark)
                            },
                            isSelected = vm.colorTheme.value == theme.id,
                            gridMinSize = gridMinSize
                        )
                    }
                }
            }.Build()
        }
        
        // Section Divider
        item {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        
        // Color Customization Section
        item {
            Components.Header(
                    text = "Color Customization",
            ).Build()
        }
        item {
            Components.Dynamic {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = "Customize individual colors to create your own theme",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    
                    // Real-time color preview card
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        shape = MaterialTheme.shapes.medium,
                        tonalElevation = 4.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Primary color preview
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = customizedColors.primaryState.value,
                                    tonalElevation = 2.dp
                                ) {}
                                Text(
                                    text = "Primary",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            
                            // Secondary color preview
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = customizedColors.secondaryState.value,
                                    tonalElevation = 2.dp
                                ) {}
                                Text(
                                    text = "Secondary",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            
                            // Bars color preview
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    modifier = Modifier.size(48.dp),
                                    shape = MaterialTheme.shapes.small,
                                    color = customizedColors.barsState.value,
                                    tonalElevation = 2.dp
                                ) {}
                                Text(
                                    text = "Toolbar",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                        preference = customizedColors.primaryState,
                        title = "Color primary",
                        subtitle = "Displayed most frequently across your app",
                        unsetColor = MaterialTheme.colorScheme.primary,
                        onChangeColor = onColorChange,
                        onRestToDefault = onColorReset,
                        showColorDialog = showColorDialog,
                        onShow = {
                            colorPickerInfo = it
                        }
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                        preference = customizedColors.secondaryState,
                        title = "Color secondary",
                        subtitle = "Accents select parts of the UI",
                        unsetColor = MaterialTheme.colorScheme.secondary,
                        onChangeColor = onColorChange,
                        onRestToDefault = onColorReset,
                        showColorDialog = showColorDialog,
                        onShow = {
                            colorPickerInfo = it
                        }
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                ColorPreference(
                        preference = customizedColors.barsState,
                        title = "Toolbar color",
                        unsetColor = AppColors.current.bars,
                        onChangeColor = onColorChange,
                        onRestToDefault = onColorReset,
                        showColorDialog = showColorDialog,
                        onShow = {
                            colorPickerInfo = it
                        }
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 2.dp
                ) {
                    Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                    ) {
                        if (vm.isSavable) {
                            Button(
                                onClick = {
                                    vm.isSavable = false
                                    scope.launchIO {
                                        val theme = vm.getThemes(vm.colorTheme.value, isLight)
                                        if (theme != null) {
                                            scope.launchIO {
                                                val themeId =
                                                        vm.themeRepository.insert(theme.toCustomTheme())
                                                vm.colorTheme.value = themeId
                                                vm.showSnackBar(UiText.MStringResource(Res.string.theme_was_saved))
                                            }
                                        } else {
                                            vm.showSnackBar(UiText.MStringResource(Res.string.theme_was_not_valid))
                                        }
                                        vm.isSavable = false
                                    }
                                },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                MidSizeTextComposable(text = localizeHelper.localize(Res.string.save_custom_theme))
                            }
                        } else if (vm.colorTheme.value > 0) {
                            TextButton(onClick = {
                                scope.launchIO {
                                    scope.launch {
                                        vm.vmThemes.find { it.id == vm.colorTheme.value }
                                                ?.toCustomTheme()
                                                ?.let { vm.themeRepository.delete(it) }
                                    }
                                    vm.showSnackBar(UiText.MStringResource(Res.string.theme_was_deleted))
                                }
                            }) {
                                MidSizeTextComposable(text = localizeHelper.localize(Res.string.delete_custom_theme))
                            }
                        }
                    }
                }
            }.Build()
        }
        
        // Section Divider
        item {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        
        // Theme Management Section
        item {
            Components.Header(
                    text = "Theme Management",
            ).Build()
        }
        item {
            Components.Dynamic {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Backup and restore your custom themes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Export current theme button
                        Button(
                            onClick = {
                                val exported = vm.exportCurrentTheme()
                                if (exported != null) {
                                    themeToExport = exported
                                    showThemeExport = true
                                } else {
                                    vm.showSnackBar(UiText.DynamicString("No custom theme selected"))
                                }
                            },
                            modifier = Modifier.weight(1f),
                            enabled = vm.colorTheme.value > 0
                        ) {
                            Text("Export Theme")
                        }
                        
                        // Export all themes button
                        Button(
                            onClick = {
                                themeToExport = vm.exportAllCustomThemes()
                                showThemeExport = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Backup All")
                        }
                    }
                    
                    // Import theme button
                    Button(
                        onClick = {
                            vm.showImportDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Import Theme")
                    }
                }
            }.Build()
        }
        
        // Section Divider
        item {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        
        // Novel Info Section
        item {
            Components.Header(
                    text = "Novel Info",
            ).Build()
        }
        item {
            // TODO: Fix preference binding when API is stable
            // Components.Switch(
            //     preference = vm.uiPreferences.hideNovelBackdrop(),
            //     title = "Hide Backdrop",
            //     subtitle = "Hide background images on novel detail screens for cleaner look and better performance",
            // ).Build()
        }
        item {
            // TODO: Fix preference binding when API is stable
            // Components.Switch(
            //     preference = vm.uiPreferences.useFabInNovelInfo(),
            //     title = "Use FAB Instead of Buttons",
            //     subtitle = "Replace standard action buttons with floating action button",
            // ).Build()
        }
        
        // Section Divider
        item {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        
        // Timestamp Section
        item {
            Components.Header(
                    text = "Date & Time",
            ).Build()
        }
        item {
            Components.Dynamic {
                ChoicePreference<PreferenceValues.RelativeTime>(
                        preference = vm.relativeTime,
                        choices = vm.relativeTimes.associateWith { value ->
                            when (value) {
                                PreferenceValues.RelativeTime.Off -> localizeHelper.localize(Res.string.off)
                                PreferenceValues.RelativeTime.Day -> localizeHelper.localize(Res.string.pref_relative_time_short)
                                PreferenceValues.RelativeTime.Week -> localizeHelper.localize(Res.string.pref_relative_time_long)
                                else -> localizeHelper.localize(Res.string.off)
                            }
                        },
                        title = localizeHelper.localize(Res.string.pref_relative_format),
                        subtitle = null,
                )
            }.Build()
        }


    }
    if (showColorDialog.value) {
        ColorPickerDialog(
                title = { Text(colorPickerInfo.title ?: "") },
                onDismissRequest = { showColorDialog.value = false },
                onSelected = {
                    colorPickerInfo.preference?.value = it
                    showColorDialog.value = false
                    colorPickerInfo.onChangeColor()
                },
                initialColor = colorPickerInfo.initialColor,
        )
    }
    

    
    // Import dialog
    if (vm.showImportDialog) {
        var importText by remember { mutableStateOf("") }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { vm.showImportDialog = false },
            title = { Text("Import Theme") },
            text = {
                Column {
                    Text("Paste the theme JSON below:")
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.requiredHeight(8.dp))
                    androidx.compose.material3.OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(200.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        ),
                        placeholder = { Text("Paste JSON here...") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launchIO {
                            val result = vm.importTheme(importText)
                            if (result.isSuccess) {
                                vm.showSnackBar(UiText.DynamicString("Theme imported successfully"))
                                vm.showImportDialog = false
                            } else {
                                vm.showSnackBar(UiText.DynamicString("Failed to import theme: ${result.exceptionOrNull()?.message}"))
                            }
                        }
                    },
                    enabled = importText.isNotBlank()
                ) {
                    Text("Import")
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.showImportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun getThemeName(themeId: Long): String {
    return when (themeId) {
        -1L -> "Tachiyomi Light"
        -2L -> "Tachiyomi Dark"
        -3L -> "Blue Light"
        -4L -> "Blue Dark"
        -5L -> "Midnight Light"
        -6L -> "Midnight Dark"
        -7L -> "Green Apple Light"
        -8L -> "Green Apple Dark"
        -9L -> "Strawberries Light"
        -10L -> "Strawberries Dark"
        -11L -> "Tako Light"
        -12L -> "Tako Dark"
        -13L -> "Ocean Blue Light"
        -14L -> "Ocean Blue Dark"
        -15L -> "Sunset Orange Light"
        -16L -> "Sunset Orange Dark"
        -17L -> "Lavender Purple Light"
        -18L -> "Lavender Purple Dark"
        -19L -> "Forest Green Light"
        -20L -> "Forest Green Dark"
        -21L -> "Monochrome Light"
        -22L -> "Monochrome Dark"
        -23L -> "Cherry Blossom Light"
        -24L -> "Cherry Blossom Dark"
        -25L -> "Midnight Sky Light"
        -26L -> "Midnight Sky Dark"
        -27L -> "Autumn Harvest Light"
        -28L -> "Autumn Harvest Dark"
        -29L -> "Emerald Forest Light"
        -30L -> "Emerald Forest Dark"
        -31L -> "Rose Gold Light"
        -32L -> "Rose Gold Dark"
        else -> "Custom Theme"
    }
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ThemeCard(
        theme: Theme,
        onClick: (Theme) -> Unit,
        onLongClick: (Theme) -> Unit = {},
        isSelected: Boolean = false,
        gridMinSize: Dp = 160.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    // Animated elevation for selection and hover states
    val elevation by animateDpAsState(
        targetValue = when {
            isSelected -> 8.dp
            isHovered -> 4.dp
            else -> 2.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    // Animated scale for selection
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    // Animated border color
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f / 1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(
                width = 3.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .hoverable(interactionSource = interactionSource)
            .combinedClickable(
                onClick = { onClick(theme) },
                onLongClick = { onLongClick(theme) }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = theme.materialColors.background
        ),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = elevation
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                // Theme preview area with 3-color gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    // Gradient background showing primary, secondary, tertiary
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Transparent
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    clip = true
                                    shape = RoundedCornerShape(8.dp)
                                }
                        ) {
                            // Three color sections
                            Row(modifier = Modifier.fillMaxSize()) {
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize(),
                                    color = theme.materialColors.primary
                                ) {}
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize(),
                                    color = theme.materialColors.secondary
                                ) {}
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxSize(),
                                    color = theme.materialColors.tertiary
                                ) {}
                            }
                            
                            // Sample text overlay
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Aa",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = theme.materialColors.onPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = theme.materialColors.surfaceVariant.copy(alpha = 0.9f)
                                ) {
                                    Text(
                                        text = "Sample",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = theme.materialColors.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Theme name
                Text(
                    text = getThemeName(theme.id),
                    style = MaterialTheme.typography.titleSmall,
                    color = theme.materialColors.onBackground,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Selection checkmark indicator
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = androidx.compose.animation.scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy
                    )
                ) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
            ) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(percent = 50),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    tonalElevation = 2.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}
