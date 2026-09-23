#include <jni.h>
#include <string>
#include "chiaki_adapter.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_flymaccin_bookwriter_ps5_Ps5Native_nativeStatus(JNIEnv* env, jobject) {
    std::string status = std::string("FME PS5 native core loaded · ") + fme_chiaki_stage();
    return env->NewStringUTF(status.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_flymaccin_bookwriter_ps5_Ps5Native_hasChiaki(JNIEnv*, jobject) {
    return fme_chiaki_available() ? JNI_TRUE : JNI_FALSE;
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_flymaccin_bookwriter_ps5_Ps5Native_mediaPipelineReady(JNIEnv*, jobject) {
    return fme_chiaki_media_pipeline_ready() ? JNI_TRUE : JNI_FALSE;
}
