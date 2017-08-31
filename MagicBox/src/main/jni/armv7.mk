include $(CLEAR_VARS)

LOCAL_MODULE := dosbox_main
LOCAL_C_INCLUDES := $(SHARED_SOURCES_PATH)/MagicBox/src/main/jni/include

$(info $(LOCAL_PATH))

CG_SUBDIRS := \
src/dos \
src/hardware \
src/hardware/serialport \
src \
src/cpu \
src/cpu/core_dynrec \
src/cpu/core_dyn_x86 \
src/cpu/core_full \
src/cpu/core_normal \
src/fpu \
src/gui \
src/gui/gui_tk \
src/gui/zmbv \
src/ints \
src/libs \
src/misc \
src/shell \
../shared/thread \
../shared/thread/pthread \
../shared/libogg \
../shared/libvorbis \
../shared/sdlsound \
../shared/sdlsound/decoders \
../shared/sdlsound/decoders/timidity \
../shared/sdlsound/decoders/mpglib \
../shared/mt32emu/src \
../shared/mt32emu/src/sha1 \
../shared/fluidsynth \
../shared/fluidsynth/include \

$(info $(CG_SUBDIRS))

LOCAL_PATH := $(SHARED_SOURCES_PATH)/MagicBox/src/main/jni/dosbox

CG_SRCDIR := $(LOCAL_PATH)
LOCAL_CFLAGS :=	-I$(LOCAL_PATH)/include \
				$(foreach D, $(CG_SUBDIRS), -I$(CG_SRCDIR)/$(D)) \
				-I$(LOCAL_PATH)/../sdl/include \
				-I$(USER_PROJECT_PATH)/MagicBox/src/main/jni/magicbox \
				-I$(LOCAL_PATH)

LOCAL_CFLAGS += -O3
LOCAL_CFLAGS += -DC_TARGETCPU=ARMV7LE
LOCAL_CFLAGS += -DC_DYNREC=1
LOCAL_CFLAGS += -DC_UNALIGNED_MEMORY=1
LOCAL_CFLAGS += -DDBXLIB_ARMEABI_V7
LOCAL_ARM_MODE := arm

$(info $(LOCAL_CFLAGS))

LOCAL_CPP_EXTENSION := .cpp
LOCAL_SRC_FILES := $(foreach F, $(CG_SUBDIRS), $(addprefix $(F)/,$(notdir $(wildcard $(LOCAL_PATH)/$(F)/*.cpp))))
LOCAL_SRC_FILES += $(foreach F, $(CG_SUBDIRS), $(addprefix $(F)/,$(notdir $(wildcard $(LOCAL_PATH)/$(F)/*.c))))

include $(BUILD_STATIC_LIBRARY)
#-----------------------------------------------------------------------------

include $(CLEAR_VARS)

LOCAL_MODULE := dosbox
LOCAL_C_INCLUDES := $(SHARED_SOURCES_PATH)/MagicBox/src/main/jni/include

$(info $(LOCAL_PATH))

CG_SUBDIRS := .\

LOCAL_PATH := $(SHARED_SOURCES_PATH)/MagicBox/src/main/jni/dosbox

CG_SRCDIR := $(LOCAL_PATH)
LOCAL_CFLAGS :=	-I$(LOCAL_PATH)/include \
				$(foreach D, $(CG_SUBDIRS), -I$(CG_SRCDIR)/$(D)) \
				-I$(LOCAL_PATH)/../sdl/include \
				-I$(LOCAL_PATH)/src \
				-I$(LOCAL_PATH) \
				-I$(USER_PROJECT_PATH)/MagicBox/src/main/jni/magicbox \

LOCAL_CFLAGS += -O3
LOCAL_CFLAGS += -DC_TARGETCPU=ARMV7LE
LOCAL_CFLAGS += -DC_DYNREC=1
LOCAL_CFLAGS += -DC_UNALIGNED_MEMORY=1
LOCAL_CFLAGS += -DDBXLIB_ARMEABI_V7

LOCAL_ARM_MODE := arm

$(info $(LOCAL_CFLAGS))

LOCAL_CPP_EXTENSION := .cpp
LOCAL_PATH := $(USER_PROJECT_PATH)/MagicBox/src/main/jni/magicbox
LOCAL_SRC_FILES := $(foreach F, $(CG_SUBDIRS), $(addprefix $(F)/,$(notdir $(wildcard $(LOCAL_PATH)/$(F)/*.cpp))))

LOCAL_STATIC_LIBRARIES := dosbox_main
LOCAL_LDLIBS += -llog -lz
#LOCAL_LDLIBS += -lz

include $(BUILD_SHARED_LIBRARY)