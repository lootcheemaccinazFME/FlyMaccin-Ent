# FME Universal Emulator — One App

One Android APK, one package ID, one app icon, one ROM library, one controller layer, one save-state system, and one frontend for the complete FME emulator stack. The emulator cores are internal engines, never separate launcher apps.

## Unified core roster

The APK packages these libretro cores for arm64-v8a and x86_64:

- SameBoy: Game Boy / Game Boy Color
- FCEUmm: NES / Famicom
- mGBA: Game Boy Advance
- bsnes: Super Nintendo / Super Famicom
- Gearsystem: Master System / Game Gear / SG-1000
- ClownMDEmu: Genesis / Mega Drive
- PCSX-ReARMed: PlayStation
- Mupen64Plus-Next GLES3: Nintendo 64 / 64DD
- melonDS DS: Nintendo DS / DSi
- PPSSPP: PSP
- Flycast: Dreamcast / NAOMI / Atomiswave
- Mednafen PCE Fast: PC Engine / TurboGrafx-16 / CD variants
- Stella: Atari 2600
- Handy: Atari Lynx
- Geolith: Neo Geo AES / MVS
- Play!: PlayStation 2

## Shared frontend

Storage Access Framework ROM import, persistent URI access, ROM SHA-256 identity, automatic core routing, Bluetooth/USB controller input, analog routing, pause/reset hooks, video/audio plumbing, and firmware checks live in one application package.

The SameBoy path is the current CI-certified device smoke and must render a 160x144 frame before the pipeline is green. Other cores remain individually certification-gated as their runtime tests are added.

## Content policy

No commercial ROMs, proprietary BIOS files, firmware dumps, or console keys are distributed. Users supply content they are legally entitled to use. Homebrew and public-domain test software may be used for CI.

## Canonical package

Application ID: `com.flymaccin.lootcheerom`

Display name: **FME Universal Emulator**

Version: **1.0.0**
\n## One-app law\n\nAll supported emulator systems live inside `com.flymaccin.lootcheerom`. Core selection happens internally. FME must not publish each core as a separate Android launcher application. Ambiguous ROM formats are resolved with an in-app system picker.\n