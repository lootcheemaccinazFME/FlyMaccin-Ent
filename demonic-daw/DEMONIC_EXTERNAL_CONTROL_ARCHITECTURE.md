# Demonic External Control Architecture v1

Status: ACTIVE IMPLEMENTATION CONTRACT

## Routing law
Audio/MIDI channels are generic resources. No channel number is reserved, preferred, or defaulted by instrument category. Track/project routing state is authoritative.

## Security
Pairing is explicit and user initiated. Device identity uses an app-scoped installation UUID plus public device label. Pairing creates a cryptographically random session credential. Store secrets with Android Keystore-backed encrypted storage. Never place credentials in WebView localStorage, project JSON, logs, URLs, or exported files.

Sessions carry: id, deviceId, createdAt, expiresAt, lastSeenAt, scopes, clientName, revokedAt. Support reconnect within validity, explicit revoke, revoke-all, inactivity timeout, absolute lifetime, local-only mode, and connection status UI.

Scopes: READ, EDIT, RECORD, RENDER, FILE, PUBLISH. Deny by default. PUBLISH is never implied by another scope. Destructive file/project replacement requires FILE plus EDIT.

## Durable native storage
Canonical project data lives in app-private native storage, not WebView object URLs.
projects/<project-id>/
  project.json
  audio/
  recordings/
  renders/
  autosave/
  cache/
Imported content is copied or intentionally linked using Android URI permissions. Recordings are incrementally written to durable files. Project JSON stores stable asset IDs/paths, not blob/object URLs. Autosave is journaled and crash recoverable.

## Native render service
Offline render runs outside the realtime Oboe callback. Required jobs: mix, stems, selected range, track bounce, freeze, resample. Jobs have stable IDs, progress, cancel state, error state, output asset IDs, sample rate, channel count and format. First production target: WAV. Realtime playback and network/AI work never execute inside the audio callback.

## DCP transport
DCP request envelope:
{protocolVersion,id,command,args,expectedRevision,sessionId}
Response:
{ok,id,revision,result,error}
Mutations increment project revision. expectedRevision prevents stale writes. Transactions produce one history entry and rollback on failure.

Event stream includes project.changed, track.changed, clip.changed, mixer.changed, automation.changed, transport.changed, recording.started, recording.stopped, project.saved, render.progress, render.completed, render.failed, engine.underrun, session.revoked, device.disconnected.

Controller subscriptions can filter event types/project IDs. Touch/UI mutations and controller mutations publish through the same event bus.

## Capability negotiation
Native state is authoritative. Handshake reports protocolVersion, appVersion, engineVersion, deviceId, projectRevision, supportedCommands, supportedEvents, permissionScopes, installedFmePacks, audioCapabilities, MIDI capabilities, renderFormats and transport modes. Clients must not infer capabilities from app version.

## External endpoint
The Android app does not expose an unauthenticated LAN listener. External control is OFF until explicitly enabled. A secure gateway/relay or explicitly paired local transport maps authenticated requests to DCP. Pairing credentials are revocable and scoped.

The ChatGPT connector endpoint is an adapter, not the DAW engine. It authenticates the user/session, exposes allowed DCP operations as connector actions, validates schemas/scopes/revisions, forwards commands, subscribes to events, and returns structured state/results. It never enters realtime DSP.

## Required connector operations
device.status
session.pair
session.reconnect
session.revoke
session.revokeAll
capabilities.get
project.getState
project.open
project.save
project.saveAs
track.*
clip.*
midi.*
sampler.*
fme.*
mixer.*
fx.*
bus.*
automation.*
transport.*
recording.*
render.*
events.subscribe
events.unsubscribe

## Implementation order
1. Native SessionManager + Keystore credential storage.
2. Permission/scope enforcement before DCP dispatch.
3. Native ProjectStore + AssetStore and migration from localStorage/object URLs.
4. Native RecordingStore.
5. Native RenderService job model and WAV renderer.
6. Shared native event/state/revision bus.
7. Capability registry generated from native components.
8. Authenticated transport with pairing/revoke/local-only controls.
9. Connector service mapping ChatGPT actions to DCP.
10. Runtime, security, reconnect, storage, render and physical-device QA.

## Completion gates
No feature is called complete merely because UI exists. Security requires invalid-token, expired-token, revoked-token and scope-denial tests. Storage requires restart/reboot recovery. Render requires output validation. Transport requires reconnect and disconnect tests. Connector control requires a real authenticated round trip to a running device.
