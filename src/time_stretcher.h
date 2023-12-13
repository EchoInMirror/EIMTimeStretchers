#ifndef EIM_TIME_STRETCHER_H
#define EIM_TIME_STRETCHER_H

#include <cmath>

namespace eim {
    inline static float semitonesToRatio(float semitones) noexcept {
        if (semitones == 0.0f) return 1.0f;

        const auto result = (float) std::pow(1.0594630943592953, semitones);
        return result > 4 ? 4.0f : result < 0.25f ? 0.25f : result;
    }

    class time_stretcher {
    public:
        time_stretcher() noexcept;
        ~time_stretcher() noexcept;

        [[maybe_unused]] virtual void initialise(float sourceSampleRate, int samplesPerBlock, int numChannels, bool isRealtime) noexcept;
        virtual int process(float const* in, float* out, int numSamples) noexcept;
        virtual void reset() noexcept;
        virtual int flush(float* out) noexcept;
        virtual void setSpeedRatio(float newSpeed) noexcept;
        virtual void setSemitones(float semitones) noexcept;
        [[nodiscard]] virtual int getMaxFramesNeeded() noexcept;
        [[nodiscard]] virtual int getFramesNeeded() noexcept;
        [[nodiscard]] int isInitialized() const noexcept;
        [[nodiscard]] virtual bool isPlanar() const noexcept;
    protected:
        bool isInitialised = false;
    };
}

#endif
