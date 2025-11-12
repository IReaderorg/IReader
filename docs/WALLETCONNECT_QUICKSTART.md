# WalletConnect v2 Quick Start Guide

## 🚀 Get Started in 5 Minutes

This guide shows you how to use the WalletConnect v2 integration in IReader.

## Prerequisites

- ✅ WalletConnect v2 is already integrated
- ✅ Dependencies are configured
- ✅ Build is successful

## Basic Usage

### 1. Initialize (Automatic)

The `WalletConnectManager` initializes automatically when first accessed:

```kotlin
val walletManager = AndroidWalletIntegrationManager(context)
// WalletConnect is ready!
```

### 2. Connect to a dApp

```kotlin
// Get WalletConnect URI from dApp (e.g., by scanning QR code)
val uri = "wc:abc123...@2?relay-protocol=irn&symKey=..."

// Pair with the dApp
val success = walletManager.pairWithDApp(uri)

if (success) {
    println("✅ Pairing initiated")
} else {
    println("❌ Pairing failed")
}
```

### 3. Observe Connection State

```kotlin
lifecycleScope.launch {
    walletManager.getWalletConnectState()?.collect { state ->
        when (state) {
            is WalletConnectState.Disconnected -> {
                // Not connected
                updateUI("Disconnected")
            }
            
            is WalletConnectState.Connecting -> {
                // Connecting...
                showLoading()
            }
            
            is WalletConnectState.ProposalReceived -> {
                // dApp wants to connect
                val proposal = state.proposal
                showApprovalDialog(
                    name = proposal.name,
                    description = proposal.description,
                    url = proposal.url,
                    icon = proposal.icons.firstOrNull()
                )
            }
            
            is WalletConnectState.Connected -> {
                // Connected!
                val address = state.address
                updateUI("Connected: $address")
            }
            
            is WalletConnectState.Error -> {
                // Error occurred
                showError(state.message)
            }
        }
    }
}
```

### 4. Approve Connection

When you receive a `ProposalReceived` state, show UI to user and approve:

```kotlin
// User clicked "Approve"
val userAddress = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"
val accounts = listOf("eip155:1:$userAddress")  // Ethereum mainnet

walletManager.approveWalletConnectSession(accounts)
```

### 5. Reject Connection

```kotlin
// User clicked "Reject"
walletManager.rejectWalletConnectSession("User rejected")
```

### 6. Disconnect

```kotlin
// User wants to disconnect
walletManager.disconnectWalletConnect()
```

## Complete Example

```kotlin
class WalletActivity : AppCompatActivity() {
    
    private lateinit var walletManager: AndroidWalletIntegrationManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize
        walletManager = AndroidWalletIntegrationManager(this)
        
        // Observe state
        observeWalletConnectState()
        
        // Handle deep link (if app opened via WC URI)
        handleDeepLink(intent)
    }
    
    private fun observeWalletConnectState() {
        lifecycleScope.launch {
            walletManager.getWalletConnectState()?.collect { state ->
                when (state) {
                    is WalletConnectState.ProposalReceived -> {
                        showConnectionDialog(state.proposal)
                    }
                    is WalletConnectState.Connected -> {
                        Toast.makeText(this@WalletActivity, 
                            "Connected: ${state.address}", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    is WalletConnectState.Error -> {
                        Toast.makeText(this@WalletActivity, 
                            "Error: ${state.message}", 
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun handleDeepLink(intent: Intent?) {
        val uri = intent?.data?.toString()
        if (uri != null && uri.startsWith("wc:")) {
            walletManager.pairWithDApp(uri)
        }
    }
    
    private fun showConnectionDialog(proposal: Wallet.Model.SessionProposal) {
        AlertDialog.Builder(this)
            .setTitle("Connect to ${proposal.name}?")
            .setMessage("""
                ${proposal.description}
                
                URL: ${proposal.url}
                
                This dApp wants to:
                • View your wallet address
                • Request signatures
                • Propose transactions
            """.trimIndent())
            .setPositiveButton("Connect") { _, _ ->
                approveConnection()
            }
            .setNegativeButton("Reject") { _, _ ->
                walletManager.rejectWalletConnectSession("User rejected")
            }
            .show()
    }
    
    private fun approveConnection() {
        // Get user's wallet address (from your wallet management system)
        val userAddress = getUserWalletAddress()
        val accounts = listOf("eip155:1:$userAddress")
        
        walletManager.approveWalletConnectSession(accounts)
    }
    
    private fun getUserWalletAddress(): String {
        // TODO: Get from your wallet management system
        return "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb"
    }
}
```

