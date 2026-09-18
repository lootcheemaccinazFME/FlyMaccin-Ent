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

## Architecture
FME Samples/Instruments -> Tracks -> Loops/Patterns -> Arrangement -> Mixer/Automation -> Master -> WAV/Stems

Recorded vocals and imported audio enter the same arrangement/mixer pipeline.

## Completion law
A feature is not complete because its UI exists. It must be wired to project state/audio behavior as applicable and pass build/runtime QA.
