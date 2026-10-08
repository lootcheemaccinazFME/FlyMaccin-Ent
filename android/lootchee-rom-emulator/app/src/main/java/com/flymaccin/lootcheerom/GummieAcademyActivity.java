package com.flymaccin.lootcheerom;
import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import java.util.Random;
public final class GummieAcademyActivity extends Activity {
  private final Random random = new Random();
  private int answer, correct, attempted;
  private TextView problem, score;
  private EditText input;
  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    ScrollView scroll = new ScrollView(this);
    LinearLayout root = new LinearLayout(this);
    root.setOrientation(LinearLayout.VERTICAL); root.setPadding(32,32,32,32);
    root.setBackgroundColor(0xff14213d); scroll.addView(root);
    TextView header = new TextView(this); header.setText("Gummie Bear Academy\nFourth Grade Math Lab");
    header.setTextColor(Color.WHITE); header.setTextSize(25); header.setGravity(Gravity.CENTER);
    root.addView(header);
    problem = new TextView(this); problem.setTextSize(26); problem.setTextColor(0xffffd166);
    problem.setGravity(Gravity.CENTER); problem.setPadding(0,50,0,30); root.addView(problem);
    input = new EditText(this); input.setHint("Enter your answer");
    input.setTextColor(Color.WHITE); input.setHintTextColor(0xffcccccc);
    input.setInputType(2); root.addView(input);
    Button check = new Button(this); check.setText("Check Answer"); root.addView(check);
    score = new TextView(this); score.setTextSize(20); score.setTextColor(Color.WHITE);
    score.setPadding(0,20,0,20); root.addView(score);
    check.setOnClickListener(v -> {
      try {
        int entered = Integer.parseInt(input.getText().toString().trim());
        attempted++;
        if (entered == answer) { correct++; Toast.makeText(this,"Correct! Great work!",Toast.LENGTH_SHORT).show(); }
        else Toast.makeText(this,"Try the next one! Answer: " + answer,Toast.LENGTH_LONG).show();
        next();
      } catch (NumberFormatException ex) { input.setError("Enter a number"); }
    });
    Button skip = new Button(this); skip.setText("New Question"); root.addView(skip);
    skip.setOnClickListener(v -> next());
    setContentView(scroll); next();
  }
  private void next() {
    int a = 2 + random.nextInt(12), b = 2 + random.nextInt(12);
    boolean multiply = random.nextBoolean();
    answer = multiply ? a*b : a+b;
    problem.setText(a + (multiply ? " × " : " + ") + b + " = ?");
    score.setText("Correct: " + correct + " / " + attempted);
    input.setText("");
  }
}
