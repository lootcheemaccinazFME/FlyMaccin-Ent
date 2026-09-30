package com.flymaccin.lootcheerom
import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
class EmulatorView(c:Context):View(c){
    @Volatile private var bitmap:Bitmap?=null
    var touchSink:((Float,Float,Boolean)->Unit)?=null
    fun submit(p:IntArray,w:Int,h:Int){bitmap=Bitmap.createBitmap(p,w,h,Bitmap.Config.ARGB_8888);postInvalidateOnAnimation()}
    override fun onDraw(canvas:Canvas){super.onDraw(canvas);bitmap?.let{b->val scale=minOf(width.toFloat()/b.width,height.toFloat()/b.height);val dw=b.width*scale;val dh=b.height*scale;canvas.drawBitmap(b,null,RectF((width-dw)/2,(height-dh)/2,(width+dw)/2,(height+dh)/2),Paint(Paint.FILTER_BITMAP_FLAG))}}
    override fun onTouchEvent(e:MotionEvent):Boolean{
        if(width<=0||height<=0)return false
        val nx=(e.x/width*2f-1f).coerceIn(-1f,1f);val ny=(e.y/height*2f-1f).coerceIn(-1f,1f)
        when(e.actionMasked){MotionEvent.ACTION_DOWN,MotionEvent.ACTION_MOVE->touchSink?.invoke(nx,ny,true);MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->touchSink?.invoke(nx,ny,false)}
        return true
    }
}
