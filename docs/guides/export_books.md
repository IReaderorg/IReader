# How to Export Books from IReader

This guide explains how to export your books from IReader to various formats and upload them to Google Books, Kindle, or other reading platforms.

## Quick Start

1. Open any book in IReader
2. Tap the menu button (⋮) in the top right
3. Select "Export as EPUB"
4. Choose your export options
5. Wait for the export to complete
6. Find your file in `Downloads/IReader/`

## Export Formats

### EPUB (Recommended)

**Best for:**
- Google Books
- Apple Books
- Most e-readers
- Desktop reading apps

**Features:**
- Reflowable text (adjusts to screen size)
- Supports cover images
- Table of contents
- Metadata (title, author, description)
- Customizable formatting

### CBZ (Coming Soon)

**Best for:**
- Manga and comics
- Image-heavy books
- Comic book readers

**Features:**
- Image-based format
- Preserves original layout
- Smaller file size for images

## Export Options

### Basic Options

**Include Cover Image**
- ✅ Recommended: ON
- Downloads and embeds the book cover
- Makes your library look better
- Required for Google Books thumbnail

**Select Chapters**
- Export all chapters (default)
- Or select specific chapters to export
- Useful for partial exports or testing

### Formatting Options

**Typography**
- Serif (default) - Traditional book font
- Sans-serif - Modern, clean font
- Default - System font

**Paragraph Spacing**
- Controls space between paragraphs
- Range: 0.5 to 2.0
- Default: 1.0

**Chapter Heading Size**
- Controls size of chapter titles
- Range: 1.0 to 3.0
- Default: 2.0

### Advanced Options

**Use Translated Content**
- Export translated chapters instead of original
- Requires translations to be available
- Select target language

## Uploading to Google Books

### Step 1: Export from IReader

1. Open the book you want to export
2. Tap menu (⋮) → "Export as EPUB"
3. Enable "Include Cover Image"
4. Tap "Export"
5. Wait for "Export complete" message

### Step 2: Find the Exported File

**On Android:**
- Open Files app
- Navigate to `Downloads/IReader/`
- Look for `BookTitle - Author.epub`

**On iOS:**
- Open Files app
- Navigate to `IReader/Exports/`
- Look for `BookTitle.epub`

### Step 3: Upload to Google Books

**Method 1: Mobile App**

1. Open Google Play Books app
2. Tap your profile picture
3. Select "Settings"
4. Enable "Upload books"
5. Go back to library
6. Tap "+" button
7. Select "Upload files"
8. Choose your EPUB file
9. Wait for upload to complete

**Method 2: Web Browser**

