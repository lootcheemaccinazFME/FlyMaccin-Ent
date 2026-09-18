package com.flymaccin.demonicdaw;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.graphics.Color;
import androidx.documentfile.provider.DocumentFile;
import org.json.JSONObject;
import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MainActivity extends Activity {
  private WebView webView;
  private static final int MIC_REQUEST = 901;
  private static final int SF2_REQUEST = 902;
  private static final int SFZ_TREE_REQUEST = 903;
  private static final String ONLINE_URL = "https://demonicaistudiohut.floot.app";
  private static final String OFFLINE_URL = "file:///android_asset/offline.html";
  private boolean nativeReady = false;
  private File factoryDir;
  private File importDir;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    factoryDir = new File(getFilesDir(), "factory");
    importDir = new File(getFilesDir(), "instrument-imports");
    importDir.mkdirs();

    // Render the DAW first. Native audio and pack preparation must never block first paint.
    webView = new WebView(this);
    webView.setBackgroundColor(Color.rgb(5,5,7));
    setContentView(webView);
    WebSettings s = webView.getSettings();
    s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setMediaPlaybackRequiresUserGesture(false); s.setDatabaseEnabled(true); s.setAllowFileAccess(true);
    webView.addJavascriptInterface(new NativeBridge(), "DemonicNative");
    webView.setWebChromeClient(new WebChromeClient() {
      @Override public void onPermissionRequest(final PermissionRequest request) {
        runOnUiThread(() -> { if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) request.grant(request.getResources()); else requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST); });
      }
    });
    webView.setWebViewClient(new WebViewClient() {
      @Override public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
        super.onReceivedError(view, request, error); if (request.isForMainFrame() && (view.getUrl()==null || !view.getUrl().startsWith("file:///android_asset/"))) view.loadUrl(OFFLINE_URL);
      }
    });
    // Demonic DAW 1.2 is local-first so the bundled FME UI is authoritative online or offline.
    webView.loadUrl(OFFLINE_URL);

    new Thread(() -> {
      try { nativeReady = NativeAudioEngine.nativeStart(); } catch (Throwable t) { nativeReady = false; }
      try { copyAssetTree("factory", factoryDir); } catch (Exception ignored) {}
      try { installBundledFmeCore(); } catch (Exception ignored) {}
      if (nativeReady) restoreLastBank();
      runOnUiThread(() -> {
        if (webView != null) webView.evaluateJavascript("window.dispatchEvent(new Event('demonic-native-ready'));", null);
      });
    }).start();
  }

  private boolean isOnline() {
    ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); if(cm==null)return false; Network n=cm.getActiveNetwork(); if(n==null)return false; NetworkCapabilities c=cm.getNetworkCapabilities(n); return c!=null&&c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
  }
  private void copyAssetTree(String assetPath, File out) throws IOException {
    String[] items=getAssets().list(assetPath); if(items==null)return; if(items.length==0){ out.getParentFile().mkdirs(); try(InputStream in=getAssets().open(assetPath); OutputStream os=new FileOutputStream(out)){ copy(in,os); } return; }
    out.mkdirs(); for(String item:items) copyAssetTree(assetPath+"/"+item,new File(out,item));
  }
  private static void copy(InputStream in, OutputStream out) throws IOException { byte[]b=new byte[32768];int n;while((n=in.read(b))>0)out.write(b,0,n); }
  private void installBundledFmeCore() throws IOException {
    File root=new File(getFilesDir(),"fme-packs/core-v1");
    File done=new File(root,".installed");
    if(done.exists())return;
    deleteTree(root); root.mkdirs();
    try(InputStream raw=getAssets().open("FME_Core_Pack_v1.zip"); ZipInputStream zin=new ZipInputStream(new BufferedInputStream(raw))){
      ZipEntry e; byte[]buf=new byte[32768];
      String rootPath=root.getCanonicalPath()+File.separator;
      while((e=zin.getNextEntry())!=null){
        File dst=new File(root,e.getName());
        String cp=dst.getCanonicalPath();
        if(!cp.startsWith(rootPath))throw new IOException("Unsafe core-pack path");
        if(e.isDirectory()){dst.mkdirs();continue;}
        File parent=dst.getParentFile(); if(parent!=null)parent.mkdirs();
        try(OutputStream out=new BufferedOutputStream(new FileOutputStream(dst))){int n;while((n=zin.read(buf))>0)out.write(buf,0,n);}
      }
    }
    try(FileOutputStream o=new FileOutputStream(done)){o.write("FME Core Pack v1".getBytes("UTF-8"));}
    getSharedPreferences("demonic_packs",MODE_PRIVATE).edit().putBoolean("fme_core_v1",true).putString("fme_core_path",root.getAbsolutePath()).apply();
  }
  private int selectFactory(String id){ if(!nativeReady)return 0; try{return SfzBank.load(new File(factoryDir,id+".sfz"));}catch(Exception e){return 0;} }

  private boolean restoreLastBank(){
    String kind=getPreferences(MODE_PRIVATE).getString("bank_kind",""); String path=getPreferences(MODE_PRIVATE).getString("bank_path","");
    if(path.isEmpty())return false; File f=new File(path); if(!f.exists())return false;
    try { if("sf2".equals(kind)) return NativeAudioEngine.nativeLoadSoundFont(path)>=0; if("sfz".equals(kind)) return SfzBank.load(f)>0; } catch(Exception ignored){} return false;
  }
  private void rememberBank(String kind, File path){ getPreferences(MODE_PRIVATE).edit().putString("bank_kind",kind).putString("bank_path",path.getAbsolutePath()).apply(); }

  private void launchSf2Picker(){
    Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.addCategory(Intent.CATEGORY_OPENABLE); i.setType("*/*");
    i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"application/octet-stream","audio/*","application/x-soundfont"}); startActivityForResult(i,SF2_REQUEST);
  }
  private void launchSfzFolderPicker(){ Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE); i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); startActivityForResult(i,SFZ_TREE_REQUEST); }

  @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
    super.onActivityResult(requestCode,resultCode,data); if(resultCode!=RESULT_OK||data==null||data.getData()==null)return; Uri uri=data.getData();
    if(requestCode==SF2_REQUEST){ importSf2(uri); }
    else if(requestCode==SFZ_TREE_REQUEST){ importSfzTree(uri,data.getFlags()); }
  }
  private void importSf2(Uri uri){
    try { File dst=new File(importDir,"custom.sf2"); try(InputStream in=getContentResolver().openInputStream(uri);OutputStream out=new FileOutputStream(dst)){if(in==null)throw new IOException("No input stream");copy(in,out);} int id=NativeAudioEngine.nativeLoadSoundFont(dst.getAbsolutePath()); boolean ok=id>=0; if(ok)rememberBank("sf2",dst); notifyImport("sf2",ok,dst.getName(),ok?"SoundFont loaded":"FluidSynth rejected the SoundFont"); }
    catch(Exception e){notifyImport("sf2",false,"",e.getMessage());}
  }
  private void importSfzTree(Uri uri,int flags){
    try {
      getContentResolver().takePersistableUriPermission(uri,flags&(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION));
      DocumentFile root=DocumentFile.fromTreeUri(this,uri); if(root==null||!root.isDirectory())throw new IOException("Folder unavailable");
      File dstRoot=new File(importDir,"sfz-bank"); deleteTree(dstRoot); dstRoot.mkdirs(); List<File> sfzFiles=new ArrayList<>(); copyDocumentTree(root,dstRoot,sfzFiles);
      if(sfzFiles.isEmpty())throw new IOException("No .sfz file found in selected folder"); File sfz=sfzFiles.get(0); int regions=SfzBank.load(sfz); boolean ok=regions>0; if(ok)rememberBank("sfz",sfz); notifyImport("sfz",ok,sfz.getName(),ok?(regions+" regions loaded"):"No playable regions found");
    } catch(Exception e){notifyImport("sfz",false,"",e.getMessage());}
  }
  private void copyDocumentTree(DocumentFile src,File dst,List<File> sfzFiles)throws IOException{
    if(src.isDirectory()){dst.mkdirs();for(DocumentFile child:src.listFiles()){String n=safeName(child.getName());copyDocumentTree(child,new File(dst,n),sfzFiles);}return;}
    dst.getParentFile().mkdirs();try(InputStream in=getContentResolver().openInputStream(src.getUri());OutputStream out=new FileOutputStream(dst)){if(in==null)throw new IOException("Cannot read "+src.getName());copy(in,out);}if(dst.getName().toLowerCase(Locale.US).endsWith(".sfz"))sfzFiles.add(dst);
  }
  private void collectWavs(File root,File f,org.json.JSONArray out){
    if(f==null||!f.exists())return;
    if(f.isDirectory()){File[]xs=f.listFiles();if(xs!=null)for(File x:xs)collectWavs(root,x,out);return;}
    if(!f.getName().toLowerCase(Locale.US).endsWith(".wav"))return;
    try{String rp=root.getCanonicalPath()+File.separator,fp=f.getCanonicalPath();if(fp.startsWith(rp))out.put(fp.substring(rp.length()).replace(File.separatorChar,'/'));}catch(Exception ignored){}
  }
  private static String safeName(String n){ if(n==null||n.trim().isEmpty())return "unnamed";return n.replaceAll("[^A-Za-z0-9._ -]","_"); }
  private static void deleteTree(File f){if(f==null||!f.exists())return;if(f.isDirectory()){File[]xs=f.listFiles();if(xs!=null)for(File x:xs)deleteTree(x);}f.delete();}
  private void notifyImport(String kind,boolean ok,String name,String message){ if(webView==null)return; final String js="window.dispatchEvent(new CustomEvent('demonic-native-import',{detail:{kind:"+JSONObject.quote(kind)+",ok:"+ok+",name:"+JSONObject.quote(name==null?"":name)+",message:"+JSONObject.quote(message==null?"":message)+"}}));"; runOnUiThread(()->webView.evaluateJavascript(js,null)); }

  public final class NativeBridge {
    @JavascriptInterface public boolean ready(){ return nativeReady; }
    @JavascriptInterface public int selectFactoryInstrument(String id){ return selectFactory(normalizeInstrument(id)); }
    @JavascriptInterface public void noteOn(int key,int velocity){ if(nativeReady)NativeAudioEngine.nativeNoteOn(0,key,velocity); }
    @JavascriptInterface public void noteOff(int key){ if(nativeReady)NativeAudioEngine.nativeNoteOff(0,key); }
    @JavascriptInterface public void setGain(float gain){ if(nativeReady)NativeAudioEngine.nativeSetGain(gain); }
    @JavascriptInterface public String mode(){ return nativeReady?"NATIVE_SAMPLE":"STARTING"; }
    @JavascriptInterface public String uiProbe(){ return "DEMONIC_DAW_LOCAL_FME_UI_V121"; }
    @JavascriptInterface public boolean hasFmeCore(){ return getSharedPreferences("demonic_packs",MODE_PRIVATE).getBoolean("fme_core_v1",false); }
    @JavascriptInterface public String fmeCorePath(){ return getSharedPreferences("demonic_packs",MODE_PRIVATE).getString("fme_core_path",""); }
    @JavascriptInterface public String listFmeCoreSamples(){
      File root=new File(getSharedPreferences("demonic_packs",MODE_PRIVATE).getString("fme_core_path",""));
      org.json.JSONArray a=new org.json.JSONArray(); collectWavs(root,root,a); return a.toString();
    }
    @JavascriptInterface public boolean loadFmeCoreSample(String relative){
      try{
        File root=new File(getSharedPreferences("demonic_packs",MODE_PRIVATE).getString("fme_core_path",""));
        File wav=new File(root,relative); String rp=root.getCanonicalPath()+File.separator, wp=wav.getCanonicalPath();
        if(!wp.startsWith(rp)||!wav.isFile()||!wav.getName().toLowerCase(Locale.US).endsWith(".wav"))return false;
        File sfz=new File(getCacheDir(),"fme-core-preview.sfz");
        String txt="<region> sample="+wav.getAbsolutePath().replace("\\","/")+" key=60\n";
        try(FileOutputStream o=new FileOutputStream(sfz)){o.write(txt.getBytes("UTF-8"));}
        return SfzBank.load(sfz)>0;
      }catch(Exception e){return false;}
    }
    @JavascriptInterface public void importSoundFont(){ runOnUiThread(()->launchSf2Picker()); }
    @JavascriptInterface public void importSfzFolder(){ runOnUiThread(()->launchSfzFolderPicker()); }
  }
  private String normalizeInstrument(String s){
    String x=s==null?"gfunk-bass":s.toLowerCase(Locale.US).replace(" / ","-").replace(' ','-');
    Map<String,String> m=new HashMap<>(); m.put("grand-piano","grand");m.put("warm-organ","organ");m.put("rock--blues-lead","lead-guitar");m.put("rock-blues-lead","lead-guitar");m.put("g-funk-bass","gfunk-bass");m.put("808-sub","808-sub");m.put("brass-stack","brass-stack");m.put("demonic-synth","synth");m.put("percussion-fx","perc-fx"); return m.getOrDefault(x,x);
  }

  @Override public void onBackPressed(){ if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed(); }
  @Override protected void onDestroy(){ try{if(nativeReady)NativeAudioEngine.nativeStop();}catch(Throwable ignored){} super.onDestroy(); }
}
