cmake_minimum_required(VERSION 3.18.1)
project(tribetalk_native LANGUAGES CXX C)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Compiler optimization flags
if(ANDROID)
    add_definitions(-DANDROID)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -ffast-math -flto")
    if(${ANDROID_ABI} STREQUAL "arm64-v8a")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -march=armv8-a+simd")
    endif()
else()
    if(MSVC)
        add_compile_options(/O2 /fp:fast /W3)
    else()
        add_compile_options(-O3 -ffast-math -Wall)
    endif()
endif()

# Include directories
if(NOT ANDROID)
    find_package(JNI QUIET)
    if(JNI_FOUND)
        include_directories(${JNI_INCLUDE_DIRS})
    else()
        if(EXISTS "C:/Program Files/Java/jdk-24/include")
            include_directories(
                "C:/Program Files/Java/jdk-24/include"
                "C:/Program Files/Java/jdk-24/include/win32"
            )
        endif()
    endif()
endif()

include_directories(
    ${CMAKE_CURRENT_SOURCE_DIR}/include
    ${CMAKE_CURRENT_SOURCE_DIR}/jni
)

# Core native sources
set(TRIBETALK_SOURCES
    src/audio/audio_buffer.cpp
    src/audio/audio_features.cpp
    src/asr/asr_engine.cpp
    src/translation/translation_engine.cpp
    src/tts/tts_engine.cpp
    src/resource/resource_manager.cpp
    src/slm/qwen_engine.cpp
    src/pipeline/pipeline.cpp
    jni/tribetalk_jni.cpp
)

# Shared library target for Android / Desktop
add_library(tribetalk_native SHARED
    ${TRIBETALK_SOURCES}
)

# Target dependencies
if(ANDROID)
    find_library(log-lib log)
    find_library(android-lib android)
    target_link_libraries(tribetalk_native PRIVATE
        ${log-lib}
        ${android-lib}
    )
endif()

# Output configuration
set_target_properties(tribetalk_native PROPERTIES
    OUTPUT_NAME "tribetalk_native"
    PREFIX "lib"
)
