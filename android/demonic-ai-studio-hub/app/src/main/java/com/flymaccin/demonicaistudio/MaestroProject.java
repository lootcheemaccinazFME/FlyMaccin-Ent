package com.flymaccin.demonicaistudio;

import org.json.*;
import java.util.*;

/** Android-side contract for Mac-Maestro generation and Director plans. */
final class MaestroProject {
    enum Kind { IMAGE, VIDEO, MUSIC, AUDIO, DIRECTOR }
    static final class Job {
        String id=UUID.randomUUID().toString(); Kind kind=Kind.IMAGE; String prompt=""; String model=""; String status="QUEUED"; int progress=0; final ArrayList<String> outputs=new ArrayList<>();
        JSONObject json() throws JSONException {JSONObject o=new JSONObject().put("id",id).put("kind",kind.name()).put("prompt",prompt).put("model",model).put("status",status).put("progress",progress);JSONArray a=new JSONArray();for(String s:outputs)a.put(s);return o.put("outputs",a);}
        static Job from(JSONObject o){Job j=new Job();j.id=o.optString("id",j.id);try{j.kind=Kind.valueOf(o.optString("kind","IMAGE"));}catch(Exception ignored){}j.prompt=o.optString("prompt","");j.model=o.optString("model","");j.status=o.optString("status","QUEUED");j.progress=o.optInt("progress",0);JSONArray a=o.optJSONArray("outputs");if(a!=null)for(int i=0;i<a.length();i++)j.outputs.add(a.optString(i));return j;}
    }
    static final class Character {String id=UUID.randomUUID().toString();String name="Character";String physical="";String wardrobe="";String voice="";}
    static final class Shot {String id=UUID.randomUUID().toString();int index;double durationSec=4;String sceneGoal="";String environment="";String visualStyle="";String mood="";String sourceMode="t2v";String continuity="independent";String videoPrompt="";String imagePrompt="";final ArrayList<String> windowPrompts=new ArrayList<>();}
    final ArrayList<Job> jobs=new ArrayList<>(); final ArrayList<Character> characters=new ArrayList<>(); final ArrayList<Shot> shots=new ArrayList<>();
    String toJson(){try{JSONObject root=new JSONObject().put("version",1);JSONArray a=new JSONArray();for(Job j:jobs)a.put(j.json());root.put("jobs",a);return root.toString();}catch(Exception e){return "{}";}}
    static MaestroProject fromJson(String raw){MaestroProject p=new MaestroProject();try{JSONArray a=new JSONObject(raw).optJSONArray("jobs");if(a!=null)for(int i=0;i<a.length();i++)p.jobs.add(Job.from(a.getJSONObject(i)));}catch(Exception ignored){}return p;}
}
