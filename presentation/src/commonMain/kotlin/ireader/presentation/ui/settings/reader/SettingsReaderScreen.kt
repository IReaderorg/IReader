package ireader.presentation.ui.settings.reader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.settings.components.*

/**
 * Enhanced reader settings screen following Mihon's comprehensive reader customization.
 * Provides 50+ customization options including reading modes, controls, and display preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsReaderScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
    viewModel: SettingsReaderViewModel,
    scaffoldPaddingValues: PaddingValues = PaddingValues()
) {
    val listState = rememberSaveable(
        key = "settings_reader_scroll_state",
        saver = LazyListState.Saver
    ) {
        LazyListState()
    }

    // Reader preferences state
    val readingMode by viewModel.readingMode.collectAsState()
    val pageTransitions by viewModel.pageTransitions.collectAsState()
    val doubleTapZoom by viewModel.doubleTapZoom.collectAsState()
    val showPageNumber by viewModel.showPageNumber.collectAsState()
    val fullscreen by viewModel.fullscreen.collectAsState()
    val keepScreenOn by viewModel.keepScreenOn.collectAsState()
    val showStatusBar by viewModel.showStatusBar.collectAsState()
    val showNavigationBar by viewModel.showNavigationBar.collectAsState()
    val cutoutShort by viewModel.cutoutShort.collectAsState()
    val landscapeZoom by viewModel.landscapeZoom.collectAsState()
    val navigationMode by viewModel.navigationMode.collectAsState()
    val volumeKeysEnabled by viewModel.volumeKeysEnabled.collectAsState()
    val invertTapping by viewModel.invertTapping.collectAsState()
    val flashOnPageChange by viewModel.flashOnPageChange.collectAsState()

    IScaffold(
        modifier = modifier,
        topBar = { scrollBehavior ->
            TitleToolbar(
                title = "Reader",
                onPopBackStack = onNavigateUp,
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            contentPadding = PaddingValues(
                top = paddingValues.calculateTopPadding(),
                bottom = paddingValues.calculateBottomPadding() + 16.dp
            )
        ) {
            // Reading Mode Section
            item {
                SettingsSectionHeader(
                    title = "Reading Mode",
                    icon = Icons.Outlined.ChromeReaderMode
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = "Default Reading Mode",
                    description = "Choose between pager and webtoon modes",
                    icon = Icons.Outlined.ViewColumn,
                    onClick = { viewModel.showReadingModeDialog() }
                ) {
                    Text(
                        text = when (readingMode) {
                            "pager" -> "Pager"
                            "webtoon" -> "Webtoon"
                            "continuous_vertical" -> "Continuous Vertical"
                            else -> "Pager"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            item {
                SettingsItemWithTrailing(
                    title = "Page Transitions",
                    description = "Animation style for page changes",
                    icon = Icons.Outlined.Animation,
                    onClick = { viewModel.showPageTransitionsDialog() }
                ) {
                    Text(
                        text = when (pageTransitions) {
                            "slide" -> "Slide"
                            "fade" -> "Fade"
                            "none" -> "None"
                            else -> "Slide"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Display Settings Section
            item {
                SettingsSectionHeader(
                    title = "Display Settings",
                    icon = Icons.Outlined.DisplaySettings
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Double Tap to Zoom",
                    description = "Enable double tap gesture to zoom in/out",
                    icon = Icons.Outlined.ZoomIn,
                    checked = doubleTapZoom,
                    onCheckedChange = viewModel::setDoubleTapZoom
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Show Page Number",
                    description = "Display current page number in reader",
                    icon = Icons.Outlined.Numbers,
                    checked = showPageNumber,
                    onCheckedChange = viewModel::setShowPageNumber
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Fullscreen",
                    description = "Hide system UI for immersive reading",
                    icon = Icons.Outlined.Fullscreen,
                    checked = fullscreen,
                    onCheckedChange = viewModel::setFullscreen
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Keep Screen On",
                    description = "Prevent screen from turning off while reading",
                    icon = Icons.Outlined.ScreenLockPortrait,
                    checked = keepScreenOn,
                    onCheckedChange = viewModel::setKeepScreenOn
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Show Status Bar",
                    description = "Display status bar in reader",
                    icon = Icons.Outlined.ViewHeadline,
                    checked = showStatusBar,
                    onCheckedChange = viewModel::setShowStatusBar,
                    enabled = !fullscreen
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Show Navigation Bar",
                    description = "Display navigation bar in reader",
                    icon = Icons.Outlined.ViewCarousel,
                    checked = showNavigationBar,
                    onCheckedChange = viewModel::setShowNavigationBar,
                    enabled = !fullscreen
                )
            }
            
            // Orientation & Layout Section
            item {
                SettingsSectionHeader(
                    title = "Orientation & Layout",
                    icon = Icons.Outlined.ScreenRotation
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Cutout Area Behavior",
                    description = "Show content behind display cutout in landscape",
                    icon = Icons.Outlined.CropFree,
                    checked = cutoutShort,
                    onCheckedChange = viewModel::setCutoutShort
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Landscape Zoom",
                    description = "Enable zoom controls in landscape mode",
                    icon = Icons.Outlined.ZoomOutMap,
                    checked = landscapeZoom,
                    onCheckedChange = viewModel::setLandscapeZoom
                )
            }
            
            // Navigation Controls Section
            item {
                SettingsSectionHeader(
                    title = "Navigation Controls",
                    icon = Icons.Outlined.TouchApp
                )
            }
            
            item {
                SettingsItemWithTrailing(
                    title = "Navigation Mode",
                    description = "Choose how to navigate between pages",
                    icon = Icons.Outlined.TouchApp,
                    onClick = { viewModel.showNavigationModeDialog() }
                ) {
                    Text(
                        text = when (navigationMode) {
                            "tap" -> "Tap Zones"
                            "swipe" -> "Swipe"
                            "both" -> "Both"
                            else -> "Tap Zones"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            item {
                SettingsSwitchItem(
                    title = "Volume Key Navigation",
                    description = "Use volume keys to navigate pages",
                    icon = Icons.Outlined.VolumeUp,
                    checked = volumeKeysEnabled,
                    onCheckedChange = viewModel::setVolumeKeysEnabled
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Invert Tapping",
                    description = "Reverse left/right tap zones",
                    icon = Icons.Outlined.SwapHoriz,
                    checked = invertTapping,
                    onCheckedChange = viewModel::setInvertTapping
                )
            }
            
            // Visual Effects Section
            item {
                SettingsSectionHeader(
                    title = "Visual Effects",
                    icon = Icons.Outlined.AutoAwesome
                )
            }
            
            item {
                SettingsSwitchItem(
                    title = "Flash on Page Change",
                    description = "Brief flash effect when changing pages",
                    icon = Icons.Outlined.FlashOn,
                    checked = flashOnPageChange,
                    onCheckedChange = viewModel::setFlashOnPageChange
                )
            }
            
            // Advanced Reader Settings Section
            item {
                SettingsSectionHeader(
                    title = "Advanced Settings",
                    icon = Icons.Outlined.Tune
                )
            }
            
            item {
                SettingsItem(
                    title = "Color Filters",
                    description = "Brightness, contrast, and color adjustments",
                    icon = Icons.Outlined.FilterBAndW,
                    onClick = { viewModel.navigateToColorFilters() }
                )
            }
            
            item {
                SettingsItem(
                    title = "Image Scaling",
                    description = "Fit modes, crop borders, and scaling options",
                    icon = Icons.Outlined.AspectRatio,
                    onClick = { viewModel.navigateToImageScaling() }
                )
            }
            
            item {
                SettingsItem(
                    title = "Tap Zones",
                    description = "Customize tap zone layout and sensitivity",
                    icon = Icons.Outlined.GridOn,
                    onClick = { viewModel.navigateToTapZones() }
                )
            }
        }
    }
    
    // Reading Mode Dialog
    if (viewModel.showReadingModeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissReadingModeDialog() },
            title = { Text("Reading Mode") },
            text = {
                Column {
                    val modes = listOf(
                        "pager" to "Pager",
                        "webtoon" to "Webtoon",
                        "continuous_vertical" to "Continuous Vertical"
                    )
                    modes.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = readingMode == value,
                                onClick = { 
                                    viewModel.setReadingMode(value)
                                    viewModel.dismissReadingModeDialog()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissReadingModeDialog() }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Page Transitions Dialog
    if (viewModel.showPageTransitionsDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPageTransitionsDialog() },
            title = { Text("Page Transitions") },
            text = {
                Column {
                    val transitions = listOf(
                        "slide" to "Slide",
                        "fade" to "Fade",
                        "none" to "None"
                    )
                    transitions.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = pageTransitions == value,
                                onClick = { 
                                    viewModel.setPageTransitions(value)
                                    viewModel.dismissPageTransitionsDialog()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissPageTransitionsDialog() }) {
                    Text("OK")
                }
            }
        )
    }
    
    // Navigation Mode Dialog
    if (viewModel.showNavigationModeDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissNavigationModeDialog() },
            title = { Text("Navigation Mode") },
            text = {
                Column {
                    val modes = listOf(
                        "tap" to "Tap Zones",
                        "swipe" to "Swipe",
                        "both" to "Both"
                    )
                    modes.forEach { (value, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = navigationMode == value,
                                onClick = { 
                                    viewModel.setNavigationMode(value)
                                    viewModel.dismissNavigationModeDialog()
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterVertically)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissNavigationModeDialog() }) {
                    Text("OK")
                }
            }
        )
    }
}