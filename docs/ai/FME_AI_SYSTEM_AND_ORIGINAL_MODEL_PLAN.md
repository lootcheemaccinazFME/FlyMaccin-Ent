# FME AI System: implementation blueprint v0.1

## Priority
Build FME AI System first, then an independently trained original model. Both run as services/modules inside LOOTCHEE OS and Demonic, not launcher applications.

## Source registry
Google Drive: FME_LOOTCHEE_OS_MASTER_AUDIT_INDEX_vCurrent_Part1.docx, source id 1OefwmpbP8meMm0hzkPnfFb1gHmT5mQst. Import only with provenance, permission and explicit asset selection. Registry topics: FME Universe Bible, FME Audit Archive, LOOTCHEE OS Continuity, genre ownership, roster, songs, visuals, lore, shared runtime, memory vaults, executive decision archive, doctrine.
Other Google Drive files discovered through search remain candidates, not ingested.

## System A: FME AI (first)
- Native Demonic AI room backed by LOOTCHEE OS service APIs, never an external launcher.
- Source connectors: user-approved Drive document/media ingest, GitHub code, local studio project assets.
- Canon registry: versioned entity/project/artist/track/character/source IDs, one authoritative canon version, archive alternate versions.
- Ingest pipeline: discover -> permission -> checksum -> MIME validate -> extract/transcribe -> chunk -> tag -> provenance -> embedding -> index.
- Retrieval: hybrid keyword/vector search, citations, source permission checks, canon priority, refusal to invent unavailable files.
- Tools: project read/write, MIDI/DAW commands, arrangement, asset search, generation queue, model inference adapter.
- AI runtime: initially existing model(s) behind pluggable local/remote adapters, with model and license disclosure.
- Security: secrets server-side, Drive OAuth scopes, per-project permissions, audit logs, no training on assets without authorization.
- Interface: FME AI Chat, Music Assistant, Director, Library Search, Asset Inspector, Job Queue, Settings.
- Acceptance: user can ask for a canon item and get source-grounded answer; create a Demonic track from structured command; queue a Director task; import an authorized Drive asset; export an audit log.

## System B: independent original FME model (second)
- Define own architecture and tokenizer/training code; avoid representing fine-tuned third-party weights as independently original.
- Dataset manifests with explicit licenses and source hashes; deduplicate, redact private data, hold out evaluation.
- Train a small proof-of-concept language model first; run reproducible training/evaluation and model cards.
- Increase size only after quality, safety, cost and rights gates; inference through same LOOTCHEE OS model adapter.
- Domain evaluation: FME terminology, entity consistency, source-grounded answers, DAW command JSON, refusal of unsupported canon claims.
- Do not claim a trained model or model weights until training has actually run.

## Execution milestones
M1 source registry + connector index; M2 retrieval/permissions; M3 Demonic native AI UI + tools; M4 evaluation; M5 original model training scaffold; M6 trained checkpoint; M7 model integration.
Progress percentages must be measured by passing tests, not estimated from UI screens.
