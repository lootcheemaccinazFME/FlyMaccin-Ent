package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.graphics.*;
import android.view.*;

final class WaveformView extends View {
    private float[] peaks=new float[0]; private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); private float start=.0f,end=1f;
    WaveformView(Context c){super(c);p.setStrokeWidth(2f);}
    void setSamples(float[] samples){int buckets=512;peaks=new float[buckets];if(samples.length>0)for(int i=0;i<buckets;i++){int a=i*samples.length/buckets,b=Math.max(a+1,(i+1)*samples.length/buckets);float peak=0;for(int x=a;x<Math.min(b,samples.length);x++)peak=Math.max(peak,Math.abs(samples[x]));peaks[i]=peak;}invalidate();}
    void setTrim(float s,float e){start=Math.max(0,Math.min(1,s));end=Math.max(start,Math.min(1,e));invalidate();}
    @Override protected void onDraw(Canvas c){super.onDraw(c);c.drawColor(Color.rgb(12,14,20));p.setColor(Color.rgb(55,226,255));float mid=getHeight()/2f;for(int i=0;i<peaks.length;i++){float x=i*getWidth()/(float)Math.max(1,peaks.length-1),h=peaks[i]*mid;c.drawLine(x,mid-h,x,mid+h,p);}p.setColor(Color.argb(90,255,206,84));c.drawRect(0,0,start*getWidth(),getHeight(),p);c.drawRect(end*getWidth(),0,getWidth(),getHeight(),p);}
}