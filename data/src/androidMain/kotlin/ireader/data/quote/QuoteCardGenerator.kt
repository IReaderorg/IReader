package ireader.data.quote

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.domain.models.quote.QuoteCardStyleColors
import java.io.ByteArrayOutputStream

/**
 * Android implementation of QuoteCardGenerator using Canvas API
 */
class AndroidQuoteCardGenerator : QuoteCardGenerator {
    
    override suspend fun generateQuoteCard(quote: LocalQuote, style: QuoteCardStyle): ByteArray {
        val width = 1080
        val height = 1920
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Get gradient colors and text color for style
        val (startColor, endColor) = getGradientColors(style)
        val textColor = getTextColor(style)
        
        // Draw gradient background
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val gradient = android.graphics.LinearGradient(
            0f, 0f, 0f, height.toFloat(),
            startColor, endColor,
            android.graphics.Shader.TileMode.CLAMP
        )
        paint.shader = gradient
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        
        // Calculate center Y position
        val centerY = height / 2f
        
        // Draw IReader logo and text at top center (matching Compose UI)
        val logoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            alpha = (0.9f * 255).toInt()
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("IReader", width / 2f, centerY - 400f, logoPaint)
        
        // Draw quote mark icon (decorative, centered above quote)
        val quotePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            alpha = (0.3f * 255).toInt()
            textSize = 120f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("\"", width / 2f, centerY - 300f, quotePaint)
        
        // Draw quote text (centered, wrapped, italic)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            textSize = 56f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        
        val quoteText = "\"${quote.text}\""
        val maxWidth = width - 160
        val lines = wrapText(quoteText, textPaint, maxWidth)
        
        var y = centerY - (lines.size * 70f / 2f)
        lines.forEach { line ->
            canvas.drawText(line, width / 2f, y, textPaint)
            y += 70f
        }
        
        // Draw book title (centered, with book icon effect)
        val bookPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            alpha = (0.9f * 255).toInt()
            textSize = 48f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(quote.bookTitle, width / 2f, centerY + 250f, bookPaint)
        
        // Draw author (centered)
        if (!quote.author.isNullOrBlank()) {
            val authorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = textColor
                alpha = (0.7f * 255).toInt()
                textSize = 40f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("by ${quote.author}", width / 2f, centerY + 310f, authorPaint)
        }
        
        // Convert to PNG bytes
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        bitmap.recycle()
        
        return outputStream.toByteArray()
    }
    
    private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val bounds = Rect()
            paint.getTextBounds(testLine, 0, testLine.length, bounds)
            
            if (bounds.width() <= maxWidth) {
                currentLine = testLine
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine)
                }
                currentLine = word
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return lines
    }
    
    private fun getTextColor(style: QuoteCardStyle): Int {
        val composeColor = QuoteCardStyleColors.getTextColor(style)
        return android.graphics.Color.argb(
            (composeColor.alpha * 255).toInt(),
            (composeColor.red * 255).toInt(),
            (composeColor.green * 255).toInt(),
            (composeColor.blue * 255).toInt()
        )
    }
    
    private fun getGradientColors(style: QuoteCardStyle): Pair<Int, Int> {
        val (startCompose, endCompose) = QuoteCardStyleColors.getGradientColors(style)
        
        val startColor = android.graphics.Color.argb(
            (startCompose.alpha * 255).toInt(),
            (startCompose.red * 255).toInt(),
            (startCompose.green * 255).toInt(),
            (startCompose.blue * 255).toInt()
        )
        
        val endColor = android.graphics.Color.argb(
            (endCompose.alpha * 255).toInt(),
            (endCompose.red * 255).toInt(),
            (endCompose.green * 255).toInt(),
            (endCompose.blue * 255).toInt()
        )
        
        return Pair(startColor, endColor)
    }
}
