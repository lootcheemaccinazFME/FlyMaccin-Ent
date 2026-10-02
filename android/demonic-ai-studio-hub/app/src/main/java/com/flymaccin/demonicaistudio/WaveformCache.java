package com.flymaccin.demonicaistudio;
import java.io.*; import java.util.*;
final class WaveformCache {
 static float[] peaks(File wav,int buckets)throws IOException{buckets=Math.max(16,buckets);float[] out=new float[buckets];try(RandomAccessFile f=new RandomAccessFile(wav,"r")){if(f.length()<=44)return out;f.seek(44);long samples=(f.length()-44)/2,per=Math.max(1,samples/buckets);for(int b=0;b<buckets;b++){int peak=0;for(long i=0;i<per&&f.getFilePointer()+1<f.length();i++){int lo=f.readUnsignedByte(),hi=f.readByte();short s=(short)((hi<<8)|lo);peak=Math.max(peak,Math.abs((int)s));}out[b]=peak/32768f;}}return out;}
}