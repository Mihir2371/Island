# Island
Android Dynamic Island-inspired UI with iOS-like capsule motion.

## CI and APK
GitHub Actions runs `test assembleDebug` on pushes, pull requests and manual dispatch. A debug APK is uploaded as an Actions artifact for 14 days (not published to Releases).

## Current build
- Compose capsule demo and compact/expanded spring animation
- Demo visibility, priority and reduce-motion controls
- Overlay permission entry point
- Native Quick Settings tile services for Island, Hide Island, Priority, Reduce Motion, Pause Activities, Quick Timer and Recorder. These currently persist local tile toggle state; they are not yet wired to overlay, countdown, or MediaProjection functionality.

## Local build
JDK 17, Gradle 8.9:
```sh
gradle test
gradle assembleDebug
```
APK output: `app/build/outputs/apk/debug/app-debug.apk`

## Remaining roadmap
- Floating overlay service, shared preferences/state and notch/cutout-aware positioning
- Cutout calibration, waterdrop/punch-hole/notch/no-cutout/foldable testing
- Activity system, priorities and lifecycle
- MediaSession music integration, real timers/stopwatch
- Calls, notifications, screen recorder with MediaProjection consent
- Navigation, rides/delivery, sports, fitness, flights, transit and transfer integrations
- Accessibility, privacy, compatibility and performance profiling (RAM under 80 MB is a target, not measured)
