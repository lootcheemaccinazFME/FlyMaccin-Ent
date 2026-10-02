package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class StudioRenderer {
    static final class Result {
        final File master;
        final List<File> stems;
        Result(File master, List<File> stems) { this.master = master; this.stems = stems; }
    }

    private static final int RATE = 44100;
    private static final String[] TRACK_NAMES = {"Piano", "Guitar", "Drums", "Voice"};
    private final Random random = new Random(923771L);

    Result render(Context context, StudioProject project, boolean exportStems) throws Exception {
        double stepSeconds = 60.0 / project.bpm / 4.0;
        int sequenceFrames = (int) Math.ceil(StudioProject.STEPS * stepSeconds * RATE);
        int frames = sequenceFrames + RATE * 2;
        float[][] tracks = new float[StudioProject.TRACK_COUNT][frames * 2];

        for (int step = 0; step < StudioProject.STEPS; step++) {
            int offset = (int) Math.round(step * stepSeconds * RATE);
            for (double frequency : project.frequenciesAt(StudioProject.TRACK_PIANO, step))
                if (frequency > 0) addMono(tracks[StudioProject.TRACK_PIANO], piano(frequency, 1100), offset,
                        project.volume[0], project.pan[0]);
            double[] guitar = project.frequenciesAt(StudioProject.TRACK_GUITAR, step);
            for (int note = 0; note < guitar.length; note++) if (guitar[note] > 0)
                addMono(tracks[StudioProject.TRACK_GUITAR], pluck(guitar[note], 1400), offset + note * 1100,
                        project.volume[1] * 0.72f, project.pan[1]);
            for (int lane = 0; lane < 4; lane++) if (project.drums[lane][step])
                addMono(tracks[StudioProject.TRACK_DRUMS], drum(lane), offset,
                        project.volume[2], project.pan[2]);
        }

        if (!project.voicePath.isEmpty()) {
            int offset = (int) Math.round(project.voiceStartStep * stepSeconds * RATE);
            addVoiceWav(tracks[StudioProject.TRACK_VOICE], new File(project.voicePath), offset,
                    project.volume[3], project.pan[3]);
        }

        for (int track = 0; track < StudioProject.TRACK_COUNT; track++) {
            if (project.delay[track]) addEcho(tracks[track], (int) (RATE * 0.27) * 2, 0.28f);
            if (project.reverb[track]) {
                addEcho(tracks[track], (int) (RATE * 0.061) * 2, 0.17f);
                addEcho(tracks[track], (int) (RATE * 0.097) * 2, 0.12f);
            }
        }

        float[] master = new float[frames * 2];
        for (int track = 0; track < StudioProject.TRACK_COUNT; track++) if (project.trackAudible(track))
            for (int index = 0; index < master.length; index++) master[index] += tracks[track][index];
        normalize(master);

        File root = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "DemonicExports");
        if (!root.exists() && !root.mkdirs()) throw new IllegalStateException("Cannot create export folder");
        String stamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.US).format(new java.util.Date());
        File masterFile = new File(root, safe(project.name) + "_MASTER_" + stamp + ".wav");
        writeWav(masterFile, master);

        List<File> stemFiles = new ArrayList<>();
        if (exportStems) {
            for (int track = 0; track < StudioProject.TRACK_COUNT; track++) {
                normalize(tracks[track]);
                File stem = new File(root, safe(project.name) + "_STEM_" + TRACK_NAMES[track] + "_" + stamp + ".wav");
                writeWav(stem, tracks[track]);
                stemFiles.add(stem);
            }
        }
        return new Result(masterFile, stemFiles);
    }


    Result renderProduction(Context context, ProductionProject project, String name, boolean exportStems) throws Exception {
        double tickSeconds=60.0/Math.max(40,project.bpm)/ProductionProject.PPQ;
        int frames=(int)Math.ceil(project.bars*4.0*ProductionProject.PPQ*tickSeconds*RATE)+RATE*3;
        List<float[]> tracks=new ArrayList<>();
        boolean anySolo=false; for(ProductionProject.Channel ch:project.channels) if(ch.solo) anySolo=true;
        for(ProductionProject.Channel ch:project.channels){
            float[] out=new float[frames*2];tracks.add(out);
            if(ch.mute||(anySolo&&!ch.solo))continue;
            for(ProductionProject.MidiNote n:ch.notes){
                int off=(int)Math.round(n.startTick*tickSeconds*RATE);
                int ms=Math.max(60,(int)Math.round(n.durationTick*tickSeconds*1000));
                double hz=440.0*Math.pow(2,(n.note-69)/12.0);
                float vel=Math.max(0,Math.min(1,n.velocity/127f));
                float[] voice=(ch.instrument.type==ProductionProject.InstrumentType.GUITAR||ch.instrument.type==ProductionProject.InstrumentType.BASS)?pluck(hz,ms):piano(hz,ms);
                addMono(out,voice,off,ch.volume*vel,ch.pan);
            }
            for(ProductionProject.AudioClip clip:ch.audio) addProductionClip(out,clip,(int)Math.round(clip.startTick*tickSeconds*RATE),ch.volume,ch.pan);
            if(!ch.bypassFx) applyProductionFx(out,ch);
        }
        java.util.Map<String,float[]> busAudio=new java.util.HashMap<>();
        for(ProductionProject.Bus bus:project.buses)busAudio.put(bus.id,new float[frames*2]);
        float[] master=new float[frames*2];
        for(int ti=0;ti<tracks.size();ti++){ProductionProject.Channel ch=project.channels.get(ti);float[] t=tracks.get(ti);float[] bus=busAudio.get(ch.route);float[] dst=bus==null?master:bus;for(int i=0;i<dst.length;i++)dst[i]+=t[i];}
        for(ProductionProject.Bus bus:project.buses){float[] b=busAudio.get(bus.id);if(b==null||bus.mute)continue;applyBusFx(b,bus);mixStereo(master,b,bus.volume,bus.pan);}
        normalize(master);
        File root=new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC),"DemonicExports");if(!root.exists()&&!root.mkdirs())throw new IllegalStateException("Cannot create export folder");
        String stamp=new java.text.SimpleDateFormat("yyyyMMdd_HHmmss",java.util.Locale.US).format(new java.util.Date());File mf=new File(root,safe(name)+"_MASTER_"+stamp+".wav");writeWav(mf,master);
        List<File> stems=new ArrayList<>();if(exportStems)for(int i=0;i<tracks.size();i++){float[] t=tracks.get(i);normalize(t);File sf=new File(root,safe(name)+"_STEM_"+safe(project.channels.get(i).name)+"_"+stamp+".wav");writeWav(sf,t);stems.add(sf);}return new Result(mf,stems);
    }
    private static void addProductionClip(float[] target,ProductionProject.AudioClip clip,int offset,float volume,float pan){
        try{WavFile.Data d=WavFile.read(new File(clip.path));float[] src=WavFile.mono(d);int a=(int)Math.max(0,clip.trimStartMs*d.rate/1000L),b=clip.trimEndMs<0?src.length:(int)Math.min(src.length,clip.trimEndMs*d.rate/1000L);if(b<=a)return;int raw=b-a;float stretch=Math.max(.125f,Math.min(8f,clip.stretch));int len=Math.max(1,(int)(raw*stretch));float[] cut=new float[len];int fi=(int)(clip.fadeInMs*RATE/1000f),fo=(int)(clip.fadeOutMs*RATE/1000f);for(int i=0;i<len;i++){int q=Math.min(raw-1,(int)(i/stretch));int si=clip.reverse?b-1-q:a+q;float g=clip.gain;if(fi>0&&i<fi)g*=i/(float)fi;if(fo>0&&i>len-fo)g*=Math.max(0,(len-i)/(float)fo);cut[i]=src[si]*g;}addMono(target,cut,offset,volume,pan);}catch(Exception ignored){}
    }
    private static void applyProductionFx(float[] s,ProductionProject.Channel ch){applyEq(s,ch.eqLow,ch.eqMid,ch.eqHigh);applyDynamics(s,ch.drive,ch.compressor,ch.reverb,ch.delay);}
    private static void applyBusFx(float[] s,ProductionProject.Bus b){applyEq(s,b.eqLow,b.eqMid,b.eqHigh);applyDynamics(s,b.drive,b.compressor,b.reverb,b.delay);}
    private static void applyDynamics(float[] s,float drive,float compressor,float reverb,float delay){
        drive=Math.max(0,drive);if(drive>0)for(int i=0;i<s.length;i++)s[i]=(float)Math.tanh(s[i]*(1+drive*5));
        if(delay>0)addEcho(s,(int)(RATE*.27)*2,Math.min(.7f,delay));
        if(reverb>0){addEcho(s,(int)(RATE*.061)*2,Math.min(.55f,reverb*.7f));addEcho(s,(int)(RATE*.097)*2,Math.min(.4f,reverb*.45f));}
        if(compressor>0){float th=.75f-compressor*.45f;for(int i=0;i<s.length;i++){float a=Math.abs(s[i]);if(a>th)s[i]=Math.signum(s[i])*(th+(a-th)/(1+compressor*6));}}
    }
    private static void applyEq(float[] s,float lowDb,float midDb,float highDb){
        if(Math.abs(lowDb)+Math.abs(midDb)+Math.abs(highDb)<.001f)return;float lg=db(lowDb),mg=db(midDb),hg=db(highDb);float[] lp={0,0},hpLast={0,0},xLast={0,0};float la=(float)Math.exp(-2*Math.PI*220/RATE),ha=(float)Math.exp(-2*Math.PI*3500/RATE);
        for(int i=0;i<s.length;i++){int ch=i&1;float x=s[i];lp[ch]=(1-la)*x+la*lp[ch];float hp=ha*(hpLast[ch]+x-xLast[ch]);xLast[ch]=x;hpLast[ch]=hp;float mid=x-lp[ch]-hp;s[i]=lp[ch]*lg+mid*mg+hp*hg;}
    }
    private static float db(float v){v=Math.max(-18,Math.min(18,v));return (float)Math.pow(10,v/20f);}
    private static void mixStereo(float[] dst,float[] src,float volume,float pan){float l=volume*(pan<=0?1f:1f-pan),r=volume*(pan>=0?1f:1f+pan);for(int i=0;i+1<dst.length;i+=2){dst[i]+=src[i]*l;dst[i+1]+=src[i+1]*r;}}


    static Uri publish(Context context, File source) throws Exception {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.DISPLAY_NAME, source.getName());
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/wav");
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/DemonicAIStudio");
            values.put(MediaStore.Audio.Media.IS_PENDING, 1);
        }
        Uri uri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) throw new IllegalStateException("Media export failed");
        try (java.io.InputStream input = new FileInputStream(source);
             java.io.OutputStream output = context.getContentResolver().openOutputStream(uri)) {
            if (output == null) throw new IllegalStateException("Cannot open media export");
            byte[] buffer = new byte[32768];
            int count;
            while ((count = input.read(buffer)) >= 0) output.write(buffer, 0, count);
        }
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            ContentValues ready = new ContentValues();
            ready.put(MediaStore.Audio.Media.IS_PENDING, 0);
            context.getContentResolver().update(uri, ready, null, null);
        }
        return uri;
    }

    private float[] piano(double frequency, int durationMs) {
        int count = RATE * durationMs / 1000;
        float[] result = new float[count];
        for (int index = 0; index < count; index++) {
            double time = index / (double) RATE;
            double attack = Math.min(1, time * 55);
            double release = Math.max(0, 1 - time / (durationMs / 1000.0));
            double wave = Math.sin(2 * Math.PI * frequency * time)
                    + Math.sin(4 * Math.PI * frequency * time) * 0.28
                    + Math.sin(6 * Math.PI * frequency * time) * 0.10;
            result[index] = (float) (wave * attack * Math.pow(release, 0.7) * 0.62);
        }
        return result;
    }

    private float[] pluck(double frequency, int durationMs) {
        int count = RATE * durationMs / 1000;
        int period = Math.max(2, (int) (RATE / Math.max(35, frequency)));
        float[] ring = new float[period];
        for (int index = 0; index < period; index++) ring[index] = random.nextFloat() * 2f - 1f;
        float[] result = new float[count];
        for (int index = 0; index < count; index++) {
            int slot = index % period;
            float value = ring[slot];
            ring[slot] = 0.498f * (value + ring[(slot + 1) % period]);
            result[index] = (float) (value * Math.exp(-(index / (double) RATE) * 1.25) * 0.78);
        }
        return result;
    }

    private float[] drum(int lane) {
        int durationMs = lane == 0 ? 320 : lane == 1 ? 210 : lane == 2 ? 105 : 160;
        int count = RATE * durationMs / 1000;
        float[] result = new float[count];
        double phase = 0;
        for (int index = 0; index < count; index++) {
            double time = index / (double) RATE;
            double envelope = Math.exp(-time * (lane == 0 ? 15 : lane == 1 ? 25 : 42));
            if (lane == 0) {
                double frequency = 160 - 112 * Math.min(1, time * 8);
                phase += 2 * Math.PI * frequency / RATE;
                result[index] = (float) (Math.sin(phase) * envelope);
            } else if (lane == 1) {
                result[index] = (float) (((random.nextDouble() * 2 - 1) * 0.82
                        + Math.sin(2 * Math.PI * 185 * time) * 0.26) * envelope);
            } else if (lane == 2) {
                result[index] = (float) ((random.nextDouble() * 2 - 1) * envelope * 0.52);
            } else {
                result[index] = (float) ((Math.sin(2 * Math.PI * 520 * time) * 0.52
                        + (random.nextDouble() * 2 - 1) * 0.16) * envelope);
            }
        }
        return result;
    }

    private static void addMono(float[] target, float[] source, int frameOffset, float volume, float pan) {
        float left = volume * (pan <= 0 ? 1f : 1f - pan);
        float right = volume * (pan >= 0 ? 1f : 1f + pan);
        int available = Math.min(source.length, target.length / 2 - frameOffset);
        for (int frame = 0; frame < available; frame++) {
            target[(frameOffset + frame) * 2] += source[frame] * left;
            target[(frameOffset + frame) * 2 + 1] += source[frame] * right;
        }
    }

    private static void addVoiceWav(float[] target, File file, int frameOffset, float volume, float pan) {
        if (!file.exists() || file.length() <= 44) return;
        float left = volume * (pan <= 0 ? 1f : 1f - pan);
        float right = volume * (pan >= 0 ? 1f : 1f + pan);
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(file))) {
            long skipped = 0;
            while (skipped < 44) {
                long amount = stream.skip(44 - skipped);
                if (amount <= 0) return;
                skipped += amount;
            }
            int frame = frameOffset;
            int low;
            while (frame < target.length / 2 && (low = stream.read()) >= 0) {
                int high = stream.read();
                if (high < 0) break;
                short sample = (short) ((high << 8) | low);
                float value = sample / 32768f;
                target[frame * 2] += value * left;
                target[frame * 2 + 1] += value * right;
                frame++;
            }
        } catch (Exception ignored) {
        }
    }

    private static void addEcho(float[] samples, int offset, float gain) {
        for (int index = offset; index < samples.length; index++) samples[index] += samples[index - offset] * gain;
    }

    private static void normalize(float[] samples) {
        float peak = 0;
        for (float sample : samples) peak = Math.max(peak, Math.abs(sample));
        if (peak > 0.96f) {
            float gain = 0.96f / peak;
            for (int index = 0; index < samples.length; index++) samples[index] *= gain;
        }
    }

    private static void writeWav(File file, float[] stereo) throws Exception {
        int dataBytes = stereo.length * 2;
        try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(file))) {
            stream.write(new byte[]{'R','I','F','F'});
            writeInt(stream, 36 + dataBytes);
            stream.write(new byte[]{'W','A','V','E','f','m','t',' '});
            writeInt(stream, 16);
            writeShort(stream, 1);
            writeShort(stream, 2);
            writeInt(stream, RATE);
            writeInt(stream, RATE * 4);
            writeShort(stream, 4);
            writeShort(stream, 16);
            stream.write(new byte[]{'d','a','t','a'});
            writeInt(stream, dataBytes);
            for (float value : stereo) writeShort(stream, (short) (Math.max(-1, Math.min(1, value)) * 32767));
        }
    }

    private static void writeInt(BufferedOutputStream stream, int value) throws Exception {
        stream.write(value & 255); stream.write((value >> 8) & 255);
        stream.write((value >> 16) & 255); stream.write((value >> 24) & 255);
    }

    private static void writeShort(BufferedOutputStream stream, int value) throws Exception {
        stream.write(value & 255); stream.write((value >> 8) & 255);
    }

    private static String safe(String value) {
        String safe = value == null ? "Demonic_Session" : value.replaceAll("[^A-Za-z0-9_-]+", "_");
        return safe.isEmpty() ? "Demonic_Session" : safe;
    }
}
