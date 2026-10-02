package com.flymaccin.lootcheerom

data class DawModule(val id:String,val title:String,val capability:String)
object DemonicDawModules {
    val modules=listOf(
        DawModule("piano","Piano Studio","keys / performance / MIDI"),
        DawModule("chords","Chord + Guitar Studio","chords / guitar / harmony"),
        DawModule("drums","Drum Sequencer","16-step drums / 808 / snare / hats / percussion"),
        DawModule("record","Voice Recorder","recording / takes / audio capture"),
        DawModule("samples","Sample Library","samples / packs / browsing"),
        DawModule("projects","Projects","session save / load / project state"),
        DawModule("timeline","Timeline","arrangement / clips / transport"),
        DawModule("mixer","Mixer","levels / routing / automation"),
        DawModule("audio","Native Audio Engine","realtime graph / playback"),
        DawModule("render","Render + Export","offline render / export"),
        DawModule("generation","DAW 2 Generation","expanded generation / production tools")
    )
}
