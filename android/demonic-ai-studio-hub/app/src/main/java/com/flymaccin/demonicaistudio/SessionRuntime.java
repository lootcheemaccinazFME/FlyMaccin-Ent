package com.flymaccin.demonicaistudio;
import android.content.Context;import java.io.File;
final class SessionRuntime {
 final Context context;final FmeFunProject project;final StudioCommandController commands;final PatternScheduler scheduler;final SamplerVoiceEngine sampler=new SamplerVoiceEngine();final WavRecorder recorder=new WavRecorder();final FmeFunRenderer renderer=new FmeFunRenderer();
 SessionRuntime(Context c,FmeFunProject p){context=c;project=p;commands=new StudioCommandController(p,p.mixer);scheduler=new PatternScheduler(p.transport);restoreSamples();}
 void restoreSamples(){for(FmeFunProject.SampleMap s:project.samples)if(s.source!=null&&!s.source.isEmpty()){sampler.load(context,s.pad,s.source);sampler.setPitch(s.pad,s.pitch);}}
 boolean startCapture(File file,int track){if(track<0||track>=project.mixer.channels.length||!project.mixer.channels[track].recordArm)return false;return recorder.start(file);}
 FmeFunProject.Clip stopCapture(int track){File f=recorder.stop();if(f==null)return null;long start=project.transport.tick();long frames=Math.max(1,(f.length()-44)/2);long ticks=Math.max(1,Math.round(frames/(double)AudioEngine.SAMPLE_RATE*project.transport.bpm()/60.0*UnifiedTransport.PPQ));FmeFunProject.Clip c=project.addClip(track,"audio",f.getAbsolutePath(),start,ticks);try{c.waveformBuckets=256;c.waveformKey=Integer.toHexString(java.util.Arrays.hashCode(WaveformCache.peaks(f,c.waveformBuckets)));}catch(Exception ignored){}return c;}
 void save()throws Exception{FmeFunStore.save(context,project);}
 void close(){sampler.release();if(recorder.isRunning())recorder.stop();}
}