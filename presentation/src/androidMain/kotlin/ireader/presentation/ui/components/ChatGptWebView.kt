package ireader.presentation.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import ireader.core.log.Log
import ireader.domain.usecases.translate.WebscrapingTranslateEngine
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import android.os.Build
import androidx.compose.foundation.layout.systemBarsPadding
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

private const val TAG = "ChatGptWebView"

// State holder for ChatGptWebView
@Stable
private class ChatGptWebViewState(context: Context) {
    var isLoading by mutableStateOf(true)
    var isLoggedIn by mutableStateOf(false)
    var isCaptchaRequired by mutableStateOf(false)
    var currentUrl by mutableStateOf("https://chat.openai.com")
    var messageToSend by mutableStateOf<String?>(null)
    var progress by mutableFloatStateOf(0f)
    var errorMessage by mutableStateOf<String?>(null)
    var loadTimeoutReached by mutableStateOf(false)
    var debugMessage by mutableStateOf<String?>(null)
    var retryCount by mutableIntStateOf(0)
    var currentFallbackUrlIndex by mutableIntStateOf(0)
    
    val fallbackUrls = listOf(
        "https://chat.openai.com",
        "https://www.google.com", 
        "https://example.com"
    )
    
    val urlToLoad: String
        get() = fallbackUrls[currentFallbackUrlIndex]
    
    val webViewAvailable = isWebViewAvailable(context)
    
    fun nextFallbackUrl(): Boolean {
        return if (currentFallbackUrlIndex < fallbackUrls.size - 1) {
            currentFallbackUrlIndex++
            true
        } else {
            false
        }
    }
    
    fun retry() {
        loadTimeoutReached = false
        errorMessage = null
        retryCount++
        debugMessage = "Retrying..."
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewUnavailableContent(
    onClose: () -> Unit,
    localizeHelper: ireader.i18n.LocalizeHelper
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = localizeHelper.localize(Res.string.webview_is_not_available_on_this_device),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            Button(
                onClick = onClose,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(localizeHelper.localize(Res.string.go_back))
            }
        }
    }
}

@Composable
fun ChatGptWebView(
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    engine: WebscrapingTranslateEngine = koinInject(),
    onTranslationComplete: (() -> Unit)? = null,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val context = LocalContext.current
    val webViewState = remember { ChatGptWebViewState(context) }
    
    // Check if WebView is available
    if (!webViewState.webViewAvailable) {
        WebViewUnavailableContent(onClose, localizeHelper)
        return
    }
    
    ChatGptWebViewContent(
        webViewState = webViewState,
        engine = engine,
        context = context,
        localizeHelper = localizeHelper,
        onClose = onClose,
        onTranslationComplete = onTranslationComplete,
        modifier = modifier
    )
}

@Composable
private fun ChatGptWebViewContent(
    webViewState: ChatGptWebViewState,
    engine: WebscrapingTranslateEngine,
    context: Context,
    localizeHelper: ireader.i18n.LocalizeHelper,
    onClose: () -> Unit,
    onTranslationComplete: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    // Setup effects
    ChatGptWebViewEffects(webViewState, engine)
    
    Column(modifier = modifier.fillMaxSize().systemBarsPadding()) {
        // Top bar
        ChatGptWebViewTopBar(
            webViewState = webViewState,
            localizeHelper = localizeHelper,
            engine = engine,
            onClose = onClose
        )
        
        // Status and debug messages
        ChatGptWebViewStatusMessages(webViewState, localizeHelper)
        
        // Main WebView content
        ChatGptWebViewMain(
            webViewState = webViewState,
            engine = engine,
            context = context,
            onTranslationComplete = onTranslationComplete
        )
    }
}

