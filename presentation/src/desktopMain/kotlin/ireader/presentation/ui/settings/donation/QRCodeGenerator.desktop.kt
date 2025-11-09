package ireader.presentation.ui.settings.donation

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import org.jetbrains.skia.Bitmap
import org.jetbrains.skia.ColorAlphaType
import org.jetbrains.skia.ImageInfo
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Desktop implementation of QR code generator
 */
actual object QRCodeGenerator {
    actual fun generateQRCode(text: String, size: Int): ImageBitmap? {
        return try {
            val hints = mapOf(
                EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
                EncodeHintType.MARGIN to 1
            )
            
            val qrCodeWriter = QRCodeWriter()
            val bitMatrix = qrCodeWriter.encode(
                text,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            )
            
            // Convert BitMatrix to Skia Bitmap
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)
            
            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) {
                        0xFF000000.toInt() // Black
                    } else {
                        0xFFFFFFFF.toInt() // White
                    }
                }
            }
            
            // Create Skia bitmap
            val imageInfo = ImageInfo.makeN32(width, height, ColorAlphaType.PREMUL)
            val bitmap = Bitmap()
            bitmap.allocPixels(imageInfo)
            
            // Convert IntArray to ByteBuffer for Skia
            val buffer = ByteBuffer.allocate(pixels.size * 4).order(ByteOrder.nativeOrder())
            for (pixel in pixels) {
                buffer.putInt(pixel)
            }
            buffer.rewind()
            
            bitmap.installPixels(buffer.array())
            
            org.jetbrains.skia.Image.makeFromBitmap(bitmap).asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
