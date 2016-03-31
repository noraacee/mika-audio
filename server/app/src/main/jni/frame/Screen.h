#ifndef SERVER_SCREEN_H

#include <android/log.h>

#include <arpa/inet.h>

#include <gui/ISurfaceComposer.h>
#include <gui/SurfaceComposerClient.h>

#include <private/gui/ComposerService.h>

#include <turbojpeg.h>
#include <unistd.h>
#include <string>

const int SIZE_PIXEL = 4;
const int SIZE_CONVERTED_PIXEL = 2;

namespace android {
    class Screen {

    public:
        Screen() {}
        ~Screen() {}

        int initDisplay(uint32_t w, uint32_t h);
        int updateFrame(char* bitmapPtr);
    private:
        ScreenshotClient* client;
        sp<IBinder> display;
        Rect* sourceCrop;
        uint16_t r, g, b, rgb;
        uint32_t width, height, pixels;
        uint32_t index, count, stride;
        unsigned long size;
        unsigned char* bitmap;
    };
}


#endif
