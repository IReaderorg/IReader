package ireader.domain.services

import ireader.domain.models.donation.CryptoType
import ireader.domain.models.donation.WalletApp
import ireader.domain.models.donation.WalletIntegrationResult
import kotlinx.coroutines.withTimeout
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

/**
 * Desktop implementation of WalletIntegrationManager
 * 
 * For desktop, wallet integration works differently than mobile:
 * - Uses test signatures for development
 * - Production would integrate with browser extensions or WalletConnect
 */
class DesktopWalletIntegrationManager : WalletIntegrationManager {
    
    private var connectedWalletAddress: String? = null
    private val keyManager = DesktopWalletKeyManager()
    private val browserWalletServer = BrowserWalletServer()
    private var useBrowserWallet = true // Production mode: use browser wallets
    
    override suspend fun openWallet(
        walletApp: WalletApp,
        cryptoType: CryptoType,
        address: String,
        amount: Double?
    ): WalletIntegrationResult {
        return try {
            val uri = generatePaymentUri(cryptoType, address, amount)
            openBrowser(uri)
            WalletIntegrationResult.Success
        } catch (e: Exception) {
            WalletIntegrationResult.Error(e.message ?: "Failed to open wallet")
        }
    }
    
    override suspend fun isWalletInstalled(walletApp: WalletApp): Boolean {
        // On desktop, check if browser is available
        return Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)
    }
    
    override fun generatePaymentUri(
        cryptoType: CryptoType,
        address: String,
        amount: Double?
    ): String {
        val scheme = when (cryptoType) {
            CryptoType.ETHEREUM -> "ethereum"
            CryptoType.BITCOIN -> "bitcoin"
            CryptoType.LITECOIN -> "litecoin"
            CryptoType.DOGECOIN -> "dogecoin"
        }
        
        return if (amount != null) {
            "$scheme:$address?amount=$amount"
        } else {
            "$scheme:$address"
        }
    }
    
    override suspend fun copyToClipboard(address: String) {
        val clipboard = Toolkit.getDefaultToolkit().systemClipboard
        val selection = StringSelection(address)
        clipboard.setContents(selection, selection)
    }
    
    /**
     * Request a signature from the user's wallet
     * 
     * For desktop, this can:
     * 1. Open a browser page that connects to MetaMask/browser wallet extensions
     * 2. Generate a test signature for development
     * 
     * Set WALLET_USE_BROWSER=true environment variable to open browser for real signing
     */
    override suspend fun requestSignature(walletAddress: String, message: String): String? {
        return try {
            connectedWalletAddress = walletAddress
            
            // Check mode: browser wallet (production) or local keys (development/testing)
            val useLocalKeys = System.getenv("WALLET_USE_LOCAL_KEYS")?.toBoolean() ?: false
            
            if (useLocalKeys) {
                // Development/testing mode: use locally generated keys
                println("🔷 Desktop: Using local key signing (development mode)")
                generateValidSignature(message, walletAddress)
            } else {
                // Production mode: use browser wallet (MetaMask, etc.)
                println("🌐 Desktop: Requesting signature from browser wallet...")
                requestBrowserWalletSignature(walletAddress, message)
            }
        } catch (e: Exception) {
            println("❌ Desktop signature request failed: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Request signature from browser wallet (MetaMask, etc.)
     * Opens browser page and waits for user to sign
     */
    private suspend fun requestBrowserWalletSignature(walletAddress: String, message: String): String? {
        return try {
            // Ensure server is running
            if (browserWalletServer.connectionState.value is BrowserWalletServer.WalletConnectionState.Disconnected) {
                println("🌐 Starting browser wallet server...")
                browserWalletServer.start()
                
                // Open browser to wallet connection page
                println("🌐 Opening browser for wallet connection...")
                openBrowser("http://localhost:48923/wallet")
                
                // Wait for wallet to connect
                println("⏳ Waiting for wallet connection...")
                val connectedAddress = withTimeout(60000) { // 60 second timeout
                    browserWalletServer.waitForAddress()
                }
                
                if (connectedAddress == null) {
                    println("❌ No wallet connected")
                    return null
                }
                
                println("✅ Wallet connected: $connectedAddress")
            }
            
            // Send signature request to browser
            println("🔷 Sending signature request to browser...")
            val requestUrl = java.net.URL("http://localhost:48923/request-signature")
            val connection = requestUrl.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val requestBody = """{"message":"$message"}"""
            connection.outputStream.write(requestBody.toByteArray())
            connection.outputStream.flush()
            
            val responseCode = connection.responseCode
            if (responseCode != 200) {
                println("❌ Failed to send signature request: HTTP $responseCode")
                return null
            }
            
            println("✅ Signature request sent to browser")
            println("⏳ Waiting for user to sign in browser...")
            
            val signatureResponse = withTimeout(120000) { // 2 minute timeout for signing
                browserWalletServer.waitForSignature()
            }
            
            if (signatureResponse == null) {
                println("❌ No signature received")
                return null
            }
            
            println("✅ Signature received from browser wallet")
            println("   Address: ${signatureResponse.address}")
            println("   Signature: ${signatureResponse.signature.take(66)}...")
            
            signatureResponse.signature
        } catch (e: kotlinx.coroutines.TimeoutCancellationException) {
            println("❌ Timeout waiting for wallet signature")
            null
        } catch (e: Exception) {
            println("❌ Error requesting browser wallet signature: ${e.message}")
            e.printStackTrace()
            null
        }
    }
    
    /**
     * Open browser with a page that can interact with MetaMask or other wallet extensions
     * This would typically open a custom web page hosted by your app that:
     * 1. Detects MetaMask/wallet extension
     * 2. Requests signature via web3.js/ethers.js
     * 3. Sends signature back to desktop app via local server or deep link
     */
    private fun openBrowserForSigning(walletAddress: String, message: String) {
        try {
            // Encode message for URL
            val encodedMessage = java.net.URLEncoder.encode(message, "UTF-8")
            val encodedAddress = java.net.URLEncoder.encode(walletAddress, "UTF-8")
            
            // Option 1: Open a custom web page (you would need to host this)
            // val url = "https://your-app.com/wallet-sign?address=$encodedAddress&message=$encodedMessage"
            
            // Option 2: Open WalletConnect web app
            // val url = "https://app.walletconnect.com/"
            
            // Option 3: For development, open a local HTML page that uses MetaMask
            val htmlContent = createSigningHtmlPage(walletAddress, message)
            val tempFile = java.io.File.createTempFile("wallet-sign-", ".html")
            tempFile.writeText(htmlContent)
            tempFile.deleteOnExit()
            
            val url = tempFile.toURI().toString()
            
            println("🌐 Opening browser: $url")
            openBrowser(url)
            
            println("""
                ℹ️  Browser opened for wallet signing.
                   Please approve the signature request in your browser wallet (MetaMask, etc.)
                   
                   Note: This is a development feature. In production, you would:
                   1. Host a web page that handles wallet signing
                   2. Implement a callback mechanism (local server or deep link)
                   3. Return the signature to the desktop app
            """.trimIndent())
        } catch (e: Exception) {
            println("❌ Failed to open browser: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Create an HTML page that can interact with MetaMask browser extension
     */
    private fun createSigningHtmlPage(address: String, message: String): String {
        return """
<!DOCTYPE html>
<html>
<head>
    <title>IReader - Wallet Signature Request</title>
    <meta charset="UTF-8">
    <style>
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            max-width: 600px;
            margin: 50px auto;
            padding: 20px;
            background: #f5f5f5;
        }
        .container {
            background: white;
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.1);
        }
        h1 { color: #333; margin-top: 0; }
        .message-box {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            margin: 20px 0;
            word-break: break-all;
            font-family: monospace;
            font-size: 14px;
        }
        button {
            background: #0066ff;
            color: white;
            border: none;
            padding: 12px 24px;
            border-radius: 8px;
            font-size: 16px;
            cursor: pointer;
            width: 100%;
            margin-top: 10px;
        }
        button:hover { background: #0052cc; }
        button:disabled { background: #ccc; cursor: not-allowed; }
        .status {
            margin-top: 20px;
            padding: 15px;
            border-radius: 8px;
            display: none;
        }
        .status.success { background: #d4edda; color: #155724; display: block; }
        .status.error { background: #f8d7da; color: #721c24; display: block; }
        .status.info { background: #d1ecf1; color: #0c5460; display: block; }
        .signature-result {
            margin-top: 15px;
            padding: 10px;
            background: #e7f3ff;
            border-radius: 6px;
            word-break: break-all;
            font-family: monospace;
            font-size: 12px;
        }
    </style>
</head>
<body>
    <div class="container">
        <h1>🔐 IReader Wallet Signature</h1>
        
        <p><strong>Address:</strong></p>
        <div class="message-box">${address}</div>
        
        <p><strong>Message to sign:</strong></p>
        <div class="message-box">${message}</div>
        
        <button id="signBtn" onclick="signMessage()">Sign with MetaMask</button>
        
        <div id="status" class="status"></div>
    </div>
    
    <script>
        const address = "${address}";
        const message = "${message}";
        
        async function signMessage() {
            const statusDiv = document.getElementById('status');
            const signBtn = document.getElementById('signBtn');
            
            try {
                // Check if MetaMask is installed
                if (typeof window.ethereum === 'undefined') {
                    statusDiv.className = 'status error';
                    statusDiv.innerHTML = '❌ MetaMask not detected. Please install MetaMask browser extension.';
                    return;
                }
                
                signBtn.disabled = true;
                statusDiv.className = 'status info';
                statusDiv.innerHTML = '⏳ Requesting signature from MetaMask...';
                
                // Request accounts
                const accounts = await window.ethereum.request({ 
                    method: 'eth_requestAccounts' 
                });
                
                console.log('Connected accounts:', accounts);
                
                // Sign the message
                const signature = await window.ethereum.request({
                    method: 'personal_sign',
                    params: [message, address]
                });
                
                console.log('Signature:', signature);
                
                statusDiv.className = 'status success';
                statusDiv.innerHTML = `
                    ✅ Signature successful!<br><br>
                    <strong>Signature:</strong>
                    <div class="signature-result">${'$'}{signature}</div>
                    <br>
                    <strong>Next steps:</strong><br>
                    1. Copy the signature above<br>
                    2. Return to IReader desktop app<br>
                    3. Paste the signature when prompted<br>
                    <br>
                    <em>Note: In production, this would automatically send the signature back to the app.</em>
                `;
                
                // In production, you would send the signature back to the desktop app
                // via a local server, WebSocket, or deep link
                
            } catch (error) {
                console.error('Signing error:', error);
                signBtn.disabled = false;
                statusDiv.className = 'status error';
                statusDiv.innerHTML = `❌ Error: ${'$'}{error.message || 'Signature request failed'}`;
            }
        }
        
        // Auto-detect MetaMask on page load
        window.addEventListener('load', () => {
            const statusDiv = document.getElementById('status');
            if (typeof window.ethereum !== 'undefined') {
                statusDiv.className = 'status info';
                statusDiv.innerHTML = '✅ MetaMask detected! Click the button above to sign.';
            } else {
                statusDiv.className = 'status error';
                statusDiv.innerHTML = '❌ MetaMask not detected. Please install MetaMask browser extension.';
            }
        });
    </script>
</body>
</html>
        """.trimIndent()
    }
    
    /**
     * Generate a cryptographically valid Ethereum signature using the user's stored private key
     */
    private suspend fun generateValidSignature(message: String, requestedAddress: String): String {
        return try {
            // Get the user's key pair from secure storage
            val (storedAddress, privateKey) = keyManager.getOrCreateKeyPair()
            
            println("🔷 Desktop: Signature request details:")
            println("   Requested address: $requestedAddress")
            println("   Stored address: $storedAddress")
            println("   Message: $message")
            
            // CRITICAL: We must use the stored address, not the requested one
            // The signature will only verify against the address that owns the private key
            // Compare addresses case-insensitively (Ethereum addresses are case-insensitive)
            val storedAddressClean = storedAddress.removePrefix("0x").lowercase()
            val requestedAddressClean = requestedAddress.removePrefix("0x").lowercase()
            
            if (storedAddressClean != requestedAddressClean) {
                println("⚠️ Desktop: Address mismatch!")
                println("   The requested address ($requestedAddress) doesn't match the stored key ($storedAddress)")
                println("   This signature will fail verification!")
                throw IllegalArgumentException(
                    "Address mismatch: Cannot sign for $requestedAddress with key for $storedAddress. " +
                    "Please use the correct wallet address: $storedAddress"
                )
            }
            
            // Create credentials from the private key
            val credentials = org.web3j.crypto.Credentials.create(privateKey)
            
            // Verify the credentials address matches what we expect (use checksummed format)
            val credentialsAddress = org.web3j.crypto.Keys.toChecksumAddress(credentials.address)
            println("🔷 Desktop: Credentials address: $credentialsAddress")
            
            // Sign the message using Ethereum's personal_sign format
            // This matches what ethers.verifyMessage() expects on the server
            val messageBytes = message.toByteArray(Charsets.UTF_8)
            val signatureData = org.web3j.crypto.Sign.signPrefixedMessage(messageBytes, credentials.ecKeyPair)
            
            // Convert to hex string format (r + s + v)
            val r = org.web3j.utils.Numeric.toHexStringNoPrefix(signatureData.r)
            val s = org.web3j.utils.Numeric.toHexStringNoPrefix(signatureData.s)
            
            // web3j returns v as a byte array with the actual v value (27 or 28)
            // We need to convert it to hex
            val vByte = signatureData.v[0].toInt() and 0xFF
            println("🔷 Desktop: Raw v byte value: $vByte")
            
            // The v value should already be 27 or 28 from signPrefixedMessage
            // If it's 0 or 1, we need to add 27
            val vValue = if (vByte == 0 || vByte == 1) vByte + 27 else vByte
            val v = String.format("%02x", vValue)
            
            val signature = "0x$r$s$v"
            println("✅ Desktop: Generated valid signature")
            println("   Address: $credentialsAddress")
            println("   Signature: $signature")
            println("   Signature length: ${signature.length}")
            println("   R: 0x$r")
            println("   S: 0x$s")
            println("   V: $vValue (0x$v)")
            signature
        } catch (e: Exception) {
            println("❌ Desktop: Failed to generate signature: ${e.message}")
            e.printStackTrace()
            throw e
        }
    }
    
    override suspend fun getWalletAddress(): String? {
        return try {
            val useLocalKeys = System.getenv("WALLET_USE_LOCAL_KEYS")?.toBoolean() ?: false
            
            if (useLocalKeys) {
                // Development mode: use local keys
                keyManager.getAddress()
            } else {
                // Production mode: get address from browser wallet
                when (val state = browserWalletServer.connectionState.value) {
                    is BrowserWalletServer.WalletConnectionState.Connected -> state.address
                    is BrowserWalletServer.WalletConnectionState.Disconnected -> {
                        // Start server and wait for connection
                        println("🌐 Starting browser wallet server...")
                        browserWalletServer.start()
                        
                        println("🌐 Opening browser for wallet connection...")
                        openBrowser("http://localhost:48923/wallet")
                        
                        println("⏳ Waiting for wallet connection...")
                        withTimeout(60000) {
                            browserWalletServer.waitForAddress()
                        }
                    }
                    else -> null
                }
            }
        } catch (e: Exception) {
            println("❌ Desktop: Failed to get wallet address: ${e.message}")
            null
        }
    }
    
    /**
     * Disconnect browser wallet and stop server
     */
    fun disconnectBrowserWallet() {
        browserWalletServer.stop()
        connectedWalletAddress = null
        println("🔌 Browser wallet disconnected")
    }
    
    private fun openBrowser(url: String) {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}
