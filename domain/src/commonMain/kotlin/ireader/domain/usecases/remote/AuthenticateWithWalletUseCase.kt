package ireader.domain.usecases.remote

import ireader.domain.data.repository.RemoteRepository
import ireader.domain.models.remote.User
import ireader.domain.services.WalletIntegrationManager

/**
 * Use case for authenticating a user with their Web3 wallet
 * 
 * This use case:
 * 1. Generates a unique challenge message with timestamp
 * 2. Requests the wallet to sign the message
 * 3. Authenticates with the backend using the signature
 * 
 * Requirements: 2.1, 2.2, 2.3, 2.4
 */
class AuthenticateWithWalletUseCase(
    private val remoteRepository: RemoteRepository,
    private val walletManager: WalletIntegrationManager
) {
    /**
     * Authenticate a user with their wallet
     * @param walletAddress The user's wallet address
     * @return Result containing the authenticated User or an error
     */
    suspend operator fun invoke(walletAddress: String): Result<User> {
        return try {
            println("🔷 AuthenticateWithWalletUseCase: Starting authentication for $walletAddress")
            
            // Generate challenge message with timestamp to prevent replay attacks
            val timestamp = System.currentTimeMillis()
            val message = "Sign this message to authenticate with IReader: $timestamp"
            
            println("🔷 Requesting signature from wallet manager...")
            // Request signature from wallet (platform-specific)
            val signature = walletManager.requestSignature(walletAddress, message)
            
            if (signature == null) {
                println("❌ Signature was null - user cancelled or error occurred")
                return Result.failure(Exception("Signature request cancelled by user"))
            }
            
            println("✅ Signature received: ${signature.take(20)}...")
            println("🔷 Authenticating with backend...")
            
            // Authenticate with backend
            val result = remoteRepository.authenticateWithWallet(walletAddress, signature, message)
            
            println("🔷 Backend authentication result: ${if (result.isSuccess) "SUCCESS" else "FAILURE"}")
            result
        } catch (e: Exception) {
            println("❌ Exception in AuthenticateWithWalletUseCase: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
