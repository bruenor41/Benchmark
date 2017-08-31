#include <jni.h>
//#include <android/log.h>
#include <iostream>
#include <string>
#include <fstream>
#include <vector>
#include <sstream>

using namespace std;

#include "dosbox_import.h"

const char * configDir;
const char* configFile;
const char * configCmdLine;

extern "C" jint Java_magiclib_dosbox_DosboxImport_nativeParseDosboxConfig(JNIEnv * env, jobject obj, jstring dir, jstring file, jstring commandLine)
{
	gEnv = env;
	configDir = env->GetStringUTFChars(dir, JNI_FALSE);
	configFile = env->GetStringUTFChars(file, JNI_FALSE);
	configCmdLine = env->GetStringUTFChars(commandLine, JNI_FALSE);

	JavaCallbackThread = env->NewGlobalRef(obj);
	JavaCallbackThreadClass = env->GetObjectClass(JavaCallbackThread);
	JavaReportIntValue = env->GetMethodID(JavaCallbackThreadClass, "callbackReportIntValue", "(ILjava/lang/String;I)V");
	JavaReportStringValue = env->GetMethodID(JavaCallbackThreadClass, "callbackReportStringValue", "(ILjava/lang/String;Ljava/lang/String;)V");

	if (!parseFile()) {
		androidlog("failed parse file");
	}

	clear();
	return 1;
}

void androidlog(const char* message) {
//	__android_log_print(ANDROID_LOG_INFO, "MagicBox", "nativeParseDosboxConfig - %s", message);
}

void reportIntValue(int section, string &property, int value) {
	jstring jproperty = gEnv->NewStringUTF(property.c_str());

	gEnv->CallVoidMethod( JavaCallbackThread, JavaReportIntValue, section, jproperty, value);

	gEnv->DeleteLocalRef(jproperty);
}

void reportStringValue(int section, string &property, string &value) {
	reportStringValue(section, property, value.c_str());
}

void reportStringValue(int section, string &property, const char* value) {
	jstring jproperty = gEnv->NewStringUTF(property.c_str());
	jstring jvalue = gEnv->NewStringUTF(value);

	gEnv->CallVoidMethod( JavaCallbackThread, JavaReportStringValue, section, jproperty, jvalue);

	gEnv->DeleteLocalRef(jproperty);
	gEnv->DeleteLocalRef(jvalue);
}

void clear() {
	if (gEnv != 0) {
		gEnv = NULL;
		JavaReportIntValue = NULL;
		JavaReportStringValue = NULL;

		JNIEnv *env = gEnv;
		gEnv = NULL;

		//crashes here..why?
		//env->DeleteGlobalRef(JavaCallbackThread);
		JavaCallbackThread = NULL;

		configDir = NULL;
		configFile = NULL;
		configCmdLine = NULL;
	}
}

void trim(string& in) {
	string::size_type loc = in.find_first_not_of(" \r\t\f\n");
	if(loc != string::npos) in.erase(0,loc);
	loc = in.find_last_not_of(" \r\t\f\n");
	if(loc != string::npos) in.erase(loc+1);
}

void parseLine(int section, string const& textLine) {
	string str1 = textLine;
	string::size_type loc = str1.find('=');

	if(loc == string::npos)
		return;

	string name = str1.substr(0,loc);
	string val = str1.substr(loc + 1);

	/* Remove quotes around value */
	trim(val);
	string::size_type length = val.length();
	if (length > 1 &&
		((val[0] == '"'  && val[length - 1] == '"' ) ||
		 (val[0] == '\'' && val[length - 1] == '\''))
			) val = val.substr(1,length - 2);
	/* trim the results incase there were spaces somewhere */
	trim(name);trim(val);

	switch (section) {
		case 0:{
			reportDosboxValue(section, name, val);
			break;
		}
		case 1:{
			reportRenderValue(section, name, val);
			break;
		}
		case 2:{
			reportCPUValue(section, name, val);
			break;
		}
		case 3:{
			reportMixerValue(section, name, val);
			break;
		}
		case 4:{
			reportMidiValue(section, name, val);
			break;
		}
		case 5:{
			reportSpeakerValue(section, name, val);
			break;
		}
		case 6:{
			reportSoundBlasterValue(section, name, val);
			break;
		}
		case 7:{
			reportIPXValue(section, name, val);
			break;
		}
		case 8:{
			reportDOSValue(section, name, val);
			break;
		}
		case 9:{
			reportGusValue(section, name, val);
			break;
		}
	}
}

