LOCAL_PATH := $(call my-dir)

# Build frame module library
include $(CLEAR_VARS)
LOCAL_MODULE := mikaframe
LOCAL_SRC_FILES := frame/Frame.cpp frame/Screen.cpp
LOCAL_C_INCLUDES := include
LOCAL_LDFLAGS := -Llibs
LOCAL_LDLIBS := -lbinder -lcutils -lgui -lui -lutils -llog -ljnigraphics
include $(BUILD_SHARED_LIBRARY)