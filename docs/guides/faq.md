# Frequently Asked Questions (FAQ)

Common questions and answers about IReader.

---

## 📱 About IReader

### What is IReader?
IReader is a free, open-source reader app for novels, light novels, and web novels. It supports multiple sources via extensions, offline reading, customizable themes, and more. No ads, no tracking, completely free forever.

### Where can I download IReader?
Download from GitHub Releases: https://github.com/IReaderorg/IReader/releases

- **Android**: Download the APK file
- **Desktop**: Available for Windows, macOS, and Linux

### Is IReader free?
Yes! IReader is 100% free and open-source. No hidden costs, no premium features, no subscriptions.

### What platforms are supported?
Android (fully supported) and Desktop (Windows, macOS, Linux). iOS is in development.

### What Android version do I need?
Android 7.0 or higher.

---

## 📦 Repositories

### What is a repository?
A repository is a collection of sources (extensions) that let you browse and read novels from different websites. Think of it like an app store for novel sources!

### Do I need to add a repository?
Yes! IReader no longer includes a default repository. You need to add at least one repository to access novel sources.

### How do I add a repository?
**Quick Setup (Recommended):**
1. Go to **Settings** → **Repository**
2. Tap the **+ (Add)** button
3. Select **"Quick Add Popular Repo"**
4. Choose **IReader** (Official) and tap **Save**
5. Repeat and also add **LNReader** for more sources

### Which repositories should I add?
We recommend adding both:
- **IReader Extensions** (Official) - Native extensions with better performance
- **LNReader** - 100+ JavaScript sources for maximum coverage

### How do I add a custom repository URL?
1. Go to **Settings** → **Repository**
2. Tap the **+ (Add)** button
3. Select **"Add Custom Repository"**
4. Enter the repository URL
5. Tap **Save**
6. Go to Sources and refresh

### What are the official repository URLs?
| Repository | URL |
|------------|-----|
| IReader Extensions | `https://raw.githubusercontent.com/IReaderorg/IReader-extensions/repov2/index.min.json` |

### How do I remove a repository?
1. Go to **Settings** → **Repository**
2. Find the repository you want to remove
3. Tap on it and select **Delete** or swipe to remove

---

## 🔌 Installing Sources

### How do I install sources/extensions?
After adding a repository:
1. Go to **Browse** → **Sources** or **Extensions**
2. Browse available extensions organized by language
3. Tap **Install** next to any extension you want
4. Wait for the download to complete
5. The source will appear in your sources list!

### What types of sources are available?
Three types:
1. **IReader Extensions** - Native extensions built for IReader (faster)
2. **LNReader Plugins** - JavaScript-based plugins (100+ sources)
3. **Local Sources** - Files on your device (EPUB, PDF, etc.)

### I see "Saved to cache" - what does that mean?
It means the extension file is stored in IReader's internal cache. This allows the app to load the extension without reinstalling, but it might be cleared if you clear the app's cache.

### How do I save extensions to external storage?
1. Go to **Settings** → **General**
2. Disable **"Saved Sources to Cache"**
3. Grant storage permissions when prompted

This makes it easier to backup and manage extensions manually.

### How do I update my extensions?
Go to **Settings** → **Extensions** → **JavaScript Plugins** and tap "Check for Updates", or enable auto-updates.

### Why do I get "Untrusted extension" warnings?
This is normal! IReader warns you before running code from external sources. Only install from trusted repositories (like official IReader or LNReader). You can manage trusted extensions in **Settings** → **Extensions** → **Trust Management**.

### How do I filter sources by language?
In the Sources screen, tap the **Filter** icon to show only sources in specific languages.

### How do I pin a source to the top?
Long-press a source and select **Pin** to keep it at the top of the list.

---

## 📚 Adding & Reading Books

### How do I search for books?
1. Go to the **Browse** tab
2. Select any source
3. Tap the **search icon** (magnifying glass)
4. Enter the title, author, or keywords
5. Browse through the results

### How do I add a book to my library?
1. Find the book you want (through search or browsing)
2. Tap on the book to view details
3. Tap the **+ Add to Library** button
4. The book will appear in your **Library** tab

### How do I download chapters for offline reading?

**Single chapter:**
1. Open a book from your library
2. Go to the chapter list
3. Tap the download icon next to a chapter

**Multiple chapters:**
1. Open a book from your library
2. Go to the chapter list
3. Tap the overflow menu (three dots)
4. Select **Download** or **Download Unread**
5. Choose how many chapters (Next 5, Next 10, Custom, All)

### How do I add local books (EPUB, PDF)?

**From device storage:**
1. Go to the **Browse** tab
2. Tap **Local Source** or **Local Storage**
3. Browse to the folder containing your books
4. Tap on a book file to add it to your library

**Using file manager:**
1. Open your device's file manager
2. Navigate to your books folder
3. Tap on a book file
4. Select **Open with** → **IReader**

### How do I refresh book information?
1. Go to your **Library**
2. Long-press on a book (or select multiple)
3. Tap **Refresh** or the refresh icon
4. The app will fetch the latest information from the source

### How do I edit book details?
1. Go to your **Library**
2. Long-press on a book
3. Select **Edit**
4. Update title, author, cover image, or description
5. Tap **Save**

---

## 🎨 Customization

### How do I customize the reading experience?
Go to **Settings** → **Reader** to customize:
- Font size and style
- Theme (light/dark/custom)
- Reading direction
- Line spacing
- And more!

### Does IReader support Text-to-Speech (TTS)?
Yes! Desktop version includes AI-powered TTS using Piper. Voice models are downloaded automatically when you first use TTS.

---

## 🔧 Troubleshooting

### "No sources found" after adding repository
1. Make sure **JavaScript Plugins** is enabled in Settings → General
2. Check your internet connection
3. Try refreshing the sources list
4. Restart the app and try again

### Extensions not showing up
1. Check the language filter - you might have filtered them out
2. Go to Sources and tap the filter icon
3. Select **All Languages** or your preferred language

### A source/extension isn't working
Sources can go down temporarily. Try:
1. Check if the source website is accessible in your browser
2. Update the extension
3. Try a different source for the same content
4. Report in Discord #support if issue persists

### The app is crashing
Try these steps:
1. Clear app cache (Settings → Apps → IReader → Clear Cache)
2. Update to the latest version from GitHub
3. If still crashing, report with:
   - Device model
   - Android version
   - IReader version
   - Steps to reproduce

### My downloads are failing
Check:
1. Internet connection
2. Storage space on your device
3. Try downloading fewer chapters at once
4. Some sources may have download limits

### Book not appearing in library after adding
1. Check if you're viewing a specific category
2. Tap the category dropdown and select **All** or **Default**
3. Pull down to refresh your library
4. Try adding the book again

### Book information is incorrect
1. Long-press the book in your library
2. Select **Edit**
3. Update the information manually
4. Or select **Refresh** to try fetching data again

---

## 📖 More Resources

- [Extension Repository Guide](extension_repository_guide.md) - How to add repositories
- [Sources Guide](sources.md) - Managing sources and extensions
- [JavaScript Plugins Guide](js_plugins.md) - JS plugin system
- [Local Source Guide](localsource-guide.md) - Adding local books
- [Text-to-Speech Guide](tts.md) - TTS setup and usage
- [Sync & Backup Guide](sync_backup.md) - Backup and restore

---

## 💬 Need More Help?

- Join our [Discord Community](https://discord.gg/ireader)
- Report issues on [GitHub](https://github.com/IReaderorg/IReader/issues)
- Check the [Documentation Index](../README.md)

---

*Last updated: June 2026*
