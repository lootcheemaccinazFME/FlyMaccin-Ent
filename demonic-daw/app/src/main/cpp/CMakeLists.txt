cmake_minimum_required(VERSION 3.22.1)
project(demonic_native_audio LANGUAGES C CXX)

find_package(oboe REQUIRED CONFIG)
find_package(fluidsynth REQUIRED CONFIG)

add_library(demonic_audio SHARED native_audio.cpp)
target_compile_features(demonic_audio PRIVATE cxx_std_17)
target_link_libraries(demonic_audio oboe::oboe fluidsynth::fluidsynth android log)
