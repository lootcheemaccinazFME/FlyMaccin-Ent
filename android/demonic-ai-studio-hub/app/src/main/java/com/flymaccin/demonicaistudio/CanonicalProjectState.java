package com.flymaccin.demonicaistudio;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

final class CanonicalProjectState {
    static final int SCHEMA_VERSION = 1;
    final String projectId;
    long revision;
    String name;
    int bpm;
    String selectedTrackId;
    String selectedClipId;
    final LinkedHashMap<String, Track> tracks = new LinkedHashMap<>();
    final LinkedHashMap<String, Clip> clips = new LinkedHashMap<>();
    final LinkedHashMap<String, AssetRef> assets = new LinkedHashMap<>();

    CanonicalProjectState(String projectId, String name) {
        this.projectId = requireId(projectId);
        this.name = name == null ? "Demonic Session" : name;
        this.bpm = 96;
    }

    static String newId() { return UUID.randomUUID().toString(); }

    Track addTrack(String name, String routeId) {
        Track t = new Track(newId(), name, routeId);
        tracks.put(t.id, t); revision++; return t;
    }

    Clip addClip(String trackId, long startTick, long lengthTicks, String assetId) {
        if (!tracks.containsKey(trackId)) throw new IllegalArgumentException("unknown track");
        if (assetId != null && !assets.containsKey(assetId)) throw new IllegalArgumentException("unknown asset");
        Clip c = new Clip(newId(), trackId, Math.max(0,startTick), Math.max(1,lengthTicks), assetId);
        clips.put(c.id,c); revision++; return c;
    }

    void validate() {
        if (bpm < 20 || bpm > 400) throw new IllegalStateException("invalid bpm");
        for (Track t: tracks.values()) {
            if (t.routeId != null && !"master".equals(t.routeId) && !tracks.containsKey(t.routeId))
                throw new IllegalStateException("dangling route " + t.id);
        }
        for (Clip c: clips.values()) {
            if (!tracks.containsKey(c.trackId)) throw new IllegalStateException("dangling clip track");
            if (c.assetId != null && !assets.containsKey(c.assetId)) throw new IllegalStateException("dangling clip asset");
        }
    }

    JSONObject toJson() {
        validate();
        JSONObject root=new JSONObject();
        root.put("schemaVersion",SCHEMA_VERSION).put("projectId",projectId).put("revision",revision)
            .put("name",name).put("bpm",bpm).put("selectedTrackId",selectedTrackId==null?JSONObject.NULL:selectedTrackId)
            .put("selectedClipId",selectedClipId==null?JSONObject.NULL:selectedClipId);
        JSONArray ts=new JSONArray(); for(Track t:tracks.values()) ts.put(t.json()); root.put("tracks",ts);
        JSONArray cs=new JSONArray(); for(Clip c:clips.values()) cs.put(c.json()); root.put("clips",cs);
        JSONArray as=new JSONArray(); for(AssetRef a:assets.values()) as.put(a.json()); root.put("assets",as);
        return root;
    }

    static CanonicalProjectState fromJson(JSONObject root) {
        int schema=root.optInt("schemaVersion",0);
        if(schema!=SCHEMA_VERSION) throw new IllegalArgumentException("unsupported schema "+schema);
        CanonicalProjectState s=new CanonicalProjectState(root.getString("projectId"),root.optString("name","Demonic Session"));
        s.revision=root.optLong("revision",0); s.bpm=root.optInt("bpm",96);
        s.selectedTrackId=root.isNull("selectedTrackId")?null:root.optString("selectedTrackId",null);
        s.selectedClipId=root.isNull("selectedClipId")?null:root.optString("selectedClipId",null);
        JSONArray ts=root.optJSONArray("tracks"); if(ts!=null) for(int i=0;i<ts.length();i++){Track t=Track.from(ts.getJSONObject(i));s.tracks.put(t.id,t);}
        JSONArray as=root.optJSONArray("assets"); if(as!=null) for(int i=0;i<as.length();i++){AssetRef a=AssetRef.from(as.getJSONObject(i));s.assets.put(a.id,a);}
        JSONArray cs=root.optJSONArray("clips"); if(cs!=null) for(int i=0;i<cs.length();i++){Clip c=Clip.from(cs.getJSONObject(i));s.clips.put(c.id,c);}
        s.validate(); return s;
    }

    private static String requireId(String id){if(id==null||id.trim().isEmpty())throw new IllegalArgumentException("id");return id;}

    static final class Track {
        final String id; String name; String routeId; float gain=1f, pan=0f; boolean mute,solo;
        Track(String id,String name,String routeId){this.id=requireId(id);this.name=name==null?"Track":name;this.routeId=routeId;}
        JSONObject json(){return new JSONObject().put("id",id).put("name",name).put("routeId",routeId==null?JSONObject.NULL:routeId).put("gain",gain).put("pan",pan).put("mute",mute).put("solo",solo);}
        static Track from(JSONObject o){Track t=new Track(o.getString("id"),o.optString("name","Track"),o.isNull("routeId")?null:o.optString("routeId",null));t.gain=(float)o.optDouble("gain",1);t.pan=(float)o.optDouble("pan",0);t.mute=o.optBoolean("mute");t.solo=o.optBoolean("solo");return t;}
    }
    static final class Clip {
        final String id,trackId; long startTick,lengthTicks; final String assetId;
        Clip(String id,String trackId,long startTick,long lengthTicks,String assetId){this.id=requireId(id);this.trackId=requireId(trackId);this.startTick=startTick;this.lengthTicks=lengthTicks;this.assetId=assetId;}
        JSONObject json(){return new JSONObject().put("id",id).put("trackId",trackId).put("startTick",startTick).put("lengthTicks",lengthTicks).put("assetId",assetId==null?JSONObject.NULL:assetId);}
        static Clip from(JSONObject o){return new Clip(o.getString("id"),o.getString("trackId"),o.optLong("startTick"),o.optLong("lengthTicks",1),o.isNull("assetId")?null:o.optString("assetId",null));}
    }
    static final class AssetRef {
        final String id,path,sha256,provenance;
        AssetRef(String id,String path,String sha256,String provenance){this.id=requireId(id);this.path=path;this.sha256=sha256;this.provenance=provenance;}
        JSONObject json(){return new JSONObject().put("id",id).put("path",path).put("sha256",sha256).put("provenance",provenance);}
        static AssetRef from(JSONObject o){return new AssetRef(o.getString("id"),o.optString("path"),o.optString("sha256"),o.optString("provenance"));}
    }
}
