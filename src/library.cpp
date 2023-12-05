#ifdef _WIN32
#  define EXPORT [[maybe_unused]] __declspec(dllexport)
#else
#  define EXPORT [[maybe_unused]] __attribute__((visibility("default")))
#endif

#include <cstring>
#include "sound_touch_time_stretcher.h"
#include "rubberband_time_stretcher.h"

extern "C" {
EXPORT const char *get_all_time_stretchers() {
    return "SoundTouch,SoundTouch (better quality),RubberBand";
}

EXPORT eim::time_stretcher *create_time_stretcher(const char *name) {
    if (strcmp(name, "SoundTouch") == 0) {
        return new eim::sound_touch_time_stretcher();
    } else if (strcmp(name, "SoundTouch (better quality)") == 0) {
        return new eim::sound_touch_time_stretcher(true);
    } else if (strcmp(name, "RubberBand") == 0) {
        return new eim::rubberband_time_stretcher();
    }
    return nullptr;
}

EXPORT void destroy_time_stretcher(eim::time_stretcher *stretcher) {
    delete stretcher;
}

EXPORT int time_stretcher_process(
        eim::time_stretcher *stretcher, float const *in, float *out, int numSamples
) {
    return stretcher->process(in, out, numSamples);
}

EXPORT void time_stretcher_reset(eim::time_stretcher *stretcher) {
    stretcher->reset();
}

EXPORT int time_stretcher_flush(eim::time_stretcher *stretcher, float *out) {
    return stretcher->flush(out);
}

EXPORT void time_stretcher_set_speed_ratio(eim::time_stretcher *stretcher, float newSpeed) {
    stretcher->setSpeedRatio(newSpeed);
}

EXPORT void time_stretcher_set_semitones(eim::time_stretcher *stretcher, float semitones) {
    stretcher->setSemitones(semitones);
}

EXPORT int time_stretcher_get_max_frames_needed(eim::time_stretcher *stretcher) {
    return stretcher->getMaxFramesNeeded();
}

EXPORT int time_stretcher_get_frames_needed(eim::time_stretcher *stretcher) {
    return stretcher->getFramesNeeded();
}

EXPORT int time_stretcher_is_initialized(eim::time_stretcher *stretcher) {
    return stretcher->isInitialized();
}

EXPORT void time_stretcher_initialise(
        eim::time_stretcher *stretcher, float sourceSampleRate, int blockSize, int channels, bool isRealtime
) {
    stretcher->initialise(sourceSampleRate, blockSize, channels, isRealtime);
}

EXPORT bool time_stretcher_is_planar(eim::time_stretcher *stretcher) {
    return stretcher->isPlanar();
}
}