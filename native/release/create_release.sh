#!/bin/bash
# Create release package for IReader

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."
VERSION="${1:-1.0.0}"
OUTPUT_DIR="${2:-$PROJECT_ROOT/build/release}"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo "IReader Release Package Creator"
echo "================================"
echo "Version: $VERSION"
echo "Output: $OUTPUT_DIR"
echo ""

# Create output directory
mkdir -p "$OUTPUT_DIR"

# 1. Build the project
echo "1. Building project..."
cd "$PROJECT_ROOT"
./gradlew clean build --no-daemon

if [ $? -ne 0 ]; then
    echo "Error: Build failed"
    exit 1
fi

echo -e "${GREEN}✓${NC} Build complete"
echo ""

# 2. Package libraries and resources
echo "2. Packaging libraries and resources..."

# Create package structure
PACKAGE_DIR="$OUTPUT_DIR/ireader-$VERSION"
rm -rf "$PACKAGE_DIR"
mkdir -p "$PACKAGE_DIR"/{lib,native,docs,licenses}

# Copy JAR files
echo "  - Copying JAR files..."
find "$PROJECT_ROOT" -name "*.jar" -path "*/build/libs/*" -exec cp {} "$PACKAGE_DIR/lib/" \;

# Copy native libraries
echo "  - Copying native libraries..."
if [ -d "$PROJECT_ROOT/domain/src/desktopMain/resources/native" ]; then
    cp -r "$PROJECT_ROOT/domain/src/desktopMain/resources/native"/* "$PACKAGE_DIR/native/" 2>/dev/null || true
fi

# Copy documentation
echo "  - Copying documentation..."
cp "$PROJECT_ROOT/README.md" "$PACKAGE_DIR/docs/" 2>/dev/null || true
cp "$PROJECT_ROOT/docs/piper-jni"/*.md "$PACKAGE_DIR/docs/" 2>/dev/null || true

# Copy licenses
echo "  - Copying licenses..."
cp "$PROJECT_ROOT/LICENSE" "$PACKAGE_DIR/licenses/" 2>/dev/null || true
cp "$PROJECT_ROOT/THIRD_PARTY_LICENSES.txt" "$PACKAGE_DIR/licenses/" 2>/dev/null || true
cp "$PROJECT_ROOT/native/licensing/ATTRIBUTION.md" "$PACKAGE_DIR/licenses/" 2>/dev/null || true

echo -e "${GREEN}✓${NC} Packaging complete"
echo ""

# 3. Generate release notes
echo "3. Generating release notes..."

cat > "$PACKAGE_DIR/RELEASE_NOTES.md" << EOF
# IReader v$VERSION Release Notes

**Release Date**: $(date +%Y-%m-%d)

## What's New

### Features
- Production-ready Piper JNI integration
- Offline text-to-speech in 20+ languages
- High-quality voice models
- Cross-platform support (Windows, macOS, Linux)
- Intuitive voice management
- Real-time speech parameter adjustment

### Improvements
- Optimized synthesis performance (< 200ms latency)
- Enhanced memory management
- Improved error handling
- Better cross-platform compatibility

### Bug Fixes
- Fixed various stability issues
- Improved error messages
- Enhanced library verification

## System Requirements

### Windows
- Windows 10 or later (64-bit)
- 4 GB RAM minimum, 8 GB recommended
- 500 MB disk space (plus space for voice models)

### macOS
- macOS 11 (Big Sur) or later
- Intel or Apple Silicon
- 4 GB RAM minimum, 8 GB recommended
- 500 MB disk space (plus space for voice models)

### Linux
- Ubuntu 20.04 or later (or equivalent)
- 4 GB RAM minimum, 8 GB recommended
- 500 MB disk space (plus space for voice models)

## Installation

### Windows
1. Download \`IReader-$VERSION-x64.msi\`
2. Run the installer
3. Follow the installation wizard
4. Launch IReader from Start Menu

### macOS
1. Download \`IReader-$VERSION-macOS.dmg\`
2. Open the DMG file
3. Drag IReader to Applications folder
4. Launch IReader from Applications

### Linux (DEB)
\`\`\`bash
sudo dpkg -i ireader_${VERSION}_amd64.deb
sudo apt-get install -f  # Install dependencies
ireader
\`\`\`

### Linux (RPM)
\`\`\`bash
sudo rpm -i ireader-${VERSION}-1.x86_64.rpm
ireader
\`\`\`

## Known Issues

- First synthesis may take longer as models are loaded
- Some voice models may require additional downloads
- GPU acceleration not yet supported

## Upgrade Notes

If upgrading from a previous version:
1. Backup your settings and bookmarks
2. Uninstall the old version
3. Install the new version
4. Your settings should be preserved

## Support

- Documentation: https://ireader.org/docs
- Issues: https://github.com/yourusername/ireader/issues
- Email: support@ireader.org

## License

IReader is licensed under the Mozilla Public License 2.0.
See LICENSE file for details.

Third-party components are licensed under their respective licenses.
See THIRD_PARTY_LICENSES.txt for details.

## Acknowledgments

Special thanks to:
- Rhasspy community for Piper TTS
- Microsoft for ONNX Runtime
- All open source contributors

---

For full changelog, see: https://github.com/yourusername/ireader/releases
EOF

echo -e "${GREEN}✓${NC} Release notes generated"
echo ""

# 4. Create version file
echo "4. Creating version file..."

cat > "$PACKAGE_DIR/VERSION" << EOF
VERSION=$VERSION
BUILD_DATE=$(date -u +%Y-%m-%dT%H:%M:%SZ)
BUILD_NUMBER=${BUILD_NUMBER:-0}
GIT_COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
GIT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
EOF

echo -e "${GREEN}✓${NC} Version file created"
echo ""

# 5. Calculate checksums
echo "5. Calculating checksums..."

cd "$OUTPUT_DIR"

# Create checksums file
CHECKSUMS_FILE="ireader-$VERSION-checksums.txt"
rm -f "$CHECKSUMS_FILE"

echo "IReader v$VERSION - File Checksums" > "$CHECKSUMS_FILE"
echo "Generated: $(date)" >> "$CHECKSUMS_FILE"
echo "========================================" >> "$CHECKSUMS_FILE"
echo "" >> "$CHECKSUMS_FILE"

# Calculate checksums for all files in package
find "$PACKAGE_DIR" -type f -exec sha256sum {} \; | sed "s|$PACKAGE_DIR/||" >> "$CHECKSUMS_FILE"

echo -e "${GREEN}✓${NC} Checksums calculated"
echo ""

# 6. Create archive
echo "6. Creating release archive..."

ARCHIVE_NAME="ireader-$VERSION.tar.gz"
tar -czf "$ARCHIVE_NAME" "ireader-$VERSION"

# Calculate archive checksum
ARCHIVE_CHECKSUM=$(sha256sum "$ARCHIVE_NAME" | awk '{print $1}')
echo "" >> "$CHECKSUMS_FILE"
echo "Release Archive:" >> "$CHECKSUMS_FILE"
echo "$ARCHIVE_CHECKSUM  $ARCHIVE_NAME" >> "$CHECKSUMS_FILE"

echo -e "${GREEN}✓${NC} Archive created: $ARCHIVE_NAME"
echo ""

# 7. Create installers (if scripts exist)
echo "7. Creating platform-specific installers..."

# Windows MSI
if [ -f "$PROJECT_ROOT/native/installers/windows/build_msi.ps1" ]; then
    echo "  - Windows MSI installer available"
    echo "    Run: powershell -File native/installers/windows/build_msi.ps1"
fi

# macOS DMG
if [ -f "$PROJECT_ROOT/native/installers/macos/create_dmg.sh" ]; then
    echo "  - macOS DMG installer available"
    echo "    Run: native/installers/macos/create_dmg.sh $VERSION"
fi

# Linux DEB
if [ -f "$PROJECT_ROOT/native/installers/linux/build_deb.sh" ]; then
    echo "  - Linux DEB package available"
    echo "    Run: native/installers/linux/build_deb.sh $VERSION"
fi

# Linux RPM
if [ -f "$PROJECT_ROOT/native/installers/linux/build_rpm.sh" ]; then
    echo "  - Linux RPM package available"
    echo "    Run: native/installers/linux/build_rpm.sh $VERSION"
fi

echo ""

# 8. Generate release summary
echo "8. Generating release summary..."

SUMMARY_FILE="$OUTPUT_DIR/RELEASE_SUMMARY.txt"

cat > "$SUMMARY_FILE" << EOF
IReader v$VERSION Release Summary
================================================================================

Release Information:
  Version: $VERSION
  Build Date: $(date)
  Build Number: ${BUILD_NUMBER:-0}
  Git Commit: $(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
  Git Branch: $(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")

Release Artifacts:
  - Source Archive: $ARCHIVE_NAME
  - Checksum File: $CHECKSUMS_FILE
  - Package Directory: ireader-$VERSION/

Package Contents:
  - Application JARs: $(find "$PACKAGE_DIR/lib" -name "*.jar" | wc -l) files
  - Native Libraries: $(find "$PACKAGE_DIR/native" -type f 2>/dev/null | wc -l) files
  - Documentation: $(find "$PACKAGE_DIR/docs" -name "*.md" | wc -l) files
  - License Files: $(find "$PACKAGE_DIR/licenses" -type f | wc -l) files

Archive Checksum:
  SHA256: $ARCHIVE_CHECKSUM

Next Steps:
  1. Test the release package on all platforms
  2. Build platform-specific installers
  3. Run QA tests (native/qa/run_full_test_suite.sh)
  4. Verify license compliance (native/licensing/verify_licenses.sh)
  5. Create GitHub release
  6. Upload release artifacts
  7. Update documentation
  8. Announce release

Platform-Specific Installers:
  Windows: powershell -File native/installers/windows/build_msi.ps1
  macOS:   native/installers/macos/create_dmg.sh $VERSION
  Linux:   native/installers/linux/build_deb.sh $VERSION
           native/installers/linux/build_rpm.sh $VERSION

Quality Assurance:
  Run: native/qa/run_full_test_suite.sh
  Manual: native/qa/manual_test_checklist.md

License Verification:
  Run: native/licensing/verify_licenses.sh

Distribution:
  Upload to: https://cdn.ireader.org/releases/$VERSION/
  Update catalog: https://cdn.ireader.org/voices/catalog.json

================================================================================
Release created successfully!
Location: $OUTPUT_DIR
================================================================================
EOF

echo -e "${GREEN}✓${NC} Release summary generated"
echo ""

# Print summary
echo "================================"
echo "Release Package Created!"
echo "================================"
echo ""
echo "Version: $VERSION"
echo "Location: $OUTPUT_DIR"
echo "Archive: $ARCHIVE_NAME"
echo "Checksum: $ARCHIVE_CHECKSUM"
echo ""
echo "Next steps:"
echo "  1. Review: $SUMMARY_FILE"
echo "  2. Test the release package"
echo "  3. Build platform installers"
echo "  4. Run QA tests"
echo "  5. Create GitHub release"
echo ""
echo -e "${GREEN}✓ Release package ready!${NC}"
