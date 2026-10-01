package com.flymaccin.pocketpotnaremote;

import android.app.*;import android.os.*;import android.content.*;import android.graphics.Color;import android.net.Uri;import android.view.*;import android.widget.*;import java.util.*;

public class MainActivity extends Activity {
 static final String RELAY="https://pocket-potna-remote.floot.app/_api/"; LinearLayout body; TextView status;
 public void onCreate(Bundle b){super.onCreate(b); build();}
 TextView t(String s,int z){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(z);v.setPadding(24,18,24,18);return v;}
 Button btn(String s,View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setOnClickListener(l);return b;}
 void build(){ScrollView sc=new ScrollView(this);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(24,32,24,32);body.setBackgroundColor(Color.rgb(5,7,10));sc.addView(body);body.addView(t("POCKET POTNA REMOTE",26));body.addView(t("FME remote command hub • v2.0.0",14));status=t("Relay: "+RELAY+"\nPairing: persistent on this device\nChatGPT authorization: separate one-time connector flow",14);body.addView(status);String[] hubs={"PS5 Remote Play","Browser","Downloads","Apps","Files","Cloud","Settings"};for(String h:hubs)body.addView(btn(h,v->open(h)));body.addView(t("Remote commands use the production relay. Pausing does not erase the local pairing identity.",13));setContentView(sc);}
 void open(String h){if(h.equals("PS5 Remote Play")){Intent i=getPackageManager().getLaunchIntentForPackage("com.playstation.remoteplay");if(i!=null)startActivity(i);else toast("PS Remote Play is not installed.");return;}if(h.equals("Browser")){startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://pocket-potna-remote.floot.app")));return;}toast(h+" hub ready");}
 void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
