package ireader.domain.services

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import ireader.domain.models.donation.WalletApp

/**
 * Android implementation of WalletIntegrationManager for donation features only
 * This is NOT used for authentication
 */
class AndroidWalletIntegrationManager(
    private val context: Context
) : WalletIntegrationManager {

    override suspend fun isWalletInstalled(walletApp: WalletApp): Boolean {
        return try {
            context.packageManager.getPackageInfo(walletApp.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    override suspend fun openWallet(walletApp: WalletApp, paymentUri: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(paymentUri)
                setPackage(walletApp.packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            true
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
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}
