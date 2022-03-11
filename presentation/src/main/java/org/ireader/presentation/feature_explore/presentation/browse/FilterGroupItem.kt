package org.ireader.presentation.feature_explore.presentation.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.ireader.core.utils.replace
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarActionButton
import tachiyomi.source.model.Filter

@Composable
fun FilterGroupItem(
    name: String,
    filters: List<Filter<*>>,
    onUpdate: (List<Filter<*>>) -> Unit,
) {
    var expanded by remember {
        mutableStateOf(false)
    }
    var mFilters by remember {
        mutableStateOf(filters)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                expanded = !expanded
            },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MidSizeTextComposable(text = name)
        TopAppBarActionButton(
            imageVector = Icons.Default.ArrowDropDown,
            title = name,
            onClick = { expanded = !expanded })
    }
    filters.forEachIndexed { index, filter ->
        when (filter) {
            is Filter.Check -> {
                if (expanded) {
                    FilterCheckItem(
                        filter = filter,
                        onCheck = { value ->
                            mFilters.replace(index, filter.apply {
                                this.value = value
                            })
                            onUpdate(mFilters)
                        }
                    )
                }
            }
            is Filter.Note -> {
                MidSizeTextComposable(text = filter.name)
            }
            is Filter.Select -> {
                var state by remember {
                    mutableStateOf(filter.value)
                }
                FilterMenuItem(
                    filter = filter.name,
                    onSelected = { value ->
                        mFilters.replace(index, filter.apply {
                            this.value = value
                        })
                        onUpdate(mFilters)
                        state = value
                    },
                    currentItem = filter.options[state],
                    items = filter.options)
            }
            is Filter.Sort -> {
                var state by remember {
                    mutableStateOf(filter.initialValue?.index ?: 0)
                }
                FilterMenuItem(
                    filter = filter.name,
                    onSelected = { value ->
                        mFilters.replace(index, filter.apply {
                            this.value = Filter.Sort.Selection(value, false)
                        })
                        onUpdate(mFilters)
                        state = value
                    },
                    currentItem = filter.options[state],
                    items = filter.options)
            }
            is Filter.Text -> {
                FilterTextItem(
                    filter = filter,
                    onUpdate = {
                        mFilters.replace(index, filter.apply {
                            this.value = it
                        })
                        onUpdate(mFilters)
                    }
                )
            }
            is Filter.Group -> {
                FilterGroupItem(
                    name = filter.name,
                    filters = filter.filters,
                    onUpdate = {
                        mFilters.replace(index, it)
                        onUpdate(mFilters)
                    },
                )
            }
            else -> {}
        }
    }
}