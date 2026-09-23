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
