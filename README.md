# PS2 Emulator

A native Android project for a PlayStation 2-inspired emulator frontend.

## Project goals
- Android Native with Kotlin and Jetpack Compose (no WebView).
- Minimum Android version: Android 6.0 / API 23.
- PS2-style startup and system browser experience.
- Import user-owned game images through Android's Storage Access Framework.
- Persistent game library with remove support.
- A real PS2 emulation engine integration, subject to compatibility and licensing verification.

## Emulation engine evaluation

The current engine candidate is [Play!](https://github.com/jpd002/Play-), an open-source C++ PlayStation 2 emulator with an Android frontend and a built-in high-level emulation BIOS. Its upstream Android Gradle configuration declares minSdk 21, so its declared minimum API level does not itself force this app above Android 6. The upstream build currently uses compileSdk 35, Android Gradle Plugin 8.13.2, NDK 29.0.14206865 and CMake 3.31.1; these toolchain requirements need to be reconciled with this app's current build before the native core is added.

Play!'s built-in BIOS means an external PS2 BIOS file is not required by that engine. The current BIOS file picker is therefore only provisional and must be redesigned if Play! is adopted. No upstream core is integrated yet.

The frontend accepts common Play! disc/content formats: ISO, CHD, CSO, CUE, ELF, ISZ and MDS. File selection and library persistence do not mean a game can already boot.

## Important status

This repository currently contains the Android application foundation and frontend shell. **It does not yet emulate PS2 games.** Engine integration must include the native C++ core, Android JNI/runtime bridge, graphics surface, audio, controller input, and tested game boot/shutdown lifecycle. Android 6 compatibility and actual game performance are not assumed until verified on API 23 devices and real hardware.

## Build

Open the project in Android Studio with JDK 17 and allow Gradle sync. The CI workflow builds the debug APK.

## Legal

Do not bundle copyrighted games or PlayStation BIOS files. Users must provide game images they are legally entitled to use. Play! is distributed under its own BSD-style license; preserve its upstream notices and license terms if its code is integrated. The PlayStation name and marks belong to their respective owners; this is an independent community project.
