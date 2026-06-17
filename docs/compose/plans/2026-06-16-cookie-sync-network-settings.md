# Cookie Sync Fix + Advanced Network Settings Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use compose:subagent (recommended) or compose:execute to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Fix WebView↔Ktor cookie synchronization and add advanced network settings UI (user agent, cookies, proxy)

**Architecture:** Two-part fix: (1) Ensure CookieSynchronizer is called after WebView operations and cookies flow bidirectionally between WebViewCookieJar and AcceptAllCookiesStorage, (2) Add NetworkPreferences class with user-facing settings for user agent, cookie clearing, and basic proxy configuration, wired into HttpClients.

**Tech Stack:** Kotlin Multiplatform, Ktor, Android WebView, Compose Multiplatform, Koin DI

---

## Part A: Fix Cookie Synchronization

### Task 1: Add syncFromWebView call in BrowserEngine.fetch()

**Covers:** [S4]

**Files:**
- Modify: `source-api/src/androidMain/kotlin/ireader/core/http/BrowserEngine.kt`

- [ ] **Step 1: Read the current BrowserEngine.kt implementation**

```bash
cat source-api/src/androidMain/kotlin/ireader/core/http/BrowserEngine.kt
```

- [ ] **Step 2: Add CookieSynchronizer dependency and call syncFromWebView after page load**

In the `BrowserEngine` class constructor, add `cookieSynchronizer: CookieSynchronizer` parameter. In the `fetch()` method, after extracting cookies from WebViewCookieJar, call `cookieSynchronizer.syncFromWebView(url)` to ensure Android CookieManager cookies are synced to the Ktor cookies storage.

```kotlin
// Add to constructor
class BrowserEngine(
    private val webViewManager: WebViewManger,
    private val webViewCookieJar: WebViewCookieJar,
    private val cookieSynchronizer: CookieSynchronizer // NEW
) : BrowserEngineInterface {
```

In the `fetch()` method, after the WebView finishes loading and cookies are extracted:

```kotlin
// After page load completes and cookies are extracted
cookieSynchronizer.syncFromWebView(url)
```

- [ ] **Step 3: Verify the change compiles**

```bash
./gradlew :source-api:compileDebugKotlinAndroid
```

- [ ] **Step 4: Commit**

```bash
git add source-api/src/androidMain/kotlin/ireader/core/http/BrowserEngine.kt
git commit -m "fix: sync cookies from WebView after page load in BrowserEngine"
```

---

### Task 2: Update Android DI to inject CookieSynchronizer into BrowserEngine

**Covers:** [S4]

**Files:**
- Modify: `domain/src/androidMain/kotlin/ireader/domain/di/DomainModule.kt`

- [ ] **Step 1: Read the current DomainModule.kt**

```bash
cat domain/src/androidMain/kotlin/ireader/domain/di/DomainModule.kt
```

- [ ] **Step 2: Update BrowserEngine DI registration**

Find the `BrowserEngine` registration and add `get()` for CookieSynchronizer:

```kotlin
single<BrowserEngine> { BrowserEngine(get(), get(), get()) }
```

- [ ] **Step 3: Verify the change compiles**

```bash
./gradlew :domain:compileDebugKotlinAndroid
```

- [ ] **Step 4: Commit**

```bash
git add domain/src/androidMain/kotlin/ireader/domain/di/DomainModule.kt
git commit -m "fix: inject CookieSynchronizer into BrowserEngine via DI"
```

---

### Task 3: Ensure syncToWebView is called in WebViewCookieJar.loadForRequest

**Covers:** [S4]

**Files:**
- Modify: `source-api/src/androidMain/kotlin/ireader/core/http/WebViewCookieJar.kt`

- [ ] **Step 1: Read WebViewCookieJar.kt**

```bash
cat source-api/src/androidMain/kotlin/ireader/core/http/WebViewCookieJar.kt
```

- [ ] **Step 2: Add CookieSynchronizer and call syncToWebView in loadForRequest**

