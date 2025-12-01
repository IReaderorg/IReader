package ireader.presentation.ui.home.sources.migration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Book
import ireader.domain.models.migration.MigrationMatch
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Source migration screen - allows users to migrate books from one source to another
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceMigrationScreen(
    viewModel: MigrationViewModel,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val novels by viewModel.novels.collectAsState()
    val selectedNovels by viewModel.selectedNovels.collectAsState()
    val targetSources by viewModel.targetSources.collectAsState()
    val currentMatchingNovel by viewModel.currentMatchingNovel.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val migrationProgress by viewModel.migrationProgress.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(localize(Res.string.migrate))
                        if (viewModel.isMigrating && selectedNovels.isNotEmpty()) {
                            Text(
                                text = "${migrationProgress.size}/${selectedNovels.size} migrated",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
                    }
                },
                actions = {
                    if (novels.isNotEmpty() && !viewModel.isMigrating) {
                        TextButton(
                            onClick = {
                                if (selectedNovels.isEmpty()) {
                                    viewModel.selectAll()
                                } else {
                                    viewModel.deselectAll()
                                }
                            }
                        ) {
                            Text(if (selectedNovels.isEmpty()) "Select All" else "Deselect All")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Target source selector
            if (targetSources.isNotEmpty() && !viewModel.isMigrating) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = localizeHelper.localize(Res.string.select_target_source),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.choose_where_to_migrate_your_books),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        targetSources.forEach { source ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.setTargetSource(source.sourceId) },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (viewModel.targetSourceId == source.sourceId) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = viewModel.targetSourceId == source.sourceId,
                                        onClick = { viewModel.setTargetSource(source.sourceId) }
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = source.name,
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                        Text(
                                            text = source.source?.lang?.uppercase() ?: "UNKNOWN",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Novel list
            if (novels.isNotEmpty() && !viewModel.isMigrating) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = localizeHelper.localize(Res.string.select_books_to_migrate),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${selectedNovels.size}/${novels.size} selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        LazyColumn(
                            modifier = Modifier.weight(1f)
                        ) {
                            items(novels) { novel ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 4.dp)
                                        .clickable { viewModel.toggleNovelSelection(novel.id) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedNovels.contains(novel.id))
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedNovels.contains(novel.id),
                                            onCheckedChange = { viewModel.toggleNovelSelection(novel.id) }
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = novel.title,
                                                style = MaterialTheme.typography.bodyLarge,
                                                maxLines = 2,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                            )
                                            if (novel.author.isNotBlank()) {
                                                Text(
                                                    text = novel.author,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Start migration button
                Button(
                    onClick = { viewModel.startMigration() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = selectedNovels.isNotEmpty() && viewModel.targetSourceId != null
                ) {
                    Text(
                        text = if (selectedNovels.isEmpty()) 
                            "Select books to migrate" 
                        else if (viewModel.targetSourceId == null)
                            "Select target source"
                        else
                            "Start Migration (${selectedNovels.size} books)"
                    )
                }
            }
            
            // Migration progress
            if (viewModel.isMigrating || currentMatchingNovel != null) {
                MigrationProgressView(
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
            
            // Loading state
            if (viewModel.isLoadingNovels) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            // Empty state
            if (novels.isEmpty() && !viewModel.isLoadingNovels) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.no_novels_found_in_this_source),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun MigrationProgressView(
    currentNovel: Book?,
    matches: List<MigrationMatch>,
    isSearching: Boolean,
    onMatchSelected: (MigrationMatch) -> Unit,
    onSkip: () -> Unit,
    onCancel: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (currentNovel != null) {
                Text(
                    text = localizeHelper.localize(Res.string.migrating_book),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = currentNovel.title,
                            style = MaterialTheme.typography.bodyLarge
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
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isSearching) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = localizeHelper.localize(Res.string.searching_for_matches_in_target_source),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else if (matches.isNotEmpty()) {
                    Text(
                        text = "Found ${matches.size} potential match${if (matches.size > 1) "es" else ""}:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    matches.forEach { match ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onMatchSelected(match) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = match.novel.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Text(
                                            text = "${(match.confidenceScore * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                                if (match.novel.author.isNotBlank()) {
                                    Text(
                                        text = match.novel.author,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = match.matchReason,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(localizeHelper.localize(Res.string.skip_this_book))
                        }
                        Button(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(localizeHelper.localize(Res.string.cancel_migration))
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = localizeHelper.localize(Res.string.no_matches_found),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = localizeHelper.localize(Res.string.the_book_could_not_be_found_in_the_target_source),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(localizeHelper.localize(Res.string.skip_this_book))
                        }
                        Button(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(localizeHelper.localize(Res.string.cancel_migration))
                        }
                    }
                }
            }
        }
    }
}
