# CatalogStore API Documentation

This document provides comprehensive documentation for the CatalogStore API in the IReader application. CatalogStore manages content sources (catalogs) including bundled, installed, and remote catalogs.

## Stability Annotations

- **@Stable**: API is stable and will not change without a major version bump
- **@Experimental**: API is experimental and may change in future releases
- **@Deprecated**: API is deprecated and will be removed in a future release

---

## CatalogStore

Manages catalog sources including loading, installation tracking, and updates.

**Package**: `ireader.domain.catalogs`

### Properties

#### catalogs
```kotlin
@Stable
var catalogs: List<CatalogLocal>
    private set
```

List of all loaded catalogs. This property is read-only from outside the class.

**Usage Example**:
```kotlin
val allCatalogs = catalogStore.catalogs
allCatalogs.forEach { catalog ->
    println("${catalog.name} (${catalog.sourceId})")
}
```

---

#### updatableCatalogs
```kotlin
@Stable
var updatableCatalogs: List<CatalogInstalled>
    private set
```

List of installed catalogs that have updates available.

**Usage Example**:
```kotlin
val updates = catalogStore.updatableCatalogs
if (updates.isNotEmpty()) {
    showUpdateNotification(updates)
}
```

---

### Stable Methods

#### get(sourceId: Long?)
```kotlin
@Stable
fun get(sourceId: Long?): CatalogLocal?
```

Retrieves a catalog by its source ID.

**Parameters**:
- `sourceId`: The unique identifier of the source. Use -200L for the local catalog source.

**Returns**: The catalog if found, null otherwise

**Usage Example**:
```kotlin
val catalog = catalogStore.get(sourceId)
if (catalog != null) {
    loadBooksFromCatalog(catalog)
} else {
    showError("Catalog not found")
}

// Get local catalog
val localCatalog = catalogStore.get(-200L)
```

**Error Cases**: Returns null if catalog doesn't exist

**Special Cases**:
- `sourceId = -200L`: Returns the bundled local catalog source
- `sourceId = null`: Returns null

---

#### getCatalogsFlow()
```kotlin
@Stable
fun getCatalogsFlow(): Flow<List<CatalogLocal>>
```

Subscribes to catalog changes. Emits updates whenever catalogs are added, removed, or updated.

**Returns**: Flow emitting list of catalogs when they change

**Usage Example**:
```kotlin
catalogStore.getCatalogsFlow()
    .collect { catalogs ->
        updateCatalogList(catalogs)
    }
```

**Error Cases**: Emits empty list if no catalogs are loaded

---

#### togglePinnedCatalog(sourceId: Long)
```kotlin
@Stable
suspend fun togglePinnedCatalog(sourceId: Long)
```

Toggles the pinned status of a catalog. Pinned catalogs appear at the top of the catalog list.

**Parameters**:
- `sourceId`: The unique identifier of the source

**Usage Example**:
```kotlin
// Pin a catalog
catalogStore.togglePinnedCatalog(sourceId)

// Unpin (call again)
catalogStore.togglePinnedCatalog(sourceId)
```

**Error Cases**: Silently succeeds if catalog doesn't exist

---

## Accessing Catalog Source

### Current API State

The `CatalogLocal` interface provides access to the underlying source implementation. However, the exact API for accessing the source property may vary depending on the catalog type.

### Stable Approach

```kotlin
@Stable
fun getCatalogSource(catalog: CatalogLocal): CatalogSource? {
    return when (catalog) {
        is CatalogBundled -> catalog.source
        is CatalogInstalled.Locally -> catalog.source
        is CatalogInstalled.SystemWide -> catalog.source
        is JSPluginCatalog -> catalog.source
        else -> null
    }
}
```

**Usage Example**:
```kotlin
val catalog = catalogStore.get(sourceId)
if (catalog != null) {
    val source = getCatalogSource(catalog)
    if (source != null) {
        // Use the source
        val books = source.getPopularBooks(page = 1)
    }
}
```

### Workaround for Unstable API

If direct property access is not available, use pattern matching:

```kotlin
fun getCatalogSourceSafe(catalog: CatalogLocal): Any? {
    return try {
        when (catalog) {
            is CatalogBundled -> catalog.source
            is CatalogInstalled -> {
                // Use reflection if needed
                val sourceField = catalog::class.members
                    .find { it.name == "source" }
                sourceField?.call(catalog)
            }
            else -> null
        }
    } catch (e: Exception) {
        Log.error("Failed to access catalog source", e)
        null
    }
}
```

---

## Catalog Types

### CatalogLocal (Base Interface)

All catalog types implement this interface.

**Properties**:
- `sourceId: Long` - Unique identifier for the source
- `name: String` - Display name of the catalog
- `isPinned: Boolean` - Whether the catalog is pinned
- `hasUpdate: Boolean` - Whether an update is available

### CatalogBundled

Built-in catalog that comes with the app (e.g., local library).

**Special Properties**:
- `source: LocalCatalogSource` - The local catalog source implementation

**Usage Example**:
```kotlin
val localCatalog = catalogStore.get(-200L) as? CatalogBundled
if (localCatalog != null) {
    val books = localCatalog.source.getBooks()
}
```

### CatalogInstalled

Installed catalog from APK or local file.

**Subtypes**:
- `CatalogInstalled.Locally` - Installed from local file
- `CatalogInstalled.SystemWide` - Installed from system APK

