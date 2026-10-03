package com.flymaccin.demonicaistudio;
final class MeterEngine {
 static void update(MixerGraph.Channel ch,float[] stereo){if(ch==null||stereo==null||stereo.length<2)return;float l=0,r=0;for(int i=0;i+1<stereo.length;i+=2){l=Math.max(l,Math.abs(stereo[i]));r=Math.max(r,Math.abs(stereo[i+1]));}ch.meterL=ch.meterL*.72f+l*.28f;ch.meterR=ch.meterR*.72f+r*.28f;}
 static float db(float linear){return linear<=.000001f?-120f:(float)(20*Math.log10(linear));}
}