# IReader AM Package Manager Script

This directory contains the installation script for the [AM package manager](https://github.com/ivan-hc/AM).

## What is AM?

AM (Application Manager) is a package manager for AppImages and portable applications on Linux. It provides:

- Easy installation and removal of applications
- Automatic updates
- Integration with system menus
- Support for delta updates via AppImageUpdate

## Installation via AM

### For all users (requires root):

```bash
am -i ireader
```

### For current user only:

```bash
appman -i ireader
```

## Manual Installation

If you want to install IReader manually using this script:

```bash
# Download the script
wget https://raw.githubusercontent.com/IReaderorg/IReader/main/desktop/am-script/ireader

# Make it executable
chmod +x ireader

# Run it
./ireader
```

## What the Script Does

1. **Downloads** the latest IReader AppImage for your architecture (x86_64 or aarch64)
2. **Installs** it to `~/.local/bin/ireader`
3. **Creates** a desktop entry in `~/.local/share/applications/`
4. **Downloads** the application icon
5. **Updates** desktop and icon caches
6. **Verifies** update information is embedded (for delta updates)

## Delta Updates

The IReader AppImage includes embedded update information:

```
gh-releases-zsync|IReaderorg|IReader|latest|IReader-x86_64.AppImage.zsync
```

This enables efficient delta updates using [AppImageUpdate](https://github.com/AppImageCommunity/AppImageUpdate).

### Using AppImageUpdate

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
appimageupdatetool --check-for-update ~/.local/bin/ireader

# Update (downloads only the diff)
appimageupdatetool ~/.local/bin/ireader
```

### Benefits of Delta Updates

- **Smaller downloads**: Only changed blocks are downloaded (typically 70-90% smaller)
- **Faster updates**: Less data to download means quicker updates
- **Bandwidth efficient**: Great for users with limited or slow internet
- **Automatic verification**: Checksums ensure integrity

## AppImageLauncher Integration

If you have [AppImageLauncher](https://github.com/TheAssassin/AppImageLauncher) installed, it will automatically:

- Recognize the update information
- Offer to check for updates
- Integrate with your system
- Provide a GUI for updates

## Uninstallation

### Via AM:

```bash
am -r ireader
# or
appman -r ireader
```

### Manual:

```bash
rm ~/.local/bin/ireader
rm ~/.local/share/applications/ireader.desktop
rm ~/.local/share/icons/hicolor/256x256/apps/ireader.png
update-desktop-database ~/.local/share/applications
gtk-update-icon-cache ~/.local/share/icons/hicolor
```

## Updating

### Via AM:

```bash
am -u ireader
# or
appman -u ireader
```

### Via AppImageUpdate:

```bash
appimageupdatetool ~/.local/bin/ireader
```

### Manual:

```bash
# Re-run the installation script
./ireader
```

## Supported Architectures

- x86_64 (Intel/AMD 64-bit)
- aarch64 (ARM 64-bit)

## Requirements

- Linux (any distribution)
- wget (for downloading)
- Basic shell utilities (mkdir, chmod, cat)

## Optional Dependencies

- **AppImageUpdate**: For delta updates
- **AppImageLauncher**: For enhanced AppImage integration
- **update-desktop-database**: For desktop entry integration
- **gtk-update-icon-cache**: For icon cache updates

## Troubleshooting

### AppImage won't run

```bash
# Make sure it's executable
chmod +x ~/.local/bin/ireader

# Try running directly
~/.local/bin/ireader
```

### Update information not found

The AppImage should have update information embedded. Verify with:

```bash
appimageupdatetool --check-for-update ~/.local/bin/ireader
```

If it's missing, please report an issue on GitHub.

### Icon not showing

```bash
# Update icon cache manually
gtk-update-icon-cache ~/.local/share/icons/hicolor
```

## Contributing to AM

To add this script to the official AM repository:

1. Fork the [AM repository](https://github.com/ivan-hc/AM)
2. Add the `ireader` script to the `programs/x86_64` directory
3. Test the installation
4. Submit a pull request

See the [AM contribution guide](https://github.com/ivan-hc/AM/blob/main/CONTRIBUTING.md) for details.

## Links

- [IReader GitHub](https://github.com/IReaderorg/IReader)
- [AM Package Manager](https://github.com/ivan-hc/AM)
- [AppImageUpdate](https://github.com/AppImageCommunity/AppImageUpdate)
- [AppImageLauncher](https://github.com/TheAssassin/AppImageLauncher)
- [AppImage Specification](https://github.com/AppImage/AppImageSpec)

## License

This script is part of the IReader project and is licensed under the Mozilla Public License v2.0.
