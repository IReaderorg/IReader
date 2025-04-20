package ireader.presentation.ui.home.history

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissState
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.HistoryWithRelations
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.domain.models.BookCover
import ireader.presentation.ui.component.BookListItemImage
import ireader.presentation.ui.component.components.BookImageCover
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.reusable_composable.WarningAlertData
import ireader.presentation.ui.core.coil.rememberBookCover
import ireader.presentation.ui.core.ui.EmptyScreen
import ireader.presentation.ui.home.history.viewmodel.HistoryViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
    val items = vm.histories
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val searchFocusRequester = remember { FocusRequester() }
    
    // Observe when back in history
    LaunchedEffect(vm.searchQuery) {
        vm.applySearchFilter()
    }
    
    // Observe refreshTrigger for UI updates
    val refreshTrigger = vm.refreshTrigger
    
    Scaffold(
        topBar = {
            HistoryTopAppBar(
                searchMode = vm.searchMode,
                searchQuery = vm.searchQuery,
                onSearchModeChange = { vm.toggleSearchMode() },
                onSearchQueryChange = { vm.onSearchQueryChange(it) },
                focusRequester = searchFocusRequester,
                onClearClick = { vm.onSearchQueryChange("") }
            )
        },
        floatingActionButton = {
            if (items.values.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { vm.deleteAllHistories() },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = localize(MR.strings.delete_all_histories),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = modifier
                .fillMaxSize()

        ) {
            when {
                items.values.flatten().isEmpty() && vm.searchQuery.isNotEmpty() -> EmptyScreen(
                    text = localize(MR.strings.no_matches_found_in_search) + " \"${vm.searchQuery}\"",
                    modifier = Modifier.fillMaxSize()
                )
                items.values.isEmpty() -> EmptyScreen(
                    text = localize(MR.strings.nothing_read_recently),
                    modifier = Modifier.fillMaxSize()
                )
                else -> HistoryContent(
                    items = items,
                    listState = listState,
                    onClickItem = onHistory,
                    onClickDelete = { history -> vm.deleteHistory(history) },
                    onClickPlay = onHistoryPlay,
                    onBookCover = onBookCover,
                    onLongClickDelete = { history -> vm.deleteHistory(history) },
                )
            }
            
            // Only show warning alert if enabled
            if (vm.warningAlert.enable.value) {
                WarningAlertDialog(data = vm.warningAlert)
            }
        }
    }
    
    // Focus the search field when search mode is activated
    LaunchedEffect(vm.searchMode) {
        if (vm.searchMode) {
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
    onClearClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    if (searchMode) {
        TextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .focusRequester(focusRequester),
            placeholder = { Text(localize(MR.strings.search_history)) },
            leadingIcon = { 
                IconButton(onClick = onSearchModeChange) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = localize(MR.strings.close)
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
                            contentDescription = localize(MR.strings.clear)
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
            title = { Text(localize(MR.strings.history)) },
            actions = {
                IconButton(onClick = onSearchModeChange) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = localize(MR.strings.search)
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
    onLongClickDelete: (HistoryWithRelations) -> Unit
) {
    // Group history items by time period
    val now = System.currentTimeMillis()
    val calendar = Calendar.getInstance()
    val today = calendar.apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    // Copy the calendar and subtract days
    val yesterday = calendar.apply {
        add(Calendar.DAY_OF_MONTH, -1)
    }.timeInMillis
    
    val lastWeek = calendar.apply {
        add(Calendar.DAY_OF_MONTH, -6) // Now 7 days ago in total
    }.timeInMillis
    
    // Group history items
    val todayItems = mutableListOf<HistoryWithRelations>()
    val yesterdayItems = mutableListOf<HistoryWithRelations>()
    val thisWeekItems = mutableListOf<HistoryWithRelations>()
    val earlierItems = mutableListOf<HistoryWithRelations>()
    
    items.values.flatten().forEach { history ->
        when {
            history.readAt >= today -> todayItems.add(history)
            history.readAt >= yesterday -> yesterdayItems.add(history)
            history.readAt >= lastWeek -> thisWeekItems.add(history)
            else -> earlierItems.add(history)
        }
    }
    
    // Format for showing time
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {
            // Today section
            if (todayItems.isNotEmpty()) {
                item {
                    HistoryTimeHeader(title = localize(MR.strings.relative_time_today))
                }
                
                items(todayItems.sortedByDescending { it.readAt }) { history ->
                    HistoryItem(
                        history = history,
                        timeString = timeFormat.format(Date(history.readAt)),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete
                    )
                }
            }
            
            // Yesterday section
            if (yesterdayItems.isNotEmpty()) {
                item {
                    HistoryTimeHeader(title = localize(MR.strings.yesterday))
                }
                
                items(yesterdayItems.sortedByDescending { it.readAt }) { history ->
                    HistoryItem(
                        history = history,
                        timeString = timeFormat.format(Date(history.readAt)),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete
                    )
                }
            }
            
            // This week section
            if (thisWeekItems.isNotEmpty()) {
                item {
                    HistoryTimeHeader(title = localize(MR.strings.weekly))
                }
                
                items(thisWeekItems.sortedByDescending { it.readAt }) { history ->
                    HistoryItem(
                        history = history,
                        timeString = dateFormat.format(Date(history.readAt)),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete
                    )
                }
            }
            
            // Earlier section
            if (earlierItems.isNotEmpty()) {
                item {
                    HistoryTimeHeader(title = localize(MR.strings.recently))
                }
                
                items(earlierItems.sortedByDescending { it.readAt }) { history ->
                    HistoryItem(
                        history = history,
                        timeString = dateFormat.format(Date(history.readAt)),
                        onClickItem = onClickItem,
                        onClickDelete = onClickDelete,
                        onClickPlay = onClickPlay,
                        onBookCover = onBookCover,
                        onLongClickDelete = onLongClickDelete,
                        onHistoryDelete = onClickDelete
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
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
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
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
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
) {
    val scope = rememberCoroutineScope()
    val dismissState = rememberDismissState(
        confirmStateChange = {
            if (it == DismissValue.DismissedToStart) {
                onHistoryDelete(history)
                return@rememberDismissState true
            }
            false
        }
    )
    
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
    
    SwipeToDismiss(
        state = dismissState,
        background = {
            DismissBackground(dismissState = dismissState)
        },
        dismissContent = {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        isPressed = true
                        scope.launch {
                            delay(100)
                            isPressed = false
                            onClickItem(history)
                        }
                    }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation)
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
                                color = MaterialTheme.colorScheme.primary,
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
                        Text(
                            text = history.title,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        Text(
                            text = history.chapterName ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.alpha(0.7f)
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = timeString,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .alpha(0.6f)
                            )
                        }
                    }
                    
                    // Action buttons
                    IconButton(
                        onClick = { onClickPlay(history) },
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = localize(MR.strings.resume),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        },
        directions = setOf(DismissDirection.EndToStart)
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
private fun DismissBackground(dismissState: DismissState) {
    val color = when (dismissState.dismissDirection) {
        DismissDirection.EndToStart -> MaterialTheme.colorScheme.errorContainer
        DismissDirection.StartToEnd -> MaterialTheme.colorScheme.primaryContainer
        null -> Color.Transparent
    }
    
    val alignment = when (dismissState.dismissDirection) {
        DismissDirection.EndToStart -> Alignment.CenterEnd
        DismissDirection.StartToEnd -> Alignment.CenterStart
        null -> Alignment.Center
    }
    
    val icon = when (dismissState.dismissDirection) {
        DismissDirection.EndToStart -> Icons.Default.Delete
        DismissDirection.StartToEnd -> Icons.Default.PlayArrow
        null -> Icons.Default.Clear
    }
    
    val iconTint = when (dismissState.dismissDirection) {
        DismissDirection.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
        DismissDirection.StartToEnd -> MaterialTheme.colorScheme.onPrimaryContainer
        null -> MaterialTheme.colorScheme.onSurface
    }
    
    val scale by animateFloatAsState(
        if (dismissState.targetValue == DismissValue.Default) 0.8f else 1.2f
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
                data.enable.value = false
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
                        data.enable.value = false
                    }
                }
            ) {
                Text(
                    text = localize(MR.strings.confirm),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    data.onDismiss.value?.invoke() ?: run {
                        // Provide a default dismiss behavior
                        data.enable.value = false
                    }
                }
            ) {
                Text(text = localize(MR.strings.cancel))
            }
        }
    )
}
