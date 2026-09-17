package com.flymaccin.demonicdaw;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import java.io.*;
import java.util.*;

public class MainActivity extends Activity {
  private WebView webView;
  private static final int MIC_REQUEST = 901;
  private static final String ONLINE_URL = "https://demonicaistudiohut.floot.app";
  private static final String OFFLINE_URL = "file:///android_asset/offline.html";
  private boolean nativeReady = false;
  private File factoryDir;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    try { nativeReady = NativeAudioEngine.nativeStart(); } catch (Throwable t) { nativeReady = false; }
    factoryDir = new File(getFilesDir(), "factory");
    try { copyAssetTree("factory", factoryDir); } catch (Exception ignored) {}
    if (nativeReady) selectFactory("gfunk-bass");

    webView = new WebView(this);
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
    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST);
    webView.loadUrl(isOnline() ? ONLINE_URL : OFFLINE_URL);
  }

  private boolean isOnline() {
    ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE); if(cm==null)return false; Network n=cm.getActiveNetwork(); if(n==null)return false; NetworkCapabilities c=cm.getNetworkCapabilities(n); return c!=null&&c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
  }
  private void copyAssetTree(String assetPath, File out) throws IOException {
    String[] items=getAssets().list(assetPath); if(items==null)return; if(items.length==0){ out.getParentFile().mkdirs(); try(InputStream in=getAssets().open(assetPath); OutputStream os=new FileOutputStream(out)){ byte[]b=new byte[16384];int n;while((n=in.read(b))>0)os.write(b,0,n);} return; }
    out.mkdirs(); for(String item:items) copyAssetTree(assetPath+"/"+item,new File(out,item));
  }
  private int selectFactory(String id){ if(!nativeReady)return 0; try{return SfzBank.load(new File(factoryDir,id+".sfz"));}catch(Exception e){return 0;} }

  public final class NativeBridge {
    @JavascriptInterface public boolean ready(){ return nativeReady; }
    @JavascriptInterface public int selectFactoryInstrument(String id){ return selectFactory(normalizeInstrument(id)); }
    @JavascriptInterface public void noteOn(int key,int velocity){ if(nativeReady)NativeAudioEngine.nativeNoteOn(0,key,velocity); }
    @JavascriptInterface public void noteOff(int key){ if(nativeReady)NativeAudioEngine.nativeNoteOff(0,key); }
    @JavascriptInterface public void setGain(float gain){ if(nativeReady)NativeAudioEngine.nativeSetGain(gain); }
    @JavascriptInterface public String mode(){ return nativeReady?"NATIVE_SAMPLE":"WEB_FALLBACK"; }
  }
  private String normalizeInstrument(String s){
    String x=s==null?"gfunk-bass":s.toLowerCase(Locale.US).replace(" / ","-").replace(' ','-');
    Map<String,String> m=new HashMap<>(); m.put("grand-piano","grand");m.put("warm-organ","organ");m.put("rock--blues-lead","lead-guitar");m.put("rock-blues-lead","lead-guitar");m.put("g-funk-bass","gfunk-bass");m.put("808-sub","808-sub");m.put("brass-stack","brass-stack");m.put("demonic-synth","synth");m.put("percussion-fx","perc-fx"); return m.getOrDefault(x,x);
  }

  @Override public void onBackPressed(){ if(webView!=null&&webView.canGoBack())webView.goBack();else super.onBackPressed(); }
  @Override protected void onDestroy(){ try{if(nativeReady)NativeAudioEngine.nativeStop();}catch(Throwable ignored){} super.onDestroy(); }
}
