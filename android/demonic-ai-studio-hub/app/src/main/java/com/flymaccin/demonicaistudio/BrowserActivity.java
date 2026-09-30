package com.flymaccin.demonicaistudio;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import android.webkit.*;
import android.widget.*;
import java.util.*;

public final class BrowserActivity extends Activity {
    private static final int BG=Color.rgb(8,9,14), PANEL=Color.rgb(25,27,37), PURPLE=Color.rgb(164,92,255), CYAN=Color.rgb(55,226,255), WHITE=Color.rgb(239,241,248);
    private final ArrayList<Tab> tabs=new ArrayList<>();
    private FrameLayout stage; private EditText address; private TextView tabLabel,status; private BrowserDb db; private int active=-1;
    private boolean privateMode=false, blockThirdParty=true, allowJs=true;
    private DemonicToolRegistry toolRegistry;
    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_PICKER=8801;

    static final class Tab { WebView view; String title="New Tab"; String url="about:blank"; Tab(WebView v){view=v;} }

    @Override public void onCreate(Bundle state){
        super.onCreate(state); requestWindowFeature(Window.FEATURE_NO_TITLE); db=new BrowserDb(this);
        toolRegistry=new DemonicToolRegistry(this); setContentView(shell()); newTab("https://www.google.com");
    }

    private View shell(){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        LinearLayout bar=row();
        bar.addView(btn("←",v->back())); bar.addView(btn("→",v->forward())); bar.addView(btn("↻",v->current().reload()));
        address=new EditText(this); address.setSingleLine(true); address.setTextColor(WHITE); address.setHintTextColor(Color.GRAY); address.setBackgroundColor(PANEL);
        address.setOnEditorActionListener((v,a,e)->{navigate(address.getText().toString()); return true;});
        bar.addView(address,new LinearLayout.LayoutParams(0,dp(48),1));
        bar.addView(btn("GO",v->navigate(address.getText().toString())));
        bar.addView(btn("＋",v->newTab("https://www.google.com")));
        tabLabel=label("0 TABS",CYAN); bar.addView(tabLabel);
        root.addView(bar,new LinearLayout.LayoutParams(-1,dp(54)));
        stage=new FrameLayout(this); root.addView(stage,new LinearLayout.LayoutParams(-1,0,1));
        HorizontalScrollView scroll=new HorizontalScrollView(this); LinearLayout tools=row();
        tools.addView(btn("TABS",v->showTabs())); tools.addView(btn("★ BOOKMARK",v->bookmark()));
        tools.addView(btn("HISTORY",v->showHistory())); tools.addView(btn("BOOKMARKS",v->showBookmarks()));
        tools.addView(btn("DOWNLOADS",v->openDownloads())); tools.addView(btn("MINI TV",v->enterPip()));
        tools.addView(btn("PRIVACY",v->privacyDialog())); tools.addView(btn("TOOLS",v->toolsDialog())); tools.addView(btn("PLUGINS",v->pluginsDialog()));
        tools.addView(btn("CLOSE TAB",v->closeTab()));
        status=label("BROWSER READY",PURPLE); tools.addView(status);
        scroll.addView(tools); root.addView(scroll,new LinearLayout.LayoutParams(-1,dp(58)));
        return root;
    }