@Composable
private fun ChatGptWebViewEffects(
    webViewState: ChatGptWebViewState,
    engine: WebscrapingTranslateEngine
) {
    // Check login state and cookies when the component is first loaded
    LaunchedEffect(Unit) {
        android.util.Log.d(TAG, "LaunchedEffect: Initializing ChatGptWebView")
        val cookies = engine.getCookies()
        if (cookies.isNotEmpty()) {
            android.util.Log.d(TAG, "Cookies found, considering user logged in")
            webViewState.isLoggedIn = true
            engine.updateLoginState(WebscrapingTranslateEngine.LoginState.LOGGED_IN)
        } else {
            android.util.Log.d(TAG, "No cookies found, user is logged out")
            engine.updateLoginState(WebscrapingTranslateEngine.LoginState.LOGGED_OUT)
        }
    }
    
    // Check for translation in progress
    LaunchedEffect(webViewState.isLoggedIn) {
        if (webViewState.isLoggedIn && engine.translationInProgress.value) {
            android.util.Log.d(TAG, "Translation in progress, preparing to send message")
            delay(1000) // Give the WebView time to fully initialize
            
            // Get the prompt from preferences
            val prompt = engine.readerPreferences.chatGptPrompt().get()
            if (prompt.isNotEmpty()) {
                android.util.Log.d(TAG, "Found prompt in preferences, will send it")
                webViewState.messageToSend = prompt
            }
        }
    }
    
    // Timeout for loading
    LaunchedEffect(webViewState.isLoading) {
        if (webViewState.isLoading) {
            delay(10000)
            if (webViewState.isLoading) {
                webViewState.loadTimeoutReached = true
                webViewState.errorMessage = "Page failed to load. Please check your connection or try again later."
                
                // Try a fallback URL if timeout is reached
                if (webViewState.nextFallbackUrl()) {
                    webViewState.debugMessage = "Trying fallback URL: ${webViewState.urlToLoad}"
                    webViewState.retryCount++
                    webViewState.loadTimeoutReached = false
                }
            }
        } else {
            webViewState.loadTimeoutReached = false
        }
    }
}
@Composable
private fun ChatGptWebViewTopBar(
    webViewState: ChatGptWebViewState,
    localizeHelper: ireader.i18n.LocalizeHelper,
    engine: WebscrapingTranslateEngine,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = localizeHelper.localize(Res.string.back)
            )
        }
        
        Text(
            text = if (webViewState.isCaptchaRequired) 
                localize(Res.string.complete_captcha) 
            else if (webViewState.isLoggedIn) 
                localize(Res.string.chatgpt_translation) 
            else 
                localize(Res.string.sign_in_to_chatgpt),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Center)
        )
        
        if (webViewState.isLoggedIn) {
            Button(
                onClick = {
                    engine.clearLoginData()
                    webViewState.isLoggedIn = false
                },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Text(localize(Res.string.logout))
            }
        }
    }
}

@Composable
private fun ChatGptWebViewStatusMessages(
    webViewState: ChatGptWebViewState,
    localizeHelper: ireader.i18n.LocalizeHelper
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Debug message - shows current URL being loaded
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .wrapContentHeight()
        ) {
            Text(
                text = "Loading: ${webViewState.urlToLoad} (Attempt: ${webViewState.retryCount + 1})",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.Center)
            )
        }
        
        // Error message
        if (webViewState.errorMessage != null || webViewState.loadTimeoutReached) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = webViewState.errorMessage ?: "Page failed to load. Please check your connection or try again later.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp)
                )
            }
        }
        
        // Debug message
        webViewState.debugMessage?.let { message ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = message,
                    color = Color.Blue,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp)
                )
            }
        }
        
        // CAPTCHA alert
        if (webViewState.isCaptchaRequired) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = localize(Res.string.complete_captcha),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(8.dp)
                )
            }
        }
        
        // Progress indicator
        if (webViewState.isLoading) {
            LinearProgressIndicator(
                progress = { webViewState.progress },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Retry buttons
        if (webViewState.loadTimeoutReached) {
            ChatGptWebViewRetryButtons(webViewState, localizeHelper)
        }
    }
}

