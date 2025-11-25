package ireader.presentation.ui.settings.badges.store

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import ireader.domain.models.remote.Badge
import ireader.domain.models.remote.BadgeRarity
import ireader.domain.models.remote.PaymentProof
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.TitleToolbar
import ireader.presentation.ui.core.ui.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeStoreScreen(
    viewModel: BadgeStoreViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show success/error snackbar
    LaunchedEffect(state.submitSuccess, state.submitError) {
        when {
            state.submitSuccess -> {
                snackbarHostState.showSnackbar(
                    message = "✓ Payment proof submitted successfully! Awaiting admin verification.",
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
    
    IScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        topBar = { scrollBehavior ->
            TopAppBar(
                title = { Text("Badge Store") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Navigate back"
                        )
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
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.error != null -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.error ?: "An error occurred",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.loadBadges() }) {
                            Text("Retry")
                        }
                    }
                }
                state.badges.isEmpty() -> {
                    Text(
                        text = "No badges available",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                else -> {
                    // Desktop optimization: Adjust grid size based on screen width
                    val minCardSize = 180.dp
                    val cardSpacing = 20.dp
                    
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = minCardSize),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(cardSpacing),
                        verticalArrangement = Arrangement.spacedBy(cardSpacing)
                    ) {
                        items(state.badges) { badge ->
                            BadgeCard(
                                badge = badge,
                                onClick = { viewModel.onBadgeClick(badge) }
                            )
                        }
                    }
                }
            }
        }
        
        // Show purchase dialog
        if (state.showPurchaseDialog && state.selectedBadge != null) {
            BadgePurchaseDialog(
                badge = state.selectedBadge!!,
                onDismiss = { viewModel.onDismissPurchaseDialog() },
                onSubmitProof = { proof -> viewModel.onSubmitPaymentProof(proof) },
                isSubmitting = state.isSubmitting
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun BadgeCard(
    badge: Badge,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Desktop hover effects
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    
    val elevation by animateDpAsState(
        targetValue = if (isHovered) 6.dp else 2.dp
    )
    
    val scale by animateFloatAsState(
        targetValue = if (isHovered) 1.03f else 1.0f
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .scale(scale)
            .hoverable(interactionSource)
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Badge Image with fallback
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (badge.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = badge.imageUrl,
                        contentDescription = badge.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = {
                            // Fallback when image fails to load
                            Surface(
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = badge.name.take(2).uppercase(),
                                        style = MaterialTheme.typography.displayLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    )
                } else {
                    // Fallback when no image URL
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = badge.name.take(2).uppercase(),
                                style = MaterialTheme.typography.displayLarge,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Badge Name
            Text(
                text = badge.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Rarity Chip
            RarityChip(rarity = badge.badgeRarity)
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Price
            badge.price?.let { price ->
                Text(
                    text = "$${"%.2f".format(price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun RarityChip(
    rarity: BadgeRarity,
    modifier: Modifier = Modifier
) {
    val (color, textColor) = when (rarity) {
        BadgeRarity.COMMON -> Color(0xFF9E9E9E) to Color.White
        BadgeRarity.RARE -> Color(0xFF2196F3) to Color.White
        BadgeRarity.EPIC -> Color(0xFF9C27B0) to Color.White
        BadgeRarity.LEGENDARY -> Color(0xFFFFD700) to Color.Black
    }
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color
    ) {
        Text(
            text = rarity.name,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun BadgePurchaseDialog(
    badge: Badge,
    onDismiss: () -> Unit,
    onSubmitProof: (PaymentProof) -> Unit,
    isSubmitting: Boolean,
    modifier: Modifier = Modifier
) {
    var transactionId by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Cryptocurrency") }
    var expanded by remember { mutableStateOf(false) }
    val paymentMethods = listOf("Cryptocurrency", "Direct Transfer", "Other")
    
    // Desktop optimization: Max width for dialog
    val dialogMaxWidth = 600.dp
    
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier
            .widthIn(max = dialogMaxWidth)
            .onKeyEvent { keyEvent ->
                // Desktop keyboard navigation: Escape to close
                if (keyEvent.key == Key.Escape && keyEvent.type == KeyEventType.KeyDown) {
                    onDismiss()
                    true
                } else {
                    false
                }
            },
        title = {
            Text(
                text = "Purchase ${badge.name}",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Badge Image Preview with fallback
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (badge.imageUrl.isNotEmpty()) {
                        AsyncImage(
                            model = badge.imageUrl,
                            contentDescription = badge.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = {
                                // Fallback when image fails to load
                                Surface(
                                    modifier = Modifier.fillMaxSize(),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = badge.name.take(2).uppercase(),
                                            style = MaterialTheme.typography.displayLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        )
                    } else {
                        // Fallback when no image URL
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = badge.name.take(2).uppercase(),
                                    style = MaterialTheme.typography.displayLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Badge Description
                Text(
                    text = badge.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Price Display
                badge.price?.let { price ->
                    Text(
                        text = "Price: $${"%.2f".format(price)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Payment Instructions
                Text(
                    text = "Payment Instructions:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "1. Send payment via crypto or direct bank transfer",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "2. Save your transaction ID",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "3. Submit proof below for verification",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Payment Method Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = paymentMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Payment Method") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        paymentMethods.forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    paymentMethod = method
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Transaction ID Field
                OutlinedTextField(
                    value = transactionId,
                    onValueChange = { transactionId = it },
                    label = { Text("Transaction ID *") },
                    placeholder = { Text("Enter your transaction ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isSubmitting
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Optional: Proof Image Upload (placeholder for future implementation)
                Text(
                    text = "Optional: Upload proof image (coming soon)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val proof = PaymentProof(
                        id = "",  // Will be generated by backend
                        userId = "",  // Will be set by backend
                        badgeId = badge.id,
                        transactionId = transactionId,
                        paymentMethod = paymentMethod,
                        proofImageUrl = null,
                        status = ireader.domain.models.remote.PaymentProofStatus.PENDING,
                        submittedAt = ""  // Will be set by backend
                    )
                    onSubmitProof(proof)
                },
                enabled = !isSubmitting && transactionId.isNotBlank()
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isSubmitting) "Submitting..." else "Submit Proof")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isSubmitting
            ) {
                Text("Cancel")
            }
        }
    )
}
