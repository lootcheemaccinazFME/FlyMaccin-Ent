package com.flymaccin.demonicaistudio;
import android.content.Context;import android.graphics.*;import android.view.*;
final class DrumStepTouchView extends View{
 final DrumPattern pattern; DrumStepTouchView(Context c,DrumPattern p){super(c);pattern=p;}
 public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;int s=Math.min(pattern.stepCount()-1,(int)(e.getX()/(getWidth()/(float)pattern.stepCount()))),l=Math.min(pattern.laneCount()-1,(int)(e.getY()/(getHeight()/(float)pattern.laneCount())));pattern.toggle(l,s);invalidate();return true;}
 protected void onDraw(Canvas c){c.drawColor(Color.rgb(8,9,14));Paint p=new Paint(1);float w=getWidth()/(float)pattern.stepCount(),h=getHeight()/(float)pattern.laneCount();for(int l=0;l<pattern.laneCount();l++)for(int s=0;s<pattern.stepCount();s++){p.setColor(pattern.step(l,s).active?Color.rgb(255,61,88):Color.rgb(38,41,52));c.drawRect(s*w+3,l*h+3,(s+1)*w-3,(l+1)*h-3,p);}}
}