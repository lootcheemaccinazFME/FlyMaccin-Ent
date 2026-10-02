package com.flymaccin.demonicaistudio;
final class MixerGraph {
 static final class Channel { float gain=1f,pan=0f,meterL=0f,meterR=0f; boolean mute,solo,recordArm,monitor; final float[] sends=new float[4]; int bus=0; }
 final Channel[] channels; final Channel[] buses;
 MixerGraph(int tracks,int busCount){channels=new Channel[Math.max(1,tracks)];buses=new Channel[Math.max(1,busCount)];for(int i=0;i<channels.length;i++)channels[i]=new Channel();for(int i=0;i<buses.length;i++)buses[i]=new Channel();}
 void gain(int ch,float v){channels[ch].gain=clamp(v,0f,2f);} void pan(int ch,float v){channels[ch].pan=clamp(v,-1f,1f);}
 void send(int ch,int bus,float v){channels[ch].sends[bus]=clamp(v,0f,1f);} void route(int ch,int bus){channels[ch].bus=Math.max(0,Math.min(buses.length-1,bus));}
 private static float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
}