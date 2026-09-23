#include <jni.h>
#include <string>
extern "C" JNIEXPORT jstring JNICALL
Java_com_flymaccin_bookwriter_ps5_Ps5Native_nativeStatus(JNIEnv* env, jobject) {
    std::string status = "FME PS5 native core loaded";
    return env->NewStringUTF(status.c_str());
}
