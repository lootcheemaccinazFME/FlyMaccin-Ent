# FME Cross-Repo Merge Audit — 2026-10-03

Status: ACTIVE CONSOLIDATION MAP
Authority target: FlyMaccin-Ent
Rule: study behavior; copy only code already proven FME-original and provenance-safe. Reimplement external/upstream behavior independently.

## Classification key
- CORE = belongs in FlyMaccin-Ent.
- DONOR = FME-created work whose unique capability should migrate into the core after provenance review.
- CLEAN-ROOM = external/upstream behavior worth reproducing as a new FME implementation; do not copy source.
- STANDALONE = valuable product that should remain independently buildable.
- REFERENCE = research/behavior reference only.
- ARCHIVE = no active production role after unique findings are documented.
- BUILD = missing capability to implement in FME.

## 35-repository disposition

| Repository | Unique engine / feature signal | Duplicate / overlap | Provenance boundary | Migration decision |
|---|---|---|---|---|
| FlyMaccin-Ent | House architecture; Android super-repo; Pocket Potna/Demonic .fmefun v3 runtime; Node/FME integration target | overlaps all FME studio shells | FME authority | CORE. Canonical destination and source of truth. |
| FME-Bookwriter-OpenAI. | none; empty | Bookwriter target already named in main repo | empty | ARCHIVE/REPURPOSE. Build Bookwriter as a FlyMaccin module. |
| Octop-Cuddie | multi-agent control plane, expert teams, connectors, local memory/KB, plugin/workspace patterns | overlaps FME AI Intelligence/agents | TencentCloud Octop upstream, MIT | REFERENCE/CLEAN-ROOM. Specify FME Agent Control Plane independently; no wholesale copy. |
| FME-Universe-infinite | explicit canonical super-app/module doctrine; intelligence/music/creator/browser/media/games/shared services | duplicates intended FlyMaccin monorepo role | FME-created integration shell | DONOR then ARCHIVE. Merge module contracts/doctrine into FlyMaccin-Ent. |
| dbxfme | database client, many DB adapters, MCP/AI database tooling | overlaps future data/admin tooling | DBX upstream, Apache-2.0 | REFERENCE. Build only FME data adapters actually needed. |
| FME | none; empty | name collides with ecosystem | empty | ARCHIVE or reserve as redirect/documentation shell. |
| jugg-wars | offline street-economy word-game loop, Heat/Memory, operations, save continuity | distinct game | none found in audit root | STANDALONE game; integrate launcher/profile/save contracts only. |
| Mac-Maestro | AI director, shot planning, local creative-model orchestration, non-destructive video editor, performance auto-tune | overlaps future FME Video/Visual House | WanGP-derived, non-commercial evaluation terms plus model licenses | REFERENCE ONLY. CLEAN-ROOM FME Director Engine and model adapter layer. Never transplant source/models. |
| 1-generic-daw | Rust DAW architecture, audio graph, CLAP host, DSP, project/core/widget separation | overlaps FunHous3 DAW | upstream Generic DAW, GPLv3 | REFERENCE ONLY. Rebuild required concepts in FME-original Android architecture. |
| ai-game-maker | prompt-driven web game maker | overlaps ArtiMac/Creator/Game House | likely generated app; no license surfaced | REFERENCE until provenance clarified. BUILD FME Game Maker independently. |
| ArtiMac-AIGameMaker | editor/player split, chapters, characters/assets, deterministic packaged visual-novel runtime | overlaps Game Maker | upstream ArtiMeow, custom FOKPL | REFERENCE/CLEAN-ROOM. Recreate editor/runtime contract, do not transplant. |
| spritebrew | sprite generation, slicing, animation preview, multi-engine export, pixel editor | overlaps Visual/Game asset pipeline | upstream SpriteBrew, AGPLv3 | REFERENCE ONLY. BUILD FME Sprite Lab clean-room. |
| MeadowlarkDemonic | DAW recording/composition/edit/mix/master architecture | overlaps FunHous3 | upstream Meadowlark, AGPLv3 | REFERENCE ONLY. Extract requirements, not code. |
| tuneflow-py | song-wide plugin data model, remote/local algorithm plugins | overlaps AI Maestro/composition plugins | upstream TuneFlow SDK, MIT | REFERENCE. BUILD FME Session Action API over .fmefun; adapters can be separate if ever needed. |
| theDAW | local generative music, edit/mix/score/sing/DJ/live/visuals, model management | overlaps music/AI studio | upstream Gantasmo/Stability lineage, MIT plus model-specific terms | REFERENCE/CLEAN-ROOM. Recreate workflows, never assume model license portability. |
| MiniMax-Music-Studio | local model studio, library + exact request provenance, EQ/player/visualizer, MCP control | overlaps AI music/model manager | upstream project MIT; model terms separate | REFERENCE. BUILD FME Model Runtime + generation provenance + local tool control. |
| Mac-street-life-3d- | offline 3D street-life/open-world product concept | overlaps FME Bay open-world game | upstream third-party, MPL-2.0 | REFERENCE. FME Bay game remains independent original build. |
| Creator-ai | multi-scene movie generation, persistent characters, merge/export, Capacitor | overlaps Mac-Maestro/Video House | upstream third-party MIT | REFERENCE. CLEAN-ROOM FME Film Pipeline. |
| onRps4-Android | Android emulator shell concepts, JNI/native runtime, Vulkan/driver/UI integration | overlaps console research | derivative/emulator ecosystem; source claims/restrictions mixed | REFERENCE ONLY. No source migration. |
| Bachata-S4 | ARM64 Android PS4 release/compatibility architecture | overlaps onRps4/prosperity | upstream shadPS4 ecosystem, GPL-2.0+ | REFERENCE ONLY. |
| MOBDAW | touch-first mobile DAW shell / Demonic OS branding lineage | overlaps D3m0n1c/FunHous3 | provenance must be reviewed file-by-file | DONOR only for FME-authored additions; otherwise REFERENCE. |
| ppssppMac | mature emulator UX, file picker, controller, rendering/device patterns | overlaps console/mobile platform research | upstream PPSSPP, GPL/third-party notices | REFERENCE ONLY. |
| FME-FUN-HUB-Studio | tablet DAW: waveform edit, mixer, DSP FX chain, master EQ, meters, import/resample, .sbrk | overlaps FunHous3/Pocket Potna | SoundBreaker upstream, GPLv3 | REFERENCE ONLY. BUILD FME equivalents: DSP rack, EQ, robust decoder/resampler, meters. |
| BeatMaker | accurate USB MIDI step sequencer, raw MIDI, bitmap UI | overlaps D3m0n1c sequencer/MIDI | upstream BeatMaker, Apache-2.0 | REFERENCE. D3m0n1c/FME implementation should be independently maintained; preserve notices only where actual code is retained. |
| music-orchestra-studio | 9 algorithmic instruments, editable notes, arrangement, AI composition, notation inputs, MIDI import | overlaps composer/FunHous3 | upstream third-party; license not surfaced | REFERENCE until license/provenance clarified. BUILD FME orchestration/instrument layer. |
| FuMiVoice | Android MIDI playback, SoundFont, waterfall, playlists, EQ, tempo/pitch, multi-format export | overlaps MIDI/player/library | upstream plus BASS proprietary licensing, LAME LGPL, SoundFont licenses | REFERENCE ONLY. BUILD FME MIDI player/SF2 path using license-compatible original stack. |
| project-2026-06-14-algo-music-composer | deterministic seeded theory composer; genres/scales/chords/melody/bass/drums; MIDI export; tests | overlaps composition engines | repo presents MIT; provenance still record | DONOR SPEC / possible safe module after file-level provenance. Reimplement algorithms in native FME composition engine to maximize independence. |
| D3m0n1c | native Android unified Studio/Beat Lab/Library; sequencer; procedural drums; Android MIDI wrapper | overlaps current Pocket Potna runtime | FME integration repo with third-party notices | HIGH-PRIORITY DONOR. Merge unique FME-authored MIDI/device and sequencer behavior into FlyMaccin-Ent, then freeze. |
| WebSiteBot_PS5 | retail browser automation | unrelated to FME core | upstream bot, no license surfaced | ARCHIVE/REFERENCE. Do not integrate. |
| PeaSyoMac | Android remote-play UX, controller mapping, haptics, streaming metrics, wake/standby | overlaps controller/device UX research | upstream PeaSyo, AGPLv3 | REFERENCE ONLY. |
| prosperity | PS4/PS5 emulator architecture, Vulkan, ARM/FEX, Android | overlaps console research | upstream Prosperity, GPLv2 | REFERENCE ONLY. |
| ps5upload | resumable transfer queues, verification, archive streaming, persistent jobs, cross-platform host/device UX | overlaps future FME asset transfer | upstream ps5upload, GPLv3 | REFERENCE ONLY. BUILD FME Transfer Queue for project/media assets, not console package functionality. |
| Kyty | early PS4/PS5 emulator architecture | duplicates newer emulator references | upstream Kyty, MIT | ARCHIVE/REFERENCE. Keep only documented lessons. |
| PeaSyo-rs | Rust-native low-latency streaming architecture, packet/FEC/stats/audio/controller split | overlaps PeaSyoMac; source no longer open | upstream/proprietary-current lineage | REFERENCE ONLY. No code copying. |
| FME-FunHouse | Android-first FME workstation; arrangement, piano/drum/modulation deck, mixer, persistent transport; FunHous3 target engine list | overlaps current .fmefun core | FME-original shell | HIGH-PRIORITY DONOR. Merge UI/engine requirements and any unique FME-owned code, then keep as historical release branch or archive. |

