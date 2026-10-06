package com.flymaccin.demonicdaw;
import org.json.*;
import java.util.*;
public final class TimelineEngine {
  public static final int PPQ=960;
  public static long beatsToTicks(double beats){return Math.max(0,Math.round(beats*PPQ));}
  public static void validateClip(JSONObject c)throws Exception{if(c.optString("id").isEmpty()||c.optString("trackId").isEmpty())throw new IllegalStateException("CLIP_ID_INVALID");if(c.optDouble("start",0)<0||c.optDouble("bars",1)<=0)throw new IllegalStateException("CLIP_RANGE_INVALID");}
  public static JSONArray quantizeNotes(JSONArray notes,int division)throws Exception{int grid=Math.max(1,PPQ*4/Math.max(1,division));JSONArray out=new JSONArray();for(int i=0;i<notes.length();i++){JSONObject n=new JSONObject(notes.getJSONObject(i).toString());long t=n.optLong("tick",0);n.put("tick",Math.max(0,Math.round((double)t/grid)*grid));n.put("durationTicks",Math.max(1,n.optLong("durationTicks",PPQ/4)));n.put("velocity",Math.max(1,Math.min(127,n.optInt("velocity",100))));out.put(n);}return out;}
  private TimelineEngine(){}
}