bool parseFile() {
	androidlog(configFile);

	ifstream in(configFile);
	if (!in)
		return false;

	string textLine;

	int section = -1;

	while (getline(in,textLine))
	{
		trim(textLine);
		if(!textLine.size()) continue;

		switch(textLine[0]){
			case '%':
			case '\0':
			case '#':
			case ' ':
			case '\n':
				continue;
				break;
			case '[':
			{
				string::size_type loc = textLine.find(']');
				if(loc == string::npos) continue;

				textLine.erase(loc);

				string sectionCode = textLine.substr(1);

				if (!sectionCode.compare("dosbox")) {
					section = 0;
				} else if (!sectionCode.compare("render")) {
					section = 1;
				} else if (!sectionCode.compare("cpu")) {
					section = 2;
				} else if (!sectionCode.compare("mixer")) {
					section = 3;
				} else if (!sectionCode.compare("midi")) {
					section = 4;
				} else if (!sectionCode.compare("speaker")) {
					section = 5;
				} else if (!sectionCode.compare("sblaster")) {
					section = 6;
				} else if (!sectionCode.compare("ipx")) {
					section = 7;
				} else if (!sectionCode.compare("dos")) {
					section = 8;
				} else if (!sectionCode.compare("gus")) {
					section = 9;
				} else if (!sectionCode.compare("autoexec")) {
					section = 10;
				}

				break;
			}
			default: {
				if (section == 10) {
					parseAutoexec(section, textLine);
				} else {
					parseLine(section, textLine);
				}
			}
		}
	}

	return true;
}

void reportDosboxValue(int section, string &property, string &value) {
	if (!property.compare("machine")) {
		if (!value.compare("cga") || !value.compare("tandy") || !value.compare("pcjr") || !value.compare("hercules") ||
			!value.compare("ega") || !value.compare("svga_s3") || !value.compare("vesa_nolfb") || !value.compare("vesa_oldvbe") ||
			!value.compare("svga_et4000") || !value.compare("svga_et3000") || !value.compare("svga_paradise")) {
			reportStringValue(section, property, value);
			return;
		}

		reportStringValue(section, property, "svga_s3");
		return;
	}

	if (!property.compare("memsize")) {
		int memsize = atoi(value.c_str());

		if (memsize == 0) {
			memsize = 8;
		} else if (memsize>63) {
			memsize = 63;
		}

		reportIntValue(section, property, memsize);
		return;
	}

	if (!property.compare("vmemsize")) {
		int vmemsize = atoi(value.c_str());

		if (vmemsize == 0) {
			vmemsize = 2;
		} else if (vmemsize>8) {
			vmemsize = 8;
		}

		reportIntValue(section, property, vmemsize);
		return;
	}

	if (!property.compare("vmemsizekb")) {
		int vmemsizekb = atoi(value.c_str());

		if (vmemsizekb > 0) {
			reportIntValue(section, property, vmemsizekb);
		}
		return;
	}
}

void reportRenderValue(int section, string &property, string &value) {
	if (!property.compare("aspect")) {
		if (!value.compare("true") || !value.compare("1") || !value.compare("on")) {
			reportIntValue(section, property, 1);
		} else {
			reportIntValue(section, property, 0);
		}
		return;
	}
}

void reportCPUValue(int section, string &property, string &value) {
	if (!property.compare("core")) {
		if (!value.compare("simple") || !value.compare("normal") || !value.compare("dynamic") || !value.compare("auto")) {
			reportStringValue(section, property, value);
			return;
		}

		reportStringValue(section, property, "dynamic");
		return;
	}

	if (!property.compare("cputype")) {
		if (!value.compare("386") || !value.compare("386_slow")) {
			reportStringValue(section, property, "386");
			return;
		}

		if (!value.compare("386_prefetch")) {
			reportStringValue(section, property, value);
			return;
		}

		if (!value.compare("486") || !value.compare("486_slow")) {
			reportStringValue(section, property, "486");
			return;
		}

		if (!value.compare("pentium") || !value.compare("pentium_slow") || !value.compare("pentium_mmx")) {
			reportStringValue(section, property, "pentium");
			return;
		}

		reportStringValue(section, property, "auto");
		return;
	}

	if (!property.compare("cycles")) {
		reportStringValue(section, property, value);
		return;
	}
}

void reportMixerValue(int section, string &property, string &value) {
	if (!property.compare("blocksize")) {
		int blocksize = atoi(value.c_str());

		if (blocksize == 0) {
			reportIntValue(section, property, 1024);
		} else {
			reportIntValue(section, property, blocksize);
		}
		return;
	}

	if (!property.compare("prebuffer")) {
		int prebuffer = atoi(value.c_str());

		if (prebuffer == 0) {
			reportIntValue(section, property, 10);
		} else {
			reportIntValue(section, property, prebuffer);
		}
		return;
	}

	if (!property.compare("rate")) {
		int rate = atoi(value.c_str());

		if (rate == 0) {
			reportIntValue(section, property, 22050);
		} else {
			reportIntValue(section, property, rate);
		}
		return;
	}
}

