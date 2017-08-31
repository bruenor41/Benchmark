package magiclib.core;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.view.View;

import magiclib.Global;

public class EmuSignal {
	public static final int MSG_MULTITAP = -1;
	public static final int MSG_LONG_PRESS = -2;
	public final static int MSG_ABSOLUTE_CALIBRATE = -3;
	public final static int MSG_SPLASH_TIMEOUT = -4;
	public final static int MSG_QUIT_TIMEOUT = -5;
	public final static int MSG_START_WIDGET = -6;

	public final static int MSG_DIM_NAVIGATION_BAR = -7;
	public final static int MSG_BACKBUTTON_LONGPRESS = -8;
	public final static int MSG_SHOW_INBUILT_KEYBOARD = -9;

	public static Handler signal;

	public static void init() {
		signal = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
					case MSG_MULTITAP: {
						break;
					}
					case MSG_LONG_PRESS: {
						break;
					}
					case MSG_ABSOLUTE_CALIBRATE: {
						break;
					}
					case MSG_SPLASH_TIMEOUT: {
						//EmuVideo.setVideoBackgroundResource(0);
						if (Global.context!=null) {
							EmuManager.finishSplash();
						}
						break;
					}
					case MSG_DIM_NAVIGATION_BAR: {
						((Activity) (Global.context)).getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
						break;
					}
					case MSG_QUIT_TIMEOUT: {
						EmuManager.forceQuit();
						break;
					}
					case MSG_START_WIDGET: {
						break;
					}
					case MSG_BACKBUTTON_LONGPRESS:{
						break;
					}
					default: {
						break;
					}
				}
			}
		};
	}

	public static void calibrateAbsoluteMouse(int delay) {
		signal.removeMessages(MSG_ABSOLUTE_CALIBRATE);
		signal.sendEmptyMessageDelayed(MSG_ABSOLUTE_CALIBRATE, delay);
	}

	public static void sendQuitMessage(int delay) {
		signal.sendEmptyMessageDelayed(MSG_QUIT_TIMEOUT, delay);
	}

	public static void removeQuitMessage() {
		signal.removeMessages(MSG_QUIT_TIMEOUT);
	}

	public static void sendSplashMessage(int delay) {
		signal.sendEmptyMessageDelayed(MSG_SPLASH_TIMEOUT, delay);
	}

	public static void sendStartWidgetMessage(int delay) {
		signal.sendEmptyMessageDelayed(MSG_START_WIDGET, delay);
	}

	public static void sendShowInbuiltKeyboardMessage(int delay) {
		signal.sendEmptyMessageDelayed(MSG_SHOW_INBUILT_KEYBOARD, delay);
	}

	public static void sendDimBarMessage(int delay) {
		signal.sendEmptyMessageDelayed(MSG_DIM_NAVIGATION_BAR, delay);
	}

	public static boolean isEmuQuitStarted() {
		return signal.hasMessages(MSG_QUIT_TIMEOUT);
	}

	public static void sendLongPressMessage(int delay) {
		signal.sendEmptyMessageDelayed(MSG_LONG_PRESS, delay);
	}

	public static void cancelLongPressMessage() {
		signal.removeMessages(MSG_LONG_PRESS);
	}

	public static void sendBackButtonLongPressMessage(int delay) {
		signal.sendEmptyMessageDelayed(MSG_BACKBUTTON_LONGPRESS, delay);
	}

	public static void cancelBackButtonLongPressMessage() {
		signal.removeMessages(MSG_BACKBUTTON_LONGPRESS);
	}
}