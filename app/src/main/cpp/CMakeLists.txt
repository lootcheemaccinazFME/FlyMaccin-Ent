cmake_minimum_required(VERSION 3.22.1)
project(fme_ps5)

add_library(fme_ps5 SHARED fme_ps5.cpp chiaki_adapter.cpp)
find_library(log-lib log)
target_link_libraries(fme_ps5 ${log-lib})

set(FME_CHIAKI_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../third_party/chiaki/upstream")
if(EXISTS "${FME_CHIAKI_DIR}/CMakeLists.txt")
  # Reuse chiaki-ng v1.10.0's own dependency setup, but keep the application
  # graph narrow. Android mode prevents the desktop GUI/CLI graph from being
  # selected while the external-project switches build target-ABI dependencies.
  set(CHIAKI_ENABLE_ANDROID ON CACHE BOOL "" FORCE)
  set(CHIAKI_ENABLE_TESTS OFF CACHE BOOL "" FORCE)
  set(CHIAKI_ENABLE_CLI OFF CACHE BOOL "" FORCE)
  set(CHIAKI_ENABLE_GUI OFF CACHE BOOL "" FORCE)
  set(CHIAKI_ENABLE_BOREALIS OFF CACHE BOOL "" FORCE)
  set(CHIAKI_ENABLE_SETSU OFF CACHE STRING "" FORCE)
  set(CHIAKI_ENABLE_STEAMDECK_NATIVE OFF CACHE STRING "" FORCE)
  set(CHIAKI_ENABLE_FFMPEG_DECODER OFF CACHE STRING "" FORCE)
  set(CHIAKI_ENABLE_PI_DECODER OFF CACHE STRING "" FORCE)
  set(CHIAKI_ENABLE_STEAM_SHORTCUT OFF CACHE BOOL "" FORCE)

  # Never resolve protocol dependencies from the Ubuntu host during an Android
  # cross-build. Let upstream build its pinned target-ABI copies.
  set(CHIAKI_USE_SYSTEM_JERASURE OFF CACHE STRING "" FORCE)
  set(CHIAKI_USE_SYSTEM_NANOPB OFF CACHE STRING "" FORCE)
  set(CHIAKI_USE_SYSTEM_CURL OFF CACHE STRING "" FORCE)
  set(CHIAKI_LIB_JSONC_EXTERNAL_PROJECT ON CACHE BOOL "" FORCE)
  set(CHIAKI_LIB_MINIUPNPC_EXTERNAL_PROJECT ON CACHE BOOL "" FORCE)
  set(CHIAKI_LIB_OPENSSL_EXTERNAL_PROJECT ON CACHE BOOL "" FORCE)

  # Keep Opus enabled because it is part of the eventual Remote Play audio path.
  set(CHIAKI_LIB_ENABLE_OPUS ON CACHE BOOL "" FORCE)

  set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384")
  set(CMAKE_MODULE_LINKER_FLAGS "${CMAKE_MODULE_LINKER_FLAGS} -Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384")

  add_subdirectory("${FME_CHIAKI_DIR}" "${CMAKE_BINARY_DIR}/chiaki-ng" EXCLUDE_FROM_ALL)

  if(NOT TARGET chiaki-lib)
    message(FATAL_ERROR "FME ERROR: chiaki-ng v1.10.0 did not expose chiaki-lib")
  endif()

  target_link_libraries(fme_ps5 PRIVATE chiaki-lib)
  target_include_directories(fme_ps5 PRIVATE "${FME_CHIAKI_DIR}/lib/include")
  target_compile_definitions(fme_ps5 PRIVATE FME_HAS_CHIAKI=1)
endif()
