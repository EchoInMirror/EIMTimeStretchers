#include "time_stretcher.h"
#include <SoundTouch.h>
#include <iostream>

namespace eim {
    class sound_touch_time_stretcher : public time_stretcher {
    public:
        explicit sound_touch_time_stretcher(bool betterQuality = false) noexcept: _betterQuality(betterQuality) { }
        ~sound_touch_time_stretcher() = default;

        void initialise(float sourceSampleRate, int blockSize, int channels, bool) noexcept override {
            soundTouch.setSampleRate((unsigned int) sourceSampleRate);
            soundTouch.setChannels(channels);
            this->samplesPerBlock = blockSize;

            if (isInitialised) return;
            isInitialised = true;
            if (!_betterQuality) return;
            soundTouch.setSetting(SETTING_USE_QUICKSEEK, 0);
            soundTouch.setSetting(SETTING_USE_AA_FILTER, 1);
            soundTouch.setSetting(SETTING_SEQUENCE_MS, 60);
            soundTouch.setSetting(SETTING_SEEKWINDOW_MS, 25);
            soundTouch.setSetting(SETTING_OVERLAP_MS, 12);
            soundTouch.setSetting(SETTING_AA_FILTER_LENGTH, 64);
        }

        int process(float const* in, float* out, int numSamples) noexcept override {
            if (!isInitialised) return 0;

            soundTouch.putSamples(in, numSamples);
            return readSamples(out);
        }
        void reset() noexcept override {
            soundTouch.clear();
        }
        int flush(float* out) noexcept override {
            if (!isInitialised) return 0;
            soundTouch.flush();
            return readSamples(out);
        }
        void setSpeedRatio(float newSpeed) noexcept override {
            soundTouch.setTempo(1.0 / newSpeed);
        }
        void setSemitones(float semitones) noexcept override {
            soundTouch.setPitchSemiTones(semitones);
        }
        int getMaxFramesNeeded() noexcept override {
            return 8192;
        }
        int getFramesNeeded() noexcept override {
            auto numAvailable = (int)soundTouch.numSamples();
            auto numRequiredForOneBlock = (int) std::round(samplesPerBlock * soundTouch.getInputOutputSampleRatio());

            return std::max(0, numRequiredForOneBlock - numAvailable);
        }
        [[nodiscard]] bool isPlanar() const noexcept override {
            return false;
        }
    private:
        soundtouch::SoundTouch soundTouch;
        int samplesPerBlock = 0;
        bool _betterQuality;

        int readSamples(float* out) noexcept {
            auto toRead = (unsigned int)std::min(samplesPerBlock, (int) soundTouch.numSamples());
            auto gg = toRead ? (int) soundTouch.receiveSamples(out, toRead) : 0;
            return gg;;
        }
    };
}
