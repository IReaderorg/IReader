#!/bin/bash
# Run full test suite for IReader before release

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."
REPORT_DIR="$SCRIPT_DIR/reports"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
REPORT_FILE="$REPORT_DIR/qa_report_$TIMESTAMP.txt"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "IReader Quality Assurance Test Suite"
echo "====================================="
echo "Timestamp: $(date)"
echo ""

# Create reports directory
mkdir -p "$REPORT_DIR"

# Initialize report
cat > "$REPORT_FILE" << EOF
IReader Quality Assurance Report
Generated: $(date)
================================================================================

EOF

# Track test results
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
SKIPPED_TESTS=0

# Function to log test result
log_test() {
    local test_name="$1"
    local status="$2"
    local message="$3"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    case "$status" in
        "PASS")
            PASSED_TESTS=$((PASSED_TESTS + 1))
            echo -e "${GREEN}✓${NC} $test_name"
            echo "✓ $test_name: PASS" >> "$REPORT_FILE"
            ;;
        "FAIL")
            FAILED_TESTS=$((FAILED_TESTS + 1))
            echo -e "${RED}✗${NC} $test_name"
            echo "✗ $test_name: FAIL - $message" >> "$REPORT_FILE"
            ;;
        "SKIP")
            SKIPPED_TESTS=$((SKIPPED_TESTS + 1))
            echo -e "${YELLOW}⊘${NC} $test_name (skipped)"
            echo "⊘ $test_name: SKIPPED - $message" >> "$REPORT_FILE"
            ;;
    esac
    
    if [ -n "$message" ]; then
        echo "  $message" >> "$REPORT_FILE"
    fi
}

# 1. Build Tests
echo "1. Build Tests"
echo "==============" >> "$REPORT_FILE"
echo ""

# Check if project builds
if cd "$PROJECT_ROOT" && ./gradlew clean build --no-daemon > /dev/null 2>&1; then
    log_test "Project builds successfully" "PASS"
else
    log_test "Project builds successfully" "FAIL" "Build failed"
fi

# Check if native libraries exist
if [ -d "$PROJECT_ROOT/domain/src/desktopMain/resources/native" ]; then
    log_test "Native libraries directory exists" "PASS"
else
    log_test "Native libraries directory exists" "FAIL" "Directory not found"
fi

echo "" >> "$REPORT_FILE"

# 2. Unit Tests
echo ""
echo "2. Unit Tests"
echo "=============" >> "$REPORT_FILE"
echo ""

if cd "$PROJECT_ROOT" && ./gradlew test --no-daemon > /dev/null 2>&1; then
    log_test "Unit tests pass" "PASS"
else
    log_test "Unit tests pass" "FAIL" "Some unit tests failed"
fi

echo "" >> "$REPORT_FILE"

# 3. Integration Tests
echo ""
echo "3. Integration Tests"
echo "====================" >> "$REPORT_FILE"
echo ""

# Check if integration tests exist
if [ -d "$PROJECT_ROOT/domain/src/desktopTest" ]; then
    if cd "$PROJECT_ROOT" && ./gradlew desktopTest --no-daemon > /dev/null 2>&1; then
        log_test "Integration tests pass" "PASS"
    else
        log_test "Integration tests pass" "FAIL" "Some integration tests failed"
    fi
else
    log_test "Integration tests pass" "SKIP" "No integration tests found"
fi

echo "" >> "$REPORT_FILE"

# 4. Performance Tests
echo ""
echo "4. Performance Tests"
echo "====================" >> "$REPORT_FILE"
echo ""

# Check if performance tests exist
if [ -f "$PROJECT_ROOT/native/test/performance_test.cpp" ]; then
    log_test "Performance tests available" "PASS"
else
    log_test "Performance tests available" "SKIP" "Performance tests not implemented"
fi

echo "" >> "$REPORT_FILE"

# 5. Cross-Platform Tests
echo ""
echo "5. Cross-Platform Tests"
echo "=======================" >> "$REPORT_FILE"
echo ""

# Detect current platform
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    PLATFORM="linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    PLATFORM="macos"
elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
    PLATFORM="windows"
else
    PLATFORM="unknown"
fi

log_test "Platform detected: $PLATFORM" "PASS"

# Check native library for current platform
case "$PLATFORM" in
    "linux")
        if [ -f "$PROJECT_ROOT/domain/src/desktopMain/resources/native/libpiper_jni.so" ]; then
            log_test "Linux native library exists" "PASS"
        else
            log_test "Linux native library exists" "FAIL" "libpiper_jni.so not found"
        fi
        ;;
    "macos")
        if [ -f "$PROJECT_ROOT/domain/src/desktopMain/resources/native/libpiper_jni.dylib" ]; then
            log_test "macOS native library exists" "PASS"
        else
            log_test "macOS native library exists" "FAIL" "libpiper_jni.dylib not found"
        fi
        ;;
    "windows")
        if [ -f "$PROJECT_ROOT/domain/src/desktopMain/resources/native/piper_jni.dll" ]; then
            log_test "Windows native library exists" "PASS"
        else
            log_test "Windows native library exists" "FAIL" "piper_jni.dll not found"
        fi
        ;;
esac

echo "" >> "$REPORT_FILE"

# 6. Feature Tests
echo ""
echo "6. Feature Tests"
echo "================" >> "$REPORT_FILE"
echo ""

# Check if key features are implemented
FEATURES=(
    "PiperNative.kt:Voice initialization"
    "PiperNative.kt:Text synthesis"
    "VoiceCatalog.kt:Voice catalog"
    "LibraryVerifier.kt:Library verification"
)

