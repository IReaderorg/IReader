package ireader.data.characterart

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

actual class ImageCompressor {
    
    actual suspend fun compress(
        imageBytes: ByteArray,
        maxWidth: Int,
        maxHeight: Int,
        quality: Int,
        maxSizeKb: Int
    ): ByteArray = withContext(Dispatchers.IO) {
        // Decode bitmap
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
        
        // Calculate sample size for initial downscaling
        val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxWidth, maxHeight)
        
        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
        }
        
        var bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, decodeOptions)
            ?: return@withContext imageBytes
        
        // Scale to exact dimensions if needed
        if (bitmap.width > maxWidth || bitmap.height > maxHeight) {
            val scale = minOf(
                maxWidth.toFloat() / bitmap.width,
                maxHeight.toFloat() / bitmap.height
            )
            val newWidth = (bitmap.width * scale).toInt()
            val newHeight = (bitmap.height * scale).toInt()
            
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
            if (scaledBitmap != bitmap) {
                bitmap.recycle()
                bitmap = scaledBitmap
            }
        }
        
        // Compress with quality adjustment to meet size target
        var currentQuality = quality
        var result: ByteArray
        
        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, currentQuality, outputStream)
            result = outputStream.toByteArray()
            
            if (result.size <= maxSizeKb * 1024 || currentQuality <= 20) {
                break
            }
            
            currentQuality -= 10
        } while (currentQuality > 20)
        
        bitmap.recycle()
        result
    }
    
    actual fun getImageDimensions(imageBytes: ByteArray): Pair<Int, Int> {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
        return Pair(options.outWidth, options.outHeight)
    }
    
    private fun calculateSampleSize(
        width: Int,
        height: Int,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        var sampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while ((halfHeight / sampleSize) >= reqHeight && (halfWidth / sampleSize) >= reqWidth) {
                sampleSize *= 2
            }
        }
        
        return sampleSize
    }
}
