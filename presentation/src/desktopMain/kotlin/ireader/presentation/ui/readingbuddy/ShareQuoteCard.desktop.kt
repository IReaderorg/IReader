package ireader.presentation.ui.readingbuddy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.geom.Ellipse2D
import java.awt.geom.Path2D
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.random.Random

actual class QuoteCardSharer {
    actual suspend fun shareQuoteCard(
        quote: Quote,
        style: QuoteCardStyle,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                val image = createQuoteImage(quote, style)
                
                val transferable = ImageTransferable(image)
                Toolkit.getDefaultToolkit().systemClipboard.setContents(transferable, null)
                
                val tempFile = File.createTempFile("ireader_quote_", ".png")
                ImageIO.write(image, "PNG", tempFile)
                
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(tempFile)
                }
            }
            onSuccess()
        } catch (e: Exception) {
            onError(e.message ?: "Failed to share quote")
        }
    }
    
    private fun createQuoteImage(quote: Quote, style: QuoteCardStyle): BufferedImage {
        val width = 1080
        val height = 1350
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val g2d = image.createGraphics()
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB)
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC)
        
        val colors = getStyleColors(style)
        
        drawBackground(g2d, width, height, colors, style)
        drawDecorativeElements(g2d, width, height, colors, style)
        drawContentCard(g2d, width, height, quote, colors, style)
        drawBranding(g2d, width, height, colors)
        
        g2d.dispose()
        return image
    }
    
    private fun drawBackground(g2d: Graphics2D, width: Int, height: Int, colors: StyleColors, style: QuoteCardStyle) {
        val gradient = GradientPaint(0f, 0f, colors.startColor, width.toFloat(), height.toFloat(), colors.endColor)
        g2d.paint = gradient
        g2d.fillRect(0, 0, width, height)
        
        if (style == QuoteCardStyle.PAPER_TEXTURE || style == QuoteCardStyle.BOOK_COVER) {
            drawPaperTexture(g2d, width, height)
        }
    }
    
    private fun drawPaperTexture(g2d: Graphics2D, width: Int, height: Int) {
        g2d.color = Color(0, 0, 0, 8)
        val random = Random(42)
        repeat(3000) {
            val x = (random.nextFloat() * width).toInt()
            val y = (random.nextFloat() * height).toInt()
            g2d.fillOval(x, y, 2, 2)
        }
    }
    
    private fun drawDecorativeElements(g2d: Graphics2D, width: Int, height: Int, colors: StyleColors, style: QuoteCardStyle) {
        when (style) {
            QuoteCardStyle.GRADIENT_SUNSET, QuoteCardStyle.GRADIENT_OCEAN,
            QuoteCardStyle.GRADIENT_FOREST, QuoteCardStyle.GRADIENT_MIDNIGHT -> {
                drawBokehEffect(g2d, width, height, colors)
            }
            QuoteCardStyle.GRADIENT_LAVENDER -> {
                drawSoftShapes(g2d, width, height, colors)
            }
            QuoteCardStyle.MINIMAL_LIGHT, QuoteCardStyle.MINIMAL_DARK -> {
                drawGeometricLines(g2d, width, height, colors)
            }
            QuoteCardStyle.PAPER_TEXTURE -> {
                drawVintageCorners(g2d, width, height, colors)
            }
            QuoteCardStyle.BOOK_COVER -> {
                drawBookElements(g2d, width, height, colors)
            }
        }
    }
    
    private fun drawBokehEffect(g2d: Graphics2D, width: Int, height: Int, colors: StyleColors) {
        val random = Random(123)
        
        repeat(8) {
            val x = (random.nextFloat() * width).toInt()
            val y = (random.nextFloat() * height).toInt()
            val radius = (60 + random.nextFloat() * 120).toInt()
            
            val gradient = RadialGradientPaint(
                x.toFloat(), y.toFloat(), radius.toFloat(),
                floatArrayOf(0f, 1f),
                arrayOf(Color(255, 255, 255, 37), Color(255, 255, 255, 0))
            )
            g2d.paint = gradient
            g2d.fill(Ellipse2D.Float((x - radius).toFloat(), (y - radius).toFloat(), (radius * 2).toFloat(), (radius * 2).toFloat()))
        }
        
        g2d.color = Color(255, 255, 255, 64)
        repeat(20) {
            val x = (random.nextFloat() * width).toInt()
            val y = (random.nextFloat() * height).toInt()
            val size = (2 + random.nextFloat() * 4).toInt()
            g2d.fillOval(x, y, size, size)
        }
    }
    
    private fun drawSoftShapes(g2d: Graphics2D, width: Int, height: Int, colors: StyleColors) {
        g2d.color = Color(colors.accentColor.red, colors.accentColor.green, colors.accentColor.blue, 32)
        
        val path1 = Path2D.Float()
        path1.moveTo(0.0, 0.0)
        path1.quadTo(150.0, 80.0, 100.0, 200.0)
        path1.quadTo(50.0, 100.0, 0.0, 0.0)
        g2d.fill(path1)
        
        val path2 = Path2D.Float()
        path2.moveTo(width.toDouble(), height.toDouble())
        path2.quadTo((width - 150).toDouble(), (height - 80).toDouble(), (width - 100).toDouble(), (height - 200).toDouble())
        path2.quadTo((width - 50).toDouble(), (height - 100).toDouble(), width.toDouble(), height.toDouble())
        g2d.fill(path2)
    }
    
    private fun drawGeometricLines(g2d: Graphics2D, width: Int, height: Int, colors: StyleColors) {
        g2d.color = Color(colors.textColor.red, colors.textColor.green, colors.textColor.blue, 21)
        g2d.stroke = BasicStroke(1f)
        
        for (i in 0 until width step 80) {
            g2d.drawLine(i, 0, i, height)
        }
        for (i in 0 until height step 80) {
            g2d.drawLine(0, i, width, i)
        }
        
        g2d.stroke = BasicStroke(3f)
        g2d.color = Color(colors.accentColor.red, colors.accentColor.green, colors.accentColor.blue, 80)
        g2d.drawLine(40, 40, 140, 40)
        g2d.drawLine(40, 40, 40, 140)
        g2d.drawLine(width - 40, height - 40, width - 140, height - 40)
        g2d.drawLine(width - 40, height - 40, width - 40, height - 140)
    }
    
    private fun drawVintageCorners(g2d: Graphics2D, width: Int, height: Int, colors: StyleColors) {
        g2d.color = Color(colors.textColor.red, colors.textColor.green, colors.textColor.blue, 53)
        g2d.stroke = BasicStroke(2f)
        
        val cornerSize = 80
        val margin = 50
        
        g2d.drawLine(margin, margin + cornerSize, margin, margin)
        g2d.drawLine(margin, margin, margin + cornerSize, margin)
        
        g2d.drawLine(width - margin - cornerSize, margin, width - margin, margin)
        g2d.drawLine(width - margin, margin, width - margin, margin + cornerSize)
        
        g2d.drawLine(margin, height - margin - cornerSize, margin, height - margin)
        g2d.drawLine(margin, height - margin, margin + cornerSize, height - margin)
        
        g2d.drawLine(width - margin - cornerSize, height - margin, width - margin, height - margin)
        g2d.drawLine(width - margin, height - margin, width - margin, height - margin - cornerSize)
    }
    
    private fun drawBookElements(g2d: Graphics2D, width: Int, height: Int, colors: StyleColors) {
        val spineGradient = GradientPaint(0f, 0f, Color(0, 0, 0, 80), 60f, 0f, Color(0, 0, 0, 0))
        g2d.paint = spineGradient
        g2d.fillRect(0, 0, 60, height)
        
        g2d.color = Color(colors.textColor.red, colors.textColor.green, colors.textColor.blue, 48)
        g2d.stroke = BasicStroke(4f)
        g2d.draw(RoundRectangle2D.Float(30f, 30f, (width - 60).toFloat(), (height - 60).toFloat(), 8f, 8f))
        
        g2d.stroke = BasicStroke(1f)
        g2d.draw(RoundRectangle2D.Float(45f, 45f, (width - 90).toFloat(), (height - 90).toFloat(), 4f, 4f))
    }
    
    private fun drawContentCard(g2d: Graphics2D, width: Int, height: Int, quote: Quote, colors: StyleColors, style: QuoteCardStyle) {
        val cardMargin = 60
        val cardTop = 180
        val cardBottom = height - 220
        
        if (style in listOf(QuoteCardStyle.MINIMAL_LIGHT, QuoteCardStyle.MINIMAL_DARK,
                QuoteCardStyle.PAPER_TEXTURE, QuoteCardStyle.BOOK_COVER)) {
            g2d.color = Color(0, 0, 0, 64)
            g2d.fill(RoundRectangle2D.Float(
                (cardMargin + 8).toFloat(), (cardTop + 8).toFloat(),
                (width - cardMargin * 2).toFloat(), (cardBottom - cardTop).toFloat(), 24f, 24f
            ))
            
            g2d.color = if (style == QuoteCardStyle.MINIMAL_DARK) Color(0x2D, 0x2D, 0x2D) else Color(0xFF, 0xFF, 0xF8)
            g2d.fill(RoundRectangle2D.Float(
                cardMargin.toFloat(), cardTop.toFloat(),
                (width - cardMargin * 2).toFloat(), (cardBottom - cardTop).toFloat(), 24f, 24f
            ))
        }
        
        val centerX = width / 2
        
        // Calculate quote text first
        g2d.font = Font("SansSerif", Font.ITALIC, 44)
        val lines = wrapText(quote.text, g2d, width - cardMargin * 2 - 80)
        val lineHeight = 64
        
        // Position opening quote mark at top left of text area
        var currentY = cardTop + 60
        g2d.font = Font("SansSerif", Font.PLAIN, 80)
        g2d.color = colors.quoteMarkColor
        g2d.drawString("❝", cardMargin + 30, currentY)
        
        // Start quote text below the opening quote mark
        currentY += 50
        g2d.color = colors.textColor
        g2d.font = Font("SansSerif", Font.ITALIC, 44)
        for (line in lines) {
            drawCenteredString(g2d, line, centerX, currentY)
            currentY += lineHeight
        }
        
        // Closing quote mark at bottom right of text area
        currentY += 10
        g2d.font = Font("SansSerif", Font.PLAIN, 80)
        g2d.color = colors.quoteMarkColor
        val metrics = g2d.fontMetrics
        g2d.drawString("❞", width - cardMargin - 30 - metrics.stringWidth("❞"), currentY)
        currentY += 40
        
        // Elegant divider
        drawElegantDivider(g2d, centerX, currentY, colors)
        currentY += 45
        
        // Book title - full opacity
        g2d.color = colors.textColor
        g2d.font = Font("SansSerif", Font.BOLD, 34)
        drawCenteredString(g2d, quote.bookTitle.uppercase(), centerX, currentY)
        currentY += 38
        
        // Author
        if (quote.author.isNotBlank()) {
            g2d.font = Font("SansSerif", Font.PLAIN, 26)
            g2d.color = colors.secondaryTextColor
            drawCenteredString(g2d, "by ${quote.author}", centerX, currentY)
            currentY += 32
        }
        
        // Submitter
        if (quote.submitterUsername.isNotBlank()) {
            g2d.font = Font("SansSerif", Font.PLAIN, 20)
            g2d.color = colors.tertiaryTextColor
            drawCenteredString(g2d, "Shared by ${quote.submitterUsername}", centerX, currentY + 15)
        }
    }
    
    private fun drawElegantDivider(g2d: Graphics2D, centerX: Int, y: Int, colors: StyleColors) {
        g2d.color = colors.dividerColor
        g2d.stroke = BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        
        val diamond = Path2D.Float()
        diamond.moveTo(centerX.toDouble(), (y - 8).toDouble())
        diamond.lineTo((centerX + 8).toDouble(), y.toDouble())
        diamond.lineTo(centerX.toDouble(), (y + 8).toDouble())
        diamond.lineTo((centerX - 8).toDouble(), y.toDouble())
        diamond.closePath()
        g2d.fill(diamond)
        
        g2d.drawLine(centerX - 100, y, centerX - 20, y)
        g2d.drawLine(centerX + 20, y, centerX + 100, y)
        
        g2d.fillOval(centerX - 109, y - 4, 8, 8)
        g2d.fillOval(centerX + 101, y - 4, 8, 8)
    }
    
    private fun drawBranding(g2d: Graphics2D, width: Int, height: Int, colors: StyleColors) {
        val centerX = width / 2
        val brandingY = height - 70
        
        // Draw book icon
        val iconSize = 48
        val iconX = centerX - 85
        val iconY = brandingY - 35
        
        // Book icon background circle
        g2d.color = Color(colors.textColor.red, colors.textColor.green, colors.textColor.blue, 32)
        g2d.fill(Ellipse2D.Float((iconX - 8).toFloat(), (iconY - 8).toFloat(), (iconSize + 16).toFloat(), (iconSize + 16).toFloat()))
        
        // Draw simple book icon
        g2d.color = colors.brandingTextColor
        g2d.stroke = BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
        
        // Book shape
        val bookPath = Path2D.Float()
        bookPath.moveTo((iconX + 8).toDouble(), (iconY + 8).toDouble())
        bookPath.lineTo((iconX + 8).toDouble(), (iconY + iconSize - 8).toDouble())
        bookPath.quadTo((iconX + iconSize / 2).toDouble(), (iconY + iconSize - 4).toDouble(), (iconX + iconSize - 8).toDouble(), (iconY + iconSize - 8).toDouble())
        bookPath.lineTo((iconX + iconSize - 8).toDouble(), (iconY + 8).toDouble())
        bookPath.quadTo((iconX + iconSize / 2).toDouble(), (iconY + 12).toDouble(), (iconX + 8).toDouble(), (iconY + 8).toDouble())
        g2d.draw(bookPath)
        
        // Center spine
        g2d.drawLine(iconX + iconSize / 2, iconY + 10, iconX + iconSize / 2, iconY + iconSize - 6)
        
        // App name
        g2d.font = Font("SansSerif", Font.BOLD, 32)
        g2d.color = colors.brandingTextColor
        g2d.drawString("IReader", centerX + 5, brandingY - 8)
        
        // Tagline
        g2d.font = Font("SansSerif", Font.PLAIN, 18)
        g2d.color = colors.brandingSubtextColor
        g2d.drawString("Your Reading Companion", centerX + 5, brandingY + 16)
    }
    
    private fun drawCenteredString(g2d: Graphics2D, text: String, centerX: Int, y: Int) {
        val metrics = g2d.fontMetrics
        val x = centerX - metrics.stringWidth(text) / 2
        g2d.drawString(text, x, y)
    }
    
    private fun wrapText(text: String, g2d: Graphics2D, maxWidth: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()
        val metrics = g2d.fontMetrics
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (metrics.stringWidth(testLine) <= maxWidth) {
                currentLine = StringBuilder(testLine)
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
    
    private fun getStyleColors(style: QuoteCardStyle): StyleColors {
        return when (style) {
            QuoteCardStyle.GRADIENT_SUNSET -> StyleColors(
                startColor = Color(0xFF, 0x6B, 0x6B),
                endColor = Color(0xFF, 0xE6, 0x6D),
                textColor = Color.WHITE,
                secondaryTextColor = Color(255, 255, 255, 230),
                tertiaryTextColor = Color(255, 255, 255, 179),
                accentColor = Color.WHITE,
                quoteMarkColor = Color(255, 255, 255, 128),
                dividerColor = Color(255, 255, 255, 153),
                brandingTextColor = Color.WHITE,
                brandingSubtextColor = Color(255, 255, 255, 179)
            )
            QuoteCardStyle.GRADIENT_OCEAN -> StyleColors(
                startColor = Color(0x66, 0x7E, 0xEA),
                endColor = Color(0x64, 0xB5, 0xF6),
                textColor = Color.WHITE,
                secondaryTextColor = Color(255, 255, 255, 230),
                tertiaryTextColor = Color(255, 255, 255, 179),
                accentColor = Color(0xE3, 0xF2, 0xFD),
                quoteMarkColor = Color(255, 255, 255, 128),
                dividerColor = Color(255, 255, 255, 153),
                brandingTextColor = Color.WHITE,
                brandingSubtextColor = Color(255, 255, 255, 179)
            )
            QuoteCardStyle.GRADIENT_FOREST -> StyleColors(
                startColor = Color(0x11, 0x99, 0x8E),
                endColor = Color(0x38, 0xEF, 0x7D),
                textColor = Color.WHITE,
                secondaryTextColor = Color(255, 255, 255, 230),
                tertiaryTextColor = Color(255, 255, 255, 179),
                accentColor = Color(0xE8, 0xF5, 0xE9),
                quoteMarkColor = Color(255, 255, 255, 128),
                dividerColor = Color(255, 255, 255, 153),
                brandingTextColor = Color.WHITE,
                brandingSubtextColor = Color(255, 255, 255, 179)
            )
            QuoteCardStyle.GRADIENT_LAVENDER -> StyleColors(
                startColor = Color(0xE8, 0xD5, 0xE8),
                endColor = Color(0xF3, 0xE5, 0xF5),
                textColor = Color(0x4A, 0x14, 0x8C),
                secondaryTextColor = Color(0x4A, 0x14, 0x8C, 230),
                tertiaryTextColor = Color(0x4A, 0x14, 0x8C, 153),
                accentColor = Color(0x7B, 0x1F, 0xA2),
                quoteMarkColor = Color(0x7B, 0x1F, 0xA2, 96),
                dividerColor = Color(0x7B, 0x1F, 0xA2, 128),
                brandingTextColor = Color(0x4A, 0x14, 0x8C),
                brandingSubtextColor = Color(0x4A, 0x14, 0x8C, 153)
            )
            QuoteCardStyle.GRADIENT_MIDNIGHT -> StyleColors(
                startColor = Color(0x23, 0x25, 0x26),
                endColor = Color(0x41, 0x43, 0x45),
                textColor = Color.WHITE,
                secondaryTextColor = Color(255, 255, 255, 230),
                tertiaryTextColor = Color(255, 255, 255, 179),
                accentColor = Color(0x90, 0xCA, 0xF9),
                quoteMarkColor = Color(0x90, 0xCA, 0xF9, 96),
                dividerColor = Color(0x90, 0xCA, 0xF9, 128),
                brandingTextColor = Color.WHITE,
                brandingSubtextColor = Color(255, 255, 255, 179)
            )
            QuoteCardStyle.MINIMAL_LIGHT -> StyleColors(
                startColor = Color(0xF5, 0xF5, 0xF5),
                endColor = Color.WHITE,
                textColor = Color(0x21, 0x21, 0x21),
                secondaryTextColor = Color(0x21, 0x21, 0x21, 230),
                tertiaryTextColor = Color(0x21, 0x21, 0x21, 153),
                accentColor = Color(0x75, 0x75, 0x75),
                quoteMarkColor = Color(0x75, 0x75, 0x75, 64),
                dividerColor = Color(0x75, 0x75, 0x75, 96),
                brandingTextColor = Color(0x42, 0x42, 0x42),
                brandingSubtextColor = Color(0x42, 0x42, 0x42, 153)
            )
            QuoteCardStyle.MINIMAL_DARK -> StyleColors(
                startColor = Color(0x1E, 0x1E, 0x1E),
                endColor = Color(0x2D, 0x2D, 0x2D),
                textColor = Color(0xE0, 0xE0, 0xE0),
                secondaryTextColor = Color(0xE0, 0xE0, 0xE0, 204),
                tertiaryTextColor = Color(0xE0, 0xE0, 0xE0, 153),
                accentColor = Color(0x9E, 0x9E, 0x9E),
                quoteMarkColor = Color(0x9E, 0x9E, 0x9E, 80),
                dividerColor = Color(0x9E, 0x9E, 0x9E, 112),
                brandingTextColor = Color(0xE0, 0xE0, 0xE0),
                brandingSubtextColor = Color(0xE0, 0xE0, 0xE0, 153)
            )
            QuoteCardStyle.PAPER_TEXTURE -> StyleColors(
                startColor = Color(0xF5, 0xF5, 0xDC),
                endColor = Color(0xFA, 0xF0, 0xE6),
                textColor = Color(0x3E, 0x27, 0x23),
                secondaryTextColor = Color(0x3E, 0x27, 0x23, 230),
                tertiaryTextColor = Color(0x3E, 0x27, 0x23, 153),
                accentColor = Color(0x5D, 0x40, 0x37),
                quoteMarkColor = Color(0x5D, 0x40, 0x37, 80),
                dividerColor = Color(0x5D, 0x40, 0x37, 112),
                brandingTextColor = Color(0x3E, 0x27, 0x23),
                brandingSubtextColor = Color(0x3E, 0x27, 0x23, 153)
            )
            QuoteCardStyle.BOOK_COVER -> StyleColors(
                startColor = Color(0x8D, 0x6E, 0x63),
                endColor = Color(0xA1, 0x88, 0x7F),
                textColor = Color(0xFF, 0xF8, 0xE1),
                secondaryTextColor = Color(0xFF, 0xF8, 0xE1, 230),
                tertiaryTextColor = Color(0xFF, 0xF8, 0xE1, 179),
                accentColor = Color(0xFF, 0xEC, 0xB3),
                quoteMarkColor = Color(0xFF, 0xEC, 0xB3, 96),
                dividerColor = Color(0xFF, 0xEC, 0xB3, 128),
                brandingTextColor = Color(0xFF, 0xF8, 0xE1),
                brandingSubtextColor = Color(0xFF, 0xF8, 0xE1, 179)
            )
        }
    }
    
    data class StyleColors(
        val startColor: Color,
        val endColor: Color,
        val textColor: Color,
        val secondaryTextColor: Color,
        val tertiaryTextColor: Color,
        val accentColor: Color,
        val quoteMarkColor: Color,
        val dividerColor: Color,
        val brandingTextColor: Color,
        val brandingSubtextColor: Color
    )
}

class ImageTransferable(private val image: BufferedImage) : Transferable {
    override fun getTransferDataFlavors(): Array<DataFlavor> = arrayOf(DataFlavor.imageFlavor)
    override fun isDataFlavorSupported(flavor: DataFlavor): Boolean = flavor == DataFlavor.imageFlavor
    override fun getTransferData(flavor: DataFlavor): Any {
        if (!isDataFlavorSupported(flavor)) throw UnsupportedFlavorException(flavor)
        return image
    }
}

@Composable
actual fun rememberQuoteCardSharer(): QuoteCardSharer {
    return remember { QuoteCardSharer() }
}
