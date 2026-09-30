#include <jni.h>
#include <dlfcn.h>
#include <stdint.h>
#include <stddef.h>
#include <vector>
#include <string>
#include <mutex>
#include <cstring>

struct retro_game_info { const char *path; const void *data; size_t size; const char *meta; };
struct retro_system_info { const char *library_name,*library_version,*valid_extensions; bool need_fullpath,block_extract; };
struct retro_game_geometry { unsigned base_width,base_height,max_width,max_height; float aspect_ratio; };
struct retro_system_timing { double fps,sample_rate; };
struct retro_system_av_info { retro_game_geometry geometry; retro_system_timing timing; };
typedef bool (*retro_environment_t)(unsigned, void*);
typedef void (*retro_video_refresh_t)(const void*, unsigned, unsigned, size_t);
typedef void (*retro_audio_sample_t)(int16_t,int16_t);
typedef size_t (*retro_audio_sample_batch_t)(const int16_t*,size_t);
typedef void (*retro_input_poll_t)(void);
typedef int16_t (*retro_input_state_t)(unsigned,unsigned,unsigned,unsigned);

static void* core=nullptr;
static void (*p_init)(); static void (*p_deinit)(); static bool (*p_load)(const retro_game_info*);
static void (*p_unload)(); static void (*p_run)(); static void (*p_reset)();
static size_t (*p_serialize_size)(); static bool (*p_serialize)(void*,size_t); static bool (*p_unserialize)(const void*,size_t);
static void* (*p_mem_data)(unsigned); static size_t (*p_mem_size)(unsigned);
static void (*p_set_env)(retro_environment_t); static void (*p_set_video)(retro_video_refresh_t);
static void (*p_set_audio)(retro_audio_sample_t); static void (*p_set_audio_batch)(retro_audio_sample_batch_t);
static void (*p_set_poll)(retro_input_poll_t); static void (*p_set_state)(retro_input_state_t);
static void (*p_get_av)(retro_system_av_info*); static void (*p_get_info)(retro_system_info*);

static std::vector<uint8_t> rom;
static std::vector<uint32_t> frame;
static std::vector<int16_t> audio;
static unsigned fw=0,fh=0,pixel_fmt=1;
static double sample_rate=48000.0;\nstatic std::string system_dir,save_dir,content_dir;\nstatic int16_t pointer_x=0,pointer_y=0,pointer_pressed=0;
static int16_t buttons[16]={0};
static std::mutex lock;

static bool env_cb(unsigned cmd, void* data) {
    if (cmd==10 && data) { pixel_fmt=*(unsigned*)data; return true; } // SET_PIXEL_FORMAT
    if (cmd==3) { if(data) *(bool*)data=true; return true; }          // GET_CAN_DUPE
    if (cmd==9) { if(data) *(const char**)data=system_dir.c_str(); return true; } // GET_SYSTEM_DIRECTORY
    if (cmd==31) { if(data) *(const char**)data=save_dir.c_str(); return true; } // GET_SAVE_DIRECTORY
    if (cmd==30) { if(data) *(const char**)data=content_dir.c_str(); return true; } // GET_CONTENT_DIRECTORY
    if (cmd==15) return false; // GET_VARIABLE
    if (cmd==17) { if(data) *(bool*)data=false; return true; }
    if (cmd==11 || cmd==16 || cmd==18) return true;
    return false;
}
static uint32_t rgb565(uint16_t p){ unsigned r=(p>>11)&31,g=(p>>5)&63,b=p&31; return 0xff000000u|((r*255/31)<<16)|((g*255/63)<<8)|(b*255/31); }
static uint32_t rgb1555(uint16_t p){ unsigned r=(p>>10)&31,g=(p>>5)&31,b=p&31; return 0xff000000u|((r*255/31)<<16)|((g*255/31)<<8)|(b*255/31); }
static void video_cb(const void* data,unsigned w,unsigned h,size_t pitch){
    if(!data) return; std::lock_guard<std::mutex> g(lock); fw=w; fh=h; frame.resize((size_t)w*h);
    for(unsigned y=0;y<h;y++){
        if(pixel_fmt==1){ auto row=(const uint32_t*)((const uint8_t*)data+y*pitch); for(unsigned x=0;x<w;x++) frame[y*w+x]=0xff000000u|(row[x]&0x00ffffffu); }
        else { auto row=(const uint16_t*)((const uint8_t*)data+y*pitch); for(unsigned x=0;x<w;x++) frame[y*w+x]=(pixel_fmt==2)?rgb565(row[x]):rgb1555(row[x]); }
    }
}
static void audio_one(int16_t l,int16_t r){ std::lock_guard<std::mutex> g(lock); audio.push_back(l); audio.push_back(r); }
static size_t audio_batch(const int16_t* d,size_t n){ std::lock_guard<std::mutex> g(lock); audio.insert(audio.end(),d,d+n*2); return n; }
static void input_poll(){}
static int16_t input_state(unsigned port,unsigned device,unsigned index,unsigned id){
    if(port) return 0;
    if(device==1 && !index && id<16) return buttons[id];
    if(device==6){ if(id==0)return pointer_x;if(id==1)return pointer_y;if(id==2)return pointer_pressed; }
    return 0;
}

