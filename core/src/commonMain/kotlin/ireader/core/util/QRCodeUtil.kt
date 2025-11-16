package ireader.core.util

/**
 * Utility object for QR code generation constants and helpers
 * 
 * Note: Actual QR code generation is implemented in the presentation layer
 * using platform-specific implementations with ZXing library.
 * See: presentation/ui/settings/donation/QRCodeGenerator.kt
 */
object QRCodeUtil {
    /**
     * Standard QR code sizes
     */
    object Size {
        const val SMALL = 200
        const val MEDIUM = 400
        const val LARGE = 600
        const val DEFAULT = MEDIUM
    }
    
    /**
     * Generate a payment URI for cryptocurrency wallets
     * 
     * @param scheme The URI scheme (e.g., "bitcoin", "ethereum")
     * @param address The wallet address
     * @param amount Optional amount to pre-fill
     * @param label Optional label for the payment
     * @return Payment URI string
     */
    fun generatePaymentUri(
        scheme: String,
        address: String,
        amount: String? = null,
        label: String? = null
    ): String {
        val uri = StringBuilder("$scheme:$address")
        
        val params = mutableListOf<String>()
        amount?.let { params.add("amount=$it") }
        label?.let { params.add("label=${it.replace(" ", "%20")}") }
        
        if (params.isNotEmpty()) {
            uri.append("?${params.joinToString("&")}")
        }
        
        return uri.toString()
    }
    
    /**
     * Validate if a string is a valid QR code input
     * 
     * @param text The text to validate
     * @return true if valid, false otherwise
     */
    fun isValidQRInput(text: String): Boolean {
        // QR codes can encode up to 4,296 alphanumeric characters
        // or 2,953 bytes in binary mode
        return text.isNotBlank() && text.length <= 2953
    }
}
