/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ireader.presentation.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ireader.presentation.ui.component.components.ISnackBarHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun PlatformScaffold(
        modifier: Modifier = Modifier,
        topBarScrollBehavior: TopAppBarScrollBehavior,
        snackbarHostState: SnackbarHostState,
        topBar: @Composable (TopAppBarScrollBehavior) -> Unit,
        bottomBar: @Composable () -> Unit,
        startBar: @Composable () -> Unit,
        snackbarHost: @Composable () -> Unit,
        floatingActionButton: @Composable () -> Unit,
        floatingActionButtonPosition: FabPosition,
        containerColor: Color,
        contentColor: Color,
        contentWindowInsets: WindowInsets,
        content: @Composable (PaddingValues) -> Unit,
)


/**
 * <a href="https://material.io/design/layout/understanding-layout.html" class="external" target="_blank">Material Design layout</a>.
 *
 * Scaffold implements the basic material design visual layout structure.
 *
 * This component provides API to put together several material components to construct your
 * screen, by ensuring proper layout strategy for them and collecting necessary data so these
 * components will work together correctly.
 *
 * Simple example of a Scaffold with [SmallTopAppBar], [FloatingActionButton]:
 *
 * @sample androidx.compose.material3.samples.SimpleScaffoldWithTopBar
 *
 * To show a [Snackbar], use [SnackbarHostState.showSnackbar].
 *
 * @sample androidx.compose.material3.samples.ScaffoldWithSimpleSnackbar
 *
 * Tachiyomi changes:
 * * Pass scroll behavior to top bar by default
 * * Remove height constraint for expanded app bar
 * * Also take account of fab height when providing inner padding
 * * Fixes for fab and snackbar horizontal placements when [contentWindowInsets] is used
 * * Handle consumed window insets
 * * Add startBar slot for Navigation Rail
 *
 * @param modifier the [Modifier] to be applied to this scaffold
 * @param topBar top app bar of the screen, typically a [SmallTopAppBar]
 * @param startBar side bar on the start of the screen, typically a [NavigationRail]
 * @param bottomBar bottom bar of the screen, typically a [NavigationBar]
 * @param snackbarHost component to host [Snackbar]s that are pushed to be shown via
 * [SnackbarHostState.showSnackbar], typically a [SnackbarHost]
 * @param floatingActionButton Main action button of the screen, typically a [FloatingActionButton]
 * @param floatingActionButtonPosition position of the FAB on the screen. See [FabPosition].
 * @param containerColor the color used for the background of this scaffold. Use [Color.Transparent]
 * to have no color.
 * @param contentColor the preferred color for content inside this scaffold. Defaults to either the
 * matching content color for [containerColor], or to the current [LocalContentColor] if
 * [containerColor] is not a color from the theme.
 * @param contentWindowInsets window insets to be passed to content slot via PaddingValues params.
 * Scaffold will take the insets into account from the top/bottom only if the topBar/ bottomBar
 * are not present, as the scaffold expect topBar/bottomBar to handle insets instead
 * @param content content of the screen. The lambda receives a [PaddingValues] that should be
 * applied to the content root via [Modifier.padding] and [Modifier.consumeWindowInsets] to
 * properly offset top and bottom bars. If using [Modifier.verticalScroll], apply this modifier to
 * the child of the scroll, and not on the scroll itself.
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

@Composable
fun IScaffold(
        modifier: Modifier = Modifier,
        topBarScrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(
                rememberTopAppBarState()
        ),
        snackbarHostState: SnackbarHostState = SnackbarHostState(),
        topBar: @Composable (TopAppBarScrollBehavior) -> Unit = {},
        bottomBar: @Composable () -> Unit = {},
        startBar: @Composable () -> Unit = {},
        snackbarHost: @Composable () -> Unit = {
            ISnackBarHost(snackbarHostState)
        },
        floatingActionButton: @Composable () -> Unit = {},
        floatingActionButtonPosition: FabPosition = FabPosition.End,
        containerColor: Color = MaterialTheme.colorScheme.background,
        contentColor: Color = contentColorFor(containerColor),
        contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
        content: @Composable (PaddingValues) -> Unit,
) {
    return PlatformScaffold(
            modifier, topBarScrollBehavior, snackbarHostState, topBar, bottomBar, startBar, snackbarHost, floatingActionButton, floatingActionButtonPosition, containerColor, contentColor, contentWindowInsets, content
    )
}


/**
 * The possible positions for a [FloatingActionButton] attached to a [Scaffold].
 */
@ExperimentalMaterial3Api
@JvmInline
value class FabPosition internal constructor(@Suppress("unused") private val value: Int) {
    companion object {
        /**
         * Position FAB at the bottom of the screen in the center, above the [NavigationBar] (if it
         * exists)
         */
        val Center = FabPosition(0)

        /**
         * Position FAB at the bottom of the screen at the end, above the [NavigationBar] (if it
         * exists)
         */
        val End = FabPosition(1)
    }

    override fun toString(): String {
        return when (this) {
            Center -> "FabPosition.Center"
            else -> "FabPosition.End"
        }
    }
}