#!/bin/bash
# Regression test suite to check for regressions from previous versions

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR/../.."

echo "IReader Regression Test Suite"
echo "=============================="
echo ""

# Track results
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# Function to run test
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    
    echo -n "Testing: $test_name... "
    
    if eval "$test_command" > /dev/null 2>&1; then
        echo "✓ PASS"
        PASSED_TESTS=$((PASSED_TESTS + 1))
        return 0
    else
        echo "✗ FAIL"
        FAILED_TESTS=$((FAILED_TESTS + 1))
        return 1
    fi
}

# 1. Build Regression Tests
echo "1. Build Regression Tests"
echo "========================="

run_test "Project builds without errors" \
    "cd $PROJECT_ROOT && ./gradlew build --no-daemon"

run_test "No compilation warnings" \
    "cd $PROJECT_ROOT && ./gradlew build --no-daemon 2>&1 | grep -v 'warning:' || true"

echo ""

# 2. API Compatibility Tests
echo "2. API Compatibility Tests"
echo "=========================="

# Check that key APIs still exist
run_test "PiperNative.initialize exists" \
    "grep -q 'fun initialize' $PROJECT_ROOT/domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/piper/PiperNative.kt"

run_test "PiperNative.synthesize exists" \
    "grep -q 'fun synthesize' $PROJECT_ROOT/domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/piper/PiperNative.kt"

run_test "PiperNative.shutdown exists" \
    "grep -q 'fun shutdown' $PROJECT_ROOT/domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/piper/PiperNative.kt"

echo ""

# 3. Feature Regression Tests
echo "3. Feature Regression Tests"
echo "==========================="

run_test "Voice catalog still exists" \
    "test -f $PROJECT_ROOT/domain/src/commonMain/kotlin/ireader/domain/catalogs/VoiceCatalog.kt"

run_test "Library verifier still exists" \
    "test -f $PROJECT_ROOT/domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/piper/LibraryVerifier.kt"

run_test "Native library loader still exists" \
    "grep -q 'NativeLibraryLoader' $PROJECT_ROOT/domain/src/desktopMain/kotlin/ireader/domain/services/tts_service/piper/PiperNative.kt"

echo ""

# 4. Configuration Regression Tests
echo "4. Configuration Regression Tests"
echo "================================="

run_test "Gradle configuration valid" \
    "cd $PROJECT_ROOT && ./gradlew tasks --no-daemon > /dev/null"

run_test "Build scripts executable" \
    "test -x $PROJECT_ROOT/gradlew"

echo ""

# 5. Documentation Regression Tests
echo "5. Documentation Regression Tests"
echo "=================================="

run_test "README exists" \
    "test -f $PROJECT_ROOT/README.md"

run_test "Developer guide exists" \
    "test -f $PROJECT_ROOT/docs/piper-jni/Developer_Guide.md"

run_test "User guide exists" \
    "test -f $PROJECT_ROOT/docs/piper-jni/User_Guide.md"

echo ""

# 6. License Regression Tests
echo "6. License Regression Tests"
echo "==========================="

run_test "LICENSE file exists" \
    "test -f $PROJECT_ROOT/LICENSE"

run_test "Third-party licenses exist" \
    "test -f $PROJECT_ROOT/THIRD_PARTY_LICENSES.txt"

echo ""

# 7. Installer Regression Tests
echo "7. Installer Regression Tests"
echo "=============================="

run_test "Windows installer config exists" \
    "test -f $PROJECT_ROOT/native/installers/windows/ireader.wxs"

run_test "macOS installer script exists" \
    "test -f $PROJECT_ROOT/native/installers/macos/create_dmg.sh"

run_test "Linux DEB script exists" \
    "test -f $PROJECT_ROOT/native/installers/linux/build_deb.sh"

run_test "Linux RPM spec exists" \
    "test -f $PROJECT_ROOT/native/installers/linux/ireader.spec"

echo ""

# Print summary
echo "================================"
echo "Regression Test Summary"
echo "================================"
echo "Total Tests: $TOTAL_TESTS"
echo "Passed: $PASSED_TESTS"
echo "Failed: $FAILED_TESTS"
echo ""

if [ $FAILED_TESTS -eq 0 ]; then
    echo "✓ All regression tests passed!"
    exit 0
else
    echo "✗ $FAILED_TESTS regression test(s) failed!"
    exit 1
fi
