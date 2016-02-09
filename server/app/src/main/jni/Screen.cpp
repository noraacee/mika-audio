#include "Screen.h"

namespace android {

    std::string Screen::init() {
        const String16 name("SurfaceFlinger");
        sp <ISurfaceComposer> composer;
        getService(name, &composer);

        sp<IBinder> display = SurfaceComposerClient::getBuiltInDisplay(
                ISurfaceComposer::eDisplayIdMain);
        DisplayInfo displayInfo;
        status_t err = SurfaceComposerClient::getDisplayInfo(display, &displayInfo);
        __android_log_print(ANDROID_LOG_VERBOSE, "mikaaudio", "getDisplayInfo: %d", err);

        sp <BufferQueue> bufferQueue = new BufferQueue();
        wp <BufferQueue::ConsumerListener> listener = static_cast<BufferQueue::ConsumerListener *>(this);
        sp <BufferQueue::ProxyConsumerListener> proxy = new BufferQueue::ProxyConsumerListener(
                listener);

        //err = bufferQueue->consumerConnect(proxy, false);
        __android_log_print(ANDROID_LOG_VERBOSE, "mikaaudio", "consumerConnect: %d", err);


        uint32_t w, h;
        err = composer->captureScreen(display, bufferQueue, w, h, 0, 0);
        __android_log_print(ANDROID_LOG_VERBOSE, "mikaaudio", "captureScreen: %d", err);

        /*
        BufferQueue::BufferItem item;
        err = bufferQueue->acquireBuffer(&item, 0);
        if (err == BufferQueue::NO_BUFFER_AVAILABLE)
            return "2";
        else if (err == NO_ERROR)
            return "3";
        else if (err == INVALID_OPERATION)
            return "4";
        else if (err == BufferQueue::PRESENT_LATER)
            return "5";
        else if (err == BufferQueue::NO_CONNECTED_API)
            return "6";*/


        return "7";
    }
}