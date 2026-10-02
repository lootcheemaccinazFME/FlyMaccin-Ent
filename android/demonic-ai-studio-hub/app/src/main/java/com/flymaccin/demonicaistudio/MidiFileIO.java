package com.flymaccin.demonicaistudio;

import java.io.*;
import java.util.*;

final class MidiFileIO {
    static List<ProductionProject.MidiNote> read(File file)throws IOException{
        try(DataInputStream in=new DataInputStream(new BufferedInputStream(new FileInputStream(file)))){
            if(in.readInt()!=0x4d546864)throw new IOException("Not MIDI");int h=in.readInt();in.readUnsignedShort();int tracks=in.readUnsignedShort();int division=in.readUnsignedShort();if(h>6)in.skipBytes(h-6);List<ProductionProject.MidiNote> out=new ArrayList<>();
            for(int tr=0;tr<tracks;tr++){if(in.readInt()!=0x4d54726b)throw new IOException("Missing MTrk");int len=in.readInt();byte[] data=new byte[len];in.readFully(data);parseTrack(data,division,out);}return out;
        }
    }
    private static void parseTrack(byte[] d,int division,List<ProductionProject.MidiNote> out)throws IOException{
        int p=0,tick=0,status=0;Map<Integer,Integer> active=new HashMap<>();while(p<d.length){int[] v=vlq(d,p);tick+=v[0];p=v[1];if(p>=d.length)break;int b=d[p]&255;if(b<128)b=status;else{status=b;p++;}if(b==0xff){p++;v=vlq(d,p);p=v[1]+v[0];continue;}if(b==0xf0||b==0xf7){v=vlq(d,p);p=v[1]+v[0];continue;}int type=b&0xf0;if(type==0x90||type==0x80){int note=d[p++]&127,vel=d[p++]&127,key=((b&15)<<8)|note;if(type==0x90&&vel>0)active.put(key,tick);else{Integer start=active.remove(key);if(start!=null)out.add(new ProductionProject.MidiNote(note,vel>0?vel:100,start*ProductionProject.PPQ/division,Math.max(1,(tick-start)*ProductionProject.PPQ/division)));}}else p+=(type==0xc0||type==0xd0)?1:2;}
    }
    private static int[] vlq(byte[] d,int p){int v=0,b;do{b=d[p++]&255;v=(v<<7)|(b&127);}while((b&128)!=0&&p<d.length);return new int[]{v,p};}
}