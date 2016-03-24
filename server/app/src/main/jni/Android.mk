LOCAL_PATH := $(call my-dir)

# Build frame module library
include $(CLEAR_VARS)
include $(call all-subdir-makefiles)
LOCAL_MODULE := mikaframe
LOCAL_SRC_FILES := frame/Frame.cpp frame/Screen.cpp
LOCAL_C_INCLUDES := include libjpeg-turbo/libjpeg-turbo-1.4.1
LOCAL_LDFLAGS := -Llibs
LOCAL_LDLIBS := -lbinder -lcutils -lgui -lui -lutils -llog -ljnigraphics
LOCAL_STATIC_LIBRARIES += libjpeg-turbo
include $(BUILD_SHARED_LIBRARY)