package ireader.presentation.ui.book.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ireader.core.source.HttpSource
import ireader.core.source.Source
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelInfoFab(
    favorite: Boolean,
    source: Source?,
    onFavorite: () -> Unit,
    onWebView: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // Primary action icon based on favorite status
    val primaryIcon = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder
    val primaryLabel = if (favorite) {
        localize(Res.string.in_library)
    } else {
        localize(Res.string.add_to_library)
    }
    
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier
    ) {
        // Extended FAB with primary action
        ExtendedFloatingActionButton(
            onClick = { showBottomSheet = true },
            icon = { 
                Icon(
                    imageVector = primaryIcon,
                    contentDescription = primaryLabel
                ) 
            },
            text = { Text(primaryLabel) },
            containerColor = if (favorite) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            },
            contentColor = if (favorite) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSecondaryContainer
            }
        )
    }
    
    // Bottom sheet with all actions
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Text(
                    text = "Actions",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                
                Divider()
                
                // Favorite action
                ActionListItem(
                    icon = if (favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    title = if (favorite) {
                        localize(Res.string.in_library)
                    } else {
                        localize(Res.string.add_to_library)
                    },
                    description = if (favorite) {
                        "Remove from your library"
                    } else {
                        "Add to your library to track updates"
                    },
                    onClick = {
                        onFavorite()
                        showBottomSheet = false
                    }
                )
                
                // WebView action (only for HTTP sources)
                if (source is HttpSource) {
                    ActionListItem(
                        icon = Icons.Default.Public,
                        title = localize(Res.string.webView),
                        description = "Open in web browser",
                        onClick = {
                            onWebView()
                            showBottomSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionListItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(description) },
            leadingContent = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = ListItemDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            tonalElevation = 0.dp,
            shadowElevation = 0.dp
        )
    }
}
