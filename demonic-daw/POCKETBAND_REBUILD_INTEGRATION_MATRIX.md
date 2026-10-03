# PocketBand Rebuild Integration Matrix

Status: ACTIVE IMPLEMENTATION INPUT
Target: Demonic DAW unified core, DD1 loop-first surface
Rule: behavior/capability study only where third-party licensing does not permit direct code reuse. Preserve notices/licenses for any code actually reused.

## Product target
Recreate the fast PocketBand/uLoops workflow as an original FME implementation:
Song -> Loops/Sections -> generic Tracks/Channels -> Devices -> FX -> Mix -> local Render.

Historical behavior target:
- up to 32 loops per song
- up to 16 channels per loop
- synth, drum machine, sampler/recording, modulator and arpeggiator workflows
- per-channel FX/mix automation
- imported audio
- rapid loop duplication/reordering
- local-first save and render, replacing historical cloud-render dependence

## Repository federation

| Repository | Use | Integration boundary |
|---|---|---|
| FlyMaccin-Ent/demonic-daw | Canonical destination | Native ProjectStore, AssetStore, Oboe/FluidSynth/SFZ, routing, AudioGraph, snapshots, DCP, release QA |
| D3m0n1c | Android performance workflow donor | Beat Lab, 16-step sequencing, procedural drums, USB MIDI, pattern storage |
| FME-FUN-HUB-Studio | Android DAW workflow donor | multitrack timeline, recording, mixer, FX, EQ, import/export, transport, project UX |
| BeatMaker | Sequencer/MIDI reference/donor subject to license | USB MIDI behavior and timing patterns |
| 1-generic-daw | Architecture reference; GPL boundary applies | graph/editor/workstation behavior only unless GPL-compatible reuse is explicitly chosen |
| music-orchestra-studio | Composition UX/reference | piano roll, arrangement, undo/redo, MIDI import, WAV export concepts |
| theDAW | Local studio/reference | local-first editing/generation workflow; do not make AI a release dependency |
| MiniMax-Music-Studio | Content workflow reference | local library/job UX; not a core Android DAW engine |
| Mac-Maestro | Creative timeline reference | non-destructive multitrack/editor workflow; not realtime audio authority |
| MOBDAW | Quarantined pending repository verification | no code promotion until source/build evidence is retrievable |

## P0ck3tb4nd operating repository federation

P0ck3tb4nd treats the repositories below as one governed operating-repository ecosystem. Membership does **not** mean blind source merging. Each repository keeps a declared role, license boundary, provenance, and capability-truth status. The canonical runtime remains `FlyMaccin-Ent/demonic-daw`.

| Repository | P0ck3tb4nd role | Operating contribution / boundary |
|---|---|---|
| FlyMaccin-Ent | CANONICAL RUNTIME | P0ck3tb4nd/DD1+DD2 application core, native state, assets, audio, routing, control, QA and releases |
| D3m0n1c | ACTIVE ANDROID DONOR | Beat Lab, step sequencing, touch pads, USB MIDI and Android performance workflow |
| FME-FUN-HUB-Studio | ACTIVE ANDROID DAW DONOR | multitrack editing, recording, mixer, DSP/FX, EQ, import/export and project UX |
| BeatMaker | ACTIVE MIDI/SEQUENCER DONOR, LICENSE-GATED | raw USB-MIDI and sequencer timing patterns; direct reuse only when license permits |
| 1-generic-daw | ACTIVE ARCHITECTURE REFERENCE, GPL BOUNDARY | workstation/audio-graph/editor concepts; no incompatible source copying |
| music-orchestra-studio | ACTIVE COMPOSITION REFERENCE | piano roll, arrangement, track controls, undo/redo, MIDI import and WAV-export concepts |
| theDAW | ACTIVE LOCAL-STUDIO REFERENCE | local-first studio/editing workflow; AI portions remain outside the non-AI release critical path |
| MiniMax-Music-Studio | ACTIVE CONTENT/JOB REFERENCE | library, jobs and local content workflow; not Android realtime audio authority |
| Mac-Maestro | ACTIVE CREATIVE-TIMELINE REFERENCE | non-destructive timeline/editor concepts; not realtime audio authority |
| MeadowlarkDemonic | ACTIVE REPOSITORY, REVIEW-GATED | available to P0ck3tb4nd research/intake; capability promotion requires source/build evidence |
| tuneflow-py | ACTIVE MUSIC-LOGIC REFERENCE | music/project manipulation concepts; Python code is not an Android realtime dependency |
| FuMiVoice | ACTIVE VOICE/AUDIO REFERENCE | voice/audio workflow research; promotion requires compatibility and provenance review |
| project-2026-06-14-algo-music-composer | ACTIVE COMPOSITION REFERENCE | algorithmic composition ideas; no AI/realtime authority implied |
| MOBDAW | QUARANTINED | repository exists and is active, but no source/code promotion until expected source/build evidence is retrievable |

