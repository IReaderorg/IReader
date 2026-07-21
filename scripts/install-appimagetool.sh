#!/bin/bash
# Script to install appimagetool without FUSE requirement

set -e

echo "=== Installing appimagetool (FUSE-free method) ==="
echo ""

# Download appimagetool
echo "1. Downloading appimagetool..."
wget -O /tmp/appimagetool-x86_64.AppImage \
    https://github.com/AppImage/AppImageKit/releases/download/continuous/appimagetool-x86_64.AppImage

# Make executable
chmod +x /tmp/appimagetool-x86_64.AppImage

# Extract (avoids FUSE requirement)
echo "2. Extracting appimagetool (no FUSE needed)..."
cd /tmp
./appimagetool-x86_64.AppImage --appimage-extract

# Install system-wide
echo "3. Installing to /opt/appimagetool..."
sudo mv squashfs-root /opt/appimagetool
sudo ln -sf /opt/appimagetool/AppRun /usr/local/bin/appimagetool

# Cleanup
rm -f /tmp/appimagetool-x86_64.AppImage

echo ""
echo "✓ appimagetool installed successfully!"
echo ""
echo "Test it:"
echo "  appimagetool --version"
echo ""
echo "Now you can build IReader AppImages:"
echo "  cd ~/StudioProjects/IReader"
echo "  ./gradlew buildAppImageWithZsync"
