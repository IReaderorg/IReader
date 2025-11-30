package ireader.presentation.ui.home.sources.extension.composables

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
 * Enhanced catalog card with custom modern design
 * - Glassmorphism effect
 * - Smooth micro-interactions
 * - Efficient rendering
 * - Custom animations
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnhancedCatalogCard(
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
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = localizeHelper.localize(Res.string.card_scale)
    )
    
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 3.dp,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = localizeHelper.localize(Res.string.card_elevation)
    )

    val lang = when (catalog) {
        is CatalogBundled -> null
        is CatalogInstalled -> catalog.source?.lang
        is CatalogRemote -> catalog.lang
    }?.let { Language(it) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .then(
                if (onClick != null || onShowDetails != null) {
                    Modifier.combinedClickable(
                        onClick = { 
                            isPressed = true
                            onClick?.invoke()
                        },
                        onLongClick = { onShowDetails?.invoke() }
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevation
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Enhanced Icon with glow
                EnhancedCatalogIcon(
                    catalog = catalog,
                    isPinned = (catalog as? CatalogLocal)?.isPinned == true,
                    modifier = Modifier.size(64.dp)
                )

                // Source Info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = catalog.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        
                        // Compact status indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            if (sourceStatus != null && catalog is CatalogInstalled) {
                                StatusDot(status = sourceStatus)
                            }
                        }
                    }

                    // Language chip with custom design
                    if (lang != null) {
                        LanguageChip(language = lang)
                    }
                }

                // Compact action buttons
                EnhancedCatalogActions(
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
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
private fun EnhancedCatalogIcon(
    catalog: Catalog,
    isPinned: Boolean,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        if (isPinned) {
                            // Glow effect for pinned sources
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFFD700).copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                ),
                                radius = size.minDimension * 0.7f
                            )
                        }
                    }
            ) {
                when (catalog) {
                    is CatalogBundled -> {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = catalog.name.take(2).uppercase(),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 22.sp
                                ),
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
        
        // Pin indicator badge
        if (isPinned) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
                    .size(20.dp),
                shape = CircleShape,
                color = Color(0xFFFFD700),
                shadowElevation = 4.dp
            ) {
                Icon(
                    imageVector = Icons.Filled.PushPin,
                    contentDescription = localizeHelper.localize(Res.string.pinned_sources),
                    tint = Color.White,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun LanguageChip(language: Language) {
    val emoji = language.toEmoji()
    
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        modifier = Modifier.wrapContentWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (emoji != null) {
                Text(
                    text = emoji,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
                )
            }
            Text(
                text = language.code.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun StatusDot(status: SourceStatus) {
    val color = when (status) {
        is SourceStatus.Online -> Color(0xFF4CAF50)
        is SourceStatus.Offline -> Color(0xFFF44336)
        is SourceStatus.LoginRequired -> Color(0xFFFF9800)
        is SourceStatus.Error -> Color(0xFFFF5722)
    }
    
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
            .drawBehind {
                // Pulse effect
                drawCircle(
                    color = color.copy(alpha = 0.3f),
                    radius = size.minDimension * 0.8f
                )
            }
    )
}

@Composable
private fun EnhancedCatalogActions(
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
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.End
    ) {
        // Primary action
        if (sourceStatus is SourceStatus.LoginRequired && onLogin != null) {
            CompactActionButton(
                icon = Icons.Default.Login,
                text = localizeHelper.localize(Res.string.login),
                onClick = onLogin,
                color = MaterialTheme.colorScheme.tertiary
            )
        } else if (installStep != null && !installStep.isFinished()) {
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(
                    onClick = { onCancelInstaller?.invoke() },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = localizeHelper.localize(Res.string.cancel),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else if (onInstall != null) {
            CompactActionButton(
                icon = if (catalog is CatalogLocal) Icons.Default.Update else Icons.Default.Download,
                text = if (catalog is CatalogLocal) "Update" else "Install",
                onClick = onInstall,
                color = MaterialTheme.colorScheme.primary
            )
        } else if (onUninstall != null && catalog is CatalogLocal) {
            CompactActionButton(
                icon = Icons.Default.Delete,
                text = localizeHelper.localize(Res.string.remove),
                onClick = onUninstall,
                color = MaterialTheme.colorScheme.error
            )
        }
        
        // Secondary actions
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (onPinToggle != null && catalog is CatalogLocal && onUninstall == null) {
                SmallIconButton(
                    icon = if (catalog.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    onClick = onPinToggle,
                    tint = if (catalog.isPinned) Color(0xFFFFD700) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            if (onMigrate != null && catalog is CatalogInstalled) {
                SmallIconButton(
                    icon = Icons.Default.SwapHoriz,
                    onClick = onMigrate,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CompactActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
    color: Color
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = Modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = color.copy(alpha = 0.15f),
            contentColor = color
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp
            )
        )
    }
}

@Composable
private fun SmallIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    tint: Color
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
    }
}
