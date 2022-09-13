package ireader.ui.home.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.ui.component.reusable_composable.AppIconButton
import ireader.ui.component.reusable_composable.DropDownMenu
import ireader.ui.component.reusable_composable.MidSizeTextComposable
import ireader.ui.component.reusable_composable.TextField
import ireader.core.api.source.model.Filter
import ireader.core.api.util.replace

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
                contentDescription = name,
                onClick = { expanded = !expanded }
            )
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
                                filters.replace(
                                    index,
                                    filter.apply {
                                        this.value = value
                                    }
                                )
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
                    DropDownMenu(
                        text = filter.name,
                        onSelected = { value ->
                            onUpdate(
                                filters.replace(
                                    index,
                                    filter.apply {
                                        this.value = Filter.Sort.Selection(value, false)
                                    }
                                )
                            )
                            state = value
                        },
                        currentValue = if (state != null) {
                            filter.options[state!!]
                        } else filter.options[0],
                        items = filter.options.map { it }.toTypedArray()
                    )
                }
                is Filter.Text -> {
                    TextField(
                        filter = filter,
                        onUpdate = {
                            onUpdate(
                                filters.replace(
                                    index,
                                    filter.apply {
                                        this.value = it
                                    }
                                )
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
                else -> {}
            }
        }
    }
}
