package ireader.presentation.ui.readingbuddy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ireader.domain.models.quote.Quote
import ireader.domain.models.quote.QuoteCardStyle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

actual class QuoteCardSharer {
    
    actual suspend fun shareQuoteCard(
        quote: Quote,
        style: QuoteCardStyle,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        withContext(Dispatchers.Main) {
            try {
                val shareText = buildShareText(quote)
                
                val rootViewController = getRootViewController()
                if (rootViewController == null) {
                    onError("Unable to present share sheet")
                    return@withContext
                }
                
                val activityItems = listOf<Any>(shareText)
                
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
                
                // Note: Popover configuration for iPad is handled automatically by UIKit
                
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
    
    private fun buildShareText(quote: Quote): String {
        return buildString {
            append("\"")
            append(quote.text)
            append("\"")
            append("\n\n")
            
            if (quote.author.isNotBlank()) {
                append("— ")
                append(quote.author)
            }
            
            if (quote.bookTitle.isNotBlank()) {
                if (quote.author.isNotBlank()) {
                    append(", ")
                } else {
                    append("— ")
                }
                append(quote.bookTitle)
            }
            
            append("\n\n")
            append("Shared via IReader")
        }
    }
    
    private fun getRootViewController(): UIViewController? {
        val keyWindow = UIApplication.sharedApplication.keyWindow
            ?: UIApplication.sharedApplication.windows.firstOrNull { window ->
                (window as? UIWindow)?.isKeyWindow() == true
            } as? UIWindow
        
        var rootVC = keyWindow?.rootViewController
        while (rootVC?.presentedViewController != null) {
            rootVC = rootVC.presentedViewController
        }
        return rootVC
    }
    
    fun copyToClipboard(quote: Quote): Boolean {
        val text = buildShareText(quote)
        UIPasteboard.generalPasteboard.string = text
        return true
    }
}

@Composable
actual fun rememberQuoteCardSharer(): QuoteCardSharer {
    return remember { QuoteCardSharer() }
}
