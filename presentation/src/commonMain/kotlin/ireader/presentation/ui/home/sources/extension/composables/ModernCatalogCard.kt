package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.core.os.InstallStep
import ireader.domain.models.entities.*
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.component.components.SourceStatusIndicator
import ireader.presentation.ui.home.sources.extension.Language
import java.util.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Modern card-based catalog item with enhanced visual design
 * Features:
 * - Elevated card design
 * - Smooth animations
 * - Better visual hierarchy
 * - Status indicators
 * - Action buttons with icons
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModernCatalogCard(
    modifier: Modifier = Modifier,
    catalog: Catalog,
    installStep: InstallStep? = null,
    onClick: (() -> Unit)? = null,
    onInstall: (() -> Unit)? = null,
    onUninstall: (() -> Unit)? = null,
    onPinToggle: (() -> Unit)? = null,
    onCancelInstaller: (() -> Unit)? = null,
    onShowDetails: (() -> Unit)? = null,
    sourceStatus: SourceStatus? = null,
    onLogin: (() -> Unit)? = null,
    onMigrate: (() -> Unit)? = null,
    isLoading: Boolean = false,
) {
    val lang = when (catalog) {
        is CatalogBundled -> null
        is CatalogInstalled -> catalog.source?.lang
        is CatalogRemote -> catalog.lang
    }?.let { Language(it) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null || onShowDetails != null) {
                    Modifier.combinedClickable(
                        onClick = { onClick?.invoke() },
                        onLongClick = { onShowDetails?.invoke() }
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 6.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Source Icon
            ModernCatalogIcon(
                catalog = catalog,
                modifier = Modifier.size(56.dp)
            )

            // Source Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = catalog.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    // Loading indicator
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    }
                    
                    // Status indicator
                    if (sourceStatus != null && catalog is CatalogInstalled) {
                        SourceStatusIndicator(
                            status = sourceStatus,
                            showLabel = false
                        )
                    }
                }

                // Language badge
                if (lang != null) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.wrapContentWidth()
                    ) {
                        Text(
                            text = lang.code.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Action Buttons
            ModernCatalogActions(
                catalog = catalog,
                installStep = installStep,
                onInstall = onInstall,
                onUninstall = onUninstall,
                onPinToggle = onPinToggle,
                onCancelInstaller = onCancelInstaller,
                sourceStatus = sourceStatus,
                onLogin = onLogin,
                onMigrate = onMigrate,
            )
        }
    }
}

@Composable
private fun ModernCatalogIcon(
    catalog: Catalog,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        when (catalog) {
            is CatalogBundled -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = catalog.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            else -> {
                IImageLoader(
                    model = catalog,
                    contentDescription = catalog.name,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}

@Composable
private fun ModernCatalogActions(
    catalog: Catalog,
    installStep: InstallStep?,
    onInstall: (() -> Unit)?,
    onUninstall: (() -> Unit)?,
    onPinToggle: (() -> Unit)?,
    onCancelInstaller: (() -> Unit)?,
    sourceStatus: SourceStatus?,
    onLogin: (() -> Unit)?,
    onMigrate: (() -> Unit)?,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Login button
        if (sourceStatus is SourceStatus.LoginRequired && onLogin != null) {
            FilledTonalButton(
                onClick = onLogin,
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = localizeHelper.localize(Res.string.login),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Login", style = MaterialTheme.typography.labelMedium)
            }
        }
        
        // Install/Update progress or button
        if (installStep != null && !installStep.isFinished()) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp
                )
                IconButton(
                    onClick = { onCancelInstaller?.invoke() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = localizeHelper.localize(Res.string.cancel),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        } else if (onInstall != null) {
            FilledTonalButton(
                onClick = onInstall,
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = if (catalog is CatalogLocal) 
                        Icons.Default.Update else Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (catalog is CatalogLocal) 
                        localize(Res.string.update) else localize(Res.string.install),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        
        // Pin toggle
        if (onPinToggle != null && catalog is CatalogLocal && onUninstall == null) {
            IconButton(
                onClick = onPinToggle,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (catalog.isPinned) 
                        Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (catalog.isPinned) "Unpin" else "Pin",
                    tint = if (catalog.isPinned) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Uninstall button
        if (onUninstall != null && catalog is CatalogLocal) {
            OutlinedButton(
                onClick = onUninstall,
                modifier = Modifier.height(36.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = localizeHelper.localize(Res.string.uninstall),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = localize(Res.string.uninstall),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        
        // Migrate button
        if (onMigrate != null && catalog is CatalogInstalled) {
            IconButton(
                onClick = onMigrate,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = localizeHelper.localize(Res.string.migrate),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
