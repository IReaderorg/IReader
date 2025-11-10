# FindONNXRuntime.cmake - Locate ONNX Runtime library
#
# This module defines:
#  ONNXRuntime_FOUND - System has ONNX Runtime
#  ONNXRuntime_INCLUDE_DIRS - The ONNX Runtime include directories
#  ONNXRuntime_LIBRARIES - The libraries needed to use ONNX Runtime
#  ONNXRuntime_VERSION - The version of ONNX Runtime found

# Search for ONNX Runtime in common locations
find_path(ONNXRuntime_INCLUDE_DIR
    NAMES onnxruntime_cxx_api.h onnxruntime_c_api.h
    PATHS
        ${ONNXRUNTIME_ROOT}/include
        ${CMAKE_SOURCE_DIR}/third_party/onnxruntime/include
        ${CMAKE_SOURCE_DIR}/../third_party/onnxruntime/include
        /usr/local/include
        /usr/include
        $ENV{ONNXRUNTIME_HOME}/include
    PATH_SUFFIXES onnxruntime core/session
)

find_library(ONNXRuntime_LIBRARY
    NAMES onnxruntime libonnxruntime
    PATHS
        ${ONNXRUNTIME_ROOT}/lib
        ${CMAKE_SOURCE_DIR}/third_party/onnxruntime/lib
        ${CMAKE_SOURCE_DIR}/../third_party/onnxruntime/lib
        /usr/local/lib
        /usr/lib
        $ENV{ONNXRUNTIME_HOME}/lib
    PATH_SUFFIXES
        ${CMAKE_LIBRARY_ARCHITECTURE}
)

# Handle the QUIETLY and REQUIRED arguments
include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(ONNXRuntime
    FOUND_VAR ONNXRuntime_FOUND
    REQUIRED_VARS
        ONNXRuntime_LIBRARY
        ONNXRuntime_INCLUDE_DIR
    VERSION_VAR ONNXRuntime_VERSION
)

if(ONNXRuntime_FOUND)
    set(ONNXRuntime_LIBRARIES ${ONNXRuntime_LIBRARY})
    set(ONNXRuntime_INCLUDE_DIRS ${ONNXRuntime_INCLUDE_DIR})
    
    # Create imported target
    if(NOT TARGET ONNXRuntime::ONNXRuntime)
        add_library(ONNXRuntime::ONNXRuntime UNKNOWN IMPORTED)
        set_target_properties(ONNXRuntime::ONNXRuntime PROPERTIES
            IMPORTED_LOCATION "${ONNXRuntime_LIBRARY}"
            INTERFACE_INCLUDE_DIRECTORIES "${ONNXRuntime_INCLUDE_DIR}"
        )
    endif()
    
    message(STATUS "Found ONNX Runtime: ${ONNXRuntime_LIBRARY}")
endif()

mark_as_advanced(ONNXRuntime_INCLUDE_DIR ONNXRuntime_LIBRARY)
