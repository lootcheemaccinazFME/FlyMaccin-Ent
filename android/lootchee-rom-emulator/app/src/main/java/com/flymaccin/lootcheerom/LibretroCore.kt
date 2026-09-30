package com.flymaccin.lootcheerom
import android.content.Context
import android.media.*
import android.net.Uri
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class LibretroCore(private val context:Context, private val spec:CoreSpec, private val onFrame:(IntArray,Int,Int)->Unit):EmulatorCore {
    override val id=spec.id; override val systems=spec.systems
    private val running=AtomicBoolean(false); private var loaded=false; private var bootMarked=false; private var thread:Thread?=null; private var audio:AudioTrack?=null; private var gameKey="game"
    private val map=mapOf(GameAction.B to 0,GameAction.Y to 1,GameAction.SELECT to 2,GameAction.START to 3,GameAction.UP to 4,GameAction.DOWN to 5,GameAction.LEFT to 6,GameAction.RIGHT to 7,GameAction.A to 8,GameAction.X to 9,GameAction.L1 to 10,GameAction.R1 to 11,GameAction.L2 to 12,GameAction.R2 to 13)
    init { check(NativeBridge.open(CoreRegistry.libraryPath(context,spec),FirmwareManager.systemDir(context).absolutePath,FirmwareManager.saveDir(context).absolutePath)){"Core failed: "+spec.id} }
    override fun load(uri:Uri)=runCatching { val record=RomVault.inspect(context,uri);gameKey=record.sha256;val bytes=context.contentResolver.openInputStream(uri)!!.use{it.readBytes()};val local=File(context.cacheDir,"content/"+gameKey+"."+CoreRegistry.extension(context,uri)).also{it.parentFile?.mkdirs();it.writeBytes(bytes)};check(NativeBridge.loadGame(local.absolutePath,bytes)){"Core rejected ROM"};loaded=true;restoreSram() }
    fun loadBuiltIn(bytes:ByteArray,key:String)=runCatching { gameKey=key;check(NativeBridge.loadGame(null,bytes));loaded=true;restoreSram() }
    override fun start()=runCatching {
        if(running.getAndSet(true))return@runCatching
        val rate=NativeBridge.sampleRate().coerceIn(8000,384000)
        val frameNs=(1_000_000_000.0/NativeBridge.fps().coerceIn(10.0,240.0)).toLong()
        if(audio==null){
            val min=AudioTrack.getMinBufferSize(rate,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT).coerceAtLeast(rate/5)
            audio=AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()).setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build()).setBufferSizeInBytes(min*2).setTransferMode(AudioTrack.MODE_STREAM).build()
        }
        audio?.play()
        thread=Thread{while(running.get()){val s=System.nanoTime();NativeBridge.runFrame();val p=NativeBridge.frame();val w=NativeBridge.frameWidth();val h=NativeBridge.frameHeight();if(p.isNotEmpty()&&w>0&&h>0){onFrame(p,w,h);if(!bootMarked){File(context.filesDir,"boot.ok").writeText(spec.id+" "+w+"x"+h);bootMarked=true}};val pcm=NativeBridge.drainAudio();if(pcm.isNotEmpty())audio?.write(pcm,0,pcm.size,AudioTrack.WRITE_NON_BLOCKING);val wait=frameNs-(System.nanoTime()-s);if(wait>0)Thread.sleep(wait/1_000_000,(wait%1_000_000).toInt())}}.apply{name="Lootchee-"+spec.id;start()}
    }
    override fun pause(){running.set(false);thread?.join(250);audio?.pause();persistSram()}
    override fun resume(){if(loaded)start()};override fun reset(){NativeBridge.reset()};override fun stop(){pause();audio?.release();audio=null;loaded=false;bootMarked=false;NativeBridge.close()}
    override fun button(action:GameAction,pressed:Boolean){map[action]?.let{NativeBridge.setButton(it,pressed)}};override fun axis(action:GameAction,value:Float){val d=.45f;if(action==GameAction.LX){button(GameAction.LEFT,value < -d);button(GameAction.RIGHT,value>d)};if(action==GameAction.LY){button(GameAction.UP,value < -d);button(GameAction.DOWN,value>d)}}
    fun saveState(slot:Int=0)=NativeBridge.saveState()?.let{stateFile(slot).writeBytes(it);persistSram();true}?:false
    fun loadState(slot:Int=0)=stateFile(slot).let{it.exists()&&NativeBridge.loadState(it.readBytes())}
    private fun stateFile(slot:Int)=File(context.filesDir,"states/"+spec.id+"/"+gameKey+".state"+slot).also{it.parentFile?.mkdirs()}
    private fun sramFile()=File(context.filesDir,"saves/"+spec.id+"/"+gameKey+".srm").also{it.parentFile?.mkdirs()}
    private fun persistSram(){NativeBridge.sram()?.let{d->val f=sramFile();val t=File(f.path+".tmp");t.writeBytes(d);if(f.exists())f.delete();t.renameTo(f)}}
    private fun restoreSram(){sramFile().takeIf{it.exists()}?.let{NativeBridge.restoreSram(it.readBytes())}}
}
