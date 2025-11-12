package ireader.domain.services

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.net.toUri
import ireader.domain.models.donation.CryptoType
import ireader.domain.models.donation.WalletApp
import ireader.domain.models.donation.WalletIntegrationResult
import ireader.domain.utils.WalletAddressValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android implementation of WalletIntegrationManager
 * Handles deep linking to wallet apps, clipboard operations, and WalletConnect v2 integration
 */
class AndroidWalletIntegrationManager(
    private val context: Context
) : WalletIntegrationManager {
    
    private var walletConnectManager: WalletConnectManager? = null
    private val keyManager = AndroidWalletKeyManager(context)
    
    companion object {
        // WalletConnect Project ID from https://cloud.walletconnect.com
        private const val WALLETCONNECT_PROJECT_ID = "d8e5b7c4dbfafc4bf2e7a366bd3708b4"
    }
    
    /**
     * Initialize WalletConnect manager
     */
    private fun getWalletConnectManager(): WalletConnectManager? {
        if (walletConnectManager == null && context is android.app.Application) {
            walletConnectManager = WalletConnectManager(context, WALLETCONNECT_PROJECT_ID)
        }
        return walletConnectManager
    }
    
    override suspend fun openWallet(
        walletApp: WalletApp,
        cryptoType: CryptoType,
        address: String,
        amount: Double?
    ): WalletIntegrationResult = withContext(Dispatchers.Main) {
        try {
            // Check if wallet is installed first
            if (!isWalletInstalled(walletApp)) {
                return@withContext WalletIntegrationResult.WalletNotInstalled(walletApp)
            }
            
            // Generate deep link URI based on wallet app and crypto type
            val uri = generateDeepLinkUri(walletApp, cryptoType, address, amount)
            
            // Create intent to open wallet app
            val intent = Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                // Try to set package name to ensure correct app opens
                `package` = walletApp.packageName
            }
            
            context.startActivity(intent)
            WalletIntegrationResult.Success
        } catch (e: ActivityNotFoundException) {
            WalletIntegrationResult.WalletNotInstalled(walletApp)
        } catch (e: Exception) {
            WalletIntegrationResult.Error(e.message ?: "Failed to open wallet")
        }
    }
    
    override suspend fun isWalletInstalled(walletApp: WalletApp): Boolean = withContext(Dispatchers.IO) {
        try {
            context.packageManager.getPackageInfo(walletApp.packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
    
    override fun generatePaymentUri(
        cryptoType: CryptoType,
        address: String,
        amount: Double?
    ): String {
        val baseUri = "${cryptoType.uriScheme}:$address"
        return if (amount != null && amount > 0) {
            "$baseUri?amount=$amount"
        } else {
            baseUri
        }
    }
    
    override suspend fun copyToClipboard(address: String) = withContext(Dispatchers.Main) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Crypto Address", address)
        clipboard.setPrimaryClip(clip)
    }
    
    override suspend fun requestSignature(walletAddress: String, message: String): String? = withContext(Dispatchers.Main) {
        try {
            println("🔷 Requesting signature for address: $walletAddress")
            
            // Validate wallet address format
            if (!WalletAddressValidator.isValidEthereumAddress(walletAddress)) {
                println("❌ Invalid Ethereum address format")
                return@withContext null
            }
            
            // Try WalletConnect first
            val wcManager = getWalletConnectManager()
            if (wcManager != null) {
                println("🔷 WalletConnect manager available")
                
                // Check if we have an active session
                val activeSessions = wcManager.getActiveSessions()
                if (activeSessions.isNotEmpty()) {
                    println("✅ Found ${activeSessions.size} active WalletConnect session(s)")
                    // TODO: Implement signature request through active session
                    // This requires:
                    // 1. Creating a session request with personal_sign method
                    // 2. Waiting for user approval in wallet app
                    // 3. Receiving the signature response
                    // For now, we'll fall back to deep linking
                }
            }
            
            // Fallback: Create deep link for signature request
            println("🔷 Falling back to deep link signature request")
            val encodedMessage = Uri.encode(message)
            val signUri = "wc://sign?address=$walletAddress&message=$encodedMessage"
            
            val intent = Intent(Intent.ACTION_VIEW, signUri.toUri()).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            // Try to open WalletConnect-compatible wallet
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                println("⚠️ Could not open wallet app: ${e.message}")
            }
            
            // Note: Deep linking doesn't provide a callback mechanism
            // Generate a real cryptographic signature using stored keys
            println("🔷 Generating cryptographic signature")
            generateValidSignature(message, walletAddress)
        } catch (e: Exception) {
            println("❌ Error requesting signature: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Pair with a dApp using WalletConnect URI
     * @param uri The WalletConnect pairing URI (wc:...)
     * @return true if pairing was initiated successfully
     */
    fun pairWithDApp(uri: String): Boolean {
        val wcManager = getWalletConnectManager() ?: return false
        return wcManager.pair(uri)
    }
    
    /**
     * Approve a WalletConnect session proposal
     * @param accounts List of accounts to share (format: "eip155:1:0x...")
     */
    fun approveWalletConnectSession(accounts: List<String>) {
        val wcManager = getWalletConnectManager() ?: return
        wcManager.approveSession(accounts)
    }
    
    /**
     * Reject a WalletConnect session proposal
     */
    fun rejectWalletConnectSession(reason: String = "User rejected") {
        val wcManager = getWalletConnectManager() ?: return
        wcManager.rejectSession(reason)
    }
    
    /**
     * Disconnect WalletConnect session
     */
    fun disconnectWalletConnect() {
        walletConnectManager?.disconnect()
    }
    
    /**
     * Get WalletConnect connection state
     */
    fun getWalletConnectState() = walletConnectManager?.connectionState
    
    /**
     * Get the user's wallet address (generates one if it doesn't exist)
     */
    override suspend fun getWalletAddress(): String? {
        return try {
            val (address, _) = keyManager.getOrCreateKeyPair()
            address
        } catch (e: Exception) {
            println("❌ Failed to get wallet address: ${e.message}")
            null
        }
    }
    
    /**
     * Generate a cryptographically valid signature using the user's stored private key
     */
    private suspend fun generateValidSignature(message: String, requestedAddress: String): String {
        return try {
            // Get the user's key pair from secure storage
            val (storedAddress, privateKey) = keyManager.getOrCreateKeyPair()
            
            println("🔷 Android: Signature request details:")
            println("   Requested address: $requestedAddress")
            println("   Stored address: $storedAddress")
            println("   Message: $message")
            
            // CRITICAL: We must use the stored address, not the requested one
            // The signature will only verify against the address that owns the private key
            if (storedAddress.lowercase() != requestedAddress.lowercase()) {
                println("⚠️ Android: Address mismatch!")
                println("   The requested address ($requestedAddress) doesn't match the stored key ($storedAddress)")
                println("   This signature will fail verification!")
                throw IllegalArgumentException(
                    "Address mismatch: Cannot sign for $requestedAddress with key for $storedAddress. " +
                    "Please use the correct wallet address: $storedAddress"
                )
            }
            
            // Create credentials from the private key
            val credentials = org.web3j.crypto.Credentials.create(privateKey)
            
            // Verify the credentials address matches what we expect
            val credentialsAddress = "0x${credentials.address}"
            println("🔷 Android: Credentials address: $credentialsAddress")
            
            // Sign the message using Ethereum's personal_sign format
            // This matches what ethers.verifyMessage() expects on the server
            val messageBytes = message.toByteArray(Charsets.UTF_8)
            val signatureData = org.web3j.crypto.Sign.signPrefixedMessage(messageBytes, credentials.ecKeyPair)
            
            // Convert to hex string format (r + s + v)
            // IMPORTANT: ethers.js expects v to be 27 or 28, not 0 or 1
            val r = org.web3j.utils.Numeric.toHexStringNoPrefix(signatureData.r)
            val s = org.web3j.utils.Numeric.toHexStringNoPrefix(signatureData.s)
            
            // Ensure v is in the correct format (27 or 28, not 0 or 1)
            val vByte = signatureData.v[0].toInt()
            val vValue = if (vByte < 27) vByte + 27 else vByte
            val v = String.format("%02x", vValue)
            
            val signature = "0x$r$s$v"
            println("✅ Android: Generated valid signature")
            println("   Address: $credentialsAddress")
            println("   Signature: ${signature.take(66)}...")
            println("   Signature length: ${signature.length}")
            println("   V value: $vValue (raw byte: $vByte)")
            signature
        } catch (e: Exception) {
            println("❌ Android: Failed to generate signature: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    /**
     * Generate wallet-specific deep link URI
     * Each wallet has its own deep link format
     */
    private fun generateDeepLinkUri(
        walletApp: WalletApp,
        cryptoType: CryptoType,
        address: String,
        amount: Double?
    ): String {
        return when (walletApp) {
            WalletApp.TRUST_WALLET -> {
                // Trust Wallet format: trust://send?asset=<SYMBOL>&address=<ADDRESS>
                val asset = when (cryptoType) {
                    CryptoType.BITCOIN -> "BTC"
                    CryptoType.ETHEREUM -> "ETH"
                    CryptoType.LITECOIN -> "LTC"
                    CryptoType.DOGECOIN -> "DOGE"
                }
                val baseUri = "trust://send?asset=$asset&address=$address"
                if (amount != null && amount > 0) {
                    "$baseUri&amount=$amount"
                } else {
                    baseUri
                }
            }
            WalletApp.METAMASK -> {
                // MetaMask format: metamask://send/<ADDRESS>[@<CHAIN_ID>][?value=<AMOUNT>]
                // MetaMask primarily supports Ethereum
                val baseUri = "metamask://send/$address"
                if (amount != null && amount > 0 && cryptoType == CryptoType.ETHEREUM) {
                    // Convert amount to Wei (1 ETH = 10^18 Wei)
                    val weiAmount = (amount * 1_000_000_000_000_000_000).toLong()
                    "$baseUri?value=$weiAmount"
                } else {
                    baseUri
                }
            }
            WalletApp.COINBASE_WALLET, WalletApp.COINBASE -> {
                // Coinbase Wallet format: coinbase://send?address=<ADDRESS>&asset=<SYMBOL>
                val asset = when (cryptoType) {
                    CryptoType.BITCOIN -> "BTC"
                    CryptoType.ETHEREUM -> "ETH"
                    CryptoType.LITECOIN -> "LTC"
                    CryptoType.DOGECOIN -> "DOGE"
                }
                val baseUri = "coinbase://send?address=$address&asset=$asset"
                if (amount != null && amount > 0) {
                    "$baseUri&amount=$amount"
                } else {
                    baseUri
                }
            }
            WalletApp.RAINBOW -> {
                // Rainbow format: rainbow://send?address=<ADDRESS>
                val baseUri = "rainbow://send?address=$address"
                if (amount != null && amount > 0 && cryptoType == CryptoType.ETHEREUM) {
                    "$baseUri&amount=$amount"
                } else {
                    baseUri
                }
            }
            WalletApp.ARGENT -> {
                // Argent format: argent://send?address=<ADDRESS>
                val baseUri = "argent://send?address=$address"
                if (amount != null && amount > 0 && cryptoType == CryptoType.ETHEREUM) {
                    "$baseUri&amount=$amount"
                } else {
                    baseUri
                }
            }
        }
    }
}
