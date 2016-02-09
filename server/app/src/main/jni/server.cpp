#include "Server.h"

extern "C" jstring Java_com_mikaaudio_server_activity_ServerActivity_init
        (JNIEnv *env, jobject thiz) {
    android::sp<android::Screen> screen;
    return env->NewStringUTF(screen->init().c_str());
}