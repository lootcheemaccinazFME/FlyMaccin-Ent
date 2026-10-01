package com.flymaccin.videogenerator;
import android.app.*; import android.os.*; import android.content.*; import android.graphics.Color; import android.net.Uri; import android.view.*; import android.widget.*; import java.util.*;
public class MainActivity extends Activity {
 LinearLayout refs; TextView status; EditText prompt; final ArrayList<Uri> images=new ArrayList<>();
 public void onCreate(Bundle b){super.onCreate(b); render();}
 TextView t(String s,int sp){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(sp);v.setPadding(0,10,0,10);return v;}
 public void render(){
  ScrollView sc=new ScrollView(this); LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(28,36,28,36);root.setBackgroundColor(Color.rgb(7,7,7));sc.addView(root);
  TextView h=t("FME AI VIDEO GENERATOR",27);h.setTextColor(Color.rgb(212,175,55));root.addView(h);root.addView(t("PROMPT → REFERENCES → GENERATE → PREVIEW → SAVE",12));
  prompt=new EditText(this);prompt.setHint("Describe the video you want...");prompt.setHintTextColor(Color.GRAY);prompt.setTextColor(Color.WHITE);prompt.setMinLines(5);prompt.setGravity(Gravity.TOP);root.addView(prompt,new LinearLayout.LayoutParams(-1,-2));
  refs=new LinearLayout(this);refs.setOrientation(LinearLayout.VERTICAL);root.addView(refs); refreshRefs();
  Button add=new Button(this);add.setText("+ ADD REFERENCE IMAGE (MAX 3)");add.setOnClickListener(v->pick());root.addView(add);
  Spinner ratio=new Spinner(this);ratio.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"16:9 Widescreen","9:16 Vertical"}));root.addView(ratio);
  Button gen=new Button(this);gen.setText("GENERATE VIDEO");gen.setOnClickListener(v->{ if(prompt.getText().toString().trim().isEmpty()){status.setText("Add a prompt first.");return;} status.setText("Generator connector ready. Choose/configure a free generation backend to create the clip.");});root.addView(gen);
  status=t("Ready. No paid API is hard-wired.",14);root.addView(status);
  root.addView(t("Safe-frame engine reserved for a later editing stage. Character artwork will not be cropped just to fill a frame.",12));
  setContentView(sc);
 }
 void pick(){if(images.size()>=3){Toast.makeText(this,"Maximum 3 reference images",Toast.LENGTH_SHORT).show();return;}Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,44);}
 protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r==44&&c==RESULT_OK&&d!=null&&d.getData()!=null){images.add(d.getData());refreshRefs();}}
 void refreshRefs(){refs.removeAllViews();refs.addView(t("REFERENCE IMAGES  "+images.size()+"/3",14));for(int i=0;i<images.size();i++)refs.addView(t("✓ Reference "+(i+1),13));}
}