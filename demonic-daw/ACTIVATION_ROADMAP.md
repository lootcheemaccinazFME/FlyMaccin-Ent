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

## Architecture
FME Samples/Instruments -> Tracks -> Loops/Patterns -> Arrangement -> Mixer/Automation -> Master -> WAV/Stems

Recorded vocals and imported audio enter the same arrangement/mixer pipeline.

## Completion law
A feature is not complete because its UI exists. It must be wired to project state/audio behavior as applicable and pass build/runtime QA.
