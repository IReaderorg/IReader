# iOS Recommended Dependencies

This document outlines recommended dependencies to improve iOS compatibility and functionality.

## Summary

The project has a **solid KMP foundation** for iOS. Most critical dependencies are already in place. The main recommendations are:

1. **High Priority:** Add `multiplatform-settings` for unified preferences
2. **Medium Priority:** Align Ktor versions (currently mixed 3.3.2 and 3.3.3)
3. **Low Priority:** Consider SKIE for better Swift interop if building native iOS app

## Current iOS Dependencies

The project already has good KMP foundation:
- ✅ Ktor Darwin engine for networking
- ✅ SQLDelight Native driver for database
- ✅ Ksoup for HTML parsing (KMP alternative to Jsoup)
- ✅ Okio for file I/O
- ✅ kotlinx-datetime for date/time
- ✅ kotlinx-serialization for JSON
- ✅ Koin for dependency injection
- ✅ Coil for image loading

## Recommended Additions

### 1. Multiplatform Settings (High Priority)

**Purpose:** Persistent key-value storage (replacement for SharedPreferences/UserDefaults)

```toml
# gradle/libs.versions.toml
[versions]
multiplatformSettings = "1.2.0"

[libraries]
multiplatform-settings = { module = "com.russhwolf:multiplatform-settings", version.ref = "multiplatformSettings" }
multiplatform-settings-coroutines = { module = "com.russhwolf:multiplatform-settings-coroutines", version.ref = "multiplatformSettings" }
multiplatform-settings-serialization = { module = "com.russhwolf:multiplatform-settings-serialization", version.ref = "multiplatformSettings" }
```

**Benefits:**
- Unified API for preferences across platforms
- Coroutines support with Flow
- Serialization support for complex objects
- Uses NSUserDefaults on iOS

---

### 2. Krypto (Medium Priority)

**Purpose:** Cryptographic operations (hashing, encryption)

```toml
[versions]
krypto = "4.0.10"

[libraries]
krypto = { module = "com.soywiz.korlibs.krypto:krypto", version.ref = "krypto" }
```

**Benefits:**
- MD5, SHA-1, SHA-256, SHA-512 hashing
- AES encryption/decryption
- HMAC support
- Pure Kotlin implementation

---

### 3. Stately (Medium Priority)

**Purpose:** Thread-safe state management for Kotlin/Native

```toml
[versions]
stately = "2.0.7"

[libraries]
stately-common = { module = "co.touchlab:stately-common", version.ref = "stately" }
stately-concurrency = { module = "co.touchlab:stately-concurrency", version.ref = "stately" }
```

**Benefits:**
- Thread-safe collections
- Atomic references
- Freeze-safe state management
- Better concurrency on iOS

---

### 4. SKIE (Optional - Build Enhancement)

**Purpose:** Better Swift interop for Kotlin/Native

```toml
[plugins]
skie = { id = "co.touchlab.skie", version = "0.8.4" }
```

**Benefits:**
- Generates Swift-friendly APIs
- Proper Swift async/await support
- Better enum handling
- Sealed class support in Swift

---

### 5. KMP-NativeCoroutines (Optional)

**Purpose:** Better coroutines support in Swift

```toml
[versions]
kmpNativeCoroutines = "1.0.0-ALPHA-31"

[libraries]
kmp-nativecoroutines-core = { module = "com.rickclephas.kmp:kmp-nativecoroutines-core", version.ref = "kmpNativeCoroutines" }
kmp-nativecoroutines-async = { module = "com.rickclephas.kmp:kmp-nativecoroutines-async", version.ref = "kmpNativeCoroutines" }
```

**Benefits:**
- Native Swift async/await integration
- Combine framework support
- Better Flow handling in Swift

---

### 6. Napier (Optional - Already have Kermit)

The project already uses Kermit for logging, which is excellent. No change needed.

---

## iOS-Specific Framework Considerations

The current implementation already uses these iOS frameworks effectively:
- ✅ JavaScriptCore - JS plugin execution
- ✅ WebKit (WKWebView) - Web content
- ✅ AVFoundation - TTS
- ✅ BackgroundTasks - Background processing
- ✅ LocalAuthentication - Biometrics
- ✅ StoreKit - In-app purchases
- ✅ UserNotifications - Notifications
- ✅ Compression - ZIP handling

### Missing iOS Framework Integrations

Consider adding Kotlin/Native bindings for:

1. **CloudKit** - iCloud sync (alternative to Google Drive/Dropbox)
2. **Translation** (iOS 17.4+) - Native translation API
3. **NaturalLanguage** - Language detection
4. **CoreSpotlight** - Spotlight search integration

---

## Dependency Updates Needed

### Update Ktor Version Consistency

Currently using mixed versions:
- `ktor = "3.3.2"` (main)
- `ktorClientDarwin = "3.3.3"` (iOS specific)

**Recommendation:** Align all Ktor dependencies to same version:

```toml
[versions]
ktor = "3.3.3"

# Remove separate ktorClientDarwin version
```

---

## Implementation Priority

| Priority | Dependency | Reason |
|----------|------------|--------|
| High | multiplatform-settings | Unified preferences API |
| Medium | Krypto | Cryptographic operations |
| Medium | Stately | Thread-safe state |
| Low | SKIE | Better Swift interop |
| Low | KMP-NativeCoroutines | Swift async support |

---

## Sample Integration

### Adding multiplatform-settings

1. Add to `gradle/libs.versions.toml`:
```toml
[versions]
multiplatformSettings = "1.2.0"

[libraries]
multiplatform-settings = { module = "com.russhwolf:multiplatform-settings", version.ref = "multiplatformSettings" }
multiplatform-settings-coroutines = { module = "com.russhwolf:multiplatform-settings-coroutines", version.ref = "multiplatformSettings" }
```

2. Add to `domain/build.gradle.kts` commonMain:
```kotlin
commonMain {
    dependencies {
        implementation(libs.multiplatform.settings)
        implementation(libs.multiplatform.settings.coroutines)
    }
}
```

3. Create platform-specific factories:

```kotlin
// commonMain
expect class SettingsFactory {
    fun create(): Settings
}

// androidMain
actual class SettingsFactory(private val context: Context) {
    actual fun create(): Settings = SharedPreferencesSettings(
        context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    )
}

// iosMain
actual class SettingsFactory {
    actual fun create(): Settings = NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
}
```

---

## Notes

- The project is already well-structured for KMP
- Most critical dependencies are already in place
- Focus on multiplatform-settings for immediate improvement
- Consider SKIE if building a native iOS app with SwiftUI
