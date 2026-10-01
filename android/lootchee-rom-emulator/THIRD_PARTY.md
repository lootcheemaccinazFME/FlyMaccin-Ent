# Third-party emulator core

LOOTCHEE ROM Emulator v0.2 uses the SameBoy libretro core for Game Boy / Game Boy Color emulation.

SameBoy: https://github.com/LIJI32/SameBoy
License: Expat/MIT-style license. Preserve the upstream copyright and license notice when redistributing the core.

The CI fetches official Libretro Android SameBoy binaries for arm64-v8a and x86_64. No commercial game ROMs or proprietary Nintendo boot ROMs are bundled.

The built-in LOOTCHEE INPUT TEST ROM is generated from original code in TestRom.kt and exists solely to verify ROM loading, video, timing, and controller input.
