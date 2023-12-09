package com.eimsound.dsp.timestretchers;

import org.jetbrains.annotations.NotNull;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

@SuppressWarnings("unused")
public final class NativeTimeStretcher implements AutoCloseable {
    private final MemorySession session = MemorySession.openShared();
    private final Addressable pointer;
    private Addressable inputBuffersPtr, outputBuffersPtr;
    private FloatBuffer inputBuffers, outputBuffers;
    private int inputBufferSize, numChannels, samplesPerBlock;
    private boolean isPlanar, isClosed, isInitialised, isRealtime;
    private final String name;
    private float speedRatio, semitones, sourceSampleRate;


    private static MethodHandle get_all_time_stretchers; // () -> char*
    private static MethodHandle create_time_stretcher; // (char*) -> void*
    private static MethodHandle destroy_time_stretcher; // (void*) -> void
    private static MethodHandle time_stretcher_process; // (void*, float**, float**, int) -> int
    private static MethodHandle time_stretcher_reset; // (void*) -> void
    private static MethodHandle time_stretcher_flush; // (void*, float**) -> int
    private static MethodHandle time_stretcher_set_speed_ratio; // (void*, float) -> void
    private static MethodHandle time_stretcher_set_semitones; // (void*, float) -> void
    private static MethodHandle time_stretcher_get_max_frames_needed; // (void*) -> int
    private static MethodHandle time_stretcher_get_frames_needed; // (void*) -> int
    private static MethodHandle time_stretcher_is_initialized; // (void*) -> bool
    private static MethodHandle time_stretcher_initialise; // (void*, float, int, int, bool) -> void
    private static MethodHandle time_stretcher_is_planar; // (void*) -> bool

