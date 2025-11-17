@echo off
REM Fast build script for Windows
REM This script provides quick access to optimized build commands

echo ========================================
echo Infinity Fast Build Script
echo ========================================
echo.

:menu
echo Select build type:
echo 1. Dev Debug (fastest - limited resources)
echo 2. Standard Debug (normal debug build)
echo 3. Standard Release
echo 4. Clean Build
echo 5. Incremental Build (skip tests)
echo 6. Build Scan (analyze performance)
echo 7. Exit
echo.

set /p choice="Enter choice (1-7): "

if "%choice%"=="1" goto devdebug
if "%choice%"=="2" goto standarddebug
if "%choice%"=="3" goto standardrelease
if "%choice%"=="4" goto clean
if "%choice%"=="5" goto incremental
if "%choice%"=="6" goto buildscan
if "%choice%"=="7" goto end
goto menu

:devdebug
echo Building Dev Debug (fastest)...
call gradlew.bat assembleDevDebug -x test -x lint
goto end

:standarddebug
echo Building Standard Debug...
call gradlew.bat assembleStandardDebug -x test -x lint
goto end

:standardrelease
echo Building Standard Release...
call gradlew.bat assembleStandardRelease
goto end

:clean
echo Cleaning build...
call gradlew.bat clean cleanBuildCache
goto end

:incremental
echo Incremental build (skip tests)...
call gradlew.bat assembleDebug -x test -x lint
goto end

:buildscan
echo Building with scan...
call gradlew.bat assembleDebug --scan
goto end

:end
echo.
echo Build complete!
pause
