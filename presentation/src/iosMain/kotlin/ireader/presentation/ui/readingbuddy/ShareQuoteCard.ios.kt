package ireader.presentation.ui.readingbuddy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle
import platform.UIKit.*
import platform.Foundation.*
import platform.CoreGraphics.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*

/**
 * iOS implementation of QuoteCardSharer
 * 
 * Uses UIActivityViewController for native iOS sharing
 */
@OptIn(ExperimentalForeignApi::class)
actual class QuoteCardSharer {
    
    /**
     * Share a quote card
     * 
     * On iOS, this uses UIActivityViewController to share the quote text.
     * Image generation would require rendering to a UIImage.
     */
    actual suspend fun shareQuoteCard(
        quote: Quote,
        style: QuoteCardStyle,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            try {
                // Build share text
                val shareText = buildShareText(quote, style)
                
                // Get the root view controller
                val rootViewController = getRootViewController()
                if (rootViewController == null) {
                    onError("Unable to present share sheet")
                    return@withContext
                }
                
                // Create activity items
                val activityItems = listOf<Any>(shareText)
                
                // Create activity view controller
                val activityVC = UIActivityViewController(
                    activityItems = activityItems,
                    applicationActivities = null
                )
                
                // Set completion handler
                activityVC.completionWithItemsHandler = { activityType, completed, returnedItems, error ->
                    if (error != null) {
                        onError(error.localizedDescription)
                    } else if (completed) {
                        onSuccess()
                    }
                }
                
                // Configure for iPad (popover presentation)
                activityVC.popoverPresentationController?.let { popover ->
                    popover.sourceView = rootViewController.view
                    popover.sourceRect = CGRectMake(
                        rootViewController.view.bounds.useContents { size.width / 2 },
                        rootViewController.view.bounds.useContents { size.height / 2 },
                        0.0,
                        0.0
                    )
                    popover.permittedArrowDirections = UIPopoverArrowDirectionAny
                }
                
                // Present the share sheet
                rootViewController.presentViewController(
                    activityVC,
                    animated = true,
                    completion = null
                )
                
            } catch (e: Exception) {
                onError("Failed to share: ${e.message}")
            }
        }
    }
    
    /**
     * Build the share text from quote and style
     */
    private fun buildShareText(quote: Quote, style: QuoteCardStyle): String {
        return buildString {
            // Quote text with quotation marks
            append("\"")
            append(quote.text)
            append("\"")
            append("\n\n")
            
            // Attribution
            if (quote.author.isNotBlank()) {
                append("— ")
                append(quote.author)
            }
            
            if (quote.source.isNotBlank()) {
                if (quote.author.isNotBlank()) {
                    append(", ")
                } else {
                    append("— ")
                }
                append(quote.source)
            }
            
            // Add chapter info if available
            if (quote.chapter.isNotBlank()) {
                append("\n")
                append("Chapter: ")
                append(quote.chapter)
            }
            
            // Add app attribution
            append("\n\n")
            append("Shared via IReader")
        }
    }
    
    /**
     * Get the root view controller for presenting the share sheet
     */
    private fun getRootViewController(): UIViewController? {
        val keyWindow = UIApplication.sharedApplication.keyWindow
            ?: UIApplication.sharedApplication.windows.firstOrNull { 
                (it as? UIWindow)?.isKeyWindow == true 
            } as? UIWindow
        
        var rootVC = keyWindow?.rootViewController
        
        // Find the topmost presented view controller
        while (rootVC?.presentedViewController != null) {
            rootVC = rootVC.presentedViewController
        }
        
        return rootVC
    }
    
    /**
     * Share quote as image (advanced implementation)
     * 
     * This would render the quote card to a UIImage and share it.
     * Requires more complex implementation with Core Graphics.
     */
    suspend fun shareQuoteCardAsImage(
        quote: Quote,
        style: QuoteCardStyle,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            try {
                // Create image from quote card
                val image = renderQuoteCardToImage(quote, style)
                
                if (image == null) {
                    // Fallback to text sharing
                    shareQuoteCard(quote, style, onSuccess, onError)
                    return@withContext
                }
                
                val rootViewController = getRootViewController()
                if (rootViewController == null) {
                    onError("Unable to present share sheet")
                    return@withContext
                }
                
                // Share the image
                val activityItems = listOf<Any>(image, buildShareText(quote, style))
                val activityVC = UIActivityViewController(
                    activityItems = activityItems,
                    applicationActivities = null
                )
                
                activityVC.completionWithItemsHandler = { _, completed, _, error ->
                    if (error != null) {
                        onError(error.localizedDescription)
                    } else if (completed) {
                        onSuccess()
                    }
                }
                
                activityVC.popoverPresentationController?.let { popover ->
                    popover.sourceView = rootViewController.view
                    popover.sourceRect = CGRectMake(
                        rootViewController.view.bounds.useContents { size.width / 2 },
                        rootViewController.view.bounds.useContents { size.height / 2 },
                        0.0,
                        0.0
                    )
                }
                
                rootViewController.presentViewController(activityVC, animated = true, completion = null)
                
            } catch (e: Exception) {
                onError("Failed to share image: ${e.message}")
            }
        }
    }
    
    /**
     * Render quote card to UIImage
     * 
     * This is a simplified implementation. A full implementation would
     * use Core Graphics to render the styled quote card.
     */
    private fun renderQuoteCardToImage(quote: Quote, style: QuoteCardStyle): UIImage? {
        // Image dimensions
        val width = 600.0
        val height = 400.0
        
        // Create graphics context
        UIGraphicsBeginImageContextWithOptions(
            CGSizeMake(width, height),
            opaque = true,
            scale = 0.0 // Use screen scale
        )
        
        val context = UIGraphicsGetCurrentContext() ?: run {
            UIGraphicsEndImageContext()
            return null
        }
        
        // Draw background
        val backgroundColor = UIColor.systemBackgroundColor
        backgroundColor.setFill()
        UIRectFill(CGRectMake(0.0, 0.0, width, height))
        
        // Draw quote text
        val quoteText = "\"${quote.text}\""
        val quoteAttributes = mapOf<Any?, Any?>(
            NSFontAttributeName to UIFont.systemFontOfSize(18.0),
            NSForegroundColorAttributeName to UIColor.labelColor
        )
        
        val quoteRect = CGRectMake(40.0, 40.0, width - 80.0, height - 120.0)
        (quoteText as NSString).drawInRect(quoteRect, withAttributes = quoteAttributes)
        
        // Draw attribution
        val attribution = "— ${quote.author}"
        val attrAttributes = mapOf<Any?, Any?>(
            NSFontAttributeName to UIFont.italicSystemFontOfSize(14.0),
            NSForegroundColorAttributeName to UIColor.secondaryLabelColor
        )
        
        val attrRect = CGRectMake(40.0, height - 60.0, width - 80.0, 40.0)
        (attribution as NSString).drawInRect(attrRect, withAttributes = attrAttributes)
        
        // Get the image
        val image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        return image
    }
    
    /**
     * Copy quote text to clipboard
     */
    fun copyToClipboard(quote: Quote): Boolean {
        val text = buildShareText(quote, QuoteCardStyle())
        UIPasteboard.generalPasteboard.string = text
        return true
    }
}

@Composable
actual fun rememberQuoteCardSharer(): QuoteCardSharer {
    return remember { QuoteCardSharer() }
}
