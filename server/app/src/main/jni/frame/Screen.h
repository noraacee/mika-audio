#ifndef SERVER_SCREEN_H
#define SERVER_SCREEN_H

#define APP_NAME "mikaaudio"

#include <android/log.h>

#include <arpa/inet.h>

#include <gui/ISurfaceComposer.h>
#include <gui/SurfaceComposerClient.h>

#include <private/gui/ComposerService.h>

#include <unistd.h>
#include <string>

const int SIZE_PIXEL = 4;
const int SIZE_PACKET = 1500;
const int SIZE_IP_HEADER = 20;
const int SIZE_UDP_HEADER = 8;
const int SIZE_FRAME_HEADER = 2;
const int SIZE_DATA = SIZE_PACKET - SIZE_IP_HEADER - SIZE_UDP_HEADER - SIZE_FRAME_HEADER;

namespace android {
    class Screen {

    public:
        Screen(char* ip, uint32_t port, uint32_t w, uint32_t h);
        ~Screen();

        int initCheck();
        void start();
        void stop();

    private:
        int initSucceed;
        volatile bool running;

        int sock;
        struct sockaddr_in from;
        struct sockaddr_in to;

        sp<ISurfaceComposer> composer;
        mutable sp<CpuConsumer> cpuConsumer;
        mutable sp<IGraphicBufferProducer> producer;
        CpuConsumer::LockedBuffer buffer;
        bool haveBuffer;

        sp<IBinder> display;
        Rect* sourceCrop;
        uint32_t width, height, pixels, count, index, len, stride;
        char* bitmap;
        char* convertedBitmap;
        char data[SIZE_FRAME_HEADER + SIZE_DATA];

        void initDisplay(uint32_t w, uint32_t h);
        void initSocket(char* ip, uint32_t port);
        void copyData();
        void sendFrame();
        void convertPixelFormat();
    };
}


#endif //SERVER_SCREEN_H
