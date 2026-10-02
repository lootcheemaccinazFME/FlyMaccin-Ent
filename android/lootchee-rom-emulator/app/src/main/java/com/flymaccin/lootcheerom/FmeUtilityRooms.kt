package com.flymaccin.lootcheerom
import android.app.Activity
import android.app.DownloadManager
import android.content.Intent
import android.provider.Settings
import android.view.View
import android.widget.*
object FmeUtilityRooms {
 private fun base(a:Activity,title:String):LinearLayout=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setPadding(24,20,24,20);addView(TextView(a).apply{text=title;textSize=28f})}
 fun agent(a:Activity):View=base(a,"FME AGENT").apply{listOf("UMD / project specification","GitHub engineering workflow","Android build pipeline","Test + lint gates","Failure-log repair loop","Release/version ledger").forEach{addView(TextView(a).apply{text="✓ $it";textSize=17f})};addView(Button(a).apply{text="DOWNLOADS";setOnClickListener{a.startActivity(Intent(DownloadManager.ACTION_VIEW_DOWNLOADS))}});addView(Button(a).apply{text="FILES";setOnClickListener{a.startActivity(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="*/*";addCategory(Intent.CATEGORY_OPENABLE)})}});addView(Button(a).apply{text="SETTINGS";setOnClickListener{a.startActivity(Intent(Settings.ACTION_SETTINGS))}})}
 fun potna(a:Activity):View=base(a,"POCKET POTNA").apply{val log=TextView(a).apply{text="Pocket Potna ready."};val input=EditText(a).apply{hint="Talk to Pocket Potna…"};addView(log,LinearLayout.LayoutParams(-1,0,1f));addView(input);addView(Button(a).apply{text="SEND";setOnClickListener{val q=input.text.toString().trim();if(q.isNotEmpty()){log.append("\nYOU: $q\nPOTNA: Command captured locally.");input.setText("")}}})}
 fun bayAuto(a:Activity):View=base(a,"BAY AUTO RP").apply{var cash=500;var rep=0;val stat=TextView(a).apply{text="CAR: Bay Coupe • CASH: $500 • REP: 0"};addView(stat);listOf("GARAGE","CRUISE","JOB","CAR MEET").forEach{s->addView(Button(a).apply{text=s;setOnClickListener{if(s=="JOB"){cash+=125;rep+=5}else rep+=1;stat.text="CAR: Bay Coupe • CASH: $$cash • REP: $rep"}})}}
}