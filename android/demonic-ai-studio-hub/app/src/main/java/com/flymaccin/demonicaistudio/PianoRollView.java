package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.util.List;

final class PianoRollView extends View {
    interface Listener { void onChanged(); void onPreview(int midi); }
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private List<ProductionProject.MidiNote> notes; private int bars=8; private Listener listener;
    private ProductionProject.MidiNote active; private float downX,downY; private int originalStart,originalDuration,originalNote; private int mode;
    PianoRollView(Context c){super(c);p.setTypeface(Typeface.MONOSPACE);setBackgroundColor(Color.rgb(10,11,17));}
    void bind(List<ProductionProject.MidiNote> n,int b){notes=n;bars=Math.max(1,b);invalidate();}
    void setListener(Listener l){listener=l;}
    private int total(){return bars*4*ProductionProject.PPQ;}
    private float rh(){return getHeight()/36f;}
    private RectF rect(ProductionProject.MidiNote n){int row=83-n.note;float x=n.startTick*getWidth()/(float)total(),w=Math.max(6,n.durationTick*getWidth()/(float)total());return new RectF(x,row*rh()+1,x+w,(row+1)*rh()-1);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);int rows=36,total=total();p.setStrokeWidth(1);for(int r=0;r<=rows;r++){p.setColor(r%12==0?Color.rgb(60,64,76):Color.rgb(31,34,43));c.drawLine(0,r*rh(),getWidth(),r*rh(),p);}for(int beat=0;beat<=bars*4;beat++){float x=beat*ProductionProject.PPQ*getWidth()/(float)total;p.setColor(beat%4==0?Color.rgb(90,72,120):Color.rgb(42,43,52));c.drawLine(x,0,x,getHeight(),p);}if(notes==null)return;for(ProductionProject.MidiNote n:notes){int row=83-n.note;if(row<0||row>=rows)continue;float vel=Math.max(0,Math.min(1,n.velocity/127f));p.setColor(Color.rgb((int)(110+100*vel),92,(int)(180+70*vel)));c.drawRoundRect(rect(n),4,4,p);}}
    private ProductionProject.MidiNote hit(float x,float y){if(notes==null)return null;for(int i=notes.size()-1;i>=0;i--){ProductionProject.MidiNote n=notes.get(i);if(rect(n).contains(x,y))return n;}return null;}
    private int qt(float x){int t=Math.round(x/Math.max(1,getWidth())*total()/120f)*120;return Math.max(0,Math.min(total()-120,t));}
    private int qn(float y){int row=(int)(y/Math.max(1,getHeight())*36);return Math.max(48,Math.min(83,83-row));}
    @Override public boolean onTouchEvent(MotionEvent e){
      if(notes==null)return true;
      if(e.getAction()==MotionEvent.ACTION_DOWN){downX=e.getX();downY=e.getY();active=hit(downX,downY);
        if(active==null){active=new ProductionProject.MidiNote(qn(downY),100,qt(downX),ProductionProject.PPQ);notes.add(active);mode=1;if(listener!=null)listener.onPreview(active.note);}
        else{RectF r=rect(active);mode=(downX>r.right-Math.max(18,r.width()*.25f))?2:1;originalStart=active.startTick;originalDuration=active.durationTick;originalNote=active.note;if(listener!=null)listener.onPreview(active.note);}
        invalidate();return true;}
      if(e.getAction()==MotionEvent.ACTION_MOVE&&active!=null){float dx=e.getX()-downX;if(mode==2){int dt=Math.round(dx/Math.max(1,getWidth())*total()/120f)*120;active.durationTick=Math.max(120,originalDuration+dt);}else{int dt=Math.round(dx/Math.max(1,getWidth())*total()/120f)*120;active.startTick=Math.max(0,Math.min(total()-120,originalStart+dt));active.note=Math.max(48,Math.min(83,originalNote-Math.round((e.getY()-downY)/rh())));}invalidate();return true;}
      if((e.getAction()==MotionEvent.ACTION_UP||e.getAction()==MotionEvent.ACTION_CANCEL)&&active!=null){if(listener!=null)listener.onChanged();active=null;invalidate();return true;}
      return true;
    }
    boolean deleteAt(float x,float y){ProductionProject.MidiNote n=hit(x,y);if(n==null)return false;notes.remove(n);if(listener!=null)listener.onChanged();invalidate();return true;}
}
