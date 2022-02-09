package org.ireader.presentation.feature_reader.presentation.reader.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ScaffoldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ireader.domain.view_models.reader.ReaderScreenViewModel
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import org.ireader.presentation.utils.scroll.CarouselScrollState

@Composable
fun MainBottomSettingComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
    scrollState: CarouselScrollState,
) {
    ChaptersSliderComposable(viewModel = viewModel, scrollState = scrollState)
    Row(modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically) {
        TopAppBarActionButton(imageVector = Icons.Default.Menu,
            title = "Chapter List Drawer",
            onClick = { scope.launch { scaffoldState.drawerState.open() } })
        TopAppBarActionButton(imageVector = Icons.Default.Settings,
            title = "Setting Drawer",
            onClick = { viewModel.toggleSettingMode(true) })
    }
}