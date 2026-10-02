package com.flymaccin.demonicaistudio;
import android.media.SoundPool; import android.content.Context; import java.util.*;
final class SamplerVoiceEngine {
 private final SoundPool pool=new SoundPool.Builder().setMaxStreams(24).build(); private final Map<Integer,Integer> sounds=new HashMap<>(); private final Map<Integer,Float> pitch=new HashMap<>();
 void load(Context c,int pad,String path){Integer old=sounds.remove(pad);if(old!=null)pool.unload(old);int id=pool.load(path,1);sounds.put(pad,id);pitch.put(pad,1f);}
 int trigger(int pad,float velocity,float pan){Integer id=sounds.get(pad);if(id==null)return 0;float rate=Math.max(.5f,Math.min(2f,pitch.getOrDefault(pad,1f)));float v=Math.max(0,Math.min(1,velocity));float l=v*(pan<=0?1:1-pan),r=v*(pan>=0?1:1+pan);return pool.play(id,l,r,1,0,rate);}
 void setPitch(int pad,float semitones){pitch.put(pad,(float)Math.pow(2,semitones/12f));} void release(){pool.release();}
}