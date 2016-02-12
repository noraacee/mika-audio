#include "Screen.h"

namespace android {
    Screen::Screen(uint32_t w, uint32_t h) {
        client = new ScreenshotClient();
        display = SurfaceComposerClient::getBuiltInDisplay(ISurfaceComposer::eDisplayIdMain);
        sourceCrop = new Rect();
        width = w;
        height = h;
    }

    int Screen::initCheck() {
        if (display == NULL)
            return 1;

        return 0;
    };

    int Screen::updateFrame(char* bitmapPtr) {
        status_t err = client->update(display, *sourceCrop, width, height, false);
        if (err != NO_ERROR)
            return err;

        stride = client->getStride();
        bitmap = (char*) client->getPixels();

        int h, w;
        for (h = 0; h < height; h++) {
            for (w = 0; w < width; w++) {
                *(bitmapPtr + h * (width * 4) + w * 4) = *(bitmap + h * (width * 4 + stride) + w * 4);
                *(bitmapPtr + h * (width * 4) + w * 4 + 1) = *(bitmap + h * (width * 4 + stride) + w * 4 + 1);
                *(bitmapPtr + h * (width * 4) + w * 4 + 2) = *(bitmap + h * (width * 4 + stride) + w * 4 + 2);
                *(bitmapPtr + h * (width * 4) + w * 4 + 3) = *(bitmap + h * (width * 4 + stride) + w * 4 + 3);
            }
        }

        return NO_ERROR;
    }
}