package com.flymaccin.demonicaistudio;
import java.util.*;
final class MidiClip {
 static final class Note { long startTick,durationTicks; int pitch,velocity,channel; Note(long s,long d,int p,int v,int c){startTick=Math.max(0,s);durationTicks=Math.max(1,d);pitch=clamp(p,0,127);velocity=clamp(v,1,127);channel=clamp(c,0,15);} }
 static final class CC { long tick; int controller,value,channel; CC(long t,int c,int v,int ch){tick=Math.max(0,t);controller=clamp(c,0,127);value=clamp(v,0,127);channel=clamp(ch,0,15);} }
 final List<Note> notes=new ArrayList<>(); final List<CC> cc=new ArrayList<>();
 Note add(long s,long d,int p,int v,int ch){Note n=new Note(s,d,p,v,ch);notes.add(n);sort();return n;}
 void remove(Note n){notes.remove(n);} void addCC(long t,int c,int v,int ch){cc.add(new CC(t,c,v,ch));}
 void move(Note n,long tick,int pitch){n.startTick=Math.max(0,tick);n.pitch=clamp(pitch,0,127);sort();}
 void resize(Note n,long duration){n.durationTicks=Math.max(1,duration);}
 void velocity(Note n,int v){n.velocity=clamp(v,1,127);}
 void quantize(long grid,float strength){grid=Math.max(1,grid);strength=Math.max(0,Math.min(1,strength));for(Note n:notes){long q=Math.round(n.startTick/(double)grid)*grid;n.startTick=Math.max(0,n.startTick+Math.round((q-n.startTick)*strength));}sort();}
 private void sort(){notes.sort(Comparator.comparingLong(n->n.startTick));} private static int clamp(int v,int a,int b){return Math.max(a,Math.min(b,v));}
}