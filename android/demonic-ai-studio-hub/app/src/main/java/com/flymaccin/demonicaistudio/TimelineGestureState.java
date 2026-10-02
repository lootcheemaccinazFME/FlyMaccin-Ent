package com.flymaccin.demonicaistudio;
final class TimelineGestureState {
 float zoom=1f,scrollX=0f; int scrollY=0; private static final float MIN=.25f,MAX=16f;
 void pinch(float scale,float focusX){float old=zoom;zoom=Math.max(MIN,Math.min(MAX,zoom*scale));if(old>0)scrollX=Math.max(0,(scrollX+focusX)*(zoom/old)-focusX);}
 void scroll(float dx,int dy){scrollX=Math.max(0,scrollX+dx);scrollY=Math.max(0,scrollY+dy);}
 long xToTick(float x){return Math.max(0,Math.round((x+scrollX)*UnifiedTransport.PPQ/(96f*zoom)));}
 float tickToX(long tick){return tick*(96f*zoom)/UnifiedTransport.PPQ-scrollX;}
 void dragClip(FmeFunProject.Clip c,float x,UnifiedTransport t){c.startTick=t.snap(xToTick(x));}
}