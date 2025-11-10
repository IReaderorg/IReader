# FindPiper.cmake - Locate Piper TTS library
#
# This module defines:
#  Piper_FOUND - System has Piper
#  Piper_INCLUDE_DIRS - The Piper include directories
#  Piper_LIBRARIES - The libraries needed to use Piper
#  Piper_VERSION - The version of Piper found

# Search for Piper in common locations
find_path(Piper_INCLUDE_DIR
    NAMES piper.hpp piper.h
    PATHS
        ${PIPER_ROOT}/include
        ${CMAKE_SOURCE_DIR}/third_party/piper/include
        ${CMAKE_SOURCE_DIR}/../third_party/piper/include
        /usr/local/include
        /usr/include
        $ENV{PIPER_HOME}/include
    PATH_SUFFIXES piper
)

find_library(Piper_LIBRARY
    NAMES piper libpiper piper_phonemize
    PATHS
        ${PIPER_ROOT}/lib
        ${CMAKE_SOURCE_DIR}/third_party/piper/lib
        ${CMAKE_SOURCE_DIR}/../third_party/piper/lib
        /usr/local/lib
        /usr/lib
        $ENV{PIPER_HOME}/lib
    PATH_SUFFIXES
        ${CMAKE_LIBRARY_ARCHITECTURE}
)

# Handle the QUIETLY and REQUIRED arguments
include(FindPackageHandleStandardArgs)
find_package_handle_standard_args(Piper
    FOUND_VAR Piper_FOUND
    REQUIRED_VARS
        Piper_LIBRARY
        Piper_INCLUDE_DIR
    VERSION_VAR Piper_VERSION
)

if(Piper_FOUND)
    set(Piper_LIBRARIES ${Piper_LIBRARY})
    set(Piper_INCLUDE_DIRS ${Piper_INCLUDE_DIR})
    
    # Create imported target
    if(NOT TARGET Piper::Piper)
        add_library(Piper::Piper UNKNOWN IMPORTED)
        set_target_properties(Piper::Piper PROPERTIES
            IMPORTED_LOCATION "${Piper_LIBRARY}"
            INTERFACE_INCLUDE_DIRECTORIES "${Piper_INCLUDE_DIR}"
        )
    endif()
    
    message(STATUS "Found Piper: ${Piper_LIBRARY}")
endif()

mark_as_advanced(Piper_INCLUDE_DIR Piper_LIBRARY)
