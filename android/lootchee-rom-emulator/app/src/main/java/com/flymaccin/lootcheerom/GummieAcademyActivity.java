package com.flymaccin.lootcheerom;
import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.os.Handler;
import android.os.Looper;
import java.util.Random;

public final class GummieAcademyActivity extends Activity {
  private final Random random=new Random();
  private final Handler clock=new Handler(Looper.getMainLooper());
  private SharedPreferences prefs;
  private LinearLayout root;
  private int answer,seconds=600;
  private String subject="Math";
  private boolean timerRunning=false;
  private TextView question,stats;
  private EditText response;
  private final String[] subjects={"Math","Reading","Science","Vocabulary"};
  private final String[][] prompts={
    {"What is 8 × 7?","What is 144 ÷ 12?","What is 35 + 48?","What is 9 × 6?","What is 100 - 37?"},
    {"A character returns a lost wallet. What trait does this show?","What is the main idea of a passage?","What does 'predict' mean?","Which word is a synonym for joyful?","What is a setting in a story?"},
    {"Which planet is known as the Red Planet?","What do plants absorb from sunlight to make food?","What force pulls objects toward Earth?","What is the solid form of water?","Which organ pumps blood?"},
    {"What does enormous mean?","What is the opposite of ancient?","What does cautious mean?","What does investigate mean?","What does generous mean?"}
  };
  private final String[][] keys={
    {"56","12","83","54","63"},
    {"honest","central message","guess what happens","happy","time and place"},
    {"mars","energy","gravity","ice","heart"},
    {"very large","modern","careful","examine","giving"}
  };
  private int current=0;
  @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("gummie_academy",MODE_PRIVATE);home();}
  private void base(String heading){
    ScrollView scroll=new ScrollView(this);root=new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL);root.setPadding(24,24,24,24);
    root.setBackgroundColor(0xff14213d);scroll.addView(root);setContentView(scroll);
    TextView title=new TextView(this);title.setText(heading);title.setTextColor(0xffffd166);
    title.setTextSize(25);title.setGravity(Gravity.CENTER);title.setPadding(0,8,0,24);root.addView(title);
  }
  private void label(String value,int size){
    TextView t=new TextView(this);t.setText(value);t.setTextColor(Color.WHITE);t.setTextSize(size);
    t.setPadding(4,14,4,14);root.addView(t);
  }
  private void button(String text,Runnable run){
    Button b=new Button(this);b.setText(text);root.addView(b);b.setOnClickListener(v->run.run());
  }
  private void back(){button("← Academy Home",this::home);}
  private int stars(){return prefs.getInt("stars",0);}
  private void home(){
    base("🐻 Gummie Bear Academy\nWelcome, Kentrey!\nFourth Grade Learning HQ");
    label("⭐ Stars earned: "+stars()+"    📚 Questions solved: "+prefs.getInt("solved",0),18);
    for(String s:subjects)button(s+" Practice",()->practice(s));
    button("📝 Homework Checklist",this::homework);
    button("⏱ Focus Timer",this::focus);
    button("💡 Study Helper",this::helper);
    button("👪 Parent View",this::parent);
  }
  private void practice(String s){subject=s;current=random.nextInt(5);showQuestion();}
  private void showQuestion(){
    base(subject+" Practice");
    label("Answer the question to earn 10 stars.",17);
    question=new TextView(this);question.setText(prompts[index()][current]);
    question.setTextSize(21);question.setTextColor(Color.WHITE);question.setPadding(0,20,0,20);root.addView(question);
    response=new EditText(this);response.setSingleLine(false);response.setMinLines(1);
    response.setTextColor(Color.WHITE);response.setHintTextColor(0xffcccccc);
    response.setHint("Your answer");root.addView(response);
    button("Check Answer",()->{
      String given=response.getText().toString().trim().toLowerCase();
      String expected=keys[index()][current];
      if(given.isEmpty()){response.setError("Enter your answer");return;}
      boolean right=given.equals(expected)||given.contains(expected)||expected.contains(given)&&given.length()>=4;
      if(right){
        prefs.edit().putInt("stars",stars()+10).putInt("solved",prefs.getInt("solved",0)+1).apply();
        Toast.makeText(this,"Correct! +10 stars ⭐",Toast.LENGTH_LONG).show();current=random.nextInt(5);showQuestion();
      }else{
        Toast.makeText(this,"Try again. Hint: "+expected.substring(0,Math.min(2,expected.length()))+"…",Toast.LENGTH_LONG).show();
      }
    });
    button("Skip Question",()->{current=(current+1)%5;showQuestion();});
    back();
  }
  private int index(){for(int i=0;i<subjects.length;i++)if(subjects[i].equals(subject))return i;return 0;}
  private void homework(){
    base("📝 Homework Checklist");
    String[] tasks={"Math homework","Reading for 20 minutes","Science review","Vocabulary practice","Pack school bag"};
    for(int i=0;i<tasks.length;i++){
      final int id=i;CheckBox box=new CheckBox(this);box.setText(tasks[i]);box.setTextColor(Color.WHITE);
      box.setTextSize(18);box.setChecked(prefs.getBoolean("hw"+i,false));root.addView(box);
      box.setOnCheckedChangeListener((b,checked)->prefs.edit().putBoolean("hw"+id,checked).apply());
    }
    back();
  }
  private final Runnable countdown=new Runnable(){
    @Override public void run(){
      if(!timerRunning)return;
      if(seconds>0){seconds--;if(stats!=null)stats.setText(String.format("%02d:%02d",seconds/60,seconds%60));clock.postDelayed(this,1000);}
      else{timerRunning=false;Toast.makeText(GummieAcademyActivity.this,"Focus session complete!",Toast.LENGTH_LONG).show();}
    }
  };
  private void focus(){
    base("⏱ Focus Timer");label("Stay focused, then take a break.",18);
    stats=new TextView(this);stats.setText(String.format("%02d:%02d",seconds/60,seconds%60));
    stats.setTextSize(42);stats.setTextColor(Color.WHITE);stats.setGravity(Gravity.CENTER);root.addView(stats);
    button("Start / Pause",()->{timerRunning=!timerRunning;clock.removeCallbacks(countdown);if(timerRunning)clock.post(countdown);});
    button("Reset 10 Minutes",()->{timerRunning=false;clock.removeCallbacks(countdown);seconds=600;focus();});
    back();
  }
  private void helper(){
    base("💡 Study Helper");
    label("Study tips: Read the question twice. Underline important words. Solve one step at a time. Check your answer.",18);
    button("Math Tip",()->Toast.makeText(this,"Use multiplication facts to check division!",Toast.LENGTH_LONG).show());
    button("Reading Tip",()->Toast.makeText(this,"Find who, what, where, when, and why.",Toast.LENGTH_LONG).show());
    button("Science Tip",()->Toast.makeText(this,"Make a prediction, observe, then explain.",Toast.LENGTH_LONG).show());
    back();
  }
  private void parent(){
    base("👪 Parent View");
    label("Kentrey's Progress",22);label("⭐ Earned stars: "+stars(),20);
    label("Questions solved: "+prefs.getInt("solved",0),19);
    int completed=0;for(int i=0;i<5;i++)if(prefs.getBoolean("hw"+i,false))completed++;
    label("Homework tasks complete: "+completed+"/5",19);
    label("Progress is saved locally on this device.",16);
    back();
  }
  @Override protected void onDestroy(){timerRunning=false;clock.removeCallbacks(countdown);super.onDestroy();}
}
