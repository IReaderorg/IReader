package org.ireader.components.components
//
// import androidx.compose.foundation.layout.Arrangement
// import androidx.compose.foundation.layout.PaddingValues
// import androidx.compose.foundation.layout.Row
// import androidx.compose.foundation.layout.RowScope
// import androidx.compose.foundation.layout.Spacer
// import androidx.compose.foundation.layout.fillMaxHeight
// import androidx.compose.foundation.layout.fillMaxWidth
// import androidx.compose.foundation.layout.height
// import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.layout.statusBarsPadding
// import androidx.compose.foundation.layout.width
// import androidx.compose.material3.AppBarDefaults
// import androidx.compose.material3.ContentAlpha
// import androidx.compose.material3.LocalContentAlpha
// import androidx.compose.material3.LocalElevationOverlay
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.ProvideTextStyle
// import androidx.compose.material3.Surface
// import androidx.compose.material3.MaterialTheme
// import androidx.compose.material3.ProvideTextStyle
// import androidx.compose.material3.Surface
// import androidx.compose.runtime.Composable
// import androidx.compose.runtime.CompositionLocalProvider
// import androidx.compose.runtime.getValue
// import androidx.compose.runtime.mutableStateOf
// import androidx.compose.runtime.remember
// import androidx.compose.runtime.setValue
// import androidx.compose.ui.Alignment
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.graphics.RectangleShape
// import androidx.compose.ui.graphics.Shape
// import androidx.compose.ui.layout.onGloballyPositioned
// import androidx.compose.ui.platform.LocalDensity
// import androidx.compose.ui.unit.Dp
// import androidx.compose.ui.unit.dp
// import org.ireader.core_ui.theme.AppColors
// import org.ireader.core_ui.theme.ContentAlpha
// import kotlin.math.abs
// import kotlin.math.max
//
// val AppBarHeight = 56.dp
// val AppBarHorizontalPadding = 4.dp
// val TitleIconModifier = Modifier.fillMaxHeight()
// var iconWidth = 72.dp - AppBarHorizontalPadding
// var withoutIconWidth = 16.dp - AppBarHorizontalPadding
//
// @Composable
// fun CenterTopAppBar(
//    title: @Composable () -> Unit,
//    modifier: Modifier = Modifier,
//    navigationIcon: @Composable (() -> Unit)? = null,
//    actions: @Composable RowScope.() -> Unit = {},
//    backgroundColor: Color = AppColors.current.bars,
//    contentColor: Color = AppColors.current.onBars,
//    elevation: Dp = AppBarDefaults.TopAppBarElevation,
//    applyInsets: Boolean = true,
// ) {
//    val defLeftSectionWidth = if (navigationIcon == null) withoutIconWidth else iconWidth
//    var leftSectionWidth by remember { mutableStateOf(defLeftSectionWidth) }
//    var rightSectionWidth by remember { mutableStateOf(-1f) }
//    var rightSectionPadding by remember { mutableStateOf(0f) }
//
//    CompositionLocalProvider(LocalElevationOverlay provides NoElevationOverlay) {
//        // Wrap in another Surface to avoid drawing elevation between status bar and toolbar
//        Surface(
//            modifier = modifier,
//            color = backgroundColor,
//            contentColor = contentColor,
//            elevation = elevation
//        ) {
//            AppBar(
//                backgroundColor,
//                contentColor,
//                elevation,
//                AppBarDefaults.ContentPadding,
//                RectangleShape,
//                modifier = if (applyInsets) modifier.statusBarsPadding() else modifier,
//            ) {
//                if (navigationIcon == null) {
//                    Spacer(Modifier.width(leftSectionWidth))
//                } else {
//                    Row(
//                        TitleIconModifier.width(leftSectionWidth),
//                        verticalAlignment = Alignment.CenterVertically
//                    ) {
//                        CompositionLocalProvider(
//                            LocalContentAlpha provides ContentAlpha.high,
//                            content = navigationIcon
//                        )
//                    }
//                }
//
//                Row(
//                    Modifier
//                        .fillMaxHeight()
//                        .weight(1f),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    if (leftSectionWidth != defLeftSectionWidth ||
//                        rightSectionPadding != 0f
//                    ) {
//                        ProvideTextStyle(value = MaterialTheme.typography.headlineSmall) {
//                            CompositionLocalProvider(
//                                LocalContentAlpha provides ContentAlpha.high,
//                                content = title
//                            )
//                        }
//                    }
//                }
//
//                CompositionLocalProvider(LocalContentAlpha provides ContentAlpha.medium) {
//                    with(LocalDensity.current) {
//                        Row(
//                            Modifier
//                                .fillMaxHeight()
//                                .padding(start = rightSectionPadding.toDp())
//                                .onGloballyPositioned {
//                                    rightSectionWidth = it.size.width.toFloat()
//                                    if (leftSectionWidth == defLeftSectionWidth &&
//                                        rightSectionWidth != -1f &&
//                                        rightSectionPadding == 0f
//                                    ) {
//                                        /*
//                                 Find the maximum width of the sections (left or right).
//                                 As a result, both sections should have the same width.
//                                 */
//                                        val maxWidth =
//                                            max(leftSectionWidth.value, rightSectionWidth)
//                                        leftSectionWidth = maxWidth.toDp()
//                                        rightSectionPadding = abs(rightSectionWidth - maxWidth)
//                                    }
//                                },
//                            horizontalArrangement = Arrangement.End,
//                            verticalAlignment = Alignment.CenterVertically,
//                            content = actions
//                        )
//                    }
//                }
//            }
//        }
//    }
// }
//
// @Composable
// fun AppBar(
//    backgroundColor: Color,
//    contentColor: Color,
//    elevation: Dp,
//    contentPadding: PaddingValues,
//    shape: Shape,
//    modifier: Modifier = Modifier,
//    content: @Composable RowScope.() -> Unit,
// ) {
//    Surface(
//        color = backgroundColor,
//        contentColor = contentColor,
//        elevation = elevation,
//        shape = shape,
//        modifier = modifier
//    ) {
//        Row(
//            Modifier
//                .fillMaxWidth()
//                .padding(contentPadding)
//                .height(AppBarHeight),
//            horizontalArrangement = Arrangement.Start,
//            verticalAlignment = Alignment.CenterVertically,
//            content = content
//        )
//    }
// }
