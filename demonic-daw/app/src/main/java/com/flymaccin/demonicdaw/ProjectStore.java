package com.flymaccin.demonicdaw;

import android.content.Context;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public final class ProjectStore {
  private final File root;
  public ProjectStore(Context c){ root=new File(c.getFilesDir(),"projects"); root.mkdirs(); }
  private static String safe(String id){ return id==null?"":id.replaceAll("[^A-Za-z0-9._-]",""); }
  public synchronized String create(String name)throws Exception{
    String id=UUID.randomUUID().toString(); File d=dir(id); d.mkdirs();
    for(String n:new String[]{"audio","recordings","renders","autosave","cache"})new File(d,n).mkdirs();
    JSONObject p=new JSONObject().put("schemaVersion",1).put("id",id).put("name",name==null?"Untitled":name).put("revision",0);
    writeAtomic(new File(d,"project.json"),p.toString(2)); return id;
  }
  public synchronized boolean save(String id,String json)throws Exception{
    File d=dir(id); if(!d.exists())return false; JSONObject p=new JSONObject(json); p.put("id",id);
    File journal=new File(d,"autosave/project.pending.json"); writeAtomic(journal,p.toString(2));
    writeAtomic(new File(d,"project.json"),p.toString(2)); journal.delete(); return true;
  }
  public synchronized String load(String id)throws Exception{
    File f=new File(dir(id),"project.json"); if(!f.isFile())return "{}"; return read(f);
  }
  public synchronized String list(){
    org.json.JSONArray a=new org.json.JSONArray(); File[] xs=root.listFiles();
    if(xs!=null)for(File d:xs)if(d.isDirectory()&&new File(d,"project.json").isFile())try{JSONObject p=new JSONObject(read(new File(d,"project.json")));a.put(new JSONObject().put("id",p.optString("id",d.getName())).put("name",p.optString("name","Untitled")).put("revision",p.optLong("revision",0)));}catch(Exception ignored){}
    return a.toString();
  }
  public File assetDir(String id,String kind)throws IOException{
    if(!kind.matches("audio|recordings|renders|cache"))throw new IOException("Invalid asset kind"); File d=new File(dir(id),kind); d.mkdirs(); return d;
  }
  private File dir(String id)throws IOException{String s=safe(id);if(s.isEmpty())throw new IOException("Invalid project id");File d=new File(root,s);String rp=root.getCanonicalPath()+File.separator;if(!d.getCanonicalPath().startsWith(rp))throw new IOException("Unsafe project path");return d;}
  private static String read(File f)throws IOException{try(InputStream in=new FileInputStream(f);ByteArrayOutputStream o=new ByteArrayOutputStream()){byte[]b=new byte[32768];int n;while((n=in.read(b))>0)o.write(b,0,n);return o.toString("UTF-8");}}
  private static void writeAtomic(File f,String s)throws IOException{File parent=f.getParentFile();if(parent!=null)parent.mkdirs();File t=new File(f.getPath()+".tmp");try(FileOutputStream o=new FileOutputStream(t)){o.write(s.getBytes(StandardCharsets.UTF_8));o.getFD().sync();}if(f.exists()&&!f.delete())throw new IOException("Cannot replace "+f.getName());if(!t.renameTo(f))throw new IOException("Cannot commit "+f.getName());}
}
