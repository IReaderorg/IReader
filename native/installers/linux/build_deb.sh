#!/bin/bash
# Build Debian package for IReader

set -e

# Configuration
APP_NAME="ireader"
VERSION="${1:-1.0.0}"
ARCH="amd64"
SOURCE_DIR="${2:-../../build/release}"
OUTPUT_DIR="${3:-../../build/installers}"

echo "Building Debian package for $APP_NAME v$VERSION..."

# Check for required tools
if ! command -v dpkg-deb &> /dev/null; then
    echo "Error: dpkg-deb not found. Please install dpkg."
    exit 1
fi

# Verify source files exist
if [ ! -f "$SOURCE_DIR/ireader.jar" ]; then
    echo "Error: $SOURCE_DIR/ireader.jar not found"
    exit 1
fi

# Create package directory structure
PKG_DIR="$OUTPUT_DIR/${APP_NAME}_${VERSION}_${ARCH}"
rm -rf "$PKG_DIR"
mkdir -p "$PKG_DIR"

# Create directory structure
mkdir -p "$PKG_DIR/DEBIAN"
mkdir -p "$PKG_DIR/usr/lib/$APP_NAME"
mkdir -p "$PKG_DIR/usr/lib/$APP_NAME/native"
mkdir -p "$PKG_DIR/usr/bin"
mkdir -p "$PKG_DIR/usr/share/applications"
mkdir -p "$PKG_DIR/usr/share/icons/hicolor/48x48/apps"
mkdir -p "$PKG_DIR/usr/share/icons/hicolor/128x128/apps"
mkdir -p "$PKG_DIR/usr/share/icons/hicolor/256x256/apps"
mkdir -p "$PKG_DIR/usr/share/doc/$APP_NAME"

# Copy control file
cp debian/control "$PKG_DIR/DEBIAN/"

# Update version in control file
sed -i "s/Version: .*/Version: $VERSION/" "$PKG_DIR/DEBIAN/control"

# Create postinst script
cat > "$PKG_DIR/DEBIAN/postinst" << 'EOF'
#!/bin/bash
set -e

# Update icon cache
if command -v gtk-update-icon-cache &> /dev/null; then
    gtk-update-icon-cache -f -t /usr/share/icons/hicolor || true
fi

# Update desktop database
if command -v update-desktop-database &> /dev/null; then
    update-desktop-database -q || true
fi

# Run ldconfig to update library cache
ldconfig || true

exit 0
EOF

chmod 755 "$PKG_DIR/DEBIAN/postinst"

# Create postrm script
cat > "$PKG_DIR/DEBIAN/postrm" << 'EOF'
#!/bin/bash
set -e

if [ "$1" = "remove" ] || [ "$1" = "purge" ]; then
    # Update icon cache
    if command -v gtk-update-icon-cache &> /dev/null; then
        gtk-update-icon-cache -f -t /usr/share/icons/hicolor || true
    fi
    
    # Update desktop database
    if command -v update-desktop-database &> /dev/null; then
        update-desktop-database -q || true
    fi
    
    # Run ldconfig
    ldconfig || true
fi

exit 0
EOF

chmod 755 "$PKG_DIR/DEBIAN/postrm"

# Copy application files
echo "Copying application files..."
cp "$SOURCE_DIR/ireader.jar" "$PKG_DIR/usr/lib/$APP_NAME/"

# Copy native libraries
if [ -d "$SOURCE_DIR/native" ]; then
    cp "$SOURCE_DIR/native"/*.so "$PKG_DIR/usr/lib/$APP_NAME/native/" 2>/dev/null || true
fi

# Create launcher script
cat > "$PKG_DIR/usr/bin/$APP_NAME" << 'EOF'
#!/bin/bash
# IReader launcher script

# Set library path for native libraries
export LD_LIBRARY_PATH="/usr/lib/ireader/native:$LD_LIBRARY_PATH"

# Launch application
exec java -jar /usr/lib/ireader/ireader.jar "$@"
EOF

chmod 755 "$PKG_DIR/usr/bin/$APP_NAME"

# Create desktop entry
cat > "$PKG_DIR/usr/share/applications/$APP_NAME.desktop" << EOF
[Desktop Entry]
Version=1.0
Type=Application
Name=IReader
GenericName=eBook Reader
Comment=Modern eBook reader with offline text-to-speech
Exec=$APP_NAME %F
Icon=$APP_NAME
Terminal=false
Categories=Office;Viewer;Literature;
MimeType=application/epub+zip;application/pdf;text/plain;
Keywords=ebook;reader;epub;pdf;tts;text-to-speech;
StartupNotify=true
EOF

# Copy icons (if available)
if [ -f "$SOURCE_DIR/icons/48x48.png" ]; then
    cp "$SOURCE_DIR/icons/48x48.png" "$PKG_DIR/usr/share/icons/hicolor/48x48/apps/$APP_NAME.png"
fi
if [ -f "$SOURCE_DIR/icons/128x128.png" ]; then
    cp "$SOURCE_DIR/icons/128x128.png" "$PKG_DIR/usr/share/icons/hicolor/128x128/apps/$APP_NAME.png"
fi
if [ -f "$SOURCE_DIR/icons/256x256.png" ]; then
    cp "$SOURCE_DIR/icons/256x256.png" "$PKG_DIR/usr/share/icons/hicolor/256x256/apps/$APP_NAME.png"
fi

# Copy documentation
cp "$SOURCE_DIR/../LICENSE" "$PKG_DIR/usr/share/doc/$APP_NAME/" 2>/dev/null || echo "License file not found"
cp "$SOURCE_DIR/../README.md" "$PKG_DIR/usr/share/doc/$APP_NAME/" 2>/dev/null || echo "README not found"

# Create copyright file
cat > "$PKG_DIR/usr/share/doc/$APP_NAME/copyright" << EOF
Format: https://www.debian.org/doc/packaging-manuals/copyright-format/1.0/
Upstream-Name: IReader
Source: https://github.com/yourusername/ireader

Files: *
Copyright: 2024 IReader Team
License: MPL-2.0
 Mozilla Public License Version 2.0
 .
 See /usr/share/common-licenses/MPL-2.0 for the full license text.
EOF

# Set permissions
find "$PKG_DIR" -type d -exec chmod 755 {} \;
find "$PKG_DIR" -type f -exec chmod 644 {} \;
chmod 755 "$PKG_DIR/usr/bin/$APP_NAME"
chmod 755 "$PKG_DIR/DEBIAN/postinst"
chmod 755 "$PKG_DIR/DEBIAN/postrm"

# Build package
echo "Building Debian package..."
dpkg-deb --build --root-owner-group "$PKG_DIR"

# Move to output directory
DEB_FILE="${APP_NAME}_${VERSION}_${ARCH}.deb"
mv "${PKG_DIR}.deb" "$OUTPUT_DIR/$DEB_FILE"

# Calculate checksum
echo "Calculating checksum..."
sha256sum "$OUTPUT_DIR/$DEB_FILE" | awk '{print $1}' > "$OUTPUT_DIR/$DEB_FILE.sha256"

# Clean up
rm -rf "$PKG_DIR"

echo "Debian package created successfully: $OUTPUT_DIR/$DEB_FILE"
echo "SHA256: $(cat $OUTPUT_DIR/$DEB_FILE.sha256)"

# Verify package
echo "Verifying package..."
dpkg-deb --info "$OUTPUT_DIR/$DEB_FILE"
dpkg-deb --contents "$OUTPUT_DIR/$DEB_FILE"

echo "Done!"
