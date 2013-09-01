/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#ifndef RECOVERY_COMMON_H
#define RECOVERY_COMMON_H

#include <stdio.h>
//#include <fs_mgr.h>

#include <android/log.h>

#define APPNAME "Freegee"


// Default allocation of progress bar segments to operations
static const int VERIFICATION_PROGRESS_TIME = 60;
static const float VERIFICATION_PROGRESS_FRACTION = 0.25;
static const float DEFAULT_FILES_PROGRESS_FRACTION = 0.4;
static const float DEFAULT_IMAGE_PROGRESS_FRACTION = 0.1;

#ifdef jni
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, APPNAME,"E:" __VA_ARGS__)
#define LOGW(...) __android_log_print(ANDROID_LOG_WARNING, APPNAME, "W:" __VA_ARGS__)
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, APPNAME, "I:" __VA_ARGS__)

#if 0
#define LOGV(...) __android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "V:" __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, APPNAME, "D:" __VA_ARGS__)
#else
#define LOGV(...) do {} while (0)
#define LOGD(...) do {} while (0)
#endif
#else
#define LOGE(...) printf("E: %s\n", __VA_ARGS__)
#define LOGW(...) fprintf(stdout, "W:" __VA_ARGS__)
#define LOGI(...) fprintf(stdout, "I:" __VA_ARGS__)

#if 0
#define LOGV(...) fprintf(stdout, "V:" __VA_ARGS__)
#define LOGD(...) fprintf(stdout, "D:" __VA_ARGS__)
#else
#define LOGV(...) do {} while (0)
#define LOGD(...) do {} while (0)
#endif
#endif

#define STRINGIFY(x) #x
#define EXPAND(x) STRINGIFY(x)

//typedef struct fstab_rec Volume;

typedef struct {
    // number of frames in indeterminate progress bar animation
    int indeterminate_frames;

    // number of frames per second to try to maintain when animating
    int update_fps;

    // number of frames in installing animation.  may be zero for a
    // static installation icon.
    int installing_frames;

    // the install icon is animated by drawing images containing the
    // changing part over the base icon.  These specify the
    // coordinates of the upper-left corner.
    int install_overlay_offset_x;
    int install_overlay_offset_y;

} UIParameters;

#endif  // RECOVERY_COMMON_H
