# FME Unified Product Architecture

Status: integration contract
Branch: integration/octop-fme-unified

## Product chain

FlyMaccin-Ent
→ Octop intelligence/control plane
→ Demonic AI Studio Hut native DAW
→ shared Browser + Downloader + Library
→ agents + memory + knowledge
→ plugins/connectors
→ automation
→ PocketBand production engine
→ media/Mini TV
→ one FME-facing product experience

## Module boundaries

- `platform/octop-cuddie/`: AI orchestration, agents, memory, knowledge, connectors, automation and control-plane services.
- `android/demonic-ai-studio-hub/`: native Android DAW and user-facing studio runtime.
- `shared/fme-bridge/`: versioned contracts between control plane and device/native capabilities.
- `shared/library/`: common asset metadata and import/download handoff contracts.
- `shared/plugins/`: FME/Demonic plugin manifests and capability declarations.

## Integration rules

1. FlyMaccin-Ent is the umbrella repository and product boundary.
2. Octop remains independently testable as the intelligence/control plane.
3. Demonic remains independently buildable as the native Android DAW.
4. PocketBand audio work stays native/local-first and is not rewritten into the Python control plane.
5. Browser, downloader and library operations expose capability contracts to both control plane and DAW.
6. Agents invoke DAW actions through the FME Bridge rather than reaching into Android internals.
7. No production signing material or private credentials enter source control.
8. Existing main branches remain unchanged until integration gates pass.

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

## Integration gates

1. Existing Demonic Android build remains green.
2. Imported Octop Python lint/type/test gates pass.
3. Octop dashboard TypeScript/Vite build passes.
4. Bridge contract tests pass without requiring Android audio hardware.
5. Android semantic smoke proves load → play → sequence → mix → render WAV → playback.
6. Combined CI publishes separate diagnostic artifacts before any pre-release packaging.
