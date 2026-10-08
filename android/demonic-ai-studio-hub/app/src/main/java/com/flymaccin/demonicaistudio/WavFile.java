package com.flymaccin.demonicaistudio;

import java.io.*;
import java.util.Arrays;

final class WavFile {
    static final class Data { final int rate,channels; final float[] samples; Data(int r,int c,float[] s){rate=r;channels=c;samples=s;} int frames(){return samples.length/channels;} }
    static Data read(File file) throws IOException {
        try(RandomAccessFile r=new RandomAccessFile(file,"r")){
            if(!"RIFF".equals(read4(r))){throw new IOException("Not RIFF");} readLE32(r); if(!"WAVE".equals(read4(r)))throw new IOException("Not WAVE");
            int channels=0,rate=0,bits=0,format=0; long dataPos=-1; int dataBytes=0;
            while(r.getFilePointer()+8<=r.length()){String id=read4(r);int size=readLE32(r);long next=r.getFilePointer()+size+(size&1);if("fmt ".equals(id)){format=readLE16(r);channels=readLE16(r);rate=readLE32(r);r.skipBytes(6);bits=readLE16(r);}else if("data".equals(id)){dataPos=r.getFilePointer();dataBytes=size;}r.seek(Math.min(next,r.length()));}
            if(dataPos<0||format!=1||bits!=16||(channels!=1&&channels!=2))throw new IOException("Only PCM16 mono/stereo WAV supported");
            r.seek(dataPos);int count=dataBytes/2;float[] s=new float[count];for(int i=0;i<count;i++)s[i]=(short)readLE16(r)/32768f;return new Data(rate,channels,s);
        }
    }
    static float[] mono(Data d){if(d.channels==1)return d.samples.clone();float[] m=new float[d.frames()];for(int i=0;i<m.length;i++)m[i]=(d.samples[i*2]+d.samples[i*2+1])*.5f;return m;}
    static float[] sliceMono(Data d,long startMs,long endMs){float[] m=mono(d);int a=(int)Math.max(0,Math.min(m.length,startMs*d.rate/1000));int b=endMs<0?m.length:(int)Math.max(a,Math.min(m.length,endMs*d.rate/1000));return Arrays.copyOfRange(m,a,b);}
    private static String read4(RandomAccessFile r)throws IOException{byte[] b=new byte[4];r.readFully(b);return new String(b,"US-ASCII");}
    private static int readLE16(RandomAccessFile r)throws IOException{return r.readUnsignedByte()|(r.readUnsignedByte()<<8);}
    private static int readLE32(RandomAccessFile r)throws IOException{return r.readUnsignedByte()|(r.readUnsignedByte()<<8)|(r.readUnsignedByte()<<16)|(r.readUnsignedByte()<<24);}
}