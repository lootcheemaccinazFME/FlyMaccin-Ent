# FME / chiaki-ng Android integration

Upstream: https://github.com/streetpea/chiaki-ng

The upstream root CMake currently exposes `CHIAKI_ENABLE_ANDROID` specifically for Gradle-project use. FME's integration must build only the protocol/native pieces needed by the APK and keep the FME application/UI layer separate.

## Build target

1. Vendor an exact upstream commit with `scripts/vendor-chiaki.sh`.
2. Record it in `UPSTREAM_COMMIT`.
3. Configure Android build with:
   - CHIAKI_ENABLE_ANDROID=ON
   - CHIAKI_ENABLE_GUI=OFF
   - CHIAKI_ENABLE_CLI=OFF
   - CHIAKI_ENABLE_TESTS=OFF
4. Build arm64-v8a first.
5. Ensure all packaged native libraries satisfy Android 16 KB page-size requirements.
6. Expose registration, wake and session calls through the FME JNI bridge.
7. Never copy PSN passwords into FME storage.

## Verification gates

- Configure CMake successfully under Android NDK.
- Link the Chiaki native library into FME APK.
- Run discovery on physical Android device.
- Register physical PS5 using user-authorized flow.
- Restart APK and decrypt saved registration.
- Wake registered PS5.
- Establish session request.

All remain PENDING until observed.


## Android dependency policy

FME pins chiaki-ng commit `a9a2805884cfa83865fdfcc09ca3ddfcd628aa42` (v1.10.0-compatible) and imports only upstream `third-party` plus `lib` CMake graphs. The upstream Android app/JNI, GUI, CLI, tests, desktop, Oboe, FFmpeg, and Steam targets are intentionally excluded. Android builds must not satisfy protocol dependencies from Ubuntu host libraries.

Required external/target builds during bring-up:
- OpenSSL crypto used by chiaki-lib
- json-c
- miniupnpc
- nanopb
- Jerasure/GF-Complete
- Opus for the eventual audio path

Curl is not an FME application dependency. If upstream requires it internally, it must be built for the Android target ABI and must never resolve to a host library.

mediaPipelineReady() must remain false until a real ChiakiSession, encoded video sink, audio sink, MediaCodec path and AudioTrack path are wired.

CI preflight is responsible for catching missing v1.10.0 options and host dependency leakage before native compilation.

## Active native milestone

- Pin: `a9a2805884cfa83865fdfcc09ca3ddfcd628aa42`
- ABI: `arm64-v8a` only during bring-up.
- `fme_ps5` links `chiaki-lib` directly.
- JNI registration uses `chiaki_regist_start` and returns completion through an asynchronous Java callback.
- PSN AccountID input is decoded as Chiaki's 8-byte base64 AccountID. The normal PSN password is never requested.
- 16 KB ELF page alignment is requested at link time for `libfme_ps5.so`.
- Physical PS5 registration, wake, and session connection remain PENDING until hardware verification.