void reportMidiValue(int section, string &property, string &value) {
	//TODO - path to roms

	if (!property.compare("mpu401")) {
		if (!value.compare("none") || !value.compare("off") || !value.compare("disabled")) {
			reportStringValue(section, property, "none");
		} else if (!value.compare("synth")) {
			reportStringValue(section, property, "synth");
		} else {
			reportStringValue(section, property, "mt32");
		}
		return;
	}
}

void reportSpeakerValue(int section, string &property, string &value) {
	if (!property.compare("pcspeaker")) {
		if (!value.compare("true") || !value.compare("on") || !value.compare("enabled")) {
			reportStringValue(section, property, "true");
		} else {
			reportStringValue(section, property, "false");
		}
		return;
	}

	if (!property.compare("pcrate")) {
		int rate = atoi(value.c_str());

		if (rate == 0) {
			reportIntValue(section, property, 22050);
		} else {
			reportIntValue(section, property, rate);
		}

		return;
	}
}

void reportSoundBlasterValue(int section, string &property, string &value) {
	if (!property.compare("sbtype")) {
		if (!value.compare("none") || !value.compare("sb1") || !value.compare("sb2") || !value.compare("sbpro1") ||
			!value.compare("sbpro2") || !value.compare("sb16")) {
			reportStringValue(section, property, value);
		} else {
			reportStringValue(section, property, "none");
		}
		return;
	}

	if (!property.compare("sbmixer")) {
		if (!value.compare("true") || !value.compare("on") || !value.compare("enabled")) {
			reportStringValue(section, property, "true");
		} else {
			reportStringValue(section, property, "false");
		}
		return;
	}

	if (!property.compare("oplmode")) {
		reportStringValue(section, property, value);
		return;
	}

	if (!property.compare("oplemu")) {
		reportStringValue(section, property, value);
		return;
	}

	if (!property.compare("oplrate")) {
		int rate = atoi(value.c_str());

		if (rate == 0) {
			reportIntValue(section, property, 22050);
		} else {
			reportIntValue(section, property, rate);
		}

		return;
	}
}

void reportIPXValue(int section, string &property, string &value) {
	if (!property.compare("ipx")) {
		if (!value.compare("true") || !value.compare("on") || !value.compare("enabled")) {
			reportStringValue(section, property, "true");
		} else {
			reportStringValue(section, property, "false");
		}
		return;
	}
}

void reportDOSValue(int section, string &property, string &value) {
	if (!property.compare("xms")) {
		if (!value.compare("true") || !value.compare("on") || !value.compare("enabled")) {
			reportStringValue(section, property, "true");
		} else {
			reportStringValue(section, property, "false");
		}
		return;
	}

	if (!property.compare("ems")) {
		if (!value.compare("true") || !value.compare("on") || !value.compare("enabled")) {
			reportStringValue(section, property, "true");
		} else {
			reportStringValue(section, property, "false");
		}
		return;
	}

	if (!property.compare("umb")) {
		if (!value.compare("true") || !value.compare("on") || !value.compare("enabled")) {
			reportStringValue(section, property, "true");
		} else {
			reportStringValue(section, property, "false");
		}
		return;
	}
}

void reportGusValue(int section, string &property, string &value) {
	if (!property.compare("gus")) {
		if (!value.compare("true") || !value.compare("on") || !value.compare("enabled")) {
			reportStringValue(section, property, "true");
		} else {
			reportStringValue(section, property, "false");
		}
		return;
	}

	if (!property.compare("gusrate")) {
		int rate = atoi(value.c_str());

		if (rate == 0) {
			reportIntValue(section, property, 22050);
		} else {
			reportIntValue(section, property, rate);
		}

		return;
	}

	if (!property.compare("gusbase")) {
		int base = atoi(value.c_str());

		if (base > 0) {
			reportIntValue(section, property, base);
		}
		return;
	}

	if (!property.compare("gusirq")) {
		int irq = atoi(value.c_str());
		reportIntValue(section, property, irq);
		return;
	}

	if (!property.compare("gusdma")) {
		int dma = atoi(value.c_str());
		reportIntValue(section, property, dma);
		return;
	}

	if (!property.compare("ultradir")) {
		reportStringValue(section, property, value);
		return;
	}
}

vector<string> split(string str, char delimiter) {
	vector<string> internal;
	stringstream ss(str); // Turn the string into a stream.
	string tok;

	while(getline(ss, tok, delimiter)) {
		internal.push_back(tok);
	}

	return internal;
}

void parseAutoexec(int section, string &textLine) {
	//reportStringValue(section, NULL, textLine);
	//androidlog(textLine.c_str());
	vector<string> tokens = split(textLine, ' ');
	int size = tokens.size();

	if (size > 1 && !tokens[0].compare("mount")) {
		for(int i = 0; i < tokens.size(); ++i) {
			androidlog(tokens[i].c_str());
		}
	} else {
		androidlog(textLine.c_str());
	}
}