#include "Screen.h"

namespace android {
    int Screen::initDisplay(uint32_t w, uint32_t h) {
        width = w;
        height = h;
        pixels = width * height * SIZE_CONVERTED_PIXEL;
        sourceCrop = new Rect();

        client = new ScreenshotClient();
        display = SurfaceComposerClient::getBuiltInDisplay(ISurfaceComposer::eDisplayIdMain);
        if (display == NULL)
            return 1;
        return 0;
    }

    int Screen::updateFrame(char *bitmapPtr) {
        status_t err = client->update(display);
        if (err != NO_ERROR)
            return err;

        stride = (client->getStride() - width) * SIZE_PIXEL;

        bitmap = (unsigned char*) client->getPixels();

        count = 0;
        for (index = 0; index < pixels; index += SIZE_CONVERTED_PIXEL) {
            if (index % (width * SIZE_CONVERTED_PIXEL) == 0 && count != 0)
                count += stride;

            r = ((bitmap[count] >> 3) & 0x001f ) << 11;
            g = ((bitmap[count + 1] >> 2) & 0x003f ) << 5;
            b = (bitmap[count + 2] >> 3) & 0x001f;
            rgb = (uint16_t) (r | g | b);

            *(bitmapPtr + index) = rgb & 0xFF;
            *(bitmapPtr + index + 1) = rgb >> 8;

            count += SIZE_PIXEL;
        }

        return NO_ERROR;
    }
}