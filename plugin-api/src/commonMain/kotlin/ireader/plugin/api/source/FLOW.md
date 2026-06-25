# Plugin API — Extension Loading Interfaces

## Overview

This module defines the **interfaces** that platform-specific loaders implement.
No implementation lives here — only contracts.

## Key Interfaces

### `ExtensionLoader`
General interface for loading extensions from various formats (APK, JAR, JS, JSON, etc.).

```
ExtensionLoader
  ├─ supportedFormats: List<ExtensionFormat>
  ├─ loadExtension(path) → ExtensionLoadResult
  ├─ loadExtensionFromBytes(bytes, format) → ExtensionLoadResult
  ├─ unloadExtension(extensionId)
  ├─ validateExtension(path) → ExtensionValidationResult
  └─ canHandle(path): Boolean
```

### `ExtensionInstaller`
Handles actual installation to device storage.

```
ExtensionInstaller
  ├─ install(path, targetDir) → ExtensionInstallResult
  ├─ installFromBytes(bytes, fileName, targetDir) → ExtensionInstallResult
  ├─ uninstall(extensionId, installDir): Boolean
  └─ listInstalled(installDir): List<String>
```

### `SourceLoaderPlugin`
Combines Plugin + ExtensionLoader for plugin-based source loading.

## Data Types

- `ExtensionFormat` — enum: APK, JAR, IPLUGIN, JSON, JS_BUNDLE, TS_BUNDLE, ZIP, DEX
- `ExtensionLoadResult` — Success(LoadedExtension) | Failure(ExtensionLoadError)
- `ExtensionLoadError` — FileNotFound, InvalidFormat, UnsupportedVersion, etc.
- `ExtensionValidationResult` — Valid | Invalid | UnsupportedVersion

## Used By

- `AndroidCatalogLoader` (implements CatalogLoader, not ExtensionLoader directly)
- `DesktopCatalogLoader` (implements CatalogLoader)
- JS plugin system (uses ExtensionLoader interface for JS bundles)
