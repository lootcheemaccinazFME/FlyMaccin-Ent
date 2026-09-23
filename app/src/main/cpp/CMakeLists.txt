cmake_minimum_required(VERSION 3.22.1)
project(fme_ps5 LANGUAGES C CXX)

set(CMAKE_C_STANDARD 11)
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

set(FME_CHIAKI_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../third_party/chiaki/upstream")
if(NOT EXISTS "${FME_CHIAKI_DIR}/lib/CMakeLists.txt")
  message(FATAL_ERROR "FME ERROR: pinned chiaki-ng source missing. Run scripts/vendor-chiaki.sh")
endif()

# Protocol-only Android graph. Do not add chiaki-ng's top-level project because
# CHIAKI_ENABLE_ANDROID there also adds its upstream Android JNI/Oboe app target.
# FME needs chiaki-lib only.
set(CHIAKI_ENABLE_ANDROID ON)
set(CHIAKI_ENABLE_TESTS OFF)
set(CHIAKI_ENABLE_CLI OFF)
set(CHIAKI_ENABLE_GUI OFF)
set(CHIAKI_ENABLE_BOREALIS OFF)
set(CHIAKI_ENABLE_SETSU OFF)
set(CHIAKI_ENABLE_STEAMDECK_NATIVE OFF)
set(CHIAKI_ENABLE_FFMPEG_DECODER OFF)
set(CHIAKI_ENABLE_PI_DECODER OFF)
set(CHIAKI_ENABLE_STEAM_SHORTCUT OFF)
set(CHIAKI_ENABLE_SPEEX OFF)
set(CHIAKI_ENABLE_RUDP OFF)
set(CHIAKI_IS_SWITCH OFF)
set(CHIAKI_LIB_ENABLE_OPUS OFF)
set(CHIAKI_LIB_ENABLE_MBEDTLS OFF)
set(CHIAKI_LIB_OPENSSL_EXTERNAL_PROJECT ON)
set(CHIAKI_LIB_JSONC_EXTERNAL_PROJECT ON)
set(CHIAKI_LIB_MINIUPNPC_EXTERNAL_PROJECT ON)
set(CHIAKI_USE_SYSTEM_JERASURE OFF)
set(CHIAKI_USE_SYSTEM_NANOPB OFF)
set(CHIAKI_USE_SYSTEM_CURL OFF)

list(APPEND CMAKE_MODULE_PATH "${FME_CHIAKI_DIR}/cmake")
find_package(PythonInterp 3 REQUIRED)
include(OpenSSLExternalProject)
include(JsoncExternalProject)
include(MiniupnpcExternalProject)
add_subdirectory("${FME_CHIAKI_DIR}/third-party" "${CMAKE_BINARY_DIR}/chiaki-third-party" EXCLUDE_FROM_ALL)

add_definitions(
  -DCHIAKI_VERSION_MAJOR=1
  -DCHIAKI_VERSION_MINOR=10
  -DCHIAKI_VERSION_PATCH=0
  -DCHIAKI_VERSION=\"1.10.0\"
)
add_subdirectory("${FME_CHIAKI_DIR}/lib" "${CMAKE_BINARY_DIR}/chiaki-lib" EXCLUDE_FROM_ALL)
if(NOT TARGET chiaki-lib)
  message(FATAL_ERROR "FME ERROR: pinned chiaki-ng did not expose chiaki-lib")
endif()

add_library(fme_ps5 SHARED fme_ps5.cpp chiaki_adapter.cpp)
find_library(log-lib log)
target_link_libraries(fme_ps5 PRIVATE chiaki-lib ${log-lib})
target_include_directories(fme_ps5 PRIVATE "${FME_CHIAKI_DIR}/lib/include")
target_compile_definitions(fme_ps5 PRIVATE FME_HAS_CHIAKI=1)

# Android 15+ 16 KB page-size compatibility for the packaged shared object.
target_link_options(fme_ps5 PRIVATE
  "-Wl,-z,max-page-size=16384"
  "-Wl,-z,common-page-size=16384"
)
