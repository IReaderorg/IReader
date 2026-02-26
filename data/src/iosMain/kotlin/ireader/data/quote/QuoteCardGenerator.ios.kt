package ireader.data.quote

import ireader.domain.models.quote.LocalQuote
import ireader.domain.models.quote.QuoteCardStyle
import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*

/**
 * iOS implementation of QuoteCardGenerator using CoreGraphics.
 */
actual class QuoteCardGenerator {
    
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun generateQuoteCard(
        quote: LocalQuote,
        style: QuoteCardStyle
    ): ByteArray = withContext(Dispatchers.Default) {
        val width = 1080.0
        val height = 1920.0
        val size = CGSizeMake(width, height)
        
        UIGraphicsBeginImageContextWithOptions(size, false, 1.0)
        val context = UIGraphicsGetCurrentContext()
        
        if (context != null) {
            // Draw background gradient
            drawBackground(context, width, height, style)
            
            // Draw quote text
            drawQuoteText(quote, width, height, style)
            
            // Get image and convert to PNG
            val image = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
            
            if (image != null) {
                val pngData = UIImagePNGRepresentation(image)
                if (pngData != null) {
                    return@withContext pngData.toByteArray()
                }
            }
        }
        
        // Fallback: return empty PNG
        byteArrayOf()
    }
    
    @OptIn(ExperimentalForeignApi::class)
    private fun drawBackground(context: CGContextRef, width: Double, height: Double, style: QuoteCardStyle) {
        val colors = getGradientColors(style)
        val colorSpace = CGColorSpaceCreateDeviceRGB()
        
        val gradient = CGGradientCreateWithColors(
            colorSpace,
            listOf(colors.first, colors.second),
            null
        )
        
        if (gradient != null) {
            CGContextDrawLinearGradient(
                context,
                gradient,
                CGPointMake(width / 2, 0.0),
                CGPointMake(width / 2, height),
                0u
            )
        }
    }
    
    @OptIn(ExperimentalForeignApi::class)
    private fun drawQuoteText(quote: LocalQuote, width: Double, height: Double, style: QuoteCardStyle) {
        val textColor = getTextColor(style)
        val padding = 80.0
        val maxWidth = width - (padding * 2)
        
        // Quote text
        val quoteText = "\"${quote.text}\""
        val quoteAttributes = mapOf(
            NSFontAttributeName to UIFont.systemFontOfSize(56.0),
            NSForegroundColorAttributeName to textColor,
            NSParagraphStyleAttributeName to NSMutableParagraphStyle().apply {
                alignment = NSTextAlignmentCenter
            }
        )
        
        val quoteString = NSAttributedString.create(quoteText, quoteAttributes)
        val quoteRect = CGRectMake(padding, height / 2 - 200, maxWidth, 400.0)
        quoteString.drawInRect(quoteRect)
        
        // Book title
        val titleAttributes = mapOf(
            NSFontAttributeName to UIFont.boldSystemFontOfSize(42.0),
            NSForegroundColorAttributeName to textColor.colorWithAlphaComponent(0.8),
            NSParagraphStyleAttributeName to NSMutableParagraphStyle().apply {
                alignment = NSTextAlignmentCenter
            }
        )
        
        val titleString = NSAttributedString.create(quote.bookTitle, titleAttributes)
        val titleRect = CGRectMake(padding, height / 2 + 250, maxWidth, 60.0)
        titleString.drawInRect(titleRect)
        
        // Author
        if (!quote.author.isNullOrBlank()) {
            val authorAttributes = mapOf(
                NSFontAttributeName to UIFont.systemFontOfSize(36.0),
                NSForegroundColorAttributeName to textColor.colorWithAlphaComponent(0.6),
                NSParagraphStyleAttributeName to NSMutableParagraphStyle().apply {
                    alignment = NSTextAlignmentCenter
                }
            )
            
            val authorString = NSAttributedString.create("by ${quote.author}", authorAttributes)
            val authorRect = CGRectMake(padding, height / 2 + 320, maxWidth, 50.0)
            authorString.drawInRect(authorRect)
        }
    }
    
    @OptIn(ExperimentalForeignApi::class)
    private fun getGradientColors(style: QuoteCardStyle): Pair<CGColorRef, CGColorRef> {
        return when (style) {
            QuoteCardStyle.GRADIENT_SUNSET -> Pair(
                UIColor(1.0, 0.42, 0.42, 1.0).CGColor,
                UIColor(1.0, 0.9, 0.43, 1.0).CGColor
            )
            QuoteCardStyle.GRADIENT_OCEAN -> Pair(
                UIColor(0.31, 0.8, 0.77, 1.0).CGColor,
                UIColor(0.34, 0.38, 0.44, 1.0).CGColor
            )
            QuoteCardStyle.GRADIENT_FOREST -> Pair(
                UIColor(0.07, 0.31, 0.37, 1.0).CGColor,
                UIColor(0.44, 0.7, 0.5, 1.0).CGColor
            )
            QuoteCardStyle.GRADIENT_LAVENDER -> Pair(
                UIColor(0.85, 0.13, 1.0, 1.0).CGColor,
                UIColor(0.59, 0.2, 0.93, 1.0).CGColor
            )
            QuoteCardStyle.GRADIENT_MIDNIGHT -> Pair(
                UIColor(0.14, 0.15, 0.15, 1.0).CGColor,
                UIColor(0.26, 0.26, 0.27, 1.0).CGColor
            )
            QuoteCardStyle.MINIMAL_LIGHT -> Pair(
                UIColor(0.96, 0.96, 0.96, 1.0).CGColor,
                UIColor(1.0, 1.0, 1.0, 1.0).CGColor
            )
            QuoteCardStyle.MINIMAL_DARK -> Pair(
                UIColor(0.1, 0.1, 0.1, 1.0).CGColor,
                UIColor(0.18, 0.18, 0.18, 1.0).CGColor
            )
            QuoteCardStyle.PAPER_TEXTURE -> Pair(
                UIColor(1.0, 0.97, 0.86, 1.0).CGColor,
                UIColor(0.98, 0.94, 0.9, 1.0).CGColor
            )
            QuoteCardStyle.BOOK_COVER -> Pair(
                UIColor(0.55, 0.27, 0.07, 1.0).CGColor,
                UIColor(0.82, 0.41, 0.12, 1.0).CGColor
            )
        }
    }
    
    private fun getTextColor(style: QuoteCardStyle): UIColor {
        return when (style) {
            QuoteCardStyle.MINIMAL_LIGHT,
            QuoteCardStyle.PAPER_TEXTURE -> UIColor.blackColor
            else -> UIColor.whiteColor
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
}
