package ireader.presentation.ui.settings.appearance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
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
import ireader.presentation.ui.settings.components.ThemePreviewCard
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.core.modifier.supportDesktopHorizontalLazyListScroll
import ireader.presentation.core.toDomainColor
import ireader.presentation.ui.core.ui.PreferenceMutableState
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

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
    // Get both light and dark color states so we can set colors on the correct one
    val lightCustomColors = vm.lightColors
    val darkCustomColors = vm.darkColors
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

    // Separate themes by light and dark
    val lightThemes = remember(vm.vmThemes.size) {
        vm.vmThemes.filter { !it.isDark }
    }
    val darkThemes = remember(vm.vmThemes.size) {
        vm.vmThemes.filter { it.isDark }
    }
    
    // Plugin themes integration
    val pluginExtension = rememberPluginThemeIntegration(vm.pluginManager, vm)
    val pluginThemeOptions by pluginExtension.getAllThemesFlow().collectAsState(initial = pluginExtension.getAllThemes())
    val lightPluginThemes = remember(pluginThemeOptions) {
        pluginThemeOptions.filterIsInstance<ireader.presentation.ui.core.theme.ThemeOption.Plugin>().filter { !it.isDark }
    }
    val darkPluginThemes = remember(pluginThemeOptions) {
        pluginThemeOptions.filterIsInstance<ireader.presentation.ui.core.theme.ThemeOption.Plugin>().filter { it.isDark }
    }
    
    // Track selected plugin theme
    var selectedPluginThemeId by remember { mutableStateOf(vm.uiPreferences.selectedPluginTheme().get()) }

    LazyColumnWithInsets(scaffoldPaddingValues) {
        // Dynamic Colors Section
        item {
            Components.Switch(
                preference = vm.dynamicColorMode,
                title = localizeHelper.localize(Res.string.material_you_dynamic_colors),
                subtitle = localizeHelper.localize(Res.string.adapt_colors_from_your_wallpaper_android_12),
            ).Build()
        }
        item {
            Components.Switch(
                preference = vm.useTrueBlack,
                title = localizeHelper.localize(Res.string.use_true_black_amoled),
                subtitle = localizeHelper.localize(Res.string.use_true_black_amoled_subtitle),
            ).Build()
        }
        item {
            Components.Switch(
                preference = vm.coverBasedThemeEnabled,
                title = localizeHelper.localize(Res.string.cover_based_dynamic_color),
                subtitle = localizeHelper.localize(Res.string.cover_based_dynamic_color_subtitle),
            ).Build()
        }
        item {
            Components.Dynamic {
                val coverStyleState = remember(vm.coverBasedThemeStyle.value) { vm.coverBasedThemeStyle }
                val coverStyleChoices = remember {
                    PreferenceValues.CoverBasedThemeStyle.entries.associateWith { style ->
                        when (style) {
                            PreferenceValues.CoverBasedThemeStyle.TonalSpot -> localizeHelper.localize(Res.string.cover_theme_tonal_spot)
                            PreferenceValues.CoverBasedThemeStyle.Neutral -> localizeHelper.localize(Res.string.cover_theme_neutral)
                            PreferenceValues.CoverBasedThemeStyle.Vibrant -> localizeHelper.localize(Res.string.cover_theme_vibrant)
                            PreferenceValues.CoverBasedThemeStyle.Expressive -> localizeHelper.localize(Res.string.cover_theme_expressive)
                            PreferenceValues.CoverBasedThemeStyle.Rainbow -> localizeHelper.localize(Res.string.cover_theme_rainbow)
                            PreferenceValues.CoverBasedThemeStyle.FruitSalad -> localizeHelper.localize(Res.string.cover_theme_fruit_salad)
                            PreferenceValues.CoverBasedThemeStyle.Monochrome -> localizeHelper.localize(Res.string.cover_theme_monochrome)
                            PreferenceValues.CoverBasedThemeStyle.Fidelity -> localizeHelper.localize(Res.string.cover_theme_fidelity)
                            PreferenceValues.CoverBasedThemeStyle.Content -> localizeHelper.localize(Res.string.cover_theme_content)
                        }
                    }
                }
                ChoicePreference(
                    preference = coverStyleState,
                    choices = coverStyleChoices,
                    title = localizeHelper.localize(Res.string.cover_theme_style),
                    subtitle = localizeHelper.localize(Res.string.cover_theme_style_subtitle),
                    enable = vm.coverBasedThemeEnabled.value
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                val sourceState = remember(vm.coverBasedThemeSource.value) { vm.coverBasedThemeSource }
                val sourceChoices = remember {
                    PreferenceValues.CoverBasedThemeSource.entries.associateWith { source ->
                        when (source) {
                            PreferenceValues.CoverBasedThemeSource.BookCover -> localizeHelper.localize(Res.string.cover_theme_source_book_cover)
                            PreferenceValues.CoverBasedThemeSource.SystemMaterialYou -> localizeHelper.localize(Res.string.cover_theme_source_system_material_you)
                            PreferenceValues.CoverBasedThemeSource.StaticTheme -> localizeHelper.localize(Res.string.cover_theme_source_static_theme)
                        }
                    }
                }
                ChoicePreference(
                    preference = sourceState,
                    choices = sourceChoices,
                    title = localizeHelper.localize(Res.string.cover_theme_source),
                    subtitle = localizeHelper.localize(Res.string.cover_theme_source_subtitle),
                    enable = vm.coverBasedThemeEnabled.value
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                val contrastState = remember(vm.coverBasedThemeContrast.value) { vm.coverBasedThemeContrast }
                val contrastChoices = remember {
                    PreferenceValues.CoverBasedThemeContrast.entries.associateWith { contrast ->
                        when (contrast) {
                            PreferenceValues.CoverBasedThemeContrast.Vibrant -> localizeHelper.localize(Res.string.cover_theme_contrast_vibrant)
                            PreferenceValues.CoverBasedThemeContrast.Muted -> localizeHelper.localize(Res.string.cover_theme_contrast_muted)
                            PreferenceValues.CoverBasedThemeContrast.HighContrast -> localizeHelper.localize(Res.string.cover_theme_contrast_high)
                        }
                    }
                }
                ChoicePreference(
                    preference = contrastState,
                    choices = contrastChoices,
                    title = localizeHelper.localize(Res.string.cover_theme_contrast),
                    subtitle = localizeHelper.localize(Res.string.cover_theme_contrast_subtitle),
                    enable = vm.coverBasedThemeEnabled.value
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                val presetState = remember(vm.coverBasedThemePreset.value) { vm.coverBasedThemePreset }
                val presetChoices = remember {
                    PreferenceValues.CoverBasedThemePreset.entries.associateWith { preset ->
                        when (preset) {
                            PreferenceValues.CoverBasedThemePreset.Off -> localizeHelper.localize(Res.string.cover_theme_preset_off)
                            PreferenceValues.CoverBasedThemePreset.Soft -> localizeHelper.localize(Res.string.cover_theme_preset_soft)
                            PreferenceValues.CoverBasedThemePreset.Medium -> localizeHelper.localize(Res.string.cover_theme_preset_medium)
                            PreferenceValues.CoverBasedThemePreset.High -> localizeHelper.localize(Res.string.cover_theme_preset_high)
                        }
                    }
                }
                ChoicePreference(
                    preference = presetState,
                    choices = presetChoices,
                    title = localizeHelper.localize(Res.string.cover_theme_preset),
                    subtitle = localizeHelper.localize(Res.string.cover_theme_preset_subtitle),
                    enable = vm.coverBasedThemeEnabled.value
                )
            }.Build()
        }
        item {
            Components.Dynamic {
                val saturationState = remember(vm.coverBasedThemeSaturation.value) { vm.coverBasedThemeSaturation }
                val intensityState = remember(vm.coverBasedThemeIntensity.value) { vm.coverBasedThemeIntensity }
                val textColorModeState = remember(vm.coverBasedThemeTextColorMode.value) { vm.coverBasedThemeTextColorMode }
                
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        text = localizeHelper.localize(Res.string.cover_theme_advanced_controls),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = localizeHelper.localize(Res.string.cover_theme_saturation))
                        Slider(
                            value = saturationState.value,
                            onValueChange = { saturationState.value = it },
                            valueRange = 0f..10f,
                            steps = 10,
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            enabled = vm.coverBasedThemeEnabled.value
                        )
                        Text(
                            text = "${saturationState.value.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(24.dp)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = localizeHelper.localize(Res.string.cover_theme_intensity))
                        Slider(
                            value = intensityState.value,
                            onValueChange = { intensityState.value = it },
                            valueRange = 0f..10f,
                            steps = 10,
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            enabled = vm.coverBasedThemeEnabled.value
                        )
                        Text(
                            text = "${intensityState.value.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(24.dp)
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = localizeHelper.localize(Res.string.cover_theme_brightness))
                        Slider(
                            value = vm.coverBasedThemeBrightness.value,
                            onValueChange = { vm.coverBasedThemeBrightness.value = it },
                            valueRange = 0f..10f,
                            steps = 10,
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            enabled = vm.coverBasedThemeEnabled.value
                        )
                        Text(
                            text = "${vm.coverBasedThemeBrightness.value.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.width(24.dp)
                        )
                    }
                    
                    val textColorModeChoices = remember {
                        PreferenceValues.CoverBasedTextColorMode.entries.associateWith { mode ->
                            when (mode) {
                                PreferenceValues.CoverBasedTextColorMode.Auto -> localizeHelper.localize(Res.string.cover_theme_text_color_auto)
                                PreferenceValues.CoverBasedTextColorMode.AdaptiveCoverTone -> localizeHelper.localize(Res.string.cover_theme_text_color_adaptive)
                                PreferenceValues.CoverBasedTextColorMode.PureWhite -> localizeHelper.localize(Res.string.cover_theme_text_color_pure_white)
                                PreferenceValues.CoverBasedTextColorMode.PureBlack -> localizeHelper.localize(Res.string.cover_theme_text_color_pure_black)
                                PreferenceValues.CoverBasedTextColorMode.Light -> localizeHelper.localize(Res.string.cover_theme_text_color_light)
                                PreferenceValues.CoverBasedTextColorMode.Dark -> localizeHelper.localize(Res.string.cover_theme_text_color_dark)
                            }
                        }
                    }
                    ChoicePreference(
                        preference = textColorModeState,
                        choices = textColorModeChoices,
                        title = localizeHelper.localize(Res.string.cover_theme_text_color),
                        subtitle = localizeHelper.localize(Res.string.cover_theme_text_color_subtitle),
                        enable = vm.coverBasedThemeEnabled.value
                    )
                }
            }.Build()
        }
        item {
            Components.Dynamic {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = localizeHelper.localize(Res.string.cover_theme_background_tint_opacity))
                    Slider(
                        value = vm.coverBasedThemeBackgroundTintOpacity.value,
                        onValueChange = { vm.coverBasedThemeBackgroundTintOpacity.value = it },
                        valueRange = 0f..10f,
                        steps = 10,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        enabled = vm.coverBasedThemeEnabled.value
                    )
                    Text(
                        text = "${vm.coverBasedThemeBackgroundTintOpacity.value.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(24.dp)
                    )
                }
            }.Build()
        }
        item {
            Components.Dynamic {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = localizeHelper.localize(Res.string.cover_theme_backdrop_blur))
                    Slider(
                        value = vm.coverBasedThemeBackdropBlur.value,
                        onValueChange = { vm.coverBasedThemeBackdropBlur.value = it },
                        valueRange = 0f..10f,
                        steps = 10,
                        modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                        enabled = vm.coverBasedThemeEnabled.value
                    )
                    Text(
                        text = "${vm.coverBasedThemeBackdropBlur.value.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.width(24.dp)
                    )
                }
            }.Build()
        }
        item {
            Components.Switch(
                preference = vm.coverBasedThemeForLibrary,
                title = localizeHelper.localize(Res.string.cover_based_theme_for_library),
                subtitle = localizeHelper.localize(Res.string.cover_based_theme_for_library_subtitle),
                enabled = vm.coverBasedThemeEnabled.value
            ).Build()
        }
        item {
            Components.Dynamic {
                OutlinedButton(
                    onClick = {
                        vm.coverBasedThemeStyle.value = PreferenceValues.CoverBasedThemeStyle.TonalSpot
                        vm.coverBasedThemeSaturation.value = 1.0f
                        vm.coverBasedThemeIntensity.value = 1.0f
                        vm.coverBasedThemeTextColorMode.value = PreferenceValues.CoverBasedTextColorMode.Auto
                        vm.coverBasedThemeSource.value = PreferenceValues.CoverBasedThemeSource.BookCover
                        vm.coverBasedThemeContrast.value = PreferenceValues.CoverBasedThemeContrast.Vibrant
                        vm.coverBasedThemeBrightness.value = 1.0f
                        vm.coverBasedThemeBackgroundTintOpacity.value = 0.3f
                        vm.coverBasedThemeBackdropBlur.value = 0f
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = vm.coverBasedThemeEnabled.value
                ) {
                    Text(text = localizeHelper.localize(Res.string.cover_theme_reset_settings))
                }
            }.Build()
        }
        
        // Section Divider
        item {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        
        // Font Customization Section
        item {
            Components.Header(
                    text = localizeHelper.localize(Res.string.font_customization),
            ).Build()
        }
        item {
            Components.Dynamic {
                ChoicePreference<String>(
                        preference = vm.appUiFont,
                        choices = vm.availableFonts,
                        title = localizeHelper.localize(Res.string.app_ui_font),
                        subtitle = localizeHelper.localize(Res.string.font_used_for_all_app),
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
                        text = localizeHelper.localize(Res.string.light_themes),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Text(
                        text = "${lightThemes.size + lightPluginThemes.size} themes available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }.Build()
        }
        item {
            Components.Dynamic {
                val lightThemesScrollState = rememberLazyListState()
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .supportDesktopHorizontalLazyListScroll(lightThemesScrollState, scope),
                    state = lightThemesScrollState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    // Built-in light themes
                    items(items = lightThemes, key = { it.id }) { theme ->
                        ThemePreviewCard(
                            theme = theme,
                            themeName = getThemeName(theme.id),
                            isSelected = vm.colorTheme.value == theme.id && selectedPluginThemeId.isEmpty(),
                            onClick = {
                                vm.colorTheme.value = theme.id
                                selectedPluginThemeId = ""
                                vm.uiPreferences.selectedPluginTheme().set("")
                                // Set colors on the LIGHT color state since this is a light theme
                                lightCustomColors.primaryState.value = theme.materialColors.primary
                                lightCustomColors.secondaryState.value = theme.materialColors.secondary
                                lightCustomColors.barsState.value = theme.extraColors.bars
                                vm.isSavable = false
                                // Auto-switch to light mode
                                vm.saveNightModePreferences(PreferenceValues.ThemeMode.Light)
                            }
                        )
                    }
                    // Plugin light themes
                    items(items = lightPluginThemes, key = { "plugin_${it.id}" }) { pluginTheme ->
                        val appliedTheme = remember(pluginTheme) {
                            pluginExtension.applyTheme(pluginTheme)
                        }
                        ThemePreviewCard(
                            theme = appliedTheme,
                            themeName = pluginTheme.name,
                            isSelected = selectedPluginThemeId == pluginTheme.id,
                            onClick = {
                                vm.colorTheme.value = appliedTheme.id
                                selectedPluginThemeId = pluginTheme.id
                                vm.uiPreferences.selectedPluginTheme().set(pluginTheme.id)
                                // Set colors
                                lightCustomColors.primaryState.value = appliedTheme.materialColors.primary
                                lightCustomColors.secondaryState.value = appliedTheme.materialColors.secondary
                                lightCustomColors.barsState.value = appliedTheme.extraColors.bars
                                vm.isSavable = false
                                // Auto-switch to light mode
                                vm.saveNightModePreferences(PreferenceValues.ThemeMode.Light)
                            }
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
                        text = localizeHelper.localize(Res.string.dark_themes),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    Text(
                        text = "${darkThemes.size + darkPluginThemes.size} themes available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }.Build()
        }
        item {
            Components.Dynamic {
                val darkThemesScrollState = rememberLazyListState()
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .supportDesktopHorizontalLazyListScroll(darkThemesScrollState, scope),
                    state = darkThemesScrollState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    // Built-in dark themes
                    items(items = darkThemes, key = { it.id }) { theme ->
                        ThemePreviewCard(
                            theme = theme,
                            themeName = getThemeName(theme.id),
                            isSelected = vm.colorTheme.value == theme.id && selectedPluginThemeId.isEmpty(),
                            onClick = {
                                vm.colorTheme.value = theme.id
                                selectedPluginThemeId = ""
                                vm.uiPreferences.selectedPluginTheme().set("")
                                // Set colors on the DARK color state since this is a dark theme
                                darkCustomColors.primaryState.value = theme.materialColors.primary
                                darkCustomColors.secondaryState.value = theme.materialColors.secondary
                                darkCustomColors.barsState.value = theme.extraColors.bars
                                vm.isSavable = false
                                // Auto-switch to dark mode
                                vm.saveNightModePreferences(PreferenceValues.ThemeMode.Dark)
                            }
                        )
                    }
                    // Plugin dark themes
                    items(items = darkPluginThemes, key = { "plugin_${it.id}" }) { pluginTheme ->
                        val appliedTheme = remember(pluginTheme) {
                            pluginExtension.applyTheme(pluginTheme)
                        }
                        ThemePreviewCard(
                            theme = appliedTheme,
                            themeName = pluginTheme.name,
                            isSelected = selectedPluginThemeId == pluginTheme.id,
                            onClick = {
                                vm.colorTheme.value = appliedTheme.id
                                selectedPluginThemeId = pluginTheme.id
                                vm.uiPreferences.selectedPluginTheme().set(pluginTheme.id)
                                // Set colors
                                darkCustomColors.primaryState.value = appliedTheme.materialColors.primary
                                darkCustomColors.secondaryState.value = appliedTheme.materialColors.secondary
                                darkCustomColors.barsState.value = appliedTheme.extraColors.bars
                                vm.isSavable = false
                                // Auto-switch to dark mode
                                vm.saveNightModePreferences(PreferenceValues.ThemeMode.Dark)
                            }
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
                    text = localizeHelper.localize(Res.string.color_customization),
            ).Build()
        }
        item {
            Components.Dynamic {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(
                        text = localizeHelper.localize(Res.string.customize_individual_colors_to_create),
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
                                    color = customizedColors.primaryState.value.toComposeColor(),
                                    tonalElevation = 2.dp
                                ) {}
                                Text(
                                    text = localizeHelper.localize(Res.string.primary),
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
                                    color = customizedColors.secondaryState.value.toComposeColor(),
                                    tonalElevation = 2.dp
                                ) {}
                                Text(
                                    text = localizeHelper.localize(Res.string.secondary),
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
                                    color = customizedColors.barsState.value.toComposeColor(),
                                    tonalElevation = 2.dp
                                ) {}
                                Text(
                                    text = localizeHelper.localize(Res.string.toolbar),
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
                val scope = rememberCoroutineScope()
                val defaultPrimaryColor = MaterialTheme.colorScheme.primary
                // Create wrapper PreferenceMutableState that converts DomainColor to Color
                val primaryColorPref: PreferenceMutableState<Color> = remember(customizedColors.primaryState.value, scope, defaultPrimaryColor) {
                    val pref = object : ireader.core.prefs.Preference<Color> {
                        override fun key(): String = "primaryColor"
                        override fun get(): Color = customizedColors.primaryState.value.toComposeColor()
                        override fun set(value: Color) { customizedColors.primaryState.value = value.toDomainColor() }
                        override fun isSet(): Boolean = true
                        override fun delete() { }
                        override fun defaultValue(): Color = defaultPrimaryColor
                        override fun changes(): kotlinx.coroutines.flow.Flow<Color> = kotlinx.coroutines.flow.flowOf(get())
                        override fun stateIn(scope: kotlinx.coroutines.CoroutineScope): kotlinx.coroutines.flow.StateFlow<Color> = 
                            kotlinx.coroutines.flow.MutableStateFlow(get())
                    }
                    PreferenceMutableState(pref, scope)
                }
                ColorPreference(
                        preference = primaryColorPref,
                        title = localizeHelper.localize(Res.string.color_primary),
                        subtitle = localizeHelper.localize(Res.string.displayed_most_frequently_across_your_app),
                        unsetColor = defaultPrimaryColor,
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
                val scope = rememberCoroutineScope()
                val defaultSecondaryColor = MaterialTheme.colorScheme.secondary
                // Create wrapper PreferenceMutableState that converts DomainColor to Color
                val secondaryColorPref: PreferenceMutableState<Color> = remember(customizedColors.secondaryState.value, scope, defaultSecondaryColor) {
                    val pref = object : ireader.core.prefs.Preference<Color> {
                        override fun key(): String = "secondaryColor"
                        override fun get(): Color = customizedColors.secondaryState.value.toComposeColor()
                        override fun set(value: Color) { customizedColors.secondaryState.value = value.toDomainColor() }
                        override fun isSet(): Boolean = true
                        override fun delete() { }
                        override fun defaultValue(): Color = defaultSecondaryColor
                        override fun changes(): kotlinx.coroutines.flow.Flow<Color> = kotlinx.coroutines.flow.flowOf(get())
                        override fun stateIn(scope: kotlinx.coroutines.CoroutineScope): kotlinx.coroutines.flow.StateFlow<Color> = 
                            kotlinx.coroutines.flow.MutableStateFlow(get())
                    }
                    PreferenceMutableState(pref, scope)
                }
                ColorPreference(
                        preference = secondaryColorPref,
                        title = localizeHelper.localize(Res.string.color_secondary),
                        subtitle = localizeHelper.localize(Res.string.accents_select_parts_of_the_ui),
                        unsetColor = defaultSecondaryColor,
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
                val scope = rememberCoroutineScope()
                val defaultBarsColor = AppColors.current.bars.toComposeColor()
                // Create wrapper PreferenceMutableState that converts DomainColor to Color
                val barsColorPref: PreferenceMutableState<Color> = remember(customizedColors.barsState.value, scope, defaultBarsColor) {
                    val pref = object : ireader.core.prefs.Preference<Color> {
                        override fun key(): String = "barsColor"
                        override fun get(): Color = customizedColors.barsState.value.toComposeColor()
                        override fun set(value: Color) { customizedColors.barsState.value = value.toDomainColor() }
                        override fun isSet(): Boolean = true
                        override fun delete() { }
                        override fun defaultValue(): Color = defaultBarsColor
                        override fun changes(): kotlinx.coroutines.flow.Flow<Color> = kotlinx.coroutines.flow.flowOf(get())
                        override fun stateIn(scope: kotlinx.coroutines.CoroutineScope): kotlinx.coroutines.flow.StateFlow<Color> = 
                            kotlinx.coroutines.flow.MutableStateFlow(get())
                    }
                    PreferenceMutableState(pref, scope)
                }
                ColorPreference(
                        preference = barsColorPref,
                        title = localizeHelper.localize(Res.string.toolbar_color),
                        unsetColor = defaultBarsColor,
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
                    text = localizeHelper.localize(Res.string.theme_management),
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
                        text = localizeHelper.localize(Res.string.backup_and_restore_your_custom_themes),
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
                            Text(localizeHelper.localize(Res.string.export_theme))
                        }
                        
                        // Export all themes button
                        Button(
                            onClick = {
                                themeToExport = vm.exportAllCustomThemes()
                                showThemeExport = true
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(localizeHelper.localize(Res.string.backup_all))
                        }
                    }
                    
                    // Import theme button
                    Button(
                        onClick = {
                            vm.showImportDialog = true
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(localizeHelper.localize(Res.string.import_theme))
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
                    text = localizeHelper.localize(Res.string.novel_info),
            ).Build()
        }
        item {
            Components.Switch(
                preference = vm.hideNovelBackdrop,
                title = localizeHelper.localize(Res.string.hide_backdrop),
                subtitle = localizeHelper.localize(Res.string.hide_background_images_on_novel),
            ).Build()
        }
        item {
            Components.Switch(
                preference = vm.useFabInNovelInfo,
                title = localizeHelper.localize(Res.string.use_fab_instead_of_buttons),
                subtitle = localizeHelper.localize(Res.string.replace_standard_action_buttons_with),
            ).Build()
        }
        
        // Section Divider
        item {
            Divider(modifier = Modifier.padding(vertical = 16.dp))
        }
        
        // Timestamp Section
        item {
            Components.Header(
                    text = localizeHelper.localize(Res.string.date_time),
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
            title = { Text(localizeHelper.localize(Res.string.import_theme)) },
            text = {
                Column {
                    Text(localizeHelper.localize(Res.string.paste_the_theme_json_below))
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
                        placeholder = { Text(localizeHelper.localize(Res.string.paste_json_here)) }
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
                                vm.showSnackBar(UiText.DynamicString("Failed to import theme: ${result.exceptionOrNull()?.message ?: "Unknown error"}"))
                            }
                        }
                    },
                    enabled = importText.isNotBlank()
                ) {
                    Text(localizeHelper.localize(Res.string.import_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.showImportDialog = false }) {
                    Text(localizeHelper.localize(Res.string.cancel))
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



