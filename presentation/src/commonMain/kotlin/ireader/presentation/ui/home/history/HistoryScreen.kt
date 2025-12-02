package ireader.presentation.ui.home.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutQuad
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.HistoryWithRelations
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.all_time
import ireader.i18n.resources.cancel
import ireader.i18n.resources.clear
import ireader.i18n.resources.close
import ireader.i18n.resources.confirm
import ireader.i18n.resources.delete_all_histories
import ireader.i18n.resources.filter
import ireader.i18n.resources.go_to_chapter
import ireader.i18n.resources.group_by_novel
import ireader.i18n.resources.history
import ireader.i18n.resources.more_options_1
import ireader.i18n.resources.no_matches_found_in_search
import ireader.i18n.resources.nothing_read_recently
import ireader.i18n.resources.past_7_days
import ireader.i18n.resources.recently
import ireader.i18n.resources.relative_time_today
import ireader.i18n.resources.remove_from_history
import ireader.i18n.resources.resume
import ireader.i18n.resources.search
import ireader.i18n.resources.search_history
import ireader.i18n.resources.view_novel_details
import ireader.i18n.resources.weekly
import ireader.i18n.resources.yesterday
import ireader.presentation.ui.component.BookListItemImage
import ireader.presentation.ui.component.reusable_composable.WarningAlertData
import ireader.presentation.ui.core.coil.rememberBookCover
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.home.history.viewmodel.DateFilter
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Stable holder for history item click handlers to prevent recomposition
 */
@Stable
private class HistoryClickHandlers(
    val onClickItem: (HistoryWithRelations) -> Unit,
    val onClickDelete: (HistoryWithRelations) -> Unit,
    val onClickPlay: (HistoryWithRelations) -> Unit,
    val onBookCover: (HistoryWithRelations) -> Unit,
    val onLongClickDelete: (HistoryWithRelations) -> Unit
)

/**
 * Stable key generator for history items
 */
