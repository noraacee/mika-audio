#ifndef SERVER_SCREEN_H
#define SERVER_SCREEN_H

#include <android/log.h>

#include <arpa/inet.h>

#include <gui/ISurfaceComposer.h>
#include <gui/SurfaceComposerClient.h>

#include <string>

const int SIZE_PACKET = 1500;
const int SIZE_IP_HEADER = 20;
const int SIZE_UDP_HEADER = 8;
const int SIZE_FRAME_HEADER = 4;
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

        ScreenshotClient* client;
        sp<IBinder> display;
        Rect* sourceCrop;
        uint32_t width, height, pixels, count, acount, len, stride;
        char* bitmap;
        char* data;

        void initDisplay(uint32_t w, uint32_t h);
        void initSocket(char* ip, uint32_t port);
        void sendFrame();
    };
}


#endif //SERVER_SCREEN_H
