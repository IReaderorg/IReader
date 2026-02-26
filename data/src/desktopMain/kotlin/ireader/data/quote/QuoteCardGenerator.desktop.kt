package ireader.data.quote

import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.*
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Desktop implementation of QuoteCardGenerator using Java2D.
 */
actual class QuoteCardGenerator {
    
    actual suspend fun generateQuoteCard(
        quote: LocalQuote,
        style: QuoteCardStyle
    ): ByteArray = withContext(Dispatchers.Default) {
        val width = 1080
        val height = 1920
        
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        
        // Draw background gradient
        drawBackground(g2d, width, height, style)
        
        // Draw quote text
        drawQuoteText(g2d, quote, width, height, style)
        
        g2d.dispose()
        
        // Convert to PNG bytes
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "PNG", outputStream)
        outputStream.toByteArray()
    }
    
    private fun drawBackground(g2d: Graphics2D, width: Int, height: Int, style: QuoteCardStyle) {
        val colors = getGradientColors(style)
        val gradient = GradientPaint(
            0f, 0f, colors.first,
            0f, height.toFloat(), colors.second
        )
        g2d.paint = gradient
        g2d.fillRect(0, 0, width, height)
    }
    
    private fun drawQuoteText(
        g2d: Graphics2D,
        quote: LocalQuote,
        width: Int,
        height: Int,
        style: QuoteCardStyle
    ) {
        val textColor = getTextColor(style)
        val padding = 80
        val maxWidth = width - (padding * 2)
        
        // Quote text
        g2d.color = textColor
        g2d.font = Font("SansSerif", Font.PLAIN, 56)
        
        val quoteText = "\"${quote.text}\""
        val quoteLines = wrapText(quoteText, g2d.font, g2d.fontMetrics, maxWidth)
        var y = height / 2 - (quoteLines.size * 70 / 2)
        
        for (line in quoteLines) {
            val lineWidth = g2d.fontMetrics.stringWidth(line)
            val x = (width - lineWidth) / 2
            g2d.drawString(line, x, y)
            y += 70
        }
        
        y += 60
        
        // Book title
        g2d.color = Color(textColor.red, textColor.green, textColor.blue, (255 * 0.8).toInt())
        g2d.font = Font("SansSerif", Font.BOLD, 42)
        
        val titleWidth = g2d.fontMetrics.stringWidth(quote.bookTitle)
        val titleX = (width - titleWidth) / 2
        g2d.drawString(quote.bookTitle, titleX, y)
        y += 50
        
        // Author
        if (!quote.author.isNullOrBlank()) {
            g2d.color = Color(textColor.red, textColor.green, textColor.blue, (255 * 0.6).toInt())
            g2d.font = Font("SansSerif", Font.PLAIN, 36)
            
            val authorText = "by ${quote.author}"
            val authorWidth = g2d.fontMetrics.stringWidth(authorText)
            val authorX = (width - authorWidth) / 2
            g2d.drawString(authorText, authorX, y)
        }
    }
    
    private fun wrapText(text: String, font: Font, metrics: FontMetrics, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val width = metrics.stringWidth(testLine)
            
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
    
    private fun getGradientColors(style: QuoteCardStyle): Pair<Color, Color> {
        return when (style) {
            QuoteCardStyle.GRADIENT_SUNSET -> Pair(Color(255, 107, 107), Color(255, 230, 109))
            QuoteCardStyle.GRADIENT_OCEAN -> Pair(Color(78, 205, 196), Color(85, 98, 112))
            QuoteCardStyle.GRADIENT_FOREST -> Pair(Color(19, 78, 94), Color(113, 178, 128))
            QuoteCardStyle.GRADIENT_LAVENDER -> Pair(Color(218, 34, 255), Color(151, 51, 238))
            QuoteCardStyle.GRADIENT_MIDNIGHT -> Pair(Color(35, 37, 38), Color(65, 67, 69))
            QuoteCardStyle.MINIMAL_LIGHT -> Pair(Color(245, 245, 245), Color(255, 255, 255))
            QuoteCardStyle.MINIMAL_DARK -> Pair(Color(26, 26, 26), Color(45, 45, 45))
            QuoteCardStyle.PAPER_TEXTURE -> Pair(Color(255, 248, 220), Color(250, 240, 230))
            QuoteCardStyle.BOOK_COVER -> Pair(Color(139, 69, 19), Color(210, 105, 30))
        }
    }
    
    private fun getTextColor(style: QuoteCardStyle): Color {
        return when (style) {
            QuoteCardStyle.MINIMAL_LIGHT,
            QuoteCardStyle.PAPER_TEXTURE -> Color.BLACK
            else -> Color.WHITE
        }
    }
}