### Federation laws
1. P0ck3tb4nd owns the integration contract. Donor repositories do not become competing runtime authorities.
2. Code is promoted only after license/provenance review, compatibility review, tests, and capability-truth evidence.
3. Reference-only repositories may inform behavior and architecture without copying protected or incompatible source.
4. No repository may introduce category-reserved audio/MIDI channels.
5. Native ProjectStore/AssetStore remain durable state authorities; donor-local state does not become canonical by accident.
6. Realtime code must obey RT-SAFE rules regardless of donor implementation.
7. Repository membership never upgrades a capability to TESTED/RUNTIME_VERIFIED/PHYSICAL_VERIFIED by itself.
8. AI-capable repositories remain subordinate to the P10 AI-last rule.
9. Quarantined repositories may be inventoried but cannot feed production code until their gate is cleared.
10. Every promoted subsystem records its source repository, license/provenance decision, target files, tests, and resulting capability status.

### Operating intake pipeline
`DISCOVER -> CLASSIFY -> LICENSE/PROVENANCE -> MAP CAPABILITY -> CLEAN-ROOM/COMPATIBLE ADAPT -> TEST -> PROMOTE -> RECORD`

This federation is the P0ck3tb4nd source-and-reference operating layer. It expands the system's available parts without creating duplicate project, time, audio, command, integrity, or content authorities.

## PocketBand-compatible original data model

Song
- id, name, tempo, time signature
- ordered Loop[] (maximum compatibility target 32; engine may support more later)

Loop
- id, name, bars, order
- TrackInstance[] referencing generic project tracks
- no instrument category is inferred from channel number

Track
- stable id/name
- optional generic integer channel assignment
- Device chain
- mixer state
- automation
- clips/pattern references

Device kinds
- SYNTH
- DRUM_MACHINE
- SAMPLER
- RECORDER
- MODULATOR
- ARPEGGIATOR
- AUDIO
- MIDI
- future devices through capability registry

## Non-negotiable corrections
1. No channel number has a musical meaning.
2. No copied PocketBand assets, branding, proprietary source or server protocol.
3. Rendering is local/native.
4. Native project state is canonical.
5. Every mutation uses revisioned command/undo history.
6. Audio callback never performs disk/network/JSON/AI work.
7. Capability registry must not advertise unverified features.
8. DD1 is the PocketBand-fast surface; DD2 exposes deeper routing/editing without project conversion.

## Execution gates
1. AssetStore + canonical native state
2. Loop/section schema + generic routing
3. AudioGraph + immutable realtime snapshots
4. mixer + voices + simultaneous SF2/SFZ/WAV
5. PocketBand-style device rack and 16-step/piano-roll editors
6. recording + imported audio + clips
7. local WAV mix/stem render
8. MIDI/hardware
9. secure non-AI DCP
10. physical tablet certification
11. AI only after non-AI release gates

## Acceptance target for first PocketBand-remake release
A user can create a song, add/reorder/duplicate loops, assign any device to any generic track/channel, program drums and notes, import/record audio, mix with FX, save/reopen without loss, and render a valid local WAV entirely offline. Touch workflow must be usable on the target Android tablet.
