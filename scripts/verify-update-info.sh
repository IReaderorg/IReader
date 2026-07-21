#!/bin/bash

APPIMAGE="desktop/build/compose/binaries/main/IReader-x86_64.AppImage"
ZSYNC="desktop/build/compose/binaries/main/IReader-x86_64.AppImage.zsync"

echo "=== Verifying AppImage Update Information ==="
echo ""

# Check files exist
if [ ! -f "$APPIMAGE" ]; then
    echo "❌ AppImage not found: $APPIMAGE"
    exit 1
fi

echo "✓ AppImage exists: $APPIMAGE"
echo "  Size: $(du -h "$APPIMAGE" | cut -f1)"

if [ -f "$ZSYNC" ]; then
    echo "✓ Zsync file exists: $ZSYNC"
    echo "  Size: $(du -h "$ZSYNC" | cut -f1)"
else
    echo "❌ Zsync file missing: $ZSYNC"
fi

echo ""
echo "Extracting update information from AppImage..."
echo ""

# Extract the AppImage to read the .desktop file
TEMP_DIR=$(mktemp -d)
cd "$TEMP_DIR"
"$OLDPWD/$APPIMAGE" --appimage-extract >/dev/null 2>&1

if [ -f "squashfs-root/ireader.desktop" ]; then
    echo "Desktop file contents:"
    grep -i "appimage" "squashfs-root/ireader.desktop" || echo "  (no AppImage-specific entries)"
fi

cd "$OLDPWD"
rm -rf "$TEMP_DIR"

echo ""
echo "To test with appimageupdatetool (if installed):"
echo "  appimageupdatetool --check-for-update $APPIMAGE"
echo ""
echo "To install appimageupdatetool:"
echo "  wget https://github.com/AppImageCommunity/AppImageUpdate/releases/latest/download/appimageupdatetool-x86_64.AppImage"
echo "  chmod +x appimageupdatetool-x86_64.AppImage"
echo "  ./appimageupdatetool-x86_64.AppImage --check-for-update $APPIMAGE"
