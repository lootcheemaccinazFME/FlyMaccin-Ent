# Demonic DAW iOS

Native iOS port of the last-known-good Demonic DAW baseline `c8da921c`.

## Port laws
- Generic MIDI/audio channels: no channel number implies drums or melody.
- Native project state is authoritative.
- Offline/local-first.
- Source assets remain immutable.
- UI commands enter one typed command bridge.
- Audio render callbacks never perform JSON, disk, network, WebView, or blocking work.
- AI is intentionally excluded until the non-AI DAW is release-certified.

## Minimum target
iOS 16+, Swift 5.9+, AVFoundation/Core Audio, CoreMIDI, WKWebView.

The initial shell includes a native project store, command bridge, audio engine, MIDI service and reusable WKWebView host. It is source-ready for Xcode generation/building; signing requires Apple tooling/credentials.
