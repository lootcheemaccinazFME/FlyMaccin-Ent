package com.flymaccin.demonicaistudio;

import org.json.JSONArray;
import org.json.JSONObject;

/** The complete, serializable state of one 16-step studio session. */
final class StudioProject {
    static final int STEPS = 16;
    static final int TRACK_PIANO = 0;
    static final int TRACK_GUITAR = 1;
    static final int TRACK_DRUMS = 2;
    static final int TRACK_VOICE = 3;
    static final int TRACK_COUNT = 4;
    private static final int MAX_NOTES_PER_STEP = 16;

    String name = "Demonic Session";
    int bpm = 96;
    int swing;
    boolean loop = true;
    final int[][] pianoMidi = new int[STEPS][MAX_NOTES_PER_STEP];
    final int[][] guitarMidi = new int[STEPS][MAX_NOTES_PER_STEP];
    final int[] pianoNoteCount = new int[STEPS];
    final int[] guitarNoteCount = new int[STEPS];
    final boolean[][] drums = new boolean[4][STEPS];
    String voicePath = "";
    int voiceStartStep;
    final float[] volume = {0.86f, 0.82f, 0.90f, 0.90f};
    final float[] pan = new float[TRACK_COUNT];
    final boolean[] muted = new boolean[TRACK_COUNT];
    final boolean[] solo = new boolean[TRACK_COUNT];
    final boolean[] delay = new boolean[TRACK_COUNT];
    final boolean[] reverb = new boolean[TRACK_COUNT];

    void addMidi(int track, int step, int midi) {
        int[][] notes = notesFor(track);
        int[] counts = countsFor(track);
        step = normalizeStep(step);
        midi = clamp(midi, 0, 127);
        for (int i = 0; i < counts[step]; i++) if (notes[step][i] == midi) return;
        if (counts[step] < MAX_NOTES_PER_STEP) notes[step][counts[step]++] = midi;
    }

    void setMidiNotes(int track, int step, int[] values) {
        int[][] notes = notesFor(track);
        int[] counts = countsFor(track);
        step = normalizeStep(step);
        counts[step] = 0;
        if (values == null) return;
        for (int value : values) addMidi(track, step, value);
    }

    int[] midiNotesAt(int track, int step) {
        int[] notes = notesFor(track)[normalizeStep(step)];
        int count = countsFor(track)[normalizeStep(step)];
        int[] result = new int[count];
        System.arraycopy(notes, 0, result, 0, count);
        return result;
    }

    boolean hasClip(int track, int step) {
        step = normalizeStep(step);
        if (track == TRACK_PIANO || track == TRACK_GUITAR) return countsFor(track)[step] > 0;
        if (track == TRACK_DRUMS) {
            for (boolean[] lane : drums) if (lane[step]) return true;
            return false;
        }
        return !voicePath.isEmpty() && voiceStartStep == step;
    }

    void clearClip(int track, int step) {
        step = normalizeStep(step);
        if (track == TRACK_PIANO || track == TRACK_GUITAR) {
            countsFor(track)[step] = 0;
        } else if (track == TRACK_DRUMS) {
            for (boolean[] lane : drums) lane[step] = false;
        } else if (track == TRACK_VOICE && voiceStartStep == step) {
            voicePath = "";
        }
    }

    void moveClip(int track, int from, int to) {
        from = normalizeStep(from);
        to = normalizeStep(to);
        if (from == to || !hasClip(track, from)) return;
        if (track == TRACK_PIANO || track == TRACK_GUITAR) {
            int[][] notes = notesFor(track);
            int[] counts = countsFor(track);
            System.arraycopy(notes[from], 0, notes[to], 0, MAX_NOTES_PER_STEP);
            counts[to] = counts[from];
            counts[from] = 0;
        } else if (track == TRACK_DRUMS) {
            for (boolean[] lane : drums) {
                lane[to] = lane[from];
                lane[from] = false;
            }
        } else {
            voiceStartStep = to;
        }
    }

    void copyClipTo(StudioProject source, int track, int from, int to) {
        from = normalizeStep(from);
        to = normalizeStep(to);
        if (track == TRACK_PIANO || track == TRACK_GUITAR) {
            setMidiNotes(track, to, source.midiNotesAt(track, from));
        } else if (track == TRACK_DRUMS) {
            for (int lane = 0; lane < drums.length; lane++) drums[lane][to] = source.drums[lane][from];
        } else if (!source.voicePath.isEmpty() && source.voiceStartStep == from) {
            voicePath = source.voicePath;
            voiceStartStep = to;
        }
    }

    boolean trackAudible(int track) {
        boolean anySolo = false;
        for (boolean value : solo) anySolo |= value;
        return !muted[track] && (!anySolo || solo[track]);
    }

    String toJson() {
        try {
            JSONObject root = new JSONObject();
            root.put("schema", 2);
            root.put("name", name);
            root.put("bpm", bpm);
            root.put("swing", swing);
            root.put("loop", loop);
            root.put("pianoMidi", noteGrid(pianoMidi, pianoNoteCount));
            root.put("guitarMidi", noteGrid(guitarMidi, guitarNoteCount));
            JSONArray drumGrid = new JSONArray();
            for (boolean[] lane : drums) {
                JSONArray values = new JSONArray();
                for (boolean value : lane) values.put(value);
                drumGrid.put(values);
            }
            root.put("drums", drumGrid);
            root.put("voicePath", voicePath);
            root.put("voiceStartStep", voiceStartStep);
            root.put("volume", floatArray(volume));
            root.put("pan", floatArray(pan));
            root.put("muted", booleanArray(muted));
            root.put("solo", booleanArray(solo));
            root.put("delay", booleanArray(delay));
            root.put("reverb", booleanArray(reverb));
            return root.toString();
        } catch (Exception ignored) {
            return "{}";
        }
    }

