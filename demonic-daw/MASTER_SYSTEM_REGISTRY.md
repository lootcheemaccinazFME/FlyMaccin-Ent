# Demonic DAW Master System Registry

Status: ACTIVE IMPLEMENTATION REGISTRY

## Unified application doctrine

All historical Demonic/Pocket Potna/PocketBand-inspired development lines now converge into **one application**. This supersedes any prior two-app or separate-DD1/DD2 packaging plan.

- Product lineage includes Pocket Potna, Pocket DAW, Demonic DAW, Demonic DAW 2/DD2, Demonic AI Studio Hut, FME Unified Studio, and PocketBand-inspired fast-loop workflows.
- These are capability/workspace modes inside one app, not separate installable products.
- The existing Android package lineage `com.flymaccin.pocketpotna` remains the continuity target unless the owner explicitly changes it.
- Demonic DAW is the unified production/audio system inside the app.
- DD1 means the fast, loop-first, lightweight PocketBand-style workflow.
- DD2 means the deeper workstation workflow: routing, buses, automation, recording, editing, mixer, FX, rendering, controller and AI control.
- DD1 and DD2 share one native Audio/Project/Command/Content core and one project format. A project may move between workflows without conversion or duplication.
- Demonic AI Studio Hut/Pocket Potna capabilities such as FME Library, writing/creative rooms, voice/media tools, optional account/provider connections, remote pairing/control, and local-first storage remain integrated surfaces around the same core.
- AI is deliberately LAST in the implementation order. Local AI and ChatGPT/cloud adapters remain planned, but no AI work may preempt unfinished DAW/audio/editing/render/MIDI/content/release work.
- Historical versions are source/audit inputs. Merge the strongest compatible implementation of each capability; do not preserve obsolete code merely because a version number is newer.
- Latest correction wins when historical versions conflict.
- Capability Truth Law remains binding: merged does not mean runtime-verified.

## Governing laws
1. Channels are generic routing resources. No channel number implies drums, melodic, sampler, vocal, or any other category.
2. Native project state is the canonical project authority. WebView/localStorage is transitional UI state only.
3. All mutations converge on one revisioned, permissioned, undoable command/transaction path.
4. Realtime audio callbacks perform no disk I/O, JSON parsing, networking, AI execution, or unbounded allocation.
5. Capability negotiation tells the truth: features are not advertised before their completion gate is met.

## Authorities
| ID | Authority | Owns |
|---|---|---|
| AUTH-PROJECT | Project | state, IDs, assets references, versions, migrations |
| AUTH-TIME | Time | transport, tempo, samples/beats, scheduling, sync |
| AUTH-AUDIO | Audio | graph, DSP, voices, buffers, recording, monitoring, rendering |
| AUTH-COMMAND | Command | transactions, permissions, history, DCP, controllers, AI |
| AUTH-INTEGRITY | Integrity | checksums, validation, recovery, diagnostics, tests, release gates |
| AUTH-CONTENT | Content | FME packs, samples, presets, kits, templates, provenance/licenses |

Kernel Coordinator orchestrates authorities but owns no duplicate domain truth.

## Completion states
SPECIFIED -> FOUNDATION -> IMPLEMENTED -> TESTED -> RUNTIME_VERIFIED -> PHYSICAL_VERIFIED -> RELEASED

