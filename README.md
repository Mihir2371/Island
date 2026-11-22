# Island

An Android Dynamic Island-inspired interface focused on iOS-like motion, compact activity surfaces, and cutout-aware positioning.

## Current foundation
- Kotlin + Jetpack Compose
- Android 8.0+ (API 26)
- Animated compact/expanded island preview
- Dark UI baseline
- GitHub Actions: tests and debug APK artifact on pushes, pull requests, and manual dispatch

## Build locally
Requires JDK 17 and Gradle 8.9.

```bash
gradle test
gradle assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Roadmap
1. Notch/cutout-aware placement and overlay architecture
2. Refined spring morphs, gestures, interruption and reduce-motion support
3. Activity framework with media, timers, calls, and system integrations
4. Performance profiling and reliability testing (RAM target under 80 MB)

The RAM target is a profiling goal, not a guarantee at this scaffold stage.