    static StudioProject fromJson(String json) {
        StudioProject project = new StudioProject();
        if (json == null || json.trim().isEmpty()) return project;
        try {
            JSONObject root = new JSONObject(json);
            project.name = root.optString("name", project.name);
            project.bpm = clamp(root.optInt("bpm", project.bpm), 50, 190);
            project.swing = clamp(root.optInt("swing", 0), 0, 35);
            project.loop = root.optBoolean("loop", true);
            JSONArray piano = root.optJSONArray("pianoMidi");
            JSONArray guitar = root.optJSONArray("guitarMidi");
            if (piano != null) readNoteGrid(piano, project.pianoMidi, project.pianoNoteCount);
            else readLegacyFrequencyGrid(root.optJSONArray("piano"), project, TRACK_PIANO);
            if (guitar != null) readNoteGrid(guitar, project.guitarMidi, project.guitarNoteCount);
            else readLegacyFrequencyGrid(root.optJSONArray("guitar"), project, TRACK_GUITAR);

            JSONArray drumGrid = root.optJSONArray("drums");
            if (drumGrid != null) {
                for (int lane = 0; lane < Math.min(4, drumGrid.length()); lane++) {
                    JSONArray values = drumGrid.optJSONArray(lane);
                    if (values == null) continue;
                    for (int step = 0; step < Math.min(STEPS, values.length()); step++)
                        project.drums[lane][step] = values.optBoolean(step, false);
                }
            }
            project.voicePath = root.optString("voicePath", "");
            project.voiceStartStep = normalizeStep(root.optInt("voiceStartStep", 0));
            readFloats(root.optJSONArray("volume"), project.volume, 0f, 1f);
            readFloats(root.optJSONArray("pan"), project.pan, -1f, 1f);
            readBooleans(root.optJSONArray("muted"), project.muted);
            readBooleans(root.optJSONArray("solo"), project.solo);
            readBooleans(root.optJSONArray("delay"), project.delay);
            readBooleans(root.optJSONArray("reverb"), project.reverb);
        } catch (Exception ignored) {
            return new StudioProject();
        }
        return project;
    }

    static int normalizeStep(int step) {
        return ((step % STEPS) + STEPS) % STEPS;
    }

    static double frequencyForMidi(int midi) {
        return 440.0 * Math.pow(2.0, (midi - 69) / 12.0);
    }

    private int[][] notesFor(int track) {
        if (track == TRACK_PIANO) return pianoMidi;
        if (track == TRACK_GUITAR) return guitarMidi;
        throw new IllegalArgumentException("Track does not contain pitched notes");
    }

    private int[] countsFor(int track) {
        if (track == TRACK_PIANO) return pianoNoteCount;
        if (track == TRACK_GUITAR) return guitarNoteCount;
        throw new IllegalArgumentException("Track does not contain pitched notes");
    }

    private static JSONArray noteGrid(int[][] notes, int[] counts) {
        JSONArray grid = new JSONArray();
        for (int step = 0; step < STEPS; step++) {
            JSONArray values = new JSONArray();
            for (int note = 0; note < counts[step]; note++) values.put(notes[step][note]);
            grid.put(values);
        }
        return grid;
    }

    private static void readNoteGrid(JSONArray grid, int[][] notes, int[] counts) {
        for (int step = 0; step < Math.min(STEPS, grid.length()); step++) {
            JSONArray values = grid.optJSONArray(step);
            if (values == null) continue;
            for (int index = 0; index < Math.min(MAX_NOTES_PER_STEP, values.length()); index++) {
                int midi = values.optInt(index, -1);
                if (midi >= 0 && midi <= 127) notes[step][counts[step]++] = midi;
            }
        }
    }

    private static void readLegacyFrequencyGrid(JSONArray grid, StudioProject project, int track) {
        if (grid == null) return;
        for (int step = 0; step < Math.min(STEPS, grid.length()); step++) {
            String[] values = grid.optString(step, "").split(",");
            for (String value : values) {
                try {
                    double frequency = Double.parseDouble(value);
                    if (frequency > 0 && !Double.isNaN(frequency) && !Double.isInfinite(frequency)) {
                        int midi = (int) Math.round(69 + 12 * (Math.log(frequency / 440.0) / Math.log(2)));
                        project.addMidi(track, step, midi);
                    }
                } catch (NumberFormatException ignored) {
                    // Skip malformed note data and keep the rest of the project.
                }
            }
        }
    }

    private static JSONArray floatArray(float[] values) {
        JSONArray result = new JSONArray();
        for (float value : values) {
            try {
                result.put((double) value);
            } catch (Exception ignored) {
                // A primitive float is always JSON-serializable.
            }
        }
        return result;
    }

    private static JSONArray booleanArray(boolean[] values) {
        JSONArray result = new JSONArray();
        for (boolean value : values) result.put(value);
        return result;
    }

    private static void readFloats(JSONArray array, float[] target, float min, float max) {
        if (array == null) return;
        for (int index = 0; index < Math.min(array.length(), target.length); index++)
            target[index] = Math.max(min, Math.min(max, (float) array.optDouble(index, target[index])));
    }

    private static void readBooleans(JSONArray array, boolean[] target) {
        if (array == null) return;
        for (int index = 0; index < Math.min(array.length(), target.length); index++)
            target[index] = array.optBoolean(index, target[index]);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