**Properties**:
- `pkgName: String` - Package name of the catalog
- `versionCode: Int` - Version code of the installed catalog
- `hasUpdate: Boolean` - Whether an update is available

**Usage Example**:
```kotlin
val installedCatalogs = catalogStore.catalogs
    .filterIsInstance<CatalogInstalled>()
    
installedCatalogs.forEach { catalog ->
    if (catalog.hasUpdate) {
        showUpdateButton(catalog)
    }
}
```

### JSPluginCatalog

JavaScript plugin-based catalog.

**Properties**:
- `pluginId: String` - Unique identifier for the plugin
- `source: JSPluginSource` - The plugin source implementation

**Usage Example**:
```kotlin
val pluginCatalogs = catalogStore.catalogs
    .filterIsInstance<JSPluginCatalog>()
    
pluginCatalogs.forEach { catalog ->
    println("Plugin: ${catalog.name}")
}
```

---

## Checking for JS Plugin Sources

To determine if a catalog is a JavaScript plugin source:

```kotlin
@Stable
fun isJSPluginSource(catalog: CatalogLocal): Boolean {
    return catalog is JSPluginCatalog
}
```

**Usage Example**:
```kotlin
val catalog = catalogStore.get(sourceId)
if (catalog != null && isJSPluginSource(catalog)) {
    // Handle JS plugin-specific features
    loadPluginFilters(catalog as JSPluginCatalog)
}
```

---

## Common Patterns

### Loading All Catalogs

```kotlin
// Get current catalogs
val catalogs = catalogStore.catalogs

// Subscribe to catalog changes
catalogStore.getCatalogsFlow()
    .collect { catalogs ->
        updateUI(catalogs)
    }
```

### Filtering Catalogs

```kotlin
// Get only installed catalogs
val installed = catalogStore.catalogs
    .filterIsInstance<CatalogInstalled>()

// Get only pinned catalogs
val pinned = catalogStore.catalogs
    .filter { it.isPinned }

// Get catalogs with updates
val withUpdates = catalogStore.updatableCatalogs
```

### Working with Catalog Sources

```kotlin
suspend fun loadBooksFromCatalog(sourceId: Long): List<Book> {
    val catalog = catalogStore.get(sourceId) ?: return emptyList()
    
    return when (catalog) {
        is CatalogBundled -> {
            catalog.source.getBooks()
        }
        is CatalogInstalled -> {
            // Access source through catalog
            val source = catalog.source
            source.getPopularBooks(page = 1)
        }
        is JSPluginCatalog -> {
            // JS plugin source
            val source = catalog.source
            source.getLatestBooks(page = 1)
        }
        else -> emptyList()
    }
}
```

### Managing Pinned Catalogs

```kotlin
// Pin a catalog
suspend fun pinCatalog(sourceId: Long) {
    catalogStore.togglePinnedCatalog(sourceId)
}

// Get pinned catalogs in order
fun getPinnedCatalogs(): List<CatalogLocal> {
    return catalogStore.catalogs
        .filter { it.isPinned }
        .sortedBy { it.name }
}
```

---

## Error Handling

### Catalog Not Found

```kotlin
val catalog = catalogStore.get(sourceId)
if (catalog == null) {
    // Handle missing catalog
    showError("Catalog not found. It may have been uninstalled.")
    return
}
```

### Source Access Failure

```kotlin
fun accessCatalogSource(catalog: CatalogLocal): CatalogSource? {
    return try {
        when (catalog) {
            is CatalogBundled -> catalog.source
            is CatalogInstalled -> catalog.source
            is JSPluginCatalog -> catalog.source
            else -> null
        }
    } catch (e: Exception) {
        Log.error("Failed to access catalog source", e)
        showError("Failed to load catalog: ${e.message}")
        null
    }
}
```

---

## Migration Notes

### From Unstable API

If you were using unstable methods to access catalog sources, migrate to the stable patterns shown above:

**Before** (Unstable):
```kotlin
// Direct property access that may not work
val source = catalog.source
```

**After** (Stable):
```kotlin
// Pattern matching with type checking
val source = when (catalog) {
    is CatalogBundled -> catalog.source
    is CatalogInstalled -> catalog.source
    is JSPluginCatalog -> catalog.source
    else -> null
}
```

---

## Testing

When testing CatalogStore functionality:

1. **Test catalog loading**: Verify catalogs are loaded correctly on initialization
2. **Test catalog updates**: Verify update detection works correctly
3. **Test pinning**: Verify pin/unpin functionality
4. **Test source access**: Verify source can be accessed for all catalog types
5. **Test catalog flow**: Verify reactive updates work correctly

**Example Test**:
```kotlin
@Test
fun testCatalogRetrieval() = runTest {
    val catalog = catalogStore.get(testSourceId)
    assertNotNull(catalog)
    assertEquals(testSourceId, catalog.sourceId)
}

@Test
fun testCatalogFlow() = runTest {
    val catalogs = mutableListOf<List<CatalogLocal>>()
    val job = launch {
        catalogStore.getCatalogsFlow()
            .take(2)
            .collect { catalogs.add(it) }
    }
    
    // Trigger catalog change
    catalogStore.togglePinnedCatalog(testSourceId)
    
    job.join()
    assertEquals(2, catalogs.size)
}
```

---

## See Also

- [Repository API Documentation](RepositoryAPI.md)
- [TranslationEngine API Documentation](TranslationEngineAPI.md)
- [Repository Migration Guide](../migration/RepositoryMigration.md)
