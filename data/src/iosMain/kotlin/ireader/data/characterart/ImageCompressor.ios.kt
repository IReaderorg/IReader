package ireader.data.characterart

import platform.UIKit.*
import platform.Foundation.*
import platform.CoreGraphics.*
import kotlinx.cinterop.*
import kotlinx.cinterop.refTo
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.addressOf

/**
 * iOS implementation of ImageCompressor
 * Uses UIImage and Core Graphics for image manipulation
 */
@OptIn(ExperimentalForeignApi::class)
actual class ImageCompressor {
    
    /**
     * Compress an image to fit within size constraints
     */
    actual suspend fun compress(
        imageBytes: ByteArray,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int,
        maxSizeKb: Int
    ): ByteArray {
        // Convert bytes to NSData
        val nsData = imageBytes.toNSData() ?: return imageBytes
        
        // Create UIImage from data
        val image = UIImage.imageWithData(nsData) ?: return imageBytes
        
        // Calculate new size maintaining aspect ratio
        val originalWidth = image.size.useContents { width }
        val originalHeight = image.size.useContents { height }
        
        var newWidth = originalWidth
        var newHeight = originalHeight
        
        // Scale down if needed
        if (originalWidth > maxWidth || originalHeight > maxHeight) {
            val widthRatio = maxWidth.toDouble() / originalWidth
            val heightRatio = maxHeight.toDouble() / originalHeight
            val ratio = minOf(widthRatio, heightRatio)
            
            newWidth = (originalWidth * ratio)
            newHeight = (originalHeight * ratio)
        }
        
        // Resize image
        val resizedImage = resizeImage(image, newWidth, newHeight) ?: return imageBytes
        
        // Compress with quality
        var compressionQuality = quality / 100.0
        var compressedData = UIImageJPEGRepresentation(resizedImage, compressionQuality)
        
        // Reduce quality until size is acceptable
        val maxSizeBytes = maxSizeKb * 1024
        while (compressedData != null && 
               compressedData.length.toInt() > maxSizeBytes && 
               compressionQuality > 0.1) {
            compressionQuality -= 0.1
            compressedData = UIImageJPEGRepresentation(resizedImage, compressionQuality)
        }
        
        return compressedData?.toByteArray() ?: imageBytes
    }
    
    /**
     * Get image dimensions without fully decoding
     */
    actual fun getImageDimensions(imageBytes: ByteArray): Pair<Int, Int> {
        val nsData = imageBytes.toNSData() ?: return Pair(0, 0)
        val image = UIImage.imageWithData(nsData) ?: return Pair(0, 0)
        
        val width = image.size.useContents { width.toInt() }
        val height = image.size.useContents { height.toInt() }
        
        return Pair(width, height)
    }
    
    /**
     * Resize image to new dimensions
     */
    private fun resizeImage(image: UIImage, width: Double, height: Double): UIImage? {
        val newSize = CGSizeMake(width, height)
        
        UIGraphicsBeginImageContextWithOptions(newSize, false, image.scale)
        
        image.drawInRect(CGRectMake(0.0, 0.0, width, height))
        
        val resizedImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return resizedImage
    }
    
    /**
     * Convert image to PNG format
     */
    fun convertToPng(imageBytes: ByteArray): ByteArray? {
        val nsData = imageBytes.toNSData() ?: return null
        val image = UIImage.imageWithData(nsData) ?: return null
        val pngData = UIImagePNGRepresentation(image) ?: return null
        return pngData.toByteArray()
    }
    
    /**
     * Convert image to JPEG format
     */
    fun convertToJpeg(imageBytes: ByteArray, quality: Int = 80): ByteArray? {
        val nsData = imageBytes.toNSData() ?: return null
        val image = UIImage.imageWithData(nsData) ?: return null
        val jpegData = UIImageJPEGRepresentation(image, quality / 100.0) ?: return null
        return jpegData.toByteArray()
    }
    
    /**
     * Crop image to square (center crop)
     */
    fun cropToSquare(imageBytes: ByteArray): ByteArray? {
        val nsData = imageBytes.toNSData() ?: return null
        val image = UIImage.imageWithData(nsData) ?: return null
        
        val originalWidth = image.size.useContents { width }
        val originalHeight = image.size.useContents { height }
        val size = minOf(originalWidth, originalHeight)
        
        val x = (originalWidth - size) / 2
        val y = (originalHeight - size) / 2
        
        val cropRect = CGRectMake(x, y, size, size)
        
        val cgImage = image.CGImage ?: return null
        val croppedCgImage = CGImageCreateWithImageInRect(cgImage, cropRect) ?: return null
        
        val croppedImage = UIImage.imageWithCGImage(croppedCgImage)
        val jpegData = UIImageJPEGRepresentation(croppedImage, 0.9) ?: return null
        
        return jpegData.toByteArray()
    }
    
    /**
     * Get image format from bytes
     */
    fun getImageFormat(imageBytes: ByteArray): IosImageFormat {
        if (imageBytes.size < 4) return IosImageFormat.UNKNOWN
        
        return when {
            // JPEG: FF D8 FF
            imageBytes[0] == 0xFF.toByte() && 
            imageBytes[1] == 0xD8.toByte() && 
            imageBytes[2] == 0xFF.toByte() -> IosImageFormat.JPEG
            
            // PNG: 89 50 4E 47
            imageBytes[0] == 0x89.toByte() && 
            imageBytes[1] == 0x50.toByte() && 
            imageBytes[2] == 0x4E.toByte() && 
            imageBytes[3] == 0x47.toByte() -> IosImageFormat.PNG
            
            // GIF: 47 49 46 38
            imageBytes[0] == 0x47.toByte() && 
            imageBytes[1] == 0x49.toByte() && 
            imageBytes[2] == 0x46.toByte() && 
            imageBytes[3] == 0x38.toByte() -> IosImageFormat.GIF
            
            // WebP: 52 49 46 46 ... 57 45 42 50
            imageBytes[0] == 0x52.toByte() && 
            imageBytes[1] == 0x49.toByte() && 
            imageBytes[2] == 0x46.toByte() && 
            imageBytes[3] == 0x46.toByte() -> IosImageFormat.WEBP
            
            else -> IosImageFormat.UNKNOWN
        }
    }
}

/**
 * iOS-specific image format enum with additional formats
 */
enum class IosImageFormat {
    JPEG,
    PNG,
    GIF,
    WEBP,
    UNKNOWN
}

/**
 * Extension to convert ByteArray to NSData
 */
@OptIn(ExperimentalForeignApi::class)
private fun ByteArray.toNSData(): NSData? {
    if (isEmpty()) return null
    return this.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = size.toULong())
    }
}

/**
 * Extension to convert NSData to ByteArray
 */
@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    if (length == 0) return ByteArray(0)
    
    return ByteArray(length).also { bytes ->
        bytes.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), this@toByteArray.bytes, this@toByteArray.length)
        }
    }
}
