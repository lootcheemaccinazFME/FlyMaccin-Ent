package com.flymaccin.demonicdaw;

import android.content.Context;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ProjectStore {
  private final File root;
  public ProjectStore(Context c){root=new File(c.getFilesDir(),"projects");root.mkdirs();}
  private static String safe(String id){return id==null?"":id.replaceAll("[^A-Za-z0-9._-]","");}
  public synchronized String create(String name)throws Exception{
    String id=UUID.randomUUID().toString();File d=dir(id);d.mkdirs();
    for(String n:new String[]{"audio","recordings","renders","autosave","cache"})new File(d,n).mkdirs();
    JSONObject p=canonical(new JSONObject().put("schemaVersion",2).put("id",id).put("name",name==null?"Untitled":name).put("revision",0));
    commit(d,p,null);return id;
  }
  public synchronized String saveRevision(String id,String json,long expectedRevision)throws Exception{
    File d=dir(id);if(!d.exists())throw new IOException("PROJECT_NOT_FOUND");
    recover(d);JSONObject current=readJson(new File(d,"project.json"));long rev=current.optLong("revision",0);
    if(expectedRevision>=0&&expectedRevision!=rev)throw new IllegalStateException("REVISION_STALE");
    JSONObject next=canonical(new JSONObject(json));next.put("id",id).put("revision",rev+1);
    commit(d,next,current);return next.toString();
  }
  public synchronized boolean save(String id,String json)throws Exception{saveRevision(id,json,-1);return true;}
  public synchronized String load(String id)throws Exception{File d=dir(id);recover(d);File f=new File(d,"project.json");return f.isFile()?read(f):"{}";}
  public synchronized String recoverProject(String id)throws Exception{return new JSONObject().put("recovered",recover(dir(id))).put("project",new JSONObject(load(id))).toString();}
  public synchronized String list(){
    JSONArray a=new JSONArray();File[]xs=root.listFiles();if(xs!=null)for(File d:xs)if(d.isDirectory())try{recover(d);File f=new File(d,"project.json");if(f.isFile()){JSONObject p=readJson(f);a.put(new JSONObject().put("id",p.optString("id",d.getName())).put("name",p.optString("name","Untitled")).put("revision",p.optLong("revision",0)));}}catch(Exception ignored){}return a.toString();
  }
  public File assetDir(String id,String kind)throws IOException{if(!kind.matches("audio|recordings|renders|cache"))throw new IOException("Invalid asset kind");File d=new File(dir(id),kind);d.mkdirs();return d;}
  private JSONObject canonical(JSONObject p)throws Exception{
    if(!p.has("schemaVersion"))p.put("schemaVersion",2);if(!p.has("tracks"))p.put("tracks",new JSONArray());if(!p.has("clips"))p.put("clips",new JSONArray());
    if(!p.has("buses"))p.put("buses",new JSONArray());if(!p.has("effects"))p.put("effects",new JSONArray());if(!p.has("automation"))p.put("automation",new JSONArray());
    if(!p.has("tempo"))p.put("tempo",120);if(!p.has("timeSignature"))p.put("timeSignature",new JSONObject().put("numerator",4).put("denominator",4));
    return p;
  }
  private void commit(File d,JSONObject next,JSONObject previous)throws Exception{
    File journal=new File(d,"autosave/project.wal.json"),project=new File(d,"project.json"),backup=new File(d,"autosave/project.previous.json");
    JSONObject wal=new JSONObject().put("state","PREPARED").put("targetRevision",next.optLong("revision")).put("next",next).put("createdAt",System.currentTimeMillis());
    if(previous!=null)wal.put("previousRevision",previous.optLong("revision"));
    writeAtomic(journal,wal.toString());if(previous!=null)writeAtomic(backup,previous.toString());
    writeAtomic(project,next.toString(2));wal.put("state","COMMITTED");writeAtomic(journal,wal.toString());if(!journal.delete())journal.deleteOnExit();
  }
  private boolean recover(File d)throws Exception{
    File journal=new File(d,"autosave/project.wal.json");if(!journal.isFile())return false;JSONObject wal=readJson(journal),next=wal.optJSONObject("next"),project=new File(d,"project.json");
    if(next==null){journal.delete();return false;}long target=wal.optLong("targetRevision",-1),current=-1;if(project.isFile())try{current=readJson(project).optLong("revision",-1);}catch(Exception ignored){}
    if(current<target)writeAtomic(project,canonical(next).toString(2));journal.delete();return true;
  }
  private File dir(String id)throws IOException{String s=safe(id);if(s.isEmpty())throw new IOException("Invalid project id");File d=new File(root,s);String rp=root.getCanonicalPath()+File.separator;if(!d.getCanonicalPath().startsWith(rp))throw new IOException("Unsafe project path");return d;}
  private static JSONObject readJson(File f)throws Exception{return new JSONObject(read(f));}
  private static String read(File f)throws IOException{try(InputStream in=new FileInputStream(f);ByteArrayOutputStream o=new ByteArrayOutputStream()){byte[]b=new byte[32768];int n;while((n=in.read(b))>0)o.write(b,0,n);return o.toString("UTF-8");}}
  private static void writeAtomic(File f,String s)throws IOException{File parent=f.getParentFile();if(parent!=null)parent.mkdirs();File t=new File(f.getPath()+".tmp");try(FileOutputStream o=new FileOutputStream(t)){o.write(s.getBytes(StandardCharsets.UTF_8));o.getFD().sync();}if(f.exists()&&!f.delete())throw new IOException("Cannot replace "+f.getName());if(!t.renameTo(f))throw new IOException("Cannot commit "+f.getName());}
}
