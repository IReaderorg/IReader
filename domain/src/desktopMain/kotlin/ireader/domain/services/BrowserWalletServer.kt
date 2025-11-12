package ireader.domain.services

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

/**
 * Local HTTP server for browser wallet integration
 * Handles communication between desktop app and browser-based wallets (MetaMask, etc.)
 */
class BrowserWalletServer(
    private val port: Int = 48923 // Random high port
) {
    private var server: HttpServer? = null
    private val _connectionState = MutableStateFlow<WalletConnectionState>(WalletConnectionState.Disconnected)
    val connectionState: StateFlow<WalletConnectionState> = _connectionState
    
    // Channels for async communication
    private val signatureChannel = Channel<SignatureResponse>(Channel.CONFLATED)
    private val addressChannel = Channel<String>(Channel.CONFLATED)
    
    sealed class WalletConnectionState {
        object Disconnected : WalletConnectionState()
        object Connecting : WalletConnectionState()
        data class Connected(val address: String, val chainId: Int) : WalletConnectionState()
        data class Error(val message: String) : WalletConnectionState()
    }
    
    data class SignatureResponse(
        val signature: String,
        val address: String,
        val message: String
    )
    
    fun start() {
        if (server != null) {
            println("🌐 BrowserWalletServer: Server already running on port $port")
            return
        }
        
        try {
            server = HttpServer.create(InetSocketAddress("localhost", port), 0).apply {
                // Endpoint for wallet connection
                createContext("/connect") { exchange ->
                    handleConnect(exchange)
                }
                
                // Endpoint for signature responses
                createContext("/signature") { exchange ->
                    handleSignature(exchange)
                }
                
                // Endpoint to request signing (called by desktop app)
                createContext("/request-signature") { exchange ->
                    handleSignatureRequest(exchange)
                }
                
                // Endpoint for wallet disconnection
                createContext("/disconnect") { exchange ->
                    handleDisconnect(exchange)
                }
                
                // Health check endpoint
                createContext("/health") { exchange ->
                    sendResponse(exchange, 200, """{"status":"ok"}""")
                }
                
                // Serve the wallet connection page
                createContext("/wallet") { exchange ->
                    serveWalletPage(exchange)
                }
                
                executor = null // Use default executor
                start()
            }
            
            println("✅ BrowserWalletServer: Started on http://localhost:$port")
            println("   Wallet page: http://localhost:$port/wallet")
        } catch (e: Exception) {
            println("❌ BrowserWalletServer: Failed to start server: ${e.message}")
            _connectionState.value = WalletConnectionState.Error("Failed to start server: ${e.message}")
            throw e
        }
    }
    
    fun stop() {
        server?.stop(0)
        server = null
        _connectionState.value = WalletConnectionState.Disconnected
        println("🛑 BrowserWalletServer: Stopped")
    }
    
    suspend fun waitForSignature(): SignatureResponse? {
        return try {
            signatureChannel.receive()
        } catch (e: Exception) {
            println("❌ BrowserWalletServer: Error waiting for signature: ${e.message}")
            null
        }
    }
    
    suspend fun waitForAddress(): String? {
        return try {
            addressChannel.receive()
        } catch (e: Exception) {
            println("❌ BrowserWalletServer: Error waiting for address: ${e.message}")
            null
        }
    }
    
    private fun handleConnect(exchange: HttpExchange) {
        if (exchange.requestMethod != "POST") {
            sendResponse(exchange, 405, """{"error":"Method not allowed"}""")
            return
        }
        
        try {
            val body = exchange.requestBody.readBytes().toString(StandardCharsets.UTF_8)
            val params = parseJsonBody(body)
            
            val address = params["address"] ?: run {
                sendResponse(exchange, 400, """{"error":"Missing address"}""")
                return
            }
            
            val chainId = params["chainId"]?.toIntOrNull() ?: 1
            
            println("✅ BrowserWalletServer: Wallet connected - $address (chain: $chainId)")
            _connectionState.value = WalletConnectionState.Connected(address, chainId)
            
            // Send address to waiting coroutine
            addressChannel.trySend(address)
            
            sendResponse(exchange, 200, """{"success":true,"message":"Connected"}""")
        } catch (e: Exception) {
            println("❌ BrowserWalletServer: Error handling connect: ${e.message}")
            sendResponse(exchange, 500, """{"error":"${e.message}"}""")
        }
    }
    
    private fun handleSignature(exchange: HttpExchange) {
        if (exchange.requestMethod != "POST") {
            sendResponse(exchange, 405, """{"error":"Method not allowed"}""")
            return
        }
        
        try {
            val body = exchange.requestBody.readBytes().toString(StandardCharsets.UTF_8)
            val params = parseJsonBody(body)
            
            val signature = params["signature"] ?: run {
                sendResponse(exchange, 400, """{"error":"Missing signature"}""")
                return
            }
            
            val address = params["address"] ?: run {
                sendResponse(exchange, 400, """{"error":"Missing address"}""")
                return
            }
            
            val message = params["message"] ?: run {
                sendResponse(exchange, 400, """{"error":"Missing message"}""")
                return
            }
            
            println("✅ BrowserWalletServer: Received signature from $address")
            
            // Send signature to waiting coroutine
            signatureChannel.trySend(SignatureResponse(signature, address, message))
            
            sendResponse(exchange, 200, """{"success":true,"message":"Signature received"}""")
        } catch (e: Exception) {
            println("❌ BrowserWalletServer: Error handling signature: ${e.message}")
            sendResponse(exchange, 500, """{"error":"${e.message}"}""")
        }
    }
    
    private fun handleDisconnect(exchange: HttpExchange) {
        if (exchange.requestMethod != "POST") {
            sendResponse(exchange, 405, """{"error":"Method not allowed"}""")
            return
        }
        
        println("🔌 BrowserWalletServer: Wallet disconnected")
        _connectionState.value = WalletConnectionState.Disconnected
        
        sendResponse(exchange, 200, """{"success":true,"message":"Disconnected"}""")
    }
    
    private var pendingSignRequest: String? = null
    
    private fun handleSignatureRequest(exchange: HttpExchange) {
        if (exchange.requestMethod == "OPTIONS") {
            exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
            exchange.responseHeaders.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
            exchange.responseHeaders.add("Access-Control-Allow-Headers", "Content-Type")
            exchange.sendResponseHeaders(204, -1)
            return
        }
        
        if (exchange.requestMethod == "POST") {
            try {
                val body = exchange.requestBody.readBytes().toString(StandardCharsets.UTF_8)
                val params = parseJsonBody(body)
                val message = params["message"] ?: run {
                    sendResponse(exchange, 400, """{"error":"Missing message"}""")
                    return
                }
                
                pendingSignRequest = message
                println("📝 BrowserWalletServer: Signature request queued")
                sendResponse(exchange, 200, """{"success":true}""")
            } catch (e: Exception) {
                sendResponse(exchange, 500, """{"error":"${e.message}"}""")
            }
        } else if (exchange.requestMethod == "GET") {
            // Browser polls for pending sign requests
            val response = if (pendingSignRequest != null) {
                val msg = pendingSignRequest
                pendingSignRequest = null
                """{"pending":true,"message":"$msg"}"""
            } else {
                """{"pending":false}"""
            }
            sendResponse(exchange, 200, response)
        } else {
            sendResponse(exchange, 405, """{"error":"Method not allowed"}""")
        }
    }
    
    private fun serveWalletPage(exchange: HttpExchange) {
        val html = createWalletConnectionPage()
        val bytes = html.toByteArray(StandardCharsets.UTF_8)
        
        exchange.responseHeaders.add("Content-Type", "text/html; charset=UTF-8")
        exchange.sendResponseHeaders(200, bytes.size.toLong())
        exchange.responseBody.write(bytes)
        exchange.responseBody.close()
    }
    
    private fun sendResponse(exchange: HttpExchange, statusCode: Int, body: String) {
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")
        exchange.sendResponseHeaders(statusCode, bytes.size.toLong())
        exchange.responseBody.write(bytes)
        exchange.responseBody.close()
    }
    
    private fun parseJsonBody(body: String): Map<String, String> {
        // Simple JSON parser for our needs
        val result = mutableMapOf<String, String>()
        val cleaned = body.trim().removeSurrounding("{", "}")
        
        cleaned.split(",").forEach { pair ->
            val parts = pair.split(":", limit = 2)
            if (parts.size == 2) {
                val key = parts[0].trim().removeSurrounding("\"")
                val value = parts[1].trim().removeSurrounding("\"")
                result[key] = value
            }
        }
        
        return result
    }
    
    private fun createWalletConnectionPage(): String {
        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>IReader - Connect Wallet</title>
    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }
        
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        
        .container {
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            max-width: 500px;
            width: 100%;
            padding: 40px;
            animation: slideUp 0.5s ease-out;
        }
        
        @keyframes slideUp {
            from {
                opacity: 0;
                transform: translateY(30px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
        
        .logo {
            text-align: center;
            margin-bottom: 30px;
        }
        
        .logo h1 {
            color: #667eea;
            font-size: 32px;
            font-weight: 700;
            margin-bottom: 10px;
        }
        
        .logo p {
            color: #666;
            font-size: 16px;
        }
        
        .status {
            padding: 15px;
            border-radius: 12px;
            margin-bottom: 20px;
            display: none;
            animation: fadeIn 0.3s ease-out;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        
        .status.info {
            background: #e3f2fd;
            color: #1976d2;
            border-left: 4px solid #1976d2;
            display: block;
        }
        
        .status.success {
            background: #e8f5e9;
            color: #2e7d32;
            border-left: 4px solid #2e7d32;
            display: block;
        }
        
        .status.error {
            background: #ffebee;
            color: #c62828;
            border-left: 4px solid #c62828;
            display: block;
        }
        
        .status.warning {
            background: #fff3e0;
            color: #ef6c00;
            border-left: 4px solid #ef6c00;
            display: block;
        }
        
        .wallet-info {
            background: #f5f5f5;
            padding: 20px;
            border-radius: 12px;
            margin-bottom: 20px;
            display: none;
        }
        
        .wallet-info.show {
            display: block;
        }
        
        .wallet-info h3 {
            color: #333;
            font-size: 14px;
            font-weight: 600;
            margin-bottom: 10px;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .wallet-info .address {
            font-family: 'Courier New', monospace;
            font-size: 14px;
            color: #667eea;
            word-break: break-all;
            background: white;
            padding: 12px;
            border-radius: 8px;
            margin-bottom: 10px;
        }
        
        .wallet-info .chain {
            font-size: 13px;
            color: #666;
        }
        
        button {
            width: 100%;
            padding: 16px;
            border: none;
            border-radius: 12px;
            font-size: 16px;
            font-weight: 600;
            cursor: pointer;
            transition: all 0.3s ease;
            margin-bottom: 10px;
        }
        
        button:disabled {
            opacity: 0.6;
            cursor: not-allowed;
        }
        
        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
        }
        
        .btn-primary:hover:not(:disabled) {
            transform: translateY(-2px);
            box-shadow: 0 10px 20px rgba(102, 126, 234, 0.3);
        }
        
        .btn-secondary {
            background: #f5f5f5;
            color: #333;
        }
        
        .btn-secondary:hover:not(:disabled) {
            background: #e0e0e0;
        }
        
        .btn-danger {
            background: #f44336;
            color: white;
        }
        
        .btn-danger:hover:not(:disabled) {
            background: #d32f2f;
        }
        
        .instructions {
            background: #f9f9f9;
            padding: 20px;
            border-radius: 12px;
            margin-top: 20px;
            font-size: 14px;
            color: #666;
            line-height: 1.6;
        }
        
        .instructions h4 {
            color: #333;
            margin-bottom: 10px;
            font-size: 16px;
        }
        
        .instructions ol {
            margin-left: 20px;
        }
        
        .instructions li {
            margin-bottom: 8px;
        }
        
        .spinner {
            display: inline-block;
            width: 16px;
            height: 16px;
            border: 3px solid rgba(255, 255, 255, 0.3);
            border-radius: 50%;
            border-top-color: white;
            animation: spin 0.8s linear infinite;
            margin-right: 8px;
            vertical-align: middle;
        }
        
        @keyframes spin {
            to { transform: rotate(360deg); }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="logo">
            <h1>📚 IReader</h1>
            <p>Connect Your Wallet</p>
        </div>
        
        <div id="status" class="status"></div>
        
        <div id="walletInfo" class="wallet-info">
            <h3>Connected Wallet</h3>
            <div class="address" id="walletAddress"></div>
            <div class="chain">Network: <span id="chainName"></span></div>
        </div>
        
        <button id="connectBtn" class="btn-primary" onclick="connectWallet()">
            Connect MetaMask
        </button>
        
        <button id="disconnectBtn" class="btn-danger" onclick="disconnectWallet()" style="display: none;">
            Disconnect
        </button>
        
        <div class="instructions">
            <h4>How it works:</h4>
            <ol>
                <li>Click "Connect MetaMask" to connect your wallet</li>
                <li>Approve the connection in MetaMask</li>
                <li>Return to the IReader desktop app</li>
                <li>Your wallet will be connected automatically</li>
            </ol>
            <p style="margin-top: 15px;">
                <strong>Note:</strong> Keep this page open while using IReader to maintain the connection.
            </p>
        </div>
    </div>
    
    <script>
        const SERVER_URL = 'http://localhost:$port';
        let connectedAddress = null;
        let connectedChainId = null;
        
        // Check if MetaMask is installed
        window.addEventListener('load', () => {
            if (typeof window.ethereum === 'undefined') {
                showStatus('error', '❌ MetaMask not detected. Please install MetaMask browser extension.');
                document.getElementById('connectBtn').disabled = true;
            } else {
                showStatus('info', '✅ MetaMask detected! Click the button above to connect.');
                
                // Check if already connected
                checkExistingConnection();
            }
        });
        
        async function checkExistingConnection() {
            try {
                const accounts = await window.ethereum.request({ method: 'eth_accounts' });
                if (accounts.length > 0) {
                    const chainId = await window.ethereum.request({ method: 'eth_chainId' });
                    await handleConnection(accounts[0], chainId);
                }
            } catch (error) {
                console.error('Error checking existing connection:', error);
            }
        }
        
        async function connectWallet() {
            const btn = document.getElementById('connectBtn');
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner"></span>Connecting...';
            
            try {
                // Request account access
                const accounts = await window.ethereum.request({ 
                    method: 'eth_requestAccounts' 
                });
                
                if (accounts.length === 0) {
                    throw new Error('No accounts found');
                }
                
                const address = accounts[0];
                const chainId = await window.ethereum.request({ method: 'eth_chainId' });
                
                await handleConnection(address, chainId);
                
            } catch (error) {
                console.error('Connection error:', error);
                showStatus('error', '❌ Connection failed: ' + error.message);
                btn.disabled = false;
                btn.innerHTML = 'Connect MetaMask';
            }
        }
        
        async function handleConnection(address, chainId) {
            connectedAddress = address;
            connectedChainId = parseInt(chainId, 16);
            
            // Notify desktop app
            try {
                const response = await fetch(SERVER_URL + '/connect', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        address: address,
                        chainId: connectedChainId.toString()
                    })
                });
                
                if (!response.ok) {
                    throw new Error('Failed to notify desktop app');
                }
                
                // Update UI
                document.getElementById('walletAddress').textContent = address;
                document.getElementById('chainName').textContent = getChainName(connectedChainId);
                document.getElementById('walletInfo').classList.add('show');
                document.getElementById('connectBtn').style.display = 'none';
                document.getElementById('disconnectBtn').style.display = 'block';
                
                showStatus('success', `✅ Connected! You can now return to IReader.`);
                
                // Start polling for signature requests
                startPolling();
                
                // Listen for account/chain changes
                window.ethereum.on('accountsChanged', handleAccountsChanged);
                window.ethereum.on('chainChanged', handleChainChanged);
                
            } catch (error) {
                console.error('Error notifying desktop app:', error);
                showStatus('error', '❌ Failed to connect to IReader desktop app. Make sure the app is running.');
            }
        }
        
        async function disconnectWallet() {
            try {
                await fetch(SERVER_URL + '/disconnect', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' }
                });
            } catch (error) {
                console.error('Error notifying disconnect:', error);
            }
            
            // Stop polling
            stopPolling();
            
            connectedAddress = null;
            connectedChainId = null;
            
            document.getElementById('walletInfo').classList.remove('show');
            document.getElementById('connectBtn').style.display = 'block';
            document.getElementById('connectBtn').disabled = false;
            document.getElementById('connectBtn').innerHTML = 'Connect MetaMask';
            document.getElementById('disconnectBtn').style.display = 'none';
            
            showStatus('info', 'Disconnected. Click "Connect MetaMask" to reconnect.');
            
            // Remove listeners
            if (window.ethereum) {
                window.ethereum.removeListener('accountsChanged', handleAccountsChanged);
                window.ethereum.removeListener('chainChanged', handleChainChanged);
            }
        }
        
        function handleAccountsChanged(accounts) {
            if (accounts.length === 0) {
                disconnectWallet();
            } else if (accounts[0] !== connectedAddress) {
                window.location.reload();
            }
        }
        
        function handleChainChanged(chainId) {
            window.location.reload();
        }
        
        function getChainName(chainId) {
            const chains = {
                1: 'Ethereum Mainnet',
                5: 'Goerli Testnet',
                11155111: 'Sepolia Testnet',
                137: 'Polygon',
                80001: 'Mumbai Testnet',
                56: 'BSC',
                97: 'BSC Testnet'
            };
            return chains[chainId] || 'Chain ID: ' + chainId;
        }
        
        function showStatus(type, message) {
            const statusDiv = document.getElementById('status');
            statusDiv.className = 'status ' + type;
            statusDiv.textContent = message;
        }
        
        // Poll for signature requests from desktop app
        let pollingInterval = null;
        
        function startPolling() {
            if (pollingInterval) return;
            
            pollingInterval = setInterval(async () => {
                if (!connectedAddress) return;
                
                try {
                    const response = await fetch(SERVER_URL + '/request-signature');
                    const data = await response.json();
                    
                    if (data.pending && data.message) {
                        console.log('Signature request received:', data.message);
                        showStatus('info', '🔐 Signature request received. Please check MetaMask...');
                        await signMessage(data.message);
                    }
                } catch (error) {
                    console.error('Polling error:', error);
                }
            }, 1000); // Poll every second
        }
        
        function stopPolling() {
            if (pollingInterval) {
                clearInterval(pollingInterval);
                pollingInterval = null;
            }
        }
        
        // Start polling when connected
        window.addEventListener('load', () => {
            if (connectedAddress) {
                startPolling();
            }
        });
        
        // Sign message function
        async function signMessage(message) {
            if (!connectedAddress) {
                throw new Error('Wallet not connected');
            }
            
            try {
                showStatus('info', '🔐 Please sign the message in MetaMask...');
                
                const signature = await window.ethereum.request({
                    method: 'personal_sign',
                    params: [message, connectedAddress]
                });
                
                console.log('Signature created:', signature);
                
                // Send signature to desktop app
                await fetch(SERVER_URL + '/signature', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        signature: signature,
                        address: connectedAddress,
                        message: message
                    })
                });
                
                showStatus('success', '✅ Signature sent to IReader!');
                
                return signature;
            } catch (error) {
                console.error('Signing error:', error);
                showStatus('error', '❌ Signing failed: ' + error.message);
                throw error;
            }
        }
    </script>
</body>
</html>
        """.trimIndent()
    }
}
