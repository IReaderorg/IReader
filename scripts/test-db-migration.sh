#!/bin/bash

# Database Migration Test Script
# This script helps test database migrations locally before pushing to CI

set -e

echo "🔍 Testing Database Migrations..."
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    print_error "gradlew not found. Please run this script from the project root."
    exit 1
fi

# Make gradlew executable
chmod +x ./gradlew

echo "Running database migration tests..."
echo ""

# Run the migration tests
if ./gradlew :data:testDebugUnitTest --tests "ireader.data.DatabaseMigrationTest" --stacktrace; then
    print_success "All database migration tests passed!"
    echo ""
    echo "✅ Database migrations are ready for release"
    exit 0
else
    print_error "Database migration tests failed!"
    echo ""
    print_warning "Please fix the migration issues before creating a release."
    print_warning "Check the test output above for details."
    echo ""
    echo "Common issues:"
    echo "  - Missing migration function for a version"
    echo "  - SQL syntax errors in migration"
    echo "  - Foreign key constraint violations"
    echo "  - Missing tables or columns"
    echo ""
    exit 1
fi
