package ireader.core.http

import okio.IOException

// Exception classes for HTTP and WebView operations
// Using okio.IOException for KMP compatibility
class OutOfDateWebView : IOException("WebView is outdated. Please update WebView from Play Store")
class NeedWebView : IOException("WebView is required but not available on this device")
class CloudflareBypassFailed : IOException("Cloudflare bypass failed")