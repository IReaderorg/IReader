package ireader.domain.models.donation

/**
 * Enum representing supported cryptocurrency wallet applications
 * Currently supports MetaMask and Trust Wallet only
 */
enum class WalletApp(
    val displayName: String,
    val packageName: String
) {
    METAMASK(
        displayName = "MetaMask",
        packageName = "io.metamask"
    ),
    TRUST_WALLET(
        displayName = "Trust Wallet",
        packageName = "com.wallet.crypto.trustapp"
    )
}
