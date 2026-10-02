package com.flymaccin.demonicaistudio;
import android.content.Context;import android.graphics.*;import android.view.*;
final class DrumStepTouchView extends View{
 interface Editor{void toggle(int lane,int step);void selected(int lane,int step);}
 final DrumPattern pattern;Editor editor;int selectedLane,selectedStep;
 DrumStepTouchView(Context c,DrumPattern p){super(c);pattern=p;}void setEditor(Editor e){editor=e;}
 public boolean onTouchEvent(MotionEvent e){if(e.getActionMasked()!=MotionEvent.ACTION_DOWN)return true;int s=Math.min(pattern.stepCount()-1,(int)(e.getX()/(getWidth()/(float)pattern.stepCount()))),l=Math.min(pattern.laneCount()-1,(int)(e.getY()/(getHeight()/(float)pattern.laneCount())));selectedLane=l;selectedStep=s;if(editor!=null){editor.toggle(l,s);editor.selected(l,s);}else pattern.toggle(l,s);invalidate();return true;}
 protected void onDraw(Canvas c){c.drawColor(Color.rgb(8,9,14));Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);float w=getWidth()/(float)pattern.stepCount(),h=getHeight()/(float)pattern.laneCount();for(int l=0;l<pattern.laneCount();l++)for(int s=0;s<pattern.stepCount();s++){DrumPattern.Step st=pattern.step(l,s);p.setColor(st.active?Color.rgb(255,61,88):Color.rgb(38,41,52));c.drawRect(s*w+3,l*h+3,(s+1)*w-3,(l+1)*h-3,p);if(l==selectedLane&&s==selectedStep){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(3);p.setColor(Color.WHITE);c.drawRect(s*w+2,l*h+2,(s+1)*w-2,(l+1)*h-2,p);p.setStyle(Paint.Style.FILL);}}}
}