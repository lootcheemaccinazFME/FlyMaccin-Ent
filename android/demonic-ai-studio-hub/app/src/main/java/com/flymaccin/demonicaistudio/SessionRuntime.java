package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.media.MediaPlayer;
import java.io.File;
import java.util.*;

final class SessionRuntime {
    final Context context;
    final FmeFunProject project;
    final StudioCommandController commands;
    final PatternScheduler scheduler;
    final SamplerVoiceEngine sampler = new SamplerVoiceEngine();
    final WavRecorder recorder = new WavRecorder();
    final FmeFunRenderer renderer = new FmeFunRenderer();
    private final List<MediaPlayer> activePlayers = new ArrayList<>();
    long captureStartTick;

    SessionRuntime(Context c, FmeFunProject p) {
        context = c;
        project = p;
        commands = new StudioCommandController(p, p.mixer);
        scheduler = new PatternScheduler(p.transport);
        restoreSamples();
    }

    void restoreSamples() {
        for (FmeFunProject.SampleMap s : project.samples) {
            if (s.source != null && !s.source.isEmpty()) {
                sampler.load(context, s.pad, s.source);
                sampler.setPitch(s.pad, s.pitch);
            }
        }
    }

    void mapSample(int pad, String path) {
        FmeFunProject.SampleMap s = project.sample(pad);
        s.source = path == null ? "" : path;
        if (!s.source.isEmpty()) {
            sampler.load(context, pad, s.source);
            sampler.setPitch(pad, s.pitch);
        }
    }

    void triggerPad(int pad, float velocity) {
        FmeFunProject.SampleMap s = project.sample(pad);
        sampler.trigger(pad, velocity * s.gain, s.pan);
    }

    void playRange(long fromTick, long toTick, AudioEngine audio) {
        scheduler.fireMidi(project.midi, fromTick, toTick, new PatternScheduler.Sink() {
            public void drum(int lane, float velocity, float pitch) { }
            public void midi(int pitch, int velocity, int channel, boolean on) {
                if (on) {
                    MixerGraph.Channel ch = project.mixer.channels[0];
                    if (!ch.mute) audio.playMidi(pitch, velocity, ch.gain, ch.pan);
                }
            }
        });

        long stepTicks = UnifiedTransport.PPQ / 4L;
        int step = Math.floorMod((int)(fromTick / stepTicks), project.drums.stepCount());
        scheduler.fireStep(project.drums, step, new PatternScheduler.Sink() {
            public void midi(int pitch, int velocity, int channel, boolean on) { }
            public void drum(int lane, float velocity, float pitch) {
                MixerGraph.Channel ch = project.mixer.channels[Math.min(2, project.mixer.channels.length - 1)];
                if (ch.mute) return;
                FmeFunProject.SampleMap map = null;
                for (FmeFunProject.SampleMap s : project.samples) if (s.pad == lane) { map = s; break; }
                if (map != null && map.source != null && !map.source.isEmpty()) triggerPad(lane, velocity);
                else audio.playDrum(lane, velocity * ch.gain, ch.pan, false, false);
            }
        });

        for (FmeFunProject.Clip c : project.clips) {
            if (c.muted || !"audio".equals(c.type) || c.source.isEmpty()) continue;
            if (c.startTick >= fromTick && c.startTick < toTick) playAudioClip(c);
        }
    }

    private void playAudioClip(FmeFunProject.Clip c) {
        try {
            MediaPlayer p = new MediaPlayer();
            p.setDataSource(c.source);
            MixerGraph.Channel ch = project.mixer.channels[Math.max(0, Math.min(project.mixer.channels.length - 1, c.track))];
            float gain = c.gain * ch.gain;
            float pan = Math.max(-1f, Math.min(1f, c.pan + ch.pan));
            float left = gain * (pan <= 0 ? 1f : 1f - pan);
            float right = gain * (pan >= 0 ? 1f : 1f + pan);
            p.setVolume(left, right);
            p.setOnCompletionListener(done -> { done.release(); activePlayers.remove(done); });
            p.prepare();
            long skipMs = Math.max(0, Math.round(c.offsetFrames * 1000.0 / AudioEngine.SAMPLE_RATE));
            if (skipMs > 0) p.seekTo((int)Math.min(Integer.MAX_VALUE, skipMs));
            activePlayers.add(p);
            p.start();
        } catch (Exception ignored) { }
    }

    void stopPlayback() {
        for (MediaPlayer p : new ArrayList<>(activePlayers)) {
            try { p.stop(); } catch (Exception ignored) { }
            try { p.release(); } catch (Exception ignored) { }
        }
        activePlayers.clear();
    }

    boolean startCapture(File file, int track) {
        if (track < 0 || track >= project.mixer.channels.length || !project.mixer.channels[track].recordArm) return false;
        captureStartTick = project.transport.tick();
        recorder.setMonitoring(project.mixer.channels[track].monitor);
        return recorder.start(file);
    }

    FmeFunProject.Clip stopCapture(int track) {
        File f = recorder.stop();
        if (f == null) return null;
        long frames = Math.max(1, (f.length() - 44) / 2);
        long ticks = Math.max(1, Math.round(frames / (double)AudioEngine.SAMPLE_RATE * project.transport.bpm() / 60.0 * UnifiedTransport.PPQ));
        FmeFunProject.Clip c = project.addClip(track, "audio", f.getAbsolutePath(), captureStartTick, ticks);
        try {
            c.waveformBuckets = 256;
            c.waveformKey = Integer.toHexString(Arrays.hashCode(WaveformCache.peaks(f, c.waveformBuckets)));
        } catch (Exception ignored) { }
        return c;
    }

    void save() throws Exception { FmeFunStore.save(context, project); }

    void close() {
        stopPlayback();
        sampler.release();
        if (recorder.isRunning()) recorder.stop();
    }
}