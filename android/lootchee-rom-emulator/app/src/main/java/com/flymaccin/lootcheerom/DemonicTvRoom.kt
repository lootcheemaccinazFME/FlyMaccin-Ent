package com.flymaccin.lootcheerom

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns

object DemonicTvRoom {
    const val IMPORT_REQUEST=7301
    private var imported:Uri?=null
    private var importedName:String?=null

    fun importFile(activity:Activity){
        val i=Intent(Intent.ACTION_OPEN_DOCUMENT).apply{
            addCategory(Intent.CATEGORY_OPENABLE)
            type="*/*"
            putExtra(Intent.EXTRA_MIME_TYPES,arrayOf("video/*","audio/*"))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        activity.startActivityForResult(i,IMPORT_REQUEST)
    }

    fun accept(activity:Activity,data:Intent?):String?{
        val uri=data?.data?:return null
        runCatching{activity.contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)}
        val type=activity.contentResolver.getType(uri).orEmpty()
        if(!(type.startsWith("video/")||type.startsWith("audio/"))) return null
        imported=uri
        importedName=queryName(activity,uri)?:uri.lastPathSegment?:"Imported media"
        return importedName
    }

    fun currentUri():Uri?=imported
    fun currentName():String?=importedName

    private fun queryName(activity:Activity,uri:Uri):String?=runCatching{
        activity.contentResolver.query(uri,arrayOf(OpenableColumns.DISPLAY_NAME),null,null,null)?.use{c->if(c.moveToFirst())c.getString(0) else null}
    }.getOrNull()
}
