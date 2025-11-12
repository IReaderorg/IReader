package ireader.domain.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys
import java.io.File
import java.util.prefs.Preferences

/**
 * Desktop implementation of WalletKeyManager using Java Preferences API
 * Stores the user's Ethereum private key securely
 */
class DesktopWalletKeyManager : WalletKeyManager {
    
    private val prefs = Preferences.userNodeForPackage(DesktopWalletKeyManager::class.java)
    
    override suspend fun getOrCreateKeyPair(): Pair<String, String> = withContext(Dispatchers.IO) {
        // Check if we already have a key pair
        val existingPrivateKey = prefs.get(KEY_PRIVATE_KEY, null)
        val existingAddress = prefs.get(KEY_ADDRESS, null)
        
        if (existingPrivateKey != null && existingAddress != null) {
            println("🔑 Desktop: Using existing wallet key pair")
            
            // Normalize the address to handle legacy formats
            // Remove any duplicate 0x prefixes and ensure proper checksumming
            val cleanAddress = existingAddress.removePrefix("0x").removePrefix("0x")
            val normalizedAddress = Keys.toChecksumAddress(cleanAddress)
            
            // Update stored address if it was in old format
            if (normalizedAddress != existingAddress) {
                println("🔑 Desktop: Normalizing stored address format")
                prefs.put(KEY_ADDRESS, normalizedAddress)
                prefs.flush()
            }
            
            return@withContext Pair(normalizedAddress, existingPrivateKey)
        }
        
        // Generate new key pair
        println("🔑 Desktop: Generating new Ethereum key pair...")
        val ecKeyPair = Keys.createEcKeyPair()
        val credentials = Credentials.create(ecKeyPair)
        
        // Use checksummed address format
        val address = Keys.toChecksumAddress(credentials.address)
        val privateKey = credentials.ecKeyPair.privateKey.toString(16).padStart(64, '0')
        
        // Store in preferences (address already has 0x prefix from toChecksumAddress)
        prefs.put(KEY_PRIVATE_KEY, privateKey)
        prefs.put(KEY_ADDRESS, address)
        prefs.flush()
        
        println("✅ Desktop: Generated new wallet: $address")
        Pair(address, privateKey)
    }
    
    override suspend fun getAddress(): String? = withContext(Dispatchers.IO) {
        val storedAddress = prefs.get(KEY_ADDRESS, null) ?: return@withContext null
        
        // Normalize the address to handle legacy formats
        // Remove any duplicate 0x prefixes and ensure proper checksumming
        val cleanAddress = storedAddress.removePrefix("0x").removePrefix("0x")
        
        // Return checksummed address with single 0x prefix
        Keys.toChecksumAddress(cleanAddress)
    }
    
    override suspend fun clearKeys() = withContext(Dispatchers.IO) {
        prefs.remove(KEY_PRIVATE_KEY)
        prefs.remove(KEY_ADDRESS)
        prefs.flush()
        println("🔑 Desktop: Wallet keys cleared")
    }
    
    companion object {
        private const val KEY_PRIVATE_KEY = "eth_private_key"
        private const val KEY_ADDRESS = "eth_address"
    }
}
