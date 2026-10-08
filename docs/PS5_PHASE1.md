# FME Native PS5 Phase 1

Order: Android/NDK foundation → discovery → registration → secure credential storage → wake → session connection.

## Current implementation
- Android NDK/CMake/JNI bridge scaffold.
- UDP PS5 LAN discovery on port 9302.
- Parsed console model with ready/standby state helpers.
- Android Keystore AES-GCM encrypted registration store.
- Phase 1 manager and explicit session state machine.
- Wake interface with fail-closed behavior until the native Chiaki-compatible packet encoder is integrated.
- FME Agent does not depend on Sony Remote Play APK.

## Registration requirements
For PS5 PIN registration the user authorizes the console from Settings → System → Remote Play → Link Device. Chiaki-compatible registration also requires the PSN AccountID. Registration output must include the Remote Play registration material required for wake/session operation. Never store a normal PSN password.

## Integration rule
Chiaki-family code is AGPL-3.0. Any copied/linked protocol implementation must preserve license notices and corresponding-source obligations. Keep third-party protocol code isolated from FME UI code and record provenance.

## Truth states
NDK: CODED / build verification pending
Discovery: CODED / physical LAN verification pending
Registration orchestration/store: CODED / protocol registration engine pending
Secure storage: CODED / device verification pending
Wake API: CODED / packet engine pending
Session state machine: CODED / protocol session engine pending

No stage becomes VERIFIED until it passes on a physical Android device and PS5.
