package com.flymaccin.demonicaistudio;
import android.content.Context; import java.io.*; import java.nio.charset.StandardCharsets;
final class FmeFunStore {
 static File projectFile(Context c,String name){String safe=name.replaceAll("[^A-Za-z0-9._-]","_");if(!safe.endsWith(".fmefun"))safe+=".fmefun";return new File(c.getFilesDir(),safe);}
 static void save(Context c,FmeFunProject p) throws Exception {File f=projectFile(c,p.name),tmp=new File(f.getPath()+".tmp");try(FileOutputStream out=new FileOutputStream(tmp)){out.write(p.toJson().toString().getBytes(StandardCharsets.UTF_8));out.getFD().sync();}if(f.exists()&&!f.delete())throw new IOException("replace failed");if(!tmp.renameTo(f))throw new IOException("commit failed");}
 static FmeFunProject load(Context c,String name){File f=projectFile(c,name);if(!f.exists())return new FmeFunProject();try(FileInputStream in=new FileInputStream(f)){byte[] b=new byte[(int)f.length()];int n=in.read(b);return FmeFunProject.fromJson(new String(b,0,Math.max(0,n),StandardCharsets.UTF_8));}catch(Exception e){return new FmeFunProject();}}
}