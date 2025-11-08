# Test Novel for Local Library

This is a sample novel to test the Local Library feature.

## Installation Instructions

### For Android:
1. Connect your device to a computer via USB
2. Navigate to: `/storage/emulated/0/Android/data/ir.kazemcodes.infinityreader.debug/files/local/`
3. Copy the entire `test_local_novel` folder to the `local` directory
4. Open the app
5. Go to Browse > Sources > Local Source
6. Pull down to refresh
7. You should see "The Adventures of Test Novel"

### For Desktop:
1. Navigate to: `~/.ireader/local/` (or `C:\Users\[YourName]\.ireader\local\` on Windows)
2. Copy the entire `test_local_novel` folder to the `local` directory
3. Open the app
4. Go to Browse > Sources > Local Source
5. Pull down to refresh
6. You should see "The Adventures of Test Novel"

## Folder Structure

```
test_local_novel/
├── details.json                      # Metadata
├── Chapter 001 - The Beginning.txt   # Chapter 1
├── Chapter 002 - The Discovery.txt   # Chapter 2
├── Chapter 003 - The Organization.txt # Chapter 3
├── Chapter 004 - The Future.txt      # Chapter 4
└── cover.jpg                         # (Add your own cover image)
```

## Adding a Cover Image

To add a cover image:
1. Find or create a JPG or PNG image (recommended size: 300x450 pixels)
2. Name it `cover.jpg` or `cover.png`
3. Place it in the `test_local_novel` folder
4. Refresh the Local Source in the app

## Customizing

Feel free to:
- Edit `details.json` to change title, author, description, etc.
- Add more chapters (use the naming pattern: `Chapter 00X - Title.txt`)
- Replace the text files with EPUB files
- Create your own novels following this structure

## Notes

- Each file = one chapter in the app
- Files are sorted alphabetically (use leading zeros: 001, 002, 003...)
- Supported formats: .txt and .epub
- The folder name becomes the default title if details.json is missing
