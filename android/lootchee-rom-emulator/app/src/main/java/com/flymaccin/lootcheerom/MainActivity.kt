package com.flymaccin.lootcheerom
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*

class MainActivity:Activity(){
    private lateinit var status:TextView
    private lateinit var screen:EmulatorView
    private lateinit var hardware:HardwareRenderView
    private lateinit var display:FrameLayout
    private var core:LibretroCore?=null
    private var liveTv:LiveTvPlayer?=null
    override fun onCreate(state:Bundle?){
        super.onCreate(state)
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(FmeCockpit.BG)}
        root.addView(FmeCockpit.header(this))
        root.addView(FmeCockpit.nav(this){openCockpitRoom(it)})
        status=TextView(this).apply{setTextColor(FmeCockpit.TEXT);setBackgroundColor(0xff070b10.toInt());text="FME • CONTROL CENTER • READY";setPadding(14,8,14,8)}
        root.addView(status)
        display=FrameLayout(this).apply{setBackgroundColor(FmeCockpit.BG)}
        screen=EmulatorView(this);screen.touchSink={x,y,down->NativeBridge.setPointer(x,y,down)}
        hardware=HardwareRenderView(this)
        root.addView(display,LinearLayout.LayoutParams(-1,0,1f))
        setContentView(root)
        showCockpitHome()
        if(intent.getBooleanExtra("autoTest",false)) loadTest()
    }
    private fun showCockpitHome(){core?.pause();liveTv?.stop();display.removeAllViews();display.addView(FmeCockpit.home(this){openCockpitRoom(it)},FrameLayout.LayoutParams(-1,-1));status.text="FME • HOME • EVERYTHING INSIDE"}
    private fun openCockpitRoom(id:String){
        if(id=="home"){showCockpitHome();return}
        val r=FmeRoomRegistry.rooms.firstOrNull{it.id==id}?:run{status.text="FME • $id • ROOM NOT REGISTERED";return}
        when(r.id){
            "tv"->showDemonicTv()
            "daw"->openRoom(r,DemonicDawRoom.build(this))
            "emulator"->openRoom(r,EmulatorCockpit.build(this){handleEmulatorCommand(it)})
            "games","8bit","hyphyxels","carnival"->openRoom(r,FmeGameRooms.build(this,r.id))
            "agent"->openRoom(r,FmeUtilityRooms.agent(this))
            "potna"->openRoom(r,FmeUtilityRooms.potna(this))
            "bayauto"->openRoom(r,FmeUtilityRooms.bayAuto(this))
            "octop"->openRoom(r,FmeControlRooms.octop(this))
            "remote"->openRoom(r,FmeControlRooms.remote(this))
            else->showRoomNotMigrated(r)
        }
    }
    private fun handleEmulatorCommand(cmd:String){when(cmd){"import"->pickRom();"test"->loadTest();"save"->{if(core?.saveState(0)==true)status.text="STATE SAVED"};"load"->{if(core?.loadState(0)==true)status.text="STATE LOADED"};"reset"->core?.reset()}}
    private fun showFmeRooms(){
        val labels=FmeRoomRegistry.rooms.map{it.title+"  •  "+it.kind}.toTypedArray()
        android.app.AlertDialog.Builder(this).setTitle("FME • EVERYTHING INSIDE").setItems(labels){_,i->
            val r=FmeRoomRegistry.rooms[i]
            when(r.id){
                "tv"->showDemonicTv()
                "emulator"->{status.text="UNIVERSAL EMULATOR • IMPORT A ROM";pickRom()}
                "games","8bit","hyphyxels","carnival"->openRoom(r,FmeGameRooms.build(this,r.id))
                "agent"->openRoom(r,FmeUtilityRooms.agent(this))
                "potna"->openRoom(r,FmeUtilityRooms.potna(this))
                "bayauto"->openRoom(r,FmeUtilityRooms.bayAuto(this))
                "octop"->openRoom(r,FmeControlRooms.octop(this))
                "remote"->openRoom(r,FmeControlRooms.remote(this))
                else->showRoomNotMigrated(r)
            }
        }.setNegativeButton("CLOSE",null).show()
    }
    private fun openRoom(r:FmeRoom,v:View){
        core?.pause();liveTv?.stop();display.removeAllViews();display.addView(v,FrameLayout.LayoutParams(-1,-1));status.text="FME ROOM • "+r.title+" • "+r.kind
    }
    private fun showRoomNotMigrated(r:FmeRoom){
        android.app.AlertDialog.Builder(this).setTitle(r.title).setMessage("This engine is not migrated into the one-APK runtime yet. It is intentionally not being presented as functional.").setPositiveButton("OK",null).show()
        status.text="NOT MIGRATED • "+r.title
    }
    private fun showDemonicTv(){
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,12,16,12);setBackgroundColor(FmeCockpit.BG);addView(FmeCockpit.title(this@MainActivity,"DEMONIC TV"));addView(FmeCockpit.subtitle(this@MainActivity,"LIVE TV • BROWSER / TV • IMPORT FILE • PLAYLISTS"))}
        val now=TextView(this).apply{text=DemonicTvRoom.currentName()?.let{"IMPORTED • "+it}?:"NO LOCAL MEDIA IMPORTED";setTextColor(FmeCockpit.TEXT);setPadding(8,8,8,8)}
        box.addView(now)
        val import=FmeCockpit.button(this,"IMPORT FILE"){DemonicTvRoom.importFile(this@MainActivity)}
        box.addView(import)
        val live=FmeCockpit.button(this,"FREE LIVE TV"){showFreeLiveTv()}
        box.addView(live)
        box.addView(FmeCockpit.subtitle(this,"Local media plus authorized/public free live streams. Everything plays inside Demonic TV."))
        android.app.AlertDialog.Builder(this).setTitle("DEMONIC TV").setView(box).setNegativeButton("CLOSE",null).show()
        status.text="FME ROOM • DEMONIC TV • MEDIA ROOM"
    }
    private fun showFreeLiveTv(){
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(14,10,14,10);setBackgroundColor(FmeCockpit.BG);addView(FmeCockpit.title(this@MainActivity,"DEMONIC TV • FREE LIVE TV"));addView(FmeCockpit.subtitle(this@MainActivity,"CHANNELS • STREAM • IMPORT • WATCH"))}
        val player=LiveTvPlayer(this);liveTv?.stop();liveTv=player;box.addView(player,LinearLayout.LayoutParams(-1,420))
        val name=FmeCockpit.field(this,"Channel name");val url=FmeCockpit.field(this,"Authorized/public stream URL (http/https)")
        box.addView(name);box.addView(url)
        val add=FmeCockpit.button(this,"ADD CHANNEL"){};add.setOnClickListener{FreeLiveTv.add(this@MainActivity,name.text.toString(),url.text.toString()).onSuccess{status.text="LIVE TV • ADDED • "+it.name;showFreeLiveTv()}.onFailure{status.text="LIVE TV • "+it.message}};box.addView(add)
        FreeLiveTv.channels(this).forEach{ch->
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            row.addView(Button(this).apply{text="WATCH • "+ch.name;setOnClickListener{player.tune(ch.name,ch.stream)}},LinearLayout.LayoutParams(0,-2,1f))
            row.addView(Button(this).apply{text="REMOVE";setOnClickListener{FreeLiveTv.remove(this@MainActivity,ch.name);showFreeLiveTv()}})
            box.addView(row)
        }
        android.app.AlertDialog.Builder(this).setTitle("DEMONIC TV • FREE LIVE TV").setView(box).setNegativeButton("CLOSE"){_,_->player.stop()}.show()
        status.text="DEMONIC TV • FREE LIVE TV"
    }
    private fun showSystems(){
        val names=CoreRegistry.specs.map{it.systems.joinToString(" / ")+"  •  "+it.id}.toTypedArray()
        android.app.AlertDialog.Builder(this).setTitle("FME UNIVERSAL EMULATOR • SYSTEMS").setItems(names){_,i->status.text="READY • "+CoreRegistry.specs[i].systems.joinToString("/")+" • IMPORT A ROM"}.setNegativeButton("CLOSE",null).show()
    }
    private fun chooseCore(uri:android.net.Uri,matches:List<CoreSpec>){
        val labels=matches.map{it.systems.joinToString(" / ")+"  •  "+it.id}.toTypedArray()
        android.app.AlertDialog.Builder(this).setTitle("CHOOSE SYSTEM").setItems(labels){_,i->loadUriWithCore(uri,matches[i])}.setNegativeButton("CANCEL",null).show()
    }
    private fun loadUriWithCore(uri:android.net.Uri,spec:CoreSpec){prepareDisplay(spec);switchCore(spec);val c=core?:return;c.load(uri).onSuccess{c.start();status.text="PLAYING "+spec.systems.joinToString("/")+" • "+spec.id}.onFailure{status.text="ROM ERROR: "+it.message}}
    private fun prepareDisplay(spec:CoreSpec){
        display.removeAllViews()
        if(RuntimeCapabilities.forCore(spec.id).backend==RenderBackend.OPENGL_ES){display.addView(hardware,FrameLayout.LayoutParams(-1,-1));status.text="HARDWARE RENDER • "+spec.id}
        else display.addView(screen,FrameLayout.LayoutParams(-1,-1))
    }
    private fun loadTest(){ prepareDisplay(CoreRegistry.specs.first{it.id=="sameboy"}); switchCore(CoreRegistry.specs.first{it.id=="sameboy"}); val c=core?:return;c.loadBuiltIn(TestRom.build(),"lootchee-test-rom").onSuccess{c.start();status.text="PLAYING: LOOTCHEE INPUT TEST • SAMEBOY"}.onFailure{status.text="TEST ROM ERROR: "+it.message} }
    private fun switchCore(spec:CoreSpec){core?.stop();core=runCatching{LibretroCore(this,spec){p,w,h->runOnUiThread{screen.submit(p,w,h)}}}.getOrElse{status.text="CORE ERROR "+spec.id+": "+it.message;null}}
    private fun pickRom(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/octet-stream";addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)},100)}
    @Deprecated("compat")
    override fun onActivityResult(req:Int,result:Int,data:Intent?){super.onActivityResult(req,result,data);if(req==DemonicTvRoom.IMPORT_REQUEST&&result==RESULT_OK){val name=DemonicTvRoom.accept(this,data);status.text=if(name!=null)"DEMONIC TV • IMPORTED • "+name else "DEMONIC TV • IMPORT FAILED";showDemonicTv();return};if(req==100&&result==RESULT_OK&&data?.data!=null){
        val uri=data.data!!;runCatching{contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)}
        val matches=CoreRegistry.candidates(this,uri);if(matches.isEmpty()){status.text="UNSUPPORTED ROM TYPE";return};if(matches.size>1){chooseCore(uri,matches);return};loadUriWithCore(uri,matches.first())
    }}
    override fun onPause(){super.onPause();core?.pause();hardware.onPause()}
    override fun onResume(){super.onResume();hardware.onResume();core?.resume()}
    override fun onDestroy(){liveTv?.stop();core?.stop();super.onDestroy()}
    override fun dispatchKeyEvent(e:KeyEvent):Boolean{
        if((e.source and InputDevice.SOURCE_GAMEPAD)!=0||(e.source and InputDevice.SOURCE_JOYSTICK)!=0){
            val a=when(e.keyCode){KeyEvent.KEYCODE_DPAD_UP->GameAction.UP;KeyEvent.KEYCODE_DPAD_DOWN->GameAction.DOWN;KeyEvent.KEYCODE_DPAD_LEFT->GameAction.LEFT;KeyEvent.KEYCODE_DPAD_RIGHT->GameAction.RIGHT;KeyEvent.KEYCODE_BUTTON_A->GameAction.A;KeyEvent.KEYCODE_BUTTON_B->GameAction.B;KeyEvent.KEYCODE_BUTTON_X->GameAction.X;KeyEvent.KEYCODE_BUTTON_Y->GameAction.Y;KeyEvent.KEYCODE_BUTTON_START->GameAction.START;KeyEvent.KEYCODE_BUTTON_SELECT->GameAction.SELECT;KeyEvent.KEYCODE_BUTTON_L1->GameAction.L1;KeyEvent.KEYCODE_BUTTON_R1->GameAction.R1;else->null}
            if(a!=null){core?.button(a,e.action==KeyEvent.ACTION_DOWN);return true}
        };return super.dispatchKeyEvent(e)
    }
    override fun onGenericMotionEvent(e:MotionEvent):Boolean{if((e.source and InputDevice.SOURCE_JOYSTICK)!=0){core?.axis(GameAction.LX,e.getAxisValue(MotionEvent.AXIS_X));core?.axis(GameAction.LY,e.getAxisValue(MotionEvent.AXIS_Y));return true};return super.onGenericMotionEvent(e)}
}
