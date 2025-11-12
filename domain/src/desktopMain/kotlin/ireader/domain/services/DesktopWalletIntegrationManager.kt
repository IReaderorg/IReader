//package ireader.domain.services
//
//import ireader.domain.models.donation.CryptoType
//import ireader.domain.models.donation.WalletApp
//import ireader.domain.models.donation.WalletIntegrationResult
//import ireader.domain.utils.WalletAddressValidator
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//import java.awt.Desktop
//import java.awt.Toolkit
//import java.awt.datatransfer.StringSelection
//import java.net.URI
//
///**
// * Desktop implementation of WalletIntegrationManager
// * On desktop, wallet apps are not typically installed, so we focus on:
// * - Generating payment URIs that can be opened in browsers
// * - Clipboard operations for manual copying
// * - QR code display (handled in UI layer)
// */
//class DesktopWalletIntegrationManager : WalletIntegrationManager {
//
//    override suspend fun openWallet(
//        walletApp: WalletApp,
//        cryptoType: CryptoType,
//        address: String,
//        amount: Double?
//    ): WalletIntegrationResult = withContext(Dispatchers.IO) {
//        try {
//            // On desktop, we can't directly open wallet apps
//            // Instead, we try to open the payment URI in the default browser
//            // This might open a web version of the wallet or prompt to install
//            val paymentUri = generatePaymentUri(cryptoType, address, amount)
//
//            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
//                Desktop.getDesktop().browse(URI(paymentUri))
//                WalletIntegrationResult.Success
//            } else {
//                WalletIntegrationResult.Error("Desktop browsing not supported")
//            }
//        } catch (e: Exception) {
//            WalletIntegrationResult.Error(e.message ?: "Failed to open payment URI")
//        }
//    }
//
//    override suspend fun isWalletInstalled(walletApp: WalletApp): Boolean {
//        // On desktop, wallet apps are typically not installed as native apps
//        // Always return false to indicate users should use QR codes or copy addresses
//        return false
//    }
//
//    override fun generatePaymentUri(
//        cryptoType: CryptoType,
//        address: String,
//        amount: Double?
//    ): String {
//        val baseUri = "${cryptoType.uriScheme}:$address"
//        return if (amount != null && amount > 0) {
//            "$baseUri?amount=$amount"
//        } else {
//            baseUri
//        }
//    }
//
//    override suspend fun copyToClipboard(address: String) = withContext(Dispatchers.Main) {
//        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
//        val selection = StringSelection(address)
//        clipboard.setContents(selection, selection)
//    }
//
//    override suspend fun requestSignature(walletAddress: String, message: String): String? = withContext(Dispatchers.IO) {
//        // Validate wallet address format
//        if (!WalletAddressValidator.isValidEthereumAddress(walletAddress)) {
//            return@withContext null
//        }
//
//        // On desktop, signature requests are handled via QR code display
//        // The UI layer should display a QR code containing the signature request
//        // and wait for the user to scan it with their mobile wallet
//
//        // Generate WalletConnect URI for QR code
//        // Format: wc:<topic>@<version>?bridge=<bridge>&key=<key>
//        // This is a simplified placeholder - real implementation would use WalletConnect SDK
//
//        // TODO: Implement proper WalletConnect QR code generation
//        // For now, return null to indicate the feature needs full implementation
//        null
//    }
//}
