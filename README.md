# PS2 Emulator

A native Android project for a PlayStation 2-inspired emulator frontend.

## Project goals
- Android Native with Kotlin and Jetpack Compose (no WebView).
- Minimum Android version: Android 6.0 / API 23.
- PS2-style startup and system browser experience.
- Import user-owned game images through Android's Storage Access Framework.
- BIOS management for user-provided BIOS files.
- A real PS2 emulation engine integration, subject to compatibility and licensing verification.

## Important status
This repository currently contains the initial Android application foundation and interface shell. **It does not yet emulate PS2 games.** The Play! engine must be evaluated and integrated in a separate, tested step. Android 6 compatibility and actual game performance are not assumed until verified on devices/emulators.

## Build
Open the project in Android Studio with JDK 17 and allow Gradle sync. The CI workflow builds the debug APK.

## Legal
Do not bundle copyrighted games or PlayStation BIOS files. Users must provide files they are legally entitled to use. The PlayStation name and marks belong to their respective owners; this is an independent community project.
