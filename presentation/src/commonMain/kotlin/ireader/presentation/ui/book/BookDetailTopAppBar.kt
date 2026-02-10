package ireader.presentation.ui.book

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlipToBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import ireader.core.source.Source
import ireader.core.source.model.Command
import ireader.i18n.LocalizeHelper
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.components.IDropdownMenu
import ireader.presentation.ui.component.components.IDropdownMenuItem
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Modern Book Detail TopAppBar with enhanced UI/UX features.
 * 
 * Design principles:
 * - Back button is always visible and accessible (pinned)
 * - Smooth animations for toolbar elements
 * - Gradient background that appears on scroll
 * - Modern circular icon buttons with press feedback
 * - Animated visibility for action buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailTopAppBar(
    modifier: Modifier = Modifier,
    source: Source?,
    onDownload: () -> Unit,
    onRefresh: () -> Unit,
    onPopBackStack: () -> Unit,
    onCommand: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    hasSelection: Boolean,
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onSelectBetween: () -> Unit,
    paddingValues: PaddingValues,
    onInfo: () -> Unit,
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    isArchived: Boolean = false,
    onShareBook: () -> Unit = {},
    onExportEpub: () -> Unit = {},
) {
    // Animate between regular and edit mode with crossfade
    AnimatedVisibility(
        visible = !hasSelection,
        enter = fadeIn(tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200)),
        exit = fadeOut(tween(150)) + scaleOut(targetScale = 0.95f, animationSpec = tween(150))
    ) {
        ModernRegularChapterDetailTopAppBar(
            modifier = modifier,
            onPopBackStack = onPopBackStack,
            scrollBehavior = scrollBehavior,
            onCommand = onCommand,
            onRefresh = onRefresh,
            source = source,
            onDownload = onDownload,
            onInfo = onInfo,
            onArchive = onArchive,
            onUnarchive = onUnarchive,
            isArchived = isArchived,
            onExportEpub = onExportEpub,
            onShareBook = onShareBook
        )
    }
    
    AnimatedVisibility(
        visible = hasSelection,
        enter = fadeIn(tween(200)) + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut(tween(150)) + slideOutVertically(targetOffsetY = { -it / 2 })
    ) {
        EditModeChapterDetailTopAppBar(
            modifier = modifier.padding(paddingValues),
            selectionSize = selectionSize,
            onClickCancelSelection = onClickCancelSelection,
            onClickSelectAll = onClickSelectAll,
            onClickInvertSelection = onClickInvertSelection,
            onSelectBetween = onSelectBetween,
            scrollBehavior = scrollBehavior,
            paddingValues = paddingValues
        )
    }
}

/**
 * Modern implementation with enhanced visual feedback and animations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernRegularChapterDetailTopAppBar(
    modifier: Modifier = Modifier,
    source: Source?,
    onDownload: () -> Unit,
    onRefresh: () -> Unit,
    onPopBackStack: () -> Unit,
    onCommand: () -> Unit,
    onInfo: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    isArchived: Boolean = false,
    onShareBook: () -> Unit = {},
    onExportEpub: () -> Unit = {}
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val (dropDownState, setDropDownState) = remember { mutableStateOf(false) }
    
    // Track scroll state for gradient background animation
    val scrollProgress = scrollBehavior?.state?.collapsedFraction ?: 0f
    val animatedScrollProgress by animateFloatAsState(
        targetValue = scrollProgress,
        animationSpec = tween(150),
        label = "scrollProgress"
    )
    
    // Calculate background alpha based on scroll
    val backgroundAlpha = (animatedScrollProgress * 1.2f).coerceIn(0f, 1f)
    
    Box(modifier = modifier.statusBarsPadding()) {
        // Animated gradient background that appears on scroll
        if (backgroundAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { alpha = backgroundAlpha }
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = 200f
                        )
                    )
                    .padding(bottom = 32.dp)
            )
        }
        
        TopAppBar(
            modifier = Modifier.align(Alignment.TopCenter),
            title = {},
            navigationIcon = {
                // Modern back button with press animation
                PressableIconButton(
                    onClick = onPopBackStack,
                    contentDescription = localizeHelper.localize(Res.string.go_back),
                    icon = Icons.AutoMirrored.Filled.ArrowBack
                )
            },
            actions = {
                // Refresh button with press animation
                PressableIconButton(
                    onClick = onRefresh,
                    contentDescription = localizeHelper.localize(Res.string.refresh),
                    icon = Icons.Default.Autorenew
                )
                
                Spacer(modifier = Modifier.width(4.dp))
                
                // More options menu
                Box {
                    PressableIconButton(
                        onClick = { setDropDownState(true) },
                        contentDescription = localize(Res.string.more_options),
                        icon = Icons.Outlined.MoreVert
                    )
                    
                    EnhancedDropdownMenu(
                        expanded = dropDownState,
                        onDismiss = { setDropDownState(false) },
                        source = source,
                        onShareBook = { onShareBook(); setDropDownState(false) },
                        onExportEpub = { onExportEpub(); setDropDownState(false) },
                        onInfo = { onInfo(); setDropDownState(false) },
                        onCommand = { onCommand(); setDropDownState(false) },
                        onDownload = { onDownload(); setDropDownState(false) },
                        onArchive = { onArchive(); setDropDownState(false) },
                        onUnarchive = { onUnarchive(); setDropDownState(false) },
                        isArchived = isArchived,
                        localizeHelper = localizeHelper
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground
            ),
            scrollBehavior = scrollBehavior
        )
    }
}

/**
 * Pressable icon button with scale animation feedback.
 */
