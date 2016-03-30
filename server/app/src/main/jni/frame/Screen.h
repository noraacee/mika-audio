#ifndef SERVER_SCREEN_H

#define APP_NAME "mikaaudio"

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
const int SIZE_PACKET_PIXEL = 2;

const int SIZE_PACKET = 1500;
const int SIZE_IP_HEADER = 20;
const int SIZE_UDP_HEADER = 8;
const int SIZE_FRAME_HEADER = 4;

const int SIZE_DATA = SIZE_PACKET - SIZE_IP_HEADER - SIZE_UDP_HEADER - SIZE_FRAME_HEADER;

const int JPEG_QUALITY = 75;


namespace android {
    class Screen {

    public:
        Screen(char* ip, uint32_t port, uint32_t w, uint32_t h);
        ~Screen();

        int initCheck();
        void start();
        void stop();
        int updateFrame(char* bitmapPtr);

    private:
        int initSucceed;
        volatile bool running;

        int sock;
        struct sockaddr_in from;
        struct sockaddr_in to;


        ScreenshotClient* client;
        sp<IBinder> display;
        tjhandle compressor;
        Rect* sourceCrop;
        uint16_t r, g, b, rgb;
        uint32_t width, height, pixels;
        uint32_t count, index, len, stride, bufferSize;
        unsigned long size;
        unsigned char* bitmap;
        unsigned char* convertedBitmap;
        unsigned char data[SIZE_FRAME_HEADER + SIZE_DATA];

        void initDisplay();
        void initSocket(char* ip, uint32_t port);
        void sendFrame();
        void writeInt(uint32_t value, uint32_t offset);
    };
}


#endif //SERVER_SCREEN_H
