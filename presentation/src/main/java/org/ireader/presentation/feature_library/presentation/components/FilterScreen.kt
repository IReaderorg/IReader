package org.ireader.presentation.feature_library.presentation.components

import androidx.annotation.Keep
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.domain.models.FilterType
import org.ireader.presentation.feature_library.presentation.viewmodel.LibraryViewModel


@Composable
fun FilterScreen(viewModel: LibraryViewModel) {
    Column(Modifier
        .fillMaxSize()
        .background(MaterialTheme.colors.background)
        .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top) {
        val items = listOf(
            FilterItem("Unread", FilterType.Unread),
            FilterItem("Completed", FilterType.Completed),
            FilterItem("Downloaded", FilterType.Downloaded),
        )
        items.forEach { filter ->
            CheckBoxWithText(filter.name,
                viewModel.filters.contains(filter.type)) {
                if (!viewModel.filters.contains(filter.type)) {
                    viewModel.addFilters(filter.type)
                } else {
                    viewModel.removeFilters(filter.type)
                }
            }
        }

    }
}

@Keep
private data class FilterItem(
    val name: String,
    val type: FilterType,
)
