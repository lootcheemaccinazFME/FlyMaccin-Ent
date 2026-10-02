package com.flymaccin.lootcheerom
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.*
import android.widget.*

object FmeCockpit {
 const val BG=0xff05070b.toInt(); const val PANEL=0xff09111a.toInt(); const val RED=0xffff1628.toInt(); const val TEXT=0xfff5f7fa.toInt(); const val MUTED=0xff8f9aa8.toInt()
 fun title(a:Activity,text:String)=TextView(a).apply{this.text=text;setTextColor(RED);textSize=22f;typeface=android.graphics.Typeface.DEFAULT_BOLD;setPadding(10,8,10,8)}
 fun subtitle(a:Activity,text:String)=TextView(a).apply{this.text=text;setTextColor(MUTED);textSize=11f;setPadding(10,2,10,8)}
 fun field(a:Activity,hintText:String)=EditText(a).apply{hint=hintText;setHintTextColor(MUTED);setTextColor(TEXT);setBackgroundColor(0xff060b11.toInt());setPadding(12,10,12,10)}
 fun section(a:Activity,title:String)=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;background=panel();setPadding(10,8,10,8);addView(FmeCockpit.title(a,title))}
 fun panel():GradientDrawable=GradientDrawable().apply{setColor(PANEL);setStroke(2,0xff551018.toInt());cornerRadius=10f}
 fun button(a:Activity,label:String,run:()->Unit)=Button(a).apply{text=label;setTextColor(TEXT);textSize=11f;isAllCaps=true;background=GradientDrawable().apply{setColor(0xff08121d.toInt());setStroke(2,0xff7d1520.toInt());cornerRadius=8f};setPadding(8,4,8,4);setOnClickListener{run()}}
 fun header(a:Activity):View=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;background=panel();setPadding(14,8,14,8)
   addView(TextView(a).apply{text="FLYMACCIN ENT";setTextColor(RED);textSize=25f;typeface=android.graphics.Typeface.DEFAULT_BOLD})
   addView(TextView(a).apply{text="ONE APP • EVERYTHING FME";setTextColor(0xffffb14a.toInt());textSize=10f})
 }
 fun nav(a:Activity,open:(String)->Unit):HorizontalScrollView=HorizontalScrollView(a).apply{isHorizontalScrollBarEnabled=false;addView(LinearLayout(a).apply{orientation=LinearLayout.HORIZONTAL
   listOf("home" to "HOME","daw" to "DEMONIC DAW","tv" to "TV / MEDIA","emulator" to "EMULATOR","games" to "FME GAMES","agent" to "AGENT / AI","video" to "AI VIDEO","comics" to "COMICS / DESIGN","writing" to "WRITING","assets" to "ASSETS").forEach{(id,n)->addView(button(a,n){open(id)},LinearLayout.LayoutParams(180,92).apply{setMargins(4,4,4,4)})}
 })}
 fun home(a:Activity,open:(String)->Unit):View=ScrollView(a).apply{setBackgroundColor(BG);addView(LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setPadding(12,12,12,12)
   addView(TextView(a).apply{text="FME CONTROL CENTER";setTextColor(TEXT);textSize=22f})
   addView(TextView(a).apply{text="CREATE • PLAY • WATCH • BUILD • DESIGN • NO BORDERS";setTextColor(MUTED);textSize=11f})
   val grid=GridLayout(a).apply{columnCount=2;setPadding(0,12,0,12)}
   listOf("daw" to "DEMONIC DAW\nMusic • Mix • Record","tv" to "DEMONIC TV\nLive TV • Media • Import","emulator" to "UNIVERSAL EMULATOR\n16 Cores • ROM Library","games" to "FME GAMES\nHY-PHYXELS • Arcade","agent" to "FME AGENT\nTools • Tasks • Files","potna" to "POCKET POTNA\nAssistant • Commands","video" to "AI VIDEO\nGenerate • Animate","octop" to "OCTOP FME\nControl Plane").forEach{(id,n)->grid.addView(button(a,n){open(id)},GridLayout.LayoutParams().apply{width=0;height=150;columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);setMargins(5,5,5,5)})}
   addView(grid)
 })}
}
