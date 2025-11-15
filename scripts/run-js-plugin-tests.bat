@echo off
REM Script to run all JavaScript plugin tests
REM Windows batch file

echo ========================================
echo JavaScript Plugin Test Suite
echo ========================================
echo.

echo [1/5] Running unit tests...
call gradlew domain:testDebugUnitTest --tests "ireader.domain.js.*"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Unit tests failed!
    exit /b 1
)
echo Unit tests passed!
echo.

echo [2/5] Running end-to-end tests...
call gradlew domain:testDebugUnitTest --tests "JSPluginEndToEndTest"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: End-to-end tests failed!
    exit /b 1
)
echo End-to-end tests passed!
echo.

echo [3/5] Running performance tests...
call gradlew domain:testDebugUnitTest --tests "JSPluginPerformanceTest"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Performance tests failed!
    exit /b 1
)
echo Performance tests passed!
echo.

echo [4/5] Running validation tests...
call gradlew domain:testDebugUnitTest --tests "JSPluginValidatorTest"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Validation tests failed!
    exit /b 1
)
echo Validation tests passed!
echo.

echo [5/5] Running filter tests...
call gradlew domain:testDebugUnitTest --tests "JSFilterConverterTest"
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Filter tests failed!
    exit /b 1
)
echo Filter tests passed!
echo.

echo ========================================
echo All tests passed successfully!
echo ========================================
echo.

echo Generating code coverage report...
call gradlew koverHtmlReport
if %ERRORLEVEL% EQU 0 (
    echo Coverage report generated at: build/reports/kover/html/index.html
)

echo.
echo Test execution complete!
pause
