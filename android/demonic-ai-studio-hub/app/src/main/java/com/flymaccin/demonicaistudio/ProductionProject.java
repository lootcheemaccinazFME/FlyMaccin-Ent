package com.flymaccin.demonicaistudio;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

final class ProductionProject {
 static final int PPQ=480;
 enum InstrumentType { DRUMS,BASS,GUITAR,PIANO,SYNTH,SAMPLER,VOCALS,AUDIO,MIDI,SFZ,SF2,WAV }
 static final class MidiNote {
  String id=UUID.randomUUID().toString(); int note,velocity,startTick,durationTick;
  MidiNote(int n,int v,int s,int d){note=n;velocity=v;startTick=s;durationTick=d;}
  JSONObject json() throws Exception{return new JSONObject().put("id",id).put("note",note).put("velocity",velocity).put("start",startTick).put("duration",durationTick);}
  static MidiNote from(JSONObject o){MidiNote n=new MidiNote(o.optInt("note",60),o.optInt("velocity",100),o.optInt("start",0),o.optInt("duration",PPQ));n.id=o.optString("id",n.id);return n;}
 }
 static final class AudioClip {
  String id=UUID.randomUUID().toString(),path=""; long trimStartMs=0,trimEndMs=-1; int startTick=0; float gain=1f,fadeInMs=0,fadeOutMs=0,stretch=1f; boolean reverse=false;
  JSONObject json() throws Exception{return new JSONObject().put("id",id).put("path",path).put("trimStartMs",trimStartMs).put("trimEndMs",trimEndMs).put("startTick",startTick).put("gain",gain).put("fadeInMs",fadeInMs).put("fadeOutMs",fadeOutMs).put("stretch",stretch).put("reverse",reverse);}
  static AudioClip from(JSONObject o){AudioClip c=new AudioClip();c.id=o.optString("id",c.id);c.path=o.optString("path","");c.trimStartMs=o.optLong("trimStartMs",0);c.trimEndMs=o.optLong("trimEndMs",-1);c.startTick=o.optInt("startTick",0);c.gain=(float)o.optDouble("gain",1);c.fadeInMs=(float)o.optDouble("fadeInMs",0);c.fadeOutMs=(float)o.optDouble("fadeOutMs",0);c.stretch=(float)o.optDouble("stretch",1);c.reverse=o.optBoolean("reverse",false);return c;}
 }
 static final class Instrument {
  InstrumentType type=InstrumentType.PIANO;String path="";int rootMidi=60,bank=0,preset=0;
  JSONObject json() throws Exception{return new JSONObject().put("type",type.name()).put("path",path).put("rootMidi",rootMidi).put("bank",bank).put("preset",preset);}
  static Instrument from(JSONObject o){Instrument i=new Instrument();try{i.type=InstrumentType.valueOf(o.optString("type","PIANO"));}catch(Exception ignored){}i.path=o.optString("path","");i.rootMidi=o.optInt("rootMidi",60);i.bank=o.optInt("bank",0);i.preset=o.optInt("preset",0);return i;}
 }
 static final class Bus {
  String id="BUS_"+UUID.randomUUID().toString(),name="Bus";float volume=1f,pan=0,eqLow=0,eqMid=0,eqHigh=0,compressor=0,drive=0,reverb=0,delay=0;boolean mute=false;
  JSONObject json() throws Exception{return new JSONObject().put("id",id).put("name",name).put("volume",volume).put("pan",pan).put("eqLow",eqLow).put("eqMid",eqMid).put("eqHigh",eqHigh).put("compressor",compressor).put("drive",drive).put("reverb",reverb).put("delay",delay).put("mute",mute);}
  static Bus from(JSONObject o){Bus b=new Bus();b.id=o.optString("id",b.id);b.name=o.optString("name","Bus");b.volume=(float)o.optDouble("volume",1);b.pan=(float)o.optDouble("pan",0);b.eqLow=(float)o.optDouble("eqLow",0);b.eqMid=(float)o.optDouble("eqMid",0);b.eqHigh=(float)o.optDouble("eqHigh",0);b.compressor=(float)o.optDouble("compressor",0);b.drive=(float)o.optDouble("drive",0);b.reverb=(float)o.optDouble("reverb",0);b.delay=(float)o.optDouble("delay",0);b.mute=o.optBoolean("mute",false);return b;}
 }
 static final class Channel {
  String id="CH_"+UUID.randomUUID().toString(),name="Track",route="MASTER";final Instrument instrument=new Instrument();final List<MidiNote> notes=new ArrayList<>();final List<AudioClip> audio=new ArrayList<>();
  float volume=.85f,pan=0,eqLow=0,eqMid=0,eqHigh=0,compressor=0,drive=0,reverb=.12f,delay=.08f;boolean mute=false,solo=false,bypassFx=false,recordArm=false;
  JSONObject json() throws Exception{JSONObject o=new JSONObject().put("id",id).put("name",name).put("route",route).put("instrument",instrument.json()).put("volume",volume).put("pan",pan).put("eqLow",eqLow).put("eqMid",eqMid).put("eqHigh",eqHigh).put("compressor",compressor).put("drive",drive).put("reverb",reverb).put("delay",delay).put("mute",mute).put("solo",solo).put("bypassFx",bypassFx).put("recordArm",recordArm);JSONArray n=new JSONArray();for(MidiNote x:notes)n.put(x.json());JSONArray a=new JSONArray();for(AudioClip x:audio)a.put(x.json());return o.put("notes",n).put("audio",a);}
  static Channel from(JSONObject o){Channel c=new Channel();c.id=o.optString("id",c.id);c.name=o.optString("name","Track");c.route=o.optString("route","MASTER");Instrument i=Instrument.from(o.optJSONObject("instrument")==null?new JSONObject():o.optJSONObject("instrument"));c.instrument.type=i.type;c.instrument.path=i.path;c.instrument.rootMidi=i.rootMidi;c.instrument.bank=i.bank;c.instrument.preset=i.preset;c.volume=(float)o.optDouble("volume",.85);c.pan=(float)o.optDouble("pan",0);c.eqLow=(float)o.optDouble("eqLow",0);c.eqMid=(float)o.optDouble("eqMid",0);c.eqHigh=(float)o.optDouble("eqHigh",0);c.compressor=(float)o.optDouble("compressor",0);c.drive=(float)o.optDouble("drive",0);c.reverb=(float)o.optDouble("reverb",.12);c.delay=(float)o.optDouble("delay",.08);c.mute=o.optBoolean("mute",false);c.solo=o.optBoolean("solo",false);c.bypassFx=o.optBoolean("bypassFx",false);c.recordArm=o.optBoolean("recordArm",false);JSONArray n=o.optJSONArray("notes");if(n!=null)for(int x=0;x<n.length();x++)c.notes.add(MidiNote.from(n.optJSONObject(x)));JSONArray a=o.optJSONArray("audio");if(a!=null)for(int x=0;x<a.length();x++)c.audio.add(AudioClip.from(a.optJSONObject(x)));return c;}
 }
 int bars=8,bpm=100,numerator=4,playheadTick=0,loopStartTick=0,loopEndTick=8*4*PPQ;boolean loop=true,metronome=false;final List<Channel> channels=new ArrayList<>();final List<Bus> buses=new ArrayList<>();
 ProductionProject(){Channel c=new Channel();c.name="Instrument 1";channels.add(c);}
 Channel addChannel(String name){Channel c=new Channel();c.name=name;channels.add(c);return c;}
 Bus addBus(String name){Bus b=new Bus();b.name=name;buses.add(b);return b;}
 String toJson(){try{JSONArray a=new JSONArray();for(Channel c:channels)a.put(c.json());JSONArray bs=new JSONArray();for(Bus b:buses)bs.put(b.json());return new JSONObject().put("version",4).put("bars",bars).put("bpm",bpm).put("numerator",numerator).put("playheadTick",playheadTick).put("loopStartTick",loopStartTick).put("loopEndTick",loopEndTick).put("loop",loop).put("metronome",metronome).put("channels",a).put("buses",bs).toString();}catch(Exception e){return "{}";}}
 static ProductionProject fromJson(String raw){ProductionProject p=new ProductionProject();if(raw==null||raw.isEmpty())return p;try{JSONObject o=new JSONObject(raw);p.bars=Math.max(1,o.optInt("bars",8));p.bpm=Math.max(40,Math.min(240,o.optInt("bpm",100)));p.numerator=Math.max(1,Math.min(16,o.optInt("numerator",4)));p.playheadTick=Math.max(0,o.optInt("playheadTick",0));p.loopStartTick=Math.max(0,o.optInt("loopStartTick",0));p.loopEndTick=Math.max(p.loopStartTick+PPQ,o.optInt("loopEndTick",p.bars*4*PPQ));p.loop=o.optBoolean("loop",true);p.metronome=o.optBoolean("metronome",false);JSONArray a=o.optJSONArray("channels");if(a!=null){p.channels.clear();for(int x=0;x<a.length();x++)p.channels.add(Channel.from(a.optJSONObject(x)));}JSONArray bs=o.optJSONArray("buses");if(bs!=null)for(int x=0;x<bs.length();x++)p.buses.add(Bus.from(bs.optJSONObject(x)));if(p.channels.isEmpty())p.addChannel("Instrument 1");}catch(Exception ignored){}return p;}
}
