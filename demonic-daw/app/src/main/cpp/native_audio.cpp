#include <jni.h>
#include <oboe/Oboe.h>
#include <fluidsynth.h>
#include <android/log.h>
#include <algorithm>
#include <cmath>
#include <cstdint>
#include <fstream>
#include <memory>
#include <mutex>
#include <string>
#include <vector>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR,"DemonicAudio",__VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"DemonicAudio",__VA_ARGS__)

struct SampleData { int sampleRate=44100, channels=1; std::vector<float> pcm; int frames() const { return channels?int(pcm.size()/channels):0; } };
struct Region {
    std::shared_ptr<SampleData> sample; int loKey=0,hiKey=127,loVel=0,hiVel=127,keyCenter=60;
    float gain=1.f,pan=0.f,tune=0.f; int transpose=0,loopMode=0,loopStart=-1,loopEnd=-1,offset=0,end=-1; float release=.15f;
};
struct Voice { const Region* r=nullptr; int note=0; double pos=0,step=1; float left=1,right=1,env=1; bool releasing=false,active=false; };

static uint16_t rd16(std::ifstream& f){ uint8_t b[2];f.read((char*)b,2);return uint16_t(b[0]|(b[1]<<8)); }
static uint32_t rd32(std::ifstream& f){ uint8_t b[4];f.read((char*)b,4);return uint32_t(b[0]|(b[1]<<8)|(b[2]<<16)|(b[3]<<24)); }

static std::shared_ptr<SampleData> loadWav(const std::string& path){
    std::ifstream f(path,std::ios::binary); if(!f)return {};
    char id[4]; f.read(id,4); if(std::string(id,4)!="RIFF")return {}; rd32(f); f.read(id,4); if(std::string(id,4)!="WAVE")return {};
    int fmt=0,ch=0,bits=0,sr=0; std::vector<uint8_t> raw;
    while(f && !f.eof()){ f.read(id,4); if(f.gcount()!=4)break; uint32_t n=rd32(f); std::string cid(id,4);
        if(cid=="fmt "){ fmt=rd16(f);ch=rd16(f);sr=(int)rd32(f);rd32(f);rd16(f);bits=rd16(f); if(n>16)f.seekg(n-16,std::ios::cur); }
        else if(cid=="data"){ raw.resize(n);f.read((char*)raw.data(),n); }
        else f.seekg(n,std::ios::cur); if(n&1)f.seekg(1,std::ios::cur);
    }
    if(raw.empty()||ch<1||ch>2||sr<=0)return {};
    auto s=std::make_shared<SampleData>();s->sampleRate=sr;s->channels=ch;
    if(fmt==1&&bits==16){ size_t count=raw.size()/2;s->pcm.resize(count); for(size_t i=0;i<count;i++){int16_t v=int16_t(raw[i*2]|(raw[i*2+1]<<8));s->pcm[i]=v/32768.f;} }
    else if(fmt==3&&bits==32){ size_t count=raw.size()/4;s->pcm.resize(count); const float* p=(const float*)raw.data();std::copy(p,p+count,s->pcm.begin()); }
    else return {};
    return s;
}

