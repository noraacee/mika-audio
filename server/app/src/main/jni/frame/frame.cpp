#include "Screen.h"
#include <android/bitmap.h>
#include <jni.h>

extern "C" void Java_com_mikaaudio_server_module_FrameModule_destroy(JNIEnv *env, jobject object, jlong ptr) {
    android::Screen* screen = (android::Screen*) ptr;
    delete screen;
}

extern "C" jlong Java_com_mikaaudio_server_module_FrameModule_init (JNIEnv *env, jobject object, jstring jip, uint32_t port) {
    char* ip;
    const char * _ip = env->GetStringUTFChars(jip, 0);
    ip = strdup(_ip);
    env->ReleaseStringUTFChars(jip, _ip);

    android::Screen* screen = new android::Screen(ip, port);
    int err = screen->initCheck();

    if (err != 0)
        return -1L;

    return (long) screen;
}

extern "C" void Java_com_mikaaudio_server_module_FrameModule_start(JNIEnv* env, jobject object, jlong ptr) {
    android::Screen* screen = (android::Screen*) ptr;
    screen->start();
}

extern "C" void Java_com_mikaaudio_server_module_FrameModule_stop(JNIEnv*, jobject object, jlong ptr) {
    android::Screen* screen = (android::Screen*) ptr;
    screen->stop();
}

extern "C" jint Java_com_mikaaudio_server_module_FrameModule_updateFrame(JNIEnv* env, jobject object, jlong ptr, jobject bitmap) {
    void* bitmapPtr;
    int err = AndroidBitmap_lockPixels(env, bitmap, &bitmapPtr);
    if (err != ANDROID_BITMAP_RESULT_SUCCESS)
        return err;


    android::Screen* screen = (android::Screen*) ptr;
    err = screen->updateFrame((char*) bitmapPtr);
    AndroidBitmap_unlockPixels(env, bitmap);

    return err;
}