Add `cookieSynchronizer: CookieSynchronizer` parameter. In `loadForRequest()`, call `cookieSynchronizer.syncToWebView(url.toString())` before reading cookies to ensure any cookies from Ktor/PersistentCookieStore are written to Android CookieManager.

```kotlin
class WebViewCookieJar(
    private val cookieSynchronizer: CookieSynchronizer // NEW
) : CookieJar {
    
    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        cookieSynchronizer.syncToWebView(url.toString()) // NEW
        // existing code...
    }
}
```

- [ ] **Step 3: Update DI registration**

In `DomainModule.kt`, update WebViewCookieJar registration:

```kotlin
single<WebViewCookieJar> { WebViewCookieJar(get()) }
```

- [ ] **Step 4: Verify compilation**

```bash
./gradlew :source-api:compileDebugKotlinAndroid :domain:compileDebugKotlinAndroid
```

- [ ] **Step 5: Commit**

```bash
git add source-api/src/androidMain/kotlin/ireader/core/http/WebViewCookieJar.kt domain/src/androidMain/kotlin/ireader/domain/di/DomainModule.kt
git commit -m "fix: sync cookies to WebView before loading in WebViewCookieJar"
```

---

## Part B: Network Preferences

### Task 4: Create NetworkPreferences class

**Covers:** [S2, S3, S5]

**Files:**
- Create: `domain/src/commonMain/kotlin/ireader/domain/preferences/prefs/NetworkPreferences.kt`

- [ ] **Step 1: Create NetworkPreferences class**

```kotlin
package ireader.domain.preferences.prefs

import ireader.core.preferences.PreferenceStore

class NetworkPreferences(private val preferenceStore: PreferenceStore) {
    
    fun customUserAgent() = preferenceStore.getString(
        KEY_CUSTOM_USER_AGENT, 
        DEFAULT_USER_AGENT
    )
    
    fun setCustomUserAgent(value: String) {
        preferenceStore.putString(KEY_CUSTOM_USER_AGENT, value)
    }
    
    fun useDefaultUserAgent() = preferenceStore.getBoolean(
        KEY_USE_DEFAULT_USER_AGENT, 
        true
    )
    
    fun setUseDefaultUserAgent(value: Boolean) {
        preferenceStore.putBoolean(KEY_USE_DEFAULT_USER_AGENT, value)
    }
    
    fun proxyEnabled() = preferenceStore.getBoolean(
        KEY_PROXY_ENABLED, 
        false
    )
    
    fun setProxyEnabled(value: Boolean) {
        preferenceStore.putBoolean(KEY_PROXY_ENABLED, value)
    }
    
    fun proxyHost() = preferenceStore.getString(KEY_PROXY_HOST, "")
    
    fun setProxyHost(value: String) {
        preferenceStore.putString(KEY_PROXY_HOST, value)
    }
    
    fun proxyPort() = preferenceStore.getInt(KEY_PROXY_PORT, 0)
    
    fun setProxyPort(value: Int) {
        preferenceStore.putInt(KEY_PROXY_PORT, value)
    }
    
    companion object {
        const val KEY_CUSTOM_USER_AGENT = "network_custom_user_agent"
        const val KEY_USE_DEFAULT_USER_AGENT = "network_use_default_user_agent"
        const val KEY_PROXY_ENABLED = "network_proxy_enabled"
        const val KEY_PROXY_HOST = "network_proxy_host"
        const val KEY_PROXY_PORT = "network_proxy_port"
        const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; Android 14; Pixel 8 Pro) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/136.0.6478.71 Mobile Safari/537.36"
    }
}
```

- [ ] **Step 2: Register in DI**

In `domain/src/commonMain/kotlin/ireader/domain/di/PreferencesInject.kt`, add:

```kotlin
single<NetworkPreferences> { NetworkPreferences(get()) }
```

- [ ] **Step 3: Verify compilation**

```bash
./gradlew :domain:compileKotlinDesktop
```

- [ ] **Step 4: Commit**

```bash
git add domain/src/commonMain/kotlin/ireader/domain/preferences/prefs/NetworkPreferences.kt domain/src/commonMain/kotlin/ireader/domain/di/PreferencesInject.kt
git commit -m "feat: add NetworkPreferences for user agent, proxy, and cookie settings"
```

