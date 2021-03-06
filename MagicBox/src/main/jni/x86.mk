#----------------------dosbox lib-------------------------------------------

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := dosbox_main
LOCAL_C_INCLUDES := $(SHARED_SOURCES_PATH)/MagicBox/src/main/jni/include

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

$(info $(CG_SUBDIRS))

LOCAL_PATH := $(SHARED_SOURCES_PATH)/MagicBox/src/main/jni/dosbox

CG_SRCDIR := $(LOCAL_PATH)
LOCAL_CFLAGS :=	-I$(LOCAL_PATH)/include \
				$(foreach D, $(CG_SUBDIRS), -I$(CG_SRCDIR)/$(D)) \
				-I$(LOCAL_PATH)/../sdl/include \
				-I$(USER_PROJECT_PATH)/MagicBox/src/main/jni/magicbox \
				-I$(LOCAL_PATH)

LOCAL_CFLAGS += -O3

#these are very outdated use C_TARGETCPU=X86 C_DYNREC=1,they are also not pic comatible and dont work on the new ndk
#LOCAL_CFLAGS += -DC_DYNAMIC_X86=1
#LOCAL_CFLAGS += -DC_FPU_X86=1

LOCAL_CFLAGS += -DC_TARGETCPU=X86 -DC_DYNREC=1
LOCAL_CFLAGS += -DDBXLIB_X86 -DBXLIB_CORE1
LOCAL_CFLAGS += -DC_UNALIGNED_MEMORY=1

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

#these are very outdated use C_TARGETCPU=X86 C_DYNREC=1,they are also not pic comatible and dont work on the new ndk
#LOCAL_CFLAGS += -DC_DYNAMIC_X86=1
#LOCAL_CFLAGS += -DC_FPU_X86=1

LOCAL_CFLAGS += -DC_TARGETCPU=X86 -DC_DYNREC=1
LOCAL_CFLAGS += -DDBXLIB_X86 -DBXLIB_CORE1
LOCAL_CFLAGS += -DC_UNALIGNED_MEMORY=1

LOCAL_CPP_EXTENSION := .cpp
LOCAL_PATH := $(USER_PROJECT_PATH)/MagicBox/src/main/jni/magicbox
LOCAL_SRC_FILES := $(foreach F, $(CG_SUBDIRS), $(addprefix $(F)/,$(notdir $(wildcard $(LOCAL_PATH)/$(F)/*.cpp))))
LOCAL_STATIC_LIBRARIES := dosbox_main
LOCAL_LDLIBS += -llog -lz
#LOCAL_LDLIBS += -lz

include $(BUILD_SHARED_LIBRARY)
