package com.flymaccin.lootcheerom
import android.app.Activity
import android.graphics.Color
import android.view.View
import android.widget.*

object EmulatorCockpit {
 fun build(a:Activity,action:(String)->Unit):View{
  val root=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(FmeCockpit.BG);setPadding(10,8,10,8)}
  root.addView(FmeCockpit.title(a,"UNIVERSAL EMULATOR"))
  root.addView(FmeCockpit.subtitle(a,"LIBRARY • PLAY • BIOS • STATES • SETTINGS • 16 CORES"))
  val actions=LinearLayout(a).apply{orientation=LinearLayout.HORIZONTAL}
  listOf("IMPORT ROM" to "import","TEST ROM" to "test","SAVE STATE" to "save","LOAD STATE" to "load","RESET" to "reset").forEach{(n,id)->actions.addView(FmeCockpit.button(a,n){action(id)},LinearLayout.LayoutParams(0,78,1f))}
  root.addView(actions)
  val scroll=ScrollView(a);val body=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL}
  val systems=FmeCockpit.section(a,"SYSTEM LIBRARY")
  CoreRegistry.specs.forEach{s->
   val row=LinearLayout(a).apply{orientation=LinearLayout.HORIZONTAL;setPadding(4,4,4,4)}
   row.addView(TextView(a).apply{text=s.systems.joinToString(" / ");setTextColor(Color.WHITE);textSize=14f},LinearLayout.LayoutParams(0,58,2f))
   row.addView(TextView(a).apply{text=s.id.uppercase()+(if(s.needsFirmware)" • BIOS" else "");setTextColor(if(s.needsFirmware)0xffffb14a.toInt() else FmeCockpit.MUTED);gravity=android.view.Gravity.CENTER_VERTICAL},LinearLayout.LayoutParams(0,58,1f))
   systems.addView(row)
  };body.addView(systems)
  val note=FmeCockpit.section(a,"RUNTIME STATUS");note.addView(TextView(a).apply{text="SameBoy includes built-in frame-test support. Other cores remain subject to legal ROM/firmware runtime certification.";setTextColor(FmeCockpit.TEXT);setPadding(8,8,8,8)});body.addView(note)
  scroll.addView(body);root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f));return root
 }
}
