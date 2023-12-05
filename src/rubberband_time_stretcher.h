#include "time_stretcher.h"
#include "../rubberband/rubberband/RubberBandStretcher.h"

namespace eim {
    class rubberband_time_stretcher : public time_stretcher {
    public:
        explicit rubberband_time_stretcher() noexcept = default;

        ~rubberband_time_stretcher() { destroy(); }

        void initialise(float sourceSampleRate, int blockSize, int channels, bool isRealtime) noexcept override {
            destroy();
            stretcher = new RubberBand::RubberBandStretcher((unsigned int) sourceSampleRate, channels, getOptionFlags(isRealtime));
            samplesPerBlock = blockSize;
            numChannels = channels;
            inputArray = new const float*[channels];
            outputArray = new float*[channels];
            isInitialised = true;
        }

        int process(float const* in, float* out, int numSamples) noexcept override {
            if (!isInitialised) return 0;
            for (int i = 0; i < numChannels; i++) { inputArray[i] = &in[i * numSamples]; }
            stretcher->process(inputArray, (size_t) numSamples, false);
            int numAvailable = stretcher->available();
            if (numSamplesToDrop > 0) {
                auto numToDropThisTime = std::min(numSamplesToDrop, std::min(numAvailable, samplesPerBlock));
                for (int i = 0; i < numChannels; i++) { outputArray[i] = &out[i * numToDropThisTime]; }
                stretcher->retrieve(outputArray, (size_t) numToDropThisTime);
                numSamplesToDrop -= numToDropThisTime;

                numAvailable -= numToDropThisTime;
            }

            if (numAvailable <= 0) return 0;
            for (int i = 0; i < numChannels; i++) { outputArray[i] = &out[i * numAvailable]; }
            return (int) stretcher->retrieve (outputArray, (size_t) numAvailable);
        }

        void reset() noexcept override {
            if (!isInitialised) return;
            stretcher->reset();
        }

        int flush(float* out) noexcept override {
            if (!isInitialised) return 0;
            auto numAvailable = stretcher->available();
            auto numThisBlock = std::min(numAvailable, samplesPerBlock);

            if (numThisBlock <= 0) return 0;
            for (int i = 0; i < numChannels; i++) { outputArray[i] = &out[i * numAvailable]; }
            return (int) stretcher->retrieve(outputArray, (size_t) numThisBlock);
        }

        void setSpeedRatio(float newSpeed) noexcept override {
            if (!isInitialised) return;
            stretcher->setTimeRatio(newSpeed);
            dropSamples();
        }

        void setSemitones(float semitones) noexcept override {
            stretcher->setPitchScale(semitonesToRatio(semitones));
            dropSamples();
        }

        [[nodiscard]] int getMaxFramesNeeded() noexcept override {
            return 8192;
        }

        [[nodiscard]] int getFramesNeeded() noexcept override {
            return (int) stretcher->getSamplesRequired();
        }

        [[nodiscard]] bool isPlanar() const noexcept override {
            return true;
        }
    private:
        RubberBand::RubberBandStretcher* stretcher = nullptr;
        int samplesPerBlock = 0, numChannels = 0, numSamplesToDrop = -1;
        const float** inputArray = nullptr;
        float** outputArray = nullptr;

        static int getOptionFlags(bool percussive) {
            if (percussive)
                return RubberBand::RubberBandStretcher::OptionProcessRealTime
                       | RubberBand::RubberBandStretcher::OptionPitchHighConsistency
                       | RubberBand::RubberBandStretcher::PercussiveOptions;

            return RubberBand::RubberBandStretcher::OptionProcessRealTime
                   | RubberBand::RubberBandStretcher::OptionPitchHighConsistency
                   | RubberBand::RubberBandStretcher::OptionWindowShort;
        }

        void destroy() {
            if (stretcher) {
                delete stretcher;
                stretcher = nullptr;
            }
            if (inputArray) {
                delete[] inputArray;
                inputArray = nullptr;
            }
            if (outputArray) {
                delete[] outputArray;
                outputArray = nullptr;
            }
        }

        void dropSamples() {
            if (numSamplesToDrop != -1) return;
            numSamplesToDrop = (int) stretcher->getLatency();
            int numSamplesToPad = (int) std::round(numSamplesToDrop * stretcher->getPitchScale());

            if (numSamplesToPad <= 0) return;

            auto chs = stretcher->getChannelCount();
            auto** buffers = new float*[chs];
            for (int i = 0; i < chs; i++) { buffers[i] = new float[samplesPerBlock]{ 0 }; }

            while (numSamplesToPad > 0) {
                const int numThisTime = std::min(numSamplesToPad, samplesPerBlock);
                stretcher->process(buffers, numThisTime, false);
                numSamplesToPad -= numThisTime;
            }
        }
    };
}