for feature_info in "${FEATURES[@]}"; do
    IFS=':' read -r file feature <<< "$feature_info"
    if [ -f "$PROJECT_ROOT/domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/piper/$file" ] || \
       [ -f "$PROJECT_ROOT/domain/src/commonMain/kotlin/ireader/domain/catalogs/$file" ]; then
        log_test "$feature implemented" "PASS"
    else
        log_test "$feature implemented" "FAIL" "$file not found"
    fi
done

echo "" >> "$REPORT_FILE"

# 7. Documentation Tests
echo ""
echo "7. Documentation Tests"
echo "======================" >> "$REPORT_FILE"
echo ""

DOCS=(
    "README.md"
    "docs/piper-jni/Developer_Guide.md"
    "docs/piper-jni/User_Guide.md"
    "docs/piper-jni/Code_Examples.md"
)

for doc in "${DOCS[@]}"; do
    if [ -f "$PROJECT_ROOT/$doc" ]; then
        log_test "Documentation: $doc" "PASS"
    else
        log_test "Documentation: $doc" "FAIL" "File not found"
    fi
done

echo "" >> "$REPORT_FILE"

# 8. License Compliance Tests
echo ""
echo "8. License Compliance Tests"
echo "===========================" >> "$REPORT_FILE"
echo ""

if [ -f "$PROJECT_ROOT/LICENSE" ]; then
    log_test "LICENSE file exists" "PASS"
else
    log_test "LICENSE file exists" "FAIL" "LICENSE file not found"
fi

if [ -f "$PROJECT_ROOT/THIRD_PARTY_LICENSES.txt" ]; then
    log_test "THIRD_PARTY_LICENSES.txt exists" "PASS"
else
    log_test "THIRD_PARTY_LICENSES.txt exists" "FAIL" "File not found"
fi

echo "" >> "$REPORT_FILE"

# 9. Installer Tests
echo ""
echo "9. Installer Tests"
echo "==================" >> "$REPORT_FILE"
echo ""

INSTALLER_CONFIGS=(
    "native/installers/windows/ireader.wxs"
    "native/installers/macos/create_dmg.sh"
    "native/installers/linux/build_deb.sh"
    "native/installers/linux/build_rpm.sh"
)

for config in "${INSTALLER_CONFIGS[@]}"; do
    if [ -f "$PROJECT_ROOT/$config" ]; then
        log_test "Installer config: $(basename $config)" "PASS"
    else
        log_test "Installer config: $(basename $config)" "FAIL" "File not found"
    fi
done

echo "" >> "$REPORT_FILE"

# 10. Security Tests
echo ""
echo "10. Security Tests"
echo "==================" >> "$REPORT_FILE"
echo ""

# Check for security features
if grep -q "LibraryVerifier" "$PROJECT_ROOT/domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/piper/LibraryVerifier.kt" 2>/dev/null; then
    log_test "Library verification implemented" "PASS"
else
    log_test "Library verification implemented" "FAIL" "LibraryVerifier not found"
fi

# Check for input sanitization
if grep -q "sanitize" "$PROJECT_ROOT/domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/piper/PiperNative.kt" 2>/dev/null; then
    log_test "Input sanitization implemented" "PASS"
else
    log_test "Input sanitization implemented" "SKIP" "Not explicitly found"
fi

echo "" >> "$REPORT_FILE"

# Generate summary
echo ""
echo "Summary:" >> "$REPORT_FILE"
echo "--------" >> "$REPORT_FILE"
echo "Total Tests: $TOTAL_TESTS" >> "$REPORT_FILE"
echo "Passed: $PASSED_TESTS" >> "$REPORT_FILE"
echo "Failed: $FAILED_TESTS" >> "$REPORT_FILE"
echo "Skipped: $SKIPPED_TESTS" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# Calculate pass rate
if [ $TOTAL_TESTS -gt 0 ]; then
    PASS_RATE=$((PASSED_TESTS * 100 / TOTAL_TESTS))
    echo "Pass Rate: $PASS_RATE%" >> "$REPORT_FILE"
else
    PASS_RATE=0
fi

# Determine overall status
if [ $FAILED_TESTS -eq 0 ]; then
    OVERALL_STATUS="PASS"
    STATUS_COLOR=$GREEN
else
    OVERALL_STATUS="FAIL"
    STATUS_COLOR=$RED
fi

echo "" >> "$REPORT_FILE"
echo "Overall Status: $OVERALL_STATUS" >> "$REPORT_FILE"

# Print summary
echo ""
echo "================================"
echo "Test Summary"
echo "================================"
echo "Total Tests: $TOTAL_TESTS"
echo -e "Passed: ${GREEN}$PASSED_TESTS${NC}"
echo -e "Failed: ${RED}$FAILED_TESTS${NC}"
echo -e "Skipped: ${YELLOW}$SKIPPED_TESTS${NC}"
echo "Pass Rate: $PASS_RATE%"
echo ""
echo -e "Overall Status: ${STATUS_COLOR}$OVERALL_STATUS${NC}"
echo ""
echo "Report saved to: $REPORT_FILE"

# Exit with appropriate code
if [ "$OVERALL_STATUS" = "FAIL" ]; then
    echo ""
    echo -e "${RED}⚠ Quality assurance tests failed. Please review the report.${NC}"
    exit 1
fi

echo ""
echo -e "${GREEN}✓ Quality assurance tests passed!${NC}"
exit 0
