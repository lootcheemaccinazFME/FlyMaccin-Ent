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
    private var core:LibretroCore?=null
    override fun onCreate(state:Bundle?){
        super.onCreate(state)
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.BLACK)}
        val bar=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        fun button(label:String,run:()->Unit)=Button(this).apply{text=label;setOnClickListener{run()}}
        status=TextView(this).apply{setTextColor(Color.WHITE);text="LOOTCHEE OS • SAMEBOY";setPadding(12,8,12,8)}
        bar.addView(button("TEST ROM"){loadTest()}); bar.addView(button("IMPORT ROM"){pickRom()})
        bar.addView(button("SAVE STATE"){if(core?.saveState(0)==true) status.text="STATE SAVED"})
        bar.addView(button("LOAD STATE"){if(core?.loadState(0)==true) status.text="STATE LOADED"})
        bar.addView(button("RESET"){core?.reset()})
        root.addView(status); root.addView(bar)
        screen=EmulatorView(this);screen.touchSink={x,y,down->NativeBridge.setPointer(x,y,down)};root.addView(screen,LinearLayout.LayoutParams(-1,0,1f));setContentView(root)
        if(intent.getBooleanExtra("autoTest",false)) loadTest()
    }
    private fun loadTest(){ switchCore(CoreRegistry.specs.first{it.id=="sameboy"}); val c=core?:return;c.loadBuiltIn(TestRom.build(),"lootchee-test-rom").onSuccess{c.start();status.text="PLAYING: LOOTCHEE INPUT TEST • SAMEBOY"}.onFailure{status.text="TEST ROM ERROR: "+it.message} }
    private fun switchCore(spec:CoreSpec){core?.stop();core=runCatching{LibretroCore(this,spec){p,w,h->runOnUiThread{screen.submit(p,w,h)}}}.getOrElse{status.text="CORE ERROR "+spec.id+": "+it.message;null}}
    private fun pickRom(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{addCategory(Intent.CATEGORY_OPENABLE);type="application/octet-stream";addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)},100)}
    @Deprecated("compat")
    override fun onActivityResult(req:Int,result:Int,data:Intent?){super.onActivityResult(req,result,data);if(req==100&&result==RESULT_OK&&data?.data!=null){
        val uri=data.data!!;runCatching{contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)}
        val spec=CoreRegistry.forUri(uri);if(spec==null){status.text="UNSUPPORTED ROM TYPE";return};switchCore(spec);val c=core?:return;c.load(uri).onSuccess{c.start();status.text="PLAYING "+spec.systems.joinToString("/")+" • "+spec.id}.onFailure{status.text="ROM ERROR: "+it.message}
    }}
    override fun onPause(){super.onPause();core?.pause()}
    override fun onResume(){super.onResume();core?.resume()}
    override fun onDestroy(){core?.stop();super.onDestroy()}
    override fun dispatchKeyEvent(e:KeyEvent):Boolean{
        if((e.source and InputDevice.SOURCE_GAMEPAD)!=0||(e.source and InputDevice.SOURCE_JOYSTICK)!=0){
            val a=when(e.keyCode){KeyEvent.KEYCODE_DPAD_UP->GameAction.UP;KeyEvent.KEYCODE_DPAD_DOWN->GameAction.DOWN;KeyEvent.KEYCODE_DPAD_LEFT->GameAction.LEFT;KeyEvent.KEYCODE_DPAD_RIGHT->GameAction.RIGHT;KeyEvent.KEYCODE_BUTTON_A->GameAction.A;KeyEvent.KEYCODE_BUTTON_B->GameAction.B;KeyEvent.KEYCODE_BUTTON_X->GameAction.X;KeyEvent.KEYCODE_BUTTON_Y->GameAction.Y;KeyEvent.KEYCODE_BUTTON_START->GameAction.START;KeyEvent.KEYCODE_BUTTON_SELECT->GameAction.SELECT;KeyEvent.KEYCODE_BUTTON_L1->GameAction.L1;KeyEvent.KEYCODE_BUTTON_R1->GameAction.R1;else->null}
            if(a!=null){core?.button(a,e.action==KeyEvent.ACTION_DOWN);return true}
        };return super.dispatchKeyEvent(e)
    }
    override fun onGenericMotionEvent(e:MotionEvent):Boolean{if((e.source and InputDevice.SOURCE_JOYSTICK)!=0){core?.axis(GameAction.LX,e.getAxisValue(MotionEvent.AXIS_X));core?.axis(GameAction.LY,e.getAxisValue(MotionEvent.AXIS_Y));return true};return super.onGenericMotionEvent(e)}
}
