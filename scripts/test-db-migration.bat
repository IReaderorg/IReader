@echo off
REM Database Migration Test Script for Windows
REM This script helps test database migrations locally before pushing to CI

setlocal enabledelayedexpansion

echo Testing Database Migrations...
echo.

REM Check if gradlew.bat exists
if not exist "gradlew.bat" (
    echo [ERROR] gradlew.bat not found. Please run this script from the project root.
    exit /b 1
)

echo Running database migration tests...
echo.

REM Run the migration tests
call gradlew.bat :data:testDebugUnitTest --tests "ireader.data.DatabaseMigrationTest" --stacktrace

if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] All database migration tests passed!
    echo.
    echo Database migrations are ready for release
    exit /b 0
) else (
    echo.
    echo [ERROR] Database migration tests failed!
    echo.
    echo Please fix the migration issues before creating a release.
    echo Check the test output above for details.
    echo.
    echo Common issues:
    echo   - Missing migration function for a version
    echo   - SQL syntax errors in migration
    echo   - Foreign key constraint violations
    echo   - Missing tables or columns
    echo.
    exit /b 1
)
