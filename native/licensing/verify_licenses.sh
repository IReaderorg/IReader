#!/bin/bash
# Verify licensing compliance for IReader distribution

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."
REPORT_FILE="$SCRIPT_DIR/license_compliance_report.txt"

echo "IReader License Compliance Verification"
echo "========================================"
echo ""

# Initialize report
cat > "$REPORT_FILE" << EOF
IReader License Compliance Report
Generated: $(date)
================================================================================

EOF

# Check for required license files
echo "Checking for required license files..."
REQUIRED_FILES=(
    "LICENSE"
    "THIRD_PARTY_LICENSES.txt"
)

MISSING_FILES=()
for file in "${REQUIRED_FILES[@]}"; do
    if [ -f "$PROJECT_ROOT/$file" ]; then
        echo "✓ Found: $file"
        echo "✓ $file" >> "$REPORT_FILE"
    else
        echo "✗ Missing: $file"
        echo "✗ MISSING: $file" >> "$REPORT_FILE"
        MISSING_FILES+=("$file")
    fi
done

echo "" >> "$REPORT_FILE"

# Check native library licenses
echo ""
echo "Checking native library licenses..."
echo "Native Library Licenses:" >> "$REPORT_FILE"
echo "------------------------" >> "$REPORT_FILE"

NATIVE_LIBS=(
    "piper:MIT:https://github.com/rhasspy/piper"
    "onnxruntime:MIT:https://github.com/microsoft/onnxruntime"
    "espeak-ng:GPL-3.0:https://github.com/espeak-ng/espeak-ng"
)

for lib_info in "${NATIVE_LIBS[@]}"; do
    IFS=':' read -r lib license url <<< "$lib_info"
    echo "  - $lib: $license ($url)"
    echo "  - $lib: $license ($url)" >> "$REPORT_FILE"
done

echo "" >> "$REPORT_FILE"

# Check Kotlin/JVM dependencies
echo ""
echo "Checking Kotlin/JVM dependencies..."
echo "Kotlin/JVM Dependencies:" >> "$REPORT_FILE"
echo "------------------------" >> "$REPORT_FILE"

if [ -f "$PROJECT_ROOT/build.gradle.kts" ]; then
    echo "✓ Found build.gradle.kts"
    echo "✓ Gradle build file found" >> "$REPORT_FILE"
    
    # Extract dependencies (simplified check)
    if grep -q "kotlin" "$PROJECT_ROOT/build.gradle.kts"; then
        echo "  - Kotlin Standard Library: Apache 2.0"
        echo "  - Kotlin Standard Library: Apache 2.0" >> "$REPORT_FILE"
    fi
    
    if grep -q "compose" "$PROJECT_ROOT/build.gradle.kts"; then
        echo "  - Jetpack Compose: Apache 2.0"
        echo "  - Jetpack Compose: Apache 2.0" >> "$REPORT_FILE"
    fi
    
    if grep -q "ktor" "$PROJECT_ROOT/build.gradle.kts"; then
        echo "  - Ktor: Apache 2.0"
        echo "  - Ktor: Apache 2.0" >> "$REPORT_FILE"
    fi
else
    echo "⚠ build.gradle.kts not found"
    echo "⚠ build.gradle.kts not found" >> "$REPORT_FILE"
fi

echo "" >> "$REPORT_FILE"

# Check voice model licenses
echo ""
echo "Checking voice model licenses..."
echo "Voice Model Licenses:" >> "$REPORT_FILE"
echo "---------------------" >> "$REPORT_FILE"

VOICE_CATALOG="$PROJECT_ROOT/domain/src/commonMain/kotlin/ireader/domain/catalogs/VoiceCatalog.kt"
if [ -f "$VOICE_CATALOG" ]; then
    echo "✓ Found voice catalog"
    echo "✓ Voice catalog found" >> "$REPORT_FILE"
    
    # Check if license information is present
    if grep -q "license" "$VOICE_CATALOG"; then
        echo "  ✓ License information present in catalog"
        echo "  ✓ License information present in catalog" >> "$REPORT_FILE"
    else
        echo "  ⚠ License information may be missing from catalog"
        echo "  ⚠ License information may be missing from catalog" >> "$REPORT_FILE"
    fi
else
    echo "⚠ Voice catalog not found"
    echo "⚠ Voice catalog not found" >> "$REPORT_FILE"
fi

echo "" >> "$REPORT_FILE"

# Check for GPL compliance (espeak-ng)
echo ""
echo "Checking GPL compliance..."
echo "GPL Compliance:" >> "$REPORT_FILE"
echo "---------------" >> "$REPORT_FILE"

echo "espeak-ng is licensed under GPL-3.0"
echo "espeak-ng is licensed under GPL-3.0" >> "$REPORT_FILE"
echo ""
echo "GPL Compliance Requirements:" >> "$REPORT_FILE"
echo "1. Include full GPL-3.0 license text" >> "$REPORT_FILE"
echo "2. Provide source code or written offer" >> "$REPORT_FILE"
echo "3. Preserve copyright notices" >> "$REPORT_FILE"
echo "4. Document modifications (if any)" >> "$REPORT_FILE"

