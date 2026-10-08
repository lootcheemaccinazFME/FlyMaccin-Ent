# FME Unified Product Architecture

Status: integration contract
Branch: integration/octop-fme-unified

## Product chain

FlyMaccin-Ent
→ one application / zero separate launcher layer / one APK
→ Octop intelligence/control plane
→ Demonic AI Studio Hut native DAW
→ shared Browser + Downloader + Library
→ agents + memory + knowledge
→ plugins/connectors
→ automation
→ PocketBand production engine
→ media/Mini TV
→ one FME-facing product experience

## Single-authority law

FME is one application with one canonical nervous system. No subsystem may introduce its own competing Project, Transport, Timeline, Mixer, AudioGraph, Track, Clip, Asset, or Undo authority.

Those domains have exactly one canonical source of truth inside the unified product. Subsystems may provide UI views, adapters, processors, services, persistence implementations, or capability bridges, but they must read from and mutate the canonical authority through defined contracts. They may not maintain an independent authoritative copy, shadow state machine, conflicting undo stack, competing transport clock, alternate timeline truth, or second audio graph.

The governing invariant is:

**ONE APP → ZERO SEPARATE LAUNCHER → ONE APK → ONE CANONICAL STATE/AUTHORITY LAYER.**

One beast, one nervous system.

## Module boundaries

- `platform/octop-cuddie/`: AI orchestration, agents, memory, knowledge, connectors, automation and control-plane services. It commands canonical application capabilities through contracts; it does not own competing DAW state.
- `android/demonic-ai-studio-hub/`: native Android DAW and user-facing studio runtime. Native audio implementations must participate in the canonical authority model rather than create parallel Project/Transport/Timeline/Mixer/AudioGraph/Track/Clip/Asset/Undo truth.
- `shared/fme-bridge/`: versioned contracts between control plane and device/native capabilities.
- `shared/library/`: common asset metadata and import/download handoff contracts. Asset identity remains canonical across all consumers.
- `shared/plugins/`: FME/Demonic plugin manifests and capability declarations. Plugins extend capabilities but cannot become state authorities.

## Integration rules

1. FlyMaccin-Ent is the umbrella repository and single product boundary.
2. The shipped Android product is one app, with zero separate launcher product/layer and one APK.
3. Exactly one canonical authority governs Project, Transport, Timeline, Mixer, AudioGraph, Track, Clip, Asset, and Undo state.
4. No subsystem, plugin, agent, bridge, UI surface, or production engine may introduce a competing authority for those domains.
5. Octop remains independently testable as an intelligence/control-plane module while consuming canonical application contracts.
6. Demonic remains independently buildable as a native Android DAW module while preserving the single-authority invariant.
7. PocketBand audio work stays native/local-first and is not rewritten into the Python control plane; native/local-first does not mean independent authority.
8. Browser, downloader and library operations expose capability contracts to both control plane and DAW.
9. Agents invoke DAW actions through the FME Bridge rather than reaching into Android internals.
10. Undo/redo mutations must flow through the canonical command/history authority so actions from UI, agents, plugins, automation, and native engines remain coherent.
11. Transport timing and playback state must resolve to one canonical transport authority.
12. Timeline, Track, Clip, Asset, Mixer, and AudioGraph identifiers must remain stable across subsystem boundaries and bridge calls.
13. No production signing material or private credentials enter source control.
14. Existing main branches remain unchanged until integration gates pass.

## Initial FME Bridge capability surface

- health/status
- project create/open/save
- asset search/download/import
- instrument/sample load
- transport play/stop
- note/chord/sequence operations
- mixer volume/pan
- FX enable/configure
- render WAV/stems
- exported-media playback
- library indexing
- browser/media handoff
- plugin capability discovery

Every mutating capability above must route to the canonical authority rather than maintaining subsystem-owned authoritative state.

## Integration gates

1. Existing Demonic Android build remains green.
2. Imported Octop Python lint/type/test gates pass.
3. Octop dashboard TypeScript/Vite build passes.
4. Bridge contract tests pass without requiring Android audio hardware.
5. Android semantic smoke proves load → play → sequence → mix → render WAV → playback.
6. Single-authority contract tests prove UI, agents, plugins, automation, PocketBand/native audio, and bridge calls converge on the same Project/Transport/Timeline/Mixer/AudioGraph/Track/Clip/Asset/Undo state.
7. Undo/redo cross-surface tests prove a mutation initiated from one surface is observed and reversible from every other authorized surface.
8. Combined CI publishes separate diagnostic artifacts before any pre-release packaging.
