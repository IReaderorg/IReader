# ePub Export Implementation

## Overview

This module implements the End of Life Management - ePub Export feature as specified in the advanced reader features specification.

## Components

### 1. ExportNovelAsEpubUseCase

A clean wrapper around the platform-specific `EpubCreator` implementations. This use case provides:

- **Export functionality**: Exports a novel as an ePub file with progress reporting
- **File picker integration**: Initiates platform-specific file picker dialogs
- **Progress callbacks**: Reports export progress to the UI

**Requirements Addressed**: 15.1, 15.2, 15.3, 15.4, 15.5, 15.6, 15.7, 15.8, 15.9, 15.10

### 2. HtmlContentCleaner

A utility class for cleaning HTML content before ePub export. It removes:

- Script tags and their content
- Style tags and their content
- HTML comments (often used for ads)
- Common ad containers (divs with "ad" class/id)
- Watermark elements
- iframe tags (often used for ads)
- noscript tags
- Inline event handlers (onclick, onload, etc.)
- Inline styles (we use our own stylesheet)
- Data attributes (may contain tracking)
- Analytics and tracking elements
- Empty paragraphs and divs
- Excessive whitespace

**Requirements Addressed**: 15.2 - HTML content cleaning

### 3. EpubCreator (Platform-Specific)

Platform-specific implementations for Android and Desktop that handle:

- **ePub file structure creation**: META-INF, OEBPS, mimetype
- **Metadata generation**: title, author, cover, table of contents
- **Chapter-to-XHTML conversion**: with proper formatting
- **toc.ncx file creation**: for navigation
- **Cover image inclusion**: if available
- **CSS stylesheet**: for consistent formatting
- **Progress reporting**: via callbacks

**Requirements Addressed**: 15.1, 15.3, 15.4, 15.5, 15.6, 15.7, 15.8, 15.9, 15.10

## Usage

### From ViewModel

```kotlin
class BookDetailViewModel(
    // ... other dependencies
    val exportNovelAsEpub: ExportNovelAsEpubUseCase
) {
    fun exportNovelAsEpub(book: Book, uri: Uri) {
        applicationScope.launch {
            try {
                exportNovelAsEpub(book, uri) { progress ->
                    showSnackBar(UiText.DynamicString(progress))
                }
                showSnackBar(UiText.MStringResource(MR.strings.success))
            } catch (e: Exception) {
                showSnackBar(UiText.ExceptionString(e))
            }
        }
    }
}
```

### From UI

```kotlin
// Request file picker
vm.exportNovelAsEpub.requestExport(book) { intent ->
    filePicker.launch(intent)
}

// After user selects location
val onExport = ActivityResultListener(onSuccess = { uri ->
    vm.exportNovelAsEpub(book, uri) { progress ->
        vm.showSnackBar(UiText.DynamicString(progress))
    }
})
```

## ePub Structure

The generated ePub files follow the EPUB 3.0 specification:

```
novel.epub
├── mimetype                    # MIME type declaration (uncompressed)
├── META-INF/
│   └── container.xml          # Container metadata
└── OEBPS/
    ├── content.opf            # Package document (metadata, manifest, spine)
    ├── toc.ncx                # NCX table of contents (EPUB 2 compatibility)
    ├── nav.xhtml              # EPUB 3 navigation document
    ├── stylesheet.css         # CSS for formatting
    ├── cover.jpg              # Cover image (if available)
    ├── chapter0.xhtml         # Chapter 1
    ├── chapter1.xhtml         # Chapter 2
    └── ...                    # Additional chapters
```

## HTML Cleaning Process

1. **Detect HTML**: Check if content contains HTML tags
2. **Clean HTML**: Remove scripts, styles, ads, watermarks
3. **Extract Text**: Convert to plain text if needed
4. **Escape XML**: Ensure proper XML encoding
5. **Format**: Wrap in proper XHTML structure

## Progress Reporting

The export process reports progress at key stages:

1. "Loading chapters..."
2. "Loading cover image..."
3. "Creating EPUB structure..."
4. "Writing metadata..."
5. "Adding cover image..."
6. "Writing chapter X/Y: [Chapter Name]"
7. "Finalizing EPUB..."
8. "EPUB created successfully!"

## Error Handling

The implementation handles various error scenarios:

- **No chapters found**: Throws exception with clear message
- **File write errors**: Propagates with context
- **Invalid URI**: Handled by platform-specific code
- **Insufficient storage**: Caught and reported to user
- **Corrupted content**: Cleaned and sanitized

## Platform Differences

### Android
- Uses Android's ContentResolver for file access
- Supports Storage Access Framework (SAF)
- Includes cover image from cache
- Uses Intent for file picker

### Desktop
- Uses Java File I/O
- Direct file system access
- Simpler file picker integration
- Estimates cover image path

## Testing

The implementation can be tested by:

1. Exporting a novel with multiple chapters
2. Verifying the ePub structure is valid
3. Opening the ePub in an ePub reader (Calibre, Adobe Digital Editions, etc.)
4. Checking that content is properly formatted
5. Verifying that ads and watermarks are removed
6. Confirming progress updates are displayed

## Future Enhancements

Potential improvements for future versions:

- Support for custom CSS themes
- Chapter grouping (parts/volumes)
- Footnotes and endnotes
- Image embedding from chapters
- Font embedding
- Advanced metadata (series, tags, etc.)
- Batch export (multiple novels)
- Export format options (EPUB 2 vs EPUB 3)
