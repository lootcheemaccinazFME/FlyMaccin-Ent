package com.flymaccin.demonicaistudio;

import org.json.*;
import java.util.*;

/** Android-side persistent contract for Mac-Maestro generation and Director plans. */
final class MaestroProject {
    enum Kind { IMAGE, VIDEO, MUSIC, AUDIO, DIRECTOR }
    static final class Job {
        String id=UUID.randomUUID().toString(); Kind kind=Kind.IMAGE; String prompt=""; String model=""; String status="QUEUED"; int progress=0; final ArrayList<String> outputs=new ArrayList<>();
        JSONObject json() throws JSONException {JSONObject o=new JSONObject().put("id",id).put("kind",kind.name()).put("prompt",prompt).put("model",model).put("status",status).put("progress",progress);JSONArray a=new JSONArray();for(String s:outputs)a.put(s);return o.put("outputs",a);}
        static Job from(JSONObject o){Job j=new Job();j.id=o.optString("id",j.id);try{j.kind=Kind.valueOf(o.optString("kind","IMAGE"));}catch(Exception ignored){}j.prompt=o.optString("prompt","");j.model=o.optString("model","");j.status=o.optString("status","QUEUED");j.progress=Math.max(0,Math.min(100,o.optInt("progress",0)));JSONArray a=o.optJSONArray("outputs");if(a!=null)for(int i=0;i<a.length();i++)j.outputs.add(a.optString(i));return j;}
    }
    static final class Character {
        String id=UUID.randomUUID().toString(),name="Character",physical="",wardrobe="",voice="";
        JSONObject json() throws JSONException{return new JSONObject().put("id",id).put("name",name).put("physical",physical).put("wardrobe",wardrobe).put("voice",voice);}
        static Character from(JSONObject o){Character c=new Character();c.id=o.optString("id",c.id);c.name=o.optString("name","Character");c.physical=o.optString("physical","");c.wardrobe=o.optString("wardrobe","");c.voice=o.optString("voice","");return c;}
    }
    static final class Shot {
        String id=UUID.randomUUID().toString();int index;double durationSec=4;String sceneGoal="",environment="",visualStyle="",mood="",sourceMode="t2v",continuity="independent",videoPrompt="",imagePrompt="";final ArrayList<String> windowPrompts=new ArrayList<>();
        JSONObject json() throws JSONException{JSONObject o=new JSONObject().put("id",id).put("index",index).put("durationSec",durationSec).put("sceneGoal",sceneGoal).put("environment",environment).put("visualStyle",visualStyle).put("mood",mood).put("sourceMode",sourceMode).put("continuity",continuity).put("videoPrompt",videoPrompt).put("imagePrompt",imagePrompt);JSONArray w=new JSONArray();for(String s:windowPrompts)w.put(s);return o.put("windowPrompts",w);}
        static Shot from(JSONObject o){Shot s=new Shot();s.id=o.optString("id",s.id);s.index=o.optInt("index",0);s.durationSec=Math.max(.1,o.optDouble("durationSec",4));s.sceneGoal=o.optString("sceneGoal","");s.environment=o.optString("environment","");s.visualStyle=o.optString("visualStyle","");s.mood=o.optString("mood","");s.sourceMode=o.optString("sourceMode","t2v");s.continuity=o.optString("continuity","independent");s.videoPrompt=o.optString("videoPrompt","");s.imagePrompt=o.optString("imagePrompt","");JSONArray w=o.optJSONArray("windowPrompts");if(w!=null)for(int i=0;i<w.length();i++)s.windowPrompts.add(w.optString(i));return s;}
    }
    final ArrayList<Job> jobs=new ArrayList<>(); final ArrayList<Character> characters=new ArrayList<>(); final ArrayList<Shot> shots=new ArrayList<>();
    String toJson(){try{JSONObject root=new JSONObject().put("version",2);JSONArray j=new JSONArray();for(Job x:jobs)j.put(x.json());JSONArray c=new JSONArray();for(Character x:characters)c.put(x.json());JSONArray s=new JSONArray();for(Shot x:shots)s.put(x.json());return root.put("jobs",j).put("characters",c).put("shots",s).toString();}catch(Exception e){return "{}";}}
    static MaestroProject fromJson(String raw){MaestroProject p=new MaestroProject();try{JSONObject root=new JSONObject(raw==null?"{}":raw);JSONArray a=root.optJSONArray("jobs");if(a!=null)for(int i=0;i<a.length();i++)p.jobs.add(Job.from(a.getJSONObject(i)));JSONArray c=root.optJSONArray("characters");if(c!=null)for(int i=0;i<c.length();i++)p.characters.add(Character.from(c.getJSONObject(i)));JSONArray s=root.optJSONArray("shots");if(s!=null)for(int i=0;i<s.length();i++)p.shots.add(Shot.from(s.getJSONObject(i)));}catch(Exception ignored){}return p;}
}