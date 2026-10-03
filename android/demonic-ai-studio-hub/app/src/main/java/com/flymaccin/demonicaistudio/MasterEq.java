package com.flymaccin.demonicaistudio;
final class MasterEq {
 float lowDb,midDb,highDb;void set(float low,float mid,float high){lowDb=low;midDb=mid;highDb=high;}
 void process(float[] s,int rate){float lg=db(lowDb),mg=db(midDb),hg=db(highDb),lpL=0,lpR=0,hpL=0,hpR=0;float a=(float)Math.exp(-2*Math.PI*220f/rate),b=(float)Math.exp(-2*Math.PI*3200f/rate);for(int i=0;i+1<s.length;i+=2){lpL=(1-a)*s[i]+a*lpL;lpR=(1-a)*s[i+1]+a*lpR;hpL=(1-b)*s[i]+b*hpL;hpR=(1-b)*s[i+1]+b*hpR;float midL=s[i]-lpL-hpL,midR=s[i+1]-lpR-hpR;s[i]=lpL*lg+midL*mg+hpL*hg;s[i+1]=lpR*lg+midR*mg+hpR*hg;}}private float db(float d){return(float)Math.pow(10,d/20f);}
}