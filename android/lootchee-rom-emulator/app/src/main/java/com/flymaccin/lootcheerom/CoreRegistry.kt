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
        CoreSpec("pcsx_rearmed","pcsx_rearmed_libretro_android",setOf("PlayStation"),setOf("cue","chd","pbp"),"GPL-2.0",true)
    )
    fun extension(uri:Uri)=uri.lastPathSegment?.substringAfterLast('.', "")?.lowercase().orEmpty()
    fun forUri(uri:Uri)=specs.firstOrNull{extension(uri) in it.extensions}
    fun libraryPath(context:Context,spec:CoreSpec)=File(context.applicationInfo.nativeLibraryDir,"lib"+spec.library+".so").absolutePath
}
