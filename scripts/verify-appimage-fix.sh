#!/bin/bash
# Verification script for AppImage update information fix

echo "=== AppImage Update Information Fix Verification ==="
echo ""

echo "1. Checking build.gradle.kts for correct update info..."
if grep -q "gh-releases-zsync|IReaderorg|IReader" desktop/build.gradle.kts; then
    echo "   ✓ build.gradle.kts has correct case (IReaderorg|IReader)"
else
    echo "   ✗ build.gradle.kts has incorrect case"
    exit 1
fi

echo ""
echo "2. Checking for any remaining incorrect references..."
if grep -r "ireaderorg|ireader" desktop/ --include="*.kt" --include="*.kts" --include="*.md" 2>/dev/null | grep -v "APPIMAGE_UPDATE_FIX.md"; then
    echo "   ✗ Found incorrect lowercase references"
    exit 1
else
    echo "   ✓ No incorrect lowercase references found"
fi

echo ""
echo "3. Checking for old IReader-org format..."
if grep -r "IReader-org" desktop/ --include="*.kt" --include="*.kts" --include="*.md" 2>/dev/null; then
    echo "   ✗ Found old IReader-org format"
    exit 1
else
    echo "   ✓ No old IReader-org format found"
fi

echo ""
echo "4. Verifying test file updates..."
if grep -q "IReaderorg" desktop/src/test/kotlin/ireader/desktop/update/AppImageUpdateInfoTest.kt; then
    echo "   ✓ Test file has correct case"
else
    echo "   ✗ Test file has incorrect case"
    exit 1
fi

echo ""
echo "5. Verifying AM script updates..."
if grep -q 'SITE="IReaderorg/IReader"' desktop/am-script/ireader; then
    echo "   ✓ AM script has correct case"
else
    echo "   ✗ AM script has incorrect case"
    exit 1
fi

echo ""
echo "=== All Verifications Passed! ==="
echo ""
echo "Next steps:"
echo "1. Commit these changes"
echo "2. Wait for next Preview or Release build"
echo "3. Users can then use AppImageUpdate to update their AppImages"
echo ""
echo "To test locally:"
echo "  ./gradlew buildAppImageWithZsync -Pappimage.update.channel=continuous"
echo "  appimageupdatetool --check-for-update desktop/build/compose/binaries/main/IReader-x86_64.AppImage"
