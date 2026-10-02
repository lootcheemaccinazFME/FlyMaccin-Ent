package com.flymaccin.lootcheerom

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.net.Uri
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import android.widget.TextView

class LiveTvPlayer(context:Context):FrameLayout(context),SurfaceHolder.Callback {
    private val surface=SurfaceView(context)
    private val label=TextView(context).apply{setTextColor(Color.WHITE);setBackgroundColor(0x66000000);text="FREE LIVE TV"}
    private var player:MediaPlayer?=null
    private var pending:Uri?=null
    init{setBackgroundColor(Color.BLACK);addView(surface,LayoutParams(-1,-1));addView(label,LayoutParams(-1,-2));surface.holder.addCallback(this)}
    fun tune(name:String,url:String){
        label.text="LIVE • $name";pending=Uri.parse(url)
        if(surface.holder.surface?.isValid==true)start()
    }
    private fun start(){
        val uri=pending?:return;player?.release();player=MediaPlayer().apply{
            setDataSource(context,uri);setDisplay(surface.holder);setOnPreparedListener{it.start()}
            setOnErrorListener{_,what,extra->label.text="LIVE TV ERROR • $what/$extra";true};prepareAsync()
        }
    }
    fun stop(){player?.release();player=null}
    override fun surfaceCreated(h:SurfaceHolder){if(pending!=null)start()}
    override fun surfaceChanged(h:SurfaceHolder,f:Int,w:Int,hgt:Int){}
    override fun surfaceDestroyed(h:SurfaceHolder){player?.setDisplay(null)}
}
