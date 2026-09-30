package com.flymaccin.lootcheerom

object NativeBridge {
    init { System.loadLibrary("lootchee_frontend") }
    external fun open(path:String, systemDir:String, saveDir:String):Boolean
    external fun loadGame(path:String?, bytes:ByteArray):Boolean
    external fun runFrame()
    external fun reset()
    external fun setButton(id:Int, down:Boolean)
    external fun setPointer(x:Float, y:Float, down:Boolean)
    external fun hardwareSurfaceCreated()
    external fun hardwareSurfaceChanged(width:Int, height:Int)
    external fun presentHardwareFrame()
    external fun hardwareSurfaceDestroyed()
    external fun hardwareRequested():Boolean
    external fun hardwareContextType():Int
    external fun frame():IntArray
    external fun frameWidth():Int
    external fun frameHeight():Int
    external fun drainAudio():ShortArray
    external fun sampleRate():Int
    external fun saveState():ByteArray?
    external fun loadState(data:ByteArray):Boolean
    external fun sram():ByteArray?
    external fun restoreSram(data:ByteArray):Boolean
}
