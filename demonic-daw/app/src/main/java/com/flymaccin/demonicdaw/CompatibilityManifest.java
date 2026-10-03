package com.flymaccin.demonicdaw;
import org.json.JSONException;
import org.json.JSONObject;
public final class CompatibilityManifest {
 public static final int PROJECT_SCHEMA_MIN=2,PROJECT_SCHEMA_MAX=2,DCP_MIN=1,DCP_MAX=1;
 public static JSONObject current(){try{return new JSONObject().put("projectSchemaMin",PROJECT_SCHEMA_MIN).put("projectSchemaMax",PROJECT_SCHEMA_MAX).put("dcpMin",DCP_MIN).put("dcpMax",DCP_MAX).put("aiEnabled",false).put("genericChannels",true);}catch(JSONException e){throw new IllegalStateException("compatibility manifest",e);}}
 private CompatibilityManifest(){}
}