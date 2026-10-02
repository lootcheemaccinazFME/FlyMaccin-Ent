package com.flymaccin.lootcheerom
import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*

object FmeControlRooms {
 fun octop(a:Activity):View{
  val l=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(FmeCockpit.BG);setPadding(12,10,12,10);addView(FmeCockpit.title(a,"OCTOP FME"));addView(FmeCockpit.subtitle(a,"CONTROL PLANE • LOCAL / NETWORK ENDPOINT"))}
  val ep=FmeCockpit.field(a,"Control plane endpoint").apply{setText("http://10.0.2.2:8088")}
  val go=FmeCockpit.button(a,"CONNECT CONTROL PLANE"){}
  val w=WebView(a).apply{settings.javaScriptEnabled=true;settings.domStorageEnabled=true;webViewClient=WebViewClient()}
  go.setOnClickListener{var u=ep.text.toString().trim();if(!u.startsWith("http"))u="http://$u";w.loadUrl(u)}
  l.addView(ep);l.addView(go);l.addView(w,LinearLayout.LayoutParams(-1,0,1f));return l
 }
 fun remote(a:Activity):View{
  val l=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(FmeCockpit.BG);setPadding(12,10,12,10);addView(FmeCockpit.title(a,"POCKET POTNA REMOTE"));addView(FmeCockpit.subtitle(a,"BROWSER • DOWNLOADS • FILES • SETTINGS"))}
  
  fun add(label:String, action:()->Unit){l.addView(FmeCockpit.button(a,label){action()})}
  add("BROWSER"){a.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com")))}
  add("DOWNLOADS"){a.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))}
  add("FILES"){a.startActivity(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="*/*";addCategory(Intent.CATEGORY_OPENABLE)})}
  add("SETTINGS"){a.startActivity(Intent(Settings.ACTION_SETTINGS))}
  return l
 }
}
