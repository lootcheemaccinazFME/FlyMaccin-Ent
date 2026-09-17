#include <jni.h>
#include <oboe/Oboe.h>
#include <fluidsynth.h>
#include <android/log.h>
#include <memory>
#include <mutex>
#include <string>

#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "DemonicAudio", __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "DemonicAudio", __VA_ARGS__)

class DemonicAudioEngine : public oboe::AudioStreamDataCallback {
public:
    bool start() {
        std::lock_guard<std::mutex> lock(mutex_);
        if (stream_) return true;

        settings_ = new_fluid_settings();
        if (!settings_) return false;
        fluid_settings_setnum(settings_, "synth.sample-rate", 48000.0);
        fluid_settings_setint(settings_, "synth.polyphony", 128);
        fluid_settings_setnum(settings_, "synth.gain", 0.7);
        fluid_settings_setint(settings_, "synth.reverb.active", 1);
        fluid_settings_setint(settings_, "synth.chorus.active", 1);
        synth_ = new_fluid_synth(settings_);
        if (!synth_) return false;

        oboe::AudioStreamBuilder builder;
        builder.setDirection(oboe::Direction::Output)
               ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
               ->setSharingMode(oboe::SharingMode::Exclusive)
               ->setFormat(oboe::AudioFormat::Float)
               ->setChannelCount(2)
               ->setSampleRate(48000)
               ->setDataCallback(this);
        auto result = builder.openStream(stream_);
        if (result != oboe::Result::OK || !stream_) {
            LOGE("openStream failed: %s", oboe::convertToText(result));
            return false;
        }
        result = stream_->requestStart();
        if (result != oboe::Result::OK) {
            LOGE("requestStart failed: %s", oboe::convertToText(result));
            return false;
        }
        LOGI("Native engine started at %d Hz", stream_->getSampleRate());
        return true;
    }

    void stop() {
        std::lock_guard<std::mutex> lock(mutex_);
        if (stream_) {
            stream_->requestStop();
            stream_->close();
            stream_.reset();
        }
        if (synth_) { delete_fluid_synth(synth_); synth_ = nullptr; }
        if (settings_) { delete_fluid_settings(settings_); settings_ = nullptr; }
    }

    int loadSoundFont(const std::string &path) {
        std::lock_guard<std::mutex> lock(mutex_);
        if (!synth_) return -1;
        return fluid_synth_sfload(synth_, path.c_str(), 1);
    }

    void noteOn(int channel, int key, int velocity) {
        std::lock_guard<std::mutex> lock(mutex_);
        if (synth_) fluid_synth_noteon(synth_, channel, key, velocity);
    }
    void noteOff(int channel, int key) {
        std::lock_guard<std::mutex> lock(mutex_);
        if (synth_) fluid_synth_noteoff(synth_, channel, key);
    }
    void programSelect(int channel, int soundFontId, int bank, int preset) {
        std::lock_guard<std::mutex> lock(mutex_);
        if (synth_) fluid_synth_program_select(synth_, channel, soundFontId, bank, preset);
    }
    void cc(int channel, int controller, int value) {
        std::lock_guard<std::mutex> lock(mutex_);
        if (synth_) fluid_synth_cc(synth_, channel, controller, value);
    }
    void pitchBend(int channel, int value) {
        std::lock_guard<std::mutex> lock(mutex_);
        if (synth_) fluid_synth_pitch_bend(synth_, channel, value);
    }
    void setGain(float gain) {
        std::lock_guard<std::mutex> lock(mutex_);
        if (synth_) fluid_synth_set_gain(synth_, gain);
    }
    void setReverb(float room, float damp, float width, float level) {
        std::lock_guard<std::mutex> lock(mutex_);
        if (synth_) fluid_synth_set_reverb(synth_, room, damp, width, level);
    }
    void setChorus(int voices, float level, float speed, float depth) {
        std::lock_guard<std::mutex> lock(mutex_);
        if (synth_) fluid_synth_set_chorus(synth_, voices, level, speed, depth, FLUID_CHORUS_MOD_SINE);
    }

    oboe::DataCallbackResult onAudioReady(oboe::AudioStream*, void *audioData, int32_t numFrames) override {
        float *out = static_cast<float *>(audioData);
        std::lock_guard<std::mutex> lock(mutex_);
        if (!synth_) {
            std::fill(out, out + numFrames * 2, 0.0f);
            return oboe::DataCallbackResult::Continue;
        }
        fluid_synth_write_float(synth_, numFrames, out, 0, 2, out, 1, 2);
        return oboe::DataCallbackResult::Continue;
    }

    ~DemonicAudioEngine() override { stop(); }

private:
    std::mutex mutex_;
    fluid_settings_t *settings_ = nullptr;
    fluid_synth_t *synth_ = nullptr;
    std::shared_ptr<oboe::AudioStream> stream_;
};

static DemonicAudioEngine gEngine;

extern "C" JNIEXPORT jboolean JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeStart(JNIEnv*, jclass) { return gEngine.start() ? JNI_TRUE : JNI_FALSE; }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeStop(JNIEnv*, jclass) { gEngine.stop(); }
extern "C" JNIEXPORT jint JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeLoadSoundFont(JNIEnv *env, jclass, jstring path) { const char *p = env->GetStringUTFChars(path, nullptr); int id = gEngine.loadSoundFont(p); env->ReleaseStringUTFChars(path, p); return id; }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeNoteOn(JNIEnv*, jclass, jint ch, jint key, jint vel) { gEngine.noteOn(ch, key, vel); }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeNoteOff(JNIEnv*, jclass, jint ch, jint key) { gEngine.noteOff(ch, key); }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeProgramSelect(JNIEnv*, jclass, jint ch, jint sfid, jint bank, jint preset) { gEngine.programSelect(ch, sfid, bank, preset); }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeCc(JNIEnv*, jclass, jint ch, jint ctrl, jint value) { gEngine.cc(ch, ctrl, value); }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativePitchBend(JNIEnv*, jclass, jint ch, jint value) { gEngine.pitchBend(ch, value); }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeSetGain(JNIEnv*, jclass, jfloat gain) { gEngine.setGain(gain); }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeSetReverb(JNIEnv*, jclass, jfloat room, jfloat damp, jfloat width, jfloat level) { gEngine.setReverb(room, damp, width, level); }
extern "C" JNIEXPORT void JNICALL Java_com_flymaccin_demonicdaw_NativeAudioEngine_nativeSetChorus(JNIEnv*, jclass, jint voices, jfloat level, jfloat speed, jfloat depth) { gEngine.setChorus(voices, level, speed, depth); }
