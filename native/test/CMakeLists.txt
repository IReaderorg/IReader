# Test configuration for Piper JNI
cmake_minimum_required(VERSION 3.15)

# Placeholder for tests
# Tests will be implemented in task 8

# For now, create a simple test that always passes
add_executable(basic_test basic_test.cpp)

# Add test
add_test(NAME BasicTest COMMAND basic_test)

message(STATUS "Test configuration complete")
