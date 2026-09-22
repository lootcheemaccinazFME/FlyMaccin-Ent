package com.flymaccin.demonicaistudio;

import android.content.Context;
import org.json.JSONObject;
import java.io.*;
import java.nio.charset.StandardCharsets;

final class TransactionalProjectStore {
    private final File root;
    TransactionalProjectStore(Context context){root=new File(context.getFilesDir(),"demonic-projects"); if(!root.exists()&&!root.mkdirs()) throw new IllegalStateException("project root");}

    synchronized void save(CanonicalProjectState state) throws IOException {
        state.validate();
        File dir=dir(state.projectId); if(!dir.exists()&&!dir.mkdirs()) throw new IOException("mkdir");
        File live=new File(dir,"project.json"), next=new File(dir,"project.next"), journal=new File(dir,"project.journal");
        writeSync(journal,new JSONObject().put("op","replace").put("revision",state.revision).put("target","project.json").toString());
        writeSync(next,state.toJson().toString());
        atomicReplace(next,live);
        if(journal.exists()&&!journal.delete()) throw new IOException("journal cleanup");
    }

    synchronized CanonicalProjectState load(String projectId) throws IOException {
        File dir=dir(projectId); recover(dir);
        File live=new File(dir,"project.json");
        if(!live.exists()) return new CanonicalProjectState(projectId,"Demonic Session");
        try{return CanonicalProjectState.fromJson(new JSONObject(read(live)));}
        catch(Exception e){throw new IOException("invalid canonical project",e);}
    }

    synchronized void recover(File dir) throws IOException {
        File journal=new File(dir,"project.journal"), next=new File(dir,"project.next"), live=new File(dir,"project.json");
        if(!journal.exists()){if(next.exists()&&!next.delete()) throw new IOException("orphan next");return;}
        if(next.exists()) {
            try { CanonicalProjectState.fromJson(new JSONObject(read(next))); atomicReplace(next,live); }
            catch(Exception invalid){ if(!next.delete()) throw new IOException("bad next cleanup",invalid); }
        }
        if(!journal.delete()) throw new IOException("journal cleanup");
    }

    private File dir(String id){return new File(root,id.replaceAll("[^A-Za-z0-9._-]","_"));}
    private static void writeSync(File f,String value)throws IOException{try(FileOutputStream out=new FileOutputStream(f)){out.write(value.getBytes(StandardCharsets.UTF_8));out.flush();out.getFD().sync();}}
    private static String read(File f)throws IOException{ByteArrayOutputStream b=new ByteArrayOutputStream();try(FileInputStream in=new FileInputStream(f)){byte[] x=new byte[8192];for(int n;(n=in.read(x))>0;)b.write(x,0,n);}return b.toString("UTF-8");}
    private static void atomicReplace(File from,File to)throws IOException{if(to.exists()&&!to.delete())throw new IOException("delete old");if(!from.renameTo(to))throw new IOException("atomic rename");}
}
