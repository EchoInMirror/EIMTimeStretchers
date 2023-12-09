package com.eimsound.dsp.timestretchers;

import org.jetbrains.annotations.NotNull;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

@SuppressWarnings("unused")
public final class NativeFFT implements AutoCloseable {
    private boolean isClosed = false;
    private final Addressable pointer;

    private static MethodHandle fft_init; // void* *fft_init(int fftSize)
    private static MethodHandle fft_destroy; // void fft_destroy(void* *fft)
    private static MethodHandle fft_forward; // void fft_forward(void* *fft, const float *realIn, float *realOut, float *imagOut)
    private static MethodHandle fft_forward_interleaved; // void fft_forward_interleaved(void* *fft, const float *realIn, float *complexOut)
    private static MethodHandle fft_forward_polar; // void fft_forward_polar(void* *fft, const float *realIn, float *magOut, float *phaseOut)
    private static MethodHandle fft_forward_magnitude; // void fft_forward_magnitude(void* *fft, const float *realIn, float *magOut)
    private static MethodHandle fft_inverse; // void fft_inverse(void* *fft, const float *realIn, const float *imagIn, float *realOut)
    private static MethodHandle fft_inverse_interleaved; // void fft_inverse_interleaved(void* *fft, const float *complexIn, float *realOut)
    private static MethodHandle fft_inverse_polar; // void fft_inverse_polar(void* *fft, const float *magIn, const float *phaseIn, float *realOut)
    private static MethodHandle fft_inverse_cepstral; // void fft_inverse_cepstral(void* *fft, const float *magIn, float *cepOut)
    private static MethodHandle fft_get_size; // int fft_get_size(void* *fft)
    private static MethodHandle fft_get_default_implementation; // const char* fft_get_default_implementation()

    private static void init() {
        if (fft_init != null) return;
        var lib = NativeLibrary.getLookup();
        var linker = Linker.nativeLinker();
        fft_init = linker.downcallHandle(
                lib.lookup("fft_init").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT
                )
        );
        fft_destroy = linker.downcallHandle(
                lib.lookup("fft_destroy").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS
                )
        );
        fft_forward = linker.downcallHandle(
                lib.lookup("fft_forward").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_forward_interleaved = linker.downcallHandle(
                lib.lookup("fft_forward_interleaved").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_forward_polar = linker.downcallHandle(
                lib.lookup("fft_forward_polar").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_forward_magnitude = linker.downcallHandle(
                lib.lookup("fft_forward_magnitude").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_inverse = linker.downcallHandle(
                lib.lookup("fft_inverse").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_inverse_interleaved = linker.downcallHandle(
                lib.lookup("fft_inverse_interleaved").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_inverse_polar = linker.downcallHandle(
                lib.lookup("fft_inverse_polar").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_inverse_cepstral = linker.downcallHandle(
                lib.lookup("fft_inverse_cepstral").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_get_size = linker.downcallHandle(
                lib.lookup("fft_get_size").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS
                )
        );
        fft_get_default_implementation = linker.downcallHandle(
                lib.lookup("fft_get_default_implementation").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS
                )
        );
    }

    public NativeFFT(int fftSize) {
        init();
        try {
            pointer = (MemoryAddress) fft_init.invokeExact(fftSize);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void forward(@NotNull Addressable realIn, @NotNull Addressable realOut, @NotNull Addressable imagOut) {
        try {
            fft_forward.invokeExact(pointer, realIn, realOut, imagOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void forwardInterleaved(@NotNull Addressable realIn, @NotNull Addressable complexOut) {
        try {
            fft_forward_interleaved.invokeExact(pointer, realIn, complexOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void forwardPolar(@NotNull Addressable realIn, @NotNull Addressable magOut, @NotNull Addressable phaseOut) {
        try {
            fft_forward_polar.invokeExact(pointer, realIn, magOut, phaseOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void forwardMagnitude(@NotNull Addressable realIn, @NotNull Addressable magOut) {
        try {
            fft_forward_magnitude.invokeExact(pointer, realIn, magOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void inverse(@NotNull Addressable realIn, @NotNull Addressable imagIn, @NotNull Addressable realOut) {
        try {
            fft_inverse.invokeExact(pointer, realIn, imagIn, realOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void inverseInterleaved(@NotNull Addressable complexIn, @NotNull Addressable realOut) {
        try {
            fft_inverse_interleaved.invokeExact(pointer, complexIn, realOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void inversePolar(@NotNull Addressable magIn, @NotNull Addressable phaseIn, @NotNull Addressable realOut) {
        try {
            fft_inverse_polar.invokeExact(pointer, magIn, phaseIn, realOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void inverseCepstral(@NotNull Addressable magIn, @NotNull Addressable cepOut) {
        try {
            fft_inverse_cepstral.invokeExact(pointer, magIn, cepOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public int getSize() {
        try {
            return (int) fft_get_size.invokeExact(pointer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public static String getDefaultImplementation() {
        init();
        try {
            return ((MemoryAddress) fft_get_default_implementation.invokeExact()).getUtf8String(0L);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (isClosed) return;
        isClosed = true;
        try {
            fft_destroy.invokeExact(pointer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}