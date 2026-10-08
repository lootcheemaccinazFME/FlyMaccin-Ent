package com.flymaccin.lootcheerom;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.graphics.Color;
import android.view.View;
import android.widget.*;
import java.util.ArrayList;

public final class FmeToolsActivity extends Activity {
  private LinearLayout root;
  private SharedPreferences prefs;
  private String mode;
  private void label(String s) { TextView t=new TextView(this);t.setText(s);t.setTextColor(Color.WHITE);t.setTextSize(18);t.setPadding(8,12,8,12);root.addView(t); }
  private void button(String title, Runnable action) { Button b=new Button(this);b.setText(title);b.setAllCaps(false);root.addView(b);b.setOnClickListener(v->action.run()); }
  private EditText input(String hint) {EditText e=new EditText(this);e.setHint(hint);e.setSingleLine(false);e.setTextColor(Color.WHITE);e.setHintTextColor(0xffaaaaaa);root.addView(e);return e;}
  private void save(String key,String value){prefs.edit().putString(key,value).apply();}
  private void refresh(){root.removeAllViews();label("FLYMACCIN ENT  •  "+mode);button("← Back",this::finish);
    switch(mode){
      case "DEMONIC TV": tv();break;
      case "FME AGENT": agent();break;
      case "POCKET POTNA": potna();break;
      case "AI VIDEO": video();break;
      case "OCTOR FME": control();break;
      case "FME GAMES": games();break;
      default: label("Module unavailable");break;
    }
  }
  @Override public void onCreate(Bundle b){super.onCreate(b);mode=getIntent().getStringExtra("module");if(mode==null)mode="OCTOR FME";prefs=getSharedPreferences("fme_tools",MODE_PRIVATE);ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setPadding(20,25,20,25);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(0xff090d19);sc.addView(root);setContentView(sc);refresh();}
  private void tv(){label("Local media library: select a video to play in an installed player.");button("Open video",()->{Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("video/*");startActivityForResult(i,1);});label("Streaming TV channels are not yet integrated.");}
  @Override protected void onActivityResult(int req,int result,Intent data){super.onActivityResult(req,result,data);if(req==1&&result==RESULT_OK&&data!=null){Uri uri=data.getData();if(uri!=null){Intent play=new Intent(Intent.ACTION_VIEW);play.setDataAndType(uri,"video/*");play.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);try{startActivity(play);}catch(Exception ex){Toast.makeText(this,"No video player found",Toast.LENGTH_LONG).show();}}}}
  private void agent(){label("Offline task board");EditText e=input("New task");button("Add task",()->{String v=e.getText().toString().trim();if(!v.isEmpty()){save("tasks",prefs.getString("tasks","")+v+"\n");refresh();}});String[] tasks=prefs.getString("tasks","").split("\n");for(String task:tasks)if(!task.isEmpty())button("✓ "+task,()->{save("tasks",prefs.getString("tasks","").replace(task+"\n",""));refresh();});}
  private void potna(){label("Offline command notebook. Commands: help, tasks, ideas.");EditText e=input("Enter command or idea");button("Run",()->{String q=e.getText().toString().trim();if(q.equalsIgnoreCase("help")){label("Try tasks or ideas. Notes are stored on this device.");}else if(q.equalsIgnoreCase("tasks")){label(prefs.getString("tasks","No tasks saved."));}else if(q.equalsIgnoreCase("ideas")){label(prefs.getString("ideas","No ideas saved."));}else if(!q.isEmpty()){save("ideas",prefs.getString("ideas","")+q+"\n");label("Saved idea: "+q);}});label("Cloud AI chat requires a configured API connection.");}
  private void video(){label("Video project storyboard (offline)");EditText e=input("Describe a scene or shot");button("Add scene",()->{String v=e.getText().toString().trim();if(!v.isEmpty()){save("scenes",prefs.getString("scenes","")+v+"\n");refresh();}});label(prefs.getString("scenes","No scenes yet."));button("Share storyboard",()->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,prefs.getString("scenes",""));startActivity(Intent.createChooser(i,"Share storyboard"));});label("Video rendering and AI generation are not yet connected.");}
  private void control(){label("LOCAL CONTROL PLANE");label("Tasks: "+prefs.getString("tasks","").split("\n").length+" entries");label("Storyboard: "+prefs.getString("scenes","").split("\n").length+" entries");label("Ideas: "+prefs.getString("ideas","").split("\n").length+" entries");button("Export local workspace",()->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,"TASKS\n"+prefs.getString("tasks","")+"\nIDEAS\n"+prefs.getString("ideas","")+"\nSCENES\n"+prefs.getString("scenes",""));startActivity(Intent.createChooser(i,"Export FME workspace"));});}
  private int answer,score,round;private final java.util.Random random=new java.util.Random();
  private void games(){label("FME Math Arcade • 10 rounds");button("Start Math Challenge",()->{score=0;round=0;nextQuestion();});}
  private void nextQuestion(){root.removeAllViews();label("FME MATH ARCADE • "+(round+1)+"/10 • Score "+score);if(round>=10){label("Finished! Score "+score+"/10");button("Play again",this::games);return;}int a=random.nextInt(12)+1,b=random.nextInt(12)+1;answer=a*b;label(a+" × "+b+" = ?");EditText e=input("Answer");button("Submit",()->{try{if(Integer.parseInt(e.getText().toString().trim())==answer)score++;}catch(NumberFormatException ignored){}round++;nextQuestion();});}
}
