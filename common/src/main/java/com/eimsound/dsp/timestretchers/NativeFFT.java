package com.eimsound.dsp.timestretchers;

import org.jetbrains.annotations.NotNull;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

@SuppressWarnings("unused")
public final class NativeFFT implements AutoCloseable {
    private boolean isClosed = false;
    private final MemorySegment pointer;

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
                lib.find("fft_init").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT
                )
        );
        fft_destroy = linker.downcallHandle(
                lib.find("fft_destroy").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS
                )
        );
        fft_forward = linker.downcallHandle(
                lib.find("fft_forward").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_forward_interleaved = linker.downcallHandle(
                lib.find("fft_forward_interleaved").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_forward_polar = linker.downcallHandle(
                lib.find("fft_forward_polar").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_forward_magnitude = linker.downcallHandle(
                lib.find("fft_forward_magnitude").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_inverse = linker.downcallHandle(
                lib.find("fft_inverse").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_inverse_interleaved = linker.downcallHandle(
                lib.find("fft_inverse_interleaved").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_inverse_polar = linker.downcallHandle(
                lib.find("fft_inverse_polar").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_inverse_cepstral = linker.downcallHandle(
                lib.find("fft_inverse_cepstral").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        fft_get_size = linker.downcallHandle(
                lib.find("fft_get_size").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS
                )
        );
        fft_get_default_implementation = linker.downcallHandle(
                lib.find("fft_get_default_implementation").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS
                )
        );
    }

    public NativeFFT(int fftSize) {
        init();
        try {
            pointer = (MemorySegment) fft_init.invokeExact(fftSize);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void forward(@NotNull MemorySegment realIn, @NotNull MemorySegment realOut, @NotNull MemorySegment imagOut) {
        try {
            fft_forward.invokeExact(pointer, realIn, realOut, imagOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void forwardInterleaved(@NotNull MemorySegment realIn, @NotNull MemorySegment complexOut) {
        try {
            fft_forward_interleaved.invokeExact(pointer, realIn, complexOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void forwardPolar(@NotNull MemorySegment realIn, @NotNull MemorySegment magOut, @NotNull MemorySegment phaseOut) {
        try {
            fft_forward_polar.invokeExact(pointer, realIn, magOut, phaseOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void forwardMagnitude(@NotNull MemorySegment realIn, @NotNull MemorySegment magOut) {
        try {
            fft_forward_magnitude.invokeExact(pointer, realIn, magOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void inverse(@NotNull MemorySegment realIn, @NotNull MemorySegment imagIn, @NotNull MemorySegment realOut) {
        try {
            fft_inverse.invokeExact(pointer, realIn, imagIn, realOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void inverseInterleaved(@NotNull MemorySegment complexIn, @NotNull MemorySegment realOut) {
        try {
            fft_inverse_interleaved.invokeExact(pointer, complexIn, realOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void inversePolar(@NotNull MemorySegment magIn, @NotNull MemorySegment phaseIn, @NotNull MemorySegment realOut) {
        try {
            fft_inverse_polar.invokeExact(pointer, magIn, phaseIn, realOut);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void inverseCepstral(@NotNull MemorySegment magIn, @NotNull MemorySegment cepOut) {
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
            return ((MemorySegment) fft_get_default_implementation.invokeExact())
                    .reinterpret(255).getUtf8String(0L);
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