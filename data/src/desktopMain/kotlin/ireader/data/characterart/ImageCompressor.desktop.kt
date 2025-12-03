package ireader.data.characterart

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam

actual class ImageCompressor {
    
    actual suspend fun compress(
        imageBytes: ByteArray,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int,
        maxSizeKb: Int
    ): ByteArray = withContext(Dispatchers.IO) {
        // Read image
        val inputStream = ByteArrayInputStream(imageBytes)
        val originalImage = ImageIO.read(inputStream) ?: return@withContext imageBytes
        
        // Calculate new dimensions
        val (newWidth, newHeight) = calculateDimensions(
            originalImage.width,
            originalImage.height,
            maxWidth,
            maxHeight
        )
        
        // Resize if needed
        val resizedImage = if (newWidth != originalImage.width || newHeight != originalImage.height) {
            val scaledImage = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)
            val bufferedImage = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
            val graphics = bufferedImage.createGraphics()
            graphics.drawImage(scaledImage, 0, 0, null)
            graphics.dispose()
            bufferedImage
        } else {
            // Convert to RGB if needed (removes alpha channel for JPEG)
            if (originalImage.type != BufferedImage.TYPE_INT_RGB) {
                val rgbImage = BufferedImage(originalImage.width, originalImage.height, BufferedImage.TYPE_INT_RGB)
                val graphics = rgbImage.createGraphics()
                graphics.drawImage(originalImage, 0, 0, null)
                graphics.dispose()
                rgbImage
            } else {
                originalImage
            }
        }
        
        // Compress with quality adjustment
        var currentQuality = quality / 100f
        var result: ByteArray
        
        do {
            result = compressToJpeg(resizedImage, currentQuality)
            
            if (result.size <= maxSizeKb * 1024 || currentQuality <= 0.2f) {
                break
            }
            
            currentQuality -= 0.1f
        } while (currentQuality > 0.2f)
        
        result
    }
    
    actual fun getImageDimensions(imageBytes: ByteArray): Pair<Int, Int> {
        val inputStream = ByteArrayInputStream(imageBytes)
        val image = ImageIO.read(inputStream) ?: return Pair(0, 0)
        return Pair(image.width, image.height)
    }
    
    private fun calculateDimensions(
        width: Int,
        height: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Pair<Int, Int> {
        if (width <= maxWidth && height <= maxHeight) {
            return Pair(width, height)
        }
        
        val scale = minOf(
            maxWidth.toFloat() / width,
            maxHeight.toFloat() / height
        )
        
        return Pair(
            (width * scale).toInt(),
            (height * scale).toInt()
        )
    }
    
    private fun compressToJpeg(image: BufferedImage, quality: Float): ByteArray {
        val outputStream = ByteArrayOutputStream()
        
        val writers = ImageIO.getImageWritersByFormatName("jpeg")
        if (!writers.hasNext()) {
            // Fallback to simple write
            ImageIO.write(image, "jpeg", outputStream)
            return outputStream.toByteArray()
        }
        
        val writer = writers.next()
        val imageOutputStream = ImageIO.createImageOutputStream(outputStream)
        writer.output = imageOutputStream
        
        val param = writer.defaultWriteParam.apply {
            compressionMode = ImageWriteParam.MODE_EXPLICIT
            compressionQuality = quality
        }
        
        writer.write(null, IIOImage(image, null, null), param)
        writer.dispose()
        imageOutputStream.close()
        
        return outputStream.toByteArray()
    }
}
