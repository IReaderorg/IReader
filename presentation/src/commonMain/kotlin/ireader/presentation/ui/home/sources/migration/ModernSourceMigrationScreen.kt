package ireader.presentation.ui.home.sources.migration

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.migration.MigrationMatch
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.back
import ireader.i18n.resources.cancel
import ireader.i18n.resources.choose_where_to_migrate_your_books
import ireader.i18n.resources.done
import ireader.i18n.resources.loading_novels
import ireader.i18n.resources.looking_in_target_source
import ireader.i18n.resources.migrate
import ireader.i18n.resources.migrating_book
import ireader.i18n.resources.migration_complete
import ireader.i18n.resources.no_matches_found
import ireader.i18n.resources.no_novels_found
import ireader.i18n.resources.searching_for_matches
import ireader.i18n.resources.select_books_to_migrate
import ireader.i18n.resources.select_target_source
import ireader.i18n.resources.selected
import ireader.i18n.resources.skip
import ireader.i18n.resources.the_book_could_not_be_found_in_the_target_source
import ireader.i18n.resources.there_are_no_novels_in_this_source_to_migrate
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Modern redesigned Source Migration Screen
 * Features:
 * - Card-based design with elevation
 * - Smooth animations and transitions
 * - Better visual feedback
 * - Enhanced progress indicators
 * - Modern color scheme
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernSourceMigrationScreen(
    viewModel: MigrationViewModel,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val novels by viewModel.novels.collectAsState()
    val selectedNovels by viewModel.selectedNovels.collectAsState()
    val targetSources by viewModel.targetSources.collectAsState()
    val currentMatchingNovel by viewModel.currentMatchingNovel.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val migrationProgress by viewModel.migrationProgress.collectAsState()
    val migrationResults by viewModel.migrationResults.collectAsState()
    
    // Track if migration just completed
    var showCompletionDialog by remember { mutableStateOf(false) }
    
    // Show completion dialog when migration finishes
    LaunchedEffect(viewModel.isMigrating, migrationResults) {
        if (!viewModel.isMigrating && migrationResults.isNotEmpty() && currentMatchingNovel == null) {
            showCompletionDialog = true
        }
    }
    
    // Convert BookItem list to Book list for display
    val booksForDisplay = remember(novels) {
        novels.map { bookItem ->
            Book(
                id = bookItem.id,
                sourceId = bookItem.sourceId,
                title = bookItem.title,
                key = bookItem.key,
                author = bookItem.author,
                description = bookItem.description,
                genres = emptyList(),
                status = 0L,
                cover = bookItem.cover,
                customCover = bookItem.customCover,
                favorite = bookItem.favorite,
                lastUpdate = 0L,
                initialized = false,
                dateAdded = 0L,
                viewer = 0L,
                flags = 0L,
                isPinned = false,
                pinnedOrder = 0,
                isArchived = false
            )
        }
    }
    
    Scaffold(
        topBar = {
            ModernMigrationTopBar(
                onBackPressed = onBackPressed,
                isMigrating = viewModel.isMigrating,
                migrationProgress = migrationProgress.size,
                totalNovels = selectedNovels.size,
                hasNovels = novels.isNotEmpty(),
                selectedCount = selectedNovels.size,
                onSelectAll = { viewModel.selectAll() },
                onDeselectAll = { viewModel.deselectAll() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                viewModel.isLoadingNovels -> {
                    LoadingState()
                }
                novels.isEmpty() -> {
                    EmptyState()
                }
                viewModel.isMigrating || currentMatchingNovel != null -> {
                    ModernMigrationProgressView(
                        currentNovel = currentMatchingNovel,
                        matches = matches,
                        isSearching = viewModel.isSearchingMatches,
                        onMatchSelected = { match ->
                            currentMatchingNovel?.let { novel ->
                                viewModel.migrateNovel(novel.id, match.novel)
                            }
                        },
                        onSkip = { viewModel.skipCurrentNovel() },
                        onCancel = { viewModel.cancelMigration() }
                    )
                }
                else -> {
                    MigrationSetupView(
                        novels = booksForDisplay,
                        selectedNovels = selectedNovels,
                        targetSources = targetSources,
                        targetSourceId = viewModel.targetSourceId,
                        onToggleNovel = { viewModel.toggleNovelSelection(it) },
                        onSetTargetSource = { viewModel.setTargetSource(it) },
                        onStartMigration = { viewModel.startMigration() }
                    )
                }
            }
            
            // Migration Completion Dialog
            if (showCompletionDialog) {
                MigrationCompletionDialog(
                    results = migrationResults,
                    onDismiss = {
                        showCompletionDialog = false
                        onBackPressed() // Navigate back after dismissing
                    }
                )
            }
        }
    }
}

@Composable
private fun MigrationCompletionDialog(
    results: List<ireader.domain.models.migration.MigrationResult>,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val successCount = results.count { it.success }
    val failedCount = results.size - successCount
    
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (failedCount == 0) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = if (failedCount == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(48.dp)
            )
        },
        title = {
            Text(
                text = localizeHelper.localize(Res.string.migration_complete),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Summary
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Successful:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "$successCount",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (failedCount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Failed/Skipped:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "$failedCount",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Text(
                    text = if (failedCount == 0) {
                        "All books were successfully migrated to the new source!"
                    } else {
                        "Some books could not be migrated. They may not exist in the target source or were skipped."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(localizeHelper.localize(Res.string.done))
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModernMigrationTopBar(
    onBackPressed: () -> Unit,
    isMigrating: Boolean,
    migrationProgress: Int,
    totalNovels: Int,
    hasNovels: Boolean,
    selectedCount: Int,
    onSelectAll: () -> Unit,
    onDeselectAll: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    TopAppBar(
        title = {
            Column {
                Text(
                    text = localize(Res.string.migrate),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (isMigrating && totalNovels > 0) {
                    Text(
                        text = "$migrationProgress / $totalNovels migrated",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = localizeHelper.localize(Res.string.back)
                )
            }
        },
        actions = {
            if (hasNovels && !isMigrating) {
                TextButton(
                    onClick = if (selectedCount == 0) onSelectAll else onDeselectAll
                ) {
                    Text(if (selectedCount == 0) "Select All" else "Deselect All")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
private fun MigrationSetupView(
    novels: List<Book>,
    selectedNovels: Set<Long>,
    targetSources: List<CatalogLocal>,
    targetSourceId: Long?,
    onToggleNovel: (Long) -> Unit,
    onSetTargetSource: (Long) -> Unit,
    onStartMigration: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Target source selector
        if (targetSources.isNotEmpty()) {
            item {
                ModernTargetSourceSelector(
                    targetSources = targetSources,
                    selectedSourceId = targetSourceId,
                    onSelectSource = onSetTargetSource
                )
            }
        }
        
        // Novel selection header
        item {
            ModernSectionHeader(
                title = localizeHelper.localize(Res.string.select_books_to_migrate),
                subtitle = "${selectedNovels.size} / ${novels.size} selected"
            )
        }
        
        // Novel list
        items(novels) { novel ->
            ModernNovelCard(
                novel = novel,
                isSelected = selectedNovels.contains(novel.id),
                onToggle = { onToggleNovel(novel.id) }
            )
        }
        
        // Start migration button
        item {
            ModernMigrationButton(
                selectedCount = selectedNovels.size,
                hasTargetSource = targetSourceId != null,
                onStartMigration = onStartMigration
            )
        }
    }
}

@Composable
private fun ModernTargetSourceSelector(
    targetSources: List<CatalogLocal>,
    selectedSourceId: Long?,
    onSelectSource: (Long) -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = localizeHelper.localize(Res.string.select_target_source),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = localizeHelper.localize(Res.string.choose_where_to_migrate_your_books),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                targetSources.forEach { source ->
                    ModernSourceOption(
                        source = source,
                        isSelected = selectedSourceId == source.sourceId,
                        onSelect = { onSelectSource(source.sourceId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernSourceOption(
    source: CatalogLocal,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect,
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.primary
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = source.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = source.source?.lang?.uppercase() ?: "UNKNOWN",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = localizeHelper.localize(Res.string.selected),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernSectionHeader(
    title: String,
    subtitle: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondary
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernNovelCard(
    novel: Book,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 3.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary
                )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = novel.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (novel.author.isNotBlank()) {
                    Text(
                        text = novel.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = localizeHelper.localize(Res.string.selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernMigrationButton(
    selectedCount: Int,
    hasTargetSource: Boolean,
    onStartMigration: () -> Unit
) {
    Button(
        onClick = onStartMigration,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = selectedCount > 0 && hasTargetSource,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when {
                selectedCount == 0 -> "Select books to migrate"
                !hasTargetSource -> "Select target source"
                else -> "Start Migration ($selectedCount books)"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ModernMigrationProgressView(
    currentNovel: Book?,
    matches: List<MigrationMatch>,
    isSearching: Boolean,
    onMatchSelected: (MigrationMatch) -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (currentNovel != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = localizeHelper.localize(Res.string.migrating_book),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = currentNovel.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                if (currentNovel.author.isNotBlank()) {
                                    Text(
                                        text = currentNovel.author,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            if (isSearching) {
                item {
                    ModernSearchingCard()
                }
            } else if (matches.isNotEmpty()) {
                item {
                    Text(
                        text = "Found ${matches.size} potential match${if (matches.size > 1) "es" else ""}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                
                items(matches) { match ->
                    ModernMatchCard(
                        match = match,
                        onSelect = { onMatchSelected(match) }
                    )
                }
                
                item {
                    ModernMigrationActions(
                        onSkip = onSkip,
                        onCancel = onCancel
                    )
                }
            } else {
                item {
                    ModernNoMatchCard()
                }
                item {
                    ModernMigrationActions(
                        onSkip = onSkip,
                        onCancel = onCancel
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernSearchingCard() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp
            )
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.searching_for_matches),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = localizeHelper.localize(Res.string.looking_in_target_source),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModernMatchCard(
    match: MigrationMatch,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = match.novel.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = "${(match.confidenceScore * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            if (match.novel.author.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                    )
                    Text(
                        text = match.novel.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
            
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Text(
                    text = match.matchReason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernNoMatchCard() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = localizeHelper.localize(Res.string.no_matches_found),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = localizeHelper.localize(Res.string.the_book_could_not_be_found_in_the_target_source),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun ModernMigrationActions(
    onSkip: () -> Unit,
    onCancel: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onSkip,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(localizeHelper.localize(Res.string.skip))
        }
        Button(
            onClick = onCancel,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Cancel,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(localizeHelper.localize(Res.string.cancel))
        }
    }
}

@Composable
private fun LoadingState() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                text = localizeHelper.localize(Res.string.loading_novels),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun EmptyState() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LibraryBooks,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
                text = localizeHelper.localize(Res.string.no_novels_found),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = localizeHelper.localize(Res.string.there_are_no_novels_in_this_source_to_migrate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
