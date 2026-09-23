package com.flymaccin.bookwriter;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class AgentActivity extends Activity {
  private LinearLayout body;
  private int fg=0xfff3f1ff, muted=0xff9ba4bb, bg=0xff090b12, accent=0xff8b5cf6;
  @Override public void onCreate(Bundle b){super.onCreate(b); dashboard();}
  private TextView text(String s,int sp,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(c);v.setPadding(4,10,4,10);return v;}
  private Button button(String s, View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setOnClickListener(l);return b;}
  private void dashboard(){
    LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(24,28,24,24);root.setBackgroundColor(bg);
    root.addView(text("FME AGENT",34,fg));root.addView(text("UMD ACTIVE · ANDROID AGENT APK · 0.1",12,accent));
    HorizontalScrollView hsv=new HorizontalScrollView(this);LinearLayout nav=new LinearLayout(this);
    nav.addView(button("AGENT",v->agent()));nav.addView(button("PS5",v->ps5()));nav.addView(button("BROWSER",v->browser()));
    nav.addView(button("DOWNLOADS",v->downloads()));nav.addView(button("APPS",v->apps()));nav.addView(button("FILES",v->files()));
    nav.addView(button("SETTINGS",v->startActivity(new Intent(Settings.ACTION_SETTINGS))));
    hsv.addView(nav);root.addView(hsv);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);root.addView(body,new LinearLayout.LayoutParams(-1,0,1));setContentView(root);agent();
  }
  private void head(String a,String b){body.removeAllViews();body.addView(text(a,25,fg));body.addView(text(b,14,muted));}
  private void agent(){head("Agent Command Center","GitHub → code → test → APK → failure review → repair → release → phone QA");
    String[] a={"UMD/project specification reader","GitHub engineering workflow","Kotlin/Android build pipeline","Test + lint gates","Actions APK artifacts","Failure-log repair loop","Release/version ledger","Physical-phone QA handoff"};
    for(String x:a)body.addView(text("✓  "+x,16,fg));
    body.addView(text("Network agent actions use an authenticated service/connector. Secrets are never embedded in the APK.",13,muted));
  }
  private void ps5(){Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("https://remoteplay.dl.playstation.net/remoteplay/"));startActivity(i);}
  private void browser(){head("Browser","Open a URL in the device browser. Embedded tab/browser engine is the next native module.");
    EditText e=new EditText(this);e.setText("https://");e.setTextColor(fg);body.addView(e);body.addView(button("OPEN",v->{String u=e.getText().toString().trim();if(!u.startsWith("http"))u="https://"+u;startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(u)));}));}
  private void downloads(){head("Downloads","Android Download Manager. Direct downloadable files only. No DRM/access-control bypass.");body.addView(button("OPEN DOWNLOADS",v->startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))));}
  private void apps(){head("App Launcher","Launchable apps visible to Android.");Intent q=new Intent(Intent.ACTION_MAIN);q.addCategory(Intent.CATEGORY_LAUNCHER);
    ScrollView sv=new ScrollView(this);LinearLayout list=new LinearLayout(this);list.setOrientation(LinearLayout.VERTICAL);
    getPackageManager().queryIntentActivities(q,0).stream().sorted(Comparator.comparing(x->x.loadLabel(getPackageManager()).toString().toLowerCase())).forEach(x->{String p=x.activityInfo.packageName;Button b=button(x.loadLabel(getPackageManager()).toString(),v->{Intent li=getPackageManager().getLaunchIntentForPackage(p);if(li!=null)startActivity(li);});b.setGravity(Gravity.START);list.addView(b);});sv.addView(list);body.addView(sv,new LinearLayout.LayoutParams(-1,0,1));}
  private void files(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivity(i);}
}
