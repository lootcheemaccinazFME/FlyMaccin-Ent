# FME Gateway — Phases 1–17 Implementation Matrix

Build branch: `build/gateway-phases-15-17`
Target runtime version: `0.17.0-dev`

| Phase | Implementation in this branch | Production evidence required |
|---|---|---|
| 1 | Gateway core, command registry, durable job contract, 1821 invariant | Existing v0.1 verification retained |
| 2 | MCP/OAuth metadata contract with PKCE S256/scopes | Public OAuth/connector verification |
| 3 | Deployment/backup schema compatibility | Hosted deployment smoke |
| 4 | Device-session bridge contract | Live pairing/event verification |
| 5 | Ten-tool MCP contract + provider-result truth boundary | Asset/provider integration smoke |
| 6 | Production schema path prepared | Public HTTPS deployment |
| 7 | MCP production contract prepared | ChatGPT registration + scan |
| 8 | Device sync/session primitives | Physical Android E2E |
| 9 | Audit/provider-health compatibility | Production audit queries |
| 10 | Backup receipt compatibility | Restore drill |
| 11 | Release gate compatibility | External acceptance evidence |
| 12 | Provider runtime interface + normalized provenance + retry boundary | Real configured provider result |
| 13 | Sync decisions, session validation and cursors | Pocket Potna live result sync |
| 14 | Fail-closed production evidence evaluator | Certification evidence |
| 15 | Retry/idempotency/dead-letter migration and policy | Live failure/replay test |
| 16 | Multi-device sessions, cursors, sync receipts/conflict policy | Two-device reconnect test |
| 17 | Release manifests/deployment receipts/release evaluator | CI/deploy/rollback verification |

## Non-negotiable production gates
A production release remains BLOCKED until public MCP, connector auth, a genuine provider-backed command, production 1821, Pocket Potna result sync, physical Android E2E, verified backup, audit, privacy review and APK compatibility all have evidence.

## Source completion boundary
This branch implements the consolidated source contracts and acceptance tests for Phases 1–17. It does not convert external acceptance gates into fake passes. Production deployment, provider credentials, ChatGPT registration and physical-device execution must produce their own receipts.
