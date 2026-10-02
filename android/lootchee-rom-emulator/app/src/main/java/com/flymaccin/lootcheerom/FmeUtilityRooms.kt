package com.flymaccin.lootcheerom
import android.app.*
import android.content.Intent
import android.graphics.Color
import android.provider.Settings
import android.view.View
import android.widget.*
object FmeUtilityRooms {
 private fun base(a:Activity,title:String,sub:String):LinearLayout=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setPadding(14,12,14,12);setBackgroundColor(FmeCockpit.BG);addView(FmeCockpit.title(a,title));addView(FmeCockpit.subtitle(a,sub))}
 fun agent(a:Activity):View=base(a,"FME AGENT / AI","AUTOMATION • TASKS • TOOLS • BUILD CONTROL").apply{
   val task=FmeCockpit.section(a,"RECENT TASKS");listOf("Android build pipeline","Runtime smoke gate","Failure-log repair","Release/version ledger").forEach{task.addView(TextView(a).apply{text="●  $it";setTextColor(FmeCockpit.TEXT);setPadding(8,8,8,8)})};addView(task,LinearLayout.LayoutParams(-1,0,1f))
   val tools=LinearLayout(a).apply{orientation=LinearLayout.HORIZONTAL};tools.addView(FmeCockpit.button(a,"DOWNLOADS"){a.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))},LinearLayout.LayoutParams(0,-2,1f));tools.addView(FmeCockpit.button(a,"FILES"){a.startActivity(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="*/*";addCategory(Intent.CATEGORY_OPENABLE)})},LinearLayout.LayoutParams(0,-2,1f));tools.addView(FmeCockpit.button(a,"SETTINGS"){a.startActivity(Intent(Settings.ACTION_SETTINGS))},LinearLayout.LayoutParams(0,-2,1f));addView(tools)
 }
 fun potna(a:Activity):View=base(a,"POCKET POTNA","ASSISTANT • COMMAND CENTER").apply{val log=TextView(a).apply{text="POTNA ONLINE\nWhat we building?";setTextColor(FmeCockpit.TEXT);setPadding(12,12,12,12);background=FmeCockpit.panel()};val input=FmeCockpit.field(a,"Type a command…");addView(log,LinearLayout.LayoutParams(-1,0,1f));addView(input);addView(FmeCockpit.button(a,"SEND COMMAND"){val q=input.text.toString().trim();if(q.isNotEmpty()){log.append("\n\nYOU  ›  $q\nPOTNA  ›  Command captured locally.");input.setText("")}})}
 fun bayAuto(a:Activity):View=base(a,"BAY AUTO RP","GARAGE • CRUISE • JOBS • CAR MEETS").apply{var cash=500;var rep=0;val stat=TextView(a).apply{text="BAY COUPE\nCASH  $500     REP  0";setTextColor(Color.WHITE);textSize=20f;setPadding(18,18,18,18);background=FmeCockpit.panel()};addView(stat,LinearLayout.LayoutParams(-1,0,1f));val controls=GridLayout(a).apply{columnCount=2};listOf("GARAGE","CRUISE","JOB","CAR MEET").forEach{s->controls.addView(FmeCockpit.button(a,s){if(s=="JOB"){cash+=125;rep+=5}else rep+=1;stat.text="BAY COUPE\nCASH  $$cash     REP  $rep"},GridLayout.LayoutParams().apply{width=0;columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f)})};addView(controls)}
}
