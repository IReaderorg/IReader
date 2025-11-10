#!/bin/bash
# setup_deps.sh - Install build dependencies
# Usage: ./setup_deps.sh

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

print_success() { echo -e "${GREEN}✓ $1${NC}"; }
print_info() { echo -e "${CYAN}$1${NC}"; }
print_warning() { echo -e "${YELLOW}⚠ $1${NC}"; }
print_error() { echo -e "${RED}✗ $1${NC}"; }

print_info "=== Piper JNI Dependency Setup ==="

# Detect OS
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    OS="linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    OS="macos"
elif [[ "$OSTYPE" == "msys" ]] || [[ "$OSTYPE" == "cygwin" ]]; then
    OS="windows"
else
    print_error "Unsupported OS: $OSTYPE"
    exit 1
fi

print_info "Detected OS: $OS"

# Install dependencies based on OS
case $OS in
    linux)
        print_info "\nDetecting Linux distribution..."
        
        if [ -f /etc/os-release ]; then
            . /etc/os-release
            DISTRO=$ID
        else
            print_error "Cannot detect Linux distribution"
            exit 1
        fi
        
        print_info "Distribution: $DISTRO"
        
        case $DISTRO in
            ubuntu|debian)
                print_info "\nInstalling dependencies for Ubuntu/Debian..."
                sudo apt-get update
                sudo apt-get install -y \
                    build-essential \
                    cmake \
                    git \
                    openjdk-17-jdk \
                    libasound2-dev \
                    libpulse-dev \
                    pkg-config
                print_success "Dependencies installed"
                ;;
            
            fedora|rhel|centos)
                print_info "\nInstalling dependencies for Fedora/RHEL..."
                sudo dnf install -y \
                    gcc-c++ \
                    cmake \
                    git \
                    java-17-openjdk-devel \
                    alsa-lib-devel \
                    pulseaudio-libs-devel \
                    pkg-config
                print_success "Dependencies installed"
                ;;
            
            arch|manjaro)
                print_info "\nInstalling dependencies for Arch Linux..."
                sudo pacman -S --noconfirm \
                    base-devel \
                    cmake \
                    git \
                    jdk17-openjdk \
                    alsa-lib \
                    libpulse \
                    pkg-config
                print_success "Dependencies installed"
                ;;
            
            *)
                print_warning "Unsupported distribution: $DISTRO"
                print_info "Please install manually:"
                print_info "  - build-essential / gcc-c++"
                print_info "  - cmake"
                print_info "  - git"
                print_info "  - openjdk-17-jdk"
                print_info "  - libasound2-dev / alsa-lib-devel"
                print_info "  - libpulse-dev / pulseaudio-libs-devel"
                ;;
        esac
        ;;
    
    macos)
        print_info "\nInstalling dependencies for macOS..."
        
        # Check if Homebrew is installed
        if ! command -v brew &> /dev/null; then
            print_warning "Homebrew not found. Installing..."
            /bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"
        fi
        
        # Install dependencies
        brew install cmake
        brew install openjdk@17
        
        # Link Java
        sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk || true
        
        print_success "Dependencies installed"
        
        # Check Xcode Command Line Tools
        if ! xcode-select -p &> /dev/null; then
            print_warning "Xcode Command Line Tools not found"
            print_info "Installing Xcode Command Line Tools..."
            xcode-select --install
            print_info "Please complete the installation and run this script again"
            exit 0
        fi
        ;;
    
    windows)
        print_warning "Windows dependency installation not automated"
        print_info "Please install manually:"
        print_info "  1. Visual Studio 2019+ with C++ development tools"
        print_info "     Download: https://visualstudio.microsoft.com/"
        print_info "  2. CMake 3.15+"
        print_info "     Download: https://cmake.org/download/"
        print_info "  3. JDK 17+"
        print_info "     Download: https://adoptium.net/"
        print_info "  4. Set JAVA_HOME environment variable"
        exit 0
        ;;
esac

# Verify installations
print_info "\nVerifying installations..."

# Check CMake
if command -v cmake &> /dev/null; then
    CMAKE_VERSION=$(cmake --version | head -n1)
    print_success "CMake: $CMAKE_VERSION"
else
    print_error "CMake not found"
fi

# Check compiler
if command -v g++ &> /dev/null; then
    GCC_VERSION=$(g++ --version | head -n1)
    print_success "G++: $GCC_VERSION"
elif command -v clang++ &> /dev/null; then
    CLANG_VERSION=$(clang++ --version | head -n1)
    print_success "Clang++: $CLANG_VERSION"
else
    print_error "C++ compiler not found"
fi

# Check Java
if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | head -n1)
    print_success "Java: $JAVA_VERSION"
    
    if [ -n "$JAVA_HOME" ]; then
        print_success "JAVA_HOME: $JAVA_HOME"
    else
        print_warning "JAVA_HOME not set"
        if command -v java &> /dev/null; then
            JAVA_PATH=$(which java)
            SUGGESTED_JAVA_HOME=$(dirname $(dirname $(readlink -f $JAVA_PATH 2>/dev/null || echo $JAVA_PATH)))
            print_info "Suggested JAVA_HOME: $SUGGESTED_JAVA_HOME"
            print_info "Add to your shell profile:"
            print_info "  export JAVA_HOME=$SUGGESTED_JAVA_HOME"
        fi
    fi
else
    print_error "Java not found"
fi

print_success "\n=== Setup Complete ==="
print_info "You can now build the native library:"
print_info "  cd native"
print_info "  ./scripts/build_$OS.sh"
