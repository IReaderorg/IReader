# 📚 IReader LocalSource User Guide

This guide explains how to use IReader's LocalSource feature to read novels stored on your device.

## 🎯 What is LocalSource?

LocalSource allows you to read novels from files stored locally on your device. It follows the Tachiyomi/Mihon folder structure model:

- **One folder per novel series**
- **One file per chapter**
- **Optional metadata files** (cover image, details.json)

**Source ID**: `-200` (reserved for LocalSource)

---

## 📁 Folder Structure

### Basic Structure

```
{AppDataDir}/local/
├── Novel Series 1/
│   ├── cover.jpg (or cover.png)
│   ├── details.json
│   ├── Chapter 1.txt
│   ├── Chapter 2.epub
│   └── Chapter 3.txt
├── Novel Series 2/
│   ├── cover.jpg
│   ├── details.json
│   ├── 001 - Prologue.txt
│   ├── 002 - First Day.txt
│   └── 003 - The Journey Begins.epub
└── Another Novel/
    ├── Ch1.txt
    ├── Ch2.txt
    └── Ch3.epub
```

### Platform-Specific Paths

| Platform | Local Folder Path |
|----------|-------------------|
| **Android** | `/storage/emulated/0/Android/data/ir.kazemcodes.infinityreader/files/local/` |
| **Desktop** | `~/.ireader/local/` (Linux/Mac)<br>`C:\Users\{Username}\.ireader\local\` (Windows) |
| **iOS** | `{AppDataDir}/local/` |

---

## 📄 Supported File Formats

### Currently Supported

| Format | Extension | Notes |
|--------|-----------|-------|
| **Plain Text** | `.txt` | UTF-8 encoding recommended |
| **EPUB** | `.epub` | Full EPUB 2.0/3.0 support |

### Planned Support (Future)

- `.pdf` - PDF documents
- `.html` - HTML files
- `.md` - Markdown files

---

## 🖼️ Cover Images

Place a cover image in the novel folder with one of these names:

- `cover.jpg` (preferred)
- `cover.png`

**Requirements:**
- Recommended size: 300x450px or similar aspect ratio
- Formats: JPG or PNG
- File must be named exactly `cover.jpg` or `cover.png` (case-sensitive on some platforms)

**Example:**
```
My Novel/
├── cover.jpg  ✅ Will be detected
├── Cover.jpg  ❌ Wrong case (on Linux/Mac)
└── thumbnail.jpg  ❌ Wrong name
```

---

## 📋 Metadata File (details.json)

Add rich metadata to your novels with an optional `details.json` file.

### Complete Example

```json
{
  "title": "The Wandering Inn",
  "author": "pirateaba",
  "artist": "John Doe",
  "description": "An inn is a place to rest, a place to talk and share stories, or a place to find adventures, a starting ground for quests and legends.\n\nIn this world, at least. To Erin Solstice, an inn seems like a medieval relic from the past. But here she is, running from Goblins and trying to survive in a world full of monsters and magic.",
  "genre": ["Fantasy", "Adventure", "Slice of Life", "Comedy"],
  "status": "Ongoing"
}
```

### Field Reference

| Field | Type | Required | Description | Example |
|-------|------|----------|-------------|---------|
| `title` | String | No | Novel title (overrides folder name) | `"The Wandering Inn"` |
| `author` | String | No | Author name | `"pirateaba"` |
| `artist` | String | No | Artist/Illustrator name | `"John Doe"` |
| `description` | String | No | Novel synopsis/summary | `"An inn is a place..."` |
| `genre` | Array | No | List of genres | `["Fantasy", "Adventure"]` |
| `status` | String | No | Publication status | `"Ongoing"`, `"Completed"`, `"Hiatus"` |

### Status Values

| Value | Display | Meaning |
|-------|---------|---------|
| `"Ongoing"` | Ongoing | Currently publishing |
| `"Completed"` | Completed | Finished series |
| `"Hiatus"` | On Hiatus | Temporarily paused |
| `"Cancelled"` | Cancelled | Discontinued |
| `"Licensed"` | Licensed | Officially licensed |

**Note:** If `details.json` is missing, the folder name will be used as the title.

---

## 📖 Chapter Files

### Naming Convention

Chapters are sorted **alphabetically** by filename. Use consistent naming:

**✅ Good Examples:**
```
001 - Prologue.txt
002 - Chapter 1.txt
003 - Chapter 2.txt
...
010 - Chapter 9.txt
011 - Chapter 10.txt
```

**❌ Bad Examples:**
```
1.txt          # Will sort incorrectly (1, 10, 11, 2, 3...)
Chapter 1.txt  # Inconsistent with Chapter 10
ch1.txt        # Lowercase vs uppercase issues
```

### Text File Format (.txt)

**Encoding:** UTF-8 (recommended)

**Structure:**
- Paragraphs separated by blank lines (`\n\n`) or single newlines (`\n`)
- Leading/trailing whitespace is automatically trimmed
- Empty lines are removed

**Example:**
```txt
This is the first paragraph of the chapter.