1. Go to [play.google.com/books](https://play.google.com/books)
2. Click "My Books"
3. Click "Upload files" button
4. Select your EPUB file
5. Wait for processing
6. Book appears in your library

### Troubleshooting Google Books

**"File format not supported"**
- Ensure file has `.epub` extension
- Try exporting again
- Check file isn't corrupted (should be >1KB)

**"Upload failed"**
- Check internet connection
- Ensure file size is under 100MB
- Try uploading from web instead of app

**Cover image not showing**
- Re-export with "Include Cover Image" enabled
- Check if original book has valid cover URL
- Cover may take a few minutes to process

## Uploading to Kindle

Kindle doesn't support EPUB directly, but you have several options:

### Method 1: Send to Kindle (Easiest)

1. **Find your Kindle email:**
   - Go to [amazon.com/mycd](https://www.amazon.com/mycd)
   - Click "Preferences"
   - Find "Send-to-Kindle Email" (e.g., username@kindle.com)

2. **Send the EPUB:**
   - Email the EPUB file to your Kindle email
   - Subject: (optional)
   - Amazon will convert it automatically
   - Book appears on your Kindle in a few minutes

3. **Approve sender (first time only):**
   - Go to "Personal Document Settings"
   - Add your email to "Approved Personal Document E-mail List"

### Method 2: Kindle Create (Best Quality)

1. **Download Kindle Create:**
   - Go to [kdp.amazon.com/tools](https://kdp.amazon.com/tools)
   - Download Kindle Create for your platform
   - Install and open

2. **Import EPUB:**
   - Click "New Project"
   - Select "Import"
   - Choose your EPUB file
   - Wait for import to complete

3. **Preview and Adjust:**
   - Check formatting
   - Adjust chapter breaks if needed
   - Preview on different devices

4. **Export:**
   - Click "Publish"
   - Choose "Export"
   - Save as KPF file

5. **Transfer to Kindle:**
   - Connect Kindle via USB
   - Copy KPF to `Documents` folder
   - Or email KPF to your Kindle email

### Method 3: Calibre Conversion (Most Control)

1. **Install Calibre:**
   - Download from [calibre-ebook.com](https://calibre-ebook.com/)
   - Install and open

2. **Add EPUB:**
   - Click "Add books"
   - Select your EPUB file
   - Book appears in library

3. **Convert to Kindle Format:**
   - Select the book
   - Click "Convert books"
   - Choose output format: MOBI or AZW3
   - Click "OK"
   - Wait for conversion

4. **Transfer to Kindle:**
   - Connect Kindle via USB
   - Right-click book → "Send to device"
   - Or email MOBI file to Kindle email

## Uploading to Other Platforms

### Apple Books (iOS/Mac)

1. Export EPUB from IReader
2. Open Files app (iOS) or Finder (Mac)
3. Tap/double-click the EPUB file
4. Choose "Open in Books"
5. Book is added to your library

### Kobo

1. Export EPUB from IReader
2. Connect Kobo via USB
3. Copy EPUB to Kobo's root folder
4. Safely eject Kobo
5. Book appears in library

### Nook

1. Export EPUB from IReader
2. Connect Nook via USB
3. Copy EPUB to `My Files/Books/` folder
4. Safely eject Nook
5. Book appears in library

## Tips and Best Practices

### Before Exporting

- ✅ Ensure book has a title and author
- ✅ Check if cover image is available
- ✅ Download all chapters you want to export
- ✅ Verify translations if exporting translated content

### During Export

- ✅ Keep IReader open until export completes
- ✅ Ensure stable internet connection (for cover download)
- ✅ Don't start multiple exports simultaneously
- ✅ Wait for "Export complete" message

### After Export

- ✅ Verify file was created (check file size >1KB)
- ✅ Test opening in an EPUB reader
- ✅ Check cover image appears
- ✅ Verify chapter order is correct

### Troubleshooting

**Export fails immediately**
- Check storage space (need at least 50MB free)
- Grant storage permissions to IReader
- Restart IReader and try again

**Export takes very long**
- Large books (>500 chapters) take longer
- Cover image download may be slow
- Check internet connection
- Be patient, don't cancel

**Exported file is very small (<1KB)**
- Export failed, try again
- Check if chapters have content
- Ensure book is fully downloaded

**Cover image missing**
- Cover download failed (check internet)
- Original book may not have cover
- Export still succeeds without cover
- You can add cover later in Calibre

## Frequently Asked Questions

**Q: Can I export multiple books at once?**
A: Not yet, but batch export is planned for a future update.

**Q: Can I export to PDF?**
A: Not directly, but you can convert EPUB to PDF using Calibre.

**Q: Will my reading progress be exported?**
A: No, only the book content is exported. Reading progress stays in IReader.

**Q: Can I export books with DRM?**
A: No, DRM-protected content cannot be exported.

**Q: How do I export manga/comics?**
A: CBZ export is coming soon. For now, use EPUB export.

**Q: Can I customize the EPUB styling?**
A: Yes, use the formatting options in the export dialog. For advanced customization, edit the EPUB in Calibre.

**Q: Where are exported files saved?**
A: Android: `Downloads/IReader/`, iOS: `Documents/Exports/`, Desktop: User-selected location

**Q: Can I share exported EPUBs with friends?**
A: Yes, but respect copyright laws. Only share books you have rights to distribute.

## Need Help?

If you encounter issues:

1. Check the [troubleshooting section](#troubleshooting) above
2. Read the [detailed technical guide](../EPUB_EXPORT_FIX.md)
3. Check logs for error messages
4. Report issues on GitHub with:
   - Book title and author
   - Error message
   - Export options used
   - Device information

## Related Guides

- [Features Overview](features.md) - All IReader features
- [Local Source Guide](localsource-guide.md) - Import local books
- [Sync & Backup](sync_backup.md) - Backup your library