@Composable
private fun ChatGptWebViewRetryButtons(
    webViewState: ChatGptWebViewState,
    localizeHelper: ireader.i18n.LocalizeHelper
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { webViewState.retry() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(localizeHelper.localize(Res.string.retry_loading))
        }
        
        // Try alternative URL button
        if (webViewState.currentFallbackUrlIndex < webViewState.fallbackUrls.size - 1) {
            Button(
                onClick = {
                    if (webViewState.nextFallbackUrl()) {
                        webViewState.loadTimeoutReached = false
                        webViewState.errorMessage = null
                        webViewState.retryCount = 0
                        webViewState.debugMessage = "Trying alternative URL: ${webViewState.urlToLoad}"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(localizeHelper.localize(Res.string.try_alternative_url))
            }
        }
    }
}
@Composable
private fun ChatGptWebViewMain(
    webViewState: ChatGptWebViewState,
    engine: WebscrapingTranslateEngine,
    context: Context,
    onTranslationComplete: (() -> Unit)?
) {
    // Force hardware acceleration for the entire activity
    LaunchedEffect(Unit) {
        try {
            val activity = context as android.app.Activity
            activity.window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                android.view.WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
            android.util.Log.d(TAG, "Hardware acceleration enabled for activity")
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Failed to enable hardware acceleration", e)
        }
    }
    
    // WebView - take most of the screen space
    Box(modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight()) {
        // WebView
        AndroidView(
            factory = { ctx ->
                android.util.Log.d(TAG, "Creating WebView with URL: ${webViewState.urlToLoad}")
                createWebView(ctx, engine).apply {
                    webViewClient = createWebViewClient(webViewState, engine)
                    
                    // Debug console messages from the WebView
                    setWebChromeClient(createWebChromeClient(webViewState))
                    
                    // Add JavaScript interface to capture ChatGPT responses
                    addJavascriptInterface(
                        createJavaScriptInterface(engine) {
                            android.util.Log.d(TAG, "Translation response received, calling onTranslationComplete")
                            onTranslationComplete?.invoke()
                        },
                        "Android"
                    )
                    
                    // Load cookies if we have them
                    if (engine.getCookies().isNotEmpty()) {
                        android.util.Log.d(TAG, "Loading cookies into WebView")
                        loadCookies(engine.getCookies())
                    }
                    
                    // Ensure that the WebView has a background color
                    setBackgroundColor(android.graphics.Color.WHITE)
                    
                    // Set hardware acceleration explicitly
                    setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                    
                    // Load the selected URL
                    android.util.Log.d(TAG, "Loading URL: ${webViewState.urlToLoad}")
                    loadUrl(webViewState.urlToLoad)
                }
            },
            update = { webView ->
                // Update WebView if needed
                if (webViewState.urlToLoad != webView.url) {
                    android.util.Log.d(TAG, "Updating WebView to load: ${webViewState.urlToLoad} (current: ${webView.url})")
                    webView.loadUrl(webViewState.urlToLoad)
                }
                
                webViewState.messageToSend?.let { message ->
                    if (webViewState.isLoggedIn && webViewState.currentUrl.contains("chat.openai.com")) {
                        android.util.Log.d(TAG, "Update: Sending message to ChatGPT")
                        webView.sendMessageToChatGPT(message)
                        webViewState.messageToSend = null
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
    
    // Clean up resources when the component is disposed
    DisposableEffect(Unit) {
        android.util.Log.d(TAG, "DisposableEffect: Cleaning up resources")
        onDispose {
            if (webViewState.isLoggedIn) {
                // Save cookies before closing
                android.util.Log.d(TAG, "Saving cookies before component disposal")
                saveCookies(context, engine)
            }
        }
    }
}

// Check if WebView is available on the device
private fun isWebViewAvailable(context: Context): Boolean {
    return try {
        // Check if WebView is installed
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                "com.google.android.webview", 
                android.content.pm.PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo("com.google.android.webview", 0)
        }
        
        android.util.Log.d(TAG, "WebView is available: ${packageInfo != null}")
        packageInfo != null
    } catch (e: Exception) {
        android.util.Log.e(TAG, "Error checking WebView availability", e)
        // Try to instantiate a WebView as a fallback check
        try {
            val webView = WebView(context)
            webView.destroy()
            true
        } catch (e: Exception) {
            android.util.Log.e(TAG, "WebView instantiation failed", e)
            false
        }
    }
}

// Create WebView with appropriate settings
private fun createWebView(context: Context, engine: WebscrapingTranslateEngine): WebView {
    android.util.Log.d(TAG, "Creating WebView with enhanced settings")
    return WebView(context).apply {
        // Enable hardware acceleration
        setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
        
        // Make sure hardware acceleration is enabled
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
        }
        
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadsImagesAutomatically = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            
            // Set a standard user agent (avoid custom user agent that might be blocked)
            userAgentString = WebSettings.getDefaultUserAgent(context)
            
            // Add additional settings to improve WebView performance
            setRenderPriority(WebSettings.RenderPriority.HIGH)
            cacheMode = WebSettings.LOAD_DEFAULT
            
            // Enable debugging for WebView
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                WebView.setWebContentsDebuggingEnabled(true)
            }
            
            // Improve scrolling
            setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY)
            
            // Ensure viewports work properly
            useWideViewPort = true
            loadWithOverviewMode = true
            
            // Enable accessible content
            textZoom = 100

            
            // Disable zoom controls
            displayZoomControls = false
            
            // Enable zooming
            builtInZoomControls = true
            
            // Important for some sites
            allowContentAccess = true
            allowFileAccess = true
            
            // Improve site rendering on Android 5+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
            }
        }
        
        // Enable cookies
        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)
        CookieManager.getInstance().setAcceptCookie(true)
        
        // Make sure WebView is visible with a white background
        setBackgroundColor(android.graphics.Color.WHITE)
        
        // Try to make WebView more responsive
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setScrollbarFadingEnabled(true)
            overScrollMode = WebView.OVER_SCROLL_NEVER
        }
    }
}

