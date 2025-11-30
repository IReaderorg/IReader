package ireader.presentation.ui.settings.donation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ireader.domain.models.donation.CryptoType
import kotlinx.coroutines.launch
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationScreen(
    viewModel: DonationViewModel,
    modifier: Modifier = Modifier,
    onPopBackStack: () -> Unit = {}
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val state by viewModel.state.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var selectedQRCode by remember { mutableStateOf<Pair<String, String>?>(null) }
    var walletSelectionState by remember { mutableStateOf<Triple<CryptoType, String, String>?>(null) }
    
    // Show wallet operation messages
    state.walletOperationMessage?.let { message ->
        scope.launch {
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearWalletMessage()
        }
    }
    
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(localizeHelper.localize(Res.string.support_development)) },
                navigationIcon = {
                    IconButton(onClick = onPopBackStack) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = localizeHelper.localize(Res.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Explanation Section
            item {
                ExplanationCard()
            }
            
            // Cryptocurrency Disclaimer
            item {
                DisclaimerCard()
            }
            
            // Card Payment Section
            item {
                Text(
                    text = localizeHelper.localize(Res.string.card_payment),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            item {
                CardPaymentCard(
                    onDonateClick = {
                        uriHandler.openUri("https://reymit.ir/kazemcodes")
                    }
                )
            }
            
            // Wallet Addresses Section
            item {
                Text(
                    text = localizeHelper.localize(Res.string.cryptocurrency_wallets),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // Bitcoin
            item {
                WalletCard(
                    cryptoName = "Bitcoin",
                    cryptoSymbol = "BTC",
                    walletAddress = DonationConfig.BITCOIN_ADDRESS,
                    color = Color(0xFFF7931A),
                    onCopyClick = {
                        viewModel.copyAddress(DonationConfig.BITCOIN_ADDRESS, "Bitcoin")
                    },
                    onQRClick = {
                        selectedQRCode = "Bitcoin" to DonationConfig.BITCOIN_ADDRESS
                    },
                    onOpenWallet = {
                        walletSelectionState = Triple(CryptoType.BITCOIN, DonationConfig.BITCOIN_ADDRESS, "Bitcoin")
                    }
                )
            }
            
            // Ethereum
            item {
                WalletCard(
                    cryptoName = "Ethereum",
                    cryptoSymbol = "ETH",
                    walletAddress = DonationConfig.ETHEREUM_ADDRESS,
                    color = Color(0xFF627EEA),
                    onCopyClick = {
                        viewModel.copyAddress(DonationConfig.ETHEREUM_ADDRESS, "Ethereum")
                    },
                    onQRClick = {
                        selectedQRCode = "Ethereum" to DonationConfig.ETHEREUM_ADDRESS
                    },
                    onOpenWallet = {
                        walletSelectionState = Triple(CryptoType.ETHEREUM, DonationConfig.ETHEREUM_ADDRESS, "Ethereum")
                    }
                )
            }
            
            // Litecoin
            item {
                WalletCard(
                    cryptoName = "Litecoin",
                    cryptoSymbol = "LTC",
                    walletAddress = DonationConfig.LITECOIN_ADDRESS,
                    color = Color(0xFF345D9D),
                    onCopyClick = {
                        viewModel.copyAddress(DonationConfig.LITECOIN_ADDRESS, "Litecoin")
                    },
                    onQRClick = {
                        selectedQRCode = "Litecoin" to DonationConfig.LITECOIN_ADDRESS
                    },
                    onOpenWallet = {
                        walletSelectionState = Triple(CryptoType.LITECOIN, DonationConfig.LITECOIN_ADDRESS, "Litecoin")
                    }
                )
            }
            
            // Fund-a-Feature Section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
            
            item {
                FundaFeatureSection(
                    fundingGoals = state.fundingGoals
                )
            }
            
            // Thank you message
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💖",
                            style = MaterialTheme.typography.displayMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.thank_you_for_your_support),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.your_contribution_helps_keep_ireader),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
    
    // QR Code Dialog
    selectedQRCode?.let { (cryptoName, address) ->
        QRCodeDialog(
            cryptoName = cryptoName,
            address = address,
            onDismiss = { selectedQRCode = null }
        )
    }
    
    // Wallet Selection Dialog
    walletSelectionState?.let { (cryptoType, address, cryptoName) ->
        WalletSelectionDialog(
            cryptoType = cryptoType,
            address = address,
            onWalletSelected = { walletApp ->
                viewModel.openWallet(
                    walletApp = walletApp,
                    address = address,
                    amount = null
                )
            },
            onDismiss = { walletSelectionState = null },
            checkWalletInstalled = { walletApp ->
                viewModel.isWalletInstalled(walletApp)
            }
        )
    }
}

@Composable
private fun ExplanationCard() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper.localize(Res.string.why_donate),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                text = localizeHelper.localize(Res.string.ireader_is_a_free_open),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            BulletPoint("Server costs for sync and cloud features")
            BulletPoint("AI translation API costs")
            BulletPoint("Ongoing development and maintenance")
            BulletPoint("New feature development")
        }
    }
}

@Composable
private fun DisclaimerCard() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.important_disclaimer),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = localizeHelper.localize(Res.string.cryptocurrency_donations_are_non_refundable),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CardPaymentCard(
    onDonateClick: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = localizeHelper.localize(Res.string.donate_via_card_1),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.support_the_project_with_a),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Button(
                onClick = onDonateClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(localizeHelper.localize(Res.string.donate_now))
            }
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = "• ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WalletCard(
    cryptoName: String,
    cryptoSymbol: String,
    walletAddress: String,
    color: Color,
    onCopyClick: () -> Unit,
    onQRClick: () -> Unit,
    onOpenWallet: () -> Unit = {}
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with crypto name and symbol
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(color.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cryptoSymbol,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = cryptoName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Wallet address
            Text(
                text = localizeHelper.localize(Res.string.wallet_address),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = walletAddress,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Action buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary action: Open Wallet
                Button(
                    onClick = onOpenWallet,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(localizeHelper.localize(Res.string.open_in_wallet))
                }
                
                // Secondary actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onCopyClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(localizeHelper.localize(Res.string.copy))
                    }
                    
                    OutlinedButton(
                        onClick = onQRClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(localizeHelper.localize(Res.string.qr_code))
                    }
                }
            }
        }
    }
}

@Composable
private fun QRCodeDialog(
    cryptoName: String,
    address: String,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val qrCodeBitmap = remember(address) {
        QRCodeGenerator.generateQRCode(address)
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$cryptoName Address",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                qrCodeBitmap?.let { bitmap ->
                    Image(
                        bitmap = bitmap,
                        contentDescription = "QR Code for $cryptoName",
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(16.dp)
                    )
                } ?: run {
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = localizeHelper.localize(Res.string.failed_to_generate_qr_code),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = address,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = localizeHelper.localize(Res.string.scan_this_qr_code_with_your_crypto_wallet_app),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(localizeHelper.localize(Res.string.close))
                }
            }
        }
    }
}
