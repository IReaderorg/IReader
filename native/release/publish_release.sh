#!/bin/bash
# Publish release to GitHub and CDN

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."
VERSION="${1:-1.0.0}"
RELEASE_DIR="${2:-$PROJECT_ROOT/build/release}"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo "IReader Release Publisher"
echo "========================="
echo "Version: $VERSION"
echo ""

# Check if gh CLI is installed
if ! command -v gh &> /dev/null; then
    echo -e "${RED}Error: GitHub CLI (gh) is not installed${NC}"
    echo "Install from: https://cli.github.com/"
    exit 1
fi

# Check if authenticated
if ! gh auth status &> /dev/null; then
    echo -e "${RED}Error: Not authenticated with GitHub${NC}"
    echo "Run: gh auth login"
    exit 1
fi

# Verify release directory exists
if [ ! -d "$RELEASE_DIR" ]; then
    echo -e "${RED}Error: Release directory not found: $RELEASE_DIR${NC}"
    echo "Run: ./create_release.sh $VERSION first"
    exit 1
fi

# 1. Create Git tag
echo "1. Creating Git tag..."

cd "$PROJECT_ROOT"

# Check if tag already exists
if git rev-parse "v$VERSION" >/dev/null 2>&1; then
    echo -e "${YELLOW}⚠${NC} Tag v$VERSION already exists"
    read -p "Delete and recreate? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git tag -d "v$VERSION"
        git push origin ":refs/tags/v$VERSION" 2>/dev/null || true
    else
        echo "Skipping tag creation"
    fi
fi

# Create tag
if ! git rev-parse "v$VERSION" >/dev/null 2>&1; then
    git tag -a "v$VERSION" -m "Release v$VERSION"
    git push origin "v$VERSION"
    echo -e "${GREEN}✓${NC} Tag created and pushed"
else
    echo -e "${GREEN}✓${NC} Using existing tag"
fi

echo ""

# 2. Create GitHub release
echo "2. Creating GitHub release..."

# Check if release already exists
if gh release view "v$VERSION" &> /dev/null; then
    echo -e "${YELLOW}⚠${NC} Release v$VERSION already exists"
    read -p "Delete and recreate? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        gh release delete "v$VERSION" -y
    else
        echo "Skipping release creation"
        exit 0
    fi
fi

# Read release notes
RELEASE_NOTES_FILE="$RELEASE_DIR/ireader-$VERSION/RELEASE_NOTES.md"
if [ ! -f "$RELEASE_NOTES_FILE" ]; then
    echo -e "${RED}Error: Release notes not found: $RELEASE_NOTES_FILE${NC}"
    exit 1
fi

# Create release
gh release create "v$VERSION" \
    --title "IReader v$VERSION" \
    --notes-file "$RELEASE_NOTES_FILE" \
    --draft

echo -e "${GREEN}✓${NC} GitHub release created (draft)"
echo ""

# 3. Upload release artifacts
echo "3. Uploading release artifacts..."

# Upload source archive
ARCHIVE_FILE="$RELEASE_DIR/ireader-$VERSION.tar.gz"
if [ -f "$ARCHIVE_FILE" ]; then
    echo "  - Uploading source archive..."
    gh release upload "v$VERSION" "$ARCHIVE_FILE"
    echo -e "    ${GREEN}✓${NC} Source archive uploaded"
fi

# Upload checksums
CHECKSUMS_FILE="$RELEASE_DIR/ireader-$VERSION-checksums.txt"
if [ -f "$CHECKSUMS_FILE" ]; then
    echo "  - Uploading checksums..."
    gh release upload "v$VERSION" "$CHECKSUMS_FILE"
    echo -e "    ${GREEN}✓${NC} Checksums uploaded"
fi

# Upload platform-specific installers
INSTALLERS_DIR="$RELEASE_DIR/installers"
if [ -d "$INSTALLERS_DIR" ]; then
    echo "  - Uploading installers..."
    
    # Windows MSI
    for msi in "$INSTALLERS_DIR"/*.msi; do
        if [ -f "$msi" ]; then
            echo "    - $(basename $msi)"
            gh release upload "v$VERSION" "$msi"
            [ -f "$msi.sha256" ] && gh release upload "v$VERSION" "$msi.sha256"
        fi
    done
    
    # macOS DMG
    for dmg in "$INSTALLERS_DIR"/*.dmg; do
        if [ -f "$dmg" ]; then
            echo "    - $(basename $dmg)"
            gh release upload "v$VERSION" "$dmg"
            [ -f "$dmg.sha256" ] && gh release upload "v$VERSION" "$dmg.sha256"
        fi
    done
    
    # Linux DEB
    for deb in "$INSTALLERS_DIR"/*.deb; do
        if [ -f "$deb" ]; then
            echo "    - $(basename $deb)"
            gh release upload "v$VERSION" "$deb"
            [ -f "$deb.sha256" ] && gh release upload "v$VERSION" "$deb.sha256"
        fi
    done
    
    # Linux RPM
    for rpm in "$INSTALLERS_DIR"/*.rpm; do
        if [ -f "$rpm" ]; then
            echo "    - $(basename $rpm)"
            gh release upload "v$VERSION" "$rpm"
            [ -f "$rpm.sha256" ] && gh release upload "v$VERSION" "$rpm.sha256"
        fi
    done
    
    echo -e "    ${GREEN}✓${NC} Installers uploaded"
fi

echo ""

# 4. Publish release (remove draft status)
echo "4. Publishing release..."

read -p "Publish release now? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    gh release edit "v$VERSION" --draft=false
    echo -e "${GREEN}✓${NC} Release published!"
else
    echo -e "${YELLOW}⚠${NC} Release remains in draft state"
    echo "Publish later with: gh release edit v$VERSION --draft=false"
fi

echo ""

# 5. Upload to CDN (optional)
echo "5. CDN Upload"

if [ -n "$CDN_HOST" ] && [ -n "$CDN_PATH" ]; then
    echo "Uploading to CDN..."
    
    # Create remote directory
    ssh "$CDN_HOST" "mkdir -p $CDN_PATH/releases/$VERSION"
    
    # Upload files
    scp -r "$RELEASE_DIR/ireader-$VERSION"/* "$CDN_HOST:$CDN_PATH/releases/$VERSION/"
    
    if [ -d "$INSTALLERS_DIR" ]; then
        scp "$INSTALLERS_DIR"/* "$CDN_HOST:$CDN_PATH/releases/$VERSION/"
    fi
    
    echo -e "${GREEN}✓${NC} Files uploaded to CDN"
else
    echo -e "${YELLOW}⚠${NC} CDN upload skipped (CDN_HOST and CDN_PATH not set)"
    echo "To enable CDN upload, set environment variables:"
    echo "  export CDN_HOST=user@cdn.ireader.org"
    echo "  export CDN_PATH=/var/www/cdn"
fi

echo ""

# 6. Update documentation
echo "6. Documentation Update"
echo "Remember to update:"
echo "  - Website download links"
echo "  - Documentation version references"
echo "  - Changelog"
echo "  - Social media announcements"
echo ""

# Print summary
echo "================================"
echo "Release Published!"
echo "================================"
echo ""
echo "Version: $VERSION"
echo "GitHub: https://github.com/$(gh repo view --json nameWithOwner -q .nameWithOwner)/releases/tag/v$VERSION"
echo ""
echo "Next steps:"
echo "  1. Test download links"
echo "  2. Update website"
echo "  3. Announce release"
echo "  4. Monitor for issues"
echo ""
echo -e "${GREEN}✓ Release publication complete!${NC}"
