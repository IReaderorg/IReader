package ireader.presentation.ui.settings.donation

/**
 * Configuration for cryptocurrency donation addresses.
 * 
 * These addresses should be updated with actual wallet addresses
 * before deployment. For security, consider moving these to
 * remote config for easier updates without app releases.
 */
object DonationConfig {
    /**
     * Bitcoin wallet address for donations
     */
    const val BITCOIN_ADDRESS = "bc1qlh484m8e0ff4pewvuyu0xg7tc7zzynzkj6ufcx"
    
    /**
     * Ethereum wallet address for donations
     */
    const val ETHEREUM_ADDRESS = "0x86dA13b11011B7Bdff2259B576AD5c1c9E94d3Ef"
    
    /**
     * Litecoin wallet address for donations
     */
    const val LITECOIN_ADDRESS = "ltc1qkt4ajre6rkst2r3pfxqjyn8wzw3mf99psx9e8q"
    
    /**
     * Disclaimer text shown to users
     */
    const val CRYPTO_DISCLAIMER = """
        Cryptocurrency donations are non-refundable. 
        Please verify the wallet address before sending.
        IReader does not store or have access to your private keys.
    """
}
