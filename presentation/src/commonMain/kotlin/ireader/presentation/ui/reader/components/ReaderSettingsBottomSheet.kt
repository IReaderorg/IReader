package ireader.presentation.ui.reader.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.prefs.PreferenceValues
import ireader.i18n.resources.*
import ireader.i18n.resources.colors
import ireader.i18n.resources.fonts
import ireader.i18n.resources.general
import ireader.i18n.resources.reader
import ireader.presentation.ui.component.IBackHandler
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import kotlinx.coroutines.launch

// Animation durations - reduced for better performance
private const val TAB_ANIMATION_DURATION_MS = 150 // Reduced from default ~300ms
private const val TAB_ANIMATION_DISABLED_MS = 0 // No animation for reduced animations mode

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ReaderSettingsBottomSheet(
    modifier: Modifier = Modifier,
    vm: ReaderScreenViewModel,
    onDismiss: () -> Unit,
    onFontSelected: (Int) -> Unit,
    onToggleAutoBrightness: () -> Unit,
    onChangeBrightness: (Float) -> Unit,
    onBackgroundChange: (themeId: Long) -> Unit,
    onTextAlign: (PreferenceValues.PreferenceTextAlignment) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // Check if reduced animations is enabled for older devices
    val reducedAnimations = vm.readerPreferences.reducedAnimations().get()
    val animationDuration = if (reducedAnimations) TAB_ANIMATION_DISABLED_MS else TAB_ANIMATION_DURATION_MS
    
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        4 // Changed from 3 to 4 to include Fonts tab
    }
    val scope = rememberCoroutineScope()
    
    // Handle back button to dismiss the sheet
    IBackHandler(enabled = true, onBack = onDismiss)

    // Use faster animation spec for bottom sheet
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Skip partial expansion for faster opening
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        dragHandle = {
            // Simplified drag handle for better performance
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), 
                            RoundedCornerShape(2.dp)
                        )
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f) // Slightly reduced height for faster rendering
        ) {
            // Tab Row with reduced/disabled animation based on preference
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Tab(
                    text = { MidSizeTextComposable(text = localizeHelper.localize(Res.string.reader)) },
                    selected = pagerState.currentPage == 0,
                    onClick = { 
                        scope.launch { 
                            if (reducedAnimations) {
                                pagerState.scrollToPage(0) // Instant, no animation
                            } else {
                                pagerState.animateScrollToPage(
                                    page = 0,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )
                            }
                        } 
                    }
                )
                Tab(
                    text = { MidSizeTextComposable(text = localizeHelper.localize(Res.string.general)) },
                    selected = pagerState.currentPage == 1,
                    onClick = { 
                        scope.launch { 
                            if (reducedAnimations) {
                                pagerState.scrollToPage(1)
                            } else {
                                pagerState.animateScrollToPage(
                                    page = 1,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )
                            }
                        } 
                    }
                )
                Tab(
                    text = { MidSizeTextComposable(text = localizeHelper.localize(Res.string.colors)) },
                    selected = pagerState.currentPage == 2,
                    onClick = { 
                        scope.launch { 
                            if (reducedAnimations) {
                                pagerState.scrollToPage(2)
                            } else {
                                pagerState.animateScrollToPage(
                                    page = 2,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )
                            }
                        } 
                    }
                )
                Tab(
                    text = { MidSizeTextComposable(text = localizeHelper.localize(Res.string.fonts)) },
                    selected = pagerState.currentPage == 3,
                    onClick = { 
                        scope.launch { 
                            if (reducedAnimations) {
                                pagerState.scrollToPage(3)
                            } else {
                                pagerState.animateScrollToPage(
                                    page = 3,
                                    animationSpec = tween(durationMillis = animationDuration)
                                )
                            }
                        } 
                    }
                )
            }

            // Content with optimized pager settings
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                // Only keep 1 page on each side in memory for better performance
                beyondViewportPageCount = if (reducedAnimations) 0 else 1,
                // Disable user scroll gesture in reduced animations mode for snappier feel
                userScrollEnabled = !reducedAnimations,
                key = { it } // Stable keys for better recomposition
            ) { page ->
                when (page) {
                    0 -> ReaderScreenTab(vm, onTextAlign)
                    1 -> GeneralScreenTab(vm)
                    2 -> ColorScreenTab(vm, onChangeBrightness, onBackgroundChange)
                    3 -> FontPickerTab(vm)
                }
            }
        }
    }
}
