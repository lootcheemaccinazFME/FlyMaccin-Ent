package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.os.Environment;
import java.io.*;
import java.util.*;

final class FmeFunRenderer {
    static final int RATE = 44100;

    static final class RenderGraph {
        final float[][] tracks;
        final float[][] buses;
        final float[] master;
        RenderGraph(int trackCount, int busCount, int frames) {
            tracks = new float[trackCount][frames * 2];
            buses = new float[busCount][frames * 2];
            master = new float[frames * 2];
        }
    }

    static final class ExportResult {
        final File master;
        final List<File> stems;
        ExportResult(File master, List<File> stems) { this.master = master; this.stems = stems; }
    }

    RenderGraph renderGraph(FmeFunProject p) throws Exception {
        long endTick = projectEnd(p);
        int frames = Math.max(RATE, (int)Math.min(Integer.MAX_VALUE / 4L,
                Math.ceil(p.transport.millisForTicks(endTick) / 1000.0 * RATE) + RATE));
        RenderGraph g = new RenderGraph(p.mixer.channels.length, p.mixer.buses.length, frames);

        renderAudioClips(p, g);
        renderMidi(p, g);
        renderDrumsAndSamples(p, g);
        routeMixer(p, g);
        normalize(g.master);
        return g;
    }

    ExportResult export(Context context, FmeFunProject p, boolean stems) throws Exception {
        RenderGraph g = renderGraph(p);
        File root = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "FMEFunExports");
        if (!root.exists() && !root.mkdirs()) throw new IOException("Cannot create export directory");
        String stamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        String base = safe(p.name);
        File master = new File(root, base + "_MASTER_" + stamp + ".wav");
        writeWav(master, g.master);
        List<File> out = new ArrayList<>();
        if (stems) {
            for (int i = 0; i < g.tracks.length; i++) {
                float[] copy = g.tracks[i].clone();
                normalize(copy);
                File f = new File(root, base + "_TRACK_" + (i + 1) + "_" + stamp + ".wav");
                writeWav(f, copy);
                out.add(f);
            }
            for (int i = 0; i < g.buses.length; i++) {
                float[] copy = g.buses[i].clone();
                normalize(copy);
                File f = new File(root, base + "_BUS_" + (i + 1) + "_" + stamp + ".wav");
                writeWav(f, copy);
                out.add(f);
            }
        }
        return new ExportResult(master, out);
    }

    private static long projectEnd(FmeFunProject p) {
        long end = p.transport.looping() ? p.transport.loopEnd() : p.transport.ticksPerBar() * 4L;
        for (FmeFunProject.Clip c : p.clips) end = Math.max(end, c.startTick + c.lengthTicks);
        for (MidiClip.Note n : p.midi.notes) end = Math.max(end, n.startTick + n.durationTicks);
        end = Math.max(end, p.drums.stepCount() * (UnifiedTransport.PPQ / 4L));
        return Math.max(end, p.transport.ticksPerBar());
    }

    private static void renderAudioClips(FmeFunProject p, RenderGraph g) throws Exception {
        for (FmeFunProject.Clip c : p.clips) {
            if (c.muted || !"audio".equals(c.type) || c.source.isEmpty()) continue;
            if (c.track < 0 || c.track >= g.tracks.length) continue;
            float[] mono = readMonoWav(new File(c.source));
            if (mono.length == 0) continue;
            addMono(g.tracks[c.track], mono, frame(p, c.startTick), c.gain, c.pan, c.offsetFrames);
        }
    }

    private static void renderMidi(FmeFunProject p, RenderGraph g) {
        if (g.tracks.length == 0) return;
        for (MidiClip.Note n : p.midi.notes) {
            float channelGain = latestCc(p.midi, n.channel, 7, n.startTick, 127) / 127f;
            float channelPan = (latestCc(p.midi, n.channel, 10, n.startTick, 64) - 64) / 64f;
            float[] voice = synthMidi(n.pitch, n.durationTicks, p);
            addMono(g.tracks[0], voice, frame(p, n.startTick), (n.velocity / 127f) * channelGain, channelPan, 0);
        }
    }

    private static int latestCc(MidiClip clip, int channel, int controller, long tick, int fallback) {
        long best = -1;
        int value = fallback;
        for (MidiClip.CC cc : clip.cc) {
            if (cc.channel == channel && cc.controller == controller && cc.tick <= tick && cc.tick >= best) {
                best = cc.tick;
                value = cc.value;
            }
        }
        return value;
    }

    private static void renderDrumsAndSamples(FmeFunProject p, RenderGraph g) throws Exception {
        if (g.tracks.length == 0) return;
        int track = Math.min(2, g.tracks.length - 1);
        long stepTicks = UnifiedTransport.PPQ / 4L;
        Random deterministic = new Random(0xF3E5L);
        for (int s = 0; s < p.drums.stepCount(); s++) {
            long swingOffset = (s % 2 == 1) ? Math.round(stepTicks * (p.drums.swing() / 100f) * 0.5f) : 0;
            for (int lane = 0; lane < p.drums.laneCount(); lane++) {
                DrumPattern.Step st = p.drums.step(lane, s);
                if (!st.active || deterministic.nextInt(100) >= st.probability) continue;
                int repeats = Math.max(1, st.repeat);
                FmeFunProject.SampleMap map = findSample(p, lane);
                float[] source;
                float gain;
                float pan;
                if (map != null && !map.source.isEmpty()) {
                    source = readMonoWav(new File(map.source));
                    source = resample(source, (float)Math.pow(2, (map.pitch + st.pitch) / 12f));
                    gain = map.gain * st.velocity / 127f;
                    pan = map.pan;
                } else {
                    source = synthDrum(lane, st.pitch);
                    gain = st.velocity / 127f;
                    pan = 0f;
                }
                for (int r = 0; r < repeats; r++) {
                    long tick = s * stepTicks + swingOffset + st.microShiftTicks + (stepTicks * r / repeats);
                    addMono(g.tracks[track], source, frame(p, Math.max(0, tick)), gain, pan, 0);
                }
            }
        }
    }

    private static FmeFunProject.SampleMap findSample(FmeFunProject p, int pad) {
        for (FmeFunProject.SampleMap s : p.samples) if (s.pad == pad) return s;
        return null;
    }

    private static void routeMixer(FmeFunProject p, RenderGraph g) {
        boolean anySolo = false;
        for (MixerGraph.Channel c : p.mixer.channels) if (c.solo) { anySolo = true; break; }

        for (int t = 0; t < g.tracks.length; t++) {
            MixerGraph.Channel ch = p.mixer.channels[t];
            if (ch.mute || (anySolo && !ch.solo)) continue;
            applyGainPan(g.tracks[t], ch.gain, ch.pan);
            int bus = Math.max(0, Math.min(g.buses.length - 1, ch.bus));
            sum(g.buses[bus], g.tracks[t], 1f);
            for (int b = 0; b < Math.min(ch.sends.length, g.buses.length); b++) {
                if (ch.sends[b] > 0f) sum(g.buses[b], g.tracks[t], ch.sends[b]);
            }
        }
        for (int b = 0; b < g.buses.length; b++) {
            MixerGraph.Channel bus = p.mixer.buses[b];
            if (bus.mute) continue;
            applyGainPan(g.buses[b], bus.gain, bus.pan);
            sum(g.master, g.buses[b], 1f);
        }
    }

    private static int frame(FmeFunProject p, long tick) {
        return Math.max(0, (int)Math.round(p.transport.millisForTicks(tick) / 1000.0 * RATE));
    }

    private static float[] synthMidi(int midi, long ticks, FmeFunProject p) {
        double hz = 440.0 * Math.pow(2.0, (midi - 69) / 12.0);
        int count = Math.max(64, (int)Math.round(p.transport.millisForTicks(ticks) / 1000.0 * RATE));
        float[] out = new float[count];
        for (int i = 0; i < count; i++) {
            double t = i / (double)RATE;
            double duration = count / (double)RATE;
            double env = Math.min(1, t * 45) * Math.max(0, 1 - t / duration);
            out[i] = (float)((Math.sin(2 * Math.PI * hz * t) + .22 * Math.sin(4 * Math.PI * hz * t)) * env * .62);
        }
        return out;
    }

    private static float[] synthDrum(int lane, float semitones) {
        int ms = lane % 4 == 0 ? 360 : lane % 4 == 1 ? 220 : lane % 4 == 2 ? 110 : 170;
        int count = RATE * ms / 1000;
        float[] out = new float[count];
        Random random = new Random(7000L + lane);
        double pitch = Math.pow(2, semitones / 12.0);
        double phase = 0;
        for (int i = 0; i < count; i++) {
            double t = i / (double)RATE;
            double env = Math.exp(-t * (lane % 4 == 0 ? 15 : lane % 4 == 1 ? 25 : 42));
            if (lane % 4 == 0) {
                double hz = (160 - 112 * Math.min(1, t * 8)) * pitch;
                phase += 2 * Math.PI * hz / RATE;
                out[i] = (float)(Math.sin(phase) * env);
            } else if (lane % 4 == 1) out[i] = (float)(((random.nextDouble()*2-1)*.82 + Math.sin(2*Math.PI*185*pitch*t)*.26)*env);
            else if (lane % 4 == 2) out[i] = (float)((random.nextDouble()*2-1)*env*.52);
            else out[i] = (float)((Math.sin(2*Math.PI*520*pitch*t)*.52+(random.nextDouble()*2-1)*.16)*env);
        }
        return out;
    }

    private static float[] readMonoWav(File f) throws Exception {
        if (!f.exists() || f.length() <= 44) return new float[0];
        try (RandomAccessFile in = new RandomAccessFile(f, "r")) {
            in.seek(44);
            int samples = (int)Math.min(Integer.MAX_VALUE - 8, (in.length() - 44) / 2);
            float[] out = new float[samples];
            for (int i = 0; i < samples && in.getFilePointer() + 1 < in.length(); i++) {
                int lo = in.readUnsignedByte(), hi = in.readByte();
                out[i] = ((short)((hi << 8) | lo)) / 32768f;
            }
            return out;
        }
    }

    private static float[] resample(float[] src, float rate) {
        if (src.length == 0 || Math.abs(rate - 1f) < .001f) return src;
        rate = Math.max(.25f, Math.min(4f, rate));
        int n = Math.max(1, Math.round(src.length / rate));
        float[] out = new float[n];
        for (int i = 0; i < n; i++) {
            float pos = i * rate;
            int a = Math.min(src.length - 1, (int)pos);
            int b = Math.min(src.length - 1, a + 1);
            float f = pos - a;
            out[i] = src[a] + (src[b] - src[a]) * f;
        }
        return out;
    }

    private static void addMono(float[] target, float[] source, int frameOffset, float gain, float pan, long sourceOffsetFrames) {
        float left = gain * (pan <= 0 ? 1f : 1f - pan);
        float right = gain * (pan >= 0 ? 1f : 1f + pan);
        int src = (int)Math.max(0, Math.min(source.length, sourceOffsetFrames));
        for (int frame = frameOffset; frame < target.length / 2 && src < source.length; frame++, src++) {
            target[frame * 2] += source[src] * left;
            target[frame * 2 + 1] += source[src] * right;
        }
    }

    private static void applyGainPan(float[] x, float gain, float pan) {
        float left = gain * (pan <= 0 ? 1f : 1f - pan);
        float right = gain * (pan >= 0 ? 1f : 1f + pan);
        for (int i = 0; i + 1 < x.length; i += 2) { x[i] *= left; x[i + 1] *= right; }
    }

    private static void sum(float[] dst, float[] src, float gain) {
        for (int i = 0; i < Math.min(dst.length, src.length); i++) dst[i] += src[i] * gain;
    }

    private static void normalize(float[] x) {
        float peak = 0f;
        for (float v : x) peak = Math.max(peak, Math.abs(v));
        if (peak > .98f) { float g = .98f / peak; for (int i = 0; i < x.length; i++) x[i] *= g; }
    }

    private static void writeWav(File file, float[] stereo) throws Exception {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
            int dataBytes = stereo.length * 2;
            out.write(new byte[]{'R','I','F','F'}); writeInt(out, 36 + dataBytes);
            out.write(new byte[]{'W','A','V','E','f','m','t',' '}); writeInt(out, 16);
            writeShort(out, 1); writeShort(out, 2); writeInt(out, RATE); writeInt(out, RATE * 4);
            writeShort(out, 4); writeShort(out, 16); out.write(new byte[]{'d','a','t','a'}); writeInt(out, dataBytes);
            for (float v : stereo) writeShort(out, (short)(Math.max(-1f, Math.min(1f, v)) * 32767));
        }
    }

    private static void writeInt(OutputStream o, int v) throws Exception { o.write(v & 255);o.write(v>>8&255);o.write(v>>16&255);o.write(v>>24&255); }
    private static void writeShort(OutputStream o, int v) throws Exception { o.write(v & 255);o.write(v>>8&255); }
    private static String safe(String s) { String v=s==null?"FMEFun":s.replaceAll("[^A-Za-z0-9_-]+","_");return v.isEmpty()?"FMEFun":v; }
}