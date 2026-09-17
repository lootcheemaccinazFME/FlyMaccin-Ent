package com.flymaccin.demonicdaw;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Lightweight SFZ region loader for offline Android playback. Supports the core SFZ opcodes
 * used by the bundled Demonic Factory banks and common user-made banks. Unknown opcodes are
 * ignored safely rather than corrupting the region map. */
public final class SfzBank {
    private static final Pattern TOKEN = Pattern.compile("([A-Za-z0-9_]+)=(\\\"[^\\\"]*\\\"|[^\\s]+)");
    private SfzBank() {}

    public static int load(File sfzFile) throws IOException {
        NativeAudioEngine.nativeClearSampleBank();
        Map<String,String> global = new HashMap<>();
        Map<String,String> group = new HashMap<>();
        Map<String,String> region = null;
        int loaded = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(sfzFile)))) {
            String line;
            while ((line = br.readLine()) != null) {
                int comment = line.indexOf("//");
                if (comment >= 0) line = line.substring(0, comment);
                line = line.trim();
                if (line.isEmpty()) continue;
                if (line.contains("<global>")) { if (region != null) loaded += emit(sfzFile, global, group, region); region = null; global.clear(); group.clear(); line = line.substring(line.indexOf("<global>")+8); }
                if (line.contains("<group>")) { if (region != null) loaded += emit(sfzFile, global, group, region); region = null; group.clear(); line = line.substring(line.indexOf("<group>")+7); }
                if (line.contains("<region>")) { if (region != null) loaded += emit(sfzFile, global, group, region); region = new HashMap<>(); line = line.substring(line.indexOf("<region>")+8); }
                Map<String,String> target = region != null ? region : (!group.isEmpty() ? group : global);
                Matcher m = TOKEN.matcher(line);
                while (m.find()) target.put(m.group(1).toLowerCase(Locale.US), strip(m.group(2)));
            }
        }
        if (region != null) loaded += emit(sfzFile, global, group, region);
        return loaded;
    }

    private static int emit(File sfzFile, Map<String,String> g, Map<String,String> grp, Map<String,String> r) {
        Map<String,String> p = new HashMap<>(g); p.putAll(grp); p.putAll(r);
        String sample = p.get("sample"); if (sample == null) return 0;
        File wav = new File(sfzFile.getParentFile(), sample.replace('\\', File.separatorChar));
        if (!wav.exists()) return 0;
        int lo = key(p.getOrDefault("lokey", p.getOrDefault("key", "0")), 0);
        int hi = key(p.getOrDefault("hikey", p.getOrDefault("key", "127")), 127);
        int center = key(p.getOrDefault("pitch_keycenter", p.getOrDefault("key", "60")), 60);
        int lovel = integer(p,"lovel",0), hivel = integer(p,"hivel",127);
        float volume = number(p,"volume",0f), pan = number(p,"pan",0f), tune = number(p,"tune",0f);
        int transpose = integer(p,"transpose",0), loopStart = integer(p,"loop_start",-1), loopEnd = integer(p,"loop_end",-1);
        int offset = integer(p,"offset",0), end = integer(p,"end",-1);
        String lm = p.getOrDefault("loop_mode","no_loop").toLowerCase(Locale.US);
        int loopMode = (lm.equals("loop_continuous") || lm.equals("loop_sustain")) ? 1 : 0;
        float release = Math.max(0.005f, number(p,"ampeg_release",0.15f));
        return NativeAudioEngine.nativeAddWavRegion(wav.getAbsolutePath(), lo, hi, lovel, hivel, center,
                volume, pan, tune, transpose, loopMode, loopStart, loopEnd, offset, end, release) ? 1 : 0;
    }

    private static String strip(String s){ return s.length()>1 && s.startsWith("\"") && s.endsWith("\"") ? s.substring(1,s.length()-1) : s; }
    private static int integer(Map<String,String> p,String k,int d){ try{return Integer.parseInt(p.getOrDefault(k,String.valueOf(d)));}catch(Exception e){return d;} }
    private static float number(Map<String,String> p,String k,float d){ try{return Float.parseFloat(p.getOrDefault(k,String.valueOf(d)));}catch(Exception e){return d;} }
    private static int key(String s,int d){
        try{return Math.max(0,Math.min(127,Integer.parseInt(s)));}catch(Exception ignored){}
        String x=s.trim().toUpperCase(Locale.US); if(x.length()<2)return d;
        String[] names={"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};
        int octavePos=1; if(x.length()>1 && x.charAt(1)=='#')octavePos=2;
        String note=x.substring(0,octavePos); int sem=-1; for(int i=0;i<names.length;i++)if(names[i].equals(note))sem=i;
        if(sem<0)return d; try{int oct=Integer.parseInt(x.substring(octavePos)); return Math.max(0,Math.min(127,(oct+1)*12+sem));}catch(Exception e){return d;}
    }
}
