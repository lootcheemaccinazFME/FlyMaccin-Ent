cmake_minimum_required(VERSION 3.22.1)
project(lootchee_frontend)

add_library(lootchee_frontend SHARED libretro_frontend.cpp)

find_library(log-lib log)
find_library(dl-lib dl)
find_library(egl-lib EGL)
find_library(gles-lib GLESv3)

target_link_libraries(
    lootchee_frontend
    ${log-lib}
    ${dl-lib}
    ${egl-lib}
    ${gles-lib}
)
