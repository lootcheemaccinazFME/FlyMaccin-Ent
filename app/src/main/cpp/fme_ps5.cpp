#include <jni.h>
#include <string>
#include "chiaki_adapter.h"

extern "C" JNIEXPORT jstring JNICALL
Java_com_flymaccin_bookwriter_ps5_Ps5Native_nativeStatus(JNIEnv* env, jobject) {
#ifdef FME_HAS_CHIAKI
    std::string status = std::string("FME PS5 native core loaded · ") + fme_chiaki_stage();
#else
    std::string status = std::string("FME PS5 native core loaded · ") + fme_chiaki_stage();
#endif
    return env->NewStringUTF(status.c_str());
}

extern "C" JNIEXPORT jboolean JNICALL
Java_com_flymaccin_bookwriter_ps5_Ps5Native_hasChiaki(JNIEnv*, jobject) {
#ifdef FME_HAS_CHIAKI
    return JNI_TRUE;
#else
    return JNI_FALSE;
#endif
}
