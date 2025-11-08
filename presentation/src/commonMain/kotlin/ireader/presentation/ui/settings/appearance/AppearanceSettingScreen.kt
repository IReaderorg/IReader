package ireader.presentation.ui.settings.appearance

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.theme.Theme
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.resources.MR
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
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
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

    LazyColumnWithInsets(scaffoldPaddingValues) {
        // Theme Mode Section
        item {
            Components.Header(
                    text = "Theme Mode",
            ).Build()
        }
        item {
            Components.Dynamic {
                ChoicePreference<PreferenceValues.ThemeMode>(
                        preference = vm.themeMode,
                        choices = mapOf(
                                PreferenceValues.ThemeMode.System to localizeHelper.localize(MR.strings.follow_system_settings),
                                PreferenceValues.ThemeMode.Light to localizeHelper.localize(MR.strings.light),
                                PreferenceValues.ThemeMode.Dark to localizeHelper.localize(MR.strings.dark)
                        ),
                        title = localizeHelper.localize(MR.strings.theme),
                        subtitle = null,
                        onValue = {
                            vm.saveNightModePreferences(it)
                        }
                )
            }.Build()
        }
        item {
            Components.Switch(
                preference = vm.dynamicColorMode,
                title = "Material You (Dynamic Colors)",
                subtitle = "Adapt colors from your wallpaper (Android 12+)",
            ).Build()
        }
        
        // Section Divider
        item {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        
        // Preset Themes Section with enhanced header
        item {
            Components.Header(
                    text = "Preset Themes",
            ).Build()
        }
        item {
            Components.Dynamic {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = "Choose from ${themesForCurrentMode.size} available themes",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(items = themesForCurrentMode) { theme ->
                            ThemeItem(
                                    theme,
                                    onClick = { theme ->
                                        vm.colorTheme.value = theme.id
                                        customizedColors.primaryState.value = theme.materialColors.primary
                                        customizedColors.secondaryState.value = theme.materialColors.secondary
                                        customizedColors.barsState.value = theme.extraColors.bars
                                        vm.isSavable = false
                                    },
                                    isSelected = vm.colorTheme.value == theme.id,
                            )
                        }
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
                Text(
                    text = "Customize individual colors to create your own theme",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
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
                                                vm.showSnackBar(UiText.MStringResource(MR.strings.theme_was_saved))
                                            }
                                        } else {
                                            vm.showSnackBar(UiText.MStringResource(MR.strings.theme_was_not_valid))
                                        }
                                        vm.isSavable = false
                                    }
                                },
                                shape = MaterialTheme.shapes.medium
                            ) {
                                MidSizeTextComposable(text = localizeHelper.localize(MR.strings.save_custom_theme))
                            }
                        } else if (vm.colorTheme.value > 0) {
                            TextButton(onClick = {
                                scope.launchIO {
                                    scope.launch {
                                        vm.vmThemes.find { it.id == vm.colorTheme.value }
                                                ?.toCustomTheme()
                                                ?.let { vm.themeRepository.delete(it) }
                                    }
                                    vm.showSnackBar(UiText.MStringResource(MR.strings.theme_was_deleted))
                                }
                            }) {
                                MidSizeTextComposable(text = localizeHelper.localize(MR.strings.delete_custom_theme))
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
                                PreferenceValues.RelativeTime.Off -> localizeHelper.localize(MR.strings.off)
                                PreferenceValues.RelativeTime.Day -> localizeHelper.localize(MR.strings.pref_relative_time_short)
                                PreferenceValues.RelativeTime.Week -> localizeHelper.localize(MR.strings.pref_relative_time_long)
                                else -> localizeHelper.localize(MR.strings.off)
                            }
                        },
                        title = localizeHelper.localize(MR.strings.pref_relative_format),
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
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ThemeItem(
        theme: Theme,
        onClick: (Theme) -> Unit,
        onLongClick: (Theme) -> Unit = {},
        isSelected: Boolean = false,
) {
    val borders = MaterialTheme.shapes.medium
    val borderColor = remember {
        if (theme.materialColors.isLight()) {
            Color.Black.copy(alpha = 0.25f)
        } else {
            Color.White.copy(alpha = 0.15f)
        }
    }
    
    // Enhanced elevation and scale animation for selected state
    val elevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isSelected) 8.dp else 2.dp,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )
    
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessLow
        )
    )
    
    Surface(
            tonalElevation = elevation,
            shadowElevation = elevation,
            color = theme.materialColors.background,
            shape = borders,
            modifier = Modifier
                    .size(110.dp, 170.dp)
                    .padding(12.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    }
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else borderColor,
                        shape = borders
                    )
                    .combinedClickable(
                        onClick = { onClick(theme) },
                        onLongClick = { onLongClick(theme) }
                    )
    ) {
        Box {
            Column(
                    modifier = Modifier.padding(2.dp)
            ) {
                // Enhanced toolbar preview
                Toolbar(
                        modifier = Modifier.requiredHeight(28.dp),
                        title = {},
                        backgroundColor = theme.extraColors.bars
                )
                
                // Content area with better spacing
                Box(
                        Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(8.dp)
                ) {
                    // Sample text
                    Text(
                        "Aa",
                        fontSize = 14.sp,
                        color = theme.materialColors.onBackground,
                        modifier = Modifier.align(Alignment.TopStart)
                    )
                    
                    // Primary color button preview
                    Button(
                            onClick = { onClick(theme) },
                            enabled = true,
                            contentPadding = PaddingValues(),
                            modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .size(44.dp, 22.dp),
                            content = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = theme.materialColors.primary
                            )
                    )
                    
                    // Secondary color FAB preview
                    Surface(
                            modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.BottomEnd),
                            shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                            color = theme.materialColors.secondary,
                            tonalElevation = 4.dp,
                            content = { }
                    )
                }
                
                // Bottom bar preview
                BottomAppBar(
                        modifier = Modifier.requiredHeight(28.dp),
                        containerColor = theme.extraColors.bars
                ) {
                }
            }
            
            // Enhanced selection indicator with animation
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = androidx.compose.animation.scaleIn(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
                    )
                ) + androidx.compose.animation.fadeIn(),
                exit = androidx.compose.animation.scaleOut() + androidx.compose.animation.fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    // Background circle for better visibility
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 2.dp
                    ) {}
                    
                    Icon(
                            modifier = Modifier
                                    .size(32.dp)
                                    .padding(4.dp),
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "theme is selected",
                            tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
