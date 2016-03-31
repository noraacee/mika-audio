#include "Screen.h"
#include <android/bitmap.h>
#include <jni.h>

extern "C" void Java_com_mikaaudio_server_module_FrameModule_destroy(JNIEnv* env, jobject object, jlong ptr) {
    android::Screen* screen = (android::Screen*) ptr;
    delete screen;
}

extern "C" jlong Java_com_mikaaudio_server_module_FrameModule_init (JNIEnv* env, jobject object, uint32_t w, uint32_t h) {
    android::Screen* screen = new android::Screen();
    int err = screen->initDisplay(w, h);

    if (err != 0)
        return -1L;

    return (long) screen;
}

extern "C" jint Java_com_mikaaudio_server_module_FrameModule_updateFrame(JNIEnv* env, jobject object, jlong ptr, jobject bitmap) {
    android::Screen* screen = (android::Screen*) ptr;
    void* bitmapPtr;
    int err = AndroidBitmap_lockPixels(env, bitmap, &bitmapPtr);
    if (err != ANDROID_BITMAP_RESULT_SUCCESS)
        return err;

    err = screen->updateFrame((char*) bitmapPtr);
    AndroidBitmap_unlockPixels(env, bitmap);

    return err;
}