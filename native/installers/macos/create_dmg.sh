#!/bin/bash
# Create macOS DMG installer for IReader with code signing

set -e

# Configuration
APP_NAME="IReader"
VERSION="${1:-1.0.0}"
SOURCE_DIR="${2:-../../build/release}"
OUTPUT_DIR="${3:-../../build/installers}"
SIGNING_IDENTITY="${CODESIGN_IDENTITY:-Developer ID Application}"
TEAM_ID="${APPLE_TEAM_ID}"
APPLE_ID="${APPLE_ID}"
NOTARIZATION_PASSWORD="${NOTARIZATION_PASSWORD}"

echo "Creating macOS DMG installer for $APP_NAME v$VERSION..."

# Check for required tools
if ! command -v codesign &> /dev/null; then
    echo "Error: codesign not found. Please install Xcode Command Line Tools."
    exit 1
fi

if ! command -v hdiutil &> /dev/null; then
    echo "Error: hdiutil not found."
    exit 1
fi

# Verify source app exists
if [ ! -d "$SOURCE_DIR/$APP_NAME.app" ]; then
    echo "Error: $SOURCE_DIR/$APP_NAME.app not found"
    exit 1
fi

# Create output directory
mkdir -p "$OUTPUT_DIR"

# Create temporary directory for DMG contents
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

echo "Copying application to temporary directory..."
cp -R "$SOURCE_DIR/$APP_NAME.app" "$TEMP_DIR/"

# Sign the application and all libraries
echo "Code signing application..."
find "$TEMP_DIR/$APP_NAME.app" -name "*.dylib" -exec codesign \
    --force \
    --sign "$SIGNING_IDENTITY" \
    --options runtime \
    --timestamp \
    {} \;

# Sign frameworks
find "$TEMP_DIR/$APP_NAME.app" -name "*.framework" -exec codesign \
    --force \
    --sign "$SIGNING_IDENTITY" \
    --options runtime \
    --timestamp \
    {} \;

# Sign the main app bundle
codesign \
    --force \
    --sign "$SIGNING_IDENTITY" \
    --options runtime \
    --timestamp \
    --entitlements entitlements.plist \
    --deep \
    "$TEMP_DIR/$APP_NAME.app"

# Verify signature
echo "Verifying code signature..."
codesign --verify --deep --strict --verbose=2 "$TEMP_DIR/$APP_NAME.app"

# Create symbolic link to Applications folder
ln -s /Applications "$TEMP_DIR/Applications"

# Create DMG
DMG_NAME="$APP_NAME-$VERSION-macOS.dmg"
DMG_PATH="$OUTPUT_DIR/$DMG_NAME"

echo "Creating DMG..."
hdiutil create \
    -volname "$APP_NAME" \
    -srcfolder "$TEMP_DIR" \
    -ov \
    -format UDZO \
    -imagekey zlib-level=9 \
    "$DMG_PATH"

# Sign the DMG
echo "Signing DMG..."
codesign \
    --force \
    --sign "$SIGNING_IDENTITY" \
    --timestamp \
    "$DMG_PATH"

# Notarize if credentials are provided
if [ -n "$APPLE_ID" ] && [ -n "$NOTARIZATION_PASSWORD" ] && [ -n "$TEAM_ID" ]; then
    echo "Submitting for notarization..."
    
    # Submit for notarization
    xcrun notarytool submit "$DMG_PATH" \
        --apple-id "$APPLE_ID" \
        --password "$NOTARIZATION_PASSWORD" \
        --team-id "$TEAM_ID" \
        --wait
    
    # Staple notarization ticket
    echo "Stapling notarization ticket..."
    xcrun stapler staple "$DMG_PATH"
    
    echo "Notarization complete!"
else
    echo "Warning: Notarization skipped (credentials not provided)"
    echo "Set APPLE_ID, NOTARIZATION_PASSWORD, and APPLE_TEAM_ID to enable notarization"
fi

# Calculate checksum
echo "Calculating checksum..."
shasum -a 256 "$DMG_PATH" | awk '{print $1}' > "$DMG_PATH.sha256"

echo "DMG created successfully: $DMG_PATH"
echo "SHA256: $(cat $DMG_PATH.sha256)"
echo "Done!"
