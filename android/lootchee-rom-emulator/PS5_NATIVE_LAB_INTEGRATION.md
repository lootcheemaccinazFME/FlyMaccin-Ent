# PS5 Native Lab | Android integration gate v0.1

Status: RESEARCH / NOT PLAYABLE / NOT CERTIFIED
Owner: LOOTCHEE OS / DEMONIC Gaming
Target package: com.flymaccin.lootcheerom

## Existing source of truth
- Universal Android application: android/lootchee-rom-emulator/
- CoreRegistry.kt registers libretro cores; it DOES NOT register a PS5 core.
- RuntimeCapabilities.kt currently has no certified PS5 backend.
- Experimental source: lootcheemaccinazFME/prosperity (master), with android/ and delta/gpu/ps5/.
- Prosperity is not a libretro core. Do not load it through the libretro ABI or mark it installed.
- Preserve the one-APK product goal without representing a remote launcher as embedded native execution.

## Integration acceptance gates
1. Confirm upstream license, dependency licenses, redistribution and asset provenance.
2. Build an ARM64 Android native library from pinned source with reproducible NDK/CMake versions.
3. Define a narrow JNI interface for initialization, homebrew loading, rendering, input, shutdown, and structured error reporting.
4. Confirm Vulkan feature support and report unsupported devices without crashes.
5. Execute an independently developed homebrew smoke test on actual Android hardware.
6. Confirm background/resume, orientation, save isolation, crash logs and thermal behavior.
7. Only after passing 1-6 add PS5 to the visible core selection. No fake FPS, compatibility, or launch success.

## Immediate next actions
- Inspect prosperity Android build entrypoints and native JNI contracts.
- Compare native dependency graph against the universal APK Gradle and CMake configuration.
- Add a feature-flagged native adapter on an isolated development branch; do not alter production core routing yet.
- Run CI compile and instrumented Android checks before claiming success.

## Explicit boundaries
No proprietary Sony firmware, keys, or copyrighted game files are bundled.
No claim of PS5 commercial-game compatibility.
This file is a real integration checkpoint, not an emulator implementation.
