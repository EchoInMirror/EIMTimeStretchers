cmake_minimum_required(VERSION 3.24)
project(EIMTimeStretchers)

set(CMAKE_CXX_STANDARD 23)

if (WIN32)
    add_definitions(-D_WIN32 -DNOMINMAX -D_USE_MATH_DEFINES -DUSE_BUILTIN_FFT -DUSE_BQRESAMPLER -DGETOPT_API=0)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /MD")
elseif (APPLE)
    add_definitions(-DUSE_BQRESAMPLER -DHAVE_VDSP -DNO_THREAD_CHECKS -DUSE_PTHREADS -DNO_TIMING -DMALLOC_IS_ALIGNED)
endif ()

# check is Release build or MinSizeRel build
if (CMAKE_BUILD_TYPE MATCHES Release OR CMAKE_BUILD_TYPE MATCHES MinSizeRel)
    add_definitions(-DNDEBUG -NO_THREAD_CHECKS -NO_TIMING)
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -ffast-math -ftree-vectorize")
endif ()

add_subdirectory(soundtouch)
include_directories(stubs)

file(GLOB_RECURSE SRC_FILES src/*.cpp src/*.h)
file(GLOB_RECURSE RUBBERBAND_SRC_FILES
        rubberband/src/common/*.cpp rubberband/src/common/*.h
        rubberband/src/finer/*.cpp rubberband/src/finer/*.h
        rubberband/src/faster/*.cpp rubberband/src/faster/*.h
        rubberband/src/ext/*.cpp rubberband/src/ext/*.h
        rubberband/rubberband/RubberBandStretcher.h rubberband/src/RubberBandStretcher.cpp
)

add_library(EIMTimeStretchers SHARED ${SRC_FILES} ${RUBBERBAND_SRC_FILES})

if (WIN32)
    target_link_libraries(EIMTimeStretchers PRIVATE SoundTouch)
elseif (APPLE)
    target_link_libraries(EIMTimeStretchers PRIVATE "-framework Accelerate" SoundTouch pthread)
endif ()
