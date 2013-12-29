# Copyright 2009 The Android Open Source Project
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := libmincrypt 
LOCAL_SRC_FILES := libs/libmincrypt.a

include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := libminzip 
LOCAL_SRC_FILES := libs/libminzip.a

include $(PREBUILT_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := libz 
LOCAL_SRC_FILES := libs/libz.a

include $(PREBUILT_STATIC_LIBRARY)


include $(CLEAR_VARS)
LOCAL_MODULE    := edifier
LOCAL_SRC_FILES := edifier/install.c \
        edifier/edifier.c \
        edifier/verifier.c
        
LOCAL_C_INCLUDES += $(LOCAL_PATH)/edifier
LOCAL_LDLIBS := -llog -landroid
LOCAL_CFLAGS := -DINTERNAL_SHA1 -DCONFIG_CRYPTO_INTERNAL -DCONFIG_NO_T_PRF -DCONFIG_NO_TLS_PRF

LOCAL_STATIC_LIBRARIES := libedify libminzip libmincrypt libz
include $(BUILD_EXECUTABLE)