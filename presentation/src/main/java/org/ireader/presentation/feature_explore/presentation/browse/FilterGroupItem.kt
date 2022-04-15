package org.ireader.presentation.feature_explore.presentation.browse

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ireader.core.utils.replace
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.core_api.source.model.Filter

@Composable
fun FilterGroupItem(
    name: String,
    filters: List<Filter<*>>,
    onUpdate: (List<Filter<*>>) -> Unit,
    isExpandable: Boolean = false,
) {
    var expanded by remember {
        mutableStateOf(!isExpandable)
    }

    if (isExpandable) {
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
            AppIconButton(
                imageVector = Icons.Default.ArrowDropDown,
                title = name,
                onClick = { expanded = !expanded })
        }
    }
    if (expanded) {
        filters.forEachIndexed { index, filter ->
            Spacer(modifier = Modifier.height(8.dp))
            when (filter) {
                is Filter.Check -> {
                    FilterCheckItem(
                        filter = filter,
                        onCheck = { value ->
                            onUpdate(
                                filters.replace(index, filter.apply {
                                    this.value = value
                                })
                            )
                        }
                    )
                }
                is Filter.Note -> {
                    MidSizeTextComposable(text = filter.name)
                }
                is Filter.Sort -> {
                    var state by remember {
                        mutableStateOf(filter.initialValue?.index)
                    }
                    LaunchedEffect(key1 = filter.value) {
                        state = filter.value?.index
                    }
                    FilterMenuItem(
                        filter = filter.name,
                        onSelected = { value ->
                            onUpdate(
                                filters.replace(index, filter.apply {
                                    this.value = Filter.Sort.Selection(value, false)
                                })
                            )
                            state = value
                        },
                        currentItem = if (state != null) {
                            filter.options[state!!]
                        } else filter.options[0],
                        items = filter.options)
                }
                is Filter.Text -> {
                    FilterTextItem(
                        filter = filter,
                        onUpdate = {
                            onUpdate(
                                filters.replace(index, filter.apply {
                                    this.value = it
                                })
                            )
                        },
                    )
                }
                is Filter.Group -> {
                    FilterGroupItem(
                        name = filter.name,
                        filters = filter.filters,
                        onUpdate = {
                            onUpdate(filters.replace(index, Filter.Group(filter.name, it)))
                        },
                        isExpandable = true,
                    )
                }
            }
        }
    }
}