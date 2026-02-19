# AppImage Delta Updates Setup Guide

This guide explains how to build, distribute, and use IReader AppImages with delta update support.

## Table of Contents

1. [For Developers: Building AppImages](#for-developers-building-appimages)
2. [For Release Managers: Publishing Updates](#for-release-managers-publishing-updates)
3. [For Users: Installing and Updating](#for-users-installing-and-updating)
4. [Technical Details](#technical-details)
5. [Troubleshooting](#troubleshooting)

---

## For Developers: Building AppImages

### Prerequisites

Install required tools on Linux:

```bash
# Ubuntu/Debian
sudo apt install zsync wget

# Fedora
sudo dnf install zsync wget

# Arch Linux
sudo pacman -S zsync wget

# Install appimagetool
wget https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage
chmod +x appimagetool-x86_64.AppImage
sudo mv appimagetool-x86_64.AppImage /usr/local/bin/appimagetool
```

### Building AppImage

#### Option 1: AppImage Only

```bash
./gradlew buildAppImage
```

This creates: `desktop/build/compose/binaries/main/IReader-x86_64.AppImage`

#### Option 2: AppImage + Zsync File (Recommended)

```bash
./gradlew buildAppImageWithZsync
```

This creates:
- `IReader-x86_64.AppImage`
- `IReader-x86_64.AppImage.zsync`

### Verifying Update Information

```bash
./gradlew verifyAppImageUpdateInfo
```

Or manually:

```bash
appimageupdatetool --check-for-update desktop/build/compose/binaries/main/IReader-x86_64.AppImage
```

### Testing Delta Updates

```bash
./gradlew testAppImageDeltaUpdate
```

This shows estimated delta update sizes.

---

## For Release Managers: Publishing Updates

### 1. Build Release AppImage

```bash
# Clean build
./gradlew clean

# Build AppImage with zsync
./gradlew buildAppImageWithZsync
```

### 2. Upload to GitHub Releases

When creating a new release, upload **both** files:

1. `IReader-x86_64.AppImage` - The actual application
2. `IReader-x86_64.AppImage.zsync` - Delta update metadata

**Important**: The zsync file must be in the same location as the AppImage!

### 3. Naming Convention

Follow this naming pattern:
- `IReader-x86_64.AppImage` (for x86_64/AMD64)
- `IReader-aarch64.AppImage` (for ARM64, if supported)

The embedded update information uses wildcards:
```
gh-releases-zsync|IReader-org|IReader|latest|IReader-*-x86_64.AppImage.zsync
```

This allows version numbers in filenames while still matching.

### 4. Release Checklist

- [ ] Build AppImage with zsync
- [ ] Test AppImage locally
- [ ] Verify update information is embedded
- [ ] Upload both .AppImage and .AppImage.zsync files
- [ ] Test delta update from previous version
- [ ] Update release notes with delta update info

---

## For Users: Installing and Updating

### Installation Methods

#### Method 1: AM Package Manager (Recommended)

```bash
# Install AM
wget -q https://raw.githubusercontent.com/ivan-hc/AM/main/APP-MANAGER -O /tmp/am && chmod +x /tmp/am && sudo /tmp/am -i am

# Install IReader
am -i ireader
```

Or for current user only:

```bash
appman -i ireader
```

#### Method 2: Manual Installation

```bash
# Download AppImage
wget https://github.com/IReader-org/IReader/releases/latest/download/IReader-x86_64.AppImage

# Make executable
chmod +x IReader-x86_64.AppImage

# Run
./IReader-x86_64.AppImage
```

#### Method 3: AppImageLauncher

1. Install [AppImageLauncher](https://github.com/TheAssassin/AppImageLauncher)
2. Download IReader AppImage
3. Double-click to integrate with your system
4. AppImageLauncher will handle updates automatically

### Updating IReader

#### Option 1: Using AppImageUpdate (Delta Updates)

Install AppImageUpdate:

```bash
# Via AM
am -i appimageupdatetool

# Or download directly
wget https://github.com/AppImageCommunity/AppImageUpdate/releases/latest/download/appimageupdatetool-x86_64.AppImage
chmod +x appimageupdatetool-x86_64.AppImage
```

Update IReader:

```bash
# Check for updates
appimageupdatetool --check-for-update IReader-x86_64.AppImage

# Apply delta update
appimageupdatetool IReader-x86_64.AppImage
```

**Benefits**:
- Only downloads changed blocks (typically 10-30% of full size)
- Faster updates
- Saves bandwidth

#### Option 2: Using AM

```bash
am -u ireader
```

#### Option 3: Manual Update

Download the latest version and replace the old one.

### Update Frequency

- **Stable releases**: Check monthly
- **Beta releases**: Check weekly
- **Nightly builds**: Check daily (if available)

---

## Technical Details

### Update Information Format

IReader AppImages embed update information in this format:

```
gh-releases-zsync|IReader-org|IReader|latest|IReader-*-x86_64.AppImage.zsync
```

This tells the updater:
- **Transport**: GitHub Releases with zsync
- **Owner**: IReader-org
- **Repository**: IReader
- **Tag**: latest (always get the latest release)
- **Filename pattern**: IReader-*-x86_64.AppImage.zsync

### How Delta Updates Work

1. **Initial Download**: User downloads full AppImage (e.g., 150 MB)
2. **Update Available**: New version released with changes
3. **Zsync Check**: Updater downloads .zsync file (~500 KB)
4. **Delta Calculation**: Compares local file with remote
5. **Block Download**: Downloads only changed blocks (e.g., 15 MB)
6. **Reconstruction**: Rebuilds new AppImage from old + delta
7. **Verification**: Checksums verify integrity

**Result**: 15 MB download instead of 150 MB (90% savings!)

### Zsync File Structure

The .zsync file contains:
- Block checksums (for identifying changes)
- File metadata (size, modification time)
- URL to the full AppImage (fallback)

### AppImage Structure

```
IReader-x86_64.AppImage
├── AppRun (launcher script)
├── ireader.desktop (desktop entry)
├── ireader.png (icon)
├── .DirIcon (symlink to icon)
└── usr/
    ├── bin/
    │   └── IReader.jar
    ├── lib/ (if needed)
    └── share/
        ├── applications/
        └── icons/
```

### Update Information Embedding

During build, appimagetool embeds update information:

```bash
appimagetool --updateinformation "gh-releases-zsync|..." IReader.AppDir
```

This information is stored in the AppImage's ELF header.

---

## Troubleshooting

### AppImage Won't Run

**Problem**: Permission denied

```bash
chmod +x IReader-x86_64.AppImage
```

**Problem**: FUSE not available

```bash
# Extract and run
./IReader-x86_64.AppImage --appimage-extract
./squashfs-root/AppRun
```

### Update Check Fails

**Problem**: No update information found

```bash
# Verify update info
appimageupdatetool --check-for-update IReader-x86_64.AppImage
```

If missing, re-download from official releases.

**Problem**: Network error

- Check internet connection
- Verify GitHub is accessible
- Try again later

### Delta Update Fails

**Problem**: Checksum mismatch

- Delete old AppImage
- Download fresh copy
- Try delta update again

**Problem**: Insufficient disk space

- Free up space (need 2x AppImage size temporarily)
- Or download full update

### AppImageLauncher Issues

**Problem**: Not recognizing update information

- Update AppImageLauncher to latest version
- Re-integrate the AppImage

### AM Installation Issues

**Problem**: Script fails to download

```bash
# Check AM is up to date
am -u am

# Try manual installation
wget https://github.com/IReader-org/IReader/releases/latest/download/IReader-x86_64.AppImage
chmod +x IReader-x86_64.AppImage
```

---

## Performance Metrics

Based on typical updates:

| Update Type | Full Download | Delta Update | Savings |
|-------------|---------------|--------------|---------|
| Minor bug fix | 150 MB | 10-20 MB | 85-90% |
| Feature update | 150 MB | 20-40 MB | 70-85% |
| Major version | 150 MB | 40-60 MB | 60-70% |

**Average savings**: 75-80% bandwidth and time

---

## Additional Resources

- [AppImage Documentation](https://docs.appimage.org/)
- [AppImageUpdate GitHub](https://github.com/AppImageCommunity/AppImageUpdate)
- [AM Package Manager](https://github.com/ivan-hc/AM)
- [AppImageLauncher](https://github.com/TheAssassin/AppImageLauncher)
- [Zsync Documentation](http://zsync.moria.org.uk/)

---

## Contributing

### Improving Delta Updates

To improve delta update efficiency:

1. **Minimize changes**: Keep stable code in same locations
2. **Compress wisely**: Use consistent compression settings
3. **Test updates**: Verify delta sizes before release
4. **Document changes**: Help users understand update sizes

### Reporting Issues

If you encounter issues with AppImage or delta updates:

1. Check this guide first
2. Search existing issues on GitHub
3. Create new issue with:
   - AppImage version
   - Linux distribution
   - Error messages
   - Steps to reproduce

---

## License

This guide is part of the IReader project and is licensed under the Mozilla Public License v2.0.
