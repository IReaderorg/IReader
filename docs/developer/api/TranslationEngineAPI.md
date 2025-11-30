# TranslationEngine API Documentation

This document provides comprehensive documentation for the TranslationEngine API in the IReader application. The TranslationEnginesManager provides access to both built-in and plugin-based translation engines.

## Stability Annotations

- **@Stable**: API is stable and will not change without a major version bump
- **@Experimental**: API is experimental and may change in future releases
- **@Deprecated**: API is deprecated and will be removed in a future release

---

## TranslationEnginesManager

Manages translation engines including built-in engines and plugin-based engines.

**Package**: `ireader.domain.usecases.translate`

### Stable Methods

#### getAvailableEngines()
```kotlin
@Stable
fun getAvailableEngines(): List<TranslationEngineSource>
```

Gets all available translation engines combining built-in and plugin engines.

**Returns**: List of translation engine sources (built-in and plugin-based)

**Usage Example**:
```kotlin
val engines = translationEnginesManager.getAvailableEngines()
engines.forEach { engine ->
    when (engine) {
        is TranslationEngineSource.BuiltIn -> {
            println("Built-in: ${engine.engine.engineName}")
        }
        is TranslationEngineSource.Plugin -> {
            println("Plugin: ${engine.plugin.manifest.name}")
        }
    }
}
```

**Error Cases**: Returns empty list if no engines are available (should not happen as built-in engines always exist)

---

#### getEngineById(id: String)
```kotlin
@Stable
fun getEngineById(id: String): TranslationEngineSource?
```

Gets a specific translation engine source by ID.

**Parameters**:
- `id`: For built-in engines, use the engine ID as a string (e.g., "1", "2"). For plugin engines, use the plugin manifest ID.

**Returns**: The translation engine source if found, null otherwise

**Usage Example**:
```kotlin
// Get built-in engine by ID
val engine = translationEnginesManager.getEngineById("1")

// Get plugin engine by m