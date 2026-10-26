# FME AI Core v0.1 (LOOTCHEE OS)

## Purpose
Native Demonic AI Studio subsystem, not an external launcher. Phase 1 uses retrieval-augmented generation and deterministic FME governance before attempting original-model training.

## Verified source corpus (Google Drive)
- LOOTCHEE OS Universal Master Doctrine Library Inventory Audit (2026-09-03), id 1_C1DWrk9Mw7vAWXj1txCTEYy3yoeEjdS
- MACCIN AZZ OPERATING SYSTEMS, id 1-NGay3nJWh2zYq9m7tWYKLEpct2FfsBs
- FME Studio Aippy Android Commands v2, id 1f2oENYV4MqyEeiYYq7opLGS-ofbsrFBR
- Universal Master Doctrine Consolidated, id 1MP9IB0GLGBi-ui3fTLfEF1LHNfWd6-EY (discovered; full contents pending ingestion)
- FME LOOTCHEE OS Master Audit Index, id 1OefwmpbP8meMm0hzkPnfFb1gHmT5mQst (discovered; full contents pending ingestion)
- FME Master Backup Manifest, id 1KaDiQ8O50CEJPx35NPok1QWo4OKVKF91ENuXSoigwZw (discovered; full contents pending ingestion)

## System boundaries
- FME AI Core: local indexing, retrieval, source citations, governance, command routing, project and asset lookup, persona context compiler, task planner.
- Generation provider: replaceable local/remote model inference adapter; never falsely claim generation occurred.
- Independent FME Model: separate training and evaluation project; no claim of trained weights until trained and validated.
- Android: Demonic hosts AI rooms directly, with shared project/asset DB and permissioned background workers.

## Ingestion contract
1. Enumerate Drive and LOOTCHEE OS files, retaining immutable source IDs, title, MIME, revision/hash, provenance and permission metadata.
2. Deduplicate by hash and canon authority, without deleting originals.
3. Parse text, markdown, documents, audio transcripts and image metadata into chunks.
4. Attach canon status, entity registry, universe, era, source span and lineage to each chunk.
5. Index keyword + vector search locally; retain citations and per-source access controls.
6. Query -> retrieval -> canon conflict check -> generation provider -> cited response -> optional human-approved action.
7. Never train on assets without rights clearance; segregate private/sensitive data.

## Independent original AI model path
1. Establish clean-room dataset with license/consent records, source splits, contamination checks and holdout evals.
2. Build original tokenizer, compact decoder architecture, training pipeline, checkpoint format and reproducible evaluation harness.
3. Train a small baseline from initialized weights, benchmark against FME tasks, then scale only if quality and compute justify it.
4. Export quantized model to Android-supported inference runtime; compare memory, latency, hallucinations, retrieval fidelity and creative quality.
5. Keep model weights and dataset lineage versioned separately from app releases.

## Completion gates
A: real indexed corpus and source citations; B: operational FME AI inference and task tools; C: Android integrated UI and local tests; D: independent model baseline trained/evaluated; E: Android deployment; F: release QA. No percentage without verified tests.
