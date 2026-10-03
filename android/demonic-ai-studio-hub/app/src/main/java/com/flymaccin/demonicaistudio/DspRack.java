package com.flymaccin.demonicaistudio;
import java.util.*;
final class DspRack {
 interface Fx { void process(float[] stereo,int rate); String id(); }
 final ArrayList<Fx> chain=new ArrayList<>();void add(Fx fx){if(fx!=null)chain.add(fx);}void clear(){chain.clear();}void process(float[] s,int rate){for(Fx fx:chain)fx.process(s,rate);}
 static final class Gain implements Fx{float gain;Gain(float g){gain=g;}public String id(){return"gain";}public void process(float[]s,int r){for(int i=0;i<s.length;i++)s[i]*=gain;}}
 static final class SoftClip implements Fx{float drive;SoftClip(float d){drive=Math.max(.1f,d);}public String id(){return"softclip";}public void process(float[]s,int r){for(int i=0;i<s.length;i++)s[i]=(float)Math.tanh(s[i]*drive);}}
 static final class Delay implements Fx{float ms,feedback,mix;Delay(float ms,float fb,float mix){this.ms=ms;feedback=fb;this.mix=mix;}public String id(){return"delay";}public void process(float[]s,int rate){int d=Math.max(2,(int)(rate*ms/1000f)*2);for(int i=d;i<s.length;i++){float wet=s[i-d];s[i]+=wet*mix;s[i]+=wet*feedback*.25f;}}}
}