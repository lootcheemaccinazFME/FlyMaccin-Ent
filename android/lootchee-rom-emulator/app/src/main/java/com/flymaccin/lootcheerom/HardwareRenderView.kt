package com.flymaccin.lootcheerom
import android.content.Context
import android.opengl.GLSurfaceView
import android.view.SurfaceHolder
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class HardwareRenderView(context:Context):GLSurfaceView(context),GLSurfaceView.Renderer {
    @Volatile var ready=false; private val latch=CountDownLatch(1)
    init { setEGLContextClientVersion(3);preserveEGLContextOnPause=true;setRenderer(this);renderMode=RENDERMODE_CONTINUOUSLY }
    override fun onSurfaceCreated(gl:GL10?,config:EGLConfig?){ready=true;latch.countDown();NativeBridge.hardwareSurfaceCreated()}
    override fun onSurfaceChanged(gl:GL10?,w:Int,h:Int){NativeBridge.hardwareSurfaceChanged(w,h)}
    override fun onDrawFrame(gl:GL10?){NativeBridge.presentHardwareFrame()}
    override fun surfaceDestroyed(holder:SurfaceHolder){ready=false;queueEvent{NativeBridge.hardwareSurfaceDestroyed()};super.surfaceDestroyed(holder)}
    fun awaitReady()=ready||latch.await(2,TimeUnit.SECONDS)
}
