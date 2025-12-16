package ireader.presentation.ui.settings.storage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Extension
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher
import ireader.domain.preferences.prefs.UiPreferences
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.grant_storage_permission
import ireader.i18n.resources.permission_feature_backup_desc
import ireader.i18n.resources.permission_feature_backup_title
import ireader.i18n.resources.permission_feature_download_desc
import ireader.i18n.resources.permission_feature_download_title
import ireader.i18n.resources.permission_feature_extensions_desc
import ireader.i18n.resources.permission_feature_extensions_title
import ireader.i18n.resources.skip_for_now
import ireader.i18n.resources.storage_permission_privacy_note
import ireader.i18n.resources.storage_permission_subtitle
import ireader.i18n.resources.storage_permission_title
import kotlinx.coroutines.delay

/**
 * Modern, beautiful storage folder selection screen using FileKit.
 * This is a KMP-compatible implementation that works on Android, iOS, and Desktop.
 * Uses Storage Access Framework (SAF) on Android for secure folder access.
 */
@Composable
fun StorageFolderPickerScreen(
    uiPreferences: UiPreferences,
    onFolderSelected: (String) -> Unit,
    onSkip: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var showContent by remember { mutableStateOf(false) }
    var isSelectingFolder by remember { mutableStateOf(false) }
    
    // Animation trigger
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }
    
    // FileKit directory picker - clean KMP API
    val directoryPicker = rememberDirectoryPickerLauncher(
        title = "Select IReader Storage Folder"
    ) { directory ->
        isSelectingFolder = false
        if (directory != null) {
            // Save the selected folder path - FileKit returns PlatformDirectory
            val folderPath = directory.toString()
            uiPreferences.selectedStorageFolderUri().set(folderPath)
            uiPreferences.hasRequestedStoragePermission().set(true)
            onFolderSelected(folderPath)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Animated icon
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { -50 }
            ) {
                PermissionIcon()
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Title
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, 100)) + slideInVertically(tween(500, 100)) { 30 }
            ) {
                Text(
                    text = localize(Res.string.storage_permission_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Subtitle
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, 200)) + slideInVertically(tween(500, 200)) { 30 }
            ) {
                Text(
                    text = localize(Res.string.storage_permission_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Feature cards
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, 300)) + slideInVertically(tween(500, 300)) { 30 }
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PermissionFeatureCard(
                        icon = Icons.AutoMirrored.Outlined.MenuBook,
                        title = localize(Res.string.permission_feature_download_title),
                        description = localize(Res.string.permission_feature_download_desc),
                        accentColor = MaterialTheme.colorScheme.primary
                    )
                    
                    PermissionFeatureCard(
                        icon = Icons.Outlined.Extension,
                        title = localize(Res.string.permission_feature_extensions_title),
                        description = localize(Res.string.permission_feature_extensions_desc),
                        accentColor = MaterialTheme.colorScheme.secondary
                    )
                    
                    PermissionFeatureCard(
                        icon = Icons.Outlined.Backup,
                        title = localize(Res.string.permission_feature_backup_title),
                        description = localize(Res.string.permission_feature_backup_desc),
                        accentColor = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Privacy note
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, 400))
            ) {
                PrivacyNote()
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Buttons
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500, 500)) + slideInVertically(tween(500, 500)) { 30 }
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Select folder button
                    Button(
                        onClick = {
                            isSelectingFolder = true
                            directoryPicker.launch()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = !isSelectingFolder
                    ) {
                        if (isSelectingFolder) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = localize(Res.string.grant_storage_permission),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    
                    // Skip button
                    TextButton(
                        onClick = {
                            uiPreferences.savedLocalCatalogLocation().set(true)
                            uiPreferences.hasRequestedStoragePermission().set(true)
                            onSkip()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = localize(Res.string.skip_for_now),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun PermissionIcon() {
    var animationPlayed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0.8f,
        animationSpec = tween(500),
        label = "icon_scale"
    )
    
    LaunchedEffect(Unit) {
        animationPlayed = true
    }
    
    Box(
        modifier = Modifier
            .size(120.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun PermissionFeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    accentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PrivacyNote() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = localize(Res.string.storage_permission_privacy_note),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }
}
