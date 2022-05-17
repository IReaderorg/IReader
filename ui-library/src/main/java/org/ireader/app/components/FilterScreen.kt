package org.ireader.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.common_models.FilterType
import org.ireader.components.text_related.TextCheckBox
import org.ireader.core_ui.ui.string
import org.ireader.ui_library.R

@Composable
fun FilterScreen(
    filters: List<FilterType>,
    addFilters: (FilterType) -> Unit,
    removeFilter: (FilterType) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {
        val items = listOf(
            FilterItem(string(id = R.string.unread), FilterType.Unread),
            FilterItem(string(id = R.string.completed), FilterType.Completed),
            FilterItem(string(id = R.string.downloaded), FilterType.Downloaded),
        )
        items.forEach { filter ->
            TextCheckBox(
                filter.name,
                filters.contains(filter.type)
            ) {
                if (!filters.contains(filter.type)) {
                    addFilters(filter.type)
                } else {
                    removeFilter(filter.type)
                }
            }
        }
    }
}

private data class FilterItem(
    val name: String,
    val type: FilterType,
)
