//package ireader.presentation.ui.migration
//
//import androidx.compose.animation.*
//import androidx.compose.animation.core.*
//import androidx.compose.foundation.background
//import androidx.compose.foundation.border
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.shape.CircleShape
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material.icons.outlined.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.draw.scale
//import androidx.compose.ui.draw.shadow
//import androidx.compose.ui.graphics.Brush
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.text.style.TextOverflow
//import androidx.compose.ui.unit.dp
//import ireader.domain.models.entities.Book
//import ireader.domain.models.migration.MigrationFlags
//import ireader.domain.models.migration.MigrationSource
//
//// Type aliases to use ViewModel's state classes
//typealias MigrationListState = MigrationListScreenModel.State
//typealias MigrationSortOrder = MigrationListScreenModel.MigrationSortOrder
//
///**
// * Modern Migration List Screen with enhanced UI/UX - Main Entry Point
// */
//@Composable
//fun MigrationListScreen(
//    vm: MigrationListScreenModel = ireader.presentation.core.ui.getIViewModel(),
//    onMigrationComplete: () -> Unit = {}
//) {
//    val state by vm.state.collectAsState()
//    val snackbarHostState = remember { SnackbarHostState() }
//
//    // Log state changes for debugging
//    LaunchedEffect(state.showMigrationSuccessDialog, state.isMigrating) {
//        ireader.core.log.Log.info { "=== STATE CHANGED IN UI ===" }
//        ireader.core.log.Log.info { "showMigrationSuccessDialog: ${state.showMigrationSuccessDialog}" }
//        ireader.core.log.Log.info { "isMigrating: ${state.isMigrating}" }
//        ireader.core.log.Log.info { "migratedBooksCount: ${state.migratedBooksCount}" }
//        ireader.core.log.Log.info { "selectedBooks: ${state.selectedBooks.size}" }
//    }
//
//    // Show error messages via snackbar
//    LaunchedEffect(state.migrationMessage) {
//        state.migrationMessage?.let { message ->
//            // Only show error messages in snackbar, success is shown in dialog
//            if (message.contains("failed", ignoreCase = true) ||
//                message.contains("error", ignoreCase = true)) {
//                snackbarHostState.showSnackbar(
//                    message = message,
//                    duration = SnackbarDuration.Long
//                )
//            }
//        }
//    }
//
//    ModernMigrationListScreen(
//        state = state,
//        snackbarHostState = snackbarHostState,
//        onBookSelect = vm::selectBook,
//        onSelectAll = vm::selectAllBooks,
//        onClearSelection = vm::clearSelection,
//        onSearchQueryChange = vm::updateSearchQuery,
//        onSortOrderChange = vm::updateSortOrder,
//        onToggleShowOnlyMigratable = vm::toggleShowOnlyMigratable,
//        onStartMigration = vm::startMigration,
//        onDismissSuccessDialog = {
//            ireader.core.log.Log.info { "Success dialog dismissed, navigating back" }
//            vm.dismissMigrationSuccessDialog()
//            onMigrationComplete()
//        }
//    )
//}
//
///**
// * Modern Migration List Screen with enhanced UI/UX - Composable Content
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun ModernMigrationListScreen(
//    state: MigrationListState,
//    snackbarHostState: SnackbarHostState,
//    onBookSelect: (Long) -> Unit,
//    onSelectAll: () -> Unit,
//    onClearSelection: () -> Unit,
//    onSearchQueryChange: (String) -> Unit,
//    onSortOrderChange: (MigrationSortOrder) -> Unit,
//    onToggleShowOnlyMigratable: () -> Unit,
//    onStartMigration: (List<Long>, MigrationFlags) -> Unit,
//    onDismissSuccessDialog: () -> Unit
//) {
//    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
//
//    Scaffold(
//        snackbarHost = { SnackbarHost(snackbarHostState) },
//        topBar = {
//            LargeTopAppBar(
//                title = {
//                    Column {
//                        Text(
//                            "Source Migration",
//                            style = MaterialTheme.typography.headlineMedium,
//                            fontWeight = FontWeight.Bold
//                        )
//                        AnimatedVisibility(visible = state.selectedBooks.isNotEmpty()) {
//                            Text(
//                                "${state.selectedBooks.size} book${if (state.selectedBooks.size > 1) "s" else ""} selected",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.primary
//                            )
//                        }
//                    }
//                },
//                actions = {
//                    AnimatedVisibility(visible = state.selectedBooks.isNotEmpty()) {
//                        Row {
//                            IconButton(onClick = onSelectAll) {
//                                Icon(Icons.Default.SelectAll, "Select All")
//                            }
//                            IconButton(onClick = onClearSelection) {
//                                Icon(Icons.Default.Clear, "Clear")
//                            }
//                        }
//                    }
//                },
//                scrollBehavior = scrollBehavior,
//                colors = TopAppBarDefaults.largeTopAppBarColors(
//                    containerColor = MaterialTheme.colorScheme.surface,
//                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
//                )
//            )
//        },
//        floatingActionButton = {
//            AnimatedVisibility(
//                visible = state.selectedBooks.isNotEmpty() && !state.isMigrating,
//                enter = scaleIn(spring(stiffness = Spring.StiffnessLow)) + fadeIn(),
//                exit = scaleOut(spring(stiffness = Spring.StiffnessLow)) + fadeOut()
//            ) {
//                ExtendedFloatingActionButton(
//                    onClick = {
//                        // Start migration with all available sources
//                        onStartMigration(emptyList(), MigrationFlags())
//                    },
//                    icon = {
//                        if (state.isMigrating) {
//                            CircularProgressIndicator(
//                                modifier = Modifier.size(24.dp),
//                                strokeWidth = 2.dp,
//                                color = MaterialTheme.colorScheme.onPrimaryContainer
//                            )
//                        } else {
//                            Icon(Icons.Default.SwapHoriz, null)
//                        }
//                    },
//                    text = {
//                        Text(
//                            if (state.isMigrating) "Migrating..."
//                            else "Migrate ${state.selectedBooks.size}"
//                        )
//                    },
//                    containerColor = MaterialTheme.colorScheme.primaryContainer,
//                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
//                )
//            }
//        }
//    ) { paddingValues ->
//        Column(
//            modifier = Modifier
//                .fillMaxSize()
//                .padding(paddingValues)
//        ) {
//            ModernMigrationFilterBar(
//                searchQuery = state.searchQuery,
//                sortOrder = state.sortOrder,
//                showOnlyMigratable = state.showOnlyMigratableBooks,
//                onSearchQueryChange = onSearchQueryChange,
//                onSortOrderChange = onSortOrderChange,
//                onToggleShowOnlyMigratable = onToggleShowOnlyMigratable
//            )
//
//            when {
//                state.isLoading -> {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.spacedBy(16.dp)
//                        ) {
//                            CircularProgressIndicator()
//                            Text("Loading books...", style = MaterialTheme.typography.bodyMedium)
//                        }
//                    }
//                }
//                state.books.isEmpty() -> {
//                    Box(
//                        modifier = Modifier.fillMaxSize(),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            horizontalAlignment = Alignment.CenterHorizontally,
//                            verticalArrangement = Arrangement.spacedBy(16.dp),
//                            modifier = Modifier.padding(32.dp)
//                        ) {
//                            Icon(
//                                Icons.Outlined.SwapHoriz,
//                                null,
//                                modifier = Modifier.size(80.dp),
//                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
//                            )
//                            Text(
//                                "No books to migrate",
//                                style = MaterialTheme.typography.titleLarge,
//                                fontWeight = FontWeight.Medium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant
//                            )
//                            Text(
//                                "Add books to your library first",
//                                style = MaterialTheme.typography.bodyMedium,
//                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
//                                textAlign = TextAlign.Center
//                            )
//                        }
//                    }
//                }
//                else -> {
//                    LazyColumn(
//                        modifier = Modifier.fillMaxSize(),
//                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
//                        verticalArrangement = Arrangement.spacedBy(12.dp)
//                    ) {
//                        items(state.books, key = { it.id }) { book ->
//                            ModernMigrationBookCard(
//                                book = book,
//                                isSelected = book.id in state.selectedBooks,
//                                onSelect = { onBookSelect(book.id) }
//                            )
//                        }
//                        item { Spacer(Modifier.height(80.dp)) }
//                    }
//                }
//            }
//        }
//
//        // Success Dialog
//        if (state.showMigrationSuccessDialog) {
//            ireader.core.log.Log.info { "=== RENDERING SUCCESS DIALOG ===" }
//            ireader.core.log.Log.info { "Migrated count: ${state.migratedBooksCount}" }
//            MigrationSuccessDialog(
//                migratedCount = state.migratedBooksCount,
//                onDismiss = onDismissSuccessDialog
//            )
//        } else {
//            ireader.core.log.Log.debug { "Success dialog not shown: showMigrationSuccessDialog = ${state.showMigrationSuccessDialog}" }
//        }
//    }
//}
//
//@Composable
//private fun MigrationSuccessDialog(
//    migratedCount: Int,
//    onDismiss: () -> Unit
//) {
//    AlertDialog(
//        onDismissRequest = onDismiss,
//        icon = {
//            Icon(
//                Icons.Default.CheckCircle,
//                null,
//                tint = MaterialTheme.colorScheme.primary,
//                modifier = Modifier.size(48.dp)
//            )
//        },
//        title = {
//            Text(
//                "Migration Started!",
//                style = MaterialTheme.typography.headlineSmall,
//                fontWeight = FontWeight.Bold
//            )
//        },
//        text = {
//            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                Text(
//                    "Successfully queued $migratedCount book${if (migratedCount > 1) "s" else ""} for migration.",
//                    style = MaterialTheme.typography.bodyLarge
//                )
//                Text(
//                    "The migration process will search for these books in other sources and transfer your reading data.",
//                    style = MaterialTheme.typography.bodyMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//                Text(
//                    "Note: This feature is still in development. Check the logs for migration progress.",
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
//                )
//            }
//        },
//        confirmButton = {
//            Button(onClick = onDismiss) {
//                Text("Got it!")
//            }
//        }
//    )
//}
//
//@Composable
//private fun ModernMigrationFilterBar(
//    searchQuery: String,
//    sortOrder: MigrationSortOrder,
//    showOnlyMigratable: Boolean,
//    onSearchQueryChange: (String) -> Unit,
//    onSortOrderChange: (MigrationSortOrder) -> Unit,
//    onToggleShowOnlyMigratable: () -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(MaterialTheme.colorScheme.surface)
//            .padding(16.dp),
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        OutlinedTextField(
//            value = searchQuery,
//            onValueChange = onSearchQueryChange,
//            label = { Text("Search books") },
//            leadingIcon = { Icon(Icons.Default.Search, null) },
//            trailingIcon = {
//                if (searchQuery.isNotEmpty()) {
//                    IconButton(onClick = { onSearchQueryChange("") }) {
//                        Icon(Icons.Default.Clear, "Clear")
//                    }
//                }
//            },
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(16.dp),
//            singleLine = true
//        )
//
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            FilterChip(
//                selected = showOnlyMigratable,
//                onClick = onToggleShowOnlyMigratable,
//                label = { Text("Migratable only") },
//                leadingIcon = {
//                    Icon(
//                        if (showOnlyMigratable) Icons.Default.CheckCircle else Icons.Outlined.Circle,
//                        null,
//                        modifier = Modifier.size(18.dp)
//                    )
//                }
//            )
//
//            var showSortMenu by remember { mutableStateOf(false) }
//            FilterChip(
//                selected = false,
//                onClick = { showSortMenu = true },
//                label = { Text("Sort: ${sortOrder.displayName}") },
//                trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
//            )
//
//            DropdownMenu(
//                expanded = showSortMenu,
//                onDismissRequest = { showSortMenu = false }
//            ) {
//                MigrationSortOrder.values().forEach { order ->
//                    DropdownMenuItem(
//                        text = { Text(order.displayName) },
//                        onClick = {
//                            onSortOrderChange(order)
//                            showSortMenu = false
//                        }
//                    )
//                }
//            }
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
//@Composable
//private fun ModernMigrationBookCard(
//    book: Book,
//    isSelected: Boolean,
//    onSelect: () -> Unit
//) {
//    val scaleValue by animateFloatAsState(
//        targetValue = if (isSelected) 0.98f else 1f,
//        animationSpec = spring(stiffness = Spring.StiffnessMedium)
//    )
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .scale(scaleValue),
//        onClick = onSelect,
//        shape = RoundedCornerShape(16.dp),
//        colors = CardDefaults.cardColors(
//            containerColor = if (isSelected) {
//                MaterialTheme.colorScheme.primaryContainer
//            } else {
//                MaterialTheme.colorScheme.surfaceContainerHigh
//            }
//        ),
//        elevation = CardDefaults.cardElevation(
//            defaultElevation = if (isSelected) 4.dp else 2.dp
//        )
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(16.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            AnimatedContent(
//                targetState = isSelected,
//                transitionSpec = {
//                    scaleIn() + fadeIn() togetherWith scaleOut() + fadeOut()
//                },
//                label = "selection"
//            ) { selected ->
//                Box(
//                    modifier = Modifier
//                        .size(48.dp)
//                        .clip(CircleShape)
//                        .background(
//                            if (selected) MaterialTheme.colorScheme.primary
//                            else MaterialTheme.colorScheme.surfaceVariant
//                        ),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Icon(
//                        if (selected) Icons.Default.CheckCircle else Icons.Outlined.Circle,
//                        null,
//                        tint = if (selected) MaterialTheme.colorScheme.onPrimary
//                        else MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//
//            Column(
//                modifier = Modifier.weight(1f),
//                verticalArrangement = Arrangement.spacedBy(4.dp)
//            ) {
//                Text(
//                    text = book.title,
//                    style = MaterialTheme.typography.titleMedium,
//                    fontWeight = FontWeight.SemiBold,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis,
//                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
//                    else MaterialTheme.colorScheme.onSurface
//                )
//
//                if (book.author.isNotBlank()) {
//                    Row(verticalAlignment = Alignment.CenterVertically) {
//                        Icon(
//                            Icons.Outlined.Person,
//                            null,
//                            modifier = Modifier.size(14.dp),
//                            tint = MaterialTheme.colorScheme.onSurfaceVariant
//                        )
//                        Spacer(Modifier.width(4.dp))
//                        Text(
//                            text = book.author,
//                            style = MaterialTheme.typography.bodyMedium,
//                            color = MaterialTheme.colorScheme.onSurfaceVariant,
//                            maxLines = 1,
//                            overflow = TextOverflow.Ellipsis
//                        )
//                    }
//                }
//
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Icon(
//                        Icons.Outlined.Source,
//                        null,
//                        modifier = Modifier.size(14.dp),
//                        tint = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                    Spacer(Modifier.width(4.dp))
//                    Text(
//                        text = "Source: ${book.sourceId}",
//                        style = MaterialTheme.typography.bodySmall,
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                }
//            }
//
//            Icon(
//                Icons.Default.SwapHoriz,
//                "Migrate",
//                tint = if (isSelected) MaterialTheme.colorScheme.primary
//                else MaterialTheme.colorScheme.onSurfaceVariant,
//                modifier = Modifier.size(28.dp)
//            )
//        }
//    }
//}
//
//
