LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := server
LOCAL_SRC_FILES := server.cpp

include $(BUILD_SHARED_LIBRARY)