// Create WebViewClient to handle page loading and navigation
private fun createWebViewClient(
    webViewState: ChatGptWebViewState,
    engine: WebscrapingTranslateEngine
): WebViewClient {
    return object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            android.util.Log.d(TAG, "WebViewClient: onPageStarted - $url")
            webViewState.currentUrl = url
            webViewState.isLoading = true
            webViewState.isCaptchaRequired = url.contains("challenge") || url.contains("captcha")
            webViewState.errorMessage = null
        }
        
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            android.util.Log.d(TAG, "WebViewClient: onPageFinished - $url")
            webViewState.currentUrl = url
            webViewState.isLoading = false
            webViewState.isCaptchaRequired = url.contains("challenge") || url.contains("captcha")
            webViewState.progress = 1f
            
            // Check if we're logged in
            if (url.contains("chat.openai.com/c/") || url.contains("chat.openai.com/?model=")) {
                android.util.Log.d(TAG, "Detected login success based on URL")
                if (!webViewState.isLoggedIn) {
                    webViewState.isLoggedIn = true
                    engine.updateLoginState(WebscrapingTranslateEngine.LoginState.LOGGED_IN)
                    view.saveCookies(engine)
                }
                
                // Send message to ChatGPT if needed
                webViewState.messageToSend?.let { message ->
                    android.util.Log.d(TAG, "Sending message to ChatGPT")
                    view.sendMessageToChatGPT(message)
                    webViewState.messageToSend = null
                }
            }
            
            // If we're on a CAPTCHA page, update the login state
            if (webViewState.isCaptchaRequired) {
                android.util.Log.d(TAG, "Detected CAPTCHA page")
                engine.updateLoginState(WebscrapingTranslateEngine.LoginState.CAPTCHA_REQUIRED)
            }
            
            // Inject JavaScript to detect ChatGPT responses
            injectResponseDetectionScript(view)
        }
        
        override fun onReceivedError(
            view: WebView?,
            request: WebResourceRequest?,
            error: android.webkit.WebResourceError?
        ) {
            super.onReceivedError(view, request, error)
            val errorCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) error?.errorCode else -1
            val errorDescription = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) error?.description?.toString() else "Unknown error"
            android.util.Log.e(TAG, "WebViewClient: onReceivedError - code=$errorCode, description=$errorDescription")
            webViewState.errorMessage = errorDescription
            webViewState.debugMessage = "Error: $errorDescription"
        }
        
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val url = request.url.toString()
            android.util.Log.d(TAG, "WebViewClient: shouldOverrideUrlLoading - $url")
            return if (isChatGptRelatedUrl(url)) {
                false // Let WebView handle ChatGPT and auth URLs
            } else {
                // Handle external URLs if needed
                android.util.Log.d(TAG, "External URL detected, not loading: $url")
                true
            }
        }
    }
}

