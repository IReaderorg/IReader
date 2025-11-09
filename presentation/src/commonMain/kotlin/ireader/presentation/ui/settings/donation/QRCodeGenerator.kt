package ireader.presentation.ui.settings.donation

import androidx.compose.ui.graphics.ImageBitmap

/**
 * Utility object for generating QR codes from text/addresses
 * Platform-specific implementations are provided in androidMain and desktopMain
 */
expect object QRCodeGenerator {
    /**
     * Generates a QR code bitmap from the given text
     * 
     * @param text The text to encode in the QR code (e.g., wallet address)
     * @param size The size of the QR code in pixels (default: 512)
     * @return ImageBitmap containing the QR code, or null if generation fails
     */
    fun generateQRCode(text: String, size: Int = 512): ImageBitmap?
}
