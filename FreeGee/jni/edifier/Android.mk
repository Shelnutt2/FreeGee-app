# Copyright 2009 The Android Open Source Project
LOCAL_PATH := $(call my-dir)

updater_src_files := \
        install.c \
        sha1.c \
        verifier.c \

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(updater_src_files)

LOCAL_C_INCLUDES += $(dir $(inc))

LOCAL_CFLAGS := -DINTERNAL_SHA1 -DCONFIG_CRYPTO_INTERNAL -DCONFIG_NO_T_PRF -DCONFIG_NO_TLS_PRF

LOCAL_STATIC_LIBRARIES := libedify libminzip libmincrypt libz libselinux

LOCAL_MODULE := libedifier

include $(BUILD_STATIC_LIBRARY)

include $(call all-subdir-makefiles)
