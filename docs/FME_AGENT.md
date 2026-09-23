# FME Agent Operating Specification

The FME Agent connects project specifications, Kotlin development, CI, release records, and real-device QA.

## Pipeline
UMD + project specs -> repository inspection -> Kotlin/code change -> local/CI validation -> GitHub Actions APK build -> failure-log review -> repair + rebuild -> RELEASES.md -> physical Android phone handoff

## FME PS5 Remote Hub target modules
- PS5 Remote: official Remote Play launch/handoff first.
- Browser: tabs, bookmarks, history, downloads, desktop mode.
- Cloud Storage: provider adapter layer.
- Downloader: direct downloadable media/files and playlist/job queue with pause/resume and storage management. No DRM bypass.
- APK Lab: developer, builder, maker, tester surfaces.
- App Launcher: enumerate permitted installed Android apps and launch through supported Android package/intents APIs.
- Files: local/cloud file surface.
- Runtime Bridge: capability-based intents, deep links, WebView/PWA, companion services, and future supported runtime/container experiments.
- Controller UI: D-pad/gamepad friendly navigation.

## Status vocabulary
Use only VERIFIED, PASS, FAIL, BLOCKED, or PENDING. Never use PASS for a physical-device test that has not happened.
