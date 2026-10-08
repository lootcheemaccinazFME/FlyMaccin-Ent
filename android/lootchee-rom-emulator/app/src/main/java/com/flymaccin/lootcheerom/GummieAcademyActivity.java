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
    button("🎮 Educational Games",this::games);\n    button("🧪 Timed Practice Tests",this::tests);\n    button("📝 Homework Checklist",this::homework);
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

  private int gameRound=0,gameScore=0,testRound=0,testScore=0;
  private long testStart=0;
  private void games(){
    base("🎮 Educational Games");
    label("Choose a game. Correct answers earn stars!",18);
    button("⚡ Multiplication Sprint",()->startGame(0));
    button("🔢 Number Detective",()->startGame(1));
    button("🧠 Word Detective",()->startGame(2));
    button("🌎 Science Explorer",()->startGame(3));
    back();
  }
  private int gameMode=0,gameAnswer=0;
  private String gameText="";
  private String[] gameChoices;
  private void startGame(int mode){gameMode=mode;gameRound=0;gameScore=0;nextGame();}
  private void nextGame(){
    if(gameRound>=10){
      int reward=gameScore*5;
      prefs.edit().putInt("stars",stars()+reward).putInt("games",prefs.getInt("games",0)+1).apply();
      base("🏆 Game Complete");
      label("Score: "+gameScore+"/10",24);label("Stars earned: +"+reward,20);
      button("Play Again",()->startGame(gameMode));back();return;
    }
    int a=2+random.nextInt(11),b=2+random.nextInt(11);
    if(gameMode==0){gameText="What is "+a+" × "+b+"?";gameAnswer=a*b;gameChoices=numberOptions(gameAnswer);}
    else if(gameMode==1){gameAnswer=a*10+b;gameText="Find the number: "+a+" tens and "+b+" ones.";gameChoices=numberOptions(gameAnswer);}
    else if(gameMode==2){
      String[] words={"enormous","cautious","ancient","observe","generous"};
      String[][] opts={{"very large","very tiny","very fast"},{"careful","careless","angry"},{"very old","brand new","colorful"},{"watch closely","run quickly","sleep"},{"giving","selfish","quiet"}};
      int n=random.nextInt(words.length);gameText="What does '"+words[n]+"' mean?";
      gameChoices=opts[n];gameAnswer=0;
    }else{
      String[] q={"Which planet is called the Red Planet?","What pulls objects toward Earth?","Which organ pumps blood?","What do bees collect from flowers?","What do roots absorb from soil?"};
      String[][] opts={{"Mars","Venus","Jupiter"},{"Gravity","Sound","Light"},{"Heart","Lungs","Stomach"},{"Nectar","Sand","Salt"},{"Water","Smoke","Plastic"}};
      int n=random.nextInt(q.length);gameText=q[n];gameChoices=opts[n];gameAnswer=0;
    }
    base("🎮 "+new String[]{"Multiplication Sprint","Number Detective","Word Detective","Science Explorer"}[gameMode]);
    label("Round "+(gameRound+1)+"/10   Correct: "+gameScore,18);label(gameText,23);
    for(int i=0;i<gameChoices.length;i++){
      final int index=i;button(gameChoices[i],()->{
        boolean right=index==gameAnswerIndex();
        if(right)gameScore++;
        Toast.makeText(this,right?"Correct! ⭐":"Answer: "+gameChoices[gameAnswerIndex()],Toast.LENGTH_SHORT).show();
        gameRound++;nextGame();
      });
    }
    back();
  }
  private int gameAnswerIndex(){return gameMode<2?gameAnswer==Integer.parseInt(gameChoices[0])?0:gameAnswer==Integer.parseInt(gameChoices[1])?1:2:0;}
  private String[] numberOptions(int correct){
    int wrong1=correct+1+random.nextInt(7),wrong2=Math.max(0,correct-1-random.nextInt(7));
    String[] arr={""+correct,""+wrong1,""+wrong2};
    for(int i=2;i>0;i--){int j=random.nextInt(i+1);String t=arr[i];arr[i]=arr[j];arr[j]=t;}
    return arr;
  }
  private int testMode=0;
  private void tests(){
    base("🧪 Timed Practice Tests");
    label("10 questions per test. Earn a score and track your best result.",18);
    for(int i=0;i<subjects.length;i++){final int m=i;button(subjects[i]+" Test",()->startTest(m));}
    back();
  }
  private void startTest(int mode){testMode=mode;testRound=0;testScore=0;testStart=System.currentTimeMillis();nextTest();}
  private void nextTest(){
    if(testRound>=10){
      int elapsed=(int)((System.currentTimeMillis()-testStart)/1000);
      String key="best_"+testMode;int best=Math.max(testScore,prefs.getInt(key,0));
      prefs.edit().putInt(key,best).putInt("tests",prefs.getInt("tests",0)+1).putInt("stars",stars()+testScore*5).apply();
      base("📊 Test Results");label(subjects[testMode]+": "+testScore+"/10",25);
      label("Time: "+elapsed+" seconds",19);label("Personal best: "+best+"/10",19);
      label("Earned "+(testScore*5)+" stars",19);
      button("Retake Test",()->startTest(testMode));back();return;
    }
    int n=random.nextInt(5);int a=2+random.nextInt(12),b=2+random.nextInt(12);
    String q,expected;
    if(testMode==0){q="Calculate "+a+" × "+b;expected=""+(a*b);}
    else{q=prompts[testMode][n];expected=keys[testMode][n];}
    base("🧪 "+subjects[testMode]+" Test");
    label("Question "+(testRound+1)+" of 10",17);label(q,22);
    EditText field=new EditText(this);field.setHint("Type answer");field.setTextColor(Color.WHITE);
    field.setHintTextColor(0xffbbbbbb);root.addView(field);
    button("Submit & Next",()->{
      String given=field.getText().toString().trim().toLowerCase();
      if(given.isEmpty()){field.setError("Answer required");return;}
      if(given.equals(expected)||given.contains(expected)||expected.contains(given)&&given.length()>=4)testScore++;
      testRound++;nextTest();
    });
    button("End Test",this::tests);
  }
\n  private void homework(){
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
    label("Questions solved: "+prefs.getInt("solved",0),19);\n    label("Games completed: "+prefs.getInt("games",0),19);\n    label("Tests completed: "+prefs.getInt("tests",0),19);\n    for(int i=0;i<subjects.length;i++)label(subjects[i]+" test best: "+prefs.getInt("best_"+i,0)+"/10",16);
    int completed=0;for(int i=0;i<5;i++)if(prefs.getBoolean("hw"+i,false))completed++;
    label("Homework tasks complete: "+completed+"/5",19);
    label("Progress is saved locally on this device.",16);
    back();
  }
  @Override protected void onDestroy(){timerRunning=false;clock.removeCallbacks(countdown);super.onDestroy();}
}
