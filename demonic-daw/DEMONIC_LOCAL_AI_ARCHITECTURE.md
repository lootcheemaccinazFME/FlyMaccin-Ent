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
