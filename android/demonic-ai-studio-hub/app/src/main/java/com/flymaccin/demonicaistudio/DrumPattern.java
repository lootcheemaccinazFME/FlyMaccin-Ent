package com.flymaccin.demonicaistudio;
final class DrumPattern {
 static final class Step { boolean active; int velocity=110,probability=100,microShiftTicks=0,repeat=1; float pitch=0f; }
 private final Step[][] lanes; private int swing;
 DrumPattern(int laneCount,int stepCount){lanes=new Step[Math.max(1,laneCount)][Math.max(1,stepCount)];for(int l=0;l<lanes.length;l++)for(int s=0;s<lanes[l].length;s++)lanes[l][s]=new Step();}
 Step step(int lane,int step){return lanes[Math.floorMod(lane,lanes.length)][Math.floorMod(step,lanes[0].length)];}
 void toggle(int lane,int step){Step s=step(lane,step);s.active=!s.active;} int laneCount(){return lanes.length;} int stepCount(){return lanes[0].length;}
 int swing(){return swing;} void setSwing(int v){swing=Math.max(0,Math.min(75,v));}
}