#include <jni.h>
#if (LIB_ARMEABI_V7)
#include <cpu-features.h>
#endif
#include <string.h>
//#include <sys/stat.h>

//#include <android/log.h>
#include <limits.h>
#include <stdlib.h>
//#define  LOG(...)  __android_log_print(ANDROID_LOG_INFO,"MagicBox",__VA_ARGS__)

extern "C" jint Java_magiclib_core_NativeCore_nativeGetArchitecture(JNIEnv * env, jobject obj)
{
	#if (LIB_ARMEABI)
		return 0;
	#else
		#if (LIB_ARMEABI_V7)
			return 1;
		#else
			#if (LIB_X86)
				return 2;
			#else
			    return -1;
			#endif
		#endif
	#endif
}

extern "C" jint Java_magiclib_core_NativeCore_nativeHasNEON(JNIEnv *env, jobject obj)
{
#if (LIB_ARMEABI_V7)
	uint64_t features = android_getCpuFeatures();
	//LOGD(LOG_TAG, "NEON CHECK %d",features);
	if (android_getCpuFamily() != ANDROID_CPU_FAMILY_ARM) {
		return 0;
	}
	if ((features & ANDROID_CPU_ARM_FEATURE_NEON) == 0) {
		return 0;
	} else {
		// valid signature
		return 1;
	}
#endif
	return 0;
}

extern "C" jstring Java_magiclib_core_NativeCore_nativeGetRealPath(JNIEnv *env, jobject obj, jstring path)
{

	//struct stat fileStat;
	//stat("/storage/sdcard1", &fileStat);

	//LOG("fileStat %d", fileStat.time_t);

	char buf[PATH_MAX+1];
	const char *convertedPath = env->GetStringUTFChars(path, JNI_FALSE);
	char *res = realpath(convertedPath, buf);
	//LOG("fileStat/mnt/sdcard2 : %s", res);
	env->ReleaseStringUTFChars(path, convertedPath);
	return env->NewStringUTF(res);
}

/*
jboolean Java_magiclib_core_NativeCore_nativeIsARMv7(JNIEnv *env, jobject obj)
{
	uint64_t features = android_getCpuFeatures();
	//LOGD(LOG_TAG, "ARM7 CHECK %d",features);
	if (android_getCpuFamily() != ANDROID_CPU_FAMILY_ARM) {
		return JNI_FALSE;
	}
	if ((features & ANDROID_CPU_ARM_FEATURE_ARMv7) == 0) {
		return JNI_FALSE;
	} else {
		// valid signature
		return JNI_TRUE;
	}
}

jint Java_magiclib_core_NativeCore_nativeGetCPUFamily(JNIEnv *env, jobject obj)
{
	AndroidCpuFamily family = android_getCpuFamily();
	if (family == ANDROID_CPU_FAMILY_X86)
		return 2;
	if (family == ANDROID_CPU_FAMILY_ARM)
		return 1;
	if (family == ANDROID_CPU_FAMILY_MIPS)
		return 3;
	if (family == ANDROID_CPU_FAMILY_UNKNOWN)
		return 0;
}*/