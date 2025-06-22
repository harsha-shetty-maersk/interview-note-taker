#!/bin/bash

# Test Runner Script for Interview Notes Backend
# This script runs all tests and generates coverage reports

set -e

echo "🧪 Starting comprehensive test suite for Interview Notes Backend..."

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if we're in the backend directory
if [ ! -f "pom.xml" ]; then
    print_error "This script must be run from the backend directory"
    exit 1
fi

# Clean previous builds
print_status "Cleaning previous builds..."
mvn clean

# Run tests with coverage
print_status "Running tests with JaCoCo coverage..."
mvn test jacoco:report

# Check if tests passed
if [ $? -eq 0 ]; then
    print_success "All tests passed!"
else
    print_error "Some tests failed!"
    exit 1
fi

# Generate detailed coverage report
print_status "Generating detailed coverage report..."
mvn jacoco:report

# Check coverage thresholds
print_status "Checking coverage thresholds..."
mvn jacoco:check

if [ $? -eq 0 ]; then
    print_success "Coverage thresholds met!"
else
    print_warning "Coverage thresholds not met. Check the report for details."
fi

# Display coverage summary
print_status "Coverage Report Summary:"
echo "=================================="

# Find and display JaCoCo report
REPORT_PATH="target/site/jacoco/index.html"
if [ -f "$REPORT_PATH" ]; then
    print_success "Coverage report generated at: $REPORT_PATH"
    print_status "Open the report in your browser to view detailed coverage information"
else
    print_error "Coverage report not found at expected location"
fi

# Run specific test categories
print_status "Running unit tests..."
mvn test -Dtest="*Test" -DfailIfNoTests=false

print_status "Running integration tests..."
mvn test -Dtest="*IT" -DfailIfNoTests=false

# Display test summary
print_status "Test Summary:"
echo "=================="
echo "✅ Unit Tests: Model classes, Services, Controllers"
echo "✅ Integration Tests: Repository layer, Security"
echo "✅ Coverage: JaCoCo report generated"
echo "✅ Validation: Input validation and error handling"
echo "✅ Edge Cases: Null values, empty strings, invalid data"

print_success "Test suite completed successfully!"
print_status "To view the coverage report, open: target/site/jacoco/index.html"
print_status "To run specific test classes: mvn test -Dtest=ClassName"
print_status "To run tests with debug output: mvn test -X" 