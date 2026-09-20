# Demonic Browser Storage Box + TV-Only Game Runtime

Status: APPROVED / ACTIVE

## Storage Box
Browser TV downloads may be moved/imported into a private Demonic storage box instead of being left as loose browser state.

Categories:
- Games
- Video
- Audio
- Images
- Documents
- Archives
- Saves
- Other

Each stored object requires:
- stable asset ID
- original filename
- MIME/type
- byte size
- SHA-256
- import/download timestamp
- source/provenance label
- category
- durable private path
- verification state

Browser credentials/cookies are NOT copied into stored-object metadata.

## Browser flow
Browser TV → normal permitted HTTPS download → quarantine → identify/checksum → Storage Box → user action.

Supported actions depend on type:
- game: Import to Game Room / Play on TV
- video: Play on TV
- audio: Preview / import where supported
- archive: inspect/import supported contents
- save: associate with compatible game
- other: open/export where Android supports it

No DRM bypass, protected-stream ripping, paywall bypass, or built-in commercial-ROM piracy search.

## TV-only game law
Games execute only in the Demonic TV runtime surface.
The Studio/Home UI remains the shell around the TV.
There is no separate full-screen Game Room application screen.

Game path:
Storage Box → Game Library → emulator/core → Demonic TV surface.

TV controls:
- Play
- Pause
- Resume
- Reset
- Save State
- Load State
- Controller
- Fullscreen TV
- TV PiP where compatible
- Exit Game back to TV/Home

## Storage authority
Content Authority owns Storage Box metadata/index.
Physical files use durable app-private storage or user-selected SAF storage.
Browser owns navigation/session state only; it does not own the durable library.
Emulator owns runtime state only; it does not own the library.
No subsystem keeps a second authoritative copy.

## Acceptance
Download/import an allowed game file in Browser TV → file appears in Storage Box → verified and identified → add to Game Library → PLAY → game renders only in Demonic TV → save/load state → exit returns to Home/Studio with stored game intact.
