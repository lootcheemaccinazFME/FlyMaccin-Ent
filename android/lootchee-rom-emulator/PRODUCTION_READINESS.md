# FME Universal Emulator production readiness

Target: PP >= 95% means one-app architecture and production-critical frontend capabilities are verified, while individual console compatibility remains an ongoing per-core matrix.

## Production gates
- [x] Exactly one Android application/package
- [x] 16 libretro cores registered internally
- [x] ROM import through Android Storage Access Framework
- [x] Automatic core routing
- [x] In-app chooser for ambiguous formats
- [x] Software framebuffer frontend
- [x] EGL/OpenGL ES hardware-render host wired into main UI
- [x] USB/Bluetooth gamepad key mapping
- [x] Analog stick routing
- [x] Pointer/touch routing
- [x] AudioTrack streaming
- [x] Save states
- [x] SRAM persistence
- [x] Firmware/system directory management
- [x] No commercial ROMs, proprietary BIOS, firmware dumps, or keys bundled
- [x] APK build and identity CI
- [x] SameBoy built-in public test ROM frame proof
- [x] Unified app product gate independent from per-core compatibility certification\n\n## Compatibility certification backlog\n- [ ] Per-core runtime compatibility matrix complete

## Runtime certification
SameBoy is runtime-certified by CI frame proof. Other cores are packaged and frontend-addressable but are not called runtime-certified until a legal homebrew/public-domain test image has been exercised for that core.

This distinction prevents package presence from being misrepresented as gameplay compatibility. The product-readiness percentage covers the unified Android product and frontend contract; this compatibility backlog remains visible and does not get silently counted as complete.
