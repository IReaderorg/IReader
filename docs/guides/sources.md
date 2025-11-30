# Sources & Extensions Guide

IReader supports a wide variety of sources through its extension system. This guide explains how to manage, install, and troubleshoot sources.

## Types of Sources

1.  **IReader Extensions**: Native extensions built for IReader.
2.  **LNReader Plugins**: JavaScript-based plugins compatible with IReader.
3.  **Local Sources**: Files (EPUB, PDF) on your device.

## Installing Extensions

### From Repositories (Recommended)
1.  Go to **Browse** → **Sources**.
2.  Tap the **Extensions** tab (or "Explore" depending on version).
3.  Browse the list of available extensions.
4.  Tap **Install** next to an extension.
5.  **Trust**: You may be asked to trust the extension. Only install extensions from trusted repositories.

### Local Installation (Manual)
If you have an extension file (`.apk` or `.js`):
1.  **APK (Native)**: Open the `.apk` file on your device and install it like any other app.
2.  **JS (Plugin)**: 
    *   Go to **Settings** → **Extensions** → **JavaScript Plugins**.
    *   Tap **Install from File**.
    *   Select the `.js` file.

### Local vs Package Installer
*   **Package Installer**: Installs the extension as a system app (APK). This is the standard method for native extensions.
*   **Local Installer**: Copies the extension (usually JS) to IReader's internal storage. It is not installed as a system app.

## Managing Sources

### Sorting & Filtering
*   **Language**: In the Sources screen, tap the **Filter** icon to show only sources in specific languages.
*   **Pinned**: Long-press a source and select **Pin** to keep it at the top of the list.
*   **Last Used**: The source you used most recently appears at the top.

### "Saved to Cache"
When you see "Saved to cache" for a source or plugin, it means the extension file has been downloaded and stored in IReader's internal cache directory. This allows the app to load the extension without reinstalling it, but it might be cleared if you clear the app's cache.

## LNReader Sources
IReader is fully compatible with LNReader plugins.
1.  Go to **Settings** → **Extensions**.
2.  Add the LNReader repository URL if not already present.
3.  You can now browse and install LNReader plugins directly from the Extensions list.

## Troubleshooting

### Extension Not Showing
*   Ensure the language filter is set correctly.
*   Check if the extension is enabled in **Settings** → **Extensions**.
*   If it's a JS plugin, ensure "Enable JavaScript Plugins" is ON.

### "Trusted Extension" Warning
*   IReader warns you before running code from unknown sources.
*   Go to **Settings** → **Extensions** → **Trust Management** to review trusted extensions.
