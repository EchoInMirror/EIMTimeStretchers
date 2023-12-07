#ifdef _WIN32
#  define EXPORT [[maybe_unused]] __declspec(dllexport)
#else
#  define EXPORT [[maybe_unused]] __attribute__((visibility("default")))
#endif

#include <cstring>
#include "sound_touch_time_stretcher.h"
#include "rubberband_time_stretcher.h"
#include "../rubberband/src/common/FFT.h"
#include "../rubberband/src/common/Resampler.h"

extern "C" {
// ---------------- Time stretchers ----------------
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

// ---------------- FFT ----------------
EXPORT RubberBand::FFT *fft_init(int fftSize) {
    auto fft = new RubberBand::FFT(fftSize);
    fft->initFloat();
    return fft;
}

EXPORT void fft_destroy(RubberBand::FFT *fft) {
    delete fft;
}

EXPORT void fft_forward(RubberBand::FFT *fft, const float *realIn, float *realOut, float *imagOut) {
    fft->forward(realIn, realOut, imagOut);
}

EXPORT void fft_forward_interleaved(RubberBand::FFT *fft, const float *realIn, float *complexOut) {
    fft->forwardInterleaved(realIn, complexOut);
}

EXPORT void fft_forward_polar(RubberBand::FFT *fft, const float *realIn, float *magOut, float *phaseOut) {
    fft->forwardPolar(realIn, magOut, phaseOut);
}

EXPORT void fft_forward_magnitude(RubberBand::FFT *fft, const float *realIn, float *magOut) {
    fft->forwardMagnitude(realIn, magOut);
}

EXPORT void fft_inverse(RubberBand::FFT *fft, const float *realIn, const float *imagIn, float *realOut) {
    fft->inverse(realIn, imagIn, realOut);
}

EXPORT void fft_inverse_interleaved(RubberBand::FFT *fft, const float *complexIn, float *realOut) {
    fft->inverseInterleaved(complexIn, realOut);
}

EXPORT void fft_inverse_polar(RubberBand::FFT *fft, const float *magIn, const float *phaseIn, float *realOut) {
    fft->inversePolar(magIn, phaseIn, realOut);
}

EXPORT void fft_inverse_cepstral(RubberBand::FFT *fft, const float *magIn, float *cepOut) {
    fft->inverseCepstral(magIn, cepOut);
}

EXPORT int fft_get_size(RubberBand::FFT *fft) {
    return fft->getSize();
}

char fftDefaultImplementation[256] = {0};
EXPORT const char* fft_get_default_implementation() {
    if (fftDefaultImplementation[0] == 0) {
        strcpy(fftDefaultImplementation, RubberBand::FFT::getDefaultImplementation().c_str());
    }
    return fftDefaultImplementation;
}

// ---------------- Resampler ----------------
EXPORT RubberBand::Resampler *resampler_init(
        int channels, double initialSampleRate,
        int quality, bool isRatioOftenChanging, bool isSuddenRatioChange
) {
    RubberBand::Resampler::Parameters parameters;
    parameters.quality = RubberBand::Resampler::Quality(quality);
    parameters.dynamism = isRatioOftenChanging ? RubberBand::Resampler::RatioOftenChanging
                                               : RubberBand::Resampler::RatioMostlyFixed;
    parameters.ratioChange = isSuddenRatioChange ? RubberBand::Resampler::SuddenRatioChange
                                                 : RubberBand::Resampler::SmoothRatioChange;
    parameters.initialSampleRate = initialSampleRate;
    parameters.maxBufferSize = 0;
    return new RubberBand::Resampler(parameters, channels);
}

EXPORT void resampler_destroy(RubberBand::Resampler *resampler) {
    delete resampler;
}

EXPORT int resampler_resample(
        RubberBand::Resampler *resampler,
        float *const *const out, int outspace,
        const float *const *const in, int incount,
        double ratio, bool final
) {
    return resampler->resample(out, outspace, in, incount, ratio, final);
}

EXPORT int resampler_resample_interleaved(
        RubberBand::Resampler *resampler,
        float *const out, int outspace,
        const float *const in, int incount,
        double ratio, bool final
) {
    return resampler->resampleInterleaved(out, outspace, in, incount, ratio, final);
}

EXPORT int resampler_get_channel_count(RubberBand::Resampler *resampler) {
    return resampler->getChannelCount();
}

EXPORT double resampler_get_effective_ratio(RubberBand::Resampler *resampler, double ratio) {
    return resampler->getEffectiveRatio(ratio);
}

EXPORT void resampler_reset(RubberBand::Resampler *resampler) {
    resampler->reset();
}
}
