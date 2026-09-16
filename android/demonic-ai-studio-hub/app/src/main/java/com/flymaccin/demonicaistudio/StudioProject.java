package com.flymaccin.demonicaistudio;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

final class StudioProject {
    static final int STEPS = 16;
    static final int TRACK_PIANO = 0;
    static final int TRACK_GUITAR = 1;
    static final int TRACK_DRUMS = 2;
    static final int TRACK_VOICE = 3;
    static final int TRACK_COUNT = 4;

    String name = "Demonic Session";
    int bpm = 96;
    int swing = 0;
    boolean loop = true;
    final String[] piano = new String[STEPS];
    final String[] guitar = new String[STEPS];
    final boolean[][] drums = new boolean[4][STEPS];
    String voicePath = "";
    int voiceStartStep = 0;
    final float[] volume = {0.86f, 0.82f, 0.90f, 0.90f};
    final float[] pan = {0f, 0f, 0f, 0f};
    final boolean[] muted = new boolean[TRACK_COUNT];
    final boolean[] solo = new boolean[TRACK_COUNT];
    final boolean[] delay = new boolean[TRACK_COUNT];
    final boolean[] reverb = new boolean[TRACK_COUNT];

    StudioProject() {
        Arrays.fill(piano, "");
        Arrays.fill(guitar, "");
    }

    void addFrequency(int track, int step, double frequency) {
        String[] lane = track == TRACK_PIANO ? piano : guitar;
        String value = String.format(java.util.Locale.US, "%.3f", frequency);
        lane[normalizeStep(step)] = lane[normalizeStep(step)].isEmpty()
                ? value : lane[normalizeStep(step)] + "," + value;
    }

    void setFrequencies(int track, int step, double[] frequencies) {
        StringBuilder value = new StringBuilder();
        for (double frequency : frequencies) {
            if (value.length() > 0) value.append(',');
            value.append(String.format(java.util.Locale.US, "%.3f", frequency));
        }
        (track == TRACK_PIANO ? piano : guitar)[normalizeStep(step)] = value.toString();
    }

    double[] frequenciesAt(int track, int step) {
        String raw = (track == TRACK_PIANO ? piano : guitar)[normalizeStep(step)];
        if (raw == null || raw.isEmpty()) return new double[0];
        String[] values = raw.split(",");
        double[] frequencies = new double[values.length];
        for (int index = 0; index < values.length; index++) {
            try {
                frequencies[index] = Double.parseDouble(values[index]);
            } catch (NumberFormatException ignored) {
                frequencies[index] = 0;
            }
        }
        return frequencies;
    }

    boolean hasClip(int track, int step) {
        step = normalizeStep(step);
        if (track == TRACK_PIANO) return !piano[step].isEmpty();
        if (track == TRACK_GUITAR) return !guitar[step].isEmpty();
        if (track == TRACK_DRUMS) {
            for (boolean[] lane : drums) if (lane[step]) return true;
            return false;
        }
        return !voicePath.isEmpty() && voiceStartStep == step;
    }

    void clearClip(int track, int step) {
        step = normalizeStep(step);
        if (track == TRACK_PIANO) piano[step] = "";
        else if (track == TRACK_GUITAR) guitar[step] = "";
        else if (track == TRACK_DRUMS) for (boolean[] lane : drums) lane[step] = false;
        else if (track == TRACK_VOICE && voiceStartStep == step) voicePath = "";
    }

    void moveClip(int track, int from, int to) {
        from = normalizeStep(from);
        to = normalizeStep(to);
        if (from == to || !hasClip(track, from)) return;
        if (track == TRACK_PIANO) {
            piano[to] = piano[from];
            piano[from] = "";
        } else if (track == TRACK_GUITAR) {
            guitar[to] = guitar[from];
            guitar[from] = "";
        } else if (track == TRACK_DRUMS) {
            for (boolean[] lane : drums) {
                lane[to] = lane[from];
                lane[from] = false;
            }
        } else if (track == TRACK_VOICE) {
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
            root.put("name", name);
            root.put("bpm", bpm);
            root.put("swing", swing);
            root.put("loop", loop);
            root.put("piano", new JSONArray(Arrays.asList(piano)));
            root.put("guitar", new JSONArray(Arrays.asList(guitar)));
            JSONArray drumArray = new JSONArray();
            for (boolean[] lane : drums) {
                JSONArray values = new JSONArray();
                for (boolean value : lane) values.put(value);
                drumArray.put(values);
            }
            root.put("drums", drumArray);
            root.put("voicePath", voicePath);
            root.put("voiceStartStep", voiceStartStep);
            root.put("volume", floats(volume));
            root.put("pan", floats(pan));
            root.put("muted", booleans(muted));
            root.put("solo", booleans(solo));
            root.put("delay", booleans(delay));
            root.put("reverb", booleans(reverb));
            return root.toString();
        } catch (Exception ignored) {
            return "{}";
        }
    }

    static StudioProject fromJson(String raw) {
        StudioProject project = new StudioProject();
        if (raw == null || raw.isEmpty()) return project;
        try {
            JSONObject root = new JSONObject(raw);
            project.name = root.optString("name", project.name);
            project.bpm = clamp(root.optInt("bpm", project.bpm), 50, 190);
            project.swing = clamp(root.optInt("swing", 0), 0, 35);
            project.loop = root.optBoolean("loop", true);
            readStrings(root.optJSONArray("piano"), project.piano);
            readStrings(root.optJSONArray("guitar"), project.guitar);
            JSONArray drumArray = root.optJSONArray("drums");
            if (drumArray != null) {
                for (int lane = 0; lane < Math.min(4, drumArray.length()); lane++) {
                    JSONArray values = drumArray.optJSONArray(lane);
                    if (values != null) for (int step = 0; step < Math.min(STEPS, values.length()); step++)
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
        }
        return project;
    }

    private static JSONArray floats(float[] values) {
        JSONArray array = new JSONArray();
        for (float value : values) {
            try { array.put((double) value); } catch (Exception ignored) { }
        }
        return array;
    }

    private static JSONArray booleans(boolean[] values) {
        JSONArray array = new JSONArray();
        for (boolean value : values) array.put(value);
        return array;
    }

    private static void readStrings(JSONArray array, String[] target) {
        if (array == null) return;
        for (int index = 0; index < Math.min(array.length(), target.length); index++)
            target[index] = array.optString(index, "");
    }

    private static void readFloats(JSONArray array, float[] target, float low, float high) {
        if (array == null) return;
        for (int index = 0; index < Math.min(array.length(), target.length); index++)
            target[index] = Math.max(low, Math.min(high, (float) array.optDouble(index, target[index])));
    }

    private static void readBooleans(JSONArray array, boolean[] target) {
        if (array == null) return;
        for (int index = 0; index < Math.min(array.length(), target.length); index++)
            target[index] = array.optBoolean(index, target[index]);
    }

    private static int normalizeStep(int step) {
        return ((step % STEPS) + STEPS) % STEPS;
    }

    private static int clamp(int value, int low, int high) {
        return Math.max(low, Math.min(high, value));
    }
}
