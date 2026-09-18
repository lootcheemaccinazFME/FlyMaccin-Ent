# Demonic DAW Activation Roadmap
Status: ACTIVE / IN DEVELOPMENT

## Core completion gates
Independent instrument/sample channels; FME multi-piece drum mapping; audio/MIDI arrangement timeline; full mixer/EQ/sends/FX; full piano roll; Recording Studio completion; project New/Save/Save As/Open/recovery; WAV mix and stem export; DD1/DD2 modes; undo/redo; rotation and physical tablet QA.

## Production systems
- Track management: add/delete/duplicate/rename/reorder, track types, instrument assignment, labels, freeze/bounce.
- Transport: stop/pause/rewind, playhead scrubbing, bar/beat counter, loop region, count-in, metronome, tempo changes.
- Editing: copy/cut/paste, multi-select, snap/grid, duplicate regions, split/join, crop/time positioning.
- Automation: volume, pan, sends, FX and instrument parameters across timeline.
- Audio clips: fades, gain, trim, looping, waveform thumbnails, non-destructive edits.
- Recording: take folders/lanes, arm, input selection, latency handling, monitoring, punch-in/out, countdown.
- Samples: favorites, categories, recent, preview, import folders, missing files, FME pack install/removal.
- Instruments: ADSR, tuning, octave, velocity response, sample start/end, filtering.
- Drums: per-pad sample, velocity, choke groups, tuning, pan, level, pad replacement.
- MIDI: external MIDI/Bluetooth input, note recording, velocity, CC, quantization.
- Mixer/Master: volume/pan/mute/solo, sends, EQ/FX, meters, limiter, master FX, clipping indication.
- Song settings: BPM, time signature, sample rate, later tempo automation.
- File/recovery: autosave versions, crash recovery, missing assets, backup/import/export.
- Import/Export: full mix, selected range, normalize, WAV, stems, MP3/AAC where appropriate.
- Performance: low latency, buffer stability, CPU/memory management, large-project and voice-limit tests.
- Tablet UI: landscape, pinch zoom, scrolling, long-press/context controls, large touch targets.
- Accessibility/reliability: selected states, destructive confirmations, useful errors, permission recovery, storage warnings.

## Full production completion scope
1. Native audio engine completion: per-channel gain/pan/mute/solo; simultaneous SF2 + SFZ/WAV; safe polyphony/voice management; choke groups; latency/buffer controls; master summing.
2. Full piano roll: arbitrary note length; drag/move/resize; velocity; octave range; multi-select; copy/paste; quantize; snap; overlapping/sustained notes.
3. Track system: instrument, drum, MIDI, vocal and audio tracks; add/delete/duplicate/rename/reorder; instrument assignment; freeze/bounce; persistent state.
4. Arrangement editor: loops, MIDI clips, vocals and imported WAVs on one timeline; move/resize/repeat/split/join/duplicate/delete/layer.
5. Recording Studio: arm tracks; input selection; monitoring; waveform recording; pause/resume; multiple takes; overdubs; punch-in/out; count-in; latency compensation; take management.
6. Audio editing: waveform display; trim/crop/split; fades; clip gain; looping; non-destructive edits.
7. Mixer: channel strips; meters; volume/pan/mute/solo; EQ; inserts; sends/returns; FX; automation; master bus.
8. Effects: EQ, compressor, reverb, delay, limiter and basic filtering first.
9. Automation: timeline automation for volume, pan, sends, FX parameters and instrument controls.
10. Transport/song engine: play/pause/stop/rewind; seek/scrub; loop regions; bar/beat; BPM; time signature; metronome; count-in; later tempo automation.
11. Sample/pack manager: FME categories; search; favorites; recents; preview; per-pad assignment; custom folders; SF2/SFZ/WAV import; pack install/remove; missing-file recovery.
12. Project system: New/Save/Save As/Open/rename; autosave; recovery versions; backup/import/export; full restoration of tracks/clips/mixer/instruments.
13. Undo/redo: reversible command/history stack for editing operations.
14. Import/export: WAV/audio to real tracks; full-song WAV; selected-range render; normalization; stems; later MP3/AAC.
15. DD1/DD2: DD1 fast PocketBand-style loop workflow; DD2 deeper arrangement/mixer/automation environment; shared project/audio core.
16. MIDI/controller support: USB/Bluetooth MIDI; live note input; velocity; CC; recording; mapping; controller transport.
17. Tablet/touch completion: portrait/landscape; pinch zoom; timeline scrolling; large controls; drag handles; long-press/context actions; responsive layouts.
18. Performance/reliability: CPU/RAM limits; large projects; polyphony; underrun protection; storage failures; permission recovery; missing samples; crash recovery.
19. Real runtime QA: automated build/startup/audio-state tests plus physical tablet touch/rotation/mic/headphones/Bluetooth/latency/recording/simultaneous-playback/save-reopen/export tests.
20. Release packaging: versioning; signed APK/AAB; clean-install and upgrade testing; final known-good release artifact.

## Extended DAW architecture scope
- Clip launcher/live mode: loops, scenes, one-shots, quantized launch, scene chaining, capture to arrangement.
- Audio-to-MIDI: transient-to-drums, monophonic pitch-to-MIDI, rhythm extraction.
- Audio analysis: BPM/key/transient/silence detection, waveform peak cache, sample classification.
- Vocal production: gate, de-esser, compressor, EQ, practical pitch correction, doubling, take alignment, vocal presets.
- Bus/master processing: parallel compression, grouped buses, reference playback, A/B comparison.
- Sidechain: compressor sidechain, ducking, envelope-following modulation, selectable triggers.
- Modulation: LFOs, envelopes, macros and parameter mapping.
- Advanced automation: curves, points, ramps, copy/paste, automation clips, write/touch/latch and reset.
- Track folders/groups, linked clips/make-unique, track versions, arrangement snapshots and reference/version history.
- Sample recording, resampling, reverse/bounce-in-place and crossfades.
- Audio normalization/analysis: peak normalization, DC-offset handling and clip statistics.
- Tuning plus scale/chord workflow.
- Drum-pad performance: touch velocity alternatives, banks, note repeat, rolls and configurable layouts.
- MPE/expressive MIDI and MIDI clock/sync where platform support permits.
- External audio interfaces: available I/O, mono/stereo selection and device-specific latency where Android permits.
- Recording safety: pre-record buffer, automatic take recovery and storage checks.
- Offline-first library: production, projects, FME packs, recording, editing and export require no network.
- Portable project package with optional collected assets.
- Android share workflow for mixes, stems and project packages.
- Metadata/export tagging and batch stem rendering.
- Render validation: clipping, missing samples, muted master, zero-length and failed-file detection.
- Thermal/battery handling under sustained load.
- Audio-thread safety: no disk I/O or unsafe realtime allocation/locking; safe UI/storage-to-audio communication.
- Deterministic project state across save/reopen.
- Testing infrastructure: serialization, edit-command, native audio, render, migration and reproducible demo-project tests.
- Recovery/safe mode for damaged projects or failing effects/assets.
- Contextual onboarding/help.
- Customizable workspace persisted per DD1/DD2 state.
- Accessibility: scalable controls/text, high contrast, reduced animation, configurable touch sensitivity.
- Localization architecture with UI strings separated from engine logic.

## Architecture
FME Samples/Instruments -> Tracks -> Loops/Patterns -> Arrangement -> Mixer/Automation -> Master -> WAV/Stems

Recorded vocals and imported audio enter the same arrangement/mixer pipeline.

## Completion law
A feature is not complete because its UI exists. It must be wired to project state/audio behavior as applicable and pass build/runtime QA.
