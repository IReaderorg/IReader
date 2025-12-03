package ireader.presentation.ui.settings.donation

import androidx.compose.ui.graphics.ImageBitmap

/**
 * iOS implementation of QR code generator
 * Uses CoreImage CIFilter for QR code generation
 */
actual object QRCodeGenerator {
    actual fun generateQRCode(text: String, size: Int): ImageBitmap? {
        // iOS QR code generation would use CIFilter with CIQRCodeGenerator
        // For now, return null as this requires CoreImage integration
        return null
    }
}
