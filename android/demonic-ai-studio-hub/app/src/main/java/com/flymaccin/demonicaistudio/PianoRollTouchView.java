package com.flymaccin.demonicaistudio;
import android.content.Context;import android.graphics.*;import android.view.*;
final class PianoRollTouchView extends View{
 interface Editor{MidiClip.Note add(long tick,long duration,int pitch,int velocity,int channel);void move(MidiClip.Note note,long tick,int pitch);}
 final MidiClip clip;final UnifiedTransport transport;MidiClip.Note selected;float pxPerBeat=180f,row=28f;Editor editor;long originalTick;int originalPitch;
 PianoRollTouchView(Context c,MidiClip m,UnifiedTransport t){super(c);clip=m;transport=t;}
 void setEditor(Editor e){editor=e;}
 public boolean onTouchEvent(MotionEvent e){long tick=transport.snap(Math.round(e.getX()/pxPerBeat*UnifiedTransport.PPQ));int pitch=Math.max(0,Math.min(127,84-(int)(e.getY()/row)));int a=e.getActionMasked();
  if(a==MotionEvent.ACTION_DOWN){selected=hit(tick,pitch);if(selected==null)selected=editor!=null?editor.add(tick,UnifiedTransport.PPQ,pitch,110,0):clip.add(tick,UnifiedTransport.PPQ,pitch,110,0);originalTick=selected.startTick;originalPitch=selected.pitch;invalidate();return true;}
  if(a==MotionEvent.ACTION_MOVE&&selected!=null){clip.move(selected,tick,pitch);invalidate();return true;}
  if((a==MotionEvent.ACTION_UP||a==MotionEvent.ACTION_CANCEL)&&selected!=null){MidiClip.Note n=selected;long nt=n.startTick;int np=n.pitch;clip.move(n,originalTick,originalPitch);if(a==MotionEvent.ACTION_UP&&editor!=null)editor.move(n,nt,np);else if(a==MotionEvent.ACTION_UP)clip.move(n,nt,np);selected=null;invalidate();return true;}return true;}
 MidiClip.Note hit(long tick,int pitch){for(MidiClip.Note n:clip.notes)if(n.pitch==pitch&&tick>=n.startTick&&tick<=n.startTick+n.durationTicks)return n;return null;}
 protected void onDraw(Canvas c){c.drawColor(Color.rgb(10,11,16));Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);for(int i=0;i<48;i++){p.setColor(i%4==0?Color.rgb(35,38,48):Color.rgb(24,26,34));float x=i*(pxPerBeat/4);c.drawLine(x,0,x,getHeight(),p);}for(MidiClip.Note n:clip.notes){p.setColor(Color.rgb(55,226,255));float l=n.startTick/(float)UnifiedTransport.PPQ*pxPerBeat,r=(n.startTick+n.durationTicks)/(float)UnifiedTransport.PPQ*pxPerBeat,top=(84-n.pitch)*row;c.drawRoundRect(l,top,r,top+row-2,5,5,p);}}
}