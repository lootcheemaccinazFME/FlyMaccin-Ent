package com.flymaccin.lootcheerom
import android.content.Context
import android.net.Uri
import java.io.File
import java.security.MessageDigest

data class FirmwareRequirement(val relativePath:String,val md5:String?=null,val required:Boolean=true)
object FirmwareManager {
    fun systemDir(context:Context)=File(context.filesDir,"system").also{it.mkdirs()}
    fun saveDir(context:Context)=File(context.filesDir,"saves").also{it.mkdirs()}
    fun stateDir(context:Context)=File(context.filesDir,"states").also{it.mkdirs()}
    fun requirements(core:String)=when(core){
        "melondsds"->listOf(
            FirmwareRequirement("bios7.bin",required=false),FirmwareRequirement("bios9.bin",required=false),
            FirmwareRequirement("firmware.bin",required=false)
        )
        "flycast"->listOf(FirmwareRequirement("dc/dc_boot.bin",required=false))
        "beetle_pce_fast"->listOf(FirmwareRequirement("syscard3.pce",required=false))
        "geolith"->listOf(FirmwareRequirement("neogeo.zip",required=true))
        else->emptyList()
    }
    fun import(context:Context,uri:Uri,relativePath:String):Result<File> = runCatching {
        require(!relativePath.contains(".."))
        val out=File(systemDir(context),relativePath);out.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)!!.use{input->out.outputStream().use{input.copyTo(it)}}
        out
    }
    fun md5(file:File):String { val d=MessageDigest.getInstance("MD5");file.inputStream().use{i->val b=ByteArray(65536);while(true){val n=i.read(b);if(n<=0)break;d.update(b,0,n)}};return d.digest().joinToString(""){"%02x".format(it)} }
}
