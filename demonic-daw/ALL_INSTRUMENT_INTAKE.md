# P0ck3tb4nd ALL-INSTRUMENT INTAKE

Status: ACTIVE / INVENTORY PASS 1
Date: 2026-10-03
Authority: Content Authority
Canonical runtime: FlyMaccin-Ent/demonic-daw

## Rules
- Never overwrite a stronger existing FME instrument solely because a donor implementation exists.
- Assets/code require provenance and license clearance before promotion.
- DISCOVERED, COMPATIBLE, IMPORTED, TESTED, RUNTIME_VERIFIED are separate states.
- Reference-only implementations are reimplemented cleanly when direct reuse is not cleared.
- No instrument category reserves an audio or MIDI channel.

## Intake registry

| Source | Instrument/content | Type | License/provenance | Intake decision | Status |
|---|---|---|---|---|---|
| FlyMaccin-Ent | FME Core + existing FME WAV/SF2/SFZ/sample content | sample/soundfont/sampler | canonical FME | preserve as preferred existing content | ACTIVE, existing |
| D3m0n1c | procedural drum synth | algorithmic drum synthesizer | repository-owned lineage; third-party notices present | adapt behavior/engine only after compatibility review; no sample assets exist | DISCOVERED |
| music-orchestra-studio | piano | additive synthesis | no repository LICENSE file found in pass 1 | clean-room/reference only until provenance cleared | DISCOVERED, REFERENCE-ONLY |
| music-orchestra-studio | violin | saw-harmonic + vibrato synthesis | no repository LICENSE file found in pass 1 | clean-room/reference only | DISCOVERED, REFERENCE-ONLY |
| music-orchestra-studio | cello | bowed-string synthesis | no repository LICENSE file found in pass 1 | clean-room/reference only | DISCOVERED, REFERENCE-ONLY |
| music-orchestra-studio | flute | sine + breath-noise synthesis | no repository LICENSE file found in pass 1 | clean-room/reference only | DISCOVERED, REFERENCE-ONLY |
| music-orchestra-studio | saxophone | reed/saw synthesis | no repository LICENSE file found in pass 1 | clean-room/reference only | DISCOVERED, REFERENCE-ONLY |
| music-orchestra-studio | trumpet | brass synthesis | no repository LICENSE file found in pass 1 | clean-room/reference only | DISCOVERED, REFERENCE-ONLY |
| music-orchestra-studio | guitar | Karplus-Strong synthesis | no repository LICENSE file found in pass 1 | clean-room/reference only | DISCOVERED, REFERENCE-ONLY |
| music-orchestra-studio | bass | low-frequency Karplus-Strong synthesis | no repository LICENSE file found in pass 1 | clean-room/reference only | DISCOVERED, REFERENCE-ONLY |
| music-orchestra-studio | drums | GM-mapped procedural percussion: kick/snare/closed+open hat/rim/crash/high+mid+low tom | no repository LICENSE file found in pass 1 | clean-room/reference only | DISCOVERED, REFERENCE-ONLY |
| FME-FUN-HUB-Studio | audio engine, FX/EQ workflow; no unique bundled instrument set established in pass 1 | DSP/workflow | GPLv3 | architecture/behavior reference unless canonical runtime licensing explicitly adopts GPL-compatible reuse | DISCOVERED, LICENSE-GATED |
| BeatMaker | sequencer/MIDI engine; no unique instrument bank established in pass 1 | MIDI/sequencer | Apache-2.0 | compatible code may be adapted with notices; not itself an instrument library | DISCOVERED |
| 1-generic-daw | DAW architecture; no unique instrument bank established in pass 1 | architecture | GPLv3 | reference-only under current boundary | DISCOVERED, LICENSE-GATED |
| theDAW | sampler/live/audio-generation/plugin workflows; no static P0ck3tb4nd-ready instrument bank established in pass 1 | workflow/models/plugins | MIT repository code; model/plugin assets have separate provenance | inventory components individually before promotion | DISCOVERED, REVIEW-GATED |
| MiniMax-Music-Studio | content/library workflow | workflow | pending per-component review | no instrument promoted in pass 1 | REVIEW-GATED |
| Mac-Maestro | creative timeline/audio workflow | workflow | pending per-component review | no instrument promoted in pass 1 | REVIEW-GATED |
| MeadowlarkDemonic | repository available | pending | pending | no promotion without instrument/source evidence | REVIEW-GATED |
| tuneflow-py | music logic/project tooling | logic | pending per-component review | not an instrument bank by registration alone | REVIEW-GATED |
| FuMiVoice | voice/audio workflow | voice/audio | pending per-component review | inventory voice engines separately; do not misclassify as musical instrument | REVIEW-GATED |
| project-2026-06-14-algo-music-composer | composition logic | algorithmic composition | pending per-component review | composition source, not automatically an instrument | REVIEW-GATED |
| MOBDAW | unresolved source/build evidence | unknown | unresolved | no production import | QUARANTINED |

## Deduplication policy
1. Existing FME Core/FME expansion content wins over duplicate donor samples/presets unless the donor is demonstrably stronger and provenance permits promotion.
2. Deduplicate binary assets by SHA-256 when bytes are available.
3. Deduplicate presets/instruments by stable identity: engine + synthesis method + parameter set + source provenance.
4. Keep alternate timbres when they are meaningfully different; same category does not mean duplicate.
5. Never replace canonical FME content destructively. Promotions are additive/versioned.

## Promotion buckets
- FME_NATIVE: canonical FME samples, SF2/SFZ/WAV, original native synths.
- COMPATIBLE_DONOR: license-cleared donor code/assets with notices.
- CLEAN_ROOM_REIMPLEMENT: behavior/synthesis concepts reimplemented in P0ck3tb4nd.
- REFERENCE_ONLY: useful design/code that cannot currently be copied.
- QUARANTINE: provenance/source/build unresolved.

## Runtime test gate
An instrument becomes TESTED only after it:
1. loads from canonical Content/Asset authority;
2. produces audio through native P0ck3tb4nd;
3. responds to note/velocity or drum trigger as applicable;
4. survives save/reopen where persistent state applies;
5. routes through generic track/channel rules;
6. survives basic polyphony/voice-stealing and stop/panic behavior;
7. passes mixer/FX routing without crash;
8. is recorded in capability/integrity evidence.

No item discovered in another repository is marked runtime-tested merely from source inspection.
