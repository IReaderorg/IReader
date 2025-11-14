package ireader.presentation.ui.settings.badges.nft

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.domain.models.remote.Badge
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.badges.BadgeIcon
import ireader.presentation.ui.component.components.TitleToolbar
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NFTBadgeScreen(
    viewModel: NFTBadgeViewModel,
    onNavigateBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Show error snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    IScaffold(
        modifier = modifier,
        snackbarHostState = snackbarHostState,
        topBar = { scrollBehavior ->
            TopAppBar(
                title = { Text("NFT Badge") },
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
                else -> {
                    // Desktop optimization: Max content width and center
                    val maxContentWidth = 800.dp
                    
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(max = maxContentWidth)
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                        // Show different UI based on verification status
                        when {
                            // No wallet saved - show input
                            state.walletAddress == null -> {
                                WalletAddressInput(
                                    currentAddress = null,
                                    onAddressSubmit = { viewModel.onSaveWalletAddress(it) },
                                    isVerifying = state.isVerifying,
                                    error = state.error
                                )
                            }
                            // Wallet saved and NFT owned - show badge display
                            state.verificationStatus?.ownsNFT == true && state.nftBadge != null -> {
                                NFTBadgeOwnershipDisplay(
                                    nftBadge = state.nftBadge!!,
                                    lastVerified = state.lastVerified ?: 0L,
                                    cacheExpiresAt = state.cacheExpiresAt ?: 0L,
                                    onReVerify = { viewModel.onVerifyOwnership() },
                                    isVerifying = state.isVerifying
                                )
                            }
                            // Wallet saved but no NFT - show marketplace link
                            else -> {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Show wallet address
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(16.dp)
                                        ) {
                                            Text(
                                                text = "Wallet Address",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = state.walletAddress ?: "",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    
                                    // Verification status
                                    if (state.verificationStatus != null) {
                                        Card(
                                            modifier = Modifier.fillMaxWidth(),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.errorContainer
                                            )
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Column {
                                                    Text(
                                                        text = "No NFT Found",
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                    Text(
                                                        text = "This wallet does not own an IReader NFT",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onErrorContainer
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    
                                    // Re-verify button
                                    Button(
                                        onClick = { viewModel.onVerifyOwnership() },
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = !state.isVerifying
                                    ) {
                                        if (state.isVerifying) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Text(if (state.isVerifying) "Verifying..." else "Re-verify Ownership")
                                    }
                                    
                                    // Marketplace link
                                    NFTMarketplaceLink(
                                        onOpenMarketplace = {
                                            val url = viewModel.onOpenMarketplace()
                                            onOpenUrl(url)
                                        }
                                    )
                                }
                            }
                        }
                        }
                    }
                }
            }
            
            // Loading overlay when verifying
            if (state.isVerifying && state.walletAddress != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.padding(32.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Verifying NFT ownership...",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WalletAddressInput(
    currentAddress: String?,
    onAddressSubmit: (String) -> Unit,
    isVerifying: Boolean,
    error: String?,
    modifier: Modifier = Modifier
) {
    var address by remember { mutableStateOf(currentAddress ?: "") }
    var validationError by remember { mutableStateOf<String?>(null) }
    
    // Desktop optimization: Larger text field width
    val textFieldMinWidth = 400.dp
    
    Column(
        modifier = modifier.fillMaxWidth()
            .onKeyEvent { keyEvent ->
                // Desktop keyboard navigation: Enter to submit
                if (keyEvent.key == Key.Enter && keyEvent.type == KeyEventType.KeyDown && 
                    !isVerifying && address.isNotBlank()) {
                    val ethereumAddressRegex = Regex("^0x[a-fA-F0-9]{40}$")
                    if (ethereumAddressRegex.matches(address)) {
                        onAddressSubmit(address)
                        true
                    } else {
                        false
                    }
                } else {
                    false
                }
            },
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Explanatory text
        Text(
            text = "Enter your Ethereum wallet address to verify NFT ownership",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Wallet address input
        OutlinedTextField(
            value = address,
            onValueChange = {
                address = it
                validationError = null
            },
            label = { Text("Wallet Address") },
            placeholder = { Text("0x...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null
                )
            },
            isError = validationError != null || error != null,
            supportingText = {
                (validationError ?: error)?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(min = textFieldMinWidth),
            singleLine = true,
            enabled = !isVerifying
        )
        
        // Verify button with hover effect
        val verifyButtonInteractionSource = remember { MutableInteractionSource() }
        val isVerifyButtonHovered by verifyButtonInteractionSource.collectIsHoveredAsState()
        
        Button(
            onClick = {
                // Validate Ethereum address format
                val ethereumAddressRegex = Regex("^0x[a-fA-F0-9]{40}$")
                if (address.isBlank()) {
                    validationError = "Please enter a wallet address"
                } else if (!ethereumAddressRegex.matches(address)) {
                    validationError = "Invalid Ethereum address format"
                } else {
                    onAddressSubmit(address)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .hoverable(verifyButtonInteractionSource)
                .pointerHoverIcon(PointerIcon.Hand),
            enabled = !isVerifying && address.isNotBlank(),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (isVerifyButtonHovered) 4.dp else 2.dp
            )
        ) {
            if (isVerifying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isVerifying) "Verifying..." else "Verify Ownership")
        }
        
        // Info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "NFT Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "Contract Address:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "0xF9Abb7e6947d0427C60Bb5cBF7AeF713B2d37eCc",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "• Verification is automatic and takes a few seconds",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "• Badge granted immediately upon successful verification",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun NFTBadgeOwnershipDisplay(
    nftBadge: Badge,
    lastVerified: Long,
    cacheExpiresAt: Long,
    onReVerify: () -> Unit,
    isVerifying: Boolean,
    modifier: Modifier = Modifier
) {
    val currentTime = System.currentTimeMillis()
    val isExpiringSoon = (cacheExpiresAt - currentTime) < (6 * 60 * 60 * 1000L) // Less than 6 hours
    
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Success message
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "You own an IReader NFT!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        
        // Large NFT badge display
        BadgeIcon(
            badge = nftBadge,
            size = 128.dp,
            showAnimation = true
        )
        
        // Badge name and description
        Text(
            text = nftBadge.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = nftBadge.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        // Verification details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isExpiringSoon) {
                    MaterialTheme.colorScheme.tertiaryContainer
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Verification Details",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Status indicator
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isExpiringSoon) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ) {
                        Text(
                            text = if (isExpiringSoon) "Expiring Soon" else "Valid",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isExpiringSoon) {
                                MaterialTheme.colorScheme.onTertiary
                            } else {
                                MaterialTheme.colorScheme.onPrimary
                            }
                        )
                    }
                }
                
                Divider()
                
                // Last verified
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Last Verified:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatTimestamp(lastVerified),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Cache expires
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Cache Expires:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatTimestamp(cacheExpiresAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isExpiringSoon) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
        
        // Re-verify button
        Button(
            onClick = onReVerify,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isVerifying
        ) {
            if (isVerifying) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(if (isVerifying) "Re-verifying..." else "Re-verify Ownership")
        }
        
        // Info text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = "Verification is cached for 24 hours. Badge remains active for 7 days if re-verification fails.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun NFTMarketplaceLink(
    onOpenMarketplace: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Don't have an IReader NFT?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            
            Text(
                text = "Purchase from our official marketplace to unlock the exclusive NFT badge",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            // Desktop hover effect for marketplace button
            val marketplaceButtonInteractionSource = remember { MutableInteractionSource() }
            val isMarketplaceButtonHovered by marketplaceButtonInteractionSource.collectIsHoveredAsState()
            
            Button(
                onClick = onOpenMarketplace,
                modifier = Modifier
                    .fillMaxWidth()
                    .hoverable(marketplaceButtonInteractionSource)
                    .pointerHoverIcon(PointerIcon.Hand),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = if (isMarketplaceButtonHovered) 4.dp else 2.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShoppingCart,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Visit Marketplace")
            }
        }
    }
}

/**
 * Format timestamp to human-readable date/time string
 */
@OptIn(ExperimentalTime::class)
private fun formatTimestamp(timestamp: Long): String {
    return try {
        val instant = Instant.fromEpochMilliseconds(timestamp)
        val dateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        "${dateTime.date} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
    } catch (e: Exception) {
        "Unknown"
    }
}