## Duplicate implementation clusters

### DAW / Studio cluster
FlyMaccin-Ent + FME-FunHouse + D3m0n1c + MOBDAW + FME-FUN-HUB-Studio + MeadowlarkDemonic + 1-generic-daw + theDAW + tuneflow-py.
Canonical implementation: FlyMaccin-Ent / .fmefun.
Unique items still needed: robust codec/import/resampling, true waveform chunk parsing, real-time meters, DSP effect rack, master EQ, automation lanes, external MIDI device manager, low-latency scheduler, plugin/session-action API, crash-safe recovery.

### Composition cluster
project-2026-06-14-algo-music-composer + music-orchestra-studio + theDAW + tuneflow-py.
Canonical implementation: new FME Composition Engine inside FlyMaccin-Ent.
Required original subsystems: seeded PRNG, scale/chord engine, voice leading, rhythm grammar, melody/bass/drum generators, genre DNA, arrangement grammar, MIDI writer, theory tests, FME Hyphy Engine.

### AI creative/director cluster
Mac-Maestro + Creator-ai + MiniMax-Music-Studio + Octop-Cuddie.
Canonical implementation: FME AI Intelligence + Director/Visual/Video House.
Required original subsystems: model registry, capability adapters, job graph, shot planner, continuity state, generation provenance, local/remote execution, resource estimator, resumable jobs, approval gates.

