#ifndef SERVER_SCREEN_H
#define SERVER_SCREEN_H

#include <android/log.h>

#include <gui/ISurfaceComposer.h>
#include <gui/SurfaceComposerClient.h>

#include <string>

namespace android {
    class Screen {

    public:

        Screen(uint32_t width, uint32_t height);
        ~Screen();

        int initCheck();
        int updateFrame(char* bitmapPtr);

    private:
        ScreenshotClient* client;
        sp<IBinder> display;
        Rect* sourceCrop;
        uint32_t width, height, size, stride;
        char* bitmap;
    };
}


#endif //SERVER_SCREEN_H
