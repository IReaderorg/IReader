package ireader.data.quote

import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.domain.models.quote.QuoteCardStyleColors
import ireader.domain.models.quote.QuoteCardConstants
import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*
import platform.posix.memcpy

/**
 * iOS implementation of QuoteCardGenerator using CoreGraphics
 */
class IOSQuoteCardGenerator : QuoteCardGenerator {
    
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun generateQuoteCard(quote: LocalQuote, style: QuoteCardStyle): ByteArray {
        val width = QuoteCardConstants.IMAGE_WIDTH.toDouble()
        val height = QuoteCardConstants.IMAGE_HEIGHT.toDouble()
        
        // Create bitmap context
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        val context = CGBitmapContextCreate(
            null,
            width.toULong(),
            height.toULong(),
            8u,
            0u,
            colorSpace,
            CGImageAlphaInfo.kCGImageAlphaPremultipliedLast.value
        ) ?: throw Exception("Failed to create bitmap context")
        
        try {
            // Get gradient colors and text color for style
            val (startColor, endColor) = getGradientColors(style)
            val textColor = getTextColor(style)
            
            // Draw gradient background
            memScoped {
                val colorArray = allocArrayOf(startColor.CGColor, endColor.CGColor)
                val cfArray = CFArrayCreate(
                    null,
                    colorArray.reinterpret(),
                    2,
                    null
                )
                
                val locations = allocArrayOf(0.0, 1.0)
                val gradient = CGGradientCreateWithColors(
                    colorSpace,
                    cfArray,
                    locations
                )
                
                CGContextDrawLinearGradient(
                    context,
                    gradient,
                    CGPointMake(0.0, 0.0),
                    CGPointMake(0.0, height),
                    0u
                )
                
                CFRelease(cfArray)
                CGGradientRelease(gradient)
            }
            
            // Calculate center Y position
            val centerY = height / 2.0
            
            // Draw IReader logo text at top center
            val logoText = "IReader" as NSString
            val logoFont = UIFont.boldSystemFontOfSize(QuoteCardConstants.AUTHOR_TEXT_SIZE.toDouble())
            val logoAttrs = mapOf<Any?, Any?>(
                NSFontAttributeName to logoFont,
                NSForegroundColorAttributeName to textColor.colorWithAlphaComponent(0.9)
            )
            val logoSize = logoText.sizeWithAttributes(logoAttrs)
            logoText.drawAtPoint(
                CGPointMake((width - logoSize.useContents { this.width }) / 2.0, centerY + QuoteCardConstants.LOGO_OFFSET_Y),
                withAttributes = logoAttrs
            )
            
            // Draw quote mark icon (decorative, centered)
            val quoteMarkText = "\"" as NSString
            val quoteMarkFont = UIFont.boldSystemFontOfSize(120.0)
            val quoteMarkAttrs = mapOf<Any?, Any?>(
                NSFontAttributeName to quoteMarkFont,
                NSForegroundColorAttributeName to textColor.colorWithAlphaComponent(0.3)
            )
            val quoteMarkSize = quoteMarkText.sizeWithAttributes(quoteMarkAttrs)
            quoteMarkText.drawAtPoint(
                CGPointMake((width - quoteMarkSize.useContents { this.width }) / 2.0, centerY + QuoteCardConstants.QUOTE_MARK_OFFSET_Y),
                withAttributes = quoteMarkAttrs
            )
            
            // Draw quote text (centered, italic)
            val quoteText = "\"${quote.text}\"" as NSString
            val textFont = UIFont.italicSystemFontOfSize(QuoteCardConstants.LOGO_TEXT_SIZE.toDouble())
            val paragraphStyle = NSMutableParagraphStyle()
            paragraphStyle.alignment = NSTextAlignmentCenter
            paragraphStyle.lineSpacing = 14.0
            
            val textAttrs = mapOf<Any?, Any?>(
                NSFontAttributeName to textFont,
                NSForegroundColorAttributeName to textColor,
                NSParagraphStyleAttributeName to paragraphStyle
            )
            val textRect = CGRectMake(
                QuoteCardConstants.HORIZONTAL_MARGIN.toDouble(), 
                centerY - 200.0, 
                width - (QuoteCardConstants.HORIZONTAL_MARGIN * 2).toDouble(), 
                400.0
            )
            quoteText.drawInRect(textRect, withAttributes = textAttrs)
            
            // Draw book title (centered, bold)
            val bookText = quote.bookTitle as NSString
            val bookFont = UIFont.boldSystemFontOfSize(QuoteCardConstants.BOOK_TITLE_SIZE.toDouble())
            val bookAttrs = mapOf<Any?, Any?>(
                NSFontAttributeName to bookFont,
                NSForegroundColorAttributeName to textColor.colorWithAlphaComponent(0.9)
            )
            val bookSize = bookText.sizeWithAttributes(bookAttrs)
            bookText.drawAtPoint(
                CGPointMake((width - bookSize.useContents { this.width }) / 2.0, centerY + QuoteCardConstants.BOOK_TITLE_OFFSET_Y),
                withAttributes = bookAttrs
            )
            
            // Draw author (centered)
            if (!quote.author.isNullOrBlank()) {
                val authorText = "by ${quote.author}" as NSString
                val authorFont = UIFont.systemFontOfSize(QuoteCardConstants.AUTHOR_TEXT_SIZE.toDouble())
                val authorAttrs = mapOf<Any?, Any?>(
                    NSFontAttributeName to authorFont,
                    NSForegroundColorAttributeName to textColor.colorWithAlphaComponent(0.7)
                )
                val authorSize = authorText.sizeWithAttributes(authorAttrs)
                authorText.drawAtPoint(
                    CGPointMake((width - authorSize.useContents { this.width }) / 2.0, centerY + QuoteCardConstants.AUTHOR_OFFSET_Y),
                    withAttributes = authorAttrs
                )
            }
            
            // Create image from context
            val cgImage = CGBitmapContextCreateImage(context)
                ?: throw Exception("Failed to create image from context")
            
            val uiImage = UIImage.imageWithCGImage(cgImage)
            val pngData = UIImagePNGRepresentation(uiImage)
                ?: throw Exception("Failed to convert image to PNG")
            
            // Convert NSData to ByteArray
            return pngData.toByteArray()
            
        } catch (e: Exception) {
            throw Exception("Failed to generate quote card: ${e.message}", e)
        } finally {
            CGContextRelease(context)
            CGColorSpaceRelease(colorSpace)
        }
    }
    
