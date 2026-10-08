package com.flymaccin.demonicaistudio;

import android.content.Context;
import android.net.Uri;
import java.util.*;

public final class DemonicToolRegistry {
    public static final String DOWNLOAD="download", IMPORT_SAMPLE="import.sample", IMPORT_INSTRUMENT="import.instrument",
            MEDIA="media", SFZ="sfz", SF2="sf2", SHORTCUT="shortcut", STREAM="stream", LIBRARY="library", PRODUCTION="production";
    private final LinkedHashMap<String,DemonicTool> tools=new LinkedHashMap<>();
    public DemonicToolRegistry(Context context){ registerBuiltIns(); }
    public synchronized void register(DemonicTool tool){
        if(tool==null||tool.id()==null||!tool.id().matches("[a-z0-9._-]{3,80}")) throw new IllegalArgumentException("Invalid tool id");
        tools.put(tool.id(),tool);
    }
    public synchronized List<DemonicTool> all(){return Collections.unmodifiableList(new ArrayList<>(tools.values()));}
    public synchronized List<DemonicTool> handlers(Uri uri,String mime){
        ArrayList<DemonicTool> out=new ArrayList<>(); for(DemonicTool t:tools.values())if(t.accepts(uri,mime))out.add(t); return out;
    }
    private void registerBuiltIns(){
        register(new DescriptorTool("fme.downloads","Download Handler",DOWNLOAD));
        register(new DescriptorTool("fme.samples","Sample Importer",IMPORT_SAMPLE));
        register(new DescriptorTool("fme.instruments","Instrument Importer",IMPORT_INSTRUMENT));
        register(new DescriptorTool("fme.media","Media Tools",MEDIA));
        register(new DescriptorTool("fme.sfz","SFZ Processor",SFZ));
        register(new DescriptorTool("fme.sf2","SF2 Processor",SF2));
        register(new DescriptorTool("fme.shortcuts","Site Shortcuts",SHORTCUT));
        register(new DescriptorTool("fme.streams","Stream Handler",STREAM));
        register(new DescriptorTool("fme.library","Library Utilities",LIBRARY));
        register(new DescriptorTool("fme.production","Production Tools",PRODUCTION));
    }
    static final class DescriptorTool implements DemonicTool {
        private final String id,name,cap; DescriptorTool(String i,String n,String c){id=i;name=n;cap=c;}
        public String id(){return id;} public String name(){return name;} public String version(){return "1.0";}
        public Set<String> capabilities(){return Collections.singleton(cap);}
        public boolean accepts(Uri u,String mime){
            String p=u==null?"":String.valueOf(u.getPath()).toLowerCase(Locale.US);
            if(cap.equals(SFZ))return p.endsWith(".sfz"); if(cap.equals(SF2))return p.endsWith(".sf2");
            if(cap.equals(IMPORT_SAMPLE))return mime!=null&&mime.startsWith("audio/");
            return true;
        }
        public ToolResult execute(Context c,ToolRequest r){return ToolResult.ok(name+" ready");}
    }
}