// Create WebChromeClient for progress reporting
private fun createWebChromeClient(webViewState: ChatGptWebViewState): android.webkit.WebChromeClient {
    return object : android.webkit.WebChromeClient() {
        override fun onConsoleMessage(consoleMessage: android.webkit.ConsoleMessage): Boolean {
            android.util.Log.d(
                "WebViewConsole", 
                "${consoleMessage.message()} -- From line ${consoleMessage.lineNumber()} of ${consoleMessage.sourceId()}"
            )
            return true
        }
        
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            webViewState.progress = newProgress / 100f
            android.util.Log.d(TAG, "Loading progress: $newProgress%")
        }
    }
}

// Check if URL is related to ChatGPT
private fun isChatGptRelatedUrl(url: String): Boolean {
    return url.startsWith("https://chat.openai.com") || 
           url.contains("auth0.openai.com") || 
           url.contains("accounts.google.com") || 
           url.contains("login.microsoftonline.com") ||
           url.contains("cloudflare") ||
           url.contains("challenge")
}

// Inject JavaScript to detect ChatGPT responses
private fun injectResponseDetectionScript(view: WebView) {
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
    
    view.evaluateJavascript(js) { result ->
        android.util.Log.d(TAG, "Injected JavaScript result: $result")
    }
}

// JavaScript interface to capture ChatGPT responses
private class ChatGptJavaScriptInterface(
    private val engine: WebscrapingTranslateEngine,
    private val onTranslationDone: () -> Unit
) {
    @JavascriptInterface
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

// Send a message to ChatGPT
private fun WebView.sendMessageToChatGPT(message: String) {
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
    
    evaluateJavascript(js) { result ->
        android.util.Log.d(TAG, "sendMessageToChatGPT: JavaScript result: $result")
    }
}

// Save cookies
private fun saveCookies(context: Context, engine: WebscrapingTranslateEngine) {
    val cookieManager = CookieManager.getInstance()
    val cookies = cookieManager.getCookie("https://chat.openai.com")
    if (!cookies.isNullOrEmpty()) {
        android.util.Log.d(TAG, "saveCookies: Saving cookies")
        engine.saveCookies(cookies)
    } else {
        android.util.Log.w(TAG, "saveCookies: No cookies found to save")
    }
}

// Save cookies from WebView
private fun WebView.saveCookies(engine: WebscrapingTranslateEngine) {
    val cookieManager = CookieManager.getInstance()
    val cookies = cookieManager.getCookie("https://chat.openai.com")
    if (!cookies.isNullOrEmpty()) {
        android.util.Log.d(TAG, "WebView.saveCookies: Saving cookies")
        engine.saveCookies(cookies)
    } else {
        android.util.Log.w(TAG, "WebView.saveCookies: No cookies found to save")
    }
}

// Load cookies into WebView
private fun WebView.loadCookies(cookies: String) {
    val cookieManager = CookieManager.getInstance()
    cookieManager.removeAllCookies(null)
    
    val cookiePairs = cookies.split(";")
    android.util.Log.d(TAG, "loadCookies: Loading ${cookiePairs.size} cookie pairs")
    for (cookiePair in cookiePairs) {
        val cookieString = cookiePair.trim()
        if (cookieString.isNotEmpty()) {
            cookieManager.setCookie("https://chat.openai.com", cookieString)
        }
    }
    cookieManager.flush()
} 