    @OptIn(ExperimentalForeignApi::class)
    private fun NSData.toByteArray(): ByteArray {
        return ByteArray(length.toInt()).apply {
            usePinned {
                memcpy(it.addressOf(0), bytes, length)
            }
        }
    }
    
    private fun getTextColor(style: QuoteCardStyle): UIColor {
        val composeColor = QuoteCardStyleColors.getTextColor(style)
        return UIColor(
            red = composeColor.red.toDouble(),
            green = composeColor.green.toDouble(),
            blue = composeColor.blue.toDouble(),
            alpha = composeColor.alpha.toDouble()
        )
    }
    
    /**
     * Retrieves the gradient colors for the specified quote card style.
     * 
     * Converts Compose Color objects from QuoteCardStyleColors to iOS's native
     * UIColor objects for use with CoreGraphics gradient drawing.
     * 
     * @param style The quote card style (OCEAN, SUNSET, FOREST, etc.)
     * @return Pair of UIColor objects (startColor, endColor) for gradient
     */
    private fun getGradientColors(style: QuoteCardStyle): Pair<UIColor, UIColor> {
        val (startCompose, endCompose) = QuoteCardStyleColors.getGradientColors(style)
        
        val startColor = UIColor(
            red = startCompose.red.toDouble(),
            green = startCompose.green.toDouble(),
            blue = startCompose.blue.toDouble(),
            alpha = startCompose.alpha.toDouble()
        )
        
        val endColor = UIColor(
            red = endCompose.red.toDouble(),
            green = endCompose.green.toDouble(),
            blue = endCompose.blue.toDouble(),
            alpha = endCompose.alpha.toDouble()
        )
        
        return Pair(startColor, endColor)
    }
}
