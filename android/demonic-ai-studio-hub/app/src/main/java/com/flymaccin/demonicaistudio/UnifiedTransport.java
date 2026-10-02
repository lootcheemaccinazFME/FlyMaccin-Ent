package com.flymaccin.demonicaistudio;
final class UnifiedTransport {
 static final int PPQ=960; static final int[] LOOP_BAR_PRESETS={4,8,16,24,38,48};
 private double bpm=96.0;private int numerator=4,denominator=4;private long tick=0,loopStart=0,loopEnd=PPQ*4L*16L;private boolean looping=true;private int snapDivision=16,loopBars=16;
 double bpm(){return bpm;}long tick(){return tick;}int numerator(){return numerator;}int denominator(){return denominator;}long loopStart(){return loopStart;}long loopEnd(){return loopEnd;}boolean looping(){return looping;}int loopBars(){return loopBars;}int snapDivision(){return snapDivision;}
 void setBpm(double v){bpm=Math.max(30,Math.min(300,v));}void setTimeSignature(int n,int d){numerator=Math.max(1,n);denominator=Math.max(1,d);}void seek(long v){tick=Math.max(0,v);}
 long ticksPerBar(){return Math.max(1,Math.round(PPQ*4.0*numerator/denominator));}
 void setLoop(long start,long end,boolean enabled){loopStart=Math.max(0,start);loopEnd=Math.max(loopStart+1,end);looping=enabled;if(enabled)loopBars=(int)Math.max(1,Math.round((loopEnd-loopStart)/(double)ticksPerBar()));else loopBars=-1;}
 void setLoopBars(int bars){if(bars<=0){looping=false;loopBars=-1;return;}loopBars=bars;looping=true;loopEnd=loopStart+ticksPerBar()*bars;}
 void setLoopStart(long start){loopStart=Math.max(0,start);if(loopBars>0)loopEnd=loopStart+ticksPerBar()*loopBars;}
 void setSnapDivision(int d){snapDivision=Math.max(1,d);}long snap(long v){long g=Math.max(1,PPQ*4L/snapDivision);return Math.round(v/(double)g)*g;}
 long advanceFrames(int frames,int sampleRate){double q=frames/(double)sampleRate*bpm/60.0;tick+=Math.max(0,Math.round(q*PPQ));if(looping&&tick>=loopEnd)tick=loopStart+(tick-loopEnd);return tick;}
 long millisForTicks(long ticks){return Math.round((ticks/(double)PPQ)*60000.0/bpm);}
}