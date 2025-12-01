# IReader Dependencies Guide

This document helps contributors understand the project's dependency structure.

## Version Catalogs

Dependencies are managed via Gradle Version Catalogs in `gradle/`:

| File | Purpose |
|------|---------|
| `libs.versions.toml` | Main dependencies (network, storage, UI) |
| `kotlinx.versions.toml` | Kotlin/KotlinX libraries |
| `androidx.versions.toml` | AndroidX libraries |
| `compose.versions.toml` | Compose-specific libraries |
| `accompanist.versions.toml` | Accompanist utilities |

## Key Dependencies by Category

### Networking
- **Ktor** - HTTP client (multiplatform)
- **OkHttp** - HTTP client (Android/JVM)
- **Jsoup** - HTML parsing

### Database
- **SQLDelight** - Multiplatform SQL database
- **DataStore** - Preferences storage

### UI
- **Jetpack Compose** - UI framework
- **Coil** - Image loading
- **Navigation Compose** - Navigation

### Dependency Injection
- **Koin** - Multiplatform DI

### Backend
- **Supabase** - Backend services (auth, database, realtime)

### JavaScript Engine
- **J2V8** (Android) - V8 JavaScript engine for plugin execution
- **GraalVM** (Desktop) - JavaScript engine for desktop

## Platform-Specific Dependencies

### Android Only
- Firebase (standard/dev flavors only, excluded from F-Droid)
- ML Kit Translation (standard/dev flavors only)
- J2V8 JavaScript engine

### Desktop Only
- `apk-parser` - For parsing extension APK files
- `dex2jar` - For loading extensions
- `piper-jni` - TTS (if enabled)
- GraalVM Polyglot - JavaScript engine

## Removed/Unused Dependencies

The following were identified as unused and commented out:

| Dependency | Reason |
|------------|--------|
| `kermit` | Custom Log implementation used instead |
| `napier` | Custom Log implementation used instead |
| `gson` | Using kotlinx.serialization |
| `jackson` | Using kotlinx.serialization |
| `protobuf` | Not used |
| `slf4j-android` | Not used |
| `voyager-tab-navigator` | Not used |
| `zxing-core` | No QR code functionality |
| `coil-gif` | No GIF support needed |
| `piper-tts-android` | Not implemented |

## Adding New Dependencies

1. Add version to appropriate `.versions.toml` file
2. Add library definition
3. Use in `build.gradle.kts` via catalog accessor
4. Document purpose in this file

## Logging

The project uses **Kermit** for logging via `ireader.core.log.Log` wrapper.

```kotlin
import ireader.core.log.Log

// Usage examples
Log.debug { "Lazy message" }
Log.info("Message with {} placeholder", value)
Log.error(exception, "Error occurred")
```

**Why Kermit?**
- Kotlin Multiplatform native
- Crashlytics integration available
- Active maintenance by Touchlab
- Configurable log levels

Do NOT add additional logging libraries (Napier, Timber, etc.).

## Serialization

Use `kotlinx.serialization` for all serialization needs. Gson and Jackson are NOT used.
