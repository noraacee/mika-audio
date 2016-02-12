LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := server
LOCAL_SRC_FILES := Server.cpp Screen.cpp
LOCAL_C_INCLUDES := include
LOCAL_LDFLAGS := -Llibs
LOCAL_LDLIBS := -lbinder -lcutils -lgui -lui -lutils -llog -ljnigraphics

include $(BUILD_SHARED_LIBRARY)