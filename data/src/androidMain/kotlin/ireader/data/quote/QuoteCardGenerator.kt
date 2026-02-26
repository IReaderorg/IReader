package ireader.data.quote

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
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
        return when (style) {
            QuoteCardStyle.MINIMAL_LIGHT,
            QuoteCardStyle.PAPER_TEXTURE -> android.graphics.Color.BLACK
            else -> android.graphics.Color.WHITE
        }
    }
    
    private fun getGradientColors(style: QuoteCardStyle): Pair<Int, Int> {
        return when (style) {
            QuoteCardStyle.GRADIENT_SUNSET -> Pair(
                android.graphics.Color.rgb(255, 107, 107),
                android.graphics.Color.rgb(255, 230, 109)
            )
            QuoteCardStyle.GRADIENT_OCEAN -> Pair(
                android.graphics.Color.rgb(102, 126, 234),
                android.graphics.Color.rgb(118, 75, 162)
            )
            QuoteCardStyle.GRADIENT_FOREST -> Pair(
                android.graphics.Color.rgb(17, 153, 142),
                android.graphics.Color.rgb(56, 239, 125)
            )
            QuoteCardStyle.GRADIENT_LAVENDER -> Pair(
                android.graphics.Color.rgb(218, 34, 255),
                android.graphics.Color.rgb(151, 51, 238)
            )
            QuoteCardStyle.GRADIENT_MIDNIGHT -> Pair(
                android.graphics.Color.rgb(44, 62, 80),
                android.graphics.Color.rgb(76, 161, 175)
            )
            QuoteCardStyle.MINIMAL_LIGHT -> Pair(
                android.graphics.Color.rgb(245, 245, 245),
                android.graphics.Color.rgb(224, 224, 224)
            )
            QuoteCardStyle.MINIMAL_DARK -> Pair(
                android.graphics.Color.rgb(26, 26, 26),
                android.graphics.Color.rgb(45, 45, 45)
            )
            QuoteCardStyle.PAPER_TEXTURE -> Pair(
                android.graphics.Color.rgb(255, 248, 220),
                android.graphics.Color.rgb(250, 235, 215)
            )
            QuoteCardStyle.BOOK_COVER -> Pair(
                android.graphics.Color.rgb(139, 69, 19),
                android.graphics.Color.rgb(210, 105, 30)
            )
        }
    }
}
