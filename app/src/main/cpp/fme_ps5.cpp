#include <jni.h>
#include <chiaki/base64.h>
#include <chiaki/common.h>
#include <chiaki/log.h>
#include <chiaki/regist.h>
#include <cstdio>
#include <cstring>
#include <memory>
#include <string>
#include <thread>
#include "chiaki_adapter.h"

static JavaVM *g_vm = nullptr;

struct RegistrationContext {
    ChiakiRegist regist{};
    ChiakiLog log{};
    jobject callback = nullptr;
};

static std::string hex_encode(const uint8_t *data, size_t size) {
    static const char *hex = "0123456789abcdef";
    std::string out(size * 2, '0');
    for(size_t i = 0; i < size; ++i) {
        out[i * 2] = hex[data[i] >> 4];
        out[i * 2 + 1] = hex[data[i] & 0xf];
    }
    return out;
}

static void deliver_registration(RegistrationContext *ctx, bool ok, const char *message,
                                 const std::string &regist_key, const std::string &rp_key,
                                 const std::string &rp_key_type) {
    JNIEnv *env = nullptr;
    bool attached = false;
    if(g_vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        if(g_vm->AttachCurrentThread(&env, nullptr) != JNI_OK) return;
        attached = true;
    }
    jclass cls = env->GetObjectClass(ctx->callback);
    jmethodID method = env->GetMethodID(cls, "onRegistrationResult",
        "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    if(method) {
        jstring jmsg = env->NewStringUTF(message ? message : "");
        jstring jreg = env->NewStringUTF(regist_key.c_str());
        jstring jrp = env->NewStringUTF(rp_key.c_str());
        jstring jtype = env->NewStringUTF(rp_key_type.c_str());
        env->CallVoidMethod(ctx->callback, method, ok ? JNI_TRUE : JNI_FALSE, jmsg, jreg, jrp, jtype);
        env->DeleteLocalRef(jmsg); env->DeleteLocalRef(jreg); env->DeleteLocalRef(jrp); env->DeleteLocalRef(jtype);
    }
    env->DeleteLocalRef(cls);
    if(attached) g_vm->DetachCurrentThread();
}

static void cleanup_registration(RegistrationContext *ctx) {
    chiaki_regist_fini(&ctx->regist);
    JNIEnv *env = nullptr;
    bool attached = false;
    if(g_vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        if(g_vm->AttachCurrentThread(&env, nullptr) == JNI_OK) attached = true;
    }
    if(env && ctx->callback) env->DeleteGlobalRef(ctx->callback);
    if(attached) g_vm->DetachCurrentThread();
    delete ctx;
}

static void registration_cb(ChiakiRegistEvent *event, void *user) {
    auto *ctx = static_cast<RegistrationContext *>(user);
    if(event->type == CHIAKI_REGIST_EVENT_TYPE_FINISHED_SUCCESS && event->registered_host) {
        const auto *host = event->registered_host;
        std::string regist_key = hex_encode(reinterpret_cast<const uint8_t *>(host->rp_regist_key),
                                            sizeof(host->rp_regist_key));
        std::string rp_key = hex_encode(host->rp_key, sizeof(host->rp_key));
        deliver_registration(ctx, true, "Registration complete", regist_key, rp_key,
                             std::to_string(host->rp_key_type));
    } else if(event->type == CHIAKI_REGIST_EVENT_TYPE_FINISHED_CANCELED) {
        deliver_registration(ctx, false, "Registration canceled", "", "", "");
    } else {
        deliver_registration(ctx, false, "Chiaki registration failed", "", "", "");
    }
    // chiaki_regist_fini joins the registration thread, so finish it from a
    // different thread after this callback returns.
    std::thread(cleanup_registration, ctx).detach();
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *) {
    g_vm = vm;
    return JNI_VERSION_1_6;
}

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

extern "C" JNIEXPORT jboolean JNICALL
Java_com_flymaccin_bookwriter_ps5_Ps5Native_startRegistration(
    JNIEnv *env, jobject, jstring host, jstring account_id, jint pin, jobject callback) {
    if(!host || !account_id || !callback || pin < 0 || pin > 99999999) return JNI_FALSE;

    const char *host_chars = env->GetStringUTFChars(host, nullptr);
    const char *account_chars = env->GetStringUTFChars(account_id, nullptr);
    if(!host_chars || !account_chars) return JNI_FALSE;

    uint8_t account[CHIAKI_PSN_ACCOUNT_ID_SIZE] = {};
    size_t account_size = sizeof(account);
    ChiakiErrorCode decode_err = chiaki_base64_decode(account_chars, std::strlen(account_chars),
                                                       account, &account_size);
    if(decode_err != CHIAKI_ERR_SUCCESS || account_size != sizeof(account)) {
        env->ReleaseStringUTFChars(host, host_chars);
        env->ReleaseStringUTFChars(account_id, account_chars);
        return JNI_FALSE;
    }

    auto *ctx = new RegistrationContext();
    ctx->callback = env->NewGlobalRef(callback);
    chiaki_log_init(&ctx->log, CHIAKI_LOG_INFO | CHIAKI_LOG_WARNING | CHIAKI_LOG_ERROR,
                    chiaki_log_cb_print, nullptr);

    ChiakiRegistInfo info{};
    info.target = CHIAKI_TARGET_PS5_1;
    info.host = host_chars;
    info.broadcast = false;
    std::memcpy(info.psn_account_id, account, sizeof(account));
    info.pin = static_cast<uint32_t>(pin);
    info.console_pin = 0;
    info.holepunch_info = nullptr;

    ChiakiErrorCode init_err = chiaki_lib_init();
    ChiakiErrorCode start_err = init_err == CHIAKI_ERR_SUCCESS
        ? chiaki_regist_start(&ctx->regist, &ctx->log, &info, registration_cb, ctx)
        : init_err;

    env->ReleaseStringUTFChars(host, host_chars);
    env->ReleaseStringUTFChars(account_id, account_chars);

    if(start_err != CHIAKI_ERR_SUCCESS) {
        env->DeleteGlobalRef(ctx->callback);
        delete ctx;
        return JNI_FALSE;
    }
    return JNI_TRUE;
}
