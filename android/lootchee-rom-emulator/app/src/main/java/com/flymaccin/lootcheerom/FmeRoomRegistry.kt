package com.flymaccin.lootcheerom

data class FmeRoom(val id:String,val title:String,val kind:String)
object FmeRoomRegistry {
    val rooms=listOf(
        FmeRoom("home","FME Home","SYSTEM"),
        FmeRoom("emulator","Universal Emulator","NATIVE ENGINE"),
        FmeRoom("daw","Demonic DAW","UNIFIED NATIVE AUDIO"),
        FmeRoom("games","FME Games","NATIVE GAMES"),
        FmeRoom("8bit","8-Bit Ism","NATIVE GAME"),
        FmeRoom("hyphyxels","HY-PHYXELS: Arcade Invasion","NATIVE GAME"),
        FmeRoom("carnival","Bay Carnival3 Parade","NATIVE GAME"),
        FmeRoom("agent","FME Agent","NATIVE SERVICE"),
        FmeRoom("potna","Pocket Potna","NATIVE CLIENT"),
        FmeRoom("remote","Pocket Potna Remote","CONTROL ROOM"),
        FmeRoom("tv","Demonic TV","MEDIA ROOM"),
        FmeRoom("video","FME AI Video Generator","AI ROOM"),
        FmeRoom("octop","Octop FME","CONTROL PLANE"),
        FmeRoom("bayauto","Bay Auto RP","NATIVE GAME")
    )
}
