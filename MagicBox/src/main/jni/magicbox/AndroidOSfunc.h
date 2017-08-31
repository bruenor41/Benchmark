/*
 *  Copyright (C) 2011 Locnet (android.locnet@gmail.com)
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

#ifndef ANDROID_OS_FUNC
#define ANDROID_OS_FUNC

#include <jni.h>
#include <stdio.h>
#include "InputEvent.h"

void Android_Init(JNIEnv * env, jobject obj, jobject videoBuffer, jint width, jint height);
void Android_ShutDown(bool forced);
void Android_SetVideoMode(int width, int height, int depth);
void Android_MouseSetVideoMode(int mode, int width, int height);
void Android_LockSurface();
void Android_UnlockSurface(int startLine, int endLine);
void Android_ResetScreen();
void Android_AudioGetBuffer();
int Android_OpenAudio(int rate, int channels, int encoding, int bufSize);
InputEvent *Android_PollEvent();
void Android_KeyboardChanged(const char *keyb);

#endif
