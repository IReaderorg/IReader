#!/bin/bash
# build_macos.sh - Build script for macOS
# Usage: ./build_macos.sh [release|debug] [clean] [test] [universal]

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Functions
print_success() { echo -e "${GREEN}✓ $1${NC}"; }
print_info() { echo -e "${CYAN}$1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠ $1${NC}"; }
print_error() { echo -e "${RED}✗ $1${NC}"; }

# Parse arguments
CONFIG="Release"
CLEAN=false
TEST=false
UNIVERSAL=false

for arg in "$@"; do
    case $arg in
        debug|Debug|DEBUG)
            CONFIG="Debug"
            ;;
        release|Release|RELEASE)
            CONFIG="Release"
            ;;
        clean|Clean|CLEAN)
            CLEAN=true
            ;;
        test|Test|TEST)
            TEST=true
            ;;
        universal|Universal|UNIVERSAL)
            UNIVERSAL=true
            ;;
        help|--help|-h)
            echo "Usage: ./build_macos.sh [release|debug] [clean] [test] [universal]"
            echo ""
            echo "Options:"
            echo "  release    Build in Release mode (default)"
            echo "  debug      Build in Debug mode"
            echo "  clean      Clean build directory before building"
            echo "  test       Run tests after building"
            echo "  universal  Build universal binary (x86_64 + arm64)"
            echo "  help       Show this help message"
            exit 0
            ;;
    esac
done

print_info "=== Piper JNI Build Script for macOS ==="
print_info "Configuration: $CONFIG"
if [ "$UNIVERSAL" = true ]; then
    print_info "Building universal binary (x86_64 + arm64)"
fi

# Check prerequisites
print_info "\nChecking prerequisites..."

# Check CMake
if command -v cmake &> /dev/null; then
    CMAKE_VERSION=$(cmake --version | head -n1)
    print_success "CMake found: $CMAKE_VERSION"
else
    print_error "CMake not found. Please install CMake 3.15 or later."
    print_info "Install with: brew install cmake"
    exit 1
fi

# Check Xcode Command Line Tools
if xcode-select -p &> /dev/null; then
    print_success "Xcode Command Line Tools found"
else
    print_error "Xcode Command Line Tools not found."
    print_info "Install with: xcode-select --install"
    exit 1
fi

# Check Java/JDK
if [ -n "$JAVA_HOME" ]; then
    print_success "JAVA_HOME set: $JAVA_HOME"
else
    print_warning "JAVA_HOME not set. Attempting to find Java..."
    if command -v java &> /dev/null; then
        JAVA_PATH=$(which java)
        JAVA_HOME=$(dirname $(dirname $(readlink -f $JAVA_PATH 2>/dev/null || echo $JAVA_PATH)))
        export JAVA_HOME
        print_info "Found Java at: $JAVA_HOME"
    else
        print_error "Java not found. Please install JDK 11 or later and set JAVA_HOME."
        exit 1
    fi
fi

# Navigate to native directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
NATIVE_DIR="$(dirname "$SCRIPT_DIR")"
cd "$NATIVE_DIR"

print_info "\nNative directory: $NATIVE_DIR"

# Determine architecture
ARCH=$(uname -m)
if [ "$UNIVERSAL" = true ]; then
    BUILD_DIR="$NATIVE_DIR/build/macos-universal"
    PLATFORM_NAME="macos-universal"
elif [ "$ARCH" = "arm64" ]; then
    BUILD_DIR="$NATIVE_DIR/build/macos-arm64"
    PLATFORM_NAME="macos-arm64"
else
    BUILD_DIR="$NATIVE_DIR/build/macos-x64"
    PLATFORM_NAME="macos-x64"
fi

# Clean if requested
if [ "$CLEAN" = true ] && [ -d "$BUILD_DIR" ]; then
    print_info "\nCleaning build directory..."
    rm -rf "$BUILD_DIR"
    print_success "Build directory cleaned"
fi

# Create build directory
mkdir -p "$BUILD_DIR"

# Configure
print_info "\nConfiguring CMake..."
cd "$BUILD_DIR"

CMAKE_ARGS=(
    -DCMAKE_BUILD_TYPE=$CONFIG
    -DCMAKE_INSTALL_PREFIX="$BUILD_DIR/install"
    -DCMAKE_OSX_DEPLOYMENT_TARGET=10.14
)

if [ "$UNIVERSAL" = true ]; then
    CMAKE_ARGS+=(-DCMAKE_OSX_ARCHITECTURES="x86_64;arm64")
    CMAKE_ARGS+=(-DBUILD_UNIVERSAL=ON)
fi

if cmake ../.. "${CMAKE_ARGS[@]}"; then
    print_success "CMake configuration complete"
else
    print_error "CMake configuration failed"
    exit 1
fi

# Build
print_info "\nBuilding..."
if cmake --build . --config $CONFIG --parallel $(sysctl -n hw.ncpu); then
    print_success "Build complete"
else
    print_error "Build failed"
    exit 1
fi

# Find the built library
LIB_PATH="$BUILD_DIR/libpiper_jni.dylib"
if [ -f "$LIB_PATH" ]; then
    print_success "\n✓ Library built successfully: $LIB_PATH"
    LIB_SIZE=$(du -h "$LIB_PATH" | cut -f1)
    print_info "  Size: $LIB_SIZE"
    
    # Show architectures
    print_info "  Architectures:"
    lipo -info "$LIB_PATH" | sed 's/^/    /'
else
    print_warning "Library not found at expected location: $LIB_PATH"
fi

# Run tests if requested
if [ "$TEST" = true ]; then
    print_info "\nRunning tests..."
    if ctest --build-config $CONFIG --output-on-failure; then
        print_success "All tests passed"
    else
        print_warning "Some tests failed"
    fi
fi

print_success "\n=== Build Complete ==="
print_info "Build artifacts located in: $BUILD_DIR"
print_info "Library copied to: domain/src/desktopMain/resources/native/$PLATFORM_NAME/"
