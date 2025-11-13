package ireader.domain.services

import ireader.domain.models.donation.WalletApp
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

/**
 * Desktop implementation of WalletIntegrationManager for donation features only
 * This is NOT used for authentication
 */
class DesktopWalletIntegrationManager : WalletIntegrationManager {
    
    override suspend fun isWalletInstalled(walletApp: WalletApp): Boolean {
        // On desktop, we can't easily check if wallet apps are installed
        // Return true to allow attempting to open the URI
        return true
    }
    
    override suspend fun openWallet(walletApp: WalletApp, paymentUri: String): Boolean {
        return try {
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(URI(paymentUri))
                    true
                } else {
                    false
                }
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    override fun generatePaymentUri(
        address: String,
        amount: String?,
        token: String?
    ): String {
        val baseUri = "ethereum:$address"
        val params = mutableListOf<String>()
        
        amount?.let { params.add("value=$it") }
        token?.let { params.add("token=$it") }
        
        return if (params.isNotEmpty()) {
            "$baseUri?${params.joinToString("&")}"
        } else {
            baseUri
        }
    }
    
    override fun copyToClipboard(text: String, label: String) {
        val selection = StringSelection(text)
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        clipboard.setContents(selection, selection)
    }
}
