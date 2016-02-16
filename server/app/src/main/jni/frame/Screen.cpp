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

    void Screen::initDisplay(uint32_t w, uint32_t h) {
        client = new ScreenshotClient();
        display = SurfaceComposerClient::getBuiltInDisplay(ISurfaceComposer::eDisplayIdMain);
        sourceCrop = new Rect();
        width = w;
        height = h;
        pixels = width * height * 4;
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
        to.sin_port = htonl(port);

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

    void Screen::sendFrame() {
        status_t err = client->update(display, *sourceCrop, width, height, false);
        if (err != NO_ERROR)
            return;

        count = 0;
        acount = 0;
        stride = client->getStride();
        bitmap = (char*) client->getPixels();

        while (count < pixels) {
            data[0] = (count >> 24) & 0xFF;
            data[1] = (count >> 16) & 0xFF;
            data[2] = (count >> 8) & 0xFF;
            data[3] = count & 0xFF;

            if (count + SIZE_DATA > pixels) {
                for (len = 0; len < pixels - count; len++) {
                    if ((len + acount) % width == 0)
                        acount += stride;

                    data[SIZE_FRAME_HEADER + len] = bitmap[acount + len];
                }
            } else {
                for (len = 0; len < SIZE_DATA; len++) {
                    if ((len + acount) % width == 0)
                        acount += stride;

                    data[SIZE_FRAME_HEADER + len] = bitmap[acount + len];
                }
            }

            count += len;

            sendto(sock, data, len + SIZE_FRAME_HEADER, 0, (struct sockaddr*) &to, sizeof(to));
        }
    }

    void Screen::start() {
        while(running)
            sendFrame();
    }

    void Screen::stop() {
        running = false;
    }
}