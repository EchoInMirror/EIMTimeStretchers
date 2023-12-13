# EIMTimeStretchers [![Release](https://github.com/EchoInMirror/EIMTimeStretchers/actions/workflows/release.yml/badge.svg)](https://github.com/EchoInMirror/EIMTimeStretchers/actions/workflows/release.yml) [![Jitpack](https://www.jitpack.io/v/EchoInMirror/EIMTimeStretchers.svg)](https://www.jitpack.io/#EchoInMirror/EIMTimeStretchers)

A collection of time stretchers, FFT and resample algorithms for digital audio signals processing.

## Algorithms

- SoundTouch
- RubberBand
- vDSP (FFT, macOS only)
- sleef (FFT, Windows only)
- libresample (Resample)

## Usage - Java

```groovy
repositories {
    maven { url 'https://www.jitpack.io' }
}

dependencies {
    implementation 'com.github.EchoInMirror.EIMTimeStretchers:common:<version>'
    
    // Pick one of the following
    implementation 'com.github.EchoInMirror.EIMTimeStretchers:windows:<version>'
    implementation 'com.github.EchoInMirror.EIMTimeStretchers:macos:<version>'
    implementation 'com.github.EchoInMirror.EIMTimeStretchers:linux:<version>'
}

tasks.withType(JavaCompile).each {
    it.options.compilerArgs.add('--enable-preview')
}

// kotlin script
// tasks.withType<JavaCompile> {
//     options.compilerArgs = options.compilerArgs + listOf("--enable-preview")
// }
```

## Author

Shirasawa

## License

[AGPL-3.0](LICENSE)
