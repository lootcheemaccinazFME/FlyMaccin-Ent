package com.flymaccin.lootcheerom
import android.content.Context
import android.graphics.*
import android.view.View
class EmulatorView(c:Context):View(c){
    @Volatile private var bitmap:Bitmap?=null
    fun submit(p:IntArray,w:Int,h:Int){ bitmap=Bitmap.createBitmap(p,w,h,Bitmap.Config.ARGB_8888); postInvalidateOnAnimation() }
    override fun onDraw(canvas:Canvas){ super.onDraw(canvas); bitmap?.let{ b ->
        val scale=minOf(width.toFloat()/b.width,height.toFloat()/b.height); val dw=b.width*scale; val dh=b.height*scale
        val dst=RectF((width-dw)/2,(height-dh)/2,(width+dw)/2,(height+dh)/2)
        canvas.drawBitmap(b,null,dst,Paint(Paint.FILTER_BITMAP_FLAG))
    }}
}
