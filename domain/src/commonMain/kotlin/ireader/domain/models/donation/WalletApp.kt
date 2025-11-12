package ireader.domain.models.donation

/**
 * Enum representing supported cryptocurrency wallet applications
 */
enum class WalletApp(
    val displayName: String,
    val packageName: String
) {
    TRUST_WALLET(
        displayName = "Trust Wallet",
        packageName = "com.wallet.crypto.trustapp"
    ),
    METAMASK(
        displayName = "MetaMask",
        packageName = "io.metamask"
    ),
    COINBASE_WALLET(
        displayName = "Coinbase Wallet",
        packageName = "org.toshi"
    ),
    RAINBOW(
        displayName = "Rainbow",
        packageName = "me.rainbow.wallet"
    ),
    ARGENT(
        displayName = "Argent",
        packageName = "com.wallet.argent"
    ),
    COINBASE(
        displayName = "Coinbase",
        packageName = "org.toshi"
    )
}