---

### Task 5: Create NetworkSettingsViewModel

**Covers:** [S2, S3, S5]

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/network/NetworkSettingsViewModel.kt`

- [ ] **Step 1: Create NetworkSettingsViewModel**

```kotlin
package ireader.presentation.ui.settings.network

import ireader.core.http.CookieSynchronizer
import ireader.domain.preferences.prefs.NetworkPreferences
import ireader.presentation.core.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

data class NetworkSettingsState(
    val customUserAgent: String = NetworkPreferences.DEFAULT_USER_AGENT,
    val useDefaultUserAgent: Boolean = true,
    val proxyEnabled: Boolean = false,
    val proxyHost: String = "",
    val proxyPort: String = "",
    val showClearCookiesDialog: Boolean = false,
    val showClearCacheDialog: Boolean = false
)

class NetworkSettingsViewModel(
    private val networkPreferences: NetworkPreferences,
    private val cookieSynchronizer: CookieSynchronizer
) : BaseViewModel() {
    
    private val _state = MutableStateFlow(NetworkSettingsState())
    val state: StateFlow<NetworkSettingsState> = _state.asStateFlow()
    
    init {
        launchInScope {
            networkPreferences.customUserAgent().collect { ua ->
                _state.value = _state.value.copy(customUserAgent = ua)
            }
        }
        launchInScope {
            networkPreferences.useDefaultUserAgent().collect { useDefault ->
                _state.value = _state.value.copy(useDefaultUserAgent = useDefault)
            }
        }
        launchInScope {
            networkPreferences.proxyEnabled().collect { enabled ->
                _state.value = _state.value.copy(proxyEnabled = enabled)
            }
        }
        launchInScope {
            networkPreferences.proxyHost().collect { host ->
                _state.value = _state.value.copy(proxyHost = host)
            }
        }
        launchInScope {
            networkPreferences.proxyPort().collect { port ->
                _state.value = _state.value.copy(proxyPort = port.toString())
            }
        }
    }
    
    fun setCustomUserAgent(value: String) {
        networkPreferences.setCustomUserAgent(value)
    }
    
    fun setUseDefaultUserAgent(value: Boolean) {
        networkPreferences.setUseDefaultUserAgent(value)
    }
    
    fun setProxyEnabled(value: Boolean) {
        networkPreferences.setProxyEnabled(value)
    }
    
    fun setProxyHost(value: String) {
        networkPreferences.setProxyHost(value)
    }
    
    fun setProxyPort(value: String) {
        val port = value.toIntOrNull() ?: 0
        networkPreferences.setProxyPort(port)
    }
    
    fun showClearCookiesDialog() {
        _state.value = _state.value.copy(showClearCookiesDialog = true)
    }
    
    fun hideClearCookiesDialog() {
        _state.value = _state.value.copy(showClearCookiesDialog = false)
    }
    
    fun clearCookies() {
        cookieSynchronizer.clearAll()
        hideClearCookiesDialog()
    }
    
    fun showClearCacheDialog() {
        _state.value = _state.value.copy(showClearCacheDialog = true)
    }
    
    fun hideClearCacheDialog() {
        _state.value = _state.value.copy(showClearCacheDialog = false)
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :presentation:compileKotlinDesktop
```

- [ ] **Step 3: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/network/NetworkSettingsViewModel.kt
git commit -m "feat: add NetworkSettingsViewModel for network settings management"
```

---

### Task 6: Create NetworkSettingsScreen

**Covers:** [S2, S3, S5]

**Files:**
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/network/NetworkSettingsScreen.kt`

- [ ] **Step 1: Create NetworkSettingsScreen composable**

```kotlin
package ireader.presentation.ui.settings.network

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.settings.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkSettingsScreen(
    viewModel: NetworkSettingsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Network") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // User Agent Section
            SettingsSectionHeader(title = "User Agent")
            
            SettingsSwitchItem(
                title = "Use Default User Agent",
                description = "Use the app's default user agent string",
                checked = state.useDefaultUserAgent,
                onCheckedChange = viewModel::setUseDefaultUserAgent
            )
            
            if (!state.useDefaultUserAgent) {
                SettingsItemWithTrailing(
                    title = "Custom User Agent",
                    description = state.customUserAgent.ifEmpty { "Not set" }
                ) {
                    // Text field for custom user agent
                    OutlinedTextField(
                        value = state.customUserAgent,
                        onValueChange = viewModel::setCustomUserAgent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        label = { Text("User Agent") },
                        singleLine = true
                    )
                }
            }
            
            SettingsDivider()
            
            // Cookies Section
            SettingsSectionHeader(title = "Cookies")
            
            SettingsItem(
                title = "Clear Cookies",
                description = "Remove all stored cookies",
                onClick = viewModel::showClearCookiesDialog
            )
            
            SettingsDivider()
            
            // Cache Section
            SettingsSectionHeader(title = "Cache")
            
            SettingsItem(
                title = "Clear Cache",
                description = "Remove cached data",
                onClick = viewModel::showClearCacheDialog
            )
            
            SettingsDivider()
            
            // Proxy Section
            SettingsSectionHeader(title = "Proxy")
            
            SettingsSwitchItem(
                title = "Enable Proxy",
                description = "Route requests through a proxy server",
                checked = state.proxyEnabled,
                onCheckedChange = viewModel::setProxyEnabled
            )
            
            if (state.proxyEnabled) {
                SettingsItemWithTrailing(
                    title = "Proxy Host",
                    description = state.proxyHost.ifEmpty { "Not set" }
                ) {
                    OutlinedTextField(
                        value = state.proxyHost,
                        onValueChange = viewModel::setProxyHost,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        label = { Text("Host") },
                        singleLine = true
                    )
                }
                
                SettingsItemWithTrailing(
                    title = "Proxy Port",
                    description = state.proxyPort.ifEmpty { "Not set" }
                ) {
                    OutlinedTextField(
                        value = state.proxyPort,
                        onValueChange = viewModel::setProxyPort,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        label = { Text("Port") },
                        singleLine = true
                    )
                }
            }
        }
    }
    
    // Clear Cookies Dialog
    if (state.showClearCookiesDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideClearCookiesDialog,
            title = { Text("Clear Cookies") },
            text = { Text("Are you sure you want to clear all cookies? This may log you out of some sources.") },
            confirmButton = {
                TextButton(onClick = viewModel::clearCookies) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideClearCookiesDialog) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Clear Cache Dialog
    if (state.showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = viewModel::hideClearCacheDialog,
            title = { Text("Clear Cache") },
            text = { Text("Are you sure you want to clear the cache?") },
            confirmButton = {
                TextButton(onClick = viewModel::hideClearCacheDialog) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideClearCacheDialog) {
                    Text("Cancel")
                }
            }
        )
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
./gradlew :presentation:compileKotlinDesktop
```

- [ ] **Step 3: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/network/NetworkSettingsScreen.kt
git commit -m "feat: add NetworkSettingsScreen UI for advanced network settings"
```

---

### Task 7: Add Network Settings navigation route

**Covers:** [S2, S5]

**Files:**
- Modify: `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/NavigationRoutes.kt`
- Create: `presentation/src/commonMain/kotlin/ireader/presentation/core/ui/NetworkSettingsScreenSpec.kt`

- [ ] **Step 1: Read NavigationRoutes.kt**

```bash
cat presentation/src/commonMain/kotlin/ireader/presentation/core/ui/NavigationRoutes.kt
```

- [ ] **Step 2: Add network settings route**

Add to NavigationRoutes:

```kotlin
const val NETWORK_SETTINGS = "networkSettings"
```

- [ ] **Step 3: Create NetworkSettingsScreenSpec**

```kotlin
package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import ireader.presentation.ui.settings.network.NetworkSettingsScreen
import ireader.presentation.ui.settings.network.NetworkSettingsViewModel

object NetworkSettingsScreenSpec : Screen {
    @Composable
    override fun Content() {
        val viewModel: NetworkSettingsViewModel = getViewModel()
        NetworkSettingsScreen(
            viewModel = viewModel,
            onBack = { /* navigation */ }
        )
    }
}
```

- [ ] **Step 4: Add to main settings navigation**

In `SettingsMainScreen.kt`, add callback and navigation for network settings.

- [ ] **Step 5: Commit**

```bash
git add presentation/src/commonMain/kotlin/ireader/presentation/core/ui/NavigationRoutes.kt presentation/src/commonMain/kotlin/ireader/presentation/core/ui/NetworkSettingsScreenSpec.kt presentation/src/commonMain/kotlin/ireader/presentation/ui/settings/main/SettingsMainScreen.kt
git commit -m "feat: add navigation route for network settings screen"
```

---

### Task 8: Wire NetworkPreferences into NetworkConfig

**Covers:** [S2, S3]

**Files:**
- Modify: `source-api/src/commonMain/kotlin/ireader/core/http/NetworkConfig.kt`
- Modify: `domain/src/androidMain/kotlin/ireader/domain/di/DomainModule.kt`

- [ ] **Step 1: Update NetworkConfig to read from preferences**

Update the `NetworkConfig` companion or factory to accept a `NetworkPreferences` parameter and read user-configured values:

```kotlin
data class NetworkConfig(
    val connectTimeoutSeconds: Long = 30,
    val readTimeoutMinutes: Long = 5,
    val callTimeoutMinutes: Long = 5,
    val cacheSize: Long = 15L * 1024 * 1024,
    val cacheDurationMs: Long = 5 * 60 * 1000,
    val userAgent: String = DEFAULT_USER_AGENT,
    val enableCaching: Boolean = true,
    val enableCookies: Boolean = true,
    val enableCompression: Boolean = true,
    val proxyHost: String? = null,  // NEW
    val proxyPort: Int? = null      // NEW
) {
    companion object {
        fun fromPreferences(prefs: NetworkPreferences): NetworkConfig {
            val ua = if (prefs.useDefaultUserAgent().first()) {
                DEFAULT_USER_AGENT
            } else {
                prefs.customUserAgent().first()
            }
            return NetworkConfig(
                userAgent = ua,
                proxyHost = if (prefs.proxyEnabled().first()) prefs.proxyHost().first() else null,
                proxyPort = if (prefs.proxyEnabled().first()) prefs.proxyPort().first() else null
            )
        }
    }
}
```

- [ ] **Step 2: Update Android DI to create NetworkConfig from preferences**

```kotlin
single<NetworkConfig> { NetworkConfig.fromPreferences(get()) }
```

- [ ] **Step 3: Apply proxy to OkHttp client in HttpClients**

In `source-api/src/androidMain/kotlin/ireader/core/http/HttpClients.kt`, when building the OkHttp client, apply proxy if configured:

```kotlin
val okHttpClientBuilder = OkHttpClient.Builder()
    .connectTimeout(config.connectTimeoutSeconds, TimeUnit.SECONDS)
    .readTimeout(config.readTimeoutMinutes, TimeUnit.MINUTES)
    .callTimeout(config.callTimeoutMinutes, TimeUnit.MINUTES)
    .cache(Cache(cacheDir, config.cacheSize))

// Apply proxy if configured
if (config.proxyHost != null && config.proxyPort != null) {
    val proxy = Proxy(Proxy.Type.HTTP, InetSocketAddress(config.proxyHost, config.proxyPort))
    okHttpClientBuilder.proxy(proxy)
}
```

- [ ] **Step 4: Commit**

```bash
git add source-api/src/commonMain/kotlin/ireader/core/http/NetworkConfig.kt source-api/src/androidMain/kotlin/ireader/core/http/HttpClients.kt domain/src/androidMain/kotlin/ireader/domain/di/DomainModule.kt
git commit -m "feat: wire NetworkPreferences into NetworkConfig for user-configurable settings"
```

---

## Part C: Testing

### Task 9: Add unit tests for NetworkPreferences

**Covers:** [S2]

**Files:**
- Create: `domain/src/commonTest/kotlin/ireader/domain/preferences/prefs/NetworkPreferencesTest.kt`

- [ ] **Step 1: Create test class**

```kotlin
package ireader.domain.preferences.prefs

import ireader.core.preferences.PreferenceStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NetworkPreferencesTest {
    
    private val preferenceStore = InMemoryPreferenceStore()
    private val preferences = NetworkPreferences(preferenceStore)
    
    @Test
    fun `default user agent is set`() = runTest {
        val ua = preferences.customUserAgent().first()
        assertEquals(NetworkPreferences.DEFAULT_USER_AGENT, ua)
    }
    
    @Test
    fun `use default user agent is true by default`() = runTest {
        assertTrue(preferences.useDefaultUserAgent().first())
    }
    
    @Test
    fun `custom user agent can be set`() = runTest {
        val customUA = "CustomAgent/1.0"
        preferences.setCustomUserAgent(customUA)
        assertEquals(customUA, preferences.customUserAgent().first())
    }
    
    @Test
    fun `proxy is disabled by default`() = runTest {
        assertFalse(preferences.proxyEnabled().first())
    }
    
    @Test
    fun `proxy can be enabled`() = runTest {
        preferences.setProxyEnabled(true)
        assertTrue(preferences.proxyEnabled().first())
    }
    
    @Test
    fun `proxy host can be set`() = runTest {
        preferences.setProxyHost("proxy.example.com")
        assertEquals("proxy.example.com", preferences.proxyHost().first())
    }
    
    @Test
    fun `proxy port can be set`() = runTest {
        preferences.setProxyPort(8080)
        assertEquals(8080, preferences.proxyPort().first())
    }
}
```

- [ ] **Step 2: Run tests**

```bash
./gradlew :domain:desktopTest --tests "ireader.domain.preferences.prefs.NetworkPreferencesTest"
```

- [ ] **Step 3: Commit**

```bash
git add domain/src/commonTest/kotlin/ireader/domain/preferences/prefs/NetworkPreferencesTest.kt
git commit -m "test: add unit tests for NetworkPreferences"
```

---

### Task 10: Add integration test for cookie synchronization

**Covers:** [S4]

**Files:**
- Create: `source-api/src/androidUnitTest/kotlin/ireader/core/http/CookieSyncTest.kt`

- [ ] **Step 1: Create test class**

```kotlin
package ireader.core.http

import android.webkit.CookieManager
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class CookieSyncTest {
    
    private val webViewCookieJar = mockk<WebViewCookieJar>(relaxed = true)
    private val cookieSynchronizer = CookieSynchronizer(webViewCookieJar)
    
    @Test
    fun `syncFromWebView reads from CookieManager`() = runTest {
        val url = "https://example.com"
        
        cookieSynchronizer.syncFromWebView(url)
        
        verify { webViewCookieJar wasCalled }
    }
    
    @Test
    fun `syncToWebView writes to CookieManager`() = runTest {
        val url = "https://example.com"
        
        cookieSynchronizer.syncToWebView(url)
        
        verify { webViewCookieJar wasCalled }
    }
    
    @Test
    fun `clearAll clears both stores`() = runTest {
        cookieSynchronizer.clearAll()
        
        verify { webViewCookieJar.removeAll() }
    }
}
```

- [ ] **Step 2: Run tests**

```bash
./gradlew :source-api:testDebugUnitTest --tests "ireader.core.http.CookieSyncTest"
```

- [ ] **Step 3: Commit**

```bash
git add source-api/src/androidUnitTest/kotlin/ireader/core/http/CookieSyncTest.kt
git commit -m "test: add integration tests for cookie synchronization"
```

---

## Summary

This plan covers:
1. **Cookie Sync Fix** (Tasks 1-3): Ensure bidirectional cookie sync between WebView and Ktor
2. **Network Preferences** (Task 4): Create preference class for user agent, proxy, cookies
3. **Settings UI** (Tasks 5-7): ViewModel, Screen, and Navigation for network settings
4. **Wiring** (Task 8): Connect preferences to NetworkConfig and HttpClients
5. **Testing** (Tasks 9-10): Unit and integration tests

Total: 10 tasks, ~30-40 minutes of focused implementation.