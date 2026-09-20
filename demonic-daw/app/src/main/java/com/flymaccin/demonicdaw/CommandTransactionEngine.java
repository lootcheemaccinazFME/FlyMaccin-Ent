package com.flymaccin.demonicdaw;

import org.json.*;
import java.util.*;

/**
 * Native-authoritative command transaction coordinator.
 * One transaction validates the expected project revision, applies supported
 * structural commands in memory, and commits exactly one new revision.
 * It never touches realtime DSP state.
 */
public final class CommandTransactionEngine {
  private final ProjectStore projects;
  public CommandTransactionEngine(ProjectStore projects){this.projects=projects;}

  public synchronized String execute(String projectId,String commandsJson,long expectedRevision)throws Exception{
    JSONObject base=new JSONObject(projects.load(projectId));
    long current=base.optLong("revision",0);
    if(expectedRevision>=0&&expectedRevision!=current)throw new IllegalStateException("REVISION_STALE");
    JSONArray commands=new JSONArray(commandsJson==null?"[]":commandsJson);
    JSONObject next=new JSONObject(base.toString());
    JSONArray results=new JSONArray();
    for(int i=0;i<commands.length();i++)results.put(apply(next,commands.getJSONObject(i)));
    String saved=projects.saveRevision(projectId,next.toString(),current);
    JSONObject canonical=new JSONObject(saved);
    return new JSONObject().put("ok",true).put("revision",canonical.optLong("revision"))
      .put("state",canonical).put("results",results).toString();
  }

  private JSONObject apply(JSONObject p,JSONObject c)throws Exception{
    String command=c.optString("command","");
    JSONObject a=c.optJSONObject("args");if(a==null)a=new JSONObject();
    switch(command){
      case "project.rename":
        p.put("name",a.optString("name","Untitled Project"));
        return ok(command);
      case "track.create":{
        JSONArray tracks=p.getJSONArray("tracks");
        JSONObject t=new JSONObject().put("id",id("track")).put("name",a.optString("name","Track "+(tracks.length()+1)))
          .put("type",a.optString("type","instrument")).put("channel",JSONObject.NULL)
          .put("mute",false).put("solo",false).put("gain",1.0).put("pan",0.0);
        tracks.put(t);return ok(command).put("track",t);
      }
      case "track.rename":{
        JSONObject t=find(p.getJSONArray("tracks"),a.optString("id"));t.put("name",a.optString("name",t.optString("name")));
        return ok(command).put("trackId",t.getString("id"));
      }
      case "track.delete":{
        String tid=a.optString("id");remove(p.getJSONArray("tracks"),tid);
        JSONArray clips=p.getJSONArray("clips");for(int i=clips.length()-1;i>=0;i--)if(tid.equals(clips.getJSONObject(i).optString("trackId")))clips.remove(i);
        return ok(command).put("trackId",tid);
      }
      case "routing.setChannel":{
        JSONObject t=find(p.getJSONArray("tracks"),a.optString("trackId",a.optString("id")));
        if(a.isNull("channel")||!a.has("channel"))t.put("channel",JSONObject.NULL);
        else {int ch=a.getInt("channel");if(ch<0||ch>15)throw new IllegalArgumentException("INVALID_CHANNEL");t.put("channel",ch);}
        return ok(command).put("trackId",t.getString("id")).put("channel",t.opt("channel"));
      }
      case "mixer.set":{
        JSONObject t=find(p.getJSONArray("tracks"),a.optString("id"));
        if(a.has("gain"))t.put("gain",clamp(a.getDouble("gain"),0,2));
        if(a.has("pan"))t.put("pan",clamp(a.getDouble("pan"),-1,1));
        if(a.has("mute"))t.put("mute",a.getBoolean("mute"));if(a.has("solo"))t.put("solo",a.getBoolean("solo"));
        return ok(command).put("trackId",t.getString("id"));
      }
      case "clip.create":{
        String tid=a.optString("trackId");find(p.getJSONArray("tracks"),tid);
        JSONObject clip=new JSONObject().put("id",id("clip")).put("name",a.optString("name","Clip"))
          .put("type",a.optString("type","midi")).put("trackId",tid).put("start",Math.max(0,a.optDouble("start",0)))
          .put("bars",Math.max(.0625,a.optDouble("bars",1))).put("repeat",Math.max(1,a.optDouble("repeat",1)))
          .put("notes",a.optJSONArray("notes")==null?new JSONArray():a.optJSONArray("notes"));
        p.getJSONArray("clips").put(clip);return ok(command).put("clip",clip);
      }
      case "clip.move": case "clip.resize": case "clip.repeat":{
        JSONObject clip=find(p.getJSONArray("clips"),a.optString("id"));
        if(a.has("start"))clip.put("start",Math.max(0,a.getDouble("start")));
        if(a.has("bars"))clip.put("bars",Math.max(.0625,a.getDouble("bars")));
        if(a.has("repeat"))clip.put("repeat",Math.max(1,a.getDouble("repeat")));
        return ok(command).put("clipId",clip.getString("id"));
      }
      default:throw new IllegalArgumentException("COMMAND_NOT_NATIVE_TRANSACTIONAL:"+command);
    }
  }
  private static JSONObject ok(String command)throws Exception{return new JSONObject().put("ok",true).put("command",command);}
  private static String id(String p){return p+"_"+UUID.randomUUID().toString();}
  private static double clamp(double v,double lo,double hi){return Math.max(lo,Math.min(hi,v));}
  private static JSONObject find(JSONArray a,String id)throws Exception{for(int i=0;i<a.length();i++){JSONObject x=a.getJSONObject(i);if(id.equals(x.optString("id")))return x;}throw new IllegalArgumentException("OBJECT_NOT_FOUND:"+id);}
  private static void remove(JSONArray a,String id)throws Exception{for(int i=0;i<a.length();i++)if(id.equals(a.getJSONObject(i).optString("id"))){a.remove(i);return;}throw new IllegalArgumentException("OBJECT_NOT_FOUND:"+id);}
}
