# Unified Build Progress

Branch: `codex/pocket-potna-demonic-unified-core-20261002`

## Implemented shared production spine
- Unified 960 PPQ transport with BPM, time signature, snap and loop persistence.
- Loop presets: 4 / 8 / 16 / 24 / 38 / 48 bars and Unlimited.
- `.fmefun` schema v3 as the canonical session document.
- Persistent clips, transport, viewport, markers, MIDI notes + CC, 8x32 drum pattern data, mixer channels, 4 buses, sends, routing, mute/solo, record arm, monitoring, sampler maps and waveform metadata.
- Draggable clips, timeline scrolling, pinch zoom and real cached waveform peak drawing.
- Piano-roll note create/move and MIDI instrument preview.
- Drum/tracker touch editing with velocity, probability, micro-shift, repeat, pitch and swing.
- 16-pad sampler mappings with pitch, gain and pan.
- 8-track mixer with gain/pan, mute/solo, arm/monitor, routing and four sends.
- Undo/redo command stack wired to clip moves, MIDI edits, drum toggles, mixer fader/pan/send edits and arm/monitor.
- Live scheduler for MIDI, drum patterns, sample pads and audio clip launch.
- Capture engine gated by record arm with optional monitoring; recorded WAV becomes a timeline clip at record-start position.
- Offline render graph: clips + MIDI + drums + sampler -> tracks -> buses/sends -> master.
- Master and track/bus stem WAV export.
- StudioActivity migrated from legacy StudioProject ownership to SessionRuntime/FmeFunProject.
- Autosave and restore through atomic `.fmefun` writes.

## Validation note
GitHub currently reports no workflow/status checks for this branch, so automated CI validation is not yet available. Source-level integration audit completed after migration.
