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
