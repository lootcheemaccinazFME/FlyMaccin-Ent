package com.flymaccin.lootcheerom
import android.app.Activity
import android.content.Intent
import android.view.View
import android.webkit.*
import android.widget.*
object FmeControlRooms {
 fun octop(a:Activity):View{val l=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL};val ep=EditText(a).apply{setText("http://10.0.2.2:8088")};val go=Button(a).apply{text="CONNECT CONTROL PLANE"};val w=WebView(a).apply{settings.javaScriptEnabled=true;settings.domStorageEnabled=true;webViewClient=WebViewClient()};go.setOnClickListener{var u=ep.text.toString().trim();if(!u.startsWith("http"))u="http://$u";w.loadUrl(u)};l.addView(ep);l.addView(go);l.addView(w,LinearLayout.LayoutParams(-1,0,1f));return l}
 fun remote(a:Activity):View=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;addView(TextView(a).apply{text="POCKET POTNA REMOTE";textSize=28f});listOf("Browser","Downloads","Files","Settings").forEach{s->addView(Button(a).apply{text=s;setOnClickListener{when(s){"Browser"->a.startActivity(Intent(Intent.ACTION_VIEW,android.net.Uri.parse("https://www.google.com"))); "Downloads"->a.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));"Files"->a.startActivity(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="*/*";addCategory(Intent.CATEGORY_OPENABLE)});else->a.startActivity(Intent(android.provider.Settings.ACTION_SETTINGS))}})}}
}