This is the second paragraph. It will be displayed as a separate block.

And this is the third paragraph.
```

### EPUB Format (.epub)

IReader automatically extracts and parses EPUB files:

- Reads spine order from `content.opf`
- Extracts text from HTML content files
- Parses `<p>`, `<h1>`-`<h6>`, `<blockquote>`, `<pre>`, `<li>` elements
- Preserves reading order

**Supported EPUB versions:** 2.0, 3.0

---

## 🚀 How to Use LocalSource

### Step 1: Prepare Your Files

1. Create a folder for your novel
2. Add chapter files (`.txt` or `.epub`)
3. (Optional) Add `cover.jpg` or `cover.png`
4. (Optional) Add `details.json` with metadata

### Step 2: Copy to Local Folder

**Android:**
1. Connect device to computer via USB
2. Navigate to: `Android/data/ir.kazemcodes.infinityreader/files/local/`
3. Copy your novel folder there

**Desktop:**
1. Navigate to: `~/.ireader/local/` (or `C:\Users\{You}\.ireader\local\`)
2. Copy your novel folder there

**iOS:**
1. Use Files app or iTunes File Sharing
2. Navigate to IReader's documents folder
3. Copy to `local/` subfolder

### Step 3: Refresh Local Library

In IReader app:
1. Go to **Browse** tab
2. Select **Local Source**
3. Pull down to refresh (or tap refresh button)
4. Your novels will appear

### Step 4: Add to Library

1. Tap on a novel from Local Source
2. Tap the **Add to Library** button (heart icon)
3. Novel is now in your library with all chapters

---

## 🔄 Refreshing Local Library

### When to Refresh

Refresh the local library when you:
- Add new novel folders
- Add new chapters to existing novels
- Update `details.json` or cover images
- Delete novels or chapters

### How to Refresh

**Method 1: Pull to Refresh**
- In Local Source browse screen, pull down to refresh

**Method 2: Manual Refresh**
- Tap the refresh icon in the toolbar

**Method 3: Automatic**
- Local library auto-refreshes when you open Local Source

### What Happens During Refresh

1. **Scans** the local folder for novel directories
2. **Detects** new novels and adds them to the database
3. **Updates** existing novels with new metadata
4. **Scans** each novel folder for chapter files
5. **Adds** new chapters to the database
6. **Preserves** reading progress and bookmarks

---

## 📊 Complete Example

### Example Novel Setup

```
~/.ireader/local/
└── The Wandering Inn/
    ├── cover.jpg
    ├── details.json
    ├── 001 - Prologue.txt
    ├── 002 - Chapter 1.01.txt
    ├── 003 - Chapter 1.02.txt
    ├── 004 - Chapter 1.03.txt
    └── 005 - Interlude.epub
```

**details.json:**
```json
{
  "title": "The Wandering Inn",
  "author": "pirateaba",
  "description": "An inn is a place to rest, a place to talk and share stories...",
  "genre": ["Fantasy", "Adventure", "Slice of Life"],
  "status": "Ongoing"
}
```

**001 - Prologue.txt:**
```txt
The Wandering Inn
Prologue

The inn was empty when she arrived.

Not just empty of guests, but empty of life. The door hung open on broken hinges, and the windows were shattered. Inside, dust covered everything.

Erin Solstice stood in the doorway and stared.