@Composable
private fun PressableIconButton(
    onClick: () -> Unit,
    contentDescription: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = tween(100),
        label = "buttonScale"
    )
    
    Surface (
        modifier = modifier
            .padding(start = 8.dp)
            .size(40.dp)
            .graphicsLayer { 
                scaleX = scale
                scaleY = scale
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        shadowElevation = 2.dp,
        tonalElevation = 1.dp,

    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            interactionSource = interactionSource
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Enhanced dropdown menu with better animations and styling.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedDropdownMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    source: Source?,
    onShareBook: () -> Unit,
    onExportEpub: () -> Unit,
    onInfo: () -> Unit,
    onCommand: () -> Unit,
    onDownload: () -> Unit,
    onArchive: () -> Unit,
    onUnarchive: () -> Unit,
    isArchived: Boolean,
    localizeHelper: LocalizeHelper
) {
    IDropdownMenu(
        modifier = Modifier,
        expanded = expanded,
        onDismissRequest = onDismiss,
    ) {
        IDropdownMenuItem(
            text = { Text(text = localizeHelper.localize(Res.string.share)) },
            onClick = onShareBook,
            leadingIcon = {
                Icon(Icons.Default.Share, contentDescription = null)
            }
        )
        IDropdownMenuItem(
            text = { Text(text = localizeHelper.localize(Res.string.export_as_epub)) },
            onClick = onExportEpub,
            leadingIcon = {
                Icon(Icons.Default.Book, contentDescription = null)
            }
        )
        IDropdownMenuItem(
            text = { Text(text = localizeHelper.localize(Res.string.info)) },
            onClick = onInfo,
            leadingIcon = {
                Icon(Icons.Default.Info, contentDescription = null)
            }
        )
        if (source is ireader.core.source.CatalogSource && source.getCommands().any { it !is Command.Fetchers }) {
            IDropdownMenuItem(
                text = { Text(text = localizeHelper.localize(Res.string.advance_commands)) },
                onClick = onCommand,
                leadingIcon = {
                    Icon(Icons.Default.Tune, contentDescription = null)
                }
            )
        }
        IDropdownMenuItem(
            text = { Text(text = localizeHelper.localize(Res.string.download)) },
            onClick = onDownload,
            leadingIcon = {
                Icon(Icons.Default.Download, contentDescription = null)
            }
        )
        if (isArchived) {
            IDropdownMenuItem(
                text = { Text(text = localizeHelper.localize(Res.string.unarchive)) },
                onClick = onUnarchive
            )
        } else {
            IDropdownMenuItem(
                text = { Text(text = localizeHelper.localize(Res.string.archive)) },
                onClick = onArchive
            )
        }
    }
}

/**
 * Legacy implementation - delegates to modern version.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegularChapterDetailTopAppBar(
    modifier: Modifier = Modifier,
    source: Source?,
    onDownload: () -> Unit,
    onRefresh: () -> Unit,
    onPopBackStack: () -> Unit,
    onCommand: () -> Unit,
    onInfo: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior?,
    onArchive: () -> Unit = {},
    onUnarchive: () -> Unit = {},
    isArchived: Boolean = false,
    onShareBook: () -> Unit = {},
    onExportEpub: () -> Unit = {}
) {
    ModernRegularChapterDetailTopAppBar(
        modifier = modifier,
        source = source,
        onDownload = onDownload,
        onRefresh = onRefresh,
        onPopBackStack = onPopBackStack,
        onCommand = onCommand,
        onInfo = onInfo,
        scrollBehavior = scrollBehavior,
        onArchive = onArchive,
        onUnarchive = onUnarchive,
        isArchived = isArchived,
        onExportEpub = onExportEpub,
        onShareBook = onShareBook
    )
}

/**
 * Edit mode toolbar with enhanced animations and visual feedback.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditModeChapterDetailTopAppBar(
    modifier: Modifier = Modifier,
    selectionSize: Int,
    onClickCancelSelection: () -> Unit,
    onClickSelectAll: () -> Unit,
    onClickInvertSelection: () -> Unit,
    onSelectBetween: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    paddingValues: PaddingValues
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
        tonalElevation = 2.dp
    ) {
        Toolbar(
            title = { 
                BigSizeTextComposable(
                    text = "$selectionSize",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                ) 
            },
            navigationIcon = {
                AppIconButton(
                    imageVector = Icons.Default.Close,
                    contentDescription = localize(Res.string.close),
                    onClick = onClickCancelSelection,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            actions = {
                AppIconButton(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = localize(Res.string.select_all),
                    onClick = onClickSelectAll,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                AppIconButton(
                    imageVector = Icons.Default.FlipToBack,
                    contentDescription = localize(Res.string.select_inverted),
                    onClick = onClickInvertSelection,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                AppIconButton(
                    imageVector = Icons.Default.SyncAlt,
                    contentDescription = localize(Res.string.select_between),
                    onClick = onSelectBetween,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            backgroundColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}