    private static void init() {
        if (get_all_time_stretchers != null) return;
        var lib = NativeLibrary.getLookup();
        var linker = Linker.nativeLinker();
        get_all_time_stretchers = linker.downcallHandle(
                lib.lookup("get_all_time_stretchers").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS
                )
        );
        create_time_stretcher = linker.downcallHandle(
                lib.lookup("create_time_stretcher").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        destroy_time_stretcher = linker.downcallHandle(
                lib.lookup("destroy_time_stretcher").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS
                )
        );
        time_stretcher_process = linker.downcallHandle(
                lib.lookup("time_stretcher_process").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT
                )
        );
        time_stretcher_reset = linker.downcallHandle(
                lib.lookup("time_stretcher_reset").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS
                )
        );
        time_stretcher_flush = linker.downcallHandle(
                lib.lookup("time_stretcher_flush").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS
                )
        );
        time_stretcher_set_speed_ratio = linker.downcallHandle(
                lib.lookup("time_stretcher_set_speed_ratio").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_FLOAT
                )
        );
        time_stretcher_set_semitones = linker.downcallHandle(
                lib.lookup("time_stretcher_set_semitones").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_FLOAT
                )
        );
        time_stretcher_get_max_frames_needed = linker.downcallHandle(
                lib.lookup("time_stretcher_get_max_frames_needed").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS
                )
        );
        time_stretcher_get_frames_needed = linker.downcallHandle(
                lib.lookup("time_stretcher_get_frames_needed").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS
                )
        );
        time_stretcher_is_initialized = linker.downcallHandle(
                lib.lookup("time_stretcher_is_initialized").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_BOOLEAN,
                        ValueLayout.ADDRESS
                )
        );
        time_stretcher_initialise = linker.downcallHandle(
                lib.lookup("time_stretcher_initialise").orElseThrow(),
                FunctionDescriptor.ofVoid(
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_FLOAT,
                        ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_INT,
                        ValueLayout.JAVA_BOOLEAN
                )
        );
        time_stretcher_is_planar = linker.downcallHandle(
                lib.lookup("time_stretcher_is_planar").orElseThrow(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_BOOLEAN,
                        ValueLayout.ADDRESS
                )
        );
    }

    @NotNull
    public static String @NotNull [] getAllTimeStretcherNames() {
        init();
        try {
            return ((MemoryAddress) get_all_time_stretchers.invokeExact()).getUtf8String(0L).split(",");
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public NativeTimeStretcher(@NotNull String name) { this(name, 1F, 0F); }
    public NativeTimeStretcher(@NotNull String name, float speedRatio, float semitones) {
        init();
        try {
            pointer = (MemoryAddress) create_time_stretcher.invokeExact((Addressable) session.allocateUtf8String(name));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        this.name = name;
        this.speedRatio = speedRatio;
        this.semitones = semitones;
    }

    public int getFramesNeeded() {
        if (isClosed || !isInitialised) return 0;
        try {
            return (int) time_stretcher_get_frames_needed.invokeExact(pointer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public int getMaxFramesNeeded() {
        if (isClosed || !isInitialised) return 0;
        try {
            return (int) time_stretcher_get_max_frames_needed.invokeExact(pointer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isInitialised() {
        if (isClosed) return false;
        try {
            return (boolean) time_stretcher_is_initialized.invokeExact(pointer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPlanar() {
        if (isClosed || !isInitialised) return false;
        try {
            return (boolean) time_stretcher_is_planar.invokeExact(pointer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public int getInputBufferSize() {
        return inputBufferSize;
    }

    public int getNumChannels() {
        return numChannels;
    }

    public int getSamplesPerBlock() {
        return samplesPerBlock;
    }

    public boolean isRealtime() {
        return isRealtime;
    }

    public float getSourceSampleRate() {
        return sourceSampleRate;
    }

    public float getSpeedRatio() {
        return speedRatio;
    }

    public float getSemitones() {
        return semitones;
    }

    @NotNull
    public String getName() { return name; }

    public void setSpeedRatio(float speedRatio) {
        if (isClosed || speedRatio == this.speedRatio) return;
        try {
            time_stretcher_set_speed_ratio.invokeExact(pointer, speedRatio);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        this.speedRatio = speedRatio;
    }

    public void setSemitones(float semitones) {
        if (isClosed || semitones == this.semitones) return;
        try {
            time_stretcher_set_semitones.invokeExact(pointer, semitones);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        this.semitones = semitones;
    }

    public void reset() {
        if (isClosed || !isInitialised) return;
        try {
            time_stretcher_reset.invokeExact(pointer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public void initialise(float sourceSampleRate, int samplesPerBlock, int numChannels, boolean isRealtime) {
        this.numChannels = numChannels;
        this.samplesPerBlock = samplesPerBlock;
        this.sourceSampleRate = sourceSampleRate;
        this.isRealtime = isRealtime;
        isInitialised = true;
        try {
            time_stretcher_initialise.invokeExact(pointer, sourceSampleRate, samplesPerBlock, numChannels, isRealtime);
            var ptr = session.allocateArray(ValueLayout.JAVA_FLOAT, (long) samplesPerBlock * numChannels);
            outputBuffers = ptr.asByteBuffer().order(ByteOrder.nativeOrder()).asFloatBuffer();
            outputBuffersPtr = ptr;
            isPlanar = (boolean) time_stretcher_is_planar.invokeExact(pointer);
            if (speedRatio != 1F) time_stretcher_set_speed_ratio.invokeExact(pointer, speedRatio);
            if (semitones != 0F) time_stretcher_set_semitones.invokeExact(pointer, semitones);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public int process(float[] @NotNull [] input, float[] @NotNull [] output, int numSamples) {
        if (isClosed || !isInitialised) return 0;
        if (inputBufferSize < numSamples * numChannels) {
            inputBufferSize = Math.max(numSamples, getMaxFramesNeeded()) * numChannels;
            var ptr = session.allocateArray(ValueLayout.JAVA_FLOAT, inputBufferSize);
            inputBuffers = ptr.asByteBuffer().order(ByteOrder.nativeOrder()).asFloatBuffer();
            inputBuffersPtr = ptr;
        }
        inputBuffers.rewind();
        if (isPlanar) for (int i = 0; i < numChannels; i++) inputBuffers.put(input[i], 0, numSamples);
        else for (int i = 0; i < numSamples; i++) for (int j = 0; j < numChannels; j++) inputBuffers.put(input[j][i]);
        try {
            var ret = (int) time_stretcher_process.invokeExact(pointer, inputBuffersPtr, outputBuffersPtr, numSamples);
            readOutput(output, ret);
            return ret;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        if (isClosed) return;
        isClosed = true;
        try {
            destroy_time_stretcher.invokeExact(pointer);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            session.close();
        }
    }

    public int flush(float[] @NotNull [] output) {
        if (isClosed || !isInitialised) return 0;
        try {
            var ret = (int) time_stretcher_flush.invokeExact(pointer, outputBuffersPtr);
            readOutput(output, ret);
            return ret;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    public NativeTimeStretcher copy() {
        var ret = new NativeTimeStretcher(name, speedRatio, semitones);
        if (isInitialised) ret.initialise(sourceSampleRate, samplesPerBlock, numChannels, isRealtime);
        return ret;
    }

    private void readOutput(float[][] output, int numSamples) {
        outputBuffers.rewind();
        if (isPlanar) for (int i = 0; i < numChannels; i++) outputBuffers.get(output[i], 0, numSamples);
        else for (int i = 0; i < numSamples; i++) for (int j = 0; j < numChannels; j++) output[j][i] = outputBuffers.get();
    }
}