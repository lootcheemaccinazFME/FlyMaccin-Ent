package com.flymaccin.demonicaistudio;
final class StudioCommandController {
 final FmeFunProject project; final MixerGraph mixer; final CommandStack history=new CommandStack();
 StudioCommandController(FmeFunProject p,MixerGraph m){project=p;mixer=m;}
 void moveClip(FmeFunProject.Clip c,long tick,int track){final long ot=c.startTick;final int otr=c.track;final long nt=project.transport.snap(tick);history.execute(new CommandStack.Command(){public void apply(){c.startTick=nt;c.track=Math.max(0,track);}public void revert(){c.startTick=ot;c.track=otr;}public String label(){return "Move clip";}});}
 void setGain(int ch,float value){final float old=mixer.channels[ch].gain;history.execute(new FloatCommand("Track gain",old,value,v->mixer.gain(ch,v)));}
 void setPan(int ch,float value){final float old=mixer.channels[ch].pan;history.execute(new FloatCommand("Track pan",old,value,v->mixer.pan(ch,v)));}
 void setSend(int ch,int bus,float value){final float old=mixer.channels[ch].sends[bus];history.execute(new FloatCommand("Send",old,value,v->mixer.send(ch,bus,v)));}
 void toggleArm(int ch){final boolean old=mixer.channels[ch].recordArm;history.execute(new CommandStack.Command(){public void apply(){mixer.channels[ch].recordArm=!old;}public void revert(){mixer.channels[ch].recordArm=old;}public String label(){return "Record arm";}});}
 void toggleMonitor(int ch){final boolean old=mixer.channels[ch].monitor;history.execute(new CommandStack.Command(){public void apply(){mixer.channels[ch].monitor=!old;}public void revert(){mixer.channels[ch].monitor=old;}public String label(){return "Monitor";}});}
 interface Setter{void set(float v);} static final class FloatCommand implements CommandStack.Command{final String n;final float a,b;final Setter s;FloatCommand(String n,float a,float b,Setter s){this.n=n;this.a=a;this.b=b;this.s=s;}public void apply(){s.set(b);}public void revert(){s.set(a);}public String label(){return n;}}
}