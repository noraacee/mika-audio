LOCAL_PATH := $(call my-dir)

# Build frame module library
include $(CLEAR_VARS)
LOCAL_MODULE := mikaframe
LOCAL_SRC_FILES := frame/Frame.cpp
LOCAL_LDFLAGS := -Llibs
include $(BUILD_SHARED_LIBRARY)