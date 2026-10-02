package com.flymaccin.lootcheerom
import android.app.Activity
import android.graphics.*
import android.view.*
import android.widget.*
import java.util.Random
object FmeGameRooms {
 fun build(a:Activity,id:String):View = if(id=="games") arcade(a) else GameView(a,id)
 fun arcade(a:Activity):View { val root=LinearLayout(a).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(FmeCockpit.BG);setPadding(10,8,10,8);addView(FmeCockpit.title(a,"FME GAMES"));addView(FmeCockpit.subtitle(a,"PLAY • MY GAMES • TEST • ASSETS"))}; val stage=FrameLayout(a).apply{background=FmeCockpit.panel()}; root.addView(stage,LinearLayout.LayoutParams(-1,0,1f)); val nav=LinearLayout(a); listOf("8bit" to "8-BIT ISM","hyphyxels" to "HY-PHYXELS","carnival" to "BAY CARNIVAL3").forEach{(id,n)->nav.addView(FmeCockpit.button(a,n){stage.removeAllViews();stage.addView(GameView(a,id))},LinearLayout.LayoutParams(0,-2,1f))};root.addView(nav);stage.addView(GameView(a,"hyphyxels"));return root }
 class GameView(a:Activity,val mode:String):View(a){val p=Paint(1);var px=160f;var py=360f;var score=0;val r=Random(323);val targets=MutableList(9){floatArrayOf(80+r.nextInt(700).toFloat(),100+r.nextInt(650).toFloat())}
  override fun onDraw(c:Canvas){c.drawColor(Color.rgb(5,7,11));p.color=Color.WHITE;p.textSize=30f;c.drawText(when(mode){"8bit"->"8-BIT ISM";"carnival"->"BAY CARNIVAL3 PARADE";else->"HY-PHYXELS: ARCADE INVASION"},25f,45f,p);p.textSize=18f;c.drawText("SCORE $score • TAP / DRAG • HIT THE PIXELS",25f,75f,p);p.color=Color.rgb(255,22,40);targets.forEach{c.drawRect(it[0]-14,it[1]-14,it[0]+14,it[1]+14,p)};p.color=Color.rgb(255,177,74);c.drawCircle(px,py,22f,p)}
  override fun onTouchEvent(e:MotionEvent):Boolean{if(e.action==0||e.action==2){px=e.x;py=e.y;targets.forEachIndexed{i,q->if(Math.hypot((px-q[0]).toDouble(),(py-q[1]).toDouble())<55){score+=if(mode=="hyphyxels")25 else 10;targets[i]=floatArrayOf(50+r.nextInt(Math.max(60,width-100)).toFloat(),100+r.nextInt(Math.max(120,height-180)).toFloat())}};invalidate()};return true}
 }
}