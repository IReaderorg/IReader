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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Flat, modern source item without cards
 * - No shadows or elevation
 * - Clean flat design
 * - Better readability
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CleanCatalogCard(
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val lang = when (catalog) {
        is CatalogBundled -> null
        is ireader.domain.models.entities.CommunityCatalog -> catalog.source.lang
        is CatalogInstalled -> catalog.source?.lang
        is CatalogRemote -> catalog.lang
    }?.let { Language(it) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if ((onClick != null || onShowDetails != null) && !isLoading) {
                    Modifier.combinedClickable(
                        onClick = { onClick?.invoke() },
                        onLongClick = { onShowDetails?.invoke() }
                    )
                } else Modifier
            )
            .then(
                if (isLoading) Modifier.alpha(0.6f) else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Source Icon
        CleanSourceIcon(
            catalog = catalog,
            modifier = Modifier.size(40.dp)
        )

        // Source Info
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = catalog.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                
                if (isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = localizeHelper.localize(Res.string.loading_1),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                if (sourceStatus != null && catalog is CatalogInstalled) {
                    SourceStatusIndicator(
                        status = sourceStatus,
                        showLabel = false
                    )
                }
            }

            if (lang != null) {
                Text(
                    text = lang.code.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Actions
        CleanActions(
            catalog = catalog,
            installStep = installStep,
            onInstall = onInstall,
            onUninstall = onUninstall,
            onPinToggle = onPinToggle,
            onCancelInstaller = onCancelInstaller,
            sourceStatus = sourceStatus,
            onLogin = onLogin,
        )
    }
}

@Composable
private fun CleanSourceIcon(
    catalog: Catalog,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (catalog) {
            is CatalogBundled -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    Text(
                        text = catalog.name.take(2).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            else -> {
                IImageLoader(
                    model = catalog,
                    contentDescription = catalog.name,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)),
                )
            }
        }
    }
}

@Composable
private fun CleanActions(
    catalog: Catalog,
    installStep: InstallStep?,
    onInstall: (() -> Unit)?,
    onUninstall: (() -> Unit)?,
    onPinToggle: (() -> Unit)?,
    onCancelInstaller: (() -> Unit)?,
    sourceStatus: SourceStatus?,
    onLogin: (() -> Unit)?,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (sourceStatus is SourceStatus.LoginRequired && onLogin != null) {
            TextButton(
                onClick = onLogin,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text("Login", style = MaterialTheme.typography.labelMedium)
            }
        } else if (installStep != null && !installStep.isFinished()) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    strokeWidth = 2.dp
                )
                IconButton(
                    onClick = { onCancelInstaller?.invoke() },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = localizeHelper.localize(Res.string.cancel),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else if (onInstall != null) {
            TextButton(
                onClick = onInstall,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (catalog is CatalogLocal) 
                        localize(Res.string.update) 
                    else 
                        localize(Res.string.install),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        } else if (onUninstall != null && catalog is CatalogLocal) {
            TextButton(
                onClick = onUninstall,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(
                    text = localize(Res.string.uninstall),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        
        if (onPinToggle != null && catalog is CatalogLocal && onUninstall == null) {
            IconButton(
                onClick = onPinToggle,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = if (catalog.isPinned) 
                        Icons.Filled.PushPin 
                    else 
                        Icons.Outlined.PushPin,
                    contentDescription = if (catalog.isPinned) "Unpin" else "Pin",
                    tint = if (catalog.isPinned) 
                        MaterialTheme.colorScheme.primary 
                    else 
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
