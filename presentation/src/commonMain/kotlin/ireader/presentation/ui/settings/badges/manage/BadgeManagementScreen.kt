package ireader.presentation.ui.settings.badges.manage

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.domain.models.remote.Badge
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.badges.BadgeIcon
import ireader.presentation.ui.component.badges.ProfileBadgeDisplay
import ireader.presentation.ui.component.badges.ReviewBadgeDisplay
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeManagementScreen(
    viewModel: BadgeManagementViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToBadgeStore: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show snackbar for save success/error
    LaunchedEffect(state.saveSuccess, state.saveError) {
        when {
            state.saveSuccess -> {
                snackbarHostState.showSnackbar(
                    message = "Badge settings saved successfully",
                    duration = SnackbarDuration.Short
                )
                viewModel.clearSaveStatus()
            }
            state.saveError != null -> {
                snackbarHostState.showSnackbar(
                    message = state.saveError ?: "Failed to save badge settings",
                    duration = SnackbarDuration.Long
                )
                viewModel.clearSaveStatus()
            }
        }
    }
    
    IScaffold(
        snackbarHostState = snackbarHostState,
        topBar = { scrollBehavior ->
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.manage_badges)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = localizeHelper.localize(Res.string.navigate_back)
                        )
                    }
                },
                actions = {
                    // Save button - enabled when changes made with desktop hover effect
                    AnimatedVisibility(visible = state.hasChanges) {
                        val saveButtonInteractionSource = remember { MutableInteractionSource() }
                        val isSaveButtonHovered by saveButtonInteractionSource.collectIsHoveredAsState()
                        
                        TextButton(
                            onClick = { viewModel.onSaveChanges() },
                            enabled = !state.isSaving,
                            modifier = Modifier
                                .hoverable(saveButtonInteractionSource)
                                .pointerHoverIcon(PointerIcon.Hand),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = if (isSaveButtonHovered) 
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                else 
                                    MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (state.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(localizeHelper.localize(Res.string.save))
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    // Loading state
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    // Error state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = state.error ?: "An error occurred",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Desktop hover effect for retry button
                        val retryButtonInteractionSource = remember { MutableInteractionSource() }
                        val isRetryButtonHovered by retryButtonInteractionSource.collectIsHoveredAsState()
                        
                        Button(
                            onClick = { viewModel.loadUserBadges() },
                            modifier = Modifier
                                .hoverable(retryButtonInteractionSource)
                                .pointerHoverIcon(PointerIcon.Hand),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = if (isRetryButtonHovered) 4.dp else 2.dp
                            )
                        ) {
                            Text(localizeHelper.localize(Res.string.retry))
                        }
                    }
                }
                state.ownedBadges.isEmpty() -> {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "You don't have any badges yet",
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Visit the Badge Store to purchase your first badge",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Desktop hover effect for button
                        val storeButtonInteractionSource = remember { MutableInteractionSource() }
                        val isStoreButtonHovered by storeButtonInteractionSource.collectIsHoveredAsState()
                        
                        Button(
                            onClick = onNavigateToBadgeStore,
                            modifier = Modifier
                                .hoverable(storeButtonInteractionSource)
                                .pointerHoverIcon(PointerIcon.Hand),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = if (isStoreButtonHovered) 4.dp else 2.dp
                            )
                        ) {
                            Text(localizeHelper.localize(Res.string.go_to_badge_store))
                        }
                    }
                }
                else -> {
                    // Desktop optimization: Side-by-side layout on large screens
                    val isLargeScreen = false // Simplified for now, can be enhanced with platform detection
                    
                    // Content
                    if (isLargeScreen) {
                        // Side-by-side layout for desktop
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            // Left side: Badge selection
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                BadgeSelectionContent(
                                    state = state,
                                    viewModel = viewModel
                                )
                            }
                            
                            // Right side: Preview
                            Column(
                                modifier = Modifier
                                    .weight(0.6f)
                            ) {
                                BadgePreviewSection(
                                    ownedBadges = state.ownedBadges,
                                    primaryBadgeId = state.primaryBadgeId,
                                    featuredBadgeIds = state.featuredBadgeIds
                                )
                            }
                        }
                    } else {
                        // Vertical layout for mobile/tablet
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            BadgeSelectionContent(
                                state = state,
                                viewModel = viewModel
                            )
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            BadgePreviewSection(
                                ownedBadges = state.ownedBadges,
                                primaryBadgeId = state.primaryBadgeId,
                                featuredBadgeIds = state.featuredBadgeIds
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeSelectionContent(
    state: BadgeManagementState,
    viewModel: BadgeManagementViewModel
) {
    Column {
        // Section 1: Primary Badge (for Reviews)
        Text(
            text = "Primary Badge (for Reviews)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "This badge will appear next to your username on all your reviews",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        BadgeSelector(
            badges = state.ownedBadges,
            selectedBadgeId = state.primaryBadgeId,
            onBadgeSelect = { viewModel.onSelectPrimaryBadge(it) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Section 2: Featured Badges (for Profile)
        Text(
            text = "Featured Badges (for Profile)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Select up to 3 badges to display prominently on your profile",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        BadgeMultiSelector(
            badges = state.ownedBadges,
            selectedBadgeIds = state.featuredBadgeIds,
            maxSelection = 3,
            onBadgesSelect = { viewModel.onToggleFeaturedBadge(it) }
        )
    }
}

@Composable
private fun BadgeSelector(
    badges: List<Badge>,
    selectedBadgeId: String?,
    onBadgeSelect: (String?) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.heightIn(max = 400.dp)
    ) {
        // "None" option to clear selection
        item {
            SelectableBadgeCard(
                badge = null,
                isSelected = selectedBadgeId == null,
                onClick = { onBadgeSelect(null) }
            )
        }
        
        // Badge options
        items(badges) { badge ->
            SelectableBadgeCard(
                badge = badge,
                isSelected = badge.id == selectedBadgeId,
                onClick = { onBadgeSelect(badge.id) }
            )
        }
    }
}

@Composable
private fun BadgeMultiSelector(
    badges: List<Badge>,
    selectedBadgeIds: List<String>,
    maxSelection: Int = 3,
    onBadgesSelect: (String) -> Unit
) {
    Column {
        // Selection count
        Text(
            text = "Selected: ${selectedBadgeIds.size}/$maxSelection",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.heightIn(max = 400.dp)
        ) {
            items(badges) { badge ->
                val isSelected = badge.id in selectedBadgeIds
                val isDisabled = !isSelected && selectedBadgeIds.size >= maxSelection
                
                SelectableBadgeCard(
                    badge = badge,
                    isSelected = isSelected,
                    isDisabled = isDisabled,
                    onClick = { onBadgesSelect(badge.id) },
                    showCheckmark = true
                )
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun SelectableBadgeCard(
    badge: Badge?,
    isSelected: Boolean,
    isDisabled: Boolean = false,
    onClick: () -> Unit,
    showCheckmark: Boolean = false
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    // Desktop hover effects
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val borderColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isHovered && !isDisabled -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        else -> Color.Transparent
    }
    
    val backgroundColor = when {
        isDisabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    // Tooltip for desktop
    val tooltipText = badge?.name ?: "No badge"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(
                width = if (isSelected) 3.dp else if (isHovered && !isDisabled) 2.dp else 1.dp,
                color = if (isSelected) borderColor else if (isHovered && !isDisabled) borderColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            )
            .hoverable(interactionSource)
            .pointerHoverIcon(if (!isDisabled) PointerIcon.Hand else PointerIcon.Default)
            .clickable(enabled = !isDisabled, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (badge == null) {
                // "None" option
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = localizeHelper.localize(Res.string.no_badge),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "None",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        BadgeIcon(
                            badge = badge,
                            size = 64.dp,
                            showAnimation = badge.type == ireader.domain.models.remote.BadgeType.NFT_EXCLUSIVE,
                            modifier = Modifier.then(
                                if (isDisabled) Modifier.graphicsLayer(alpha = 0.5f) else Modifier
                            )
                        )
                        
                        // Rarity indicator
                        if (!isDisabled) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(getRarityColor(badge.badgeRarity)),
                                contentAlignment = Alignment.Center
                            ) {
                                // Optional: Add rarity icon or text
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = badge.name,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        color = if (isDisabled) 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        else 
                            MaterialTheme.colorScheme.onSurface
                    )
                    // Show rarity text
                    if (!isDisabled) {
                        Text(
                            text = badge.badgeRarity.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = getRarityColor(badge.badgeRarity),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            // Selection indicator
            if (isSelected || (showCheckmark && isSelected)) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = localizeHelper.localize(Res.string.selected),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgePreviewSection(
    ownedBadges: List<Badge>,
    primaryBadgeId: String?,
    featuredBadgeIds: List<String>
) {
    Text(
        text = "Preview",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(12.dp))
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Profile Preview
            Text(
                text = "How it appears on your profile:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Your Username",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val featuredBadges = ownedBadges.filter { it.id in featuredBadgeIds }
                    if (featuredBadges.isNotEmpty()) {
                        ProfileBadgeDisplay(badges = featuredBadges)
                    } else {
                        Text(
                            text = "No featured badges selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Review Preview
            Text(
                text = "How it appears on your reviews:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    val primaryBadge = ownedBadges.find { it.id == primaryBadgeId }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Your Username",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        if (primaryBadge != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            ReviewBadgeDisplay(badge = primaryBadge)
                        }
                    }
                    
                    if (primaryBadge == null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "No primary badge selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This is a sample review text that shows how your badge will appear next to your username when you write reviews.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Returns the color associated with a badge rarity level.
 */
@Composable
private fun getRarityColor(rarity: ireader.domain.models.remote.BadgeRarity): Color {
    return when (rarity) {
        ireader.domain.models.remote.BadgeRarity.COMMON -> Color(0xFF9E9E9E) // Gray
        ireader.domain.models.remote.BadgeRarity.RARE -> Color(0xFF2196F3) // Blue
        ireader.domain.models.remote.BadgeRarity.EPIC -> Color(0xFF9C27B0) // Purple
        ireader.domain.models.remote.BadgeRarity.LEGENDARY -> Color(0xFFFFD700) // Gold
    }
}
