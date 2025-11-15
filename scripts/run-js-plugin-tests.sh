#!/bin/bash
# Script to run all JavaScript plugin tests
# Unix/Linux/macOS shell script

set -e  # Exit on error

echo "========================================"
echo "JavaScript Plugin Test Suite"
echo "========================================"
echo ""

echo "[1/5] Running unit tests..."
./gradlew domain:testDebugUnitTest --tests "ireader.domain.js.*"
echo "✓ Unit tests passed!"
echo ""

echo "[2/5] Running end-to-end tests..."
./gradlew domain:testDebugUnitTest --tests "JSPluginEndToEndTest"
echo "✓ End-to-end tests passed!"
echo ""

echo "[3/5] Running performance tests..."
./gradlew domain:testDebugUnitTest --tests "JSPluginPerformanceTest"
echo "✓ Performance tests passed!"
echo ""

echo "[4/5] Running validation tests..."
./gradlew domain:testDebugUnitTest --tests "JSPluginValidatorTest"
echo "✓ Validation tests passed!"
echo ""

echo "[5/5] Running filter tests..."
./gradlew domain:testDebugUnitTest --tests "JSFilterConverterTest"
echo "✓ Filter tests passed!"
echo ""

echo "========================================"
echo "All tests passed successfully!"
echo "========================================"
echo ""

echo "Generating code coverage report..."
./gradlew koverHtmlReport
if [ $? -eq 0 ]; then
    echo "Coverage report generated at: build/reports/kover/html/index.html"
fi

echo ""
echo "Test execution complete!"
