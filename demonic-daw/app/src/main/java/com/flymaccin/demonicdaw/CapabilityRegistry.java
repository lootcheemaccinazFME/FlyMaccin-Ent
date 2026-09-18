package com.flymaccin.demonicdaw;
import org.json.*;
public final class CapabilityRegistry {
  public static String snapshot(boolean nativeReady,boolean fme)throws Exception{
    return new JSONObject().put("protocolVersion","1.1-native-foundation").put("nativeAudio",nativeReady).put("genericChannels",true).put("fmeCoreInstalled",fme)
      .put("permissions",new JSONArray().put("READ").put("EDIT").put("RECORD").put("RENDER").put("FILE").put("PUBLISH"))
      .put("nativeServices",new JSONArray().put("ProjectStore").put("SessionManager").put("CapabilityRegistry"))
      .put("renderFormats",new JSONArray().put("WAV"))
      .put("notes","Channel numbers have no instrument-category defaults.").toString();
  }
}