@Stable
private fun stableHistoryKey(history: HistoryWithRelations): Any = history.id

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    vm: HistoryViewModel,
    onHistory: (HistoryWithRelations) -> Unit,
    onHistoryDelete: (HistoryWithRelations) -> Unit,
    onHistoryPlay: (HistoryWithRelations) -> Unit,
    onBookCover: (HistoryWithRelations) -> Unit,
    onLongClickDelete: (HistoryWithRelations) -> Unit,
) {
    // Collect state reactively - this is the key fix!
    val state by vm.state.collectAsState()
    val items = state.histories
    val searchQuery = state.searchQuery
    val searchMode = state.isSearchMode
    val groupByNovel = state.groupByNovel
    val dateFilter = state.dateFilter
    
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val searchFocusRequester = remember { FocusRequester() }
    val localizeHelper = requireNotNull<ireader.i18n.LocalizeHelper>(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // Memoize click handlers to prevent unnecessary recompositions
    val clickHandlers = remember(onHistory, onHistoryDelete, onHistoryPlay, onBookCover, onLongClickDelete) {
        HistoryClickHandlers(
            onClickItem = onHistory,
            onClickDelete = onHistoryDelete,
            onClickPlay = onHistoryPlay,
            onBookCover = onBookCover,
            onLongClickDelete = onLongClickDelete
        )
    }
    
    // Derive screen state for efficient rendering
    val screenState by remember(items, searchQuery) {
        derivedStateOf {
            when {
                items.values.flatten().isEmpty() && searchQuery.isNotEmpty() -> HistoryScreenState.NoSearchResults
                items.values.isEmpty() -> HistoryScreenState.Empty
                else -> HistoryScreenState.Content
            }
        }
    }
    
    // Observe when back in history
    LaunchedEffect(searchQuery) {
        vm.applySearchFilter()
    }
    
    Scaffold(
        topBar = {
            HistoryTopAppBar(
                searchMode = searchMode,
                searchQuery = searchQuery,
                onSearchModeChange = { vm.toggleSearchMode() },
                onSearchQueryChange = { vm.onSearchQueryChange(it) },
                focusRequester = searchFocusRequester,
                onClearClick = { vm.onSearchQueryChange("") },
                groupByNovel = groupByNovel,
                onToggleGroupByNovel = { vm.toggleGroupByNovel() },
                dateFilter = dateFilter,
                onDateFilterChange = { vm.setDateFilterHistory(it) },
                onClearAll = { vm.deleteAllHistories(localizeHelper) },
                hasHistory = items.values.isNotEmpty()
            )
        },
        floatingActionButton = {
            if (items.values.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { vm.deleteAllHistories(localizeHelper) },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = localize(Res.string.delete_all_histories),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (screenState) {
                HistoryScreenState.NoSearchResults -> EmptyScreen(
                    text = localize(Res.string.no_matches_found_in_search) + " \"${searchQuery}\"",
                    modifier = Modifier.fillMaxSize(),
                    icon = Icons.Outlined.Search
                )
                HistoryScreenState.Empty -> EmptyScreen(
                    text = localize(Res.string.nothing_read_recently),
                    modifier = Modifier.fillMaxSize(),
                    icon = Icons.Outlined.History
                )
                HistoryScreenState.Content -> HistoryContent(
                    items = items,
                    listState = listState,
                    onClickItem = clickHandlers.onClickItem,
                    onClickDelete = { history -> vm.deleteHistory(history, localizeHelper) },
                    onClickPlay = clickHandlers.onClickPlay,
                    onBookCover = clickHandlers.onBookCover,
                    onLongClickDelete = { history -> vm.deleteHistory(history, localizeHelper) },
                    vm = vm,
                    dateFilter = dateFilter,
                    searchQuery = searchQuery
                )
            }
        }
        
        // Show warning alert dialog outside the Box to ensure it's on top
        if (vm.warningAlert.enable) {
            WarningAlertDialog(data = vm.warningAlert)
        }
    }
    
    // Focus the search field when search mode is activated
    LaunchedEffect(searchMode) {
        if (searchMode) {
            delay(100) // Short delay to ensure the TextField is in the hierarchy
            searchFocusRequester.requestFocus()
        }
    }
}

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
    hasHistory: Boolean
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
                    enter = fadeIn() + expandHorizontally(),
                    exit = fadeOut() + shrinkHorizontally()
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
            shape = RoundedCornerShape(24.dp),
            colors = TextFieldDefaults.colors(
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    } else {
        TopAppBar(
            title = { Text(localize(Res.string.history)) },
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
                        contentDescription = localize(Res.string.filter)
                    )
                }
                
                // More menu with Clear All option
                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        enabled = hasHistory
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = localizeHelper.localize(Res.string.more_options_1),
                            tint = if (hasHistory) MaterialTheme.colorScheme.onSurface 
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    text = localize(Res.string.delete_all_histories),
                                    color = MaterialTheme.colorScheme.error
                                ) 
                            },
                            onClick = {
                                showMoreMenu = false
                                onClearAll()
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DeleteForever,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
                
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { 
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = groupByNovel,
                                    onCheckedChange = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(localizeHelper.localize(Res.string.group_by_novel))
                            }
                        },
                        onClick = {
                            onToggleGroupByNovel()
                        }
                    )
                    
                    HorizontalDivider()
                    
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
                        text = { Text(localize(Res.string.relative_time_today)) },
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
                        text = { Text(localize(Res.string.yesterday)) },
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
                        text = { Text(localize(Res.string.past_7_days)) },
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
            }
        )
    }
}

