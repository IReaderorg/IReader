package ireader.data.quote

import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.domain.models.quote.QuoteCardStyleColors
import java.awt.Color
import java.awt.Font
import java.awt.GradientPaint
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

/**
 * Desktop implementation of QuoteCardGenerator using Java2D
 */
class DesktopQuoteCardGenerator : QuoteCardGenerator {
    
    override suspend fun generateQuoteCard(quote: LocalQuote, style: QuoteCardStyle): ByteArray {
        val width = 1080
        val height = 1920
        
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        
        // Enable anti-aliasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        
        // Get gradient colors and text color for style
        val (startColor, endColor) = getGradientColors(style)
        val textColor = getTextColor(style)
        
        // Draw gradient background
        val gradient = GradientPaint(0f, 0f, startColor, 0f, height.toFloat(), endColor)
        g2d.paint = gradient
        g2d.fillRect(0, 0, width, height)
        
        // Calculate center Y position
        val centerY = height / 2
        
        // Draw IReader logo and text at top center (matching Compose UI)
        g2d.color = Color(textColor.red, textColor.green, textColor.blue, (255 * 0.9).toInt())
        g2d.font = Font("SansSerif", Font.BOLD, 42)
        val logoText = "IReader"
        val logoWidth = g2d.fontMetrics.stringWidth(logoText)
        g2d.drawString(logoText, (width - logoWidth) / 2, centerY - 400)
        
        // Draw quote mark icon (decorative, centered above quote)
        g2d.color = Color(textColor.red, textColor.green, textColor.blue, (255 * 0.3).toInt())
        g2d.font = Font("Serif", Font.BOLD, 120)
        val quoteMarkText = "\""
        val quoteMarkWidth = g2d.fontMetrics.stringWidth(quoteMarkText)
        g2d.drawString(quoteMarkText, (width - quoteMarkWidth) / 2, centerY - 300)
        
        // Draw quote text (centered, wrapped, italic)
        g2d.color = textColor
        g2d.font = Font("Serif", Font.ITALIC, 56)
        
        val quoteText = "\"${quote.text}\""
        val maxWidth = width - 160
        val lines = wrapText(quoteText, g2d.font, g2d.fontMetrics, maxWidth)
        
        var y = centerY - (lines.size * 70 / 2)
        lines.forEach { line ->
            val lineWidth = g2d.fontMetrics.stringWidth(line)
            val x = (width - lineWidth) / 2
            g2d.drawString(line, x, y)
            y += 70
        }
        
        // Draw book title (centered, bold)
        g2d.color = Color(textColor.red, textColor.green, textColor.blue, (255 * 0.9).toInt())
        g2d.font = Font("SansSerif", Font.BOLD, 48)
        val bookTitleWidth = g2d.fontMetrics.stringWidth(quote.bookTitle)
        g2d.drawString(quote.bookTitle, (width - bookTitleWidth) / 2, centerY + 250)
        
        // Draw author (centered)
        if (!quote.author.isNullOrBlank()) {
            g2d.color = Color(textColor.red, textColor.green, textColor.blue, (255 * 0.7).toInt())
            g2d.font = Font("SansSerif", Font.PLAIN, 40)
            val authorText = "by ${quote.author}"
            val authorWidth = g2d.fontMetrics.stringWidth(authorText)
            g2d.drawString(authorText, (width - authorWidth) / 2, centerY + 310)
        }
        
        g2d.dispose()
        
        // Convert to PNG bytes
        val outputStream = ByteArrayOutputStream()
        ImageIO.write(image, "PNG", outputStream)
        
        return outputStream.toByteArray()
    }
    
    private fun wrapText(text: String, font: Font, fontMetrics: java.awt.FontMetrics, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        
        words.forEach { word ->
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val lineWidth = fontMetrics.stringWidth(testLine)
            
            if (lineWidth <= maxWidth) {
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
    
    private fun getTextColor(style: QuoteCardStyle): Color {
        val composeColor = QuoteCardStyleColors.getTextColor(style)
        return Color(
            (composeColor.red * 255).toInt(),
            (composeColor.green * 255).toInt(),
            (composeColor.blue * 255).toInt(),
            (composeColor.alpha * 255).toInt()
        )
    }
    
    private fun getGradientColors(style: QuoteCardStyle): Pair<Color, Color> {
        val (startCompose, endCompose) = QuoteCardStyleColors.getGradientColors(style)
        
        val startColor = Color(
            (startCompose.red * 255).toInt(),
            (startCompose.green * 255).toInt(),
            (startCompose.blue * 255).toInt(),
            (startCompose.alpha * 255).toInt()
        )
        
        val endColor = Color(
            (endCompose.red * 255).toInt(),
            (endCompose.green * 255).toInt(),
            (endCompose.blue * 255).toInt(),
            (endCompose.alpha * 255).toInt()
        )
        
        return Pair(startColor, endColor)
    }
}
