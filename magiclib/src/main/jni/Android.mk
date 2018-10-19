include $(CLEAR_VARS)

LOCAL_PATH := d:\Projects\AndroidStudio\DoomBenchmark\Benchmark\magiclib\src\main\jni\magiclib
LOCAL_MODULE    := magiclib
CG_SUBDIRS := \

ifeq ($(TARGET_ARCH_ABI),armeabi)
	LOCAL_CFLAGS = -DLIB_ARMEABI
endif

ifeq ($(TARGET_ARCH_ABI),x86)
	LOCAL_CFLAGS = -DLIB_X86
endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
	LOCAL_CFLAGS = -DLIB_ARMEABI_V7
endif

LOCAL_CFLAGS += -O3

$(info $(LOCAL_CFLAGS))

LOCAL_CPP_EXTENSION := .cpp
LOCAL_SRC_FILES := android.cpp dosbox_import.cpp
LOCAL_STATIC_LIBRARIES := cpufeatures
#LOCAL_LDLIBS += -llog

include $(BUILD_SHARED_LIBRARY)

$(call import-module,android/cpufeatures)