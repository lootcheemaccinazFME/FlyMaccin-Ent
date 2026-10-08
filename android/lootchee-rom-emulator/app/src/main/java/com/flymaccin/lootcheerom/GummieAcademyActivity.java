package com.flymaccin.lootcheerom;
import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebSettings;
public final class GummieAcademyActivity extends Activity {
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    WebView view = new WebView(this);
    view.setWebViewClient(new WebViewClient());
    WebSettings settings = view.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    settings.setAllowFileAccess(true);
    view.loadUrl("file:///android_asset/gummie/index.html");
    setContentView(view);
  }
  @Override protected void onDestroy() { super.onDestroy(); }
}