### Game creator cluster
ai-game-maker + ArtiMac-AIGameMaker + spritebrew + Creator-ai.
Canonical implementation: FME Game/Creator House.
Required original subsystems: project schema, scene/chapter graph, deterministic player runtime, asset registry, sprite slicing/animation metadata, export profiles, AI-assisted authoring with human approval.

### Console/device research cluster
onRps4-Android + Bachata-S4 + ppssppMac + prosperity + Kyty + PeaSyoMac + PeaSyo-rs + ps5upload.
Disposition: isolated research. No emulator/remote-play code enters FlyMaccin-Ent. Reusable *behavioral requirements* only: controller mapping, device capability detection, rendering surface lifecycle, performance overlay, resumable transfer queue, background-service resilience.

## Exact FlyMaccin-Ent target modules

1. android/demonic-ai-studio-hub — canonical FunHous3/Pocket Potna music runtime.
2. core/session — .fmefun schema, migration, asset manifest, recovery journal.
3. core/audio — playback/capture, decoder/resampler, metering, routing, DSP.
4. core/midi — Android MIDI device discovery, mapping, recording, clock/sync.
5. core/sequencer — transport, scheduling, patterns, automation.
6. core/composition — FME-original seeded theory + Hyphy composition engine.
7. core/sampler — mappings, slicing, envelopes, choke groups, kits/presets.
8. core/plugins — session-action API with permissioned, non-destructive mutations.
9. core/provenance — source/asset/model/license ledger and generation receipts.
10. houses/video — FME Director Engine.
11. houses/visual — image/sprite pipeline.
12. houses/game — game authoring + deterministic runtime/export.
13. houses/bookwriter — scripture/book/document authoring workflow.
14. core/agents — FME-original multi-agent coordinator, memory contracts, approvals.
15. core/transfer — resumable FME asset/project transfer queue.
16. core/device — controllers, capability detection, performance telemetry.

## Build-needed priority

P0: compile/device CI for FlyMaccin-Ent; .fmefun migration tests; session recovery journal; real WAV/codec parser; track-length crop; metering; external MIDI manager.
P1: DSP rack + master EQ; sampler slicing/envelopes/kits; automation; composition engine; MIDI import/export; SoundFont-compatible instrument path.
P2: FME session-action/plugin API; AI model registry/job graph; Director Engine; asset provenance ledger; project transfer queue.
P3: Game House; Sprite Lab; Bookwriter House; visual/video generation adapters; expanded shared library.

## Archive / freeze queue
- Empty: FME-Bookwriter-OpenAI., FME.
- After migration: FME-Universe-infinite, FME-FunHouse, D3m0n1c.
- Research-only/freeze: WebSiteBot_PS5, Kyty, onRps4-Android, Bachata-S4, ppssppMac, prosperity, PeaSyoMac, PeaSyo-rs, ps5upload.
- External DAW/creator references should remain untouched and clearly labeled upstream/reference rather than being merged.

## FME clean-room rule
For every external/reference feature:
1. Record observed behavior and user value.
2. Record source repo + license/provenance.
3. Write an implementation-independent FME requirement.
4. Implement from that requirement in FlyMaccin-Ent without consulting/copying source line-by-line.
5. Add tests based on FME behavior requirements.
6. Record the new FME file/commit in the provenance ledger.
7. Keep external assets/models out unless their licenses are independently approved.

This is the governing migration map for the current 35-repository inventory.
