# Fastlane Setup for IReader

This directory contains Fastlane configuration for automated builds and F-Droid metadata.

## Structure

```
fastlane/
├── Fastfile                          # Fastlane automation scripts
├── metadata/
│   └── android/
│       └── en-US/
│           ├── title.txt             # App name (max 30 chars)
│           ├── short_description.txt # Short description (max 80 chars)
│           ├── full_description.txt  # Full description (max 4000 chars)
│           ├── changelogs/
│           │   └── 50.txt           # Changelog for versionCode 50
│           └── images/
│               ├── icon.png          # App icon (512x512)
│               ├── featureGraphic.png # Feature graphic (1024x500)
│               └── phoneScreenshots/ # Phone screenshots
│                   ├── 1.png
│                   ├── 2.png
│                   └── ...
```

## F-Droid Integration

F-Droid automatically reads this metadata structure. When you update:

1. **Descriptions**: Edit the `.txt` files in `en-US/`
2. **Changelogs**: Add new files in `changelogs/` named `{versionCode}.txt`
3. **Screenshots**: Add PNG/JPG files to `images/phoneScreenshots/`
4. **Icon**: Place `icon.png` (512x512) in `images/`

## Adding Screenshots

See `metadata/android/en-US/images/SCREENSHOTS_README.md` for detailed instructions.

## Localization

To add more languages, create additional directories:

```
metadata/android/
├── en-US/
├── es-ES/  # Spanish
├── fr-FR/  # French
├── de-DE/  # German
└── ...
```

Each language directory should have the same structure as `en-US/`.

## Usage with Fastlane

If you have Fastlane installed:

```bash
# Build F-Droid release
fastlane android fdroid

# Build standard release
fastlane android release
```

## For F-Droid Submission

This structure is ready for F-Droid. Just ensure:

1. ✓ Metadata files are filled out
2. ✓ Screenshots are added
3. ✓ Icon is present
4. ✓ Changelogs are up to date

F-Droid will automatically use this metadata when building your app listing.
