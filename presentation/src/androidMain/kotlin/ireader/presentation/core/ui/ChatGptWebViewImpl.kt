package ireader.presentation.core.ui

import android.annotation.SuppressLint
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ireader.domain.usecases.translate.WebscrapingTranslateEngine
import ireader.i18n.localize
import ireader.i18n.resources.MR
import kotlinx.coroutines.delay

private const val TAG = "ChatGptWebViewImpl"

@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun ChatGptWebViewImpl(
    engine: WebscrapingTranslateEngine,
    onTranslationDone: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    
    // State
    var isLoading by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var isCaptchaRequired by remember { mutableStateOf(false) }
    var currentUrl by remember { mutableStateOf("https://chat.openai.com") }
    var messageToSend by remember { mutableStateOf<String?>(null) }
    var progress by remember { mutableStateOf(0f) }
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
                        imageVector = if (canGoBack) Icons.Default.ArrowBack else Icons.Default.Close,
                        contentDescription = if (canGoBack) "Back" else "Close"
                    )
                }
                
                Text(
                    text = if (isCaptchaRequired) 
                        localize(MR.strings.complete_captcha) 
                    else if (isLoggedIn) 
                        localize(MR.strings.chatgpt_translation) 
                    else 
                        localize(MR.strings.sign_in_to_chatgpt),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
                
                // Reload button
                IconButton(
                    onClick = { webView?.reload() },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reload"
                    )
                }
            }
        }
        
        // Loading indicator
        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth()
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
        
        // WebView
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
                            
                            // Set a standard user agent
                            userAgentString = android.webkit.WebSettings.getDefaultUserAgent(context)
                            
                            // Improve performance
                            setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
                            
                            // Enable debugging
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                                WebView.setWebContentsDebuggingEnabled(true)
                            }
                            
                            // Proper viewport handling
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            
                            // Enable zooming if needed
                            builtInZoomControls = true
                            displayZoomControls = false
                        }
                        
                        // Set client
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                android.util.Log.d(TAG, "onPageStarted: $url")
                                isLoading = true
                                currentUrl = url
                                isCaptchaRequired = url.contains("challenge") || url.contains("captcha")
                                canGoBack = view.canGoBack()
                            }
                            
                            override fun onPageFinished(view: WebView, url: String) {
                                super.onPageFinished(view, url)
                                android.util.Log.d(TAG, "onPageFinished: $url")
                                isLoading = false
                                currentUrl = url
                                canGoBack = view.canGoBack()
                                
                                // Check for login state based on URL
                                if (url.contains("chat.openai.com/c/") || url.contains("chat.openai.com/?model=")) {
                                    android.util.Log.d(TAG, "Detected login success based on URL")
                                    if (!isLoggedIn) {
                                        isLoggedIn = true
                                        engine.updateLoginState(WebscrapingTranslateEngine.LoginState.LOGGED_IN)
                                        saveCookies(view, engine)
                                    }
                                    
                                    // Send message to ChatGPT if needed
                                    messageToSend?.let { message ->
                                        android.util.Log.d(TAG, "Sending message to ChatGPT")
                                        sendMessageToChatGPT(view, message)
                                        messageToSend = null
                                    }
                                }
                                
                                // If we're on a CAPTCHA page, update the login state
                                if (isCaptchaRequired) {
                                    android.util.Log.d(TAG, "Detected CAPTCHA page")
                                    engine.updateLoginState(WebscrapingTranslateEngine.LoginState.CAPTCHA_REQUIRED)
                                }
                                
                                // Inject JavaScript to detect ChatGPT responses
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
                                android.util.Log.e(TAG, "onReceivedError: $errorDescription")
                                errorMessage = errorDescription
                                isLoading = false
                            }
                        }
                        
                        // Chrome client for progress
                        webChromeClient = object : android.webkit.WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                super.onProgressChanged(view, newProgress)
                                progress = newProgress / 100f
                            }
                        }
                        
                        // Enable cookies
                        android.webkit.CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
                        android.webkit.CookieManager.getInstance().setAcceptCookie(true)
                        
                        // Set background color
                        setBackgroundColor(android.graphics.Color.WHITE)
                        
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
                        
                        // Set hardware acceleration
                        setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                        
                        // Load the URL
                        loadUrl("https://chat.openai.com")
                    }
                },
                update = { view ->
                    // Update WebView if needed
                    messageToSend?.let { message ->
                        if (isLoggedIn && currentUrl.contains("chat.openai.com")) {
                            android.util.Log.d(TAG, "Update: Sending message to ChatGPT")
                            sendMessageToChatGPT(view, message)
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

// JavaScript interface to capture ChatGPT responses
private class ChatGptJavaScriptInterface(
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
): ChatGptJavaScriptInterface {
    return ChatGptJavaScriptInterface(engine, onTranslationDone)
}

// Inject JavaScript to listen for ChatGPT responses
private fun injectResponseListener(
    webView: WebView, 
    engine: WebscrapingTranslateEngine,
    onTranslationDone: () -> Unit
) {
    val js = """
        (function() {
            console.log('Injecting observer script');
            // Monitor for changes to the DOM
            const observer = new MutationObserver(function(mutations) {
                // Look for new messages from the assistant
                for (let mutation of mutations) {
                    if (mutation.type === 'childList' && mutation.addedNodes.length > 0) {
                        const messages = document.querySelectorAll('[data-message-author-role="assistant"]');
                        if (messages && messages.length > 0) {
                            console.log('Found assistant messages: ' + messages.length);
                            // Get the last message from the assistant
                            const lastMessage = messages[messages.length - 1];
                            if (lastMessage) {
                                const messageText = lastMessage.innerText;
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
            });
            
            // Start observing the entire document for changes
            observer.observe(document.body, { childList: true, subtree: true });
            console.log('Observer script installed');
        })();
    """.trimIndent()
    
    webView.evaluateJavascript(js) { result ->
        android.util.Log.d(TAG, "Injected JavaScript result: $result")
    }
}

// Send a message to ChatGPT via JavaScript
private fun sendMessageToChatGPT(webView: WebView, message: String) {
    val escapedMessage = message.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    android.util.Log.d(TAG, "sendMessageToChatGPT: Preparing to send message")
    val js = """
        (function() {
            console.log('Starting to send message to ChatGPT');
            // Find the textarea
            const textarea = document.querySelector('textarea[placeholder="Message ChatGPT…"]');
            if (textarea) {
                console.log('Found textarea, setting value');
                // Set the value
                const nativeTextAreaValueSetter = Object.getOwnPropertyDescriptor(window.HTMLTextAreaElement.prototype, 'value').set;
                nativeTextAreaValueSetter.call(textarea, "$escapedMessage");
                
                // Create and dispatch the input event
                textarea.dispatchEvent(new Event('input', { bubbles: true }));
                
                // Find and click the send button
                setTimeout(function() {
                    const sendButton = document.querySelector('button[data-testid="send-button"]');
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
        android.util.Log.d(TAG, "sendMessageToChatGPT: JavaScript result: $result")
    }
}

// Save cookies from the WebView
private fun saveCookies(webView: WebView, engine: WebscrapingTranslateEngine) {
    val cookieManager = android.webkit.CookieManager.getInstance()
    val cookies = cookieManager.getCookie("https://chat.openai.com")
    if (!cookies.isNullOrEmpty()) {
        android.util.Log.d(TAG, "saveCookies: Saving cookies")
        engine.saveCookies(cookies)
    } else {
        android.util.Log.w(TAG, "saveCookies: No cookies found to save")
    }
}

// Save cookies from context
private fun saveCookies(context: android.content.Context, engine: WebscrapingTranslateEngine) {
    val cookieManager = android.webkit.CookieManager.getInstance()
    val cookies = cookieManager.getCookie("https://chat.openai.com")
    if (!cookies.isNullOrEmpty()) {
        android.util.Log.d(TAG, "saveCookies: Saving cookies from context")
        engine.saveCookies(cookies)
    } else {
        android.util.Log.w(TAG, "saveCookies: No cookies found to save from context")
    }
}

// Load cookies into WebView
private fun loadCookies(webView: WebView, engine: WebscrapingTranslateEngine) {
    val cookieManager = android.webkit.CookieManager.getInstance()
    cookieManager.removeAllCookies(null)
    
    val cookiePairs = engine.getCookies().split(";")
    android.util.Log.d(TAG, "loadCookies: Loading ${cookiePairs.size} cookie pairs")
    for (cookiePair in cookiePairs) {
        val cookieString = cookiePair.trim()
        if (cookieString.isNotEmpty()) {
            cookieManager.setCookie("https://chat.openai.com", cookieString)
        }
    }
    cookieManager.flush()
} 