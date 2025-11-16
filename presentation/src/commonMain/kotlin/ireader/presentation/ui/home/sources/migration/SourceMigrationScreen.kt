package ireader.presentation.ui.home.sources.migration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Book
import ireader.domain.models.migration.MigrationMatch
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

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
    val novels by viewModel.novels.collectAsState()
    val selectedNovels by viewModel.selectedNovels.collectAsState()
    val targetSources by viewModel.targetSources.collectAsState()
    val currentMatchingNovel by viewModel.currentMatchingNovel.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val migrationProgress by viewModel.migrationProgress.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localize(Res.string.migrate)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Target Source",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        targetSources.forEach { source ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setTargetSource(source.sourceId) }
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = viewModel.targetSourceId == source.sourceId,
                                    onClick = { viewModel.setTargetSource(source.sourceId) }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = source.name)
                            }
                        }
                    }
                }
            }
            
            // Novel list
            if (novels.isNotEmpty() && !viewModel.isMigrating) {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(novels) { novel ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleNovelSelection(novel.id) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = selectedNovels.contains(novel.id),
                                onCheckedChange = { viewModel.toggleNovelSelection(novel.id) }
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = novel.title,
                                    style = MaterialTheme.typography.bodyLarge
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
                
                // Start migration button
                Button(
                    onClick = { viewModel.startMigration() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = selectedNovels.isNotEmpty() && viewModel.targetSourceId != null
                ) {
                    Text("Start Migration")
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
                        text = "No novels found in this source",
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (currentNovel != null) {
                Text(
                    text = "Migrating: ${currentNovel.title}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                if (isSearching) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Searching for matches...")
                    }
                } else if (matches.isNotEmpty()) {
                    Text(
                        text = "Select a match:",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    matches.forEach { match ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onMatchSelected(match) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = match.novel.title,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                if (match.novel.author.isNotBlank()) {
                                    Text(
                                        text = match.novel.author,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    text = "${(match.confidenceScore * 100).toInt()}% match - ${match.matchReason}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Skip")
                        }
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                } else {
                    Text("No matches found")
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSkip,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Skip")
                        }
                        OutlinedButton(
                            onClick = onCancel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
