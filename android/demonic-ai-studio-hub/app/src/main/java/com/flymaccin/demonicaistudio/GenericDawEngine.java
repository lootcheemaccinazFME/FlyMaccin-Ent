package com.flymaccin.demonicaistudio;

import org.json.JSONObject;

/** Native Generic DAW state adapter running inside Demonic's shared production session. */
final class GenericDawEngine {
    boolean metronome;
    boolean loopEnabled;
    long loopStartTick;
    long loopEndTick;
    int numerator = 4;

    String toJson() {
        try {
            return new JSONObject()
                    .put("version", 1)
                    .put("metronome", metronome)
                    .put("loop_enabled", loopEnabled)
                    .put("loop_start_tick", loopStartTick)
                    .put("loop_end_tick", loopEndTick)
                    .put("numerator", numerator)
                    .toString();
        } catch (Exception e) { return "{}"; }
    }

    static GenericDawEngine fromJson(String raw) {
        GenericDawEngine g = new GenericDawEngine();
        try {
            JSONObject o = new JSONObject(raw == null ? "{}" : raw);
            g.metronome = o.optBoolean("metronome", false);
            g.loopEnabled = o.optBoolean("loop_enabled", false);
            g.loopStartTick = Math.max(0, o.optLong("loop_start_tick", 0));
            g.loopEndTick = Math.max(g.loopStartTick, o.optLong("loop_end_tick", ProductionProject.PPQ * 4L));
            g.numerator = Math.max(1, Math.min(16, o.optInt("numerator", 4)));
        } catch (Exception ignored) {}
        return g;
    }

    void normalizeFor(ProductionProject p) {
        if (p == null) return;
        long projectEnd = Math.max(ProductionProject.PPQ * 4L, (long)p.bars * ProductionProject.PPQ * 4L);
        if (loopEndTick <= loopStartTick) loopEndTick = Math.min(projectEnd, loopStartTick + ProductionProject.PPQ * 4L);
        loopStartTick = Math.min(loopStartTick, Math.max(0, projectEnd - 1));
        loopEndTick = Math.max(loopStartTick + 1, Math.min(loopEndTick, projectEnd));
    }
}
