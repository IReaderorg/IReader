#!/bin/bash
# build_linux.sh - Build script for Linux
# Usage: ./build_linux.sh [release|debug] [clean] [test]

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
        help|--help|-h)
            echo "Usage: ./build_linux.sh [release|debug] [clean] [test]"
            echo ""
            echo "Options:"
            echo "  release    Build in Release mode (default)"
            echo "  debug      Build in Debug mode"
            echo "  clean      Clean build directory before building"
            echo "  test       Run tests after building"
            echo "  help       Show this help message"
            exit 0
            ;;
    esac
done

print_info "=== Piper JNI Build Script for Linux ==="
print_info "Configuration: $CONFIG"

# Check prerequisites
print_info "\nChecking prerequisites..."

# Check CMake
if command -v cmake &> /dev/null; then
    CMAKE_VERSION=$(cmake --version | head -n1)
    print_success "CMake found: $CMAKE_VERSION"
else
    print_error "CMake not found. Please install CMake 3.15 or later."
    print_info "Install with: sudo apt-get install cmake (Ubuntu/Debian)"
    print_info "           or: sudo dnf install cmake (Fedora)"
    print_info "           or: sudo pacman -S cmake (Arch)"
    exit 1
fi

# Check GCC/G++
if command -v g++ &> /dev/null; then
    GCC_VERSION=$(g++ --version | head -n1)
    print_success "G++ found: $GCC_VERSION"
else
    print_error "G++ not found. Please install build-essential."
    print_info "Install with: sudo apt-get install build-essential"
    exit 1
fi

# Check Java/JDK
if [ -n "$JAVA_HOME" ]; then
    print_success "JAVA_HOME set: $JAVA_HOME"
else
    print_warning "JAVA_HOME not set. Attempting to find Java..."
    if command -v java &> /dev/null; then
        JAVA_PATH=$(which java)
        JAVA_HOME=$(dirname $(dirname $(readlink -f $JAVA_PATH)))
        export JAVA_HOME
        print_info "Found Java at: $JAVA_HOME"
    else
        print_error "Java not found. Please install JDK 11 or later and set JAVA_HOME."
        print_info "Install with: sudo apt-get install openjdk-17-jdk"
        exit 1
    fi
fi

# Check for required libraries
print_info "\nChecking for required libraries..."
MISSING_LIBS=()

if ! ldconfig -p | grep -q libasound; then
    MISSING_LIBS+=("libasound2-dev")
fi

if [ ${#MISSING_LIBS[@]} -gt 0 ]; then
    print_warning "Missing libraries: ${MISSING_LIBS[*]}"
    print_info "Install with: sudo apt-get install ${MISSING_LIBS[*]}"
fi

# Navigate to native directory
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
NATIVE_DIR="$(dirname "$SCRIPT_DIR")"
cd "$NATIVE_DIR"

print_info "\nNative directory: $NATIVE_DIR"

# Build directory
BUILD_DIR="$NATIVE_DIR/build/linux"

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

if cmake ../.. \
    -DCMAKE_BUILD_TYPE=$CONFIG \
    -DCMAKE_INSTALL_PREFIX="$BUILD_DIR/install" \
    -DCMAKE_CXX_FLAGS="-D_GLIBCXX_USE_CXX11_ABI=0"; then
    print_success "CMake configuration complete"
else
    print_error "CMake configuration failed"
    exit 1
fi

# Build
print_info "\nBuilding..."
NUM_CORES=$(nproc)
if cmake --build . --config $CONFIG --parallel $NUM_CORES; then
    print_success "Build complete"
else
    print_error "Build failed"
    exit 1
fi

# Find the built library
LIB_PATH="$BUILD_DIR/libpiper_jni.so"
if [ -f "$LIB_PATH" ]; then
    print_success "\n✓ Library built successfully: $LIB_PATH"
    LIB_SIZE=$(du -h "$LIB_PATH" | cut -f1)
    print_info "  Size: $LIB_SIZE"
    
    # Show dependencies
    print_info "  Dependencies:"
    ldd "$LIB_PATH" | grep -v "not found" | head -5 | sed 's/^/    /'
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
print_info "Library copied to: domain/src/desktopMain/resources/native/linux/"
