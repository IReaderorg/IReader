#!/bin/bash

# Build iOS Framework Script
# This script builds the Kotlin Multiplatform iOS frameworks
# Run this on macOS with Xcode installed

set -e

echo "🔨 Building iOS frameworks..."

# Navigate to project root
cd "$(dirname "$0")/.."

# Build presentation framework for iOS Simulator (Apple Silicon)
echo "📦 Building presentation framework for iOS Simulator (arm64)..."
./gradlew :presentation:linkDebugFrameworkIosSimulatorArm64

# Build presentation-core framework
echo "📦 Building presentation-core framework for iOS Simulator (arm64)..."
./gradlew :presentation-core:linkDebugFrameworkIosSimulatorArm64

# Build ios-build-check framework (for testing)
echo "📦 Building ios-build-check framework..."
./gradlew :ios-build-check:linkDebugFrameworkIosSimulatorArm64

echo ""
echo "✅ iOS frameworks built successfully!"
echo ""
echo "Framework locations:"
echo "  - presentation/build/bin/iosSimulatorArm64/debugFramework/presentation.framework"
echo "  - presentation-core/build/bin/iosSimulatorArm64/debugFramework/presentation_core.framework"
echo "  - ios-build-check/build/bin/iosSimulatorArm64/debugFramework/iosBuildCheck.framework"
echo ""
echo "To build for physical devices, run:"
echo "  ./gradlew :presentation:linkDebugFrameworkIosArm64"
echo ""
echo "To build release versions, run:"
echo "  ./gradlew :presentation:linkReleaseFrameworkIosArm64"