"This is not what I expected," she said to no one in particular.
```

### Result in IReader

- **Title:** "The Wandering Inn" (from details.json)
- **Author:** "pirateaba"
- **Cover:** Displays cover.jpg
- **Chapters:** 5 chapters in order
  - Prologue
  - Chapter 1.01
  - Chapter 1.02
  - Chapter 1.03
  - Interlude
- **Status:** Ongoing
- **Genres:** Fantasy, Adventure, Slice of Life

---

## 🛠️ Advanced Usage

### Multiple Novels

You can have as many novel folders as you want:

```
local/
├── Novel A/
├── Novel B/
├── Novel C/
└── Novel D/
```

Each novel is independent with its own chapters and metadata.

### Mixed File Formats

You can mix `.txt` and `.epub` files in the same novel:

```
My Novel/
├── Chapter 1.txt
├── Chapter 2.epub
├── Chapter 3.txt
└── Chapter 4.epub
```

### Organizing by Arc/Volume

Use prefixes to organize chapters:

```
My Novel/
├── Arc 1 - 001 - Beginning.txt
├── Arc 1 - 002 - Rising Action.txt
├── Arc 1 - 003 - Climax.txt
├── Arc 2 - 001 - New Start.txt
└── Arc 2 - 002 - Continuation.txt
```

### Batch Import

To import multiple novels at once:

1. Prepare all novel folders on your computer
2. Copy all folders to `local/` directory at once
3. Open IReader and refresh Local Source
4. All novels will be detected and added

---

## 🐛 Troubleshooting

### Novel Not Appearing

**Problem:** Novel folder exists but doesn't show in Local Source

**Solutions:**
1. ✅ Ensure folder is directly in `local/` (not nested deeper)
2. ✅ Check folder name doesn't contain special characters
3. ✅ Verify at least one supported file (`.txt` or `.epub`) exists
4. ✅ Pull to refresh in Local Source
5. ✅ Restart IReader app

### Chapters Not Loading

**Problem:** Novel appears but chapters don't load

**Solutions:**
1. ✅ Check file extensions are `.txt` or `.epub` (lowercase)
2. ✅ Verify files aren't corrupted
3. ✅ For `.txt` files, ensure UTF-8 encoding
4. ✅ For `.epub` files, verify they're valid EPUB format
5. ✅ Check file permissions (read access required)

### Cover Not Showing

**Problem:** Cover image doesn't display

**Solutions:**
1. ✅ File must be named exactly `cover.jpg` or `cover.png`
2. ✅ Check case sensitivity (lowercase on Linux/Mac)
3. ✅ Verify image file isn't corrupted
4. ✅ Try JPG format if PNG doesn't work
5. ✅ Refresh local library after adding cover

### Wrong Chapter Order

**Problem:** Chapters appear in wrong order

**Solutions:**
1. ✅ Use zero-padded numbers: `001`, `002`, `010` (not `1`, `2`, `10`)
2. ✅ Ensure consistent naming pattern
3. ✅ Rename files and refresh library
4. ✅ Alphabetical sorting is used, not numerical

### details.json Not Working

**Problem:** Metadata from details.json not applied

**Solutions:**
1. ✅ Verify JSON syntax is valid (use JSON validator)
2. ✅ Check file is named exactly `details.json` (lowercase)
3. ✅ Ensure UTF-8 encoding
4. ✅ Remove any BOM (Byte Order Mark) from file
5. ✅ Refresh local library after editing

---

## 📱 Platform-Specific Notes

### Android

- **Permissions:** IReader needs storage permission
- **Scoped Storage:** Files are in app-specific directory
- **File Manager:** Use any file manager app to copy files
- **USB Transfer:** Connect via USB and use computer's file browser

### Desktop (Windows/Mac/Linux)

- **Hidden Folder:** `.ireader` folder is hidden on Linux/Mac (use `ls -la` or show hidden files)
- **Direct Access:** Easy to copy files directly
- **Symlinks:** You can use symbolic links to point to existing novel folders

### iOS

- **Files App:** Use iOS Files app to manage local files
- **iTunes:** Can use iTunes File Sharing (older iOS versions)
- **iCloud:** Can sync local folder via iCloud Drive (if configured)

---

## 🎓 Best Practices

### 1. Consistent Naming

Use a consistent naming scheme for all chapters:

```
✅ Good:
001 - Prologue.txt
002 - Chapter 1.txt
003 - Chapter 2.txt

