//package ireader.presentation.ui.settings.appearance
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyListState
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.outlined.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.runtime.saveable.rememberSaveable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import ireader.domain.models.prefs.PreferenceValues
//import ireader.i18n.localize
//import ireader.i18n.resources.Res
//import ireader.i18n.resources.*
//import ireader.presentation.ui.component.IScaffold
//import ireader.presentation.ui.component.components.TitleToolbar
//import ireader.presentation.ui.settings.components.*
//
///**
// * Enhanced appearance settings screen following Mihon's comprehensive theming system.
// * Provides Material Design 3 theming, dynamic colors, and advanced customization options.
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun SettingsAppearanceScreen(
//    modifier: Modifier = Modifier,
//    onNavigateUp: () -> Unit,
//    viewModel: SettingsAppearanceViewModel,
//    scaffoldPaddingValues: PaddingValues = PaddingValues()
//) {
//    val listState = rememberSaveable(
//        key = "settings_appearance_scroll_state",
//        saver = LazyListState.Saver
//    ) {
//        LazyListState()
//    }
//
//    val themeMode by viewModel.themeMode.collectAsState()
//    val dynamicColors by viewModel.dynamicColors.collectAsState()
//    val amoledMode by viewModel.amoledMode.collectAsState()
//    val appFont by viewModel.appFont.collectAsState()
//    val hideNovelBackdrop by viewModel.hideNovelBackdrop.collectAsState()
//    val useFabInNovelInfo by viewModel.useFabInNovelInfo.collectAsState()
//    val relativeTime by viewModel.relativeTime.collectAsState()
//
//    IScaffold(
//        modifier = modifier,
//        topBar = { scrollBehavior ->
//            TitleToolbar(
//                title = "Appearance",
//                popBackStack = onNavigateUp,
//                scrollBehavior = scrollBehavior
//            )
//        }
//    ) { paddingValues ->
//        LazyColumn(
//            modifier = Modifier.fillMaxSize(),
//            state = listState,
//            contentPadding = PaddingValues(
//                top = paddingValues.calculateTopPadding(),
//                bottom = paddingValues.calculateBottomPadding() + 16.dp
//            )
//        ) {
//            // Theme Section
//            item {
//                SettingsSectionHeader(
//                    title = "Theme",
//                    icon = Icons.Outlined.Palette
//                )
//            }
//
//            item {
//                SettingsItemWithTrailing(
//                    title = "App Theme",
//                    description = "Choose between light, dark, or system default",
//                    icon = Icons.Outlined.DarkMode,
//                    onClick = { viewModel.showThemeModeDialog() }
//                ) {
//                    Text(
//                        text = when (themeMode) {
//                            PreferenceValues.ThemeMode.Light -> "Light"
//                            PreferenceValues.ThemeMode.Dark -> "Dark"
//                            PreferenceValues.ThemeMode.System -> "System"
//                        },
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//            item {
//                SettingsSwitchItem(
//                    title = "Dynamic Colors (Material You)",
//                    description = "Adapt colors from your wallpaper (Android 12+)",
//                    icon = Icons.Outlined.AutoAwesome,
//                    checked = dynamicColors,
//                    onCheckedChange = viewModel::setDynamicColors
//                )
//            }
//
//            item {
//                SettingsSwitchItem(
//                    title = "Pure Black (AMOLED)",
//                    description = "Pure black backgrounds for power saving on AMOLED screens",
//                    icon = Icons.Outlined.Brightness2,
//                    checked = amoledMode,
//                    onCheckedChange = viewModel::setAmoledMode
//                )
//            }
//
//            // Typography Section
//            item {
//                SettingsSectionHeader(
//                    title = "Typography",
//                    icon = Icons.Outlined.FontDownload
//                )
//            }
//
//            item {
//                SettingsItemWithTrailing(
//                    title = "App Font",
//                    description = "Font used for all app interface elements",
//                    icon = Icons.Outlined.FontDownload,
//                    onClick = { viewModel.showFontDialog() }
//                ) {
//                    Text(
//                        text = appFont.ifEmpty { "System Default" },
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//            // Display Section
//            item {
//                SettingsSectionHeader(
//                    title = "Display",
//                    icon = Icons.Outlined.DisplaySettings
//                )
//            }
//
//            item {
//                SettingsSwitchItem(
//                    title = "Hide Novel Backdrop",
//                    description = "Hide background images on novel detail screens for cleaner look",
//                    icon = Icons.Outlined.HideImage,
//                    checked = hideNovelBackdrop,
//                    onCheckedChange = viewModel::setHideNovelBackdrop
//                )
//            }
//
//            item {
//                SettingsSwitchItem(
//                    title = "Use FAB in Novel Info",
//                    description = "Replace standard action buttons with floating action button",
//                    icon = Icons.Outlined.TouchApp,
//                    checked = useFabInNovelInfo,
//                    onCheckedChange = viewModel::setUseFabInNovelInfo
//                )
//            }
//
//            // Date & Time Section
//            item {
//                SettingsSectionHeader(
//                    title = "Date & Time",
//                    icon = Icons.Outlined.Schedule
//                )
//            }
//
//            item {
//                SettingsItemWithTrailing(
//                    title = "Relative Timestamps",
//                    description = "Show relative time instead of absolute dates",
//                    icon = Icons.Outlined.Schedule,
//                    onClick = { viewModel.showRelativeTimeDialog() }
//                ) {
//                    Text(
//                        text = when (relativeTime) {
//                            PreferenceValues.RelativeTime.Off -> "Off"
//                            PreferenceValues.RelativeTime.Day -> "24 hours"
//                            PreferenceValues.RelativeTime.Week -> "7 days"
//                            PreferenceValues.RelativeTime.Seconds -> "seconds"
//                            PreferenceValues.RelativeTime.Minutes -> "seconds"
//                            PreferenceValues.RelativeTime.Hour -> "hour"
//                        },
//                        style = MaterialTheme.typography.bodyMedium,
//                        color = MaterialTheme.colorScheme.primary
//                    )
//                }
//            }
//
//            // Advanced Theming Section
//            item {
//                SettingsSectionHeader(
//                    title = "Advanced Theming",
//                    icon = Icons.Outlined.ColorLens
//                )
//            }
//
//            item {
//                SettingsItem(
//                    title = "Custom Colors",
//                    description = "Customize individual theme colors",
//                    icon = Icons.Outlined.ColorLens,
//                    onClick = { viewModel.navigateToColorCustomization() }
//                )
//            }
//
//            item {
//                SettingsItem(
//                    title = "Theme Management",
//                    description = "Import, export, and manage custom themes",
//                    icon = Icons.Outlined.Folder,
//                    onClick = { viewModel.navigateToThemeManagement() }
//                )
//            }
//        }
//    }
//
//    // Theme Mode Dialog
//    if (viewModel.showThemeModeDialog) {
//        AlertDialog(
//            onDismissRequest = { viewModel.dismissThemeModeDialog() },
//            title = { Text("App Theme") },
//            text = {
//                Column {
//                    PreferenceValues.ThemeMode.values().forEach { mode ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp)
//                        ) {
//                            RadioButton(
//                                selected = themeMode == mode,
//                                onClick = {
//                                    viewModel.setThemeMode(mode)
//                                    viewModel.dismissThemeModeDialog()
//                                }
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                text = when (mode) {
//                                    PreferenceValues.ThemeMode.Light -> "Light"
//                                    PreferenceValues.ThemeMode.Dark -> "Dark"
//                                    PreferenceValues.ThemeMode.System -> "Follow System"
//                                },
//                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
//                            )
//                        }
//                    }
//                }
//            },
//            confirmButton = {
//                TextButton(onClick = { viewModel.dismissThemeModeDialog() }) {
//                    Text("OK")
//                }
//            }
//        )
//    }
//
//    // Font Dialog
//    if (viewModel.showFontDialog) {
//        AlertDialog(
//            onDismissRequest = { viewModel.dismissFontDialog() },
//            title = { Text("App Font") },
//            text = {
//                Column {
//                    viewModel.availableFonts.forEach { font ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp)
//                        ) {
//                            RadioButton(
//                                selected = appFont == font,
//                                onClick = {
//                                    viewModel.setAppFont(font)
//                                    viewModel.dismissFontDialog()
//                                }
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                text = font.ifEmpty { "System Default" },
//                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
//                            )
//                        }
//                    }
//                }
//            },
//            confirmButton = {
//                TextButton(onClick = { viewModel.dismissFontDialog() }) {
//                    Text("OK")
//                }
//            }
//        )
//    }
//
//    // Relative Time Dialog
//    if (viewModel.showRelativeTimeDialog) {
//        AlertDialog(
//            onDismissRequest = { viewModel.dismissRelativeTimeDialog() },
//            title = { Text("Relative Timestamps") },
//            text = {
//                Column {
//                    PreferenceValues.RelativeTime.values().forEach { time ->
//                        Row(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(vertical = 8.dp)
//                        ) {
//                            RadioButton(
//                                selected = relativeTime == time,
//                                onClick = {
//                                    viewModel.setRelativeTime(time)
//                                    viewModel.dismissRelativeTimeDialog()
//                                }
//                            )
//                            Spacer(modifier = Modifier.width(8.dp))
//                            Text(
//                                text = when (time) {
//                                    PreferenceValues.RelativeTime.Off -> "Off"
//                                    PreferenceValues.RelativeTime.Day -> "24 hours"
//                                    PreferenceValues.RelativeTime.Week -> "7 days"
//                                    PreferenceValues.RelativeTime.Seconds -> "seconds"
//                                    PreferenceValues.RelativeTime.Minutes -> "seconds"
//                                    PreferenceValues.RelativeTime.Hour -> "hour"
//                                },
//                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
//                            )
//                        }
//                    }
//                }
//            },
//            confirmButton = {
//                TextButton(onClick = { viewModel.dismissRelativeTimeDialog() }) {
//                    Text("OK")
//                }
//            }
//        )
//    }
//}
