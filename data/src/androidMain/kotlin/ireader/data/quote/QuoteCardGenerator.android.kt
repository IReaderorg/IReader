package ireader.data.quote

import android.graphics.*
import android.graphics.drawable.GradientDrawable
import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Android implementation of QuoteCardGenerator using Canvas/Bitmap.
 */
actual class QuoteCardGenerator {
    
    actual suspend fun generateQuoteCard(
        quote: LocalQuote,
        style: QuoteCardStyle
    ): ByteArray = withContext(Dispatchers.Default) {
        val width = 1080
        val height = 1920
        
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw background gradient
        drawBackground(canvas, width, height, style)
        
        // Draw quote text
        drawQuoteText(canvas, quote, width, height, style)
        
        // Convert to PNG bytes
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        bitmap.recycle()
        
        outputStream.toByteArray()
    }
    
    private fun drawBackground(canvas: Canvas, width: Int, height: Int, style: QuoteCardStyle) {
        val colors = getGradientColors(style)
        val gradient = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            colors
        )
        gradient.setBounds(0, 0, width, height)
        gradient.draw(canvas)
    }
    
    private fun drawQuoteText(
        canvas: Canvas,
        quote: LocalQuote,
        width: Int,
        height: Int,
        style: QuoteCardStyle
    ) {
        val textColor = getTextColor(style)
        val padding = 80f
        val maxWidth = width - (padding * 2)
        
        // Quote text
        val quotePaint = Paint().apply {
            color = textColor
            textSize = 56f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        val quoteText = "\"${quote.text}\""
        val quoteLines = wrapText(quoteText, quotePaint, maxWidth)
        var y = height / 2f - (quoteLines.size * 70f / 2f)
        
        for (line in quoteLines) {
            canvas.drawText(line, width / 2f, y, quotePaint)
            y += 70f
        }
        
        y += 60f
        
        // Book title
        val titlePaint = Paint().apply {
            color = adjustAlpha(textColor, 0.8f)
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawText(quote.bookTitle, width / 2f, y, titlePaint)
        y += 50f
        
        // Author
        if (!quote.author.isNullOrBlank()) {
            val authorPaint = Paint().apply {
                color = adjustAlpha(textColor, 0.6f)
                textSize = 36f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }
            
            canvas.drawText("by ${quote.author}", width / 2f, y, authorPaint)
        }
    }
    
    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = paint.measureText(testLine)
            
            if (width > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }
        
        return lines
    }
    
    private fun getGradientColors(style: QuoteCardStyle): IntArray {
        return when (style) {
            QuoteCardStyle.GRADIENT_SUNSET -> intArrayOf(0xFFFF6B6B.toInt(), 0xFFFFE66D.toInt())
            QuoteCardStyle.GRADIENT_OCEAN -> intArrayOf(0xFF4ECDC4.toInt(), 0xFF556270.toInt())
            QuoteCardStyle.GRADIENT_FOREST -> intArrayOf(0xFF134E5E.toInt(), 0xFF71B280.toInt())
            QuoteCardStyle.GRADIENT_LAVENDER -> intArrayOf(0xFFDA22FF.toInt(), 0xFF9733EE.toInt())
            QuoteCardStyle.GRADIENT_MIDNIGHT -> intArrayOf(0xFF232526.toInt(), 0xFF414345.toInt())
            QuoteCardStyle.MINIMAL_LIGHT -> intArrayOf(0xFFF5F5F5.toInt(), 0xFFFFFFFF.toInt())
            QuoteCardStyle.MINIMAL_DARK -> intArrayOf(0xFF1A1A1A.toInt(), 0xFF2D2D2D.toInt())
            QuoteCardStyle.PAPER_TEXTURE -> intArrayOf(0xFFFFF8DC.toInt(), 0xFFFAF0E6.toInt())
            QuoteCardStyle.BOOK_COVER -> intArrayOf(0xFF8B4513.toInt(), 0xFFD2691E.toInt())
        }
    }
    
    private fun getTextColor(style: QuoteCardStyle): Int {
        return when (style) {
            QuoteCardStyle.MINIMAL_LIGHT,
            QuoteCardStyle.PAPER_TEXTURE -> Color.BLACK
            else -> Color.WHITE
        }
    }
    
    private fun adjustAlpha(color: Int, alpha: Float): Int {
        val a = (Color.alpha(color) * alpha).toInt()
        val r = Color.red(color)
        val g = Color.green(color)
        val b = Color.blue(color)
        return Color.argb(a, r, g, b)
    }
}