@Composable
fun HistoryContent(
    items: Map<Long, List<HistoryWithRelations>>,
    listState: LazyListState,
    onClickItem: (HistoryWithRelations) -> Unit,
    onClickDelete: (HistoryWithRelations) -> Unit,
    onClickPlay: (HistoryWithRelations) -> Unit,
    onBookCover: (HistoryWithRelations) -> Unit,
    onLongClickDelete: (HistoryWithRelations) -> Unit,
    vm: HistoryViewModel,
    dateFilter: DateFilter? = null,
    searchQuery: String = ""
) {
    // Memoize time boundaries to avoid recalculation
    val timeBoundaries = remember {
        val calendar = Calendar.getInstance()
        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val yesterday = calendar.apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }.timeInMillis
        
        val lastWeek = calendar.apply {
            add(Calendar.DAY_OF_MONTH, -6)
        }.timeInMillis
        
        Triple(today, yesterday, lastWeek)
    }
    
    val (today, yesterday, lastWeek) = timeBoundaries
    
    // Use derivedStateOf for grouped items to minimize recompositions
    val groupedItems by remember(items, dateFilter, today, yesterday, lastWeek) {
        derivedStateOf {
            val allItems = items.values.flatten()
            val filteredItems = when (dateFilter) {
                DateFilter.TODAY -> allItems.filter { it.readAt >= today }
                DateFilter.YESTERDAY -> allItems.filter { it.readAt >= yesterday && it.readAt < today }
                DateFilter.PAST_7_DAYS -> allItems.filter { it.readAt >= lastWeek }
                null -> allItems
            }
            
            val todayItems = mutableListOf<HistoryWithRelations>()
            val yesterdayItems = mutableListOf<HistoryWithRelations>()
            val thisWeekItems = mutableListOf<HistoryWithRelations>()
            val earlierItems = mutableListOf<HistoryWithRelations>()
            
            filteredItems.forEach { history ->
                when {
                    history.readAt >= today -> todayItems.add(history)
                    history.readAt >= yesterday -> yesterdayItems.add(history)
                    history.readAt >= lastWeek -> thisWeekItems.add(history)
                    else -> earlierItems.add(history)
                }
            }
            
            GroupedHistoryItems(
                todayItems = todayItems.sortedByDescending { it.readAt },
                yesterdayItems = yesterdayItems.sortedByDescending { it.readAt },
                thisWeekItems = thisWeekItems.sortedByDescending { it.readAt },
                earlierItems = earlierItems.sortedByDescending { it.readAt }
            )
        }
    }
    
    // Memoize date formatters
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Today section
            if (groupedItems.todayItems.isNotEmpty()) {
                item(key = "header_today") {
                    HistoryTimeHeader(title = localize(Res.string.relative_time_today))
                }
                
                items(
                    items = groupedItems.todayItems,
                    key = { history -> stableHistoryKey(history) },
                    contentType = { "history_item" }
                ) { history ->
                    HistoryItem(
                        history = history,
                        timeString = timeFormat.format(Date(history.readAt)),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete,
                        vm = vm,
                        searchQuery = searchQuery
                    )
                }
            }
            
            // Yesterday section
            if (groupedItems.yesterdayItems.isNotEmpty()) {
                item(key = "header_yesterday") {
                    HistoryTimeHeader(title = localize(Res.string.yesterday))
                }
                
                items(
                    items = groupedItems.yesterdayItems,
                    key = { history -> stableHistoryKey(history) },
                    contentType = { "history_item" }
                ) { history ->
                    HistoryItem(
                        history = history,
                        timeString = timeFormat.format(Date(history.readAt)),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete,
                        vm = vm,
                        searchQuery = searchQuery
                    )
                }
            }
            
            // This week section
            if (groupedItems.thisWeekItems.isNotEmpty()) {
                item(key = "header_week") {
                    HistoryTimeHeader(title = localize(Res.string.weekly))
                }
                
                items(
                    items = groupedItems.thisWeekItems,
                    key = { history -> stableHistoryKey(history) },
                    contentType = { "history_item" }
                ) { history ->
                    HistoryItem(
                        history = history,
                        timeString = dateFormat.format(Date(history.readAt)),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete,
                        vm = vm,
                        searchQuery = searchQuery
                    )
                }
            }
            
            // Earlier section
            if (groupedItems.earlierItems.isNotEmpty()) {
                item(key = "header_earlier") {
                    HistoryTimeHeader(title = localize(Res.string.recently))
                }
                
                items(
                    items = groupedItems.earlierItems,
                    key = { history -> stableHistoryKey(history) },
                    contentType = { "history_item" }
                ) { history ->
                    HistoryItem(
                        history = history,
                        timeString = dateFormat.format(Date(history.readAt)),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete,
                        vm = vm,
                        searchQuery = searchQuery
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryTimeHeader(title: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier
                .weight(0.15f)
                .padding(end = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryItem(
    history: HistoryWithRelations,
    timeString: String,
    onClickItem: (HistoryWithRelations) -> Unit,
    onClickDelete: (HistoryWithRelations) -> Unit,
    onClickPlay: (HistoryWithRelations) -> Unit,
    onBookCover: (HistoryWithRelations) -> Unit,
    onLongClickDelete: (HistoryWithRelations) -> Unit,
    onHistoryDelete: (HistoryWithRelations) -> Unit,
    vm: HistoryViewModel,
    searchQuery: String = ""
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scope = rememberCoroutineScope()
    var showContextMenu by remember { mutableStateOf(false) }
    
    // Keep track of whether an alert is active to prevent multiple dismisses
    val alertShowing = remember { mutableStateOf(false) }
    
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.EndToStart && !alertShowing.value) {
                alertShowing.value = true
                // Call delete handler immediately (not in coroutine) to show dialog
                onHistoryDelete(history)
                // Don't confirm the state change - we'll wait for the alert dialog response
                false
            } else {
                false
            }
        }
    )
    
    // Reset the dismiss state and alertShowing when the dialog is closed
    LaunchedEffect(key1 = vm.warningAlert.enable) {
        if (!vm.warningAlert.enable && alertShowing.value) {
            // Dialog was closed, reset states
            alertShowing.value = false
            dismissState.reset()
        }
    }
    
    // Also reset dismiss state if it's not in default position
    LaunchedEffect(key1 = dismissState.currentValue) {
        if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
            // Small delay to allow the swipe animation to complete
            delay(100)
            dismissState.reset()
        }
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    var isPressed by remember { mutableStateOf(false) }
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    // Animation for entering items
    val animatedProgress = remember { Animatable(initialValue = 0f) }
    LaunchedEffect(key1 = history.id) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(300, easing = EaseOutQuad)
        )
    }
    
    val scale by animatedProgress.asState()
    val alpha by animatedProgress.asState()
    
    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            DismissBackground(dismissState = dismissState)
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        content = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            isPressed = true
                            scope.launch {
                                delay(100)
                                isPressed = false
                                onClickItem(history)
                            }
                        },
                        onLongClick = {
                            showContextMenu = true
                        }
                    )
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Book cover
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onBookCover(history) }
                    ) {
                        BookListItemImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(8.dp)),
                            mangaCover = rememberBookCover(history)
                        )
                        // Progress indicator
                        if (history.progress > 0) {
                            LinearProgressIndicator(
                                progress = { history.progress.toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .align(Alignment.BottomCenter),
                                color = MaterialTheme.colorScheme.secondary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                    
                    // Book info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        HighlightedText(
                            text = history.title,
                            searchQuery = searchQuery,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        HighlightedText(
                            text = history.chapterName ?: "",
                            searchQuery = searchQuery,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .padding(start = 4.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    // Action buttons
                    IconButton(
                        onClick = { onClickPlay(history) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = localize(Res.string.resume),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    )
    
    // Context menu
    DropdownMenu(
        expanded = showContextMenu,
        onDismissRequest = { showContextMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text(localizeHelper.localize(Res.string.go_to_chapter)) },
            onClick = {
                showContextMenu = false
                onClickItem(history)
            },
            leadingIcon = {
                Icon(Icons.Default.MenuBook, contentDescription = null)
            }
        )
        
        DropdownMenuItem(
            text = { Text(localizeHelper.localize(Res.string.view_novel_details)) },
            onClick = {
                showContextMenu = false
                onBookCover(history)
            },
            leadingIcon = {
                Icon(Icons.Default.Info, contentDescription = null)
            }
        )
        
        HorizontalDivider()
        
        DropdownMenuItem(
            text = { Text(localizeHelper.localize(Res.string.remove_from_history)) },
            onClick = {
                showContextMenu = false
                onHistoryDelete(history)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DismissBackground(dismissState: androidx.compose.material3.SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection
    val color = when (direction) {
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
        SwipeToDismissBoxValue.Settled -> Color.Transparent
    }
    
    val alignment = when (direction) {
        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
        SwipeToDismissBoxValue.Settled -> Alignment.Center
    }
    
    val icon = when (direction) {
        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.PlayArrow
        SwipeToDismissBoxValue.Settled -> Icons.Default.Clear
    }
    
    val iconTint = when (direction) {
        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
        SwipeToDismissBoxValue.StartToEnd -> MaterialTheme.colorScheme.onPrimaryContainer
        SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.onSurface
    }
    
    val scale by animateFloatAsState(
        if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.8f else 1.2f
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color),
        contentAlignment = alignment
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
            tint = iconTint
        )
    }
}

@Composable
fun WarningAlertDialog(data: WarningAlertData) {
    AlertDialog(
        onDismissRequest = { 
            data.onDismiss.value?.invoke() ?: run {
                // Provide a default dismiss behavior
                data.enable = false
            }
        },
        title = { 
            Text(text = data.title.value?.toString() ?: "") 
        },
        text = { 
            Text(text = data.text.value?.toString() ?: "") 
        },
        confirmButton = {
            TextButton(
                onClick = {
                    data.onConfirm.value?.invoke() ?: run {
                        // Provide a default confirm behavior
                        data.enable = false
                    }
                }
            ) {
                Text(
                    text = localize(Res.string.confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    data.onDismiss.value?.invoke() ?: run {
                        // Provide a default dismiss behavior
                        data.enable = false
                    }
                }
            ) {
                Text(text = localize(Res.string.cancel))
            }
        }
    )
}

@Composable
fun HighlightedText(
    text: String,
    searchQuery: String,
    modifier: Modifier = Modifier,
    style: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    color: Color = Color.Unspecified
) {
    if (searchQuery.isBlank()) {
        Text(
            text = text,
            modifier = modifier,
            style = style,
            maxLines = maxLines,
            overflow = overflow,
            color = color
        )
    } else {
        val annotatedString = androidx.compose.ui.text.buildAnnotatedString {
            var currentIndex = 0
            val lowerText = text.lowercase()
            val lowerQuery = searchQuery.lowercase()
            
            while (currentIndex < text.length) {
                val matchIndex = lowerText.indexOf(lowerQuery, currentIndex)
                if (matchIndex == -1) {
                    append(text.substring(currentIndex))
                    break
                }
                
                // Add text before match
                if (matchIndex > currentIndex) {
                    append(text.substring(currentIndex, matchIndex))
                }
                
                // Add highlighted match
                withStyle(
                    style = androidx.compose.ui.text.SpanStyle(
                        background = MaterialTheme.colorScheme.primaryContainer,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(text.substring(matchIndex, matchIndex + searchQuery.length))
                }
                
                currentIndex = matchIndex + searchQuery.length
            }
        }
        
        Text(
            text = annotatedString,
            modifier = modifier,
            style = style,
            maxLines = maxLines,
            overflow = overflow,
            color = if (color != Color.Unspecified) color else LocalContentColor.current
        )
    }
}

/**
 * Enum representing the different states of the history screen
 * Used with derivedStateOf for efficient recomposition
 */
private enum class HistoryScreenState {
    NoSearchResults,
    Empty,
    Content
}

/**
 * Stable data class for grouped history items
 */
@Stable
private data class GroupedHistoryItems(
    val todayItems: List<HistoryWithRelations>,
    val yesterdayItems: List<HistoryWithRelations>,
    val thisWeekItems: List<HistoryWithRelations>,
    val earlierItems: List<HistoryWithRelations>
)
