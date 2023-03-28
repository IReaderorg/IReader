package ireader.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import ireader.domain.utils.fastForEach
import ireader.domain.utils.fastMap
import ireader.domain.utils.fastMaxBy
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
actual fun PlatformScaffold(modifier: Modifier,
                            topBarScrollBehavior: TopAppBarScrollBehavior,
                            snackbarHostState: SnackbarHostState,
                            topBar: @Composable (TopAppBarScrollBehavior) -> Unit,
                            bottomBar: @Composable () -> Unit,
                            startBar: @Composable () -> Unit,
                            snackbarHost: @Composable () -> Unit,
                            floatingActionButton: @Composable () -> Unit,
                            floatingActionButtonPosition: FabPosition,
                            containerColor: Color, contentColor: Color,
                            contentWindowInsets: WindowInsets,
                            content: @Composable (PaddingValues) -> Unit) {
    // Tachiyomi: Handle consumed window insets
    val remainingWindowInsets = remember { MutableWindowInsets() }
    androidx.compose.material3.Surface(
        modifier = Modifier
            .nestedScroll(topBarScrollBehavior.nestedScrollConnection)
            .withConsumedWindowInsets {
                remainingWindowInsets.insets = contentWindowInsets.exclude(it)
            }
            .then(modifier),
        color = containerColor,
        contentColor = contentColor,
    ) {
        ScaffoldLayout(
            fabPosition = floatingActionButtonPosition,
            topBar = { topBar(topBarScrollBehavior) },
            startBar = startBar,
            bottomBar = bottomBar,
            content = content,
            snackbar = snackbarHost,
            contentWindowInsets = remainingWindowInsets,
            fab = floatingActionButton,
        )
    }
}

