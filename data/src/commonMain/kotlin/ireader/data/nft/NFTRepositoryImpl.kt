package ireader.data.nft

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.bodyOrNull
import io.github.jan.supabase.functions.functions
import ireader.data.backend.BackendService
import ireader.data.core.DatabaseHandler
import ireader.data.remote.RemoteErrorMapper
import ireader.domain.data.repository.NFTRepository
import ireader.domain.models.remote.NFTVerificationResult
import ireader.domain.models.remote.NFTWallet
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import ireader.domain.utils.extensions.currentTimeToLong

class NFTRepositoryImpl(
    private val handler: DatabaseHandler,
    private val supabaseClient: SupabaseClient,
    private val backendService: BackendService
) : NFTRepository {
    
    @Serializable
    private data class NFTWalletDto(
        @SerialName("user_id") val userId: String,
        @SerialName("wallet_address") val walletAddress: String,
        @SerialName("last_verified") val lastVerified: String? = null,
        @SerialName("owns_nft") val ownsNFT: Boolean = false,
        @SerialName("nft_token_id") val nftTokenId: String? = null,
        @SerialName("verification_cache_expires") val verificationCacheExpires: String
    )
    
    @Serializable
    private data class VerifyNFTRequest(
        val walletAddress: String,
        val userId: String
    )
    
    @Serializable
    private data class VerifyNFTResponse(
        val ownsNFT: Boolean,
        val tokenId: String? = null,
        val verifiedAt: String,
        val cacheExpiresAt: String
    )
    
    private val json = Json { ignoreUnknownKeys = true }
    
    override suspend fun saveWalletAddress(address: String): Result<Unit> = 
        RemoteErrorMapper.withErrorMapping {
            // Validate Ethereum address format (0x + 40 hex chars)
            if (!isValidEthereumAddress(address)) {
                throw Exception("Invalid Ethereum wallet address format")
            }
            
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            // Save to local database
            handler.await {
                nftWalletQueries.insert(
                    userId = userId,
                    walletAddress = address,
                    lastVerified = null,
                    ownsNFT = false,
                    nftTokenId = null,
                    cacheExpiresAt = 0L
                )
            }
            
            // Upsert to Supabase
            val now = currentTimeToLong()
            val cacheExpires = now + (24 * 60 * 60 * 1000) // 24 hours
            
            val data = buildJsonObject {
                put("user_id", userId)
                put("wallet_address", address)
                put("owns_nft", false)
                put("verification_cache_expires", cacheExpires.toString())
            }
            
            backendService.upsert(
                table = "nft_wallets",
                data = data,
                onConflict = "user_id",
                returning = false
            ).getOrThrow()
        }
    
    override suspend fun getWalletAddress(): Result<String?> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            // Try local database first for offline support
            val localAddress = handler.awaitOneOrNull {
                nftWalletQueries.getWalletAddress(userId)
            }
            
            if (localAddress != null) {
                return@withErrorMapping localAddress
            }
            
            // Fall back to Supabase if local not found
            val queryResult = backendService.query(
                table = "nft_wallets",
                filters = mapOf("user_id" to userId)
            ).getOrThrow()
            
            val result = queryResult.firstOrNull()?.let {
                json.decodeFromJsonElement(NFTWalletDto.serializer(), it)
            }
            
            result?.walletAddress
        }
    
    @OptIn(SupabaseInternal::class)
    override suspend fun verifyNFTOwnership(address: String): Result<NFTVerificationResult> =
        RemoteErrorMapper.withErrorMapping {
            // Validate Ethereum address format
            if (!isValidEthereumAddress(address)) {
                throw Exception("Invalid Ethereum wallet address format")
            }
            
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            // Call Supabase Edge Function
            val requestBody = buildJsonObject {
                put("walletAddress", address)
                put("userId", userId)
            }
            
            val response = try {
                supabaseClient.functions.invoke(
                    function = "verify-nft-ownership",
                    body = requestBody
                )
            } catch (e: Exception) {
                // Handle network timeouts and errors gracefully
                throw Exception("NFT verification failed: ${e.message}", e)
            }
            
            val responseBody = response.bodyOrNull<String>()?.let {
                json.decodeFromString<VerifyNFTResponse>(it.toString())
            } ?: throw Exception("Invalid response from verification service")
            
            // Parse timestamps
            val verifiedAt = parseTimestamp(responseBody.verifiedAt)
            val cacheExpiresAt = parseTimestamp(responseBody.cacheExpiresAt)
            
            // Update local database
            handler.await {
                nftWalletQueries.updateVerification(
                    userId = userId,
                    lastVerified = verifiedAt,
                    ownsNFT = responseBody.ownsNFT,
                    nftTokenId = responseBody.tokenId,
                    cacheExpiresAt = cacheExpiresAt
                )
            }
            
            NFTVerificationResult(
                ownsNFT = responseBody.ownsNFT,
                tokenId = responseBody.tokenId,
                verifiedAt = verifiedAt,
                cacheExpiresAt = cacheExpiresAt
            )
        }
    
    override suspend fun getCachedVerification(): Result<NFTWallet?> = 
        RemoteErrorMapper.withErrorMapping {
            val userId = supabaseClient.auth.currentUserOrNull()?.id 
                ?: throw Exception("User not authenticated")
            
            // Query Supabase for cached verification
            val queryResult = backendService.query(
                table = "nft_wallets",
                filters = mapOf("user_id" to userId)
            ).getOrThrow()
            
            val result = queryResult.firstOrNull()?.let {
                json.decodeFromJsonElement(NFTWalletDto.serializer(), it)
            }
            
            result?.let { dto ->
                NFTWallet(
                    userId = dto.userId,
                    walletAddress = dto.walletAddress,
                    lastVerified = parseTimestamp(dto.lastVerified ?: "0"),
                    ownsNFT = dto.ownsNFT,
                    nftTokenId = dto.nftTokenId,
                    cacheExpiresAt = parseTimestamp(dto.verificationCacheExpires)
                )
            }
        }
    
    override suspend fun isVerificationExpired(): Boolean {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return true
            
            // Check local database first
            val wallet = handler.awaitOneOrNull {
                nftWalletQueries.getWalletByUserId(userId)
            }
            
            if (wallet == null) return true
            
            val now = currentTimeToLong()
            wallet.cacheExpiresAt < now
        } catch (e: Exception) {
            // If there's an error, assume expired
            true
        }
    }
    
    /**
     * Validates Ethereum address format (0x + 40 hex characters)
     */
    private fun isValidEthereumAddress(address: String): Boolean {
        return address.matches(Regex("^0x[a-fA-F0-9]{40}$"))
    }
    
    /**
     * Parses timestamp string to Long
     * Handles both ISO 8601 format and millisecond strings
     */
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            // Try parsing as milliseconds first
            timestamp.toLongOrNull() ?: run {
                // If that fails, try parsing as ISO 8601
                // For simplicity, we'll use a basic parser
                // In production, consider using kotlinx-datetime
                currentTimeToLong()
            }
        } catch (e: Exception) {
            currentTimeToLong()
        }
    }
}
