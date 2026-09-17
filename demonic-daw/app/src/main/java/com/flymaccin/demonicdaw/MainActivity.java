package com.flymaccin.demonicdaw;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
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

public class MainActivity extends Activity {
  private WebView webView;
  private static final int MIC_REQUEST = 901;
  private static final String ONLINE_URL = "https://demonicaistudiohut.floot.app";
  private static final String OFFLINE_URL = "file:///android_asset/offline.html";

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    webView = new WebView(this);
    setContentView(webView);
    WebSettings s = webView.getSettings();
    s.setJavaScriptEnabled(true);
    s.setDomStorageEnabled(true);
    s.setMediaPlaybackRequiresUserGesture(false);
    s.setDatabaseEnabled(true);
    s.setAllowFileAccess(true);
    webView.setWebChromeClient(new WebChromeClient() {
      @Override public void onPermissionRequest(final PermissionRequest request) {
        runOnUiThread(() -> {
          if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) request.grant(request.getResources());
          else requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST);
        });
      }
    });
    webView.setWebViewClient(new WebViewClient() {
      @Override public void onReceivedError(WebView view, WebResourceRequest request, android.webkit.WebResourceError error) {
        super.onReceivedError(view, request, error);
        if (request.isForMainFrame() && !view.getUrl().startsWith("file:///android_asset/")) view.loadUrl(OFFLINE_URL);
      }
    });
    if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, MIC_REQUEST);
    webView.loadUrl(isOnline() ? ONLINE_URL : OFFLINE_URL);
  }

  private boolean isOnline() {
    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    if (cm == null) return false;
    Network n = cm.getActiveNetwork();
    if (n == null) return false;
    NetworkCapabilities c = cm.getNetworkCapabilities(n);
    return c != null && c.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
  }

  @Override public void onBackPressed() {
    if (webView != null && webView.canGoBack()) webView.goBack(); else super.onBackPressed();
  }
}
