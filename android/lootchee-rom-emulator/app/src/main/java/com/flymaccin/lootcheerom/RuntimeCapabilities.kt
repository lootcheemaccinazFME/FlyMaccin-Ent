package com.flymaccin.lootcheerom
enum class RenderBackend { SOFTWARE, OPENGL_ES, VULKAN, UNSUPPORTED }
data class RuntimeCapability(val core:String,val backend:RenderBackend,val note:String)
object RuntimeCapabilities {
    fun forCore(id:String)=when(id){
        "sameboy","fceumm","mgba","bsnes","gearsystem","clownmdemu","pcsx_rearmed","mednafen_pce_fast","stella","handy","geolith" -> RuntimeCapability(id,RenderBackend.SOFTWARE,"Software framebuffer supported by LOOTCHEE frontend")
        "mupen64plus_next","melondsds","ppsspp","flycast","play" -> RuntimeCapability(id,RenderBackend.OPENGL_ES,"Requires hardware-render context; EGL host in progress")
        else -> RuntimeCapability(id,RenderBackend.UNSUPPORTED,"No certified renderer")
    }
}
