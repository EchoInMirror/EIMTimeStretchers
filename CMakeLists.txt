cmake_minimum_required(VERSION 3.24)
project(EIMTimeStretchers)

set(CMAKE_CXX_STANDARD 23)

add_definitions(-DNO_TIMING)

set(BUILD_TESTING OFF CACHE BOOL "" FORCE)
set(BUILD_SHARED_LIBS OFF CACHE BOOL "" FORCE)
set(SOUNDSTRETCH OFF CACHE BOOL "" FORCE)

if (WIN32)
    add_definitions(-D_WIN32 -DNOMINMAX -D_USE_MATH_DEFINES -DHAVE_SLEEF -DHAVE_LIBSAMPLERATE -DGETOPT_API=0 -DSLEEF_STATIC_LIBS)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} /MD")
elseif (LINUX)
    set(CMAKE_POSITION_INDEPENDENT_CODE ON CACHE BOOL "" FORCE)
    add_definitions(-DUSE_PTHREADS -DHAVE_LIBSAMPLERATE -DHAVE_SLEEF -DHAVE_POSIX_MEMALIGN -DSLEEF_STATIC_LIBS)
elseif (APPLE)
    add_definitions(-DHAVE_LIBSAMPLERATE -DHAVE_VDSP -DNO_THREAD_CHECKS -DUSE_PTHREADS -DMALLOC_IS_ALIGNED)
endif ()

if (WIN32 OR LINUX)
    set(sleef_SOURCE_DIR ${CMAKE_SOURCE_DIR}/sleef)
    set(sleef_BINARY_DIR ${CMAKE_BINARY_DIR}/sleef)
    set(BUILD_DFT ON CACHE BOOL "" FORCE)
    set(BUILD_TESTS OFF CACHE BOOL "" FORCE)
    set(ENABLE_LTO ON CACHE BOOL "" FORCE)
    add_subdirectory("sleef")
    include_directories(${sleef_BINARY_DIR}/include)
    include_directories(${sleef_SOURCE_DIR}/include)
    link_directories(${sleef_BINARY_DIR}/lib)
    set(CMAKE_PROJECT_VERSION 2.3.2)
endif ()

# check is Release build or MinSizeRel build
if (CMAKE_BUILD_TYPE MATCHES Release OR CMAKE_BUILD_TYPE MATCHES MinSizeRel)
    add_definitions(-DNDEBUG -DNO_THREAD_CHECKS)
    
    if (NOT MSVC)
        SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O3 -ffast-math -ftree-vectorize")
    endif()
endif()

add_subdirectory(libsamplerate)
add_subdirectory(soundtouch)
include_directories(stubs)

file(GLOB_RECURSE RUBBERBAND_SRC_FILES
        rubberband/src/common/*.cpp rubberband/src/common/*.h
        rubberband/src/finer/*.cpp rubberband/src/finer/*.h
        rubberband/src/faster/*.cpp rubberband/src/faster/*.h
        rubberband/src/ext/*.cpp rubberband/src/ext/*.h
        rubberband/rubberband/RubberBandStretcher.h rubberband/src/RubberBandStretcher.cpp
)
add_library(rubberband STATIC ${RUBBERBAND_SRC_FILES})
target_link_libraries(rubberband PRIVATE samplerate)

file(GLOB_RECURSE SRC_FILES lib/*.cpp lib/*.h)
add_library(EIMTimeStretchers SHARED ${SRC_FILES})
#add_executable(EIMTimeStretchers ${SRC_FILES})

if (WIN32)
    add_dependencies(rubberband sleef)
    target_link_libraries(rubberband PRIVATE sleef sleefdft)
    set_target_properties(EIMTimeStretchers PROPERTIES COMPILE_FLAGS "$<$<CONFIG:Release>:/GL>")
    set_target_properties(EIMTimeStretchers PROPERTIES LINK_FLAGS "$<$<CONFIG:Release>:/LTCG>")
    target_link_libraries(EIMTimeStretchers PRIVATE SoundTouch rubberband)
elseif (APPLE)
    target_link_libraries(EIMTimeStretchers PRIVATE "-framework Accelerate" SoundTouch pthread rubberband)
elseif (LINUX)
    add_dependencies(rubberband sleef)
    target_link_libraries(EIMTimeStretchers PRIVATE SoundTouch rubberband)
endif ()