/**
 * Layout for a [Scaffold]'s content.
 *
 * @param fabPosition [FabPosition] for the FAB (if present)
 * @param topBar the content to place at the top of the [Scaffold], typically a [SmallTopAppBar]
 * @param content the main 'body' of the [Scaffold]
 * @param snackbar the [Snackbar] displayed on top of the [content]
 * @param fab the [FloatingActionButton] displayed on top of the [content], below the [snackbar]
 * and above the [bottomBar]
 * @param bottomBar the content to place at the bottom of the [Scaffold], on top of the
 * [content], typically a [NavigationBar].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScaffoldLayout(
    fabPosition: FabPosition,
    topBar: @Composable () -> Unit,
    startBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    snackbar: @Composable () -> Unit,
    fab: @Composable () -> Unit,
    contentWindowInsets: WindowInsets,
    bottomBar: @Composable () -> Unit,
) {
    SubcomposeLayout { constraints ->
        val layoutWidth = constraints.maxWidth
        val layoutHeight = constraints.maxHeight

        val looseConstraints = constraints.copy(minWidth = 0, minHeight = 0)

        /**
         * Tachiyomi: Remove height constraint for expanded app bar
         */

        /**
         * Tachiyomi: Remove height constraint for expanded app bar
         */
        /**
         * Tachiyomi: Remove height constraint for expanded app bar
         */
        /**
         * Tachiyomi: Remove height constraint for expanded app bar
         */
        val topBarConstraints = looseConstraints.copy(maxHeight = Constraints.Infinity)

        layout(layoutWidth, layoutHeight) {
            val leftInset = contentWindowInsets.getLeft(this@SubcomposeLayout, layoutDirection)
            val rightInset = contentWindowInsets.getRight(this@SubcomposeLayout, layoutDirection)
            val bottomInset = contentWindowInsets.getBottom(this@SubcomposeLayout)

            // Tachiyomi: Add startBar slot for Navigation Rail
            val startBarPlaceables = subcompose(ScaffoldLayoutContent.StartBar, startBar).fastMap {
                it.measure(looseConstraints)
            }
            val startBarWidth = startBarPlaceables.fastMaxBy { it.width }?.width ?: 0

            // Tachiyomi: layoutWidth after horizontal insets
            val insetLayoutWidth = layoutWidth - leftInset - rightInset - startBarWidth

            val topBarPlaceables = subcompose(ScaffoldLayoutContent.TopBar, topBar).fastMap {
                it.measure(topBarConstraints)
            }

            val topBarHeight = topBarPlaceables.fastMaxBy { it.height }?.height ?: 0

            val snackbarPlaceables = subcompose(ScaffoldLayoutContent.Snackbar, snackbar).fastMap {
                it.measure(looseConstraints)
            }

            val snackbarHeight = snackbarPlaceables.fastMaxBy { it.height }?.height ?: 0
            val snackbarWidth = snackbarPlaceables.fastMaxBy { it.width }?.width ?: 0

            // Tachiyomi: Calculate insets for snackbar placement offset
            val snackbarLeft = if (snackbarPlaceables.isNotEmpty()) {
                (insetLayoutWidth - snackbarWidth) / 2 + leftInset
            } else {
                0
            }

            val fabPlaceables =
                subcompose(ScaffoldLayoutContent.Fab, fab).fastMap { measurable ->
                    measurable.measure(looseConstraints)
                }

            val fabWidth = fabPlaceables.fastMaxBy { it.width }?.width ?: 0
            val fabHeight = fabPlaceables.fastMaxBy { it.height }?.height ?: 0

            val fabPlacement = if (fabPlaceables.isNotEmpty() && fabWidth != 0 && fabHeight != 0) {
                // FAB distance from the left of the layout, taking into account LTR / RTL
                // Tachiyomi: Calculate insets for fab placement offset
                val fabLeftOffset = if (fabPosition == FabPosition.End) {
                    if (layoutDirection == LayoutDirection.Ltr) {
                        layoutWidth - FabSpacing.roundToPx() - fabWidth - rightInset
                    } else {
                        FabSpacing.roundToPx() + leftInset
                    }
                } else {
                    leftInset + ((insetLayoutWidth - fabWidth) / 2)
                }

                FabPlacement(
                    left = fabLeftOffset,
                    width = fabWidth,
                    height = fabHeight,
                )
            } else {
                null
            }

            val bottomBarPlaceables = subcompose(ScaffoldLayoutContent.BottomBar) {
                CompositionLocalProvider(
                    LocalFabPlacement provides fabPlacement,
                    content = bottomBar,
                )
            }.fastMap { it.measure(looseConstraints) }

            val bottomBarHeight = bottomBarPlaceables.fastMaxBy { it.height }?.height
            val fabOffsetFromBottom = fabPlacement?.let {
                max(bottomBarHeight ?: 0, bottomInset) + it.height + FabSpacing.roundToPx()
            }

            val snackbarOffsetFromBottom = if (snackbarHeight != 0) {
                snackbarHeight + (fabOffsetFromBottom ?: bottomBarHeight ?: bottomInset)
            } else {
                0
            }

            val bodyContentPlaceables = subcompose(ScaffoldLayoutContent.MainContent) {
                val insets = contentWindowInsets.asPaddingValues(this@SubcomposeLayout)
                val fabOffsetDp = fabOffsetFromBottom?.toDp() ?: 0.dp
                val bottomBarHeightPx = bottomBarHeight ?: 0
                val innerPadding = PaddingValues(
                    top =
                    if (topBarPlaceables.isEmpty()) {
                        insets.calculateTopPadding()
                    } else {
                        topBarHeight.toDp()
                    },
                    bottom = // Tachiyomi: Also take account of fab height when providing inner padding
                    if (bottomBarPlaceables.isEmpty() || bottomBarHeightPx == 0) {
                        max(insets.calculateBottomPadding(), fabOffsetDp)
                    } else {
                        max(bottomBarHeightPx.toDp(), fabOffsetDp)
                    },
                    start = max(
                        insets.calculateStartPadding((this@SubcomposeLayout).layoutDirection),
                        startBarWidth.toDp()
                    ),
                    end = insets.calculateEndPadding((this@SubcomposeLayout).layoutDirection),
                )
                content(innerPadding)
            }.fastMap { it.measure(looseConstraints) }

            // Placing to control drawing order to match default elevation of each placeable

            bodyContentPlaceables.fastForEach {
                it.place(0, 0)
            }
            startBarPlaceables.fastForEach {
                it.placeRelative(0, 0)
            }
            topBarPlaceables.fastForEach {
                it.place(0, 0)
            }
            snackbarPlaceables.fastForEach {
                it.place(
                    snackbarLeft,
                    layoutHeight - snackbarOffsetFromBottom,
                )
            }
            // The bottom bar is always at the bottom of the layout
            bottomBarPlaceables.fastForEach {
                it.place(0, layoutHeight - (bottomBarHeight ?: 0))
            }
            // Explicitly not using placeRelative here as `leftOffset` already accounts for RTL
            fabPlaceables.fastForEach {
                it.place(fabPlacement?.left ?: 0, layoutHeight - (fabOffsetFromBottom ?: 0))
            }
        }
    }
}




/**
 * Placement information for a [FloatingActionButton] inside a [Scaffold].
 *
 * @property left the FAB's offset from the left edge of the bottom bar, already adjusted for RTL
 * support
 * @property width the width of the FAB
 * @property height the height of the FAB
 */
@Immutable
internal class FabPlacement(
    val left: Int,
    val width: Int,
    val height: Int,
)

/**
 * CompositionLocal containing a [FabPlacement] that is used to calculate the FAB bottom offset.
 */
internal val LocalFabPlacement = staticCompositionLocalOf<FabPlacement?> { null }

// FAB spacing above the bottom bar / bottom of the Scaffold
private val FabSpacing = 16.dp

private enum class ScaffoldLayoutContent { TopBar, MainContent, Snackbar, Fab, BottomBar, StartBar }