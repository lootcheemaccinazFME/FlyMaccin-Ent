package com.flymaccin.demonicaistudio;
import java.util.*;
final class AutomationLane {
 static final class Point { long tick; float value; Point(long t,float v){tick=Math.max(0,t);value=v;} }
 final String target; final ArrayList<Point> points=new ArrayList<>();
 AutomationLane(String target){this.target=target==null?"":target;}
 void put(long tick,float value){Point found=null;for(Point p:points)if(p.tick==tick){found=p;break;}if(found==null)points.add(new Point(tick,value));else found.value=value;Collections.sort(points,(a,b)->Long.compare(a.tick,b.tick));}
 float valueAt(long tick,float fallback){if(points.isEmpty())return fallback;Point a=null,b=null;for(Point p:points){if(p.tick<=tick)a=p;if(p.tick>=tick){b=p;break;}}if(a==null)return points.get(0).value;if(b==null||a==b)return a.value;float x=(tick-a.tick)/(float)Math.max(1,b.tick-a.tick);return a.value+(b.value-a.value)*x;}
}