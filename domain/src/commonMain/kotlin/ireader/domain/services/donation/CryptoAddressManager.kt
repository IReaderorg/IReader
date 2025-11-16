package ireader.domain.services.donation

import ireader.domain.models.donation.CryptoAddress

/**
 * Manager for cryptocurrency donation addresses
 * Provides access to wallet addresses for various cryptocurrencies
 */
interface CryptoAddressManager {
    /**
     * Get the wallet address for a specific cryptocurrency
     * 
     * @param currency The cryptocurrency symbol (e.g., "BTC", "ETH", "USDT")
     * @return CryptoAddress if the currency is supported, null otherwise
     */
    fun getAddress(currency: String): CryptoAddress?
    
    /**
     * Get all supported cryptocurrency addresses
     * 
     * @return List of all available crypto addresses
     */
    fun getAllAddresses(): List<CryptoAddress>
    
    /**
     * Check if a currency is supported
     * 
     * @param currency The cryptocurrency symbol to check
     * @return true if the currency is supported, false otherwise
     */
    fun isSupported(currency: String): Boolean
}

/**
 * Default implementation of CryptoAddressManager
 * Stores hardcoded donation addresses for major cryptocurrencies
 * 
 * In production, these addresses could be:
 * - Loaded from remote config (Firebase, Supabase)
 * - Fetched from a backend API
 * - Configured via environment variables
 */
class DefaultCryptoAddressManager : CryptoAddressManager {
    
    private val addresses = mapOf(
        "BTC" to CryptoAddress(
            currency = "BTC",
            address = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
            qrCodeData = "bitcoin:1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
            explorerUrl = CryptoAddress.generateExplorerUrl("BTC", "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa")
        ),
        "BITCOIN" to CryptoAddress(
            currency = "BTC",
            address = "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
            qrCodeData = "bitcoin:1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa",
            explorerUrl = CryptoAddress.generateExplorerUrl("BTC", "1A1zP1eP5QGefi2DMPTfTL5SLmv7DivfNa")
        ),
        "ETH" to CryptoAddress(
            currency = "ETH",
            address = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
            qrCodeData = "ethereum:0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
            explorerUrl = CryptoAddress.generateExplorerUrl("ETH", "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
        ),
        "ETHEREUM" to CryptoAddress(
            currency = "ETH",
            address = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
            qrCodeData = "ethereum:0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
            explorerUrl = CryptoAddress.generateExplorerUrl("ETH", "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
        ),
        "USDT" to CryptoAddress(
            currency = "USDT",
            address = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
            qrCodeData = "ethereum:0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
            explorerUrl = CryptoAddress.generateExplorerUrl("USDT", "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
        ),
        "LTC" to CryptoAddress(
            currency = "LTC",
            address = "LM2WMpR1Rp6j3Sa59cMXMs1SPGKhGGZ1Jz",
            qrCodeData = "litecoin:LM2WMpR1Rp6j3Sa59cMXMs1SPGKhGGZ1Jz",
            explorerUrl = CryptoAddress.generateExplorerUrl("LTC", "LM2WMpR1Rp6j3Sa59cMXMs1SPGKhGGZ1Jz")
        ),
        "LITECOIN" to CryptoAddress(
            currency = "LTC",
            address = "LM2WMpR1Rp6j3Sa59cMXMs1SPGKhGGZ1Jz",
            qrCodeData = "litecoin:LM2WMpR1Rp6j3Sa59cMXMs1SPGKhGGZ1Jz",
            explorerUrl = CryptoAddress.generateExplorerUrl("LTC", "LM2WMpR1Rp6j3Sa59cMXMs1SPGKhGGZ1Jz")
        ),
        "DOGE" to CryptoAddress(
            currency = "DOGE",
            address = "DH5yaieqoZN36fDVciNyRueRGvGLR3mr7L",
            qrCodeData = "dogecoin:DH5yaieqoZN36fDVciNyRueRGvGLR3mr7L",
            explorerUrl = CryptoAddress.generateExplorerUrl("DOGE", "DH5yaieqoZN36fDVciNyRueRGvGLR3mr7L")
        ),
        "DOGECOIN" to CryptoAddress(
            currency = "DOGE",
            address = "DH5yaieqoZN36fDVciNyRueRGvGLR3mr7L",
            qrCodeData = "dogecoin:DH5yaieqoZN36fDVciNyRueRGvGLR3mr7L",
            explorerUrl = CryptoAddress.generateExplorerUrl("DOGE", "DH5yaieqoZN36fDVciNyRueRGvGLR3mr7L")
        ),
        "BNB" to CryptoAddress(
            currency = "BNB",
            address = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
            qrCodeData = "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb",
            explorerUrl = CryptoAddress.generateExplorerUrl("BNB", "0x742d35Cc6634C0532925a3b844Bc9e7595f0bEb")
        )
    )
    
    override fun getAddress(currency: String): CryptoAddress? {
        return addresses[currency.uppercase()]
    }
    
    override fun getAllAddresses(): List<CryptoAddress> {
        // Return unique addresses (avoid duplicates from aliases)
        return addresses.values.distinctBy { it.address }
    }
    
    override fun isSupported(currency: String): Boolean {
        return addresses.containsKey(currency.uppercase())
    }
}
