#!/bin/bash
# Build RPM package for IReader

set -e

# Configuration
APP_NAME="ireader"
VERSION="${1:-1.0.0}"
RELEASE="1"
ARCH="x86_64"
SOURCE_DIR="${2:-../../build/release}"
OUTPUT_DIR="${3:-../../build/installers}"

echo "Building RPM package for $APP_NAME v$VERSION..."

# Check for required tools
if ! command -v rpmbuild &> /dev/null; then
    echo "Error: rpmbuild not found. Please install rpm-build."
    exit 1
fi

# Verify source files exist
if [ ! -f "$SOURCE_DIR/ireader.jar" ]; then
    echo "Error: $SOURCE_DIR/ireader.jar not found"
    exit 1
fi

# Create RPM build directory structure
RPM_ROOT="$OUTPUT_DIR/rpmbuild"
rm -rf "$RPM_ROOT"
mkdir -p "$RPM_ROOT"/{BUILD,RPMS,SOURCES,SPECS,SRPMS}

# Create source tarball
TARBALL_DIR="${APP_NAME}-${VERSION}"
TARBALL_PATH="$RPM_ROOT/SOURCES/${APP_NAME}-${VERSION}.tar.gz"

echo "Creating source tarball..."
TEMP_DIR=$(mktemp -d)
trap "rm -rf $TEMP_DIR" EXIT

mkdir -p "$TEMP_DIR/$TARBALL_DIR"
cp "$SOURCE_DIR/ireader.jar" "$TEMP_DIR/$TARBALL_DIR/"
mkdir -p "$TEMP_DIR/$TARBALL_DIR/native"
cp "$SOURCE_DIR/native"/*.so "$TEMP_DIR/$TARBALL_DIR/native/" 2>/dev/null || true

# Copy icons
mkdir -p "$TEMP_DIR/$TARBALL_DIR/icons"
if [ -d "$SOURCE_DIR/icons" ]; then
    cp -r "$SOURCE_DIR/icons"/* "$TEMP_DIR/$TARBALL_DIR/icons/" 2>/dev/null || true
fi

# Copy documentation
cp "$SOURCE_DIR/../LICENSE" "$TEMP_DIR/$TARBALL_DIR/" 2>/dev/null || echo "License file not found"
cp "$SOURCE_DIR/../README.md" "$TEMP_DIR/$TARBALL_DIR/" 2>/dev/null || echo "README not found"
cp "$SOURCE_DIR/../THIRD_PARTY_LICENSES.txt" "$TEMP_DIR/$TARBALL_DIR/" 2>/dev/null || echo "Third-party licenses not found"

# Create tarball
tar -czf "$TARBALL_PATH" -C "$TEMP_DIR" "$TARBALL_DIR"

# Copy spec file
cp ireader.spec "$RPM_ROOT/SPECS/"

# Update version in spec file
sed -i "s/^Version:.*/Version:        $VERSION/" "$RPM_ROOT/SPECS/ireader.spec"
sed -i "s/^Release:.*/Release:        $RELEASE%{?dist}/" "$RPM_ROOT/SPECS/ireader.spec"

# Build RPM
echo "Building RPM package..."
rpmbuild \
    --define "_topdir $RPM_ROOT" \
    --define "_version $VERSION" \
    --define "_release $RELEASE" \
    -ba "$RPM_ROOT/SPECS/ireader.spec"

# Find generated RPM
RPM_FILE=$(find "$RPM_ROOT/RPMS" -name "${APP_NAME}-${VERSION}-*.rpm" | head -n 1)

if [ -z "$RPM_FILE" ]; then
    echo "Error: RPM file not found"
    exit 1
fi

# Copy to output directory
mkdir -p "$OUTPUT_DIR"
RPM_FILENAME=$(basename "$RPM_FILE")
cp "$RPM_FILE" "$OUTPUT_DIR/$RPM_FILENAME"

# Calculate checksum
echo "Calculating checksum..."
sha256sum "$OUTPUT_DIR/$RPM_FILENAME" | awk '{print $1}' > "$OUTPUT_DIR/$RPM_FILENAME.sha256"

echo "RPM package created successfully: $OUTPUT_DIR/$RPM_FILENAME"
echo "SHA256: $(cat $OUTPUT_DIR/$RPM_FILENAME.sha256)"

# Verify package
echo "Verifying package..."
rpm -qip "$OUTPUT_DIR/$RPM_FILENAME"
rpm -qlp "$OUTPUT_DIR/$RPM_FILENAME"

echo "Done!"
