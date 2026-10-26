package com.flymaccin.demonicaistudio;

import java.io.*;
import java.util.*;

final class InstrumentDefinition {
    static final class Region { String sample=""; int keyLo=0,keyHi=127,root=60; }
    final List<Region> regions=new ArrayList<>();
    static InstrumentDefinition sfz(File file)throws IOException{
        InstrumentDefinition d=new InstrumentDefinition();Region current=null;
        try(BufferedReader r=new BufferedReader(new FileReader(file))){String line;while((line=r.readLine())!=null){line=line.replaceAll("//.*","").trim();if(line.isEmpty())continue;String[] tokens=line.split("\\s+");for(String t:tokens){if(t.equals("<region>")){current=new Region();d.regions.add(current);continue;}if(current==null)continue;int eq=t.indexOf('=');if(eq<1)continue;String k=t.substring(0,eq),v=t.substring(eq+1);try{if(k.equals("sample"))current.sample=v;else if(k.equals("key"))current.keyLo=current.keyHi=midi(v);else if(k.equals("lokey"))current.keyLo=midi(v);else if(k.equals("hikey"))current.keyHi=midi(v);else if(k.equals("pitch_keycenter"))current.root=midi(v);}catch(Exception ignored){}}}}
        if(d.regions.isEmpty())throw new IOException("No SFZ regions");return d;
    }
    Region region(int midi){for(Region r:regions)if(midi>=r.keyLo&&midi<=r.keyHi)return r;return regions.get(0);}
    private static int midi(String s){try{return Integer.parseInt(s);}catch(Exception ignored){}String[] n={"C","C#","D","D#","E","F","F#","G","G#","A","A#","B"};String u=s.toUpperCase(Locale.US);for(int i=0;i<n.length;i++)if(u.startsWith(n[i])){int o=Integer.parseInt(u.substring(n[i].length()));return (o+1)*12+i;}return 60;}
    static boolean sf2Header(File f){try(RandomAccessFile r=new RandomAccessFile(f,"r")){byte[] b=new byte[12];r.readFully(b);return new String(b,0,4,"US-ASCII").equals("RIFF")&&new String(b,8,4,"US-ASCII").equals("sfbk");}catch(Exception e){return false;}}
}