❌ Bad:
Prologue.txt
Ch1.txt
chapter_2.txt
```

### 2. Always Use details.json

Even if you only set the title, it's better than relying on folder names:

```json
{
  "title": "My Novel",
  "author": "Author Name"
}
```

### 3. Add Covers

Covers make your library look professional and help identify novels quickly.

### 4. UTF-8 Encoding

Always save `.txt` files as UTF-8 to avoid character encoding issues.

### 5. Backup Your Files

Keep backups of your local novels folder, especially if you've added custom metadata.

### 6. Test Small First

When setting up a new novel, test with 1-2 chapters first to ensure everything works before adding all chapters.

---

## 🔗 Related Features

### Import EPUB

IReader has a dedicated EPUB import feature:
- Imports EPUB files directly into local library
- Automatically extracts metadata
- Creates proper folder structure
- See: Import EPUB documentation

### Import PDF

IReader can import PDF files:
- Converts PDF pages to readable text
- Creates chapters from PDF structure
- See: Import PDF documentation

### Export to Local

You can export downloaded novels to LocalSource format:
- Preserves metadata
- Creates proper folder structure
- Useful for backup and offline reading

---

## 📚 Example Workflows

### Workflow 1: Web Novel to Local

1. Download web novel chapters as `.txt` files
2. Create folder: `{NovelName}/`
3. Rename files: `001.txt`, `002.txt`, etc.
4. Create `details.json` with metadata
5. Find and add `cover.jpg`
6. Copy folder to `local/`
7. Refresh in IReader

### Workflow 2: EPUB Collection

1. Collect EPUB files for a series
2. Create folder: `{SeriesName}/`
3. Rename EPUBs: `Vol 1.epub`, `Vol 2.epub`, etc.
4. Add `cover.jpg` and `details.json`
5. Copy to `local/`
6. Refresh in IReader

### Workflow 3: Mixed Format

1. Have some chapters as `.txt`, some as `.epub`
2. Create folder with consistent naming:
   - `001 - Chapter 1.txt`
   - `002 - Chapter 2.epub`
   - `003 - Chapter 3.txt`
3. Add metadata and cover
4. Copy to `local/`
5. Refresh in IReader

---

## 🎯 Quick Reference

### Folder Structure
```
local/
└── {NovelName}/
    ├── cover.jpg (optional)
    ├── details.json (optional)
    ├── 001 - Chapter 1.txt
    ├── 002 - Chapter 2.epub
    └── ...
```

### details.json Template
```json
{
  "title": "Novel Title",
  "author": "Author Name",
  "description": "Synopsis here",
  "genre": ["Genre1", "Genre2"],
  "status": "Ongoing"
}
```

### Supported Files
- ✅ `.txt` (UTF-8)
- ✅ `.epub` (2.0/3.0)
- 🔜 `.pdf` (planned)
- 🔜 `.html` (planned)

### Key Points
- One folder = One novel
- One file = One chapter
- Alphabetical sorting
- Refresh to detect changes
- Add to library to read

---

## 💡 Tips & Tricks

### Tip 1: Use Calibre for EPUB Management

[Calibre](https://calibre-ebook.com/) is great for:
- Converting formats to EPUB
- Editing EPUB metadata
- Organizing your ebook collection
- Batch renaming files

### Tip 2: Automate with Scripts

Create scripts to:
- Download web novels and format them
- Generate `details.json` from web metadata
- Batch rename chapter files
- Sync local folder across devices

### Tip 3: Cloud Sync

Sync your `local/` folder using:
- Google Drive
- Dropbox
- OneDrive
- Syncthing

This keeps your local library synced across devices.

### Tip 4: Organize by Language

Create subfolders for different languages:
```
local/
├── English/
│   ├── Novel A/
│   └── Novel B/
└── Japanese/
    ├── Novel C/
    └── Novel D/
```

**Note:** IReader will scan all subfolders, so this works!

### Tip 5: Version Control

Use Git to version control your local library:
- Track changes to `details.json`
- Manage chapter additions
- Sync across devices
- Rollback if needed

---

## 🆘 Getting Help

If you encounter issues:

1. **Check this guide** - Most common issues are covered
2. **Check app logs** - Enable debug logging in settings
3. **GitHub Issues** - Report bugs on IReader GitHub
4. **Community** - Ask in IReader Discord/Reddit

---

## 📝 Summary

LocalSource is a powerful feature that lets you:
- ✅ Read novels from local files
- ✅ Organize your personal collection
- ✅ Add custom metadata and covers
- ✅ Mix different file formats
- ✅ Keep full control of your library

**Key Requirements:**
- Proper folder structure (`local/{NovelName}/`)
- Supported file formats (`.txt`, `.epub`)
- Consistent chapter naming
- Refresh after changes

**Optional Enhancements:**
- `details.json` for rich metadata
- `cover.jpg` for visual appeal
- Organized naming scheme

Happy reading! 📖