## Priority implementation registry
| System ID | Authority | Dependencies | Current status | Surfaces | Realtime | Persistent | Completion gate |
|---|---|---|---|---|---|---|---|
| ROUTE-GENERIC-001 | Project/Audio | Track routing | IMPLEMENTED | Native/UI/DCP | yes | project | no category defaults anywhere; compile + runtime routing tests |
| CAP-TRUTH-001 | Integrity | CapabilityRegistry | IMPLEMENTED | Native/DCP | no | no | renderer absent => no WAV capability advertised |
| STATE-NATIVE-001 | Project | ProjectStore, schema, journal | IMPLEMENTED | Native/UI/DCP | no | yes | restart/reboot restores complete project without localStorage authority |
| ASSET-001 | Project/Content/Integrity | ProjectStore, checksum store | FOUNDATION | Native/UI/DCP | no | yes | stable IDs/checksums/provenance/relink/manifest verified |
| GRAPH-001 | Audio | routing, parameter registry | SPECIFIED | Native/DCP | yes | project | validated immutable graph drives realtime and offline render |
| SNAPSHOT-001 | Audio | GRAPH-001 | SPECIFIED | Native | yes | no | lock-safe atomic graph swap under stress |
| COMMAND-001 | Command | native state, revision model | FOUNDATION | Native/UI/DCP/AI | no | journal | one atomic transaction + rollback + one undo entry |
| SECURITY-001 | Command/Integrity | Android Keystore | IMPLEMENTED | Native/DCP | no | yes | encrypted credentials, explicit approval, deny default, revoke-all, expiry/reconnect |
| AUDIO-MIX-001 | Audio | graph, SF2/SFZ | FOUNDATION | Native | yes | project | SF2 + samples simultaneous; gain/pan/mute/solo both engines |
| VOICE-001 | Audio | AUDIO-MIX-001 | SPECIFIED | Native | yes | preset/project | polyphony limits, stealing, choke groups, release tails stress-tested |
| RT-SAFE-001 | Audio/Integrity | snapshot, buffer pools | SPECIFIED | Native | yes | diagnostics | callback safety + underrun regression tests |
| RECORD-001 | Audio/Project | asset store, journal | FOUNDATION | Native/UI | yes | yes | crash-safe WAV recording, pause/resume/takes/overdub |
| CLIP-001 | Project/Time | assets, timeline | FOUNDATION | UI/Native | no | yes | nondestructive move/trim/split/fades/stretch/warp |
| MIDI-001 | Project/Time | scheduler | FOUNDATION | UI/Native | yes | yes | arbitrary notes/velocity/CC/quantize/copy-paste |
| MIXER-001 | Audio | graph/parameters | FOUNDATION | UI/Native/DCP | yes | yes | buses/sends/FX/automation validated |
| RENDER-001 | Audio/Integrity | offline graph, jobs, assets | SPECIFIED | Native/UI/DCP | no | yes | WAV mix/stems/range/bounce with validation/progress/cancel |
| JOB-001 | Integrity | service lifecycle | SPECIFIED | Native/UI/DCP | no | yes | resumable/cancellable job states verified |
| DCP-EXT-001 | Command/Integrity | SECURITY-001, event bus | SPECIFIED | DCP/connector | no | yes | authenticated reconnecting command + subscription roundtrip |
| AI-LOCAL-001 | Command | ALL NON-AI RELEASE GATES, DCP, analysis, project memory | DEFERRED-LAST | Native/UI | no | yes | airplane-mode macros/analysis with undo/history |
| CHATGPT-001 | Command | ALL NON-AI RELEASE GATES, DCP-EXT-001 | DEFERRED-LAST | connector | no | no | installed tablet state inspect + command + event roundtrip verified |
| RELEASE-001 | Integrity | tests, migration, render | FOUNDATION | CI/device | no | build | signed APK/AAB + clean install/upgrade + physical certification |

## Immediate dependency order
P0: generic routing cleanup, capability truth, native state/schema/assets, command/security foundations.
P1: immutable audio graph, realtime snapshots, SF2+SFZ routing/mixer, scheduler/voices/buffers.
P2: durable recording, clips, piano roll, arrangement, tracks.
P3: mixer/DSP/buses/sends/automation/latency.
P4: native render/job system and validation.
P5: MIDI/hardware/device calibration and sync.
P6: FME content manager, pack lifecycle, sampler/time-pitch/vocal production, templates and advanced production workflow.
P7: authenticated external DCP transport as a non-AI controller protocol; hardware/controller sync and conflict handling.
P8: advanced/live/collaboration/content SDK, diagnostics, accessibility, tablet UX, compatibility and migration.
P9: certification and release: stress/recovery, emulator/device matrix, signing, APK/AAB, clean install/upgrade.
P10 LAST ONLY AFTER P9: local AI, AI analysis/macros/mix/sampler helpers, project AI memory, then ChatGPT/cloud adapters. AI must not block or preempt the DAW release core.


## Owner priority override — 2026-09-21
AI is last. Finish every non-AI DAW subsystem and its verification gates before implementing AI features. The non-AI execution train is:

P0 integrity/state/storage/transactions -> P1 realtime graph/snapshots/audio -> P2 arrangement/piano roll/audio editing/recording -> P3 mixer/DSP/automation -> P4 rendering/project packaging -> P5 MIDI/hardware/sampler/time-pitch/vocal -> P6 FME content/workflows -> P7 non-AI external control -> P8 advanced UX/live/collaboration/diagnostics -> P9 certification/release -> P10 AI last.

This ordering is mandatory unless the owner explicitly changes it. AI scaffolding already present may remain, but it receives no priority over unfinished non-AI systems.
