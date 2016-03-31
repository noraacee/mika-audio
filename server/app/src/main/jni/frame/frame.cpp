#include "Screen.h"
#include <android/bitmap.h>
#include <jni.h>

extern "C" void Java_com_mikaaudio_server_module_FrameModule_destroy(JNIEnv* env, jobject object, jlong ptr) {
    android::Screen* screen = (android::Screen*) ptr;
    delete screen;
}

extern "C" jlong Java_com_mikaaudio_server_module_FrameModule_init (JNIEnv* env, jobject object, jstring jip, uint32_t port, uint32_t w, uint32_t h) {
    char* ip;
    const char * _ip = env->GetStringUTFChars(jip, 0);
    ip = strdup(_ip);
    env->ReleaseStringUTFChars(jip, _ip);

    android::Screen* screen = new android::Screen(ip, port, w, h);
    int err = screen->initCheck();

    if (err != 0)
        return -1L;

    return (long) screen;
}

extern "C" void Java_com_mikaaudio_server_module_FrameModule_start(JNIEnv* env, jobject object, jlong ptr) {
    android::Screen* screen = (android::Screen*) ptr;
    screen->start();
}

extern "C" void Java_com_mikaaudio_server_module_FrameModule_stop(JNIEnv* env, jobject object, jlong ptr) {
    android::Screen* screen = (android::Screen*) ptr;
    screen->stop();
}

extern "C" jbyteArray Java_com_mikaaudio_server_module_FrameModule_update(JNIEnv* env, jobject object, jlong ptr) {
    android::Screen* screen = (android::Screen*) ptr;
    jbyteArray bitmap = env->NewByteArray(screen->getSize());
    env->SetByteArrayRegion(bitmap, 0, screen->getSize(), (jbyte*) screen->update());

    return bitmap;
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