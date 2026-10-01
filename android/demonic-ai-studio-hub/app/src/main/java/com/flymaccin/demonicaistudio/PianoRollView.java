package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.util.List;

final class PianoRollView extends View {
    interface Listener { void onNote(int midi,int tick); }
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); private List<ProductionProject.MidiNote> notes; private int bars=8; private Listener listener;
    PianoRollView(Context c){super(c);p.setTypeface(Typeface.MONOSPACE);setBackgroundColor(Color.rgb(10,11,17));}
    void bind(List<ProductionProject.MidiNote> n,int b){notes=n;bars=Math.max(1,b);invalidate();}
    void setListener(Listener l){listener=l;}
    @Override protected void onDraw(Canvas c){super.onDraw(c);int rows=36;float rh=getHeight()/(float)rows;int total=bars*4*ProductionProject.PPQ;p.setStrokeWidth(1);for(int r=0;r<=rows;r++){p.setColor(r%12==0?Color.rgb(60,64,76):Color.rgb(31,34,43));c.drawLine(0,r*rh,getWidth(),r*rh,p);}for(int beat=0;beat<=bars*4;beat++){float x=beat*ProductionProject.PPQ*getWidth()/(float)total;p.setColor(beat%4==0?Color.rgb(90,72,120):Color.rgb(42,43,52));c.drawLine(x,0,x,getHeight(),p);}if(notes==null)return;for(ProductionProject.MidiNote n:notes){int row=83-n.note;if(row<0||row>=rows)continue;float x=n.startTick*getWidth()/(float)total,w=Math.max(4,n.durationTick*getWidth()/(float)total);p.setColor(Color.rgb(164,92,255));c.drawRoundRect(x,row*rh+1,x+w,(row+1)*rh-1,4,4,p);}}
    @Override public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_DOWN||listener==null)return true;int total=bars*4*ProductionProject.PPQ;int tick=Math.round(e.getX()/Math.max(1,getWidth())*total/120f)*120;int row=(int)(e.getY()/Math.max(1,getHeight())*36);listener.onNote(Math.max(48,Math.min(83,83-row)),Math.max(0,tick));return true;}
}