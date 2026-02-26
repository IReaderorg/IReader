package ireader.data.quote

import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import ireader.domain.models.quote.QuoteCardStyleColors
import kotlinx.cinterop.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*

/**
 * iOS implementation of QuoteCardGenerator using CoreGraphics
 */
class IosQuoteCardGenerator : QuoteCardGenerator {
    
    @OptIn(ExperimentalForeignApi::class)
    override suspend fun generateQuoteCard(quote: LocalQuote, style: QuoteCardStyle): ByteArray {
        val width = 1080.0
        val height = 1920.0
        
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
            val colors = listOf(startColor, endColor)
            val locations = doubleArrayOf(0.0, 1.0)
            
            memScoped {
                val gradient = CGGradientCreateWithColors(
                    colorSpace,
                    CFArrayCreate(null, colors.map { it.objcPtr() }.toCValues(), 2, null),
                    locations.toCValues()
                )
                
                CGContextDrawLinearGradient(
                    context,
                    gradient,
                    CGPointMake(0.0, 0.0),
                    CGPointMake(0.0, height),
                    0u
                )
            }
            
            // Calculate center Y position
            val centerY = height / 2.0
            
            // Draw IReader logo text at top center
            val logoText = "IReader" as NSString
            val logoFont = UIFont.boldSystemFontOfSize(42.0)
            val logoAttrs = mapOf(
                NSFontAttributeName to logoFont,
                NSForegroundColorAttributeName to textColor.colorWithAlphaComponent(0.9)
            )
            val logoSize = logoText.sizeWithAttributes(logoAttrs)
            logoText.drawAtPoint(
                CGPointMake((width - logoSize.useContents { width }) / 2.0, centerY - 400.0),
                withAttributes = logoAttrs
            )
            
            // Draw quote mark icon (decorative, centered)
            val quoteMarkText = "\"" as NSString
            val quoteMarkFont = UIFont.boldSystemFontOfSize(120.0)
            val quoteMarkAttrs = mapOf(
                NSFontAttributeName to quoteMarkFont,
                NSForegroundColorAttributeName to textColor.colorWithAlphaComponent(0.3)
            )
            val quoteMarkSize = quoteMarkText.sizeWithAttributes(quoteMarkAttrs)
            quoteMarkText.drawAtPoint(
                CGPointMake((width - quoteMarkSize.useContents { width }) / 2.0, centerY - 300.0),
                withAttributes = quoteMarkAttrs
            )
            
            // Draw quote text (centered, italic)
            val quoteText = "\"${quote.text}\"" as NSString
            val textFont = UIFont.italicSystemFontOfSize(56.0)
            val paragraphStyle = NSMutableParagraphStyle().apply {
                alignment = NSTextAlignmentCenter
                lineSpacing = 14.0
            }
            val textAttrs = mapOf(
                NSFontAttributeName to textFont,
                NSForegroundColorAttributeName to textColor,
                NSParagraphStyleAttributeName to paragraphStyle
            )
            val textRect = CGRectMake(80.0, centerY - 200.0, width - 160.0, 400.0)
            quoteText.drawInRect(textRect, withAttributes = textAttrs)
            
            // Draw book title (centered, bold)
            val bookText = quote.bookTitle as NSString
            val bookFont = UIFont.boldSystemFontOfSize(48.0)
            val bookAttrs = mapOf(
                NSFontAttributeName to bookFont,
                NSForegroundColorAttributeName to textColor.colorWithAlphaComponent(0.9)
            )
            val bookSize = bookText.sizeWithAttributes(bookAttrs)
            bookText.drawAtPoint(
                CGPointMake((width - bookSize.useContents { width }) / 2.0, centerY + 250.0),
                withAttributes = bookAttrs
            )
            
            // Draw author (centered)
            if (!quote.author.isNullOrBlank()) {
                val authorText = "by ${quote.author}" as NSString
                val authorFont = UIFont.systemFontOfSize(40.0)
                val authorAttrs = mapOf(
                    NSFontAttributeName to authorFont,
                    NSForegroundColorAttributeName to textColor.colorWithAlphaComponent(0.7)
                )
                val authorSize = authorText.sizeWithAttributes(authorAttrs)
                authorText.drawAtPoint(
                    CGPointMake((width - authorSize.useContents { width }) / 2.0, centerY + 310.0),
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
