package org.ireader.presentation.feature_explore.presentation.browse

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.ireader.core.utils.replaceAll
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import tachiyomi.source.model.Filter
import timber.log.Timber

@Composable
fun FilterBottomSheet(
    onApply: () -> Unit,
    onReset: () -> Unit,
    onUpdate: (Filter<*>, Int) -> Unit,
    filters: List<Filter<*>>,
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = {
                onReset()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(text = "Reset", color = MaterialTheme.colors.primary)
            }
            Button(onClick = {
                onApply()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(text = "Apply", color = MaterialTheme.colors.onPrimary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        filters.forEachIndexed { index, filter ->
            Spacer(modifier = Modifier.height(8.dp))
            when (filter) {
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
                            onUpdate(filter.apply { this.value = value }, index)
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
                            onUpdate(filter.apply {
                                this.value = Filter.Sort.Selection(value, false)
                            }, index)
                            state = value
                        },
                        currentItem = filter.options[state],
                        items = filter.options)
                }
                is Filter.Text -> {
                    FilterTextItem(
                        filter = filter,
                        onUpdate = {
                            onUpdate(filter.apply { this.value = it }, index)
                        }
                    )
                }
                is Filter.Group -> {
                    FilterGroupItem(
                        name = filter.name,
                        filters = filter.filters,
                        onUpdate = {
                            Timber.e("TAG 1 " + filter.filters.map { it.value })
                            onUpdate(filter.apply {
                                this.filters.replaceAll(it)
                            }, index)
                            Timber.e("TAG 2 " + filter.apply {
                                this.filters.replaceAll(it)
                            }.filters.map { it.value })
                        },
                    )
                }
                is Filter.Check -> {
                    FilterCheckItem(
                        filter = filter,
                        onCheck = { value ->
                            onUpdate(filter.apply {
                                this.value = value
                            }, index)
                        }
                    )

                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun FilterBottomSheetPrev() {
    FilterBottomSheet(
        {},
        {},
        { f, i -> },
        emptyList()
    )
}
