#include "Screen.h"

namespace android {
    Screen::Screen(char* ip, uint32_t port) {
        initSucceed = 0;
        initSocket(ip, port);
        if (initSucceed == 0)
            initDisplay();
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

    void Screen::convertPixelFormat() {
        count = 0;
        for (index = 0; index < SIZE; index += SIZE_CONVERTED_PIXEL) {
            if (index % (WIDTH * SIZE_CONVERTED_PIXEL) == 0 && count != 0)
                count += stride;

            r = ((bitmap[count] >> 3) & 0x001f ) << 11;
            g = ((bitmap[count + 1] >> 2) & 0x003f ) << 5;
            b = (bitmap[count + 2] >> 3) & 0x001f;
            rgb = (uint16_t) (r | g | b);

            hi = rgb & 0xFF;
            lo = rgb >> 8;

            //if (convertedBitmap[index] == hi && convertedBitmap[index + 1] == lo) {
                //convertedBitmap[index + 2] = 0x01;
            //} else {
                convertedBitmap[index] = hi;
                convertedBitmap[index + 1] = lo;
                //convertedBitmap[index + 2] = 0;
            //}

            count += SIZE_PIXEL;
        }
    }

    void Screen::initDisplay() {
        client = new ScreenshotClient();
        display = SurfaceComposerClient::getBuiltInDisplay(ISurfaceComposer::eDisplayIdMain);
        if (display == NULL) {
            initSucceed = 1;
            return;
        }

        sourceCrop = new Rect();

        convertedBitmap = new char[SIZE];
        for (int i = 0; i < SIZE; i++)
            convertedBitmap[i] = 0;

        writeInt(WIDTH, 0);
        writeInt(HEIGHT, 4);

        sendto(sock, data, 8, 0, (struct sockaddr*) &to, sizeof(to));
        recvfrom(sock, data, 4, 0, 0, 0);

        bufferSize = (data[0] << 24) | (data[1] << 16) | (data[2] << 8) | (data[3]);
        __android_log_print(ANDROID_LOG_VERBOSE, APP_NAME, "buffer size: %u", bufferSize);
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

    void Screen::sendFrame() {
        status_t err = client->update(display, *sourceCrop, WIDTH, HEIGHT, false);
        if (err != NO_ERROR)
            return;

        stride = (client->getStride() - WIDTH) * 4;
        bitmap = (char*) client->getPixels();

        convertPixelFormat();

        count = 0;
        while (count < PIXELS) {
            //while (convertedBitmap[count * SIZE_CONVERTED_PIXEL + index * SIZE_CONVERTED_PIXEL + 2] == 0x01) {}
                //count++;

            //if (count > PIXELS)
                //break;

            data[1] = (count >> 16) & 0xFF;
            data[2] = (count >> 8) & 0xFF;
            data[3] = count & 0xFF;

            if (count + SIZE_DATA / SIZE_PACKET_PIXEL > PIXELS)
                len = PIXELS - count;
            else
                len = SIZE_DATA / SIZE_PACKET_PIXEL;

            for (index = 0; index < len; index++) {
                //if (convertedBitmap[count * SIZE_CONVERTED_PIXEL + index * SIZE_CONVERTED_PIXEL + 2] == 0x01)
                    //break;
                data[SIZE_FRAME_HEADER + index * SIZE_PACKET_PIXEL] = convertedBitmap[
                        count * SIZE_CONVERTED_PIXEL + index * SIZE_CONVERTED_PIXEL];
                data[SIZE_FRAME_HEADER + index * SIZE_PACKET_PIXEL + 1] = convertedBitmap[
                        count * SIZE_CONVERTED_PIXEL + index * SIZE_CONVERTED_PIXEL + 1];
            }

            count += index;

            if (count == PIXELS)
                data[0] = 0x01;
            else
                data[0] = 0x00;

            usleep(200);
            sendto(sock, data, index * SIZE_PACKET_PIXEL + SIZE_FRAME_HEADER, 0,
                   (struct sockaddr *) &to, sizeof(to));
        }
    }

    void Screen::writeInt(uint32_t value, uint32_t offset) {
        data[offset] = (value >> 24);
        data[offset + 1] = (value >> 16);
        data[offset + 2] = (value >> 8);
        data[offset + 3] = (value);
    }
}