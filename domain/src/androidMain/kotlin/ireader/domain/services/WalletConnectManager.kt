package ireader.domain.services

import android.app.Application
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * WalletConnect v2 Manager for handling wallet connections and signature requests
 * Uses the official WalletConnect Kotlin v2 SDK
 */
class WalletConnectManager(
    private val application: Application,
    private val projectId: String
) {
    private val _connectionState = MutableStateFlow<WalletConnectState>(WalletConnectState.Disconnected)
    val connectionState: StateFlow<WalletConnectState> = _connectionState.asStateFlow()
    
    private val _sessionProposal = MutableStateFlow<Wallet.Model.SessionProposal?>(null)
    val sessionProposal: StateFlow<Wallet.Model.SessionProposal?> = _sessionProposal.asStateFlow()
    
    private var currentSession: Wallet.Model.Session? = null
    
    init {
        initializeWalletConnect()
    }
    
    private fun initializeWalletConnect() {
        try {
            // Initialize Core Client
            val serverUrl = "wss://relay.walletconnect.com?projectId=$projectId"
            val connectionType = ConnectionType.AUTOMATIC
            val appMetaData = Core.Model.AppMetaData(
                name = "IReader",
                description = "Web3-enabled reading app with cross-device sync",
                url = "https://ireader.org",
                icons = listOf("https://ireader.org/icon.png"),
                redirect = "ireader://wc"
            )
            
            CoreClient.initialize(
                relayServerUrl = serverUrl,
                connectionType = connectionType,
                application = application,
                metaData = appMetaData,
                onError = { error ->
                    println("❌ CoreClient initialization error: ${error.throwable.message}")
                    _connectionState.value = WalletConnectState.Error(error.throwable.message ?: "Core initialization failed")
                }
            )
            
            // Initialize Web3Wallet
            val initParams = Wallet.Params.Init(core = CoreClient)
            Web3Wallet.initialize(initParams) { error ->
                println("❌ WalletConnect initialization error: ${error.throwable.message}")
                _connectionState.value = WalletConnectState.Error(error.throwable.message ?: "Initialization failed")
            }
            
            // Set up wallet delegate
            setupWalletDelegate()
            
            println("✅ WalletConnect v2 initialized successfully")
        } catch (e: Exception) {
            println("❌ Failed to initialize WalletConnect: ${e.message}")
            e.printStackTrace()
            _connectionState.value = WalletConnectState.Error(e.message ?: "Unknown error")
        }
    }
    
    private fun setupWalletDelegate() {
        val walletDelegate = object : Web3Wallet.WalletDelegate {
            override fun onSessionProposal(sessionProposal: Wallet.Model.SessionProposal, verifyContext: Wallet.Model.VerifyContext) {
                println("📱 Session proposal received from: ${sessionProposal.name}")
                println("📱 Description: ${sessionProposal.description}")
                println("📱 URL: ${sessionProposal.url}")
                
                _sessionProposal.value = sessionProposal
                _connectionState.value = WalletConnectState.ProposalReceived(sessionProposal)
            }
            
            override fun onSessionRequest(sessionRequest: Wallet.Model.SessionRequest, verifyContext: Wallet.Model.VerifyContext) {
                println("📱 Session request received: ${sessionRequest.request.method}")
                println("📱 Chain ID: ${sessionRequest.chainId}")
                
                when (sessionRequest.request.method) {
                    "personal_sign" -> handlePersonalSign(sessionRequest)
                    "eth_sendTransaction" -> handleSendTransaction(sessionRequest)
                    "eth_signTypedData" -> handleSignTypedData(sessionRequest)
                    else -> {
                        println("⚠️ Unsupported method: ${sessionRequest.request.method}")
                        rejectRequest(sessionRequest, "Unsupported method")
                    }
                }
            }
            
            override fun onAuthRequest(authRequest: Wallet.Model.AuthRequest, verifyContext: Wallet.Model.VerifyContext) {
                println("📱 Auth request received")
                // Handle authentication requests if needed
            }
            
            override fun onSessionDelete(sessionDelete: Wallet.Model.SessionDelete) {
                println("📱 Session deleted")
                currentSession = null
                _connectionState.value = WalletConnectState.Disconnected
            }
            
            override fun onSessionSettleResponse(settleSessionResponse: Wallet.Model.SettledSessionResponse) {
                println("📱 Session settled")
                when (settleSessionResponse) {
                    is Wallet.Model.SettledSessionResponse.Result -> {
                        currentSession = settleSessionResponse.session
                        val accounts = settleSessionResponse.session.namespaces.values
                            .flatMap { it.accounts }
                        val address = accounts.firstOrNull()?.split(":")?.lastOrNull() ?: ""
                        _connectionState.value = WalletConnectState.Connected(address)
                        println("✅ Connected to wallet: $address")
                    }
                    is Wallet.Model.SettledSessionResponse.Error -> {
                        println("❌ Session settlement error: ${settleSessionResponse.errorMessage}")
                        _connectionState.value = WalletConnectState.Error(settleSessionResponse.errorMessage)
                    }
                }
            }
            
            override fun onSessionExtend(session: Wallet.Model.Session) {
                println("📱 Session extended")
                currentSession = session
            }
            
            override fun onSessionUpdateResponse(sessionUpdateResponse: Wallet.Model.SessionUpdateResponse) {
                println("📱 Session updated")
            }
            
            override fun onProposalExpired(proposal: Wallet.Model.ExpiredProposal) {
                println("⚠️ Session proposal expired")
                _sessionProposal.value = null
                _connectionState.value = WalletConnectState.Disconnected
            }
            
            override fun onRequestExpired(request: Wallet.Model.ExpiredRequest) {
                println("⚠️ Session request expired")
            }
            
            override fun onConnectionStateChange(state: Wallet.Model.ConnectionState) {
                println("📱 Connection state changed: ${state.isAvailable}")
            }
            
            override fun onError(error: Wallet.Model.Error) {
                println("❌ WalletConnect error: ${error.throwable.message}")
                _connectionState.value = WalletConnectState.Error(error.throwable.message ?: "Unknown error")
            }
        }
        
        Web3Wallet.setWalletDelegate(walletDelegate)
    }
    
    /**
     * Generate a pairing URI for connecting to a dApp
     * This URI should be displayed as a QR code or used in a deep link
     */
    fun pair(uri: String): Boolean {
        return try {
            val pairParams = Wallet.Params.Pair(uri)
            Web3Wallet.pair(pairParams) { error ->
                println("❌ Pairing error: ${error.throwable.message}")
                _connectionState.value = WalletConnectState.Error(error.throwable.message ?: "Pairing failed")
            }
            _connectionState.value = WalletConnectState.Connecting
            true
        } catch (e: Exception) {
            println("❌ Failed to pair: ${e.message}")
            _connectionState.value = WalletConnectState.Error(e.message ?: "Pairing failed")
            false
        }
    }
    
    /**
     * Approve a session proposal
     * @param accounts List of accounts to share with the dApp (format: "eip155:1:0x...")
     */
    fun approveSession(accounts: List<String>) {
        val proposal = _sessionProposal.value ?: run {
            println("❌ No session proposal to approve")
            return
        }
        
        try {
            // Build namespaces from the proposal
            val namespaces = proposal.requiredNamespaces.mapValues { (key, namespace) ->
                Wallet.Model.Namespace.Session(
                    chains = namespace.chains ?: emptyList(),
                    accounts = accounts,
                    methods = namespace.methods,
                    events = namespace.events
                )
            }
            
            val approveParams = Wallet.Params.SessionApprove(
                proposerPublicKey = proposal.proposerPublicKey,
                namespaces = namespaces
            )
            
            Web3Wallet.approveSession(approveParams) { error ->
                println("❌ Approve session error: ${error.throwable.message}")
                _connectionState.value = WalletConnectState.Error(error.throwable.message ?: "Approval failed")
            }
            
            println("✅ Session approved")
        } catch (e: Exception) {
            println("❌ Failed to approve session: ${e.message}")
            _connectionState.value = WalletConnectState.Error(e.message ?: "Approval failed")
        }
    }
    
    /**
     * Reject a session proposal
     */
    fun rejectSession(reason: String = "User rejected") {
        val proposal = _sessionProposal.value ?: run {
            println("❌ No session proposal to reject")
            return
        }
        
        try {
            val rejectParams = Wallet.Params.SessionReject(
                proposerPublicKey = proposal.proposerPublicKey,
                reason = reason
            )
            
            Web3Wallet.rejectSession(rejectParams) { error ->
                println("❌ Reject session error: ${error.throwable.message}")
            }
            
            _sessionProposal.value = null
            _connectionState.value = WalletConnectState.Disconnected
            println("✅ Session rejected")
        } catch (e: Exception) {
            println("❌ Failed to reject session: ${e.message}")
        }
    }
    
    /**
     * Handle personal_sign request
     */
    private fun handlePersonalSign(request: Wallet.Model.SessionRequest) {
        try {
            // In a real implementation, this would:
            // 1. Parse the message from request.request.params
            // 2. Show UI to user for approval
            // 3. Sign the message with the user's private key
            // 4. Return the signature
            
            // For now, we'll reject it as it requires user interaction
            println("⚠️ personal_sign requires user interaction - rejecting")
            rejectRequest(request, "User interaction required")
        } catch (e: Exception) {
            println("❌ Failed to handle personal_sign: ${e.message}")
            rejectRequest(request, "Failed to process request")
        }
    }
    
    /**
     * Handle eth_sendTransaction request
     */
    private fun handleSendTransaction(request: Wallet.Model.SessionRequest) {
        println("⚠️ eth_sendTransaction requires user interaction - rejecting")
        rejectRequest(request, "User interaction required")
    }
    
    /**
     * Handle eth_signTypedData request
     */
    private fun handleSignTypedData(request: Wallet.Model.SessionRequest) {
        println("⚠️ eth_signTypedData requires user interaction - rejecting")
        rejectRequest(request, "User interaction required")
    }
    
    /**
     * Approve a session request with a result
     */
    fun approveRequest(request: Wallet.Model.SessionRequest, result: String) {
        try {
            val response = Wallet.Params.SessionRequestResponse(
                sessionTopic = request.topic,
                jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcResult(
                    id = request.request.id,
                    result = result
                )
            )
            
            Web3Wallet.respondSessionRequest(response) { error ->
                println("❌ Response error: ${error.throwable.message}")
            }
            
            println("✅ Request approved with result")
        } catch (e: Exception) {
            println("❌ Failed to approve request: ${e.message}")
        }
    }
    
    /**
     * Reject a session request
     */
    private fun rejectRequest(request: Wallet.Model.SessionRequest, reason: String) {
        try {
            val response = Wallet.Params.SessionRequestResponse(
                sessionTopic = request.topic,
                jsonRpcResponse = Wallet.Model.JsonRpcResponse.JsonRpcError(
                    id = request.request.id,
                    code = 5000,
                    message = reason
                )
            )
            
            Web3Wallet.respondSessionRequest(response) { error ->
                println("❌ Response error: ${error.throwable.message}")
            }
            
            println("✅ Request rejected: $reason")
        } catch (e: Exception) {
            println("❌ Failed to reject request: ${e.message}")
        }
    }
    
    /**
     * Disconnect the current session
     */
    fun disconnect() {
        currentSession?.let { session ->
            try {
                val disconnectParams = Wallet.Params.SessionDisconnect(session.topic)
                Web3Wallet.disconnectSession(disconnectParams) { error ->
                    println("❌ Disconnect error: ${error.throwable.message}")
                }
            } catch (e: Exception) {
                println("❌ Failed to disconnect: ${e.message}")
            }
        }
        currentSession = null
        _connectionState.value = WalletConnectState.Disconnected
    }
    
    /**
     * Get all active sessions
     */
    fun getActiveSessions(): List<Wallet.Model.Session> {
        return try {
            Web3Wallet.getListOfActiveSessions()
        } catch (e: Exception) {
            println("❌ Failed to get active sessions: ${e.message}")
            emptyList()
        }
    }
}

/**
 * Sealed class representing WalletConnect connection states
 */
sealed class WalletConnectState {
    object Disconnected : WalletConnectState()
    object Connecting : WalletConnectState()
    data class ProposalReceived(val proposal: Wallet.Model.SessionProposal) : WalletConnectState()
    data class Connected(val address: String) : WalletConnectState()
    data class Error(val message: String) : WalletConnectState()
}
