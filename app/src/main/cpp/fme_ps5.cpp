#include <jni.h>
#include <string>

extern "C" JNIEXPORT jstring JNICALL
Java_com_flymaccin_bookwriter_ps5_Ps5Native_nativeStatus(JNIEnv* env, jobject) {
#ifdef FME_HAS_CHIAKI
    std::string status = "FME PS5 native core loaded · Chiaki linked";
#else
    std::string status = "FME PS5 native core loaded · Chiaki source not vendored";
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
