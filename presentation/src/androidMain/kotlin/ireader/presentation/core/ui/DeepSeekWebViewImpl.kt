package ireader.presentation.core.ui

import android.annotation.SuppressLint
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ireader.presentation.ui.core.theme.currentOrThrow
import ireader.domain.usecases.translate.WebscrapingTranslateEngine
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.delay
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

private const val TAG = "DeepSeekWebViewImpl"
private const val DEEPSEEK_URL = "https://chat.deepseek.com"

@SuppressLint("SetJavaScriptEnabled")
@Suppress("CyclomaticComplexMethod", "LongMethod")
@Composable
actual fun DeepSeekWebViewImpl(
    engine: WebscrapingTranslateEngine,
    onTranslationDone: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    // State - using primitive state variants for better performance
    var isLoading by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }
    val isCaptchaRequired = remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf(DEEPSEEK_URL) }
    var messageToSend by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableFloatStateOf(0f) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var canGoBack by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    
    // Check login state when the component is first loaded
    LaunchedEffect(Unit) {
        android.util.Log.d(TAG, "LaunchedEffect: Checking login state")
        val cookies = engine.getCookies()
        if (cookies.isNotEmpty()) {
            android.util.Log.d(TAG, "Cookies found, considering user logged in")
            isLoggedIn = true
            engine.updateLoginState(WebscrapingTranslateEngine.LoginState.LOGGED_IN)
        } else {
            android.util.Log.d(TAG, "No cookies found, user is logged out")
            engine.updateLoginState(WebscrapingTranslateEngine.LoginState.LOGGED_OUT)
        }
    }
    
    // Check for translation in progress
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn && engine.translationInProgress.value) {
            android.util.Log.d(TAG, "Translation in progress, preparing to send message")
            delay(1000) // Give the WebView time to fully initialize
            
            // Get the prompt from preferences
            val prompt = engine.readerPreferences.chatGptPrompt().get()
            if (prompt.isNotEmpty()) {
                android.util.Log.d(TAG, "Found prompt in preferences, will send it")
                messageToSend = prompt
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
        // Top bar
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                IconButton(
                    onClick = { 
                        if (canGoBack && webView?.canGoBack() == true) {
                            webView?.goBack()
                        } else {
                            onClose()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = if (canGoBack) Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                        contentDescription = if (canGoBack) "Back" else "Close"
                    )
                }
                
                Text(
                    text = if (isCaptchaRequired.value)
                        localize(Res.string.complete_captcha) 
                    else if (isLoggedIn) 
                        "DeepSeek Translation" 
                    else 
                        "Sign in to DeepSeek",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                // Show refresh button
                IconButton(
                    onClick = { 
                        if (isCaptchaRequired.value && webView != null) {
                            // Just reload the page for manual verification
                            webView?.reload()
                        } else {
                            webView?.reload() 
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = localizeHelper.localize(Res.string.reload)
                    )
                }
            }
        }
        
        // Loading indicator
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        }
        
        // Error message
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        // WebView - make sure it fills the remaining space but doesn't overlap the app bar
        Box(modifier = Modifier.weight(1f)) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        webView = this
                        
                        // WebView settings
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            loadsImagesAutomatically = true
                            
                            // Set a standard user agent - use a desktop Chrome agent to ensure full page rendering
                            userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/118.0.0.0 Safari/537.36"
                            
                            // Additional settings for better usability
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            loadWithOverviewMode = true
                            useWideViewPort = true
                            
                            // Performance improvements
                            setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
                            
                            // Additional browser settings for better website compatibility
                            javaScriptCanOpenWindowsAutomatically = true
                            // Accept all content types
                            blockNetworkImage = false
                            mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            
                            // Enable debugging
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                WebView.setWebContentsDebuggingEnabled(true)
                            }
                        }
                        
                        // Scrollbar settings
                        isVerticalScrollBarEnabled = true
                        isHorizontalScrollBarEnabled = false
                        
                        // Set hardware acceleration
                        setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                        
                        // Force desktop mode
                        setDesktopMode(webView!!)
                        
                        // Set client
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                android.util.Log.d(TAG, "onPageStarted: $url")
                                isLoading = true
                                currentUrl = url
                                
                                // Check for Cloudflare challenge right away
                                checkForCloudflare(url, view, isCaptchaRequired, engine)
                                
                                canGoBack = view.canGoBack()
                                
                                // Clear any previous error message
                                errorMessage = null
                            }
                            
                            override fun onPageFinished(view: WebView, url: String) {
                                super.onPageFinished(view, url)
                                android.util.Log.d(TAG, "onPageFinished: $url")
                                isLoading = false
                                currentUrl = url
                                canGoBack = view.canGoBack()
                                
                                // Force content display
                                val forceDisplayScript = """
                                    (function() {
                                        console.log('Forcing content display...');
                                        // Hide any skip-to-content buttons
                                        var skipLinks = document.querySelectorAll('a[href="#content"], .skip-to-content, [aria-label=localizeHelper.localize(Res.string.skip_to_content)]');
                                        skipLinks.forEach(function(link) {
                                            link.style.display = 'none';
                                        });
                                        
                                        // Force display of main content
                                        var mainContent = document.querySelector('main, [role="main"], #__next, [class*="content"]');
                                        if (mainContent) {
                                            mainContent.style.display = 'block';
                                            mainContent.style.visibility = 'visible';
                                            mainContent.style.opacity = '1';
                                            console.log('Found and showed main content');
                                        }
                                        
                                        // Force all sections to be visible
                                        var allSections = document.querySelectorAll('section, [class*="section"], .main-content');
                                        allSections.forEach(function(section) {
                                            section.style.display = 'block';
                                            section.style.visibility = 'visible';
                                            section.style.opacity = '1';
                                        });
                                        
                                        // Force viewport to be correct
                                        var meta = document.querySelector('meta[name="viewport"]');
                                        if (meta) {
                                            meta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=yes';
                                        } else {
                                            var newMeta = document.createElement('meta');
                                            newMeta.name = 'viewport';
                                            newMeta.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=yes';
                                            document.head.appendChild(newMeta);
                                        }
                                        
                                        // Remove any loading screens
                                        var loadingElements = document.querySelectorAll('[class*="loading"], [class*="spinner"], [aria-busy="true"]');
                                        loadingElements.forEach(function(el) {
                                            el.style.display = 'none';
                                        });
                                    })();
                                """.trimIndent()
                                
                                view.evaluateJavascript(forceDisplayScript) { result ->
                                    android.util.Log.d(TAG, "Force display script result: $result")
                                }
                                
                                // Check for Cloudflare challenge
                                checkForCloudflare(url, view, isCaptchaRequired, engine)
                                
                                // Check for login state based on URL
                                if (url.contains("chat.deepseek.com") && !url.contains("/login")) {
                                    android.util.Log.d(TAG, "Detected login success based on URL")
                                    if (!isLoggedIn) {
                                        isLoggedIn = true
                                        engine.updateLoginState(WebscrapingTranslateEngine.LoginState.LOGGED_IN)
                                        saveCookies(view, engine)
                                    }
                                    
                                    // Also handle skip to content button here
                                    handleSkipToContentButton(view)
                                    
                                    // Send message to DeepSeek if needed
                                    messageToSend?.let { message ->
                                        android.util.Log.d(TAG, "Sending message to DeepSeek")
                                        sendMessageToDeepSeek(view, message)
                                        messageToSend = null
                                    }
                                }
                                
                                // Inject JavaScript to detect DeepSeek responses
                                injectResponseListener(view, engine, onTranslationDone)
                            }
                            
                            override fun onReceivedError(
                                view: WebView, 
                                request: WebResourceRequest, 
                                error: WebResourceError
                            ) {
                                super.onReceivedError(view, request, error)
                                val errorDescription = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                                    error.description?.toString() ?: "Unknown error"
                                } else {
                                    "Error loading page"
                                }
                                android.util.Log.d(TAG, "onReceivedError: $errorDescription")
                                errorMessage = errorDescription
                                isLoading = false
                            }
                            
                            // Don't override URL loading - let the WebView handle navigation normally
                            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                                return false // Return false to allow WebView to load the URL
                            }
                        }
                        
                        // Chrome client for progress
                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                progress = newProgress / 100f
                            }
                        }
                        
                        // Enable cookies
                        val cookieManager = android.webkit.CookieManager.getInstance()
                        cookieManager.setAcceptThirdPartyCookies(this, true)
                        cookieManager.setAcceptCookie(true)
                        
                        // Cache settings
                        settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                        
                        // Add JavaScript interface for catching responses
                        addJavascriptInterface(
                            createJavaScriptInterface(engine, onTranslationDone),
                            "Android"
                        )
                        
                        // Load cookies if we have them
                        if (engine.getCookies().isNotEmpty()) {
                            android.util.Log.d(TAG, "Loading cookies into WebView")
                            loadCookies(this, engine)
                        }
                        
                        // Load the URL
                        loadUrl(DEEPSEEK_URL)
                    }
                },
                update = { view ->
                    // Update WebView if needed
                    messageToSend?.let { message ->
                        if (isLoggedIn && currentUrl.contains("chat.deepseek.com")) {
                            android.util.Log.d(TAG, "Update: Sending message to DeepSeek")
                            sendMessageToDeepSeek(view, message)
                            messageToSend = null
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    // Clean up resources when the component is disposed
    DisposableEffect(Unit) {
        android.util.Log.d(TAG, "DisposableEffect: Setting up cleanup")
        onDispose {
            if (isLoggedIn) {
                // Save cookies before closing
                android.util.Log.d(TAG, "Saving cookies before component disposal")
                saveCookies(context, engine)
            }
        }
    }
}

// JavaScript interface to capture DeepSeek responses
private class DeepSeekJavaScriptInterface(
    private val engine: WebscrapingTranslateEngine,
    private val onTranslationDone: () -> Unit
) {
    @android.webkit.JavascriptInterface
    fun processResponse(response: String) {
        android.util.Log.d(TAG, "JavascriptInterface: processResponse - received response")
        if (response.isNotEmpty()) {
            // Update the last message in the translation engine
            engine.updateLastRawMessage(response)
            android.util.Log.d(TAG, "JavascriptInterface: calling onTranslationDone")
            onTranslationDone()
        } else {
            android.util.Log.w(TAG, "JavascriptInterface: Empty response received")
        }
    }
}

// Create JavaScript interface
private fun createJavaScriptInterface(
    engine: WebscrapingTranslateEngine,
    onTranslationDone: () -> Unit
): DeepSeekJavaScriptInterface {
    return DeepSeekJavaScriptInterface(engine, onTranslationDone)
}

// Inject JavaScript to listen for DeepSeek responses
@Suppress("UNUSED_PARAMETER")
private fun injectResponseListener(
    webView: WebView, 
    engine: WebscrapingTranslateEngine,
    onTranslationDone: () -> Unit
) {
    val js = """
        (function() {
            console.log('Injecting observer script for DeepSeek');
            // Monitor for changes to the DOM
            const observer = new MutationObserver(function(mutations) {
                // Look for new messages from the assistant
                for (let mutation of mutations) {
                    if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
                        // DeepSeek uses different selectors than ChatGPT
                        const messages = document.querySelectorAll('.message-virtual-list .message-item.assistant');
                        if (messages && messages.length > 0) {
                            console.log('Found assistant messages: ' + messages.length);
                            // Get the last message from the assistant
                            const lastMessage = messages[messages.length - 1];
                            if (lastMessage) {
                                const messageContainer = lastMessage.querySelector('.message-content');
                                if (messageContainer) {
                                    const messageText = messageContainer.innerText;
                                    console.log('Processing assistant message');
                                    // Send the message to Android
                                    if (messageText && messageText.trim() !== '') {
                                        console.log('Sending message to Android interface');
                                        Android.processResponse(messageText);
                                    }
                                }
                            }
                        }
                    }
                }
            });
            
            // Start observing the entire document for changes
            observer.observe(document.body, { childList: true, subtree: true });
            console.log('Observer script installed for DeepSeek');
        })();
    """.trimIndent()
    
    webView.evaluateJavascript(js) { result ->
        android.util.Log.d(TAG, "Injected JavaScript result: $result")
    }
}

// Send a message to DeepSeek via JavaScript
private fun sendMessageToDeepSeek(webView: WebView, message: String) {
    val escapedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    android.util.Log.d(TAG, "sendMessageToDeepSeek: Preparing to send message")
    val js = """
        (function() {
            console.log('Starting to send message to DeepSeek');
            
            // DeepSeek has a different UI than ChatGPT
            // First, try to find the textarea
            const textarea = document.querySelector('.chat-input textarea');
            if (textarea) {
                console.log('Found textarea, setting value');
                // Set the value
                const nativeTextAreaValueSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value').set;
                nativeTextAreaValueSetter.call(textarea, "$escapedMessage");
                
                // Create and dispatch the input event
                textarea.dispatchEvent(new Event('input', { bubbles: true }));
                
                // Find and click the send button
                setTimeout(function() {
                    // DeepSeek has different button selectors
                    const sendButton = document.querySelector('.chat-input button.send-button') || 
                                      document.querySelector('.chat-input-actions button[type="submit"]') ||
                                      document.querySelector('.chat-input-container button:last-child');
                    
                    if (sendButton) {
                        console.log('Found send button, clicking it');
                        sendButton.click();
                    } else {
                        console.log('Send button not found');
                    }
                }, 500);
            } else {
                console.log('Textarea not found');
            }
        })();
    """.trimIndent()
    
    webView.evaluateJavascript(js) { result ->
        android.util.Log.d(TAG, "sendMessageToDeepSeek: JavaScript result: $result")
    }
}

// Helper function to check for Cloudflare challenges
private fun checkForCloudflare(url: String, view: WebView, isCaptchaRequired: MutableState<Boolean>, engine: WebscrapingTranslateEngine) {
    val cloudflareIndicators = listOf(
        "cloudflare", "challenge", "captcha", "cf_chl", "ray-id", "turnstile", 
        "security check", "cf-", "cf_challenge", "managed_checking", "checking_browser"
    )
    
    val isCloudflare = cloudflareIndicators.any { url.contains(it, ignoreCase = true) } || 
                       url.contains("cdn-cgi", ignoreCase = true)
    
    if (isCloudflare) {
        android.util.Log.d(TAG, "Detected Cloudflare challenge page: $url")
        isCaptchaRequired.value = true
        
        // Update login state for CAPTCHA required
        engine.updateLoginState(WebscrapingTranslateEngine.LoginState.CAPTCHA_REQUIRED)
        
        // Add instructions to help user complete verification manually
        view.evaluateJavascript("""
            (function() {
                // Check if we already added instructions
                if (document.getElementById('manual-verification-notice')) {
                    return;
                }
                
                // Create a simple notice to guide the user
                var notice = document.createElement('div');
                notice.id = 'manual-verification-notice';
                notice.style.cssText = 'position: fixed; top: 10px; right: 10px; z-index: 9999; background: rgba(0,0,0,0.7); color: white; padding: 10px; border-radius: 5px; font-size: 14px; max-width: 220px;';
                notice.innerHTML = 'Please complete the Cloudflare verification manually. This will only be required once.';
                
                // Add it to the page
                document.body.appendChild(notice);
                
                // Remove it after some time
                setTimeout(function() {
                    if (notice.parentNode) {
                        notice.parentNode.removeChild(notice);
                    }
                }, 8000);
            })();
        """) { result ->
            android.util.Log.d(TAG, "Manual verification notice result: $result")
        }
    } else {
        isCaptchaRequired.value = false
        
        // Check for "Skip to content" button and handle it
        handleSkipToContentButton(view)
    }
}

// Function to handle "Skip to content" button
@Suppress("LongMethod")
private fun handleSkipToContentButton(webView: WebView) {
    val js = """
        (function() {
            console.log('Checking for Skip to content button...');
            
            // Function to find and handle skip links
            function handleSkipLinks() {
                // Find the skip to content button
                const skipButtons = document.querySelectorAll('a[href="#skip-to-content"], a[href="#content"], a:contains("Skip to content")');
                
                if (skipButtons.length > 0) {
                    console.log('Found Skip to content button');
                    
                    // First approach: click the button
                    skipButtons.forEach(button => {
                        console.log('Clicking skip button');
                        button.click();
                    });
                    
                    // Second approach: directly scroll to content area
                    const contentArea = document.querySelector('#skip-to-content, #content, main, [role="main"], .main-content');
                    if (contentArea) {
                        console.log('Found content area, scrolling to it');
                        contentArea.scrollIntoView();
                        
                        // Try to focus on the content area
                        contentArea.focus();
                        
                        // Simulate click on the content area
                        contentArea.click();
                    }
                    
                    // Third approach: hide the skip button as it might be just a visual element
                    skipButtons.forEach(button => {
                        button.style.display = 'none';
                    });
                    
                    return true;
                }
                
                // Look for elements that might be blocking the view
                const possibleOverlays = document.querySelectorAll('.overlay, [role="dialog"], [class*="modal"], [class*="cookie"], [class*="popup"], [class*="banner"]');
                possibleOverlays.forEach(overlay => {
                    if (overlay.style.display !== 'none' && overlay.offsetParent !== null) {
                        console.log('Found possible overlay, trying to hide it');
                        overlay.style.display = 'none';
                    }
                });
                
                return false;
            }
            
            // Initial check
            const result = handleSkipLinks();
            
            // If we didn't find anything, try again after a short delay
            if (!result) {
                setTimeout(handleSkipLinks, 1000);
                setTimeout(handleSkipLinks, 2500);
            }
        })();
    """.trimIndent()
    
    webView.evaluateJavascript(js) { result ->
        android.util.Log.d(TAG, "Skip to content handler result: $result")
    }
}

// Helper function to assist with Cloudflare challenges
@Suppress("UNUSED_PARAMETER")
private fun injectCloudflareHelper(webView: WebView) {
    // Do nothing - let the user complete the verification manually
    android.util.Log.d(TAG, "Letting user complete Cloudflare verification manually")
}

// Set desktop mode for the WebView
private fun setDesktopMode(webView: WebView) {
    val settings = webView.settings
    
    // Force desktop mode
    settings.userAgentString = settings.userAgentString.replace("Mobile", "Desktop")
    settings.useWideViewPort = true
    settings.loadWithOverviewMode = true
    settings.setSupportZoom(true)
    settings.builtInZoomControls = true
    settings.displayZoomControls = false
    
    // Enhance rendering
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        settings.offscreenPreRaster = true
    }
    
    // Inject CSS to ensure full page visibility
    val cssInjection = """
        (function() {
            var style = document.createElement('style');
            style.type = 'text/css';
            style.innerHTML = `
                body, html { 
                    width: 100% !important;
                    height: auto !important;
                    overflow-x: hidden !important;
                    overflow-y: auto !important;
                    display: block !important;
                    visibility: visible !important;
                }
                #__next, main, [class*="container"] {
                    display: block !important;
                    width: 100% !important;
                    visibility: visible !important;
                    opacity: 1 !important;
                }
                [class*="MobileNav"], [class*="mobile-nav"], .skip-to-content { 
                    display: none !important; 
                }
            `;
            document.head.appendChild(style);
            console.log('Desktop mode CSS injected');
        })();
    """.trimIndent()
    
    webView.evaluateJavascript(cssInjection) { result ->
        android.util.Log.d(TAG, "CSS injection result: $result")
    }
}

// Save cookies from the WebView
@Suppress("UNUSED_PARAMETER")
private fun saveCookies(webView: WebView, engine: WebscrapingTranslateEngine) {
    val cookieManager = android.webkit.CookieManager.getInstance()
    val cookies = cookieManager.getCookie(DEEPSEEK_URL)
    if (!cookies.isNullOrEmpty()) {
        android.util.Log.d(TAG, "saveCookies: Saving cookies")
        engine.saveCookies(cookies)
    } else {
        android.util.Log.w(TAG, "saveCookies: No cookies found to save")
    }
}

// Save cookies from context
@Suppress("UNUSED_PARAMETER")
private fun saveCookies(context: android.content.Context, engine: WebscrapingTranslateEngine) {
    val cookieManager = android.webkit.CookieManager.getInstance()
    val cookies = cookieManager.getCookie(DEEPSEEK_URL)
    if (!cookies.isNullOrEmpty()) {
        android.util.Log.d(TAG, "saveCookies: Saving cookies from context")
        engine.saveCookies(cookies)
    } else {
        android.util.Log.w(TAG, "saveCookies: No cookies found to save from context")
    }
}

// Load cookies into WebView
@Suppress("UNUSED_PARAMETER")
private fun loadCookies(webView: WebView, engine: WebscrapingTranslateEngine) {
    val cookieManager = android.webkit.CookieManager.getInstance()
    cookieManager.removeAllCookies(null)
    
    val cookiePairs = engine.getCookies().split(";")
    android.util.Log.d(TAG, "loadCookies: Loading ${cookiePairs.size} cookie pairs")
    for (cookiePair in cookiePairs) {
        val cookieString = cookiePair.trim()
        if (cookieString.isNotEmpty()) {
            cookieManager.setCookie(DEEPSEEK_URL, cookieString)
        }
    }
    cookieManager.flush()
}
