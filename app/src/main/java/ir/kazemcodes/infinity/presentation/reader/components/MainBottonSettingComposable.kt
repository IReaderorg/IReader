package ir.kazemcodes.infinity.presentation.reader.components

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
import ir.kazemcodes.infinity.presentation.reader.ReaderScreenViewModel
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun MainBottomSettingComposable(
    modifier: Modifier = Modifier,
    viewModel: ReaderScreenViewModel,
    scope: CoroutineScope,
    scaffoldState: ScaffoldState,
) {
    ChaptersSliderComposable(viewModel = viewModel)
    Row(modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically) {
        TopAppBarActionButton(imageVector = Icons.Default.Menu,
            title = "Chapter List Drawer",
            onClick = { scope.launch(Dispatchers.Main) { scaffoldState.drawerState.open() } })
        TopAppBarActionButton(imageVector = Icons.Default.Settings,
            title = "Setting Drawer",
            onClick = { viewModel.toggleSettingMode(true) })
    }
}