class DemonicAudioEngine: public oboe::AudioStreamDataCallback {
public:
 bool start(){ std::lock_guard<std::mutex> l(m_); if(stream_)return true; settings_=new_fluid_settings(); if(!settings_)return false;
   fluid_settings_setnum(settings_,"synth.sample-rate",48000.0);fluid_settings_setint(settings_,"synth.polyphony",128);fluid_settings_setnum(settings_,"synth.gain",.7);
   synth_=new_fluid_synth(settings_); if(!synth_)return false;
   oboe::AudioStreamBuilder b; b.setDirection(oboe::Direction::Output)->setPerformanceMode(oboe::PerformanceMode::LowLatency)->setSharingMode(oboe::SharingMode::Exclusive)->setFormat(oboe::AudioFormat::Float)->setChannelCount(2)->setSampleRate(48000)->setDataCallback(this);
   auto r=b.openStream(stream_); if(r!=oboe::Result::OK||!stream_){LOGE("openStream %s",oboe::convertToText(r));return false;} r=stream_->requestStart(); if(r!=oboe::Result::OK)return false; return true; }
 void stop(){std::lock_guard<std::mutex>l(m_);if(stream_){stream_->requestStop();stream_->close();stream_.reset();}if(synth_){delete_fluid_synth(synth_);synth_=nullptr;}if(settings_){delete_fluid_settings(settings_);settings_=nullptr;}}
 int loadSf2(const std::string&p){std::lock_guard<std::mutex>l(m_);regions_.clear();voices_.clear();sampleMode_=false;return synth_?fluid_synth_sfload(synth_,p.c_str(),1):-1;}
 void clearSamples(){std::lock_guard<std::mutex>l(m_);regions_.clear();voices_.clear();sampleMode_=true;}
 bool addRegion(const std::string&p,int lo,int hi,int lv,int hv,int kc,float db,float pan,float tune,int tr,int lm,int ls,int le,int off,int end,float rel){auto s=loadWav(p);if(!s)return false;Region r;r.sample=s;r.loKey=lo;r.hiKey=hi;r.loVel=lv;r.hiVel=hv;r.keyCenter=kc;r.gain=std::pow(10.f,db/20.f);r.pan=std::max(-100.f,std::min(100.f,pan))/100.f;r.tune=tune;r.transpose=tr;r.loopMode=lm;r.loopStart=ls;r.loopEnd=le;r.offset=std::max(0,off);r.end=end;r.release=std::max(.005f,rel);std::lock_guard<std::mutex>l(m_);regions_.push_back(std::move(r));sampleMode_=true;return true;}
 void noteOn(int ch,int key,int vel){std::lock_guard<std::mutex>l(m_);if(!sampleMode_&&synth_){fluid_synth_noteon(synth_,ch,key,vel);return;}for(auto& r:regions_)if(key>=r.loKey&&key<=r.hiKey&&vel>=r.loVel&&vel<=r.hiVel){Voice v;v.r=&r;v.note=key;v.pos=r.offset;double semis=(key-r.keyCenter)+r.transpose+r.tune/100.0;v.step=std::pow(2.0,semis/12.0)*(double)r.sample->sampleRate/48000.0;float angle=(r.pan+1.f)*0.78539816339f;v.left=std::cos(angle)*r.gain*(vel/127.f);v.right=std::sin(angle)*r.gain*(vel/127.f);v.active=true;if(voices_.size()>=96)voices_.erase(voices_.begin());voices_.push_back(v);}}
 void noteOff(int ch,int key){std::lock_guard<std::mutex>l(m_);if(!sampleMode_&&synth_){fluid_synth_noteoff(synth_,ch,key);return;}for(auto&v:voices_)if(v.active&&v.note==key)v.releasing=true;}
 void programSelect(int ch,int sfid,int bank,int preset){std::lock_guard<std::mutex>l(m_);if(synth_)fluid_synth_program_select(synth_,ch,sfid,bank,preset);sampleMode_=false;}
 void cc(int ch,int c,int v){std::lock_guard<std::mutex>l(m_);if(synth_)fluid_synth_cc(synth_,ch,c,v);} void bend(int ch,int v){std::lock_guard<std::mutex>l(m_);if(synth_)fluid_synth_pitch_bend(synth_,ch,v);}
 void gain(float g){std::lock_guard<std::mutex>l(m_);master_=std::max(0.f,std::min(2.f,g));if(synth_)fluid_synth_set_gain(synth_,master_);}
 void reverb(float room,float damp,float width,float level){std::lock_guard<std::mutex>l(m_);if(synth_)fluid_synth_set_reverb(synth_,room,damp,width,level);} void chorus(int n,float level,float speed,float depth){std::lock_guard<std::mutex>l(m_);if(synth_)fluid_synth_set_chorus(synth_,n,level,speed,depth,FLUID_CHORUS_MOD_SINE);}
 oboe::DataCallbackResult onAudioReady(oboe::AudioStream*,void*data,int32_t n) override {float*out=(float*)data;std::fill(out,out+n*2,0.f);std::lock_guard<std::mutex>l(m_);if(!sampleMode_&&synth_){fluid_synth_write_float(synth_,n,out,0,2,out,1,2);return oboe::DataCallbackResult::Continue;}for(auto&v:voices_){if(!v.active||!v.r)continue;const Region&r=*v.r;const auto&s=*r.sample;int last=r.end>=0?std::min(r.end,s.frames()-1):s.frames()-1;int ls=r.loopStart>=0?r.loopStart:0,le=r.loopEnd>ls?std::min(r.loopEnd,last):last;for(int i=0;i<n&&v.active;i++){int a=(int)v.pos;if(a>=last){if(r.loopMode&&le>ls){v.pos=ls; a=ls;}else{v.active=false;break;}}int b=std::min(a+1,last);float frac=float(v.pos-a);auto read=[&](int frame,int c){int cc=std::min(c,s.channels-1);return s.pcm[frame*s.channels+cc];};float L=read(a,0)+(read(b,0)-read(a,0))*frac;float R=s.channels>1?(read(a,1)+(read(b,1)-read(a,1))*frac):L;if(v.releasing){float dec=1.f/(48000.f*std::max(.005f,r.release));v.env=std::max(0.f,v.env-dec);if(v.env<=0){v.active=false;break;}}out[i*2]+=L*v.left*v.env*master_;out[i*2+1]+=R*v.right*v.env*master_;v.pos+=v.step;if(r.loopMode&&v.pos>=le)v.pos=ls+(v.pos-le);}}
 voices_.erase(std::remove_if(voices_.begin(),voices_.end(),[](const Voice&v){return !v.active;}),voices_.end());for(int i=0;i<n*2;i++)out[i]=std::tanh(out[i]);return oboe::DataCallbackResult::Continue; }
 ~DemonicAudioEngine()override{stop();}
private:std::mutex m_;fluid_settings_t*settings_=nullptr;fluid_synth_t*synth_=nullptr;std::shared_ptr<oboe::AudioStream>stream_;std::vector<Region>regions_;std::vector<Voice>voices_;bool sampleMode_=false;float master_=.85f;
};
static DemonicAudioEngine E;
extern "C" JNIEXPORT jboolean JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeStart(JNIEnv*,jclass){return E.start();}
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeStop(JNIEnv*,jclass){E.stop();}
extern "C" JNIEXPORT jint JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeLoadSoundFont(JNIEnv*e,jclass,jstring s){const char*p=e->GetStringUTFChars(s,0);int r=E.loadSf2(p);e->ReleaseStringUTFChars(s,p);return r;}
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeClearSampleBank(JNIEnv*,jclass){E.clearSamples();}
extern "C" JNIEXPORT jboolean JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeAddWavRegion(JNIEnv*e,jclass,jstring s,jint lo,jint hi,jint lv,jint hv,jint kc,jfloat db,jfloat pan,jfloat tune,jint tr,jint lm,jint ls,jint le,jint off,jint end,jfloat rel){const char*p=e->GetStringUTFChars(s,0);bool ok=E.addRegion(p,lo,hi,lv,hv,kc,db,pan,tune,tr,lm,ls,le,off,end,rel);e->ReleaseStringUTFChars(s,p);return ok;}
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeNoteOn(JNIEnv*,jclass,jint c,jint k,jint v){E.noteOn(c,k,v);}extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeNoteOff(JNIEnv*,jclass,jint c,jint k){E.noteOff(c,k);}extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeProgramSelect(JNIEnv*,jclass,jint c,jint s,jint b,jint p){E.programSelect(c,s,b,p);}extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeCc(JNIEnv*,jclass,jint c,jint n,jint v){E.cc(c,n,v);}extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativePitchBend(JNIEnv*,jclass,jint c,jint v){E.bend(c,v);}extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeSetGain(JNIEnv*,jclass,jfloat v){E.gain(v);}extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeSetReverb(JNIEnv*,jclass,jfloat a,jfloat b,jfloat c,jfloat d){E.reverb(a,b,c,d);}extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeSetChorus(JNIEnv*,jclass,jint a,jfloat b,jfloat c,jfloat d){E.chorus(a,b,c,d);}
