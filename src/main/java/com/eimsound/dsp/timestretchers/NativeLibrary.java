package com.eimsound.dsp.timestretchers;

import java.lang.foreign.Arena;
import java.lang.foreign.SymbolLookup;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

final class NativeLibrary {
    private static final String fileName;
    private static SymbolLookup lookup;

    static {
        var os = System.getProperty("os.name");
        var ext = "";
        if (os.startsWith("Windows")) ext = ".dll";
        else if ("Mac OS X".equalsIgnoreCase(os)) {
            ext = System.getProperty("os.arch", "").equals("aarch64") ? ".dylib" : "-x86.dylib";
        } else ext = ".so";
        fileName = "libEIMTimeStretchers" + ext;
    }

    static SymbolLookup getLookup() {
        if (lookup == null) lookup = SymbolLookup.libraryLookup(getLibraryPath(), Arena.global());
        return lookup;
    }

    private static Path getLibraryPath() {
        try (var hashStream = NativeLibrary.class.getClassLoader()
                .getResourceAsStream("eim-time-stretchers/" + fileName + ".sha256")) {
            if (hashStream == null) throw new RuntimeException("Could not find hash file");
            var hash = new String(hashStream.readAllBytes());
            var path = Path.of(System.getProperty("user.home"), ".eim", "native", hash, fileName);
            if (Files.exists(path)) return path;
            try (var stream = NativeLibrary.class.getClassLoader()
                    .getResourceAsStream("eim-time-stretchers/" + fileName)) {
                if (stream == null) throw new RuntimeException("Could not find native library");
                var parent = path.getParent();
                if (parent != null) Files.createDirectories(parent);
                var bytes = stream.readAllBytes();
                if (!Objects.equals(hash, calcHash(bytes))) {
                    throw new RuntimeException("Hash mismatch");
                }
                Files.write(path, bytes);
                return path;
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static String calcHash(byte[] bytes) throws NoSuchAlgorithmException {
        var digest = MessageDigest.getInstance("SHA-256");
        var hash = digest.digest(bytes);
        var builder = new StringBuilder();
        for (var b : hash) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
