cmake_minimum_required(VERSION 3.22.1)
project(fme_ps5)

add_library(fme_ps5 SHARED fme_ps5.cpp chiaki_adapter.cpp)
find_library(log-lib log)
target_link_libraries(fme_ps5 ${log-lib})

# FME consumes only chiaki-lib. Keep desktop/UI/update dependencies out of the
# Android protocol core while the session bridge is being brought up.
set(FME_CHIAKI_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../third_party/chiaki/upstream")
if(EXISTS "${FME_CHIAKI_DIR}/CMakeLists.txt")
  # These names/types match chiaki-ng v1.10.0's top-level CMake options.
  set(CHIAKI_ENABLE_ANDROID ON CACHE BOOL "" FORCE)
  set(CHIAKI_ENABLE_TESTS OFF CACHE BOOL "" FORCE)
  set(CHIAKI_ENABLE_CLI OFF CACHE BOOL "" FORCE)
  set(CHIAKI_ENABLE_GUI OFF CACHE BOOL "" FORCE)
  set(CHIAKI_ENABLE_BOREALIS OFF CACHE BOOL "" FORCE)
  set(CHIAKI_ENABLE_SETSU OFF CACHE STRING "" FORCE)
  set(CHIAKI_ENABLE_STEAMDECK_NATIVE OFF CACHE STRING "" FORCE)
  set(CHIAKI_LIB_ENABLE_OPUS OFF CACHE BOOL "" FORCE)
  set(CHIAKI_USE_SYSTEM_JERASURE OFF CACHE STRING "" FORCE)
  set(CHIAKI_USE_SYSTEM_NANOPB OFF CACHE STRING "" FORCE)
  set(CHIAKI_USE_SYSTEM_CURL OFF CACHE STRING "" FORCE)
  set(CHIAKI_LIB_JSONC_EXTERNAL_PROJECT OFF CACHE BOOL "" FORCE)
  set(CHIAKI_LIB_MINIUPNPC_EXTERNAL_PROJECT OFF CACHE BOOL "" FORCE)

  set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,-z,max-page-size=16384 -Wl,-z,common-page-size=16384")

  # Do not add the upstream top-level project here: v1.10.0's top-level graph
  # brings bundled curl/desktop dependencies that are not required for the
  # local-console chiaki-lib session core. Build the protocol library directly.
  add_subdirectory("${FME_CHIAKI_DIR}/lib" "${CMAKE_BINARY_DIR}/chiaki-lib" EXCLUDE_FROM_ALL)

  if(NOT TARGET chiaki-lib)
    message(FATAL_ERROR "FME ERROR: chiaki-ng v1.10.0 lib did not expose chiaki-lib")
  endif()
  if(TARGET libcurl OR TARGET curl OR TARGET CURL::libcurl)
    message(FATAL_ERROR "FME ERROR: curl entered the Android chiaki-lib core build")
  endif()

  target_link_libraries(fme_ps5 PRIVATE chiaki-lib)
  target_include_directories(fme_ps5 PRIVATE "${FME_CHIAKI_DIR}/lib/include")
  target_compile_definitions(fme_ps5 PRIVATE FME_HAS_CHIAKI=1)
endif()
