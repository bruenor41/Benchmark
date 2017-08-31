JNI_DIR := $(call my-dir)

USER_PROJECT_PATH := $(call my-dir)/../../../..
SHARED_SOURCES_PATH := $(call my-dir)/../../../../../Benchmark

$(info "	JNI_DIR" + $(JNI_DIR))
$(info "	USER_PROJECT_PATH" + $(USER_PROJECT_PATH))
$(info "	SHARED_SOURCES_PATH" + $(SHARED_SOURCES_PATH))

ifeq ($(TARGET_ARCH_ABI),armeabi)
    include $(JNI_DIR)/armeabi.mk
endif

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    include $(JNI_DIR)/armv7.mk
endif

ifeq ($(TARGET_ARCH_ABI),x86)
    include $(JNI_DIR)/x86.mk
endif