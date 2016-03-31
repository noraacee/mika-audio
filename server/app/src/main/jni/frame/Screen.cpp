#include "Screen.h"

namespace android {
    Screen::Screen(char* ip, uint32_t port, uint32_t w, uint32_t h) {
        width = w;
        height = h;
        pixels = width * height * SIZE_CONVERTED_PIXEL;

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

    unsigned char* Screen::update() {
        status_t err = client->update(display);
        if (err != NO_ERROR)
            return convertedBitmap;

        stride = (client->getStride()) - width * 4;
        bitmap = (unsigned char*) client->getPixels();

        count = 0;
        for (index = 0; index < pixels; index += SIZE_CONVERTED_PIXEL) {
            if (index % (width * SIZE_CONVERTED_PIXEL) == 0 && count != 0)
                count += stride;

            r = ((bitmap[count] >> 3) & 0x001f ) << 11;
            g = ((bitmap[count + 1] >> 2) & 0x003f ) << 5;
            b = (bitmap[count + 2] >> 3) & 0x001f;
            rgb = (uint16_t) (r | g | b);

            *(convertedBitmap + index * SIZE_CONVERTED_PIXEL) = rgb & 0xFF;
            *(convertedBitmap + index * SIZE_CONVERTED_PIXEL + 1) = rgb >> 8;

            count += SIZE_PIXEL;
        }

        return convertedBitmap;
    }

    int Screen::updateFrame(char *bitmapPtr) {
        status_t err = client->update(display);
        if (err != NO_ERROR)
            return err;

        stride = (client->getStride() - width) * 4;
        bitmap = (unsigned char*) client->getPixels();

        count = 0;
        for (index = 0; index < pixels; index += SIZE_CONVERTED_PIXEL) {
            if (index % (width * SIZE_CONVERTED_PIXEL) == 0 && count != 0)
                count += stride;

            r = ((bitmap[count] >> 3) & 0x001f ) << 11;
            g = ((bitmap[count + 1] >> 2) & 0x003f ) << 5;
            b = (bitmap[count + 2] >> 3) & 0x001f;
            rgb = (uint16_t) (r | g | b);

            *bitmapPtr = rgb & 0xFF;
            *(bitmapPtr + 1) = rgb >> 8;

            count += SIZE_PIXEL;
            bitmapPtr += SIZE_CONVERTED_PIXEL;
        }

        return NO_ERROR;
    }

    void Screen::initDisplay() {
        client = new ScreenshotClient();
        display = SurfaceComposerClient::getBuiltInDisplay(ISurfaceComposer::eDisplayIdMain);
        if (display == NULL) {
            initSucceed = 1;
            return;
        }

        sourceCrop = new Rect();

        convertedBitmap = new unsigned char[pixels];

        writeInt(width, 0);
        writeInt(height, 4);

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
        status_t err = client->update(display);
        if (err != NO_ERROR)
            return;

        stride = (client->getStride() - width) * 4;
        bitmap = (unsigned char*) client->getPixels();

        compressor = tjInitCompress();
        tjCompress2(compressor, bitmap, width, stride, height, TJPF_RGBA, &convertedBitmap, &size, TJSAMP_444, JPEG_QUALITY, TJFLAG_FASTDCT);

        count = 0;
        while (count < size) {
            data[1] = (count >> 16) & 0xFF;
            data[2] = (count >> 8) & 0xFF;
            data[3] = count & 0xFF;

            if (count + SIZE_DATA > size)
                len = size - count;
            else
                len = SIZE_DATA;

            for (index = 0; index < len; index++)
                data[SIZE_FRAME_HEADER + index] = convertedBitmap[
                        count + index];

            count += index;

            if (count == size)
                data[0] = 0x01;
            else
                data[0] = 0x00;

            sendto(sock, data, index + SIZE_FRAME_HEADER, 0, (struct sockaddr *) &to, sizeof(to));
        }

        tjDestroy(compressor);
    }

    void Screen::writeInt(uint32_t value, uint32_t offset) {
        data[offset] = (value >> 24);
        data[offset + 1] = (value >> 16);
        data[offset + 2] = (value >> 8);
        data[offset + 3] = (value);
    }
}