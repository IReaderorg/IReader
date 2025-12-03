package ireader.domain.models.donation

import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Represents a cryptocurrency donation transaction
 * 
 * @property id Unique identifier for this donation record
 * @property currency The cryptocurrency used (e.g., "BTC", "ETH")
 * @property amount The amount donated in the cryptocurrency
 * @property txHash The blockchain transaction hash (null if not yet confirmed)
 * @property timestamp Unix timestamp when the donation was recorded
 * @property goalId Optional ID of the funding goal this donation is for
 */
data class CryptoDonation(
    val id: String,
    val currency: String,
    val amount: Double,
    val txHash: String? = null,
    val timestamp: Long = currentTimeToLong(),
    val goalId: String? = null
) {
    /**
     * Check if this donation has been confirmed on the blockchain
     */
    val isConfirmed: Boolean
        get() = txHash != null && txHash.isNotBlank()
    
    /**
     * Generate blockchain explorer URL for this transaction
     */
    fun getTransactionUrl(): String? {
        if (txHash == null) return null
        
        return when (currency.uppercase()) {
            "BTC", "BITCOIN" -> "https://blockchain.com/btc/tx/$txHash"
            "ETH", "ETHEREUM" -> "https://etherscan.io/tx/$txHash"
            "USDT" -> "https://etherscan.io/tx/$txHash"
            "LTC", "LITECOIN" -> "https://blockchair.com/litecoin/transaction/$txHash"
            "DOGE", "DOGECOIN" -> "https://blockchair.com/dogecoin/transaction/$txHash"
            "BNB" -> "https://bscscan.com/tx/$txHash"
            "ADA", "CARDANO" -> "https://cardanoscan.io/transaction/$txHash"
            "XRP", "RIPPLE" -> "https://xrpscan.com/tx/$txHash"
            else -> null
        }
    }
}
