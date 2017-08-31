# The ARMv7 is significanly faster due to the use of the hardware FPU
#NDK_TOOLCHAIN_VERSION=clang
NDK_TOOLCHAIN_VERSION=4.9
#APP_ABI := armeabi armeabi-v7a x86
APP_ABI := armeabi-v7a
APP_OPTIM := release
APP_STL := stlport_static
APP_PLATFORM := android-8
LOCAL_LDLIBS += -lz