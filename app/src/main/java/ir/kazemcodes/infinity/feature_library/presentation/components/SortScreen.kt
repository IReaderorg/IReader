package ir.kazemcodes.infinity.feature_library.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.kazemcodes.infinity.feature_library.presentation.LibraryViewModel


@Composable
fun SortScreen(viewModel: LibraryViewModel) {
    val items = listOf<SortType>(
        SortType.DateAdded,
        SortType.Alphabetically,
        SortType.LastRead,
        SortType.TotalChapter,
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(12.dp)
    ) {
        Column(Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top) {
            items.forEach { item ->

                IconWithText(item.name,
                    if (!viewModel.state.value.isSortAcs) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    viewModel.state.value.sortType == item,
                    onClick = {
                        viewModel.changeSortIndex(item)
                    })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}