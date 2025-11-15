package ireader.presentation.ui.home.sources.migration

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

/**
 * Source migration screen stub.
 * TODO: Implement full migration functionality
 * See spec: .kiro/specs/migration-feature/ (to be created)
 * 
 * Full implementation should:
 * - Display list of novels from source
 * - Allow selection of novels to migrate
 * - Show target source selector
 * - Handle migration process with progress
 * - Match novels between sources
 * - Preserve reading progress and bookmarks
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SourceMigrationScreen(
    viewModel: MigrationViewModel,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(localize(Res.string.migrate)) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Migration Feature Coming Soon",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "This feature will allow you to migrate your library between sources.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
