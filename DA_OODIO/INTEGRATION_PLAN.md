# DA OODIO UNIVERSAL — integration workstream

Status: SOURCE AUDIT / NOT YET MERGED / NO VERIFIED APK

## Goal
Build one original Android-first DAW/game with DA OODIO as the master identity. Preserve all original source projects, character identities, and game mechanics. No destructive replacement of existing projects.

## Uploaded candidate packages (2026-10-31)
| File | Container evidence | Action |
|---|---|---|
| da-oodio-v6.apk | packaged game assets and game.json | identify runtime and export source |
| loon3y-haha-bonkerz-run-v8.apk | packaged game assets and project-backup.json | preserve arcade game |
| cosmic-flow-v3.apk | packaged game assets and rhymes.json | preserve writing game |
| song-studio-v3.apk | packaged game assets and game.json | map songwriting features |
| mic-empire-v1.apk | packaged game assets and game.json | map performance gameplay |
| Studio_5.apk | Android APK; source not supplied | inspect legally reusable interfaces |
| android-app-debug-1793398646248e6d51d56-5401-410e-9eea-f5b48f58ff96 (1).apk | Capacitor configuration present | identify project before reuse |
| REMIX_LYRIA_ANDROID_NEXT_BUILD.zip | Android Studio project + web-source React/Vite app | inspect Android build and feature parity |
| fly-maccin-daw+ (2).zip | Android Kotlin/Gradle source | audit native functionality |
| generic-daw (4).zip | Android app + web package + GitHub workflow | audit license and architecture |

## Repository assessment
- `theDAW`: README identifies an upstream Python/PyTorch/FastAPI + React project, not a proven Android DA OODIO foundation.
- `1-generic-daw`: README identifies upstream Rust/Iced desktop DAW and GPLv3; do not merge code without license compliance review.
- `MiniMax-Music-Studio`: README identifies upstream Windows GPU AI music software; not an Android foundation.
- Use this FME-owned repository integration branch for non-destructive planning until the actual master source is chosen.

## Target modules
1. Shared identity, projects, autosave, and import/export
2. Functional Android recording and audio transport
3. DAW tracks, editing, beat pads, sequencing
4. Writer challenges, lyrics, rhyme mechanics, and saved compositions
5. Song Studio, Remix Lab, Mic Arena
6. Game House and character-based arcade
7. Android build, signing, and device QA

## Acceptance criteria
- Android APK builds reproducibly through GitHub Actions
- APK installs and opens on an Android device
- Record, playback, save, reload, and export work
- Writer-game output transfers to a song project without loss
- All visible controls function or have explicit disabled status
- No proprietary third-party code/assets copied without permission
- No claims of integration or build success without verified artifacts

## Next engineering tasks
1. Inventory all source packages and package IDs.
2. Select the actual DA OODIO source root and Android packaging strategy.
3. Establish module contracts and persistence schema.
4. Integrate one module at a time with regression tests.
5. Add CI only after selecting a buildable project path.
