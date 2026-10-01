package com.flymaccin.demonicdaw;
import org.json.*;
import java.util.*;
public final class NativeProjectState {
  public static final int SCHEMA_VERSION=2;
  private JSONObject state;
  public NativeProjectState(String json)throws Exception{state=normalize(new JSONObject(json==null?"{}":json));validate(state);}
  public synchronized JSONObject snapshot()throws Exception{return new JSONObject(state.toString());}
  public synchronized long revision(){return state.optLong("revision",0);}
  public synchronized void replace(JSONObject next)throws Exception{JSONObject n=normalize(new JSONObject(next.toString()));validate(n);state=n;}
  public static JSONObject normalize(JSONObject p)throws Exception{
    if(!p.has("schemaVersion"))p.put("schemaVersion",SCHEMA_VERSION);
    if(!p.has("revision"))p.put("revision",0);
    if(!p.has("tracks"))p.put("tracks",new JSONArray()); if(!p.has("clips"))p.put("clips",new JSONArray());
    if(!p.has("assets"))p.put("assets",new JSONArray()); if(!p.has("buses"))p.put("buses",new JSONArray());
    if(!p.has("effects"))p.put("effects",new JSONArray()); if(!p.has("automation"))p.put("automation",new JSONArray());
    if(!p.has("tempo"))p.put("tempo",120); if(!p.has("selection"))p.put("selection",new JSONObject());
    return p;
  }
  public static void validate(JSONObject p)throws Exception{
    if(p.optInt("schemaVersion")!=SCHEMA_VERSION)throw new IllegalStateException("SCHEMA_UNSUPPORTED");
    double tempo=p.optDouble("tempo",120);if(tempo<20||tempo>400)throw new IllegalStateException("TEMPO_INVALID");
    Set<String> tracks=ids(p.getJSONArray("tracks"),"track"),assets=ids(p.getJSONArray("assets"),"asset"),clips=ids(p.getJSONArray("clips"),"clip");
    JSONArray cs=p.getJSONArray("clips");for(int i=0;i<cs.length();i++){JSONObject c=cs.getJSONObject(i);if(!tracks.contains(c.optString("trackId")))throw new IllegalStateException("CLIP_TRACK_DANGLING");String a=c.optString("assetId","");if(!a.isEmpty()&&!assets.contains(a))throw new IllegalStateException("CLIP_ASSET_DANGLING");}
    JSONObject sel=p.optJSONObject("selection");if(sel!=null){String t=sel.optString("trackId",""),c=sel.optString("clipId","");if(!t.isEmpty()&&!tracks.contains(t))throw new IllegalStateException("SELECTION_TRACK_DANGLING");if(!c.isEmpty()&&!clips.contains(c))throw new IllegalStateException("SELECTION_CLIP_DANGLING");}
  }
  private static Set<String> ids(JSONArray a,String kind)throws Exception{Set<String>s=new HashSet<>();for(int i=0;i<a.length();i++){String id=a.getJSONObject(i).optString("id","");if(id.isEmpty()||!s.add(id))throw new IllegalStateException(kind.toUpperCase()+"_ID_INVALID");}return s;}
}
