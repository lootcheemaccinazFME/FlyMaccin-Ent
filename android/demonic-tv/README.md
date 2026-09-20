# Demonic TV

Standalone Android TV/source surface for the FlyMaccin ecosystem.

## Product boundary
- Standalone APK: `com.flymaccin.demonictv`
- minSdk 23, target/compile 35
- Sources: PS5 | Browser | Free TV | Suno | Local Video
- Display states: Docked | Fullscreen | Demonic PiP
- PS5 session state is owned by `Ps5Receiver`, not the DAW/browser.
- PS5 audio is a TV-source concern and capture into DAW is OFF by default.
- Controller lock is explicitly owned by the receiver.
- Current PS5 transport is a clean interface/scaffold only. It does **not** claim working Remote Play interoperability yet and does not incorporate Chiaki code.

## Merge contract
The package is intentionally isolated so the feature can ship as one APK and its source can also be imported into Pocket Potna / Demonic / FME Universal ♾️ as a feature module. Embedded builds should retain the receiver/source APIs but use the parent app's applicationId, navigation, signer, project/security layer and lifecycle owner.

## Next engineering gates
1. Protocol interoperability research and licensing review.
2. Console discovery + owner-authorized pairing/session credentials.
3. Encrypted session transport, video/audio depacketization and MediaCodec decode.
4. DualShock/DualSense mapping, focus capture/release and reconnect.
5. TV audio bus with volume/mute/background routing and explicit capture permission.
6. Lifecycle/endurance tests across Docked/Fullscreen/PiP and Studio switching.
7. Build APK, install, launch, reconnect and controller QA on real Android hardware.
