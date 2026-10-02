package com.flymaccin.lootcheerom
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.*

object DemonicDawRoom {
 fun build(a:Activity):View{
  val root=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(FmeCockpit.BG);setPadding(10,8,10,8)}
  root.addView(FmeCockpit.title(a,"DEMONIC DAW"))
  root.addView(FmeCockpit.subtitle(a,"ARRANGE • CREATE • MIX • RECORD • AI / MAESTRO • DELIVER"))
  val transport=LinearLayout(a).apply{orientation=LinearLayout.HORIZONTAL;background=FmeCockpit.panel()}
  listOf("⏮","▶","■","●","↻","LOOP").forEach{transport.addView(FmeCockpit.button(a,it){},LinearLayout.LayoutParams(0,76,1f))}
  transport.addView(TextView(a).apply{text="  MY SONG   •   96 BPM";setTextColor(Color.WHITE);gravity=android.view.Gravity.CENTER},LinearLayout.LayoutParams(0,76,2f));root.addView(transport)
  val scroll=ScrollView(a);val body=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL}
  val timeline=FmeCockpit.section(a,"ARRANGEMENT / TIMELINE")
  listOf("DRUMS","808","BASS","KEYS","SYNTH","VOCALS","AI MUSIC","FX","VIDEO").forEachIndexed{i,n->
   val row=LinearLayout(a).apply{orientation=LinearLayout.HORIZONTAL}
   row.addView(TextView(a).apply{text=n;setTextColor(Color.WHITE);setPadding(8,8,8,8)},LinearLayout.LayoutParams(0,56,1f))
   row.addView(ProgressBar(a,null,android.R.attr.progressBarStyleHorizontal).apply{max=100;progress=35+(i*7)%60},LinearLayout.LayoutParams(0,56,3f));timeline.addView(row)
  };body.addView(timeline)
  val modules=FmeCockpit.section(a,"CREATE / ENGINE MODULES")
  val grid=GridLayout(a).apply{columnCount=2}
  DemonicDawModules.modules.forEach{m->grid.addView(FmeCockpit.button(a,m.title+"\n"+m.capability){Toast.makeText(a,m.title+" • engine transplant pending where native dependency is required",Toast.LENGTH_SHORT).show()},GridLayout.LayoutParams().apply{width=0;height=130;columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);setMargins(4,4,4,4)})}
  modules.addView(grid);body.addView(modules)
  val mixer=FmeCockpit.section(a,"MIXER / AUDIO GRAPH")
  val meters=LinearLayout(a).apply{orientation=LinearLayout.HORIZONTAL};listOf("DRUM","808","BASS","KEYS","SYNTH","VOCAL","FX","MASTER").forEachIndexed{i,n->meters.addView(LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;gravity=android.view.Gravity.CENTER;addView(ProgressBar(a,null,android.R.attr.progressBarStyleHorizontal).apply{rotation=-90f;max=100;progress=45+i*6},LinearLayout.LayoutParams(100,100));addView(TextView(a).apply{text=n;setTextColor(FmeCockpit.MUTED);textSize=9f})},LinearLayout.LayoutParams(0,150,1f))};mixer.addView(meters);body.addView(mixer)
  scroll.addView(body);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));return root
 }
}
