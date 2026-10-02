package com.flymaccin.demonicaistudio;
import java.util.Random;
final class PatternScheduler {
 interface Sink{void drum(int lane,float velocity,float pitch);void midi(int pitch,int velocity,int channel,boolean on);}
 private final Random rng=new Random(); final UnifiedTransport transport; PatternScheduler(UnifiedTransport t){transport=t;}
 void fireStep(DrumPattern p,int step,Sink sink){for(int lane=0;lane<p.laneCount();lane++){DrumPattern.Step s=p.step(lane,step);if(!s.active||rng.nextInt(100)>=s.probability)continue;for(int r=0;r<Math.max(1,s.repeat);r++)sink.drum(lane,s.velocity/127f,s.pitch);}}
 void fireMidi(MidiClip clip,long from,long to,Sink sink){for(MidiClip.Note n:clip.notes)if(n.startTick>=from&&n.startTick<to)sink.midi(n.pitch,n.velocity,n.channel,true);}
}