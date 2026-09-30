package com.flymaccin.lootcheerom
import android.content.Context
import android.net.Uri
import java.io.File

data class CoreSpec(val id:String,val library:String,val systems:Set<String>,val extensions:Set<String>,val license:String,val needsFirmware:Boolean=false)
object CoreRegistry {
    val specs=listOf(
        CoreSpec("sameboy","sameboy_libretro_android",setOf("Game Boy","Game Boy Color"),setOf("gb","gbc"),"MIT"),
        CoreSpec("fceumm","fceumm_libretro_android",setOf("NES","Famicom"),setOf("nes","unf","unif"),"GPL-2.0"),
        CoreSpec("mgba","mgba_libretro_android",setOf("Game Boy Advance"),setOf("gba"),"MPL-2.0"),
        CoreSpec("bsnes","bsnes_libretro_android",setOf("Super Nintendo","Super Famicom"),setOf("sfc","smc"),"GPL-3.0"),
        CoreSpec("gearsystem","gearsystem_libretro_android",setOf("Master System","Game Gear","SG-1000"),setOf("sms","gg","sg"),"GPL-3.0"),
        CoreSpec("clownmdemu","clownmdemu_libretro_android",setOf("Genesis","Mega Drive"),setOf("md","gen"),"AGPL-3.0"),
        CoreSpec("pcsx_rearmed","pcsx_rearmed_libretro_android",setOf("PlayStation"),setOf("cue","chd","pbp"),"GPL-2.0",true),
        CoreSpec("mupen64plus_next","mupen64plus_next_gles3_libretro_android",setOf("Nintendo 64","Nintendo 64DD"),setOf("n64","v64","z64","ndd"),"GPL-3.0"),
        CoreSpec("melondsds","melondsds_libretro_android",setOf("Nintendo DS","Nintendo DSi"),setOf("nds","dsi","ids"),"GPL-3.0",true),
        CoreSpec("ppsspp","ppsspp_libretro_android",setOf("PSP"),setOf("cso","prx"),"GPL-2.0"),
        CoreSpec("flycast","flycast_libretro_android",setOf("Dreamcast","NAOMI","Atomiswave"),setOf("cdi","gdi","elf","lst","dat","m3u"),"GPL-2.0",true),
        CoreSpec("beetle_pce_fast","mednafen_pce_fast_libretro_android",setOf("PC Engine","TurboGrafx-16","PC Engine CD","TurboGrafx-CD"),setOf("pce","ccd","img"),"GPL-2.0",true),
        CoreSpec("stella","stella_libretro_android",setOf("Atari 2600"),setOf("a26"),"GPL-2.0"),
        CoreSpec("handy","handy_libretro_android",setOf("Atari Lynx"),setOf("lnx"),"zlib"),
        CoreSpec("geolith","geolith_libretro_android",setOf("Neo Geo AES","Neo Geo MVS"),setOf("neo"),"BSD-3-Clause/MIT",true),
        CoreSpec("fbneo","fbneo_libretro_android",setOf("Arcade","Neo Geo"),setOf("zip","7z"),"Non-commercial",true),
        CoreSpec("play","play_libretro_android",setOf("PlayStation 2"),setOf("isz"),"MIT")
    )
    fun extension(context:Context,uri:Uri)=RomVault.extension(context,uri)
    fun forUri(context:Context,uri:Uri)=specs.firstOrNull{extension(context,uri) in it.extensions}
    fun libraryPath(context:Context,spec:CoreSpec)=File(context.applicationInfo.nativeLibraryDir,"lib"+spec.library+".so").absolutePath
}
