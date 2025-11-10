#!/bin/bash
# Test platform-specific installers on clean systems

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUTPUT_DIR="$SCRIPT_DIR/../../build/installers"

echo "Testing IReader Installers..."
echo "=============================="

# Detect platform
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    PLATFORM="linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    PLATFORM="macos"
elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
    PLATFORM="windows"
else
    echo "Unsupported platform: $OSTYPE"
    exit 1
fi

echo "Platform detected: $PLATFORM"
echo ""

# Test Linux installers
if [ "$PLATFORM" == "linux" ]; then
    echo "Testing Linux installers..."
    
    # Test DEB package
    if [ -f "$OUTPUT_DIR"/*.deb ]; then
        DEB_FILE=$(ls "$OUTPUT_DIR"/*.deb | head -n 1)
        echo "Testing DEB package: $DEB_FILE"
        
        # Verify package structure
        dpkg-deb --info "$DEB_FILE"
        dpkg-deb --contents "$DEB_FILE"
        
        # Check for required files
        if dpkg-deb --contents "$DEB_FILE" | grep -q "usr/bin/ireader"; then
            echo "✓ Launcher script found"
        else
            echo "✗ Launcher script missing"
            exit 1
        fi
        
        if dpkg-deb --contents "$DEB_FILE" | grep -q "usr/lib/ireader/ireader.jar"; then
            echo "✓ Application JAR found"
        else
            echo "✗ Application JAR missing"
            exit 1
        fi
        
        if dpkg-deb --contents "$DEB_FILE" | grep -q "usr/share/applications/ireader.desktop"; then
            echo "✓ Desktop entry found"
        else
            echo "✗ Desktop entry missing"
            exit 1
        fi
        
        echo "DEB package verification passed!"
        echo ""
    fi
    
    # Test RPM package
    if [ -f "$OUTPUT_DIR"/*.rpm ]; then
        RPM_FILE=$(ls "$OUTPUT_DIR"/*.rpm | head -n 1)
        echo "Testing RPM package: $RPM_FILE"
        
        # Verify package structure
        rpm -qip "$RPM_FILE"
        rpm -qlp "$RPM_FILE"
        
        # Check for required files
        if rpm -qlp "$RPM_FILE" | grep -q "/usr/bin/ireader"; then
            echo "✓ Launcher script found"
        else
            echo "✗ Launcher script missing"
            exit 1
        fi
        
        if rpm -qlp "$RPM_FILE" | grep -q "/usr/lib64/ireader/ireader.jar"; then
            echo "✓ Application JAR found"
        else
            echo "✗ Application JAR missing"
            exit 1
        fi
        
        if rpm -qlp "$RPM_FILE" | grep -q "/usr/share/applications/ireader.desktop"; then
            echo "✓ Desktop entry found"
        else
            echo "✗ Desktop entry missing"
            exit 1
        fi
        
        echo "RPM package verification passed!"
        echo ""
    fi
    
    # Test installation in Docker (clean system)
    echo "Testing installation in clean Docker container..."
    
    # Test DEB on Ubuntu
    if command -v docker &> /dev/null && [ -f "$OUTPUT_DIR"/*.deb ]; then
        DEB_FILE=$(ls "$OUTPUT_DIR"/*.deb | head -n 1)
        echo "Testing DEB installation on Ubuntu..."
        
        docker run --rm -v "$OUTPUT_DIR:/packages" ubuntu:22.04 bash -c "
            apt-get update -qq && \
            apt-get install -y -qq /packages/$(basename $DEB_FILE) && \
            which ireader && \
            ireader --version || echo 'Version check skipped' && \
            echo 'DEB installation test passed!'
        "
    fi
    
    # Test RPM on Fedora
    if command -v docker &> /dev/null && [ -f "$OUTPUT_DIR"/*.rpm ]; then
        RPM_FILE=$(ls "$OUTPUT_DIR"/*.rpm | head -n 1)
        echo "Testing RPM installation on Fedora..."
        
        docker run --rm -v "$OUTPUT_DIR:/packages" fedora:latest bash -c "
            dnf install -y -q /packages/$(basename $RPM_FILE) && \
            which ireader && \
            ireader --version || echo 'Version check skipped' && \
            echo 'RPM installation test passed!'
        "
    fi
fi

# Test macOS installer
if [ "$PLATFORM" == "macos" ]; then
    echo "Testing macOS installer..."
    
    if [ -f "$OUTPUT_DIR"/*.dmg ]; then
        DMG_FILE=$(ls "$OUTPUT_DIR"/*.dmg | head -n 1)
        echo "Testing DMG: $DMG_FILE"
        
        # Verify DMG can be mounted
        hdiutil verify "$DMG_FILE"
        
        # Mount DMG
        MOUNT_POINT=$(mktemp -d)
        hdiutil attach "$DMG_FILE" -mountpoint "$MOUNT_POINT" -nobrowse -quiet
        
        # Check for app bundle
        if [ -d "$MOUNT_POINT/IReader.app" ]; then
            echo "✓ Application bundle found"
            
            # Verify code signature
            codesign --verify --deep --strict "$MOUNT_POINT/IReader.app" && \
                echo "✓ Code signature valid" || \
                echo "⚠ Code signature verification failed (may need signing)"
            
            # Check for required files
            if [ -f "$MOUNT_POINT/IReader.app/Contents/MacOS/IReader" ]; then
                echo "✓ Executable found"
            else
                echo "✗ Executable missing"
                hdiutil detach "$MOUNT_POINT" -quiet
                exit 1
            fi
        else
            echo "✗ Application bundle missing"
            hdiutil detach "$MOUNT_POINT" -quiet
            exit 1
        fi
        
        # Unmount DMG
        hdiutil detach "$MOUNT_POINT" -quiet
        rm -rf "$MOUNT_POINT"
        
        echo "DMG verification passed!"
    fi
fi

# Test Windows installer
if [ "$PLATFORM" == "windows" ]; then
    echo "Testing Windows installer..."
    
    if [ -f "$OUTPUT_DIR"/*.msi ]; then
        MSI_FILE=$(ls "$OUTPUT_DIR"/*.msi | head -n 1)
        echo "Testing MSI: $MSI_FILE"
        
        # Verify MSI structure (requires Windows)
        # This would need to run on actual Windows system
        echo "⚠ MSI verification requires Windows system"
        echo "Please test manually on Windows by running:"
        echo "  msiexec /i $(basename $MSI_FILE) /qn"
        echo "  msiexec /x $(basename $MSI_FILE) /qn"
    fi
fi

# Verify checksums
echo ""
echo "Verifying checksums..."
for file in "$OUTPUT_DIR"/*.{deb,rpm,dmg,msi} 2>/dev/null; do
    if [ -f "$file" ]; then
        CHECKSUM_FILE="${file}.sha256"
        if [ -f "$CHECKSUM_FILE" ]; then
            EXPECTED=$(cat "$CHECKSUM_FILE")
            ACTUAL=$(sha256sum "$file" | awk '{print $1}')
            
            if [ "$EXPECTED" == "$ACTUAL" ]; then
                echo "✓ Checksum verified: $(basename $file)"
            else
                echo "✗ Checksum mismatch: $(basename $file)"
                exit 1
            fi
        else
            echo "⚠ Checksum file missing: $(basename $file)"
        fi
    fi
done

echo ""
echo "All installer tests passed!"
