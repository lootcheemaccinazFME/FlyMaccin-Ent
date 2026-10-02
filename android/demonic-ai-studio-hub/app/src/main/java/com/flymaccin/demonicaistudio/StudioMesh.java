package com.flymaccin.demonicaistudio;

import android.app.Activity;
import android.content.*;
import org.json.JSONObject;

public final class StudioMesh {
    public static final String ACTION="com.flymaccin.STUDIO_HANDOFF";
    public static final String MIME="application/vnd.flymaccin.studio+json";
    private StudioMesh(){}

    public static String receive(Activity a){
        Intent i=a.getIntent(); if(i==null)return null; String payload=null;
        if(ACTION.equals(i.getAction())||Intent.ACTION_SEND.equals(i.getAction())) payload=i.getStringExtra(Intent.EXTRA_TEXT);
        if(Intent.ACTION_VIEW.equals(i.getAction())&&i.getData()!=null) payload=i.getData().getQueryParameter("payload");
        if(payload!=null&&!payload.trim().isEmpty()){
            a.getSharedPreferences("fme_mesh",0).edit().putString("last_bundle",payload).apply();
            return payload;
        }
        return null;
    }
    public static String last(Activity a){return a.getSharedPreferences("fme_mesh",0).getString("last_bundle","");}
    public static String field(String payload,String key){try{return new JSONObject(payload).optString(key,"");}catch(Exception e){return "";}}
    public static String demonicBundle(String workspace,String legacyProject,String productionProject,String maestroProject){
        try{return new JSONObject()
            .put("mesh_version",1)
            .put("source","Demonic AI Studio")
            .put("workspace",workspace)
            .put("timestamp",System.currentTimeMillis())
            .put("legacy_project",legacyProject)
            .put("production_project",productionProject)
            .put("maestro_project",maestroProject).toString();
        }catch(Exception e){return "{\"mesh_version\":1,\"source\":\"Demonic AI Studio\"}";}
    }
    public static void send(Activity a,String targetPackage,String payload){
        Intent i=new Intent(ACTION); i.setType(MIME); i.setPackage(targetPackage); i.putExtra(Intent.EXTRA_TEXT,payload);
        try{a.startActivity(i);}catch(Exception e){android.widget.Toast.makeText(a,"Target studio APK not installed",android.widget.Toast.LENGTH_SHORT).show();}
    }
    public static void share(Activity a,String payload){
        Intent i=new Intent(Intent.ACTION_SEND); i.setType(MIME); i.putExtra(Intent.EXTRA_TEXT,payload);
        a.startActivity(Intent.createChooser(i,"Send through FME Studio Mesh"));
    }
}
