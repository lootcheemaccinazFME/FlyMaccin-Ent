package com.flymaccin.demonicaistudio;
import android.content.Context;import android.graphics.*;import android.view.*;
final class PianoRollTouchView extends View{
 final MidiClip clip;final UnifiedTransport transport;MidiClip.Note selected;float pxPerBeat=180f,row=28f;
 PianoRollTouchView(Context c,MidiClip m,UnifiedTransport t){super(c);clip=m;transport=t;}
 public boolean onTouchEvent(MotionEvent e){long tick=transport.snap(Math.round(e.getX()/pxPerBeat*UnifiedTransport.PPQ));int pitch=Math.max(0,Math.min(127,84-(int)(e.getY()/row)));if(e.getAction()==0){selected=hit(tick,pitch);if(selected==null)selected=clip.add(tick,UnifiedTransport.PPQ,pitch,110,0);invalidate();return true;}if(e.getAction()==2&&selected!=null){clip.move(selected,tick,pitch);invalidate();return true;}return true;}
 MidiClip.Note hit(long tick,int pitch){for(MidiClip.Note n:clip.notes)if(n.pitch==pitch&&tick>=n.startTick&&tick<=n.startTick+n.durationTicks)return n;return null;}
 protected void onDraw(Canvas c){c.drawColor(Color.rgb(10,11,16));Paint p=new Paint(1);for(MidiClip.Note n:clip.notes){p.setColor(Color.rgb(55,226,255));float l=n.startTick/(float)UnifiedTransport.PPQ*pxPerBeat,r=(n.startTick+n.durationTicks)/(float)UnifiedTransport.PPQ*pxPerBeat,top=(84-n.pitch)*row;c.drawRoundRect(l,top,r,top+row-2,5,5,p);}}
}