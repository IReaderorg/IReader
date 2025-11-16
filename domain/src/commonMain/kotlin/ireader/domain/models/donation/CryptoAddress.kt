package ireader.domain.models.donation

/**
 * Represents a cryptocurrency wallet address for donations
 * 
 * @property currency The cryptocurrency symbol (e.g., "BTC", "ETH", "USDT")
 * @property address The wallet address string
 * @property qrCodeData The data to encode in QR code (usually the address or payment URI)
 * @property explorerUrl The blockchain explorer URL for this address
 */
data class CryptoAddress(
    val currency: String,
    val address: String,
    val qrCodeData: String = address,
    val explorerUrl: String
) {
    companion object {
        /**
         * Generate blockchain explorer URL for a given currency and address
         */
        fun generateExplorerUrl(currency: String, address: String): String {
            return when (currency.uppercase()) {
                "BTC", "BITCOIN" -> "https://blockchain.com/btc/address/$address"
                "ETH", "ETHEREUM" -> "https://etherscan.io/address/$address"
                "USDT" -> "https://etherscan.io/token/0xdac17f958d2ee523a2206206994597c13d831ec7?a=$address"
                "LTC", "LITECOIN" -> "https://blockchair.com/litecoin/address/$address"
                "DOGE", "DOGECOIN" -> "https://blockchair.com/dogecoin/address/$address"
                "BNB" -> "https://bscscan.com/address/$address"
                "ADA", "CARDANO" -> "https://cardanoscan.io/address/$address"
                "XRP", "RIPPLE" -> "https://xrpscan.com/account/$address"
                else -> ""
            }
        }
    }
}
