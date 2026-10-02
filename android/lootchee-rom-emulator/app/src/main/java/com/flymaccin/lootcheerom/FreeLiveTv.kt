package com.flymaccin.lootcheerom

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject

data class LiveChannel(val name:String,val stream:String)

object FreeLiveTv {
    private const val PREF="fme_free_live_tv"
    private const val KEY="channels"

    fun channels(context:Context):MutableList<LiveChannel>{
        val raw=context.getSharedPreferences(PREF,Context.MODE_PRIVATE).getString(KEY,"[]")?:"[]"
        return runCatching{
            val a=JSONArray(raw);MutableList(a.length()){i->val o=a.getJSONObject(i);LiveChannel(o.getString("name"),o.getString("stream"))}
        }.getOrElse{mutableListOf()}
    }

    fun add(context:Context,name:String,stream:String):Result<LiveChannel> = runCatching {
        require(name.isNotBlank()){"Channel name required"}
        val u=Uri.parse(stream.trim())
        require(u.scheme=="https"||u.scheme=="http"){"Use an HTTP(S) stream URL"}
        val item=LiveChannel(name.trim(),u.toString())
        val all=channels(context);all.removeAll{it.name.equals(item.name,true)};all.add(item);save(context,all);item
    }

    fun remove(context:Context,name:String){val all=channels(context);all.removeAll{it.name==name};save(context,all)}

    private fun save(context:Context,items:List<LiveChannel>){
        val a=JSONArray();items.forEach{a.put(JSONObject().put("name",it.name).put("stream",it.stream))}
        context.getSharedPreferences(PREF,Context.MODE_PRIVATE).edit().putString(KEY,a.toString()).apply()
    }
}
