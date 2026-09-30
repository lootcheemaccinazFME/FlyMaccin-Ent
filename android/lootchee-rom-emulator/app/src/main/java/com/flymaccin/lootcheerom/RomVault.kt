package com.flymaccin.lootcheerom
import android.content.Context
import android.net.Uri
import java.security.MessageDigest
data class RomRecord(val uri: Uri, val name: String, val size: Long, val sha256: String)
object RomVault {
    fun inspect(context: Context, uri: Uri): RomRecord {
        val name = uri.lastPathSegment ?: "game"
        val digest = MessageDigest.getInstance("SHA-256")
        var size = 0L
        context.contentResolver.openInputStream(uri)!!.use { input ->
            val buf = ByteArray(65536)
            while (true) {
                val n = input.read(buf); if (n <= 0) break
                digest.update(buf, 0, n); size += n
            }
        }
        return RomRecord(uri, name, size, digest.digest().joinToString("") { b -> "%02x".format(b) })
    }
}