    private void newTab(String url){
        WebView web=new WebView(this); configure(web); Tab tab=new Tab(web); tabs.add(tab); active=tabs.size()-1; showActive(); web.loadUrl(url);
    }
    private void configure(WebView web){
        WebSettings s=web.getSettings(); s.setJavaScriptEnabled(allowJs); s.setDomStorageEnabled(!privateMode); s.setMediaPlaybackRequiresUserGesture(false);
        s.setBuiltInZoomControls(true); s.setDisplayZoomControls(false); CookieManager.getInstance().setAcceptThirdPartyCookies(web,!blockThirdParty);
        web.setDownloadListener((url,ua,disposition,mime,length)->download(url,ua,disposition,mime));
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageFinished(WebView v,String url){ Tab t=tabFor(v); if(t==null)return; t.url=url; t.title=v.getTitle()==null?url:v.getTitle(); if(v==current()){address.setText(url);status.setText(t.title);} if(!privateMode)db.history(t.title,url); }
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){ return false; }
        });
        web.setWebChromeClient(new WebChromeClient(){
            @Override public void onPermissionRequest(PermissionRequest request){ runOnUiThread(()->permissionPrompt(request)); }
            @Override public boolean onShowFileChooser(WebView w,ValueCallback<Uri[]> cb,FileChooserParams p){
                if(fileCallback!=null)fileCallback.onReceiveValue(null); fileCallback=cb;
                try{startActivityForResult(p.createIntent(),FILE_PICKER);return true;}catch(Exception e){fileCallback=null;return false;}
            }
            @Override public void onProgressChanged(WebView v,int p){ if(v==current())status.setText("LOADING "+p+"%"); }
        });
    }
    private void permissionPrompt(PermissionRequest request){
        String host=Uri.parse(request.getOrigin().toString()).getHost();
        new AlertDialog.Builder(this).setTitle("Site permission").setMessage((host==null?"This site":host)+" requests: "+Arrays.toString(request.getResources()))
            .setNegativeButton("DENY",(d,w)->request.deny()).setPositiveButton("ALLOW",(d,w)->request.grant(request.getResources())).show();
    }
    private void download(String url,String ua,String disposition,String mime){
        try{
            DownloadManager.Request r=new DownloadManager.Request(Uri.parse(url)); r.setMimeType(mime); r.addRequestHeader("User-Agent",ua);
            r.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            r.setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS,URLUtil.guessFileName(url,disposition,mime));
            ((DownloadManager)getSystemService(DOWNLOAD_SERVICE)).enqueue(r); status.setText("DOWNLOAD QUEUED");
        }catch(Exception e){status.setText("DOWNLOAD BLOCKED");}
    }
    private void navigate(String raw){
        String q=raw.trim(); if(q.isEmpty())return;
        String url=(q.contains(" ")||(!q.contains(".")&&!q.startsWith("http")))?"https://www.google.com/search?q="+Uri.encode(q):(q.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*")?q:"https://"+q);
        current().loadUrl(url);
    }
    private void bookmark(){ Tab t=tabs.get(active); db.bookmark(t.title,t.url); status.setText("BOOKMARK SAVED"); }
    private void showHistory(){ showRows("History",db.rows("SELECT title,url FROM history ORDER BY visited_at DESC LIMIT 100")); }
    private void showBookmarks(){ showRows("Bookmarks",db.rows("SELECT title,url FROM bookmarks ORDER BY created_at DESC")); }
    private void showRows(String title,List<String[]> rows){
        String[] names=new String[rows.size()]; for(int i=0;i<rows.size();i++)names[i]=rows.get(i)[0]+"\\n"+rows.get(i)[1];
        new AlertDialog.Builder(this).setTitle(title).setItems(names,(d,w)->navigate(rows.get(w)[1])).setNegativeButton("CLOSE",null).show();
    }
    private void showTabs(){
        String[] names=new String[tabs.size()]; for(int i=0;i<tabs.size();i++)names[i]=(i==active?"● ":"○ ")+tabs.get(i).title;
        new AlertDialog.Builder(this).setTitle("Tabs").setItems(names,(d,w)->{active=w;showActive();}).setPositiveButton("NEW TAB",(d,w)->newTab("https://www.google.com")).show();
    }
    private void privacyDialog(){
        String[] items={"Private session: "+onOff(privateMode),"Block third-party cookies: "+onOff(blockThirdParty),"JavaScript: "+onOff(allowJs),"Clear browser data"};
        new AlertDialog.Builder(this).setTitle("Privacy controls").setItems(items,(d,w)->{
            if(w==0){privateMode=!privateMode; if(privateMode){CookieManager.getInstance().removeAllCookies(null);WebStorage.getInstance().deleteAllData();}}
            if(w==1)blockThirdParty=!blockThirdParty;
            if(w==2)allowJs=!allowJs;
            if(w==3){db.clearHistory();CookieManager.getInstance().removeAllCookies(null);WebStorage.getInstance().deleteAllData();}
            for(Tab t:tabs){t.view.getSettings().setJavaScriptEnabled(allowJs);t.view.getSettings().setDomStorageEnabled(!privateMode);CookieManager.getInstance().setAcceptThirdPartyCookies(t.view,!blockThirdParty);}
        }).show();
    }
    private void pluginsDialog(){
        List<DemonicTool> all=toolRegistry.all(); String[] names=new String[all.size()];
        for(int i=0;i<all.size();i++){DemonicTool t=all.get(i);names[i]=t.name()+"  v"+t.version()+"  "+t.capabilities();}
        new AlertDialog.Builder(this).setTitle("Demonic Plugins / Tools").setItems(names,(d,w)->status.setText(all.get(w).name()+" READY")).setNegativeButton("CLOSE",null).show();
    }
    private void toolsDialog(){
        String[] items={"Page info","Open external app","Desktop mode","Find in page"};
        new AlertDialog.Builder(this).setTitle("Demonic Browser Tools").setItems(items,(d,w)->{
            if(w==0)new AlertDialog.Builder(this).setTitle("Page info").setMessage(current().getUrl()).setPositiveButton("OK",null).show();
            if(w==1){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(current().getUrl())));}catch(Exception ignored){}}
            if(w==2){WebSettings s=current().getSettings();String ua=s.getUserAgentString();s.setUserAgentString(ua.contains("Android")?ua.replace("Android","X11; Linux x86_64"):null);current().reload();}
            if(w==3)promptFind();
        }).show();
    }
    private void promptFind(){ final EditText q=new EditText(this); new AlertDialog.Builder(this).setTitle("Find in page").setView(q).setPositiveButton("FIND",(d,w)->current().findAllAsync(q.getText().toString())).setNegativeButton("CANCEL",null).show(); }
    private void openDownloads(){ try{startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));}catch(Exception e){status.setText("DOWNLOAD UI UNAVAILABLE");} }
    private void enterPip(){ if(Build.VERSION.SDK_INT>=26){try{enterPictureInPictureMode(new PictureInPictureParams.Builder().build());}catch(Exception e){status.setText("PIP UNAVAILABLE");}} }
    private void closeTab(){ if(tabs.size()==1){current().loadUrl("about:blank");return;} Tab t=tabs.remove(active);t.view.destroy();active=Math.max(0,active-1);showActive(); }
    private void showActive(){ stage.removeAllViews();stage.addView(current(),new FrameLayout.LayoutParams(-1,-1));address.setText(current().getUrl());tabLabel.setText((active+1)+"/"+tabs.size()+" TABS"); }
    private void back(){if(current().canGoBack())current().goBack();} private void forward(){if(current().canGoForward())current().goForward();}
    private WebView current(){return tabs.get(active).view;} private Tab tabFor(WebView w){for(Tab t:tabs)if(t.view==w)return t;return null;}
    private String onOff(boolean v){return v?"ON":"OFF";}

    @Override public void onBackPressed(){ if(active>=0&&current().canGoBack())current().goBack(); else super.onBackPressed(); }
    @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==FILE_PICKER&&fileCallback!=null){fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));fileCallback=null;}}
    @Override protected void onDestroy(){for(Tab t:tabs)t.view.destroy();db.close();super.onDestroy();}

    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private Button btn(String s,View.OnClickListener c){Button b=new Button(this);b.setText(s);b.setTextColor(WHITE);b.setBackgroundColor(PANEL);b.setOnClickListener(c);b.setAllCaps(false);return b;}
    private TextView label(String s,int color){TextView t=new TextView(this);t.setText(s);t.setTextColor(color);t.setPadding(dp(10),0,dp(10),0);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}

    static final class BrowserDb extends SQLiteOpenHelper {
        BrowserDb(Context c){super(c,"demonic_browser.db",null,1);}
        @Override public void onCreate(SQLiteDatabase d){
            d.execSQL("CREATE TABLE history(id INTEGER PRIMARY KEY,title TEXT,url TEXT NOT NULL,visited_at INTEGER NOT NULL)");
            d.execSQL("CREATE TABLE bookmarks(id INTEGER PRIMARY KEY,title TEXT,url TEXT NOT NULL UNIQUE,created_at INTEGER NOT NULL)");
        }
        @Override public void onUpgrade(SQLiteDatabase d,int o,int n){}
        void history(String title,String url){getWritableDatabase().execSQL("INSERT INTO history(title,url,visited_at) VALUES(?,?,?)",new Object[]{title,url,System.currentTimeMillis()});}
        void bookmark(String title,String url){getWritableDatabase().execSQL("INSERT OR REPLACE INTO bookmarks(title,url,created_at) VALUES(?,?,?)",new Object[]{title,url,System.currentTimeMillis()});}
        List<String[]> rows(String sql){ArrayList<String[]> out=new ArrayList<>();try(Cursor c=getReadableDatabase().rawQuery(sql,null)){while(c.moveToNext())out.add(new String[]{c.getString(0),c.getString(1)});}return out;}
        void clearHistory(){getWritableDatabase().delete("history",null,null);}
    }
}
