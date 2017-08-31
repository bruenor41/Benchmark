#include <jni.h>
#include <cstddef>

#ifndef _Included_magiclib_dosbox_DosboxImport
#define _Included_magiclib_dosbox_DosboxImport

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jint JNICALL Java_magiclib_dosbox_DosboxImport_nativeParseDosboxConfig(JNIEnv *, jobject, jstring, jstring, jstring);

#ifdef __cplusplus
}
#endif

jobject JavaCallbackThread = NULL;
jclass JavaCallbackThreadClass = NULL;
JNIEnv * gEnv = NULL;

jmethodID JavaReportIntValue = NULL;
jmethodID JavaReportStringValue = NULL;

void reportStringValue(int section, string &property, string &value);
void reportStringValue(int section, string &property, const char* value);
void reportIntValue(int section, string &property, int value);
void androidlog(const char* message);
bool parseFile();
void parseLine(int section, string const& textLine);
void parseAutoexec(int section, string &textLine);
void clear();
void trim(string& in);
void reportDosboxValue(int section, string &property, string &value);
void reportRenderValue(int section, string &property, string &value);
void reportCPUValue(int section, string &property, string &value);
void reportMixerValue(int section, string &property, string &value);
void reportMidiValue(int section, string &property, string &value);
void reportSpeakerValue(int section, string &property, string &value);
void reportSoundBlasterValue(int section, string &property, string &value);
void reportIPXValue(int section, string &property, string &value);
void reportDOSValue(int section, string &property, string &value);
void reportGusValue(int section, string &property, string &value);
#endif
