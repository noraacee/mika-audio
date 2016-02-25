#include "Screen.h"

namespace android {
    Screen::Screen(char* ip, uint32_t port, uint32_t w, uint32_t h) {
        initSucceed = 0;
        initSocket(ip, port);
        if (initSucceed == 0)
            initDisplay(w, h);
    }

    Screen::~Screen() {

    }

    int Screen::initCheck() {
        return initSucceed;
    };

    void Screen::start() {
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "starting frame send");
        running = true;
        while(running)
            sendFrame();
    }

    void Screen::stop() {
        running = false;
    }

    void Screen::initDisplay(uint32_t w, uint32_t h) {
        composer = ComposerService::getComposerService();
        if (composer == NULL) {
            initSucceed = 1;
            return;
        }

        sp<IGraphicBufferConsumer> consumer;
        BufferQueue::createBufferQueue(&producer, &consumer);
        cpuConsumer = new CpuConsumer(consumer, 1, true);
        cpuConsumer->setName(String8(APP_NAME));
        cpuConsumer->setDefaultBufferSize(w * 4, h);
        cpuConsumer->setDefaultBufferFormat(HAL_PIXEL_FORMAT_RGB_565);
        memset(&buffer, 0, sizeof(buffer));
        haveBuffer = false;

        display = SurfaceComposerClient::getBuiltInDisplay(ISurfaceComposer::eDisplayIdMain);
        if (display == NULL) {
            initSucceed = 1;
            return;
        }

        sourceCrop = new Rect();
        width = w;
        height = h;
        pixels = width * height * SIZE_PIXEL;

        convertedBitmap = new char[pixels];
    }

    void Screen::initSocket(char* ip, uint32_t port) {
        memset((char *) &to, 0, sizeof(to));
        if (inet_pton(AF_INET, ip, &to.sin_addr) != 1) {
            initSucceed = 1;
            free((char*) ip);
            return;
        }
        free(ip);

        to.sin_family = AF_INET;
        to.sin_port = htons(port);

        if ((sock = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
            initSucceed = 1;
            return;
        }

        memset((char *) &from, 0, sizeof(from));
        from.sin_family = AF_INET;
        from.sin_addr.s_addr = htonl(INADDR_ANY);
        from.sin_port = htons(0);

        if (bind(sock, (struct sockaddr*) &from, sizeof(from)) < 0) {
            initSucceed = 1;
            return;
        }
    }

    void Screen::copyData() {
        if ((len + count) % (width * SIZE_PIXEL) == 0 && count + len != 0)
            index += stride;

        data[SIZE_FRAME_HEADER + len] = convertedBitmap[index + len];
    }

    void Screen::sendFrame() {
        if (haveBuffer) {
            cpuConsumer->unlockBuffer(buffer);
            haveBuffer = false;
        }

        status_t err = composer->captureScreen(display, producer, *sourceCrop, width, height, 0, -1UL, false, static_cast<ISurfaceComposer::Rotation>(ISurfaceComposer::eRotateNone));
        if (err == NO_ERROR) {
            cpuConsumer->lockNextBuffer(&buffer);
            if (err == NO_ERROR)
                haveBuffer = true;
        }

        if (err != NO_ERROR)
            return;

        count = 0;
        index = 0;
        stride = buffer.stride;
        bitmap = (char*) buffer.data;

        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "pixel format: %d", buffer.format);

        while (count < pixels) {
            data[0] = (count >> 24) & 0xFF;
            data[1] = (count >> 16) & 0xFF;
            data[2] = (count >> 8) & 0xFF;
            data[3] = count & 0xFF;

            if (count + SIZE_DATA > pixels) {
                for (len = 0; len < pixels - count; len++)
                    copyData();
            } else {
                for (len = 0; len < SIZE_DATA; len++)
                    copyData();
            }

            count += len;
            index += len;

            usleep(50);

            sendto(sock, data, len + SIZE_FRAME_HEADER, 0, (struct sockaddr*) &to, sizeof(to));
        }
    }

    void Screen::convertPixelFormat() {
    }
}