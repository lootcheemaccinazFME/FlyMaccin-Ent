package com.flymaccin.lootcheerom
import android.content.Context
import android.media.*
import android.net.Uri
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean

class SameBoyCore(private val context:Context, private val onFrame:(IntArray,Int,Int)->Unit):EmulatorCore {
    override val id="sameboy"
    override val systems=setOf("Game Boy","Game Boy Color")
    private val running=AtomicBoolean(false)
    private var thread:Thread?=null
    private var audio:AudioTrack?=null
    private var gameKey="game"
    private val map=mapOf(
        GameAction.B to 0, GameAction.Y to 1, GameAction.SELECT to 2, GameAction.START to 3,
        GameAction.UP to 4, GameAction.DOWN to 5, GameAction.LEFT to 6, GameAction.RIGHT to 7,
        GameAction.A to 8, GameAction.X to 9, GameAction.L1 to 10, GameAction.R1 to 11
    )
    init {
        val path=File(context.applicationInfo.nativeLibraryDir,"libsameboy_libretro_android.so").absolutePath
        check(NativeBridge.open(path)) { "SameBoy core failed to load" }
    }
    override fun load(uri:Uri):Result<Unit> = runCatching {
        val bytes=context.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
        gameKey=RomVault.inspect(context,uri).sha256
        check(NativeBridge.loadRom(bytes)) { "SameBoy rejected ROM" }
        restoreSram()
    }
    fun loadBuiltIn(bytes:ByteArray):Result<Unit> = runCatching { gameKey="lootchee-test-rom"; check(NativeBridge.loadRom(bytes)) { "SameBoy rejected built-in test ROM" }; restoreSram() }\n    override fun start():Result<Unit> = runCatching {
        if(running.getAndSet(true)) return@runCatching
        val rate=NativeBridge.sampleRate().coerceAtLeast(8000)
        val min=AudioTrack.getMinBufferSize(rate,AudioFormat.CHANNEL_OUT_STEREO,AudioFormat.ENCODING_PCM_16BIT).coerceAtLeast(rate/5)
        audio=AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(AudioFormat.Builder().setSampleRate(rate).setEncoding(AudioFormat.ENCODING_PCM_16BIT).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build())
            .setBufferSizeInBytes(min*2).setTransferMode(AudioTrack.MODE_STREAM).build().also{it.play()}
        thread=Thread {
            val frameNs=16_742_706L
            while(running.get()) {
                val start=System.nanoTime(); NativeBridge.runFrame()
                val px=NativeBridge.frame(); val w=NativeBridge.frameWidth(); val h=NativeBridge.frameHeight()
                if(px.isNotEmpty()&&w>0&&h>0) onFrame(px,w,h)
                val pcm=NativeBridge.drainAudio(); if(pcm.isNotEmpty()) audio?.write(pcm,0,pcm.size,AudioTrack.WRITE_NON_BLOCKING)
                val wait=frameNs-(System.nanoTime()-start); if(wait>0) Thread.sleep(wait/1_000_000,(wait%1_000_000).toInt())
            }
        }.apply{name="LootcheeEmulation";start()}
    }
    override fun pause(){ running.set(false); thread?.join(250); audio?.pause(); persistSram() }
    override fun resume(){ start() }
    override fun reset(){ NativeBridge.reset() }
    override fun stop(){ pause(); audio?.release(); audio=null }
    override fun button(action:GameAction,pressed:Boolean){ map[action]?.let{NativeBridge.setButton(it,pressed)} }
    override fun axis(action:GameAction,value:Float) {
        val dead=.45f
        when(action){ GameAction.LX->{button(GameAction.LEFT,value < -dead);button(GameAction.RIGHT,value > dead)}
            GameAction.LY->{button(GameAction.UP,value < -dead);button(GameAction.DOWN,value > dead)} else->Unit }
    }
    fun saveState(slot:Int=0):Boolean { val d=NativeBridge.saveState()?:return false; stateFile(slot).writeBytes(d); persistSram(); return true }
    fun loadState(slot:Int=0):Boolean { val f=stateFile(slot); return f.exists()&&NativeBridge.loadState(f.readBytes()) }
    private fun stateFile(slot:Int)=File(context.filesDir,"states/"+gameKey+".state"+slot).also{it.parentFile?.mkdirs()}
    private fun sramFile()=File(context.filesDir,"saves/"+gameKey+".srm").also{it.parentFile?.mkdirs()}
    private fun persistSram(){ NativeBridge.sram()?.let{ data -> val f=sramFile(); val tmp=File(f.path+".tmp"); tmp.writeBytes(data); if(f.exists())f.delete(); tmp.renameTo(f)} }
    private fun restoreSram(){ val f=sramFile(); if(f.exists()) NativeBridge.restoreSram(f.readBytes()) }
}
