package com.flymaccin.lootcheerom
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.security.MessageDigest

data class RomRecord(val uri:Uri,val name:String,val size:Long,val sha256:String)
object RomVault {
    fun displayName(context:Context,uri:Uri):String {
        context.contentResolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use { c ->
            if(c.moveToFirst()){ val i=c.getColumnIndex(OpenableColumns.DISPLAY_NAME); if(i>=0) c.getString(i)?.takeIf{it.isNotBlank()}?.let{return it} }
        }
        return uri.lastPathSegment ?: "game"
    }
    fun extension(context:Context,uri:Uri)=displayName(context,uri).substringAfterLast('.', "").lowercase()
    fun inspect(context:Context,uri:Uri):RomRecord {
        val name=displayName(context,uri)
        val digest=MessageDigest.getInstance("SHA-256")
        var size=0L
        context.contentResolver.openInputStream(uri)!!.use { input ->
            val buf=ByteArray(65536)
            while(true){val n=input.read(buf);if(n<=0)break;digest.update(buf,0,n);size+=n}
        }
        return RomRecord(uri,name,size,digest.digest().joinToString(""){"%02x".format(it)})
    }
}
