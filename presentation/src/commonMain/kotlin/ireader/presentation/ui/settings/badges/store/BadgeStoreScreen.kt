package ireader.presentation.ui.settings.badges.store

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.BadgeRarity
import ireader.domain.models.remote.PaymentProof
import ireader.domain.models.remote.PaymentProofStatus
import ireader.i18n.resources.Res
import ireader.i18n.resources.back
import ireader.i18n.resources.badge_store
import ireader.i18n.resources.check_back_later_for_new_badges
import ireader.i18n.resources.click_the_donation_button_below
import ireader.i18n.resources.complete_your_donation
import ireader.i18n.resources.copy_your_transaction_id
import ireader.i18n.resources.enter_your_transaction_id
import ireader.i18n.resources.exclusive
import ireader.i18n.resources.exclusive_designs
import ireader.i18n.resources.featured_badges
import ireader.i18n.resources.how_to_purchase
import ireader.i18n.resources.loading_badges
import ireader.i18n.resources.no_badges_available
import ireader.i18n.resources.purchase_exclusive_badges_to_support
import ireader.i18n.resources.purchase_exclusive_badges_to_support_1
import ireader.i18n.resources.submit_for_verification
import ireader.i18n.resources.support_collect
import ireader.i18n.resources.support_development
import ireader.i18n.resources.transaction_id
import ireader.i18n.resources.try_again
import ireader.i18n.resources.verified
import ireader.i18n.resources.verified_supporter
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.AsyncImage

private const val DONATION_URL = "https://reymit.ir/kazemcodes"

// Rarity color schemes
private val commonColors = listOf(Color(0xFF607D8B), Color(0xFF78909C))
private val rareColors = listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
private val epicColors = listOf(Color(0xFF7B1FA2), Color(0xFFAB47BC))
private val legendaryColors = listOf(Color(0xFFFF8F00), Color(0xFFFFD54F))

// Responsive breakpoints
private val COMPACT_WIDTH = 480.dp  // Mobile phones
private val MEDIUM_WIDTH = 720.dp   // Tablets portrait
private val EXPANDED_WIDTH = 1024.dp // Tablets landscape / small desktop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeStoreScreen(
    viewModel: BadgeStoreViewModel,
    onNavigateBack: () -> Unit,
    onOpenDonationLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(state.submitSuccess, state.submitError) {
        when {
            state.submitSuccess -> {
                snackbarHostState.showSnackbar(
                    message = "✓ Payment proof submitted! Awaiting verification.",
                    duration = SnackbarDuration.Long
                )
                viewModel.clearSubmitStatus()
            }
            state.submitError != null -> {
                snackbarHostState.showSnackbar(
                    message = "✗ ${state.submitError}",
                    duration = SnackbarDuration.Long
                )
                viewModel.clearSubmitStatus()
            }
        }
    }

    val categories = remember(state.badges) {
        state.badges.map { it.category }.distinct()
    }

    val filteredBadges = remember(state.badges, selectedCategory) {
        if (selectedCategory == null) state.badges
        else state.badges.filter { it.category == selectedCategory }
    }

    val featuredBadges = remember(state.badges) {
        state.badges.filter { 
            it.badgeRarity == BadgeRarity.LEGENDARY || it.badgeRarity == BadgeRarity.EPIC 
        }.take(5)
    }

    IScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        topBar = { scrollBehavior ->
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocalOffer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(localizeHelper.localize(Res.string.badge_store))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val screenWidth = maxWidth
            val isCompact = screenWidth < COMPACT_WIDTH
            val isMedium = screenWidth >= COMPACT_WIDTH && screenWidth < MEDIUM_WIDTH
            val isExpanded = screenWidth >= MEDIUM_WIDTH && screenWidth < EXPANDED_WIDTH
            val isLarge = screenWidth >= EXPANDED_WIDTH

            // Responsive values - optimized for mobile
            val horizontalPadding = when {
                isLarge -> 48.dp
                isExpanded -> 32.dp
                isMedium -> 16.dp
                else -> 12.dp  // Smaller padding on mobile
            }
            
            val gridMinSize = when {
                isLarge -> 220.dp
                isExpanded -> 200.dp
                isMedium -> 160.dp
                else -> 140.dp  // Smaller cards on mobile to fit 2 per row
            }
            
            val contentMaxWidth = when {
                isLarge -> 1400.dp
                isExpanded -> 1200.dp
                else -> screenWidth
            }
            
            val verticalPadding = if (isCompact) 16.dp else 24.dp

            when {
                state.isLoading -> LoadingState()
                state.error != null -> ErrorState(
                    error = state.error ?: "An error occurred",
                    onRetry = { viewModel.loadBadges() }
                )
                state.badges.isEmpty() -> EmptyState()
                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = gridMinSize),
                            modifier = Modifier.widthIn(max = contentMaxWidth),
                            contentPadding = PaddingValues(
                                horizontal = horizontalPadding,
                                vertical = verticalPadding
                            ),
                            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 20.dp),
                            verticalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 20.dp)
                        ) {
                            // Hero Section
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                HeroSection(isWideScreen = !isCompact)
                            }

                            // Featured Badges
                            if (featuredBadges.isNotEmpty()) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    FeaturedBadgesSection(
                                        badges = featuredBadges,
                                        onBadgeClick = { viewModel.onBadgeClick(it) },
                                        cardWidth = when {
                                            isLarge -> 280.dp
                                            isExpanded -> 240.dp
                                            isMedium -> 200.dp
                                            else -> 160.dp  // Smaller featured cards on mobile
                                        },
                                        isCompact = isCompact
                                    )
                                }
                            }

                            // Category Filter
                            if (categories.size > 1) {
                                item(span = { GridItemSpan(maxLineSpan) }) {
                                    CategoryFilter(
                                        categories = categories,
                                        selectedCategory = selectedCategory,
                                        onCategorySelected = { selectedCategory = it }
                                    )
                                }
                            }

                            // Section Header
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedCategory ?: "All Badges",
                                        style = if (isCompact) 
                                            MaterialTheme.typography.titleLarge 
                                        else 
                                            MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = "${filteredBadges.size}",
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp, 
                                                vertical = 6.dp
                                            ),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            }

                            // Badge Grid
                            items(
                                items = filteredBadges,
                                key = { badge -> "grid_${badge.id}" }
                            ) { badge ->
                                ModernBadgeCard(
                                    badge = badge,
                                    onClick = { viewModel.onBadgeClick(badge) },
                                    isLargeScreen = isExpanded || isLarge
                                )
                            }
                            
                            // Bottom spacing
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    }
                }
            }
        }

        // Purchase Dialog
        if (state.showPurchaseDialog && state.selectedBadge != null) {
            ModernPurchaseDialog(
                badge = state.selectedBadge!!,
                onDismiss = { viewModel.onDismissPurchaseDialog() },
                onSubmitProof = { proof -> viewModel.onSubmitPaymentProof(proof) },
                isSubmitting = state.isSubmitting,
                onOpenDonationLink = { onOpenDonationLink(DONATION_URL) }
            )
        }
    }
}

@Composable
private fun HeroSection(isWideScreen: Boolean = false) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (isWideScreen) 28.dp else 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(if (isWideScreen) 32.dp else 24.dp)
        ) {
            if (isWideScreen) {
                // Wide screen: horizontal layout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = localizeHelper.localize(Res.string.support_collect),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.purchase_exclusive_badges_to_support) +
                                    "Each badge is unique and shows your support for the project.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Spacer(modifier = Modifier.width(32.dp))
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureChip(icon = Icons.Outlined.Verified, text = localizeHelper.localize(Res.string.verified_supporter))
                        FeatureChip(icon = Icons.Outlined.Diamond, text = localizeHelper.localize(Res.string.exclusive_designs))
                        FeatureChip(icon = Icons.Outlined.Wallet, text = localizeHelper.localize(Res.string.support_development))
                    }
                }
            } else {
                // Compact: vertical layout
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.support_collect),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.purchase_exclusive_badges_to_support_1),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeatureChip(icon = Icons.Outlined.Verified, text = localizeHelper.localize(Res.string.verified))
                        FeatureChip(icon = Icons.Outlined.Diamond, text = localizeHelper.localize(Res.string.exclusive))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = Color.White
            )
        }
    }
}

@Composable
private fun FeaturedBadgesSection(
    badges: List<Badge>,
    onBadgeClick: (Badge) -> Unit,
    cardWidth: Dp = 200.dp,
    isCompact: Boolean = false
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(if (isCompact) 22.dp else 28.dp)
            )
            Spacer(modifier = Modifier.width(if (isCompact) 8.dp else 10.dp))
            Text(
                text = localizeHelper.localize(Res.string.featured_badges),
                style = if (isCompact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(if (isCompact) 12.dp else 16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(if (isCompact) 12.dp else 20.dp),
            contentPadding = PaddingValues(end = if (isCompact) 8.dp else 16.dp)
        ) {
            items(
                items = badges,
                key = { badge -> "featured_${badge.id}" }
            ) { badge ->
                FeaturedBadgeCard(
                    badge = badge,
                    onClick = { onBadgeClick(badge) },
                    cardWidth = cardWidth,
                    isCompact = isCompact
                )
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun FeaturedBadgeCard(
    badge: Badge,
    onClick: () -> Unit,
    cardWidth: Dp = 200.dp,
    isCompact: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (isHovered) 1.05f else 1f)
    val gradientColors = getRarityGradient(badge.badgeRarity)
    
    val imageSize = if (isCompact) 64.dp else 100.dp
    val cardPadding = if (isCompact) 12.dp else 20.dp
    val cornerRadius = if (isCompact) 16.dp else 24.dp

    Card(
        modifier = Modifier
            .width(cardWidth)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHovered) 16.dp else 8.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = Brush.verticalGradient(gradientColors))
        ) {
            Column(
                modifier = Modifier.padding(cardPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Badge Image
                Box(
                    modifier = Modifier
                        .size(imageSize)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(if (isCompact) 2.dp else 3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (badge.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = badge.imageUrl,
                            contentDescription = badge.name,
                            modifier = Modifier.fillMaxSize().clip(CircleShape),
                            contentScale = ContentScale.Crop,
                            error = { BadgePlaceholder(badge.name) }
                        )
                    } else {
                        BadgePlaceholder(badge.name)
                    }
                }

                Spacer(modifier = Modifier.height(if (isCompact) 10.dp else 16.dp))

                Text(
                    text = badge.name,
                    style = if (isCompact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(if (isCompact) 4.dp else 6.dp))

                RarityBadge(rarity = badge.badgeRarity, compact = isCompact)

                Spacer(modifier = Modifier.height(if (isCompact) 8.dp else 12.dp))

                badge.price?.let { price ->
                    Surface(
                        shape = RoundedCornerShape(if (isCompact) 12.dp else 16.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (isCompact) 12.dp else 20.dp,
                                vertical = if (isCompact) 6.dp else 10.dp
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = gradientColors.first(),
                                modifier = Modifier.size(if (isCompact) 14.dp else 18.dp)
                            )
                            Spacer(modifier = Modifier.width(if (isCompact) 4.dp else 8.dp))
                            Text(
                                text = "${ireader.presentation.ui.core.utils.toDecimalString(price, 2)} USD",
                                style = if (isCompact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = gradientColors.first()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryFilter(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item(key = "category_all") {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onCategorySelected(null) },
                label = { 
                    Text(
                        "All", 
                        style = MaterialTheme.typography.labelLarge
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
        items(
            items = categories,
            key = { category -> "category_$category" }
        ) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onCategorySelected(category) },
                label = { 
                    Text(
                        category, 
                        style = MaterialTheme.typography.labelLarge
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun ModernBadgeCard(
    badge: Badge,
    onClick: () -> Unit,
    isLargeScreen: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    val elevation by animateDpAsState(if (isHovered) 16.dp else 6.dp)
    val scale by animateFloatAsState(if (isHovered) 1.04f else 1f)
    val gradientColors = getRarityGradient(badge.badgeRarity)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(if (isLargeScreen) 0.8f else 0.85f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(if (isLargeScreen) 24.dp else 20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Gradient accent at top
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isLargeScreen) 8.dp else 6.dp)
                    .background(brush = Brush.horizontalGradient(gradientColors))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = if (isLargeScreen) 8.dp else 6.dp)
                    .padding(if (isLargeScreen) 16.dp else 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Badge Image Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(if (isLargeScreen) 20.dp else 16.dp))
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    gradientColors.first().copy(alpha = 0.15f),
                                    MaterialTheme.colorScheme.surfaceVariant
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (badge.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = badge.imageUrl,
                            contentDescription = badge.name,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (isLargeScreen) 12.dp else 8.dp)
                                .clip(RoundedCornerShape(if (isLargeScreen) 16.dp else 12.dp)),
                            contentScale = ContentScale.Fit,
                            error = { BadgePlaceholder(badge.name, gradientColors.first()) }
                        )
                    } else {
                        BadgePlaceholder(badge.name, gradientColors.first())
                    }

                    // Rarity indicator
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                    ) {
                        RarityBadge(rarity = badge.badgeRarity, compact = true)
                    }
                }

                Spacer(modifier = Modifier.height(if (isLargeScreen) 12.dp else 8.dp))

                // Badge Name
                Text(
                    text = badge.name,
                    style = if (isLargeScreen) 
                        MaterialTheme.typography.titleMedium 
                    else 
                        MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(if (isLargeScreen) 8.dp else 4.dp))

                // Price Button
                badge.price?.let { price ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(if (isLargeScreen) 14.dp else 12.dp),
                        color = gradientColors.first()
                    ) {
                        Row(
                            modifier = Modifier.padding(
                                horizontal = if (isLargeScreen) 16.dp else 12.dp,
                                vertical = if (isLargeScreen) 10.dp else 8.dp
                            ),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(if (isLargeScreen) 18.dp else 14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = ireader.presentation.ui.core.utils.toDecimalString(price, 2),
                                style = if (isLargeScreen) 
                                    MaterialTheme.typography.titleSmall 
                                else 
                                    MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgePlaceholder(
    name: String,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.take(2).uppercase(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun RarityBadge(
    rarity: BadgeRarity,
    compact: Boolean = false
) {
    val (bgColor, textColor) = when (rarity) {
        BadgeRarity.COMMON -> Color(0xFF607D8B) to Color.White
        BadgeRarity.RARE -> Color(0xFF1976D2) to Color.White
        BadgeRarity.EPIC -> Color(0xFF7B1FA2) to Color.White
        BadgeRarity.LEGENDARY -> Color(0xFFFF8F00) to Color.White
    }

    Surface(
        shape = RoundedCornerShape(if (compact) 8.dp else 14.dp),
        color = bgColor
    ) {
        Text(
            text = if (compact) rarity.name.take(1) else rarity.name,
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 4.dp else 6.dp
            ),
            style = if (compact) 
                MaterialTheme.typography.labelSmall 
            else 
                MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

private fun getRarityGradient(rarity: BadgeRarity): List<Color> = when (rarity) {
    BadgeRarity.COMMON -> commonColors
    BadgeRarity.RARE -> rareColors
    BadgeRarity.EPIC -> epicColors
    BadgeRarity.LEGENDARY -> legendaryColors
}

@Composable
private fun LoadingState() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(
                modifier = Modifier.size(56.dp),
                strokeWidth = 5.dp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = localizeHelper.localize(Res.string.loading_badges),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
    val localizeHelper = LocalLocalizeHelper.current
    val userError = ireader.presentation.ui.component.components.UserError.fromMessage(error)
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(max = 400.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Use appropriate icon based on error type
                Icon(
                    imageVector = userError.icon,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = userError.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = userError.message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(localizeHelper?.localize(Res.string.try_again) ?: "Try Again", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "🏪", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = localizeHelper.localize(Res.string.no_badges_available),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = localizeHelper.localize(Res.string.check_back_later_for_new_badges),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun ModernPurchaseDialog(
    badge: Badge,
    onDismiss: () -> Unit,
    onSubmitProof: (PaymentProof) -> Unit,
    isSubmitting: Boolean,
    onOpenDonationLink: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    var transactionId by remember { mutableStateOf("") }
    val gradientColors = getRarityGradient(badge.badgeRarity)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth(0.95f)  // Use percentage for mobile compatibility
            .widthIn(max = 560.dp)  // Cap max width for desktop
            .onKeyEvent { keyEvent ->
                if (keyEvent.key == Key.Escape && keyEvent.type == KeyEventType.KeyDown) {
                    onDismiss()
                    true
                } else false
            }
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            BoxWithConstraints {
                val currentMaxWidth = maxWidth // Capture maxWidth in a local variable
                val isCompactDialog = currentMaxWidth < 400.dp
                val isVeryCompactDialog = currentMaxWidth < 320.dp // For vertical button layout
                val headerPadding = if (isCompactDialog) 20.dp else 32.dp
                val contentPadding = if (isCompactDialog) 16.dp else 28.dp
                val badgeImageSize = if (isCompactDialog) 80.dp else 120.dp
                val buttonHeight = if (isCompactDialog) 44.dp else 52.dp
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())  // Make dialog scrollable on small screens
                ) {
                    // Header with gradient
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(brush = Brush.horizontalGradient(gradientColors))
                            .padding(headerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Badge Preview
                            Box(
                                modifier = Modifier
                                    .size(badgeImageSize)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .border(if (isCompactDialog) 2.dp else 4.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (badge.imageUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = badge.imageUrl,
                                        contentDescription = badge.name,
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = ContentScale.Crop,
                                        error = {
                                            Text(
                                                text = badge.name.take(2).uppercase(),
                                                style = if (isCompactDialog) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    )
                                } else {
                                    Text(
                                        text = badge.name.take(2).uppercase(),
                                        style = if (isCompactDialog) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(if (isCompactDialog) 12.dp else 16.dp))

                            Text(
                                text = badge.name,
                                style = if (isCompactDialog) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(if (isCompactDialog) 6.dp else 8.dp))

                            RarityBadge(rarity = badge.badgeRarity, compact = isCompactDialog)

                            badge.price?.let { price ->
                                Spacer(modifier = Modifier.height(if (isCompactDialog) 12.dp else 16.dp))
                                Surface(
                                    shape = RoundedCornerShape(if (isCompactDialog) 14.dp else 20.dp),
                                    color = Color.White
                                ) {
                                    Text(
                                        text = "${ireader.presentation.ui.core.utils.toDecimalString(price, 2)} USD",
                                        modifier = Modifier.padding(
                                            horizontal = if (isCompactDialog) 16.dp else 24.dp,
                                            vertical = if (isCompactDialog) 8.dp else 12.dp
                                        ),
                                        style = if (isCompactDialog) MaterialTheme.typography.titleMedium else MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = gradientColors.first()
                                    )
                                }
                            }
                        }
                    }

                    // Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(contentPadding)
                    ) {
                        Text(
                            text = badge.description,
                            style = if (isCompactDialog) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(if (isCompactDialog) 16.dp else 24.dp))

                        // Purchase Steps
                        Card(
                            shape = RoundedCornerShape(if (isCompactDialog) 14.dp else 20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(if (isCompactDialog) 14.dp else 20.dp)) {
                                Text(
                                    text = localizeHelper.localize(Res.string.how_to_purchase),
                                    style = if (isCompactDialog) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(if (isCompactDialog) 10.dp else 16.dp))
                                PurchaseStep(number = "1", text = localizeHelper.localize(Res.string.click_the_donation_button_below), isCompact = isCompactDialog)
                                PurchaseStep(number = "2", text = localizeHelper.localize(Res.string.complete_your_donation), isCompact = isCompactDialog)
                                PurchaseStep(number = "3", text = localizeHelper.localize(Res.string.copy_your_transaction_id), isCompact = isCompactDialog)
                                PurchaseStep(number = "4", text = localizeHelper.localize(Res.string.submit_for_verification), isCompact = isCompactDialog)
                            }
                        }

                        Spacer(modifier = Modifier.height(if (isCompactDialog) 14.dp else 20.dp))

                        // Donation Button
                        Button(
                            onClick = onOpenDonationLink,
                            modifier = Modifier.fillMaxWidth().height(buttonHeight),
                            shape = RoundedCornerShape(if (isCompactDialog) 10.dp else 14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Wallet,
                                contentDescription = null,
                                modifier = Modifier.size(if (isCompactDialog) 16.dp else 20.dp)
                            )
                            Spacer(modifier = Modifier.width(if (isCompactDialog) 6.dp else 10.dp))
                            Text(
                                "Open Donation Page",
                                style = if (isCompactDialog) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleSmall
                            )
                        }

                        Text(
                            text = DONATION_URL,
                            modifier = Modifier.fillMaxWidth().padding(top = if (isCompactDialog) 4.dp else 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(if (isCompactDialog) 14.dp else 20.dp))

                        // Transaction ID Input
                        OutlinedTextField(
                            value = transactionId,
                            onValueChange = { transactionId = it },
                            label = { Text(localizeHelper.localize(Res.string.transaction_id)) },
                            placeholder = { Text(localizeHelper.localize(Res.string.enter_your_transaction_id)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(if (isCompactDialog) 10.dp else 14.dp),
                            textStyle = if (isCompactDialog) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
                        )

                        Spacer(modifier = Modifier.height(if (isCompactDialog) 16.dp else 24.dp))

                        // Action Buttons - Stack vertically on very small screens
                        if (isCompactDialog && isVeryCompactDialog) {
                            // Vertical button layout for very small screens
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val proof = PaymentProof(
                                            id = "",
                                            userId = "",
                                            badgeId = badge.id,
                                            transactionId = transactionId,
                                            paymentMethod = "Reymit Donation",
                                            proofImageUrl = null,
                                            status = PaymentProofStatus.PENDING,
                                            submittedAt = ""
                                        )
                                        onSubmitProof(proof)
                                    },
                                    modifier = Modifier.fillMaxWidth().height(buttonHeight),
                                    enabled = !isSubmitting && transactionId.isNotBlank(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = gradientColors.first()
                                    )
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }
                                    Text(
                                        if (isSubmitting) "Submitting..." else "Submit",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                }
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.fillMaxWidth().height(buttonHeight),
                                    enabled = !isSubmitting,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Cancel", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        } else {
                            // Horizontal button layout
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(if (isCompactDialog) 10.dp else 16.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f).height(buttonHeight),
                                    enabled = !isSubmitting,
                                    shape = RoundedCornerShape(if (isCompactDialog) 10.dp else 14.dp)
                                ) {
                                    Text("Cancel", style = if (isCompactDialog) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleSmall)
                                }

                                Button(
                                    onClick = {
                                        val proof = PaymentProof(
                                            id = "",
                                            userId = "",
                                            badgeId = badge.id,
                                            transactionId = transactionId,
                                            paymentMethod = "Reymit Donation",
                                            proofImageUrl = null,
                                            status = PaymentProofStatus.PENDING,
                                            submittedAt = ""
                                        )
                                        onSubmitProof(proof)
                                    },
                                    modifier = Modifier.weight(1f).height(buttonHeight),
                                    enabled = !isSubmitting && transactionId.isNotBlank(),
                                    shape = RoundedCornerShape(if (isCompactDialog) 10.dp else 14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = gradientColors.first()
                                    )
                                ) {
                                    if (isSubmitting) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(if (isCompactDialog) 16.dp else 20.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(if (isCompactDialog) 6.dp else 10.dp))
                                    }
                                    Text(
                                        if (isSubmitting) "Submitting..." else "Submit",
                                        style = if (isCompactDialog) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseStep(number: String, text: String, isCompact: Boolean = false) {
    Row(
        modifier = Modifier.padding(vertical = if (isCompact) 4.dp else 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(if (isCompact) 22.dp else 28.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = number,
                    style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.width(if (isCompact) 10.dp else 14.dp))
        Text(
            text = text,
            style = if (isCompact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Backward compatibility functions
@Composable
fun BadgeCard(
    badge: Badge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernBadgeCard(badge = badge, onClick = onClick)
}

@Composable
fun RarityChip(
    rarity: BadgeRarity,
    modifier: Modifier = Modifier
) {
    RarityBadge(rarity = rarity)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun BadgePurchaseDialog(
    badge: Badge,
    onDismiss: () -> Unit,
    onSubmitProof: (PaymentProof) -> Unit,
    isSubmitting: Boolean,
    onOpenDonationLink: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModernPurchaseDialog(
        badge = badge,
        onDismiss = onDismiss,
        onSubmitProof = onSubmitProof,
        isSubmitting = isSubmitting,
        onOpenDonationLink = onOpenDonationLink
    )
}
