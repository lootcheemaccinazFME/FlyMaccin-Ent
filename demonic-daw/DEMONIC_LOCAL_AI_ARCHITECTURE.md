# Demonic Local AI Architecture v1

Status: ACTIVE IMPLEMENTATION CONTRACT

## Product law
Demonic DAW remains fully usable without a cloud AI subscription. Local AI and deterministic automation are core/offline capabilities. ChatGPT and other cloud providers are optional controllers. No provider owns the project format, DCP, audio engine, or production rules.

## Layers
1. Deterministic Production Rules: zero-model macros, validation, quantize/groove operations, routing, gain staging helpers, naming, arrangement transforms, FME content filtering.
2. Local Intent Engine: maps supported natural-language intents into validated DCP plans.
3. Optional On-Device Model Adapter: pluggable compatible local model for richer language understanding. Model absence never disables the DAW.
4. DCP Planner/Executor: converts a plan into scoped commands/transactions and respects project revision, undo and permissions.
5. Optional Cloud Controller Adapter: ChatGPT/other authorized provider connects through the same authenticated DCP gateway.

## Safety and execution
AI never calls realtime DSP directly. AI proposes or executes DCP commands according to session scopes. Destructive/file/publish operations remain permission gated. Every AI mutation records source=local-ai or source=<provider>, command IDs, transaction ID, before revision, after revision, result and undo entry.

## Local AI capabilities
- project/track/clip search and selection
- track/clip create, rename, duplicate, reorder and arrangement transforms
- MIDI note/pattern generation and editing
- groove/quantize/humanize through deterministic transforms
- FME pack/sample search and selection from installed content
- sampler mapping assistance
- mixer balance/pan/mute/solo operations
- FX-chain and parameter operations when native FX exist
- bus/send/routing operations when native routing exists
- automation generation/editing
- transport and recording preparation
- project save/version operations
- render/bounce/stem requests when native renderer exists
- project diagnostics and missing-asset detection

## Provider independence
Define AiController interface:
getCapabilities()
plan(intent,state)
execute(plan,session)
cancel(transactionId)
explain(transactionId)

Adapters: LocalRulesController, OnDeviceModelController, ChatGptController, FutureProviderController.

## Offline behavior
When disconnected, local rules and installed on-device model remain available. No API key is required for deterministic local automation. The UI clearly labels LOCAL AI vs CONNECTED AI and never silently sends project/audio content to a cloud provider.

## Model/content privacy
Cloud transfer is opt-in per connection/session and bounded by permission scope. Audio files are not uploaded merely to interpret ordinary control commands. Local project state sent externally should be minimized to fields required for the requested action.

## AI UX
AI panel: command input, Local/Connected indicator, current permissions, planned actions, Execute/Cancel, Undo AI Action, history, errors. Voice control may be layered later through the same intent interface.

## Completion gates
Local AI works with airplane mode.
Cloud disconnection does not block DAW functions.
Unsupported intent fails without project mutation.
Transactions are undoable.
Stale-revision commands are rejected.
Permission-denied commands do not execute.
No AI/network code runs in realtime audio callback.


## Advanced Local AI / Production Intelligence Modules

### Musical project memory
Maintain project-scoped musical context separately from assistant/account memory. Store song structure, section identities, motifs, estimated key/chord context, groove fingerprints, production decisions, corrections, accepted/rejected AI actions and provenance. Project memory is portable with the project and can be reset without affecting general AI state.

### Audition, snapshots and version graph
Every substantial AI edit can branch non-destructively. Snapshots form a version graph with stable IDs, parent revision, timestamp, transaction provenance and restore metadata. A/B audition compares selected snapshots without overwriting either branch.

### Production macros
Macros are validated DCP transaction templates. Initial library: Tighten Drums, Build Hook, Clean Vocal, Make Space for Bass, Create Breakdown, Prepare Stems. Macros expose parameters, preview/planning, permission requirements and one-step undo.

### Local audio analysis
Background analyzers provide BPM/tempo confidence, transient markers, silence regions, peaks, clipping, RMS/loudness estimates, pitch/key estimates, waveform summaries and section candidates. Analysis results are cached as derived assets and never block realtime audio.

### Local MIDI intelligence
Provide scale/key analysis, chord identification, voicing transforms, bass/chord relationship analysis, groove extraction, controlled humanization, pattern variation, transpose and constraint-aware note generation.

### AI sampler builder
Analyze FME/imported samples, derive duration/peak/pitch where practical, classify/tag candidates, propose zones/layers/kits and create sampler maps through DCP. Routing remains explicit project state; sample category never implies a channel number.

### AI mix assistant
Consume native meters and analysis data before making recommendations. Support gain staging, clipping/crest warnings, frequency-conflict hints, dynamics observations, stereo/bus analysis and scoped DCP actions. Measurement and deterministic constraints outrank model guesses.

### Background job manager
All analysis, waveform generation, indexing, rendering, stem export, sample analysis and other expensive work use native jobs with stable IDs, queued/running/completed/failed/cancelled states, progress, cancellation and recovery metadata.

### Asset database
Index FME content, recordings, imports, renders, presets and projects by stable asset ID. Support tags, categories, favorites, recents, search, provenance, dependency references and missing-asset recovery. Filesystem paths are implementation details, not project identity.

### Device/audio diagnostics
Expose sample rate, buffer/burst configuration, underrun count, engine state, storage capacity, microphone/input state, USB audio, Bluetooth audio, MIDI connectivity and performance diagnostics through native capabilities/state.

### Per-project AI permissions
Projects may restrict AI to read-only, edit, record preparation/control, render, file and publish scopes independently of global session permissions. Effective permission is the intersection of session and project policy.

### Connector audit trail
Record controller identity, local/cloud source, session ID, scopes used, transaction/command IDs, project revision before/after, timestamp, affected entities, result and undo reference. Never store secret credentials in audit records.

### Offline fallback
Cloud loss changes controller availability only. Core DAW, deterministic automation, installed local model, project storage, editing, recording and rendering remain local-first.

### Portable project package
Package canonical project state, durable audio assets, MIDI, automation, sampler maps, presets, FME dependency manifest, version metadata and integrity hashes. Missing licensed/bundled content is represented as a dependency rather than silently duplicated.

### DD1/DD2 compatibility
DD1 and DD2 share project schema, stable IDs, DCP, storage, asset database, render service and AI interfaces. UI capability exposure differs; project meaning does not.

### Recovery and safe mode
Maintain journaled autosave, last-known-good snapshot, incomplete-recording recovery, incomplete-render cleanup and an engine-safe startup path that can suppress optional content/plugins while preserving project access.

### Testing harness
Automate DCP schema/transaction tests, permission denial, invalid/expired/revoked authentication, project revision conflicts, persistence/restart/reboot recovery, render validation, audio-engine stress, job cancellation/recovery and connector audit verification. Physical-device regression remains a release gate.

### Collaboration, later phase
Authenticated sharing uses portable project packages/state, stable IDs, scoped permissions, revisions and explicit conflict handling. Collaboration is gated behind durable local storage, versioning and recovery readiness.
