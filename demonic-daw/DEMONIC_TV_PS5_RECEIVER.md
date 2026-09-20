# Demonic TV — PS5 Receiver Build Contract

Status: APPROVED / ACTIVE

## Goal
PS5 → Remote Play protocol → Demonic PS5 receiver → Demonic TV surface.

The PS5 feed is a first-class Demonic TV source, not an external-app PiP overlay.

## TV sources
- PS5
- Browser
- Free TV
- Suno
- Local Video

## Required UX
- Embedded 16:9 TV dock on Demonic Studio/Home.
- Source switching without replacing the DAW UI.
- TV dock → fullscreen → Demonic PiP → TV dock.
- Independent TV mute/volume controls.
- Controller input remains associated with the active PS5 session.
- Browser state remains persistent separately from project state.

## Architecture
- Native PS5 receiver module/service owns Remote Play session/network/video/input state.
- Receiver feeds a dedicated video surface owned by Demonic TV.
- Kernel Coordinator may orchestrate but owns no receiver domain data.
- Do not place Remote Play state in Audio Authority or Browser state.
- DAW Audio Authority remains isolated from TV/Remote Play audio.
- No channel-number assumptions are introduced.

## Security
- Explicit PS5 registration/pairing.
- Credentials/tokens stored using Android Keystore-backed encrypted storage.
- Never write credentials to project JSON, URLs, logs, browser storage, exports, or diagnostics.
- No unauthenticated LAN control endpoint.
- Disconnect/revoke/forget-console operations required.

## Licensing boundary
Do not copy or incorporate AGPL/GPL Remote Play implementation code into Demonic without an explicit distribution/licensing decision. Protocol research may inform interoperability. Prefer a clean-room implementation or a separately distributed component with a deliberate license boundary.

## Build order
1. Demonic TV embedded SurfaceView/TextureView container.
2. TV source router and lifecycle.
3. PS5 registration/session data model and encrypted credential store.
4. Remote Play discovery/connection handshake.
5. Video decode/render pipeline into Demonic TV surface.
6. Remote audio playback with independent TV controls.
7. Controller/input forwarding.
8. Reconnect/network-change handling.
9. TV dock/fullscreen/PiP transitions.
10. Runtime QA, latency/thermal/network tests, then release gating.

## Acceptance milestone
From Demonic Home/Studio: select PS5 → connect registered console → see/hear live PS5 stream inside the TV dock → control PS5 → continue using Demonic UI → fullscreen TV → PiP → return to dock → reconnect after app/network interruption without exposing credentials.
