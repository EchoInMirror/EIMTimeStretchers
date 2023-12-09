package com.eimsound.dsp.timestretchers;

import org.jetbrains.annotations.NotNull;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

@SuppressWarnings("unused")
public final class NativeResampler implements AutoCloseable {
    private boolean isClosed = false;
    private final Addressable pointer;

    private static MethodHandle resampler_init; // void* *resampler_init(int channels, double initialSampleRate, int quality, bool isRatioOftenChanging, bool isSuddenRatioChange)
    private static MethodHandle resampler_destroy; // void resampler_destroy(void* *resampler)
    private static MethodHandle resampler_resample; // int resampler_resample(void* *resampler, float *const *const out, int outspace, const float *const *const in, int incount, double ratio, bool final)
    private static MethodHandle resampler_resample_interleaved; // int resampler_resample_interleaved(void* *resampler, float *const out, int outspace, const float *const in, int incount, double ratio, bool final)
    private static MethodHandle resampler_get_channel_count; // int resampler_get_channel_count(void* *resampler)
    private static MethodHandle resampler_get_effective_ratio; // double resampler_get_effective_ratio(void* *resampler, double ratio)
    private static MethodHandle resampler_reset; // void resampler_reset(void* *resampler)
    private static MethodHandle resampler_get_implementation; // const char* resampler_get_implementation()

    private static void init() {
        if (resampler_init != null) return;
        var lib = NativeLibrary.getLookup();
        var linker = Linker.nativeLinker();
        resampler_init = linker.downcallHandle(
                lib.lookup("resampler_init").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_DOUBLE,
                        ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_BOOLEAN,
                        ValueLayout.JAVA_BOOLEAN
                )
        );
        resampler_destroy = linker.downcallHandle(
                lib.lookup("resampler_destroy").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS
                )
        );
        resampler_resample = linker.downcallHandle(
                lib.lookup("resampler_resample").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_DOUBLE,
                        ValueLayout.JAVA_BOOLEAN
                )
        );
        resampler_resample_interleaved = linker.downcallHandle(
                lib.lookup("resampler_resample_interleaved").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_DOUBLE,
                        ValueLayout.JAVA_BOOLEAN
                )
        );
        resampler_get_channel_count = linker.downcallHandle(
                lib.lookup("resampler_get_channel_count").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS
                )
        );
        resampler_get_effective_ratio = linker.downcallHandle(
                lib.lookup("resampler_get_effective_ratio").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_DOUBLE,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_DOUBLE
                )
        );
        resampler_reset = linker.downcallHandle(
                lib.lookup("resampler_reset").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS
                )
        );
        resampler_get_implementation = linker.downcallHandle(
                lib.lookup("resampler_get_implementation").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
    }

    public NativeResampler(int channels, double initialSampleRate, int quality, boolean isRatioOftenChanging, boolean isSuddenRatioChange) {
        init();
        try {
            pointer = (Addressable) resampler_init.invokeExact(channels, initialSampleRate, quality, isRatioOftenChanging, isSuddenRatioChange);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (isClosed) return;
        isClosed = true;
        try {
            resampler_destroy.invokeExact(pointer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public int resample(@NotNull Addressable out, int outspace, @NotNull Addressable in, int incount, double ratio, boolean final_) {
        try {
            return (int) resampler_resample.invokeExact(pointer, out, outspace, in, incount, ratio, final_);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public int interleaved(@NotNull Addressable out, int outspace, @NotNull Addressable in, int incount, double ratio, boolean final_) {
        try {
            return (int) resampler_resample_interleaved.invokeExact(pointer, out, outspace, in, incount, ratio, final_);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public int getChannelCount() {
        try {
            return (int) resampler_get_channel_count.invokeExact(pointer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public double getEffectiveRatio(double ratio) {
        try {
            return (double) resampler_get_effective_ratio.invokeExact(pointer, ratio);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        try {
            resampler_reset.invokeExact(pointer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static String getImplementation() {
        init();
        try {
            return ((MemoryAddress) resampler_get_implementation.invokeExact()).getUtf8String(0L);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
