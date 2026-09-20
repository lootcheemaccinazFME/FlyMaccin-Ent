package com.flymaccin.demonicdaw;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.security.MessageDigest;
import java.util.*;

public final class AssetStore {
  private final ProjectStore projects;
  public AssetStore(Context c,ProjectStore p){projects=p;}
  public synchronized String importFile(String projectId,String kind,InputStream in,String originalName,String provenance)throws Exception{
    File dir=projects.assetDir(projectId,kind);String id=UUID.randomUUID().toString();
    String ext=extension(originalName);File tmp=new File(dir,id+".part");MessageDigest md=MessageDigest.getInstance("SHA-256");long bytes=0;
    try(FileOutputStream out=new FileOutputStream(tmp)){byte[]b=new byte[65536];int n;while((n=in.read(b))>0){out.write(b,0,n);md.update(b,0,n);bytes+=n;}out.getFD().sync();}
    String sha=hex(md.digest());File dst=new File(dir,id+ext);if(!tmp.renameTo(dst))throw new IOException("ASSET_COMMIT_FAILED");
    JSONObject x=new JSONObject().put("id",id).put("kind",kind).put("file",dst.getName()).put("originalName",originalName==null?"":originalName)
      .put("sha256",sha).put("bytes",bytes).put("provenance",provenance==null?"unknown":provenance).put("immutableSource",true).put("createdAt",System.currentTimeMillis());
    File meta=new File(dir,id+".asset.json");try(FileOutputStream o=new FileOutputStream(meta)){o.write(x.toString(2).getBytes("UTF-8"));o.getFD().sync();}
    return x.toString();
  }
  public synchronized String importAndRegister(String projectId,String kind,InputStream in,String originalName,String provenance,long expectedRevision)throws Exception{
    JSONObject asset=new JSONObject(importFile(projectId,kind,in,originalName,provenance));
    try{
      JSONObject project=new JSONObject(projects.load(projectId));
      long current=project.optLong("revision",0);
      if(expectedRevision>=0&&expectedRevision!=current)throw new IllegalStateException("REVISION_STALE");
      JSONArray assets=project.optJSONArray("assets");if(assets==null){assets=new JSONArray();project.put("assets",assets);}
      assets.put(asset);
      String saved=projects.saveRevision(projectId,project.toString(),current);
      return new JSONObject().put("ok",true).put("asset",asset).put("state",new JSONObject(saved)).toString();
    }catch(Exception e){
      deleteAssetFiles(projectId,kind,asset.optString("id"));
      throw e;
    }
  }
  public synchronized boolean deleteAssetFiles(String projectId,String kind,String assetId)throws Exception{
    File dir=projects.assetDir(projectId,kind),meta=new File(dir,safe(assetId)+".asset.json");boolean ok=true;
    if(meta.isFile()){try{JSONObject x=new JSONObject(read(meta));File f=new File(dir,x.optString("file",""));if(f.isFile())ok=f.delete()&&ok;}catch(Exception ignored){}ok=meta.delete()&&ok;}
    return ok;
  }
  public synchronized String verify(String projectId,String kind,String assetId)throws Exception{
    File dir=projects.assetDir(projectId,kind),meta=new File(dir,safe(assetId)+".asset.json");if(!meta.isFile())return new JSONObject().put("ok",false).put("error","ASSET_NOT_FOUND").toString();
    JSONObject x=new JSONObject(read(meta));File f=new File(dir,x.getString("file"));if(!f.isFile())return new JSONObject().put("ok",false).put("error","ASSET_MISSING").put("asset",x).toString();
    String actual=sha256(f);return new JSONObject().put("ok",actual.equals(x.optString("sha256"))).put("expected",x.optString("sha256")).put("actual",actual).put("asset",x).toString();
  }
  private static String safe(String s){return s==null?"":s.replaceAll("[^A-Za-z0-9._-]","");}
  private static String extension(String n){if(n==null)return ".bin";int i=n.lastIndexOf('.');if(i<0||i<n.length()-8)return ".bin";return n.substring(i).toLowerCase(Locale.US).replaceAll("[^a-z0-9.]","");}
  private static String sha256(File f)throws Exception{MessageDigest md=MessageDigest.getInstance("SHA-256");try(InputStream in=new FileInputStream(f)){byte[]b=new byte[65536];int n;while((n=in.read(b))>0)md.update(b,0,n);}return hex(md.digest());}
  private static String hex(byte[]b){StringBuilder s=new StringBuilder();for(byte x:b)s.append(String.format(Locale.US,"%02x",x&255));return s.toString();}
  private static String read(File f)throws IOException{try(InputStream in=new FileInputStream(f);ByteArrayOutputStream o=new ByteArrayOutputStream()){byte[]b=new byte[32768];int n;while((n=in.read(b))>0)o.write(b,0,n);return o.toString("UTF-8");}}
}
