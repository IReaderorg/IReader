package ireader.presentation.ui.home.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.all_time
import ireader.i18n.resources.clear
import ireader.i18n.resources.close
import ireader.i18n.resources.delete_all_histories
import ireader.i18n.resources.filter
import ireader.i18n.resources.group_by_novel
import ireader.i18n.resources.history
import ireader.i18n.resources.more_options_1
import ireader.i18n.resources.past_7_days
import ireader.i18n.resources.relative_time_today
import ireader.i18n.resources.search
import ireader.i18n.resources.search_history
import ireader.i18n.resources.yesterday
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.history.viewmodel.DateFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryTopAppBar(
    searchMode: Boolean,
    searchQuery: String,
    onSearchModeChange: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onClearClick: () -> Unit,
    groupByNovel: Boolean,
    onToggleGroupByNovel: () -> Unit,
    dateFilter: DateFilter?,
    onDateFilterChange: (DateFilter?) -> Unit,
    onClearAll: () -> Unit,
    hasHistory: Boolean,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val interactionSource = remember { MutableInteractionSource() }
    var showFilterMenu by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    if (searchMode) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .focusRequester(focusRequester),
            placeholder = { Text(localize(Res.string.search_history)) },
            leadingIcon = { 
                IconButton(onClick = onSearchModeChange) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = localize(Res.string.close)
                    )
                }
            },
            trailingIcon = {
                AnimatedVisibility(
                    visible = searchQuery.isNotEmpty(),
                ) {
                    IconButton(onClick = onClearClick) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = localize(Res.string.clear)
                        )
                    }
                }
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
    } else {
        TopAppBar(
            title = { Text(localize(Res.string.history)) },
            scrollBehavior = scrollBehavior,
            actions = {
                IconButton(onClick = onSearchModeChange) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = localize(Res.string.search)
                    )
                }
                
                IconButton(onClick = { showFilterMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = localize(Res.string.filter),
                        tint = if (dateFilter != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(localizeHelper.localize(Res.string.all_time)) },
                        onClick = {
                            onDateFilterChange(null)
                            showFilterMenu = false
                        },
                        trailingIcon = {
                            if (dateFilter == null) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(localizeHelper.localize(Res.string.relative_time_today)) },
                        onClick = {
                            onDateFilterChange(DateFilter.TODAY)
                            showFilterMenu = false
                        },
                        trailingIcon = {
                            if (dateFilter == DateFilter.TODAY) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(localizeHelper.localize(Res.string.yesterday)) },
                        onClick = {
                            onDateFilterChange(DateFilter.YESTERDAY)
                            showFilterMenu = false
                        },
                        trailingIcon = {
                            if (dateFilter == DateFilter.YESTERDAY) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(localizeHelper.localize(Res.string.past_7_days)) },
                        onClick = {
                            onDateFilterChange(DateFilter.PAST_7_DAYS)
                            showFilterMenu = false
                        },
                        trailingIcon = {
                            if (dateFilter == DateFilter.PAST_7_DAYS) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                }
                
                IconButton(onClick = { showMoreMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = localize(Res.string.more_options_1)
                    )
                }
                
                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(localizeHelper.localize(Res.string.group_by_novel)) },
                        onClick = {
                            onToggleGroupByNovel()
                            showMoreMenu = false
                        },
                        trailingIcon = {
                            if (groupByNovel) {
                                Icon(Icons.Default.Check, contentDescription = null)
                            }
                        }
                    )
                    
                    if (hasHistory) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(localizeHelper.localize(Res.string.delete_all_histories)) },
                            onClick = {
                                showMoreMenu = false
                                onClearAll()
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.error
                            )
                        )
                    }
                }
            }
        )
    }
}