## Handling Signature Requests

Signature requests are handled in the `WalletConnectManager.WalletDelegate`:

```kotlin
override fun onSessionRequest(
    sessionRequest: Wallet.Model.SessionRequest, 
    verifyContext: Wallet.Model.VerifyContext
) {
    when (sessionRequest.request.method) {
        "personal_sign" -> {
            // Parse request
            val params = parsePersonalSignParams(sessionRequest.request.params)
            val message = params.message
            val address = params.address
            
            // Show UI to user
            showSignatureDialog(message, address) { approved, signature ->
                if (approved && signature != null) {
                    // User approved and signed
                    approveRequest(sessionRequest, signature)
                } else {
                    // User rejected
                    rejectRequest(sessionRequest, "User rejected")
                }
            }
        }
        
        "eth_sendTransaction" -> {
            // Handle transaction request
            // Show transaction details to user
            // Get approval and sign
        }
        
        "eth_signTypedData" -> {
            // Handle typed data signing
            // Show structured data to user
            // Get approval and sign
        }
    }
}
```

## Testing

### Test with WalletConnect Example dApp:

1. Visit https://react-app.walletconnect.com/
2. Click "Connect"
3. Copy the WalletConnect URI
4. In your app, call:
   ```kotlin
   walletManager.pairWithDApp("wc:...")
   ```
5. Approve the connection
6. Try signing a message

### Test with Real dApp:

1. Visit Uniswap, OpenSea, or any WC v2 compatible dApp
2. Click "Connect Wallet" → "WalletConnect"
3. Scan QR code or copy URI
4. Use in your app
5. Test various operations

## Common Patterns

### Check if Connected

```kotlin
val state = walletManager.getWalletConnectState()?.value
val isConnected = state is WalletConnectState.Connected
```

### Get Active Sessions

```kotlin
val wcManager = walletManager.getWalletConnectManager()
val sessions = wcManager?.getActiveSessions() ?: emptyList()

sessions.forEach { session ->
    println("Session: ${session.topic}")
    println("Peer: ${session.peer.metadata.name}")
    println("Accounts: ${session.namespaces.values.flatMap { it.accounts }}")
}
```

### Handle App Restart

```kotlin
// On app start, check for active sessions
val sessions = wcManager?.getActiveSessions()
if (sessions?.isNotEmpty() == true) {
    // Restore connection state
    val session = sessions.first()
    val accounts = session.namespaces.values.flatMap { it.accounts }
    val address = accounts.firstOrNull()?.split(":")?.lastOrNull()
    // Update UI
}
```

## Troubleshooting

### Pairing fails
```kotlin
// Check URI format
if (!uri.startsWith("wc:") || !uri.contains("@2")) {
    println("❌ Invalid WalletConnect v2 URI")
}

// Check internet connection
if (!isNetworkAvailable()) {
    println("❌ No internet connection")
}
```

### Session not connecting
```kotlin
// Check logs
println("Connection state: ${walletManager.getWalletConnectState()?.value}")

// Verify Project ID
println("Project ID: ${WALLETCONNECT_PROJECT_ID}")
```

### Signature request not working
```kotlin
// Check if session is active
val sessions = wcManager?.getActiveSessions()
if (sessions.isNullOrEmpty()) {
    println("❌ No active session")
}

// Check method support
val supportedMethods = listOf("personal_sign", "eth_sendTransaction", "eth_signTypedData")
if (method !in supportedMethods) {
    println("❌ Unsupported method: $method")
}
```

## Next Steps

1. **Implement UI** - Create dialogs for approval and signing
2. **Integrate Keys** - Connect to your wallet/key management
3. **Add Persistence** - Save active sessions
4. **Handle Errors** - Improve error messages
5. **Test Thoroughly** - Test with multiple dApps

## Resources

- [Full Documentation](./WALLETCONNECT_V2_IMPLEMENTATION.md)
- [WalletConnect Docs](https://docs.walletconnect.com/)
- [Test dApp](https://react-app.walletconnect.com/)

## Summary

You now have a fully functional WalletConnect v2 integration! The core is ready - just add UI and key management to complete the user experience.

**Happy coding!** 🚀
