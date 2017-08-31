in local.properties fix path to sdk
in jni library magiclib in android.mk fix path LOCAL_PATH
in build.gradle in magiclib fix paths to ndk
in build.gradle in magicbox fix paths to ndk

in jni for magicbox in application.mk switch between clang and gcc
#NDK_TOOLCHAIN_VERSION=4.9
----

application extracts and starts doom time demo for testing system performance. At the end you can see :
timed 2134 gameticks in 2302 realticks

compare realticks compiled with gcc and with clang. Lower number means better performance

Close with back button on device. Tested armeabi-v7a