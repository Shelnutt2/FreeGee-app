#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <dlfcn.h>
#include <sys/system_properties.h>

#include "edu_shell_freegee_NativeTask.h"
#include "install.h"

#include <android/log.h>

#define APPNAME "Freegee"

#define PROPERTY_KEY_MAX   32
#define PROPERTY_VALUE_MAX  92

JNIEXPORT jint JNICALL Java_edu_shell_freegee_NativeTask_action
  (JNIEnv *env, jclass class, jstring name)
{
  const char* w_buf = (*env)->GetStringUTFChars(env, name, 0);
  int result = 0;
  if(w_buf == NULL) {
    __android_log_print(ANDROID_LOG_INFO, "TESTJNI","file path recv nothing");
  }

  else {
        __android_log_print(ANDROID_LOG_INFO, "TESTJNI","String: %s", w_buf);
       result = install_package(w_buf);
  }

  (*env)->ReleaseStringUTFChars(env, name, w_buf);
  return(result);
  
}