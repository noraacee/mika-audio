#include <android/bitmap.h>
#include "Server.h"

extern "C" jlong Java_com_mikaaudio_server_util_FrameManager_init (JNIEnv *env, jobject object, int w, int h) {
    android::Screen* screen = new android::Screen(w, h);
    int err = screen->initCheck();

    if (err != 0)
        return -1L;

    return (long) screen;
}

extern "C" jint Java_com_mikaaudio_server_util_FrameManager_updateFrame(JNIEnv* env, jobject object, jlong ptr, jobject bitmap) {
    void* bitmapPtr;
    int err = AndroidBitmap_lockPixels(env, bitmap, &bitmapPtr);
    if (err != ANDROID_BITMAP_RESULT_SUCCESS)
        return err;


    android::Screen* screen = (android::Screen*) ptr;
    err = screen->updateFrame((char*) bitmapPtr);
    AndroidBitmap_unlockPixels(env, bitmap);

    return err;
}