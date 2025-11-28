package ireader.presentation.ui.settings.badges.store

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.BadgeRarity
import ireader.domain.models.remote.PaymentProof
import ireader.domain.models.remote.PaymentProofStatus
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.ui.AsyncImage

private const val DONATION_URL = "https://reymit.ir/kazemcodes"

// Rarity color schemes
private val commonColors = listOf(Color(0xFF607D8B), Color(0xFF78909C))
private val rareColors = listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
private val epicColors = listOf(Color(0xFF7B1FA2), Color(0xFFAB47BC))
private val legendaryColors = listOf(Color(0xFFFF8F00), Color(0xFFFFD54F))

// Desktop breakpoints
private val COMPACT_WIDTH = 600.dp
private val MEDIUM_WIDTH = 900.dp
private val EXPANDED_WIDTH = 1200.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeStoreScreen(
    viewModel: BadgeStoreViewModel,
    onNavigateBack: () -> Unit,
    onOpenDonationLink: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                        Text("Badge Store")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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

            // Responsive values
            val horizontalPadding = when {
                isLarge -> 48.dp
                isExpanded -> 32.dp
                isMedium -> 24.dp
                else -> 16.dp
            }
            
            val gridMinSize = when {
                isLarge -> 220.dp
                isExpanded -> 200.dp
                isMedium -> 180.dp
                else -> 160.dp
            }
            
            val contentMaxWidth = when {
                isLarge -> 1400.dp
                isExpanded -> 1200.dp
                else -> screenWidth
            }

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
                                vertical = 24.dp
                            ),
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
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
                                        cardWidth = if (isLarge) 280.dp else if (isExpanded) 240.dp else 200.dp
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
                            items(filteredBadges) { badge ->
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
                                text = "Support & Collect",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Purchase exclusive badges to support development and customize your profile. " +
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
                        FeatureChip(icon = Icons.Outlined.Verified, text = "Verified Supporter")
                        FeatureChip(icon = Icons.Outlined.Diamond, text = "Exclusive Designs")
                        FeatureChip(icon = Icons.Outlined.Wallet, text = "Support Development")
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
                            text = "Support & Collect",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Purchase exclusive badges to support development and customize your profile",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        FeatureChip(icon = Icons.Outlined.Verified, text = "Verified")
                        FeatureChip(icon = Icons.Outlined.Diamond, text = "Exclusive")
                        FeatureChip(icon = Icons.Outlined.Wallet, text = "Support")
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
    cardWidth: Dp = 200.dp
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Featured Badges",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            items(badges) { badge ->
                FeaturedBadgeCard(
                    badge = badge,
                    onClick = { onBadgeClick(badge) },
                    cardWidth = cardWidth
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
    cardWidth: Dp = 200.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val scale by animateFloatAsState(if (isHovered) 1.05f else 1f)
    val gradientColors = getRarityGradient(badge.badgeRarity)

    Card(
        modifier = Modifier
            .width(cardWidth)
            .scale(scale)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
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
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Badge Image
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
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

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = badge.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                RarityBadge(rarity = badge.badgeRarity)

                Spacer(modifier = Modifier.height(12.dp))

                badge.price?.let { price ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = gradientColors.first(),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${"%.2f".format(price)} USD",
                                style = MaterialTheme.typography.titleMedium,
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
        item {
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
        items(categories) { category ->
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
            .scale(scale)
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
                                text = "${"%.2f".format(price)}",
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
                text = "Loading badges...",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(error: String, onRetry: () -> Unit) {
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
                Text(text = "😕", fontSize = 56.sp)
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = error,
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
                    Text("Try Again", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
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
                text = "No badges available",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Check back later for new badges!",
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
    var transactionId by remember { mutableStateOf("") }
    val gradientColors = getRarityGradient(badge.badgeRarity)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .widthIn(min = 400.dp, max = 560.dp)
            .onKeyEvent { keyEvent ->
                if (keyEvent.key == Key.Escape && keyEvent.type == KeyEventType.KeyDown) {
                    onDismiss()
                    true
                } else false
            }
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header with gradient
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = Brush.horizontalGradient(gradientColors))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Badge Preview
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                                .border(4.dp, Color.White.copy(alpha = 0.6f), CircleShape),
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
                                            style = MaterialTheme.typography.displaySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                )
                            } else {
                                Text(
                                    text = badge.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = badge.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        RarityBadge(rarity = badge.badgeRarity)

                        badge.price?.let { price ->
                            Spacer(modifier = Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White
                            ) {
                                Text(
                                    text = "${"%.2f".format(price)} USD",
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                    style = MaterialTheme.typography.headlineSmall,
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
                        .padding(28.dp)
                ) {
                    Text(
                        text = badge.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Purchase Steps
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "How to Purchase",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            PurchaseStep(number = "1", text = "Click the donation button below")
                            PurchaseStep(number = "2", text = "Complete your donation")
                            PurchaseStep(number = "3", text = "Copy your transaction ID")
                            PurchaseStep(number = "4", text = "Submit for verification")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Donation Button
                    Button(
                        onClick = onOpenDonationLink,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Wallet,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Open Donation Page", style = MaterialTheme.typography.titleSmall)
                    }

                    Text(
                        text = DONATION_URL,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Transaction ID Input
                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text("Transaction ID") },
                        placeholder = { Text("Enter your transaction ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(14.dp),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(52.dp),
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Cancel", style = MaterialTheme.typography.titleSmall)
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
                            modifier = Modifier.weight(1f).height(52.dp),
                            enabled = !isSubmitting && transactionId.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = gradientColors.first()
                            )
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Text(
                                if (isSubmitting) "Submitting..." else "Submit",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PurchaseStep(number: String, text: String) {
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = number,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
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
