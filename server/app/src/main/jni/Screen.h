#ifndef SERVER_SCREEN_H
#define SERVER_SCREEN_H

#include <android/log.h>

#include <binder/IServiceManager.h>

#include <gui/BufferQueue.h>
#include <gui/ISurfaceComposer.h>
#include <gui/SurfaceComposerClient.h>

#include <ui/DisplayInfo.h>

#include <utils/RefBase.h>

#include <string>

namespace android {
    class Screen : public BufferQueue::ConsumerListener {
    public:
        Screen() {};
        virtual ~Screen() {};

        std::string init();

    protected:
        virtual void onFrameAvailable() {};
        virtual void onBuffersReleased() {};
    };
}


#endif //SERVER_SCREEN_H
