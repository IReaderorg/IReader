package ireader.presentation.ui.reader.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * State for find in chapter functionality
 */
data class FindInChapterState(
    val query: String = "",
    val matches: List<IntRange> = emptyList(),
    val currentMatchIndex: Int = 0
) {
    val totalMatches: Int get() = matches.size
    val hasMatches: Boolean get() = matches.isNotEmpty()
    val currentMatch: IntRange? get() = matches.getOrNull(currentMatchIndex)
}

/**
 * Find in chapter search bar component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindInChapterBar(
    state: FindInChapterState,
    onQueryChange: (String) -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Search text field
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                placeholder = { Text("Find in chapter") },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            
            // Match counter
            if (state.query.isNotEmpty()) {
                Text(
                    text = if (state.hasMatches) {
                        "${state.currentMatchIndex + 1}/${state.totalMatches}"
                    } else {
                        "0/0"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            // Previous button
            IconButton(
                onClick = onPrevious,
                enabled = state.hasMatches
            ) {
                Icon(
                    Icons.Default.ArrowUpward,
                    contentDescription = "Previous match"
                )
            }
            
            // Next button
            IconButton(
                onClick = onNext,
                enabled = state.hasMatches
            ) {
                Icon(
                    Icons.Default.ArrowDownward,
                    contentDescription = "Next match"
                )
            }
            
            // Close button
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Close search"
                )
            }
        }
    }
}
