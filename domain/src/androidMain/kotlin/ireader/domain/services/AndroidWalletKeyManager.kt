package ireader.domain.services

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.crypto.Keys

/**
 * Android implementation of WalletKeyManager using EncryptedSharedPreferences
 * Securely stores the user's Ethereum private key
 */
class AndroidWalletKeyManager(private val context: Context) : WalletKeyManager {
    
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "wallet_keys",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    override suspend fun getOrCreateKeyPair(): Pair<String, String> = withContext(Dispatchers.IO) {
        // Check if we already have a key pair
        val existingPrivateKey = sharedPreferences.getString(KEY_PRIVATE_KEY, null)
        val existingAddress = sharedPreferences.getString(KEY_ADDRESS, null)
        
        if (existingPrivateKey != null && existingAddress != null) {
            println("🔑 Using existing wallet key pair")
            return@withContext Pair(existingAddress, existingPrivateKey)
        }
        
        // Generate new key pair
        println("🔑 Generating new Ethereum key pair...")
        val ecKeyPair = Keys.createEcKeyPair()
        val credentials = Credentials.create(ecKeyPair)
        
        val address = credentials.address
        val privateKey = credentials.ecKeyPair.privateKey.toString(16).padStart(64, '0')
        
        // Store securely
        sharedPreferences.edit()
            .putString(KEY_PRIVATE_KEY, privateKey)
            .putString(KEY_ADDRESS, "0x$address")
            .apply()
        
        println("✅ Generated new wallet: 0x$address")
        Pair("0x$address", privateKey)
    }
    
    override suspend fun getAddress(): String? = withContext(Dispatchers.IO) {
        sharedPreferences.getString(KEY_ADDRESS, null)
    }
    
    override suspend fun clearKeys() = withContext(Dispatchers.IO) {
        sharedPreferences.edit()
            .remove(KEY_PRIVATE_KEY)
            .remove(KEY_ADDRESS)
            .apply()
        println("🔑 Wallet keys cleared")
    }
    
    companion object {
        private const val KEY_PRIVATE_KEY = "eth_private_key"
        private const val KEY_ADDRESS = "eth_address"
    }
}
