# Demonic Unified Version Harvest
Status: ACTIVE MERGE PLAN
Rule: one application, one project format, one native core. Historical versions are donors, not separate products. Latest correction wins. Capability Truth Law applies.

## Lineage audit and harvest
| Line | Best verified/progress evidence | Harvest into unified app |
|---|---|---|
| v1.1-v1.9 Pocket Potna | APK/source lineage through Author Studio, controls/chat tools, launcher/media, device hub/provider registry/ChatGPT launcher | writing/author tools, device/provider hub, launch integrations, controls |
| v2.0.0 | Full Library Hub / Game Beat | Library/Vault, game/beat workspace concepts |
| v2.0.1 | Backgrounds / Development / DAW Scripture | development/background workspace and preserved project content |
| v2.0.2 | Creator AI Rooms source | creator-room organization |
| v2.0.3 | DAW Integrated / DAW Scripture debug APK | integrated-DAW app structure; historical parent for unified v2.1 |
| v2.0.4 | Demonic SF2/SFZ Sampler source | SF2/SFZ intent only; superseded technically by current FluidSynth + SFZ/WAV native engine |
| v2.1.0 | Demonic Unified / Glass Redesign; CI-built APK | unified product shell, visual direction, integrated DAW |
| v2.1.1 | verified APK install/launch/relaunch | known-good Android baseline and QA discipline |
| v2.1.2 | Full Studio debug APK | studio workspace/surface donor |
| v2.1.3 | Studio OS / Glass Redesign APK | Studio OS organization + visual donor |
| v2.1.4 | Browser Persistence APK | browser/session persistence donor |
| v2.1.5 | Functional Rooms APK | working-room navigation/behavior donor |
| v2.1.6 | Functional Core APK | functional application-core donor |
| v2.1.7 | Merged source | merge intent/audit only unless code proves stronger |
| v2.1.8 | Phase 7 Persona OS; build passed | Persona OS/workspaces; signer mismatch on debug artifact must not become release identity |
| v2.1.9 | Library Sync signed APK/AAB | controlled Library Sync subtree, provenance/checksum manifest, Library/Vault integration |
| v2.1.10 | Suno Home canonical release APK/AAB | broadest recorded implemented capability map: rooms, Creator AI, voice/audio/device, Suno/external lanes, FME systems |
| v2.1.11 | Publish Ready source/static validation | Credential Manager pattern, HTTPS/WebView hardening, secret hygiene, Play/release checklist; not runtime-verified |
| v2.1.12 | Diagnostics source/static validation | diagnostics donor; no compiled APK claim |
| v2.1.13 | ChatGPT Gateway debug APK + source | gateway/control UX donor; current DCP security/native protocol supersedes unsafe/older control internals |
| v2.1.14 | FME Visual Branding complete source | latest complete-source visual/branding donor |
| v2.1.15 | Phase 13 Live Sync checksum/source-development record | live-sync concepts only until source/runtime evidence is harvested |
| current native Demonic branch | ProjectStore v2 revisions/WAL, AssetStore hashes/provenance, Keystore sessions/scopes, generic routing, FluidSynth+SFZ/WAV, FME Core pack, DCP | authoritative technical core for DAW/audio/control/state/security |

## Unified product model
Visible product: Demonic AI Studio Hut.
Package continuity target: com.flymaccin.pocketpotna.
Demonic DAW is the production engine/workspace inside the app.

### Production workflow modes
- Fast Mode (DD1 heritage): loop-first, step/drum grid, quick sample browsing, immediate beat creation.
- Studio Mode (DD2 heritage): tracks, arrangement, piano roll, recording, audio editing, mixer, buses/sends, FX, automation, rendering.
Both modes use the same ProjectStore, AssetStore, Command Engine, Audio Graph, FME Content Library and project format.

### Integrated non-DAW workspaces to preserve/merge
Home; Book Writer/Author Studio; Creator AI; Scripture Writer; Art Room; Notes; Game Maker; Development; Beat Maker; Tools; Library/Vault; Audit; Settings; Persona OS; Voice Studio; FME project/asset/canon systems; optional Suno/browser/provider launch lanes.

## Merge precedence
1. Current native implementation wins for project state, routing, audio engine, assets, security, DCP and future rendering.
2. A historical implementation may replace current code only when it is demonstrably more complete and compatible with the six-authority architecture.
3. v2.1.10 is the broad capability donor, not the technical base.
4. v2.1.14 is the current visual/branding donor where compatible.
5. v2.1.9 supplies Library Sync/provenance patterns.
6. v2.1.11 supplies release/security hardening patterns that do not conflict with newer Keystore/DCP work.
7. v2.1.13 supplies ChatGPT Gateway UX/concepts; authenticated DCP remains authoritative.
8. v2.1.15 live-sync features remain quarantined until source and runtime behavior are verified.
9. Do not merge duplicate state stores, duplicate routing truth, legacy hardcoded channel defaults, plaintext credentials, fake renderers, ephemeral recording URLs, or capability claims without working native services.

## Immediate harvest queue
A. Native routing assignment UI + DCP routing.setChannel/getGraph.
B. Transactionally bind AssetStore imports to ProjectStore revisions.
C. Port Library/Vault + v2.1.9 controlled library_sync behavior around current AssetStore.
D. Port v2.1.10 room/workspace shell without replacing native DAW state.
E. Port v2.1.14 branding/glass visuals as presentation only.
F. Port v2.1.13 gateway surface onto authenticated DCP sessions/scopes.
G. Audit v2.1.15 Live Sync source before adopting any sync behavior.
H. Build immutable Audio Graph + realtime snapshot; then recording/rendering.
