package com.flymaccin.lootcheerom
object NativeBridge {
    init { System.loadLibrary("lootchee_frontend") }
    external fun open(path:String, systemDir:String, saveDir:String):Boolean
    external fun loadGame(path:String?, bytes:ByteArray):Boolean
    external fun runFrame()
    external fun reset()
    external fun setButton(id:Int, down:Boolean)\n    external fun setPointer(x:Float,y:Float,down:Boolean)
    external fun hardwareSurfaceCreated()\n    external fun hardwareSurfaceChanged(width:Int,height:Int)\n    external fun presentHardwareFrame()\n    external fun hardwareSurfaceDestroyed()\n    external fun hardwareRequested():Boolean\n    external fun hardwareContextType():Int\n    external fun frame():IntArray
    external fun frameWidth():Int
    external fun frameHeight():Int
    external fun drainAudio():ShortArray
    external fun sampleRate():Int
    external fun saveState():ByteArray?
    external fun loadState(data:ByteArray):Boolean
    external fun sram():ByteArray?
    external fun restoreSram(data:ByteArray):Boolean
}
