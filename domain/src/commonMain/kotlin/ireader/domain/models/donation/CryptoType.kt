package ireader.domain.models.donation

/**
 * Enum representing supported cryptocurrency types
 */
enum class CryptoType(
    val displayName: String,
    val symbol: String,
    val uriScheme: String
) {
    BITCOIN(
        displayName = "Bitcoin",
        symbol = "BTC",
        uriScheme = "bitcoin"
    ),
    ETHEREUM(
        displayName = "Ethereum",
        symbol = "ETH",
        uriScheme = "ethereum"
    ),
    LITECOIN(
        displayName = "Litecoin",
        symbol = "LTC",
        uriScheme = "litecoin"
    ),
    DOGECOIN(
        displayName = "Dogecoin",
        symbol = "DOGE",
        uriScheme = "dogecoin"
    ),
}