template<class T> static bool sym(T& out,const char* n){ out=(T)dlsym(core,n); return out!=nullptr; }
static void close_core(){ if(p_unload) p_unload(); if(p_deinit) p_deinit(); if(core) dlclose(core); core=nullptr; }

extern "C" JNIEXPORT jboolean JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_open(JNIEnv* e,jobject,jstring path,jstring jsystem,jstring jsave){
    const char* sd=e->GetStringUTFChars(jsystem,nullptr);system_dir=sd?sd:"";e->ReleaseStringUTFChars(jsystem,sd);
    const char* sv=e->GetStringUTFChars(jsave,nullptr);save_dir=sv?sv:"";e->ReleaseStringUTFChars(jsave,sv);
    const char* p=e->GetStringUTFChars(path,nullptr); core=dlopen(p,RTLD_NOW|RTLD_LOCAL); e->ReleaseStringUTFChars(path,p); if(!core) return false;
    bool ok=sym(p_init,"retro_init")&&sym(p_deinit,"retro_deinit")&&sym(p_load,"retro_load_game")&&sym(p_unload,"retro_unload_game")&&sym(p_run,"retro_run")&&sym(p_reset,"retro_reset")&&sym(p_serialize_size,"retro_serialize_size")&&sym(p_serialize,"retro_serialize")&&sym(p_unserialize,"retro_unserialize")&&sym(p_mem_data,"retro_get_memory_data")&&sym(p_mem_size,"retro_get_memory_size")&&sym(p_set_env,"retro_set_environment")&&sym(p_set_video,"retro_set_video_refresh")&&sym(p_set_audio,"retro_set_audio_sample")&&sym(p_set_audio_batch,"retro_set_audio_sample_batch")&&sym(p_set_poll,"retro_set_input_poll")&&sym(p_set_state,"retro_set_input_state")&&sym(p_get_av,"retro_get_system_av_info")&&sym(p_get_info,"retro_get_system_info");
    if(!ok){ close_core(); return false; }
    p_set_env(env_cb); p_set_video(video_cb); p_set_audio(audio_one); p_set_audio_batch(audio_batch); p_set_poll(input_poll); p_set_state(input_state); p_init(); return true;
}
extern "C" JNIEXPORT jboolean JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_loadGame(JNIEnv* e,jobject,jstring jpath,jbyteArray a){
    retro_system_info si{}; p_get_info(&si);
    std::string path;
    if(jpath){ const char* p=e->GetStringUTFChars(jpath,nullptr); path=p?p:""; e->ReleaseStringUTFChars(jpath,p); auto slash=path.find_last_of("/\\\\"); content_dir=slash==std::string::npos?"":path.substr(0,slash); }
    retro_game_info info{};
    if(si.need_fullpath){ if(path.empty()) return false; info.path=path.c_str(); }
    else { jsize n=e->GetArrayLength(a); rom.resize(n); e->GetByteArrayRegion(a,0,n,(jbyte*)rom.data()); info.data=rom.data(); info.size=rom.size(); info.path=path.empty()?nullptr:path.c_str(); }
    if(!p_load||!p_load(&info)) return false; retro_system_av_info av{};p_get_av(&av);sample_rate=av.timing.sample_rate;return true;
}
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_runFrame(JNIEnv*,jobject){ if(p_run) p_run(); }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_reset(JNIEnv*,jobject){ if(p_reset) p_reset(); }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_setButton(JNIEnv*,jobject,jint id,jboolean down){ if(id>=0&&id<16) buttons[id]=down?1:0; }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_setPointer(JNIEnv*,jobject,jfloat x,jfloat y,jboolean down){ pointer_x=(int16_t)(x*32767.0f);pointer_y=(int16_t)(y*32767.0f);pointer_pressed=down?1:0; }\nextern "C" JNIEXPORT jintArray JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_frame(JNIEnv* e,jobject){
    std::lock_guard<std::mutex> g(lock); jintArray a=e->NewIntArray((jsize)frame.size()); if(a&&!frame.empty()) e->SetIntArrayRegion(a,0,(jsize)frame.size(),(jint*)frame.data()); return a;
}
extern "C" JNIEXPORT jint JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_frameWidth(JNIEnv*,jobject){return fw;}
extern "C" JNIEXPORT jint JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_frameHeight(JNIEnv*,jobject){return fh;}
extern "C" JNIEXPORT jshortArray JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_drainAudio(JNIEnv* e,jobject){
    std::lock_guard<std::mutex> g(lock); jshortArray a=e->NewShortArray((jsize)audio.size()); if(a&&!audio.empty()) e->SetShortArrayRegion(a,0,(jsize)audio.size(),audio.data()); audio.clear(); return a;
}
extern "C" JNIEXPORT jint JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_sampleRate(JNIEnv*,jobject){return (jint)sample_rate;}
extern "C" JNIEXPORT jbyteArray JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_saveState(JNIEnv* e,jobject){
    size_t n=p_serialize_size?p_serialize_size():0; if(!n) return nullptr; std::vector<uint8_t>b(n); if(!p_serialize(b.data(),n)) return nullptr; jbyteArray a=e->NewByteArray(n); e->SetByteArrayRegion(a,0,n,(jbyte*)b.data()); return a;
}
extern "C" JNIEXPORT jboolean JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_loadState(JNIEnv* e,jobject,jbyteArray a){
    if(!p_unserialize) return false; jsize n=e->GetArrayLength(a); std::vector<uint8_t>b(n); e->GetByteArrayRegion(a,0,n,(jbyte*)b.data()); return p_unserialize(b.data(),n);
}
extern "C" JNIEXPORT jbyteArray JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_sram(JNIEnv* e,jobject){
    size_t n=p_mem_size?p_mem_size(0):0; void*d=p_mem_data?p_mem_data(0):nullptr; if(!n||!d) return nullptr; jbyteArray a=e->NewByteArray(n); e->SetByteArrayRegion(a,0,n,(jbyte*)d); return a;
}
extern "C" JNIEXPORT jboolean JNICALL Java_com_flymaccin_lootcheerom_NativeBridge_restoreSram(JNIEnv* e,jobject,jbyteArray a){
    size_t n=p_mem_size?p_mem_size(0):0; void*d=p_mem_data?p_mem_data(0):nullptr; if(!n||!d||e->GetArrayLength(a)!=(jsize)n) return false; e->GetByteArrayRegion(a,0,n,(jbyte*)d); return true;
}
