#include "time_stretcher.h"

using namespace eim;

time_stretcher::time_stretcher() noexcept = default;
time_stretcher::~time_stretcher() noexcept = default;

[[maybe_unused]] void time_stretcher::initialise(float, int, int, bool) noexcept {
    isInitialised = true;
}

int time_stretcher::process(float const*, float*, int) noexcept {
    return 0;
}

void time_stretcher::reset() noexcept {
}

int time_stretcher::flush(float*) noexcept {
    return 0;
}

void time_stretcher::setSpeedRatio(float) noexcept {
}

void time_stretcher::setSemitones(float) noexcept {
}

int time_stretcher::getMaxFramesNeeded() noexcept {
    return 0;
}

int time_stretcher::getFramesNeeded() noexcept {
    return 0;
}

int time_stretcher::isInitialized() const noexcept {
    return isInitialised;
}

bool time_stretcher::isPlanar() const noexcept {
    return true;
}