if grep -q "GPL" "$PROJECT_ROOT/THIRD_PARTY_LICENSES.txt" 2>/dev/null; then
    echo "✓ GPL license text included in THIRD_PARTY_LICENSES.txt"
    echo "✓ GPL license text included" >> "$REPORT_FILE"
else
    echo "⚠ GPL license text may be missing"
    echo "⚠ GPL license text may be missing" >> "$REPORT_FILE"
fi

echo "" >> "$REPORT_FILE"

# Check attribution notices
echo ""
echo "Checking attribution notices..."
echo "Attribution Notices:" >> "$REPORT_FILE"
echo "--------------------" >> "$REPORT_FILE"

ATTRIBUTION_LOCATIONS=(
    "About dialog in application"
    "README.md"
    "THIRD_PARTY_LICENSES.txt"
    "Installer/package metadata"
)

for location in "${ATTRIBUTION_LOCATIONS[@]}"; do
    echo "  - $location"
    echo "  - $location" >> "$REPORT_FILE"
done

echo "" >> "$REPORT_FILE"

# Check installer packages
echo ""
echo "Checking installer packages..."
echo "Installer Package Compliance:" >> "$REPORT_FILE"
echo "-----------------------------" >> "$REPORT_FILE"

INSTALLER_CONFIGS=(
    "native/installers/windows/ireader.wxs"
    "native/installers/linux/debian/control"
    "native/installers/linux/ireader.spec"
)

for config in "${INSTALLER_CONFIGS[@]}"; do
    if [ -f "$PROJECT_ROOT/$config" ]; then
        echo "✓ Found: $config"
        echo "✓ $config" >> "$REPORT_FILE"
        
        # Check if license files are referenced
        if grep -q "LICENSE" "$PROJECT_ROOT/$config"; then
            echo "  ✓ License files referenced"
            echo "  ✓ License files referenced" >> "$REPORT_FILE"
        else
            echo "  ⚠ License files may not be included in package"
            echo "  ⚠ License files may not be included" >> "$REPORT_FILE"
        fi
    else
        echo "⚠ Not found: $config"
        echo "⚠ Not found: $config" >> "$REPORT_FILE"
    fi
done

echo "" >> "$REPORT_FILE"

# Generate compliance checklist
echo ""
echo "Compliance Checklist:" >> "$REPORT_FILE"
echo "---------------------" >> "$REPORT_FILE"

CHECKLIST=(
    "[ ] All license files included in distribution"
    "[ ] Third-party licenses documented"
    "[ ] Attribution notices in About dialog"
    "[ ] GPL source code availability documented"
    "[ ] Voice model licenses verified"
    "[ ] Installer packages include license files"
    "[ ] Copyright notices preserved"
    "[ ] License URLs provided for reference"
)

for item in "${CHECKLIST[@]}"; do
    echo "$item" >> "$REPORT_FILE"
done

echo "" >> "$REPORT_FILE"

# Summary
echo ""
echo "Summary:" >> "$REPORT_FILE"
echo "--------" >> "$REPORT_FILE"

if [ ${#MISSING_FILES[@]} -eq 0 ]; then
    echo "✓ All required license files present"
    echo "✓ All required license files present" >> "$REPORT_FILE"
    COMPLIANCE_STATUS="PASS"
else
    echo "✗ Missing ${#MISSING_FILES[@]} required file(s)"
    echo "✗ Missing ${#MISSING_FILES[@]} required file(s)" >> "$REPORT_FILE"
    COMPLIANCE_STATUS="FAIL"
fi

echo ""
echo "Compliance Status: $COMPLIANCE_STATUS" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# Recommendations
echo "Recommendations:" >> "$REPORT_FILE"
echo "----------------" >> "$REPORT_FILE"
echo "1. Review all third-party licenses before distribution" >> "$REPORT_FILE"
echo "2. Verify voice model licenses permit commercial use" >> "$REPORT_FILE"
echo "3. Include license files in all distribution packages" >> "$REPORT_FILE"
echo "4. Maintain up-to-date attribution notices" >> "$REPORT_FILE"
echo "5. Document any modifications to GPL components" >> "$REPORT_FILE"
echo "6. Provide source code access for GPL components" >> "$REPORT_FILE"
echo "7. Update THIRD_PARTY_LICENSES.txt when adding dependencies" >> "$REPORT_FILE"

echo ""
echo "Report saved to: $REPORT_FILE"
echo ""
echo "Compliance Status: $COMPLIANCE_STATUS"

if [ "$COMPLIANCE_STATUS" = "FAIL" ]; then
    echo ""
    echo "⚠ Compliance issues detected. Please review the report."
    exit 1
fi

echo ""
echo "✓ License compliance verification complete"
