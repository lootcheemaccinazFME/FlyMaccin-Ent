package com.flymaccin.demonicaistudio;
import android.content.Context;import java.io.*;import java.util.*;
final class FmeFunRenderer {
 static final int RATE=44100;
 static final class RenderGraph { final float[][] tracks,buses; final float[] master; RenderGraph(int tc,int bc,int frames){tracks=new float[tc][frames*2];buses=new float[bc][frames*2];master=new float[frames*2];} }
 RenderGraph renderGraph(FmeFunProject p)throws Exception{
  long end=UnifiedTransport.PPQ*4L;for(FmeFunProject.Clip c:p.clips)end=Math.max(end,c.startTick+c.lengthTicks);for(MidiClip.Note n:p.midi.notes)end=Math.max(end,n.startTick+n.durationTicks);
  int frames=(int)Math.min(Integer.MAX_VALUE/4,Math.ceil(p.transport.millisForTicks(end)/1000.0*RATE)+RATE);RenderGraph g=new RenderGraph(p.mixer.channels.length,p.mixer.buses.length,frames);
  for(FmeFunProject.Clip c:p.clips)if(!c.muted&&"audio".equals(c.type)&&!c.source.isEmpty())addWav(g.tracks[c.track],new File(c.source),frame(p,c.startTick),c.offsetFrames,c.gain,c.pan);
  for(int t=0;t<g.tracks.length;t++){MixerGraph.Channel ch=p.mixer.channels[t];if(ch.mute)continue;applyGainPan(g.tracks[t],ch.gain,ch.pan);int bus=Math.max(0,Math.min(g.buses.length-1,ch.bus));sum(g.buses[bus],g.tracks[t],1f);for(int b=0;b<Math.min(ch.sends.length,g.buses.length);b++)if(ch.sends[b]>0)sum(g.buses[b],g.tracks[t],ch.sends[b]);}
  for(int b=0;b<g.buses.length;b++){MixerGraph.Channel bus=p.mixer.buses[b];if(!bus.mute){applyGainPan(g.buses[b],bus.gain,bus.pan);sum(g.master,g.buses[b],1f);}}normalize(g.master);return g;
 }
 private static int frame(FmeFunProject p,long tick){return (int)Math.round(p.transport.millisForTicks(tick)/1000.0*RATE);}
 private static void addWav(float[] target,File f,int start,long offsetFrames,float gain,float pan)throws Exception{if(!f.exists()||f.length()<=44)return;try(RandomAccessFile in=new RandomAccessFile(f,"r")){in.seek(Math.min(in.length(),44+offsetFrames*2));int fr=start;while(fr<target.length/2&&in.getFilePointer()+1<in.length()){int lo=in.readUnsignedByte(),hi=in.readByte();float v=((short)((hi<<8)|lo))/32768f;float l=gain*(pan<=0?1:1-pan),r=gain*(pan>=0?1:1+pan);target[fr*2]+=v*l;target[fr*2+1]+=v*r;fr++;}}}
 private static void applyGainPan(float[] x,float gain,float pan){float l=gain*(pan<=0?1:1-pan),r=gain*(pan>=0?1:1+pan);for(int i=0;i<x.length;i+=2){x[i]*=l;x[i+1]*=r;}}
 private static void sum(float[] d,float[] s,float g){for(int i=0;i<Math.min(d.length,s.length);i++)d[i]+=s[i]*g;}private static void normalize(float[] x){float p=0;for(float v:x)p=Math.max(p,Math.abs(v));if(p>.98f){float g=.98f/p;for(int i=0;i<x.length;i++)x[i]*=g;}}
}