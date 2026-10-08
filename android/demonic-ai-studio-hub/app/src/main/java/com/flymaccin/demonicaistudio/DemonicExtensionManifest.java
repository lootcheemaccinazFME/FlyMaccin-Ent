package com.flymaccin.demonicaistudio;

import org.json.*;
import java.util.*;

public final class DemonicExtensionManifest {
    public final String id,name,version,entryPoint; public final Set<String> capabilities; public final int apiVersion;
    private DemonicExtensionManifest(String i,String n,String v,String e,int a,Set<String> c){id=i;name=n;version=v;entryPoint=e;apiVersion=a;capabilities=c;}
    public static DemonicExtensionManifest parse(String json) throws JSONException {
        JSONObject o=new JSONObject(json); String id=o.getString("id");
        if(!id.matches("[a-z0-9._-]{3,80}"))throw new JSONException("Invalid extension id");
        JSONArray a=o.optJSONArray("capabilities"); LinkedHashSet<String> caps=new LinkedHashSet<>();
        if(a!=null)for(int i=0;i<a.length();i++)caps.add(a.getString(i));
        return new DemonicExtensionManifest(id,o.getString("name"),o.getString("version"),o.optString("entryPoint",""),o.optInt("apiVersion",1),Collections.unmodifiableSet(caps));
    }
}
