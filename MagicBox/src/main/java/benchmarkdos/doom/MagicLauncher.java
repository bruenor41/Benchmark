package benchmarkdos.doom;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

import magiclib.Global;
import magiclib.IO.Files;
import magiclib.core.EmuConfig;
import magiclib.core.EmuManager;
import magiclib.core.EmuSignal;
import magiclib.core.NativeOption;
import magiclib.core.Screen;
import magiclib.dosbox.DosboxConfig;
import magiclib.graphics.EmuVideo;
import magiclib.logging.Log;
import magiclib.mouse.MouseType;

public class MagicLauncher extends Activity
{		
	private boolean not_permitted_start = false;
    private boolean firstResume = true;
	private boolean dbxKeyboardShown;
	private boolean isFocusAfterResume = false;

	private RelativeLayout mainView;
	
	public static native void nativeMouseMax(boolean enabled, int max_width, int max_height);
	public static native void nativeSetAbsoluteMouseType(int type);
	public static native void nativeMouseRoundMaxByVideoMode(boolean enabled);
	public static native int nativeGetMouseVideoWidth();
	public static native int nativeGetMouseVideoHeight();	
	public static native void nativeSaveState(String path);
	public static native void nativeLoadState(String path);	
	public static native void nativeInit();
	public static native void nativeShutDown();
	public static native void nativeSetOption(int option, int value, String value2);
	public native void nativeStart(Buffer videoBuffer, int width, int height, int version);
	public static native void nativePause(int state);
	public static native void nativeStop();
	public static native int nativeGetLibArchitecture();
	public static native int nativeGetCoreType();
	public static native void nativeForceStop();
	
	public Audio mAudioDevice = null;
	public DosBoxThread mDosBoxThread = null;		
	public boolean mTurboOn = false;

	public DOSProgramUpdater updater;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		AppGlobal.context = this;
		AppGlobal.appName = "Benchmark";
		dbxKeyboardShown = false;
		isFocusAfterResume = false;

		AppGlobal.init();

		System.loadLibrary("magiclib");
		mainView = (RelativeLayout)getLayoutInflater().inflate(R.layout.main, null);
		updater = new DOSProgramUpdater(MagicLauncher.this, mainView);
		updater.setOnDOSProgramUpdaterEventListener(getDOSProgramUpdaterEventListener());
		updater.start();
	}

	private DOSProgramUpdater.DOSProgramUpdaterEventListener getDOSProgramUpdaterEventListener() {
		return new DOSProgramUpdater.DOSProgramUpdaterEventListener() {
			@Override
			public void onFinish() {
				startDosbox();
			}
		};
	}

	public void hideProgress() {
		this.mainView.removeAllViews();
		this.mainView.addView(EmuVideo.surface);
	}

	private void startDosbox() {
		EmuVideo.surface = new DosboxVideo(this);
		EmuVideo.surface.setKeepScreenOn(true);
		EmuManager.init(getEmulatorInitEvent());
	}

	@Override
	public boolean onKeyDown(int keyCode, final KeyEvent event)
	{
		if (event.getRepeatCount() > 0) {
			return true;
		}
		return EmuManager.onHandleKey(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, final KeyEvent event)
	{
		if (event.getRepeatCount() > 0) {
			return true;
		}
		return EmuManager.onHandleKey(keyCode, event);
	}

	private EmuManager.onEmuManagerEventListener getEmulatorInitEvent() {
		return new EmuManager.onEmuManagerEventListener() {
			@Override
			public void onInit() {
				System.loadLibrary("dosbox");

				mAudioDevice = new Audio(MagicLauncher.this);
				nativeInit();

				setupAndStartDosbox();
			}

			@Override
			public boolean onPause() {
				if (!isDosboxPaused())
				{
					pauseDosBox(true);
				}

				return true;
			}

			@Override
			public boolean onUnPause() {
				if (isDosboxPaused())
				{
					pauseDosBox(false);
				}
				return  true;
			}

			@Override
			public void onQuit() {}

			@Override
			public void onForceQuit() {
				doExit();
				nativeForceStop();
			}

			@Override
			public void onNativeOption(NativeOption option, int value, String value2) {}

			@Override
			public void onSplashTimeOut() {
				hideProgress();
			}

			@Override
			public void onMainMenuShow() {}
		};
	}

	@Override
	protected void onDestroy() 
	{
		if (Log.DEBUG)  Log.log("onDestroy");

		if (!not_permitted_start)
		{
			stopDosBox();
			shutDownDosBox();
			EmuVideo.surface = null;
		}

		// very important:
		if (updater != null) {
			updater.dispose();
			updater = null;
		}

		super.onDestroy();

		if (Log.DEBUG)  Log.log("/onDestroy");
	}

	private boolean tmpIsPaused = false;
	public volatile boolean isExiting = false;

	@Override
	protected void onPause() 
	{
		if (Log.DEBUG) Log.log("onPause " + Thread.currentThread().getId() + ", isExiting=" + isExiting);
		try {
			if (!not_permitted_start) {
				if (isExiting) {
					if (mAudioDevice != null) {
						mAudioDevice.pause();
						mAudioDevice = null;
					}

					if (EmuVideo.surface!=null) {
						EmuVideo.surface.onPause();
						EmuVideo.surface = null;
					}

					EmuManager.dispose();
				} else {
					tmpIsPaused = isDosboxPaused();

					if (!tmpIsPaused) {
						pauseDosBox(true);
					}
				}
			}
		} catch (Exception exc) {
			if (Log.DEBUG)  Log.log("/onPause exception : " + ((exc == null)?"":exc.getMessage()));
		}

		super.onPause();
	}

	@Override
	protected void onResume() {
		Global.context = this;
		isFocusAfterResume = true;

		super.onResume();

		resumeDosbox();
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus && isFocusAfterResume) {
			isFocusAfterResume = false;

			if (EmuManager.isInitialized && (dbxKeyboardShown || Screen.orientation == Configuration.ORIENTATION_PORTRAIT)) {
				EmuSignal.sendShowInbuiltKeyboardMessage(50);
			}
		}
	}

	private void resumeDosbox()
	{
		if (mDosBoxThread == null)
			return;

		if (!tmpIsPaused)
			pauseDosBox(false);

		AppGlobal.dimNavigationBar();

		if (firstResume) {
			firstResume = false;

			//
		}

		if (EmuVideo.surface != null) {
			EmuVideo.surface.requestFocus();
			EmuVideo.surface.requestFocusFromTouch();
		}
	}

	public static void joystickEnable(int index, boolean enable) {
		MagicLauncher.nativeSetOption(index == 0 ? 18 : 19, enable ? 1 : 0, null);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) 
	{
		super.onConfigurationChanged(newConfig);

		if (EmuManager.isInitialized) {
			EmuManager.onConfigurationChanged(newConfig);
		}
	}

	public boolean isDosboxPaused()
	{
		return (mDosBoxThread != null && !mDosBoxThread.mDosBoxRunning);
	}
	
	public void pauseDosBox(boolean pause) {
		if (pause) {
			mDosBoxThread.mDosBoxRunning = false;
			nativePause(1);
			if (mAudioDevice != null)
				mAudioDevice.pause();			
		}
		else {
			nativePause(0);
			mDosBoxThread.mDosBoxRunning = true;
			//will auto play audio when have data
			//if (mAudioDevice != null)
			//	mAudioDevice.play();		
		}
	}

	private void setupAndStartDosbox() {
		try {
			String dosbox_config = AppGlobal.appTempPath + "dosbox.config";

			Files.writeTextFile(dosbox_config, DosboxConfig.generateDosboxConfig());

			MagicLauncher.nativeSetOption(5, 0, dosbox_config);

			nativeSetOption(12, (EmuConfig.speedPatchR) ? 1 : 0, null);
			nativeSetOption(13, (EmuConfig.speedPatchC) ? 1 : 0, null);
			nativeSetOption(16, EmuConfig.mouse_type == MouseType.absolute ? 100 : EmuConfig.mouse_sensitivity, null);
			joystickEnable(1, false);

			nativeMouseRoundMaxByVideoMode(EmuConfig.roundAbsoluteByVideoMode);
			nativeMouseMax(EmuConfig.mouse_max_enabled, EmuConfig.mouse_max_width, EmuConfig.mouse_max_height);

			nativeSetAbsoluteMouseType(1);

			mDosBoxThread = new DosBoxThread(this);

			startDosBox();

			//don't know whether one more handler will hurt, so abuse key handler
			EmuSignal.sendSplashMessage(1000);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void shutDownDosBox() {
		if (Log.DEBUG)  Log.log("shutDownDosBox");

		if (mDosBoxThread != null) {
			boolean retry;
			retry = true;
			while (retry) {
				try {
					mDosBoxThread.join();
					retry =	false;
				}
				catch (InterruptedException e) { // try again shutting down the thread
				}
			}

			nativeShutDown();

			if (mAudioDevice != null) {
				mAudioDevice.shutDownAudio();
				mAudioDevice = null;
			}

			mDosBoxThread = null;
		}


		if (Log.DEBUG)  Log.log("/shutDownDosBox");
	}	

	void startDosBox() 
	{
		if (mDosBoxThread != null)
			mDosBoxThread.start();
	}
	
	public void stopDosBox()
	{
		if (Log.DEBUG)  Log.log("stopDosBox " + Thread.currentThread().getId());
		try
		{
			nativePause(0);//it won't die if not running

			//stop audio AFTER above
			if (mAudioDevice != null) {
				mAudioDevice.pause();
				mAudioDevice = null;
			}

			if (EmuVideo.surface != null) {
				EmuVideo.surface.onPause();
				EmuVideo.surface = null;
			}

			EmuManager.dispose();

			EmuSignal.sendQuitMessage(2000);
			nativeStop();
		}
		catch(Exception exc) 
		{
			if (Log.DEBUG)
				Log.log("stopDosBox 1 exc : " + ((exc == null)? "" : exc.getMessage()));
		}

		if (Log.DEBUG)  Log.log("/stopDosBox");
	}

	public void callbackKeybChanged(String keyb)
	{
	}

	public void callbackExit()
	{
		EmuSignal.sendQuitMessage(1);
	}

	public void doExit() {
		if (Log.DEBUG)  Log.log("doExit " + Thread.currentThread().getId());

		EmuSignal.removeQuitMessage();

		if (mDosBoxThread != null) {
			mDosBoxThread.mDosBoxRunning = false;
		}

		if (EmuVideo.surface != null) {
			InputMethodManager imm = (InputMethodManager) AppGlobal.context.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.hideSoftInputFromWindow(EmuVideo.surface.getWindowToken(), 0);
			}
		}

		finish();

		if (Log.DEBUG)  Log.log("/doExit");
	}

	//video
	public void callbackVideoRedraw( int w, int h, int s, int e) 
	{
		//if (Log.DEBUG) Log.log("callbackVideoRedraw w,h[" + w + "," + h + "] s,e[" + s + "," + e + "]");
		try {
			if ((EmuVideo.surface == null) || (EmuVideo.surface.renderer == null)) {
				if (Log.DEBUG) Log.log("renderer is null 1");
				return;
			}

			EmuVideo surface = EmuVideo.surface;

			surface.renderer.mSrc_width = w;
			surface.renderer.mSrc_height = h;

			synchronized (surface.renderer.mDirty) {
				if (surface.renderer.mDirty) {
					surface.renderer.mStartLine = Math.min(surface.renderer.mStartLine, s);
					surface.renderer.mEndLine = Math.max(surface.renderer.mEndLine, e);
				} else {
					surface.renderer.mStartLine = s;
					surface.renderer.mEndLine = e;
				}

				//if (Log.DEBUG) Log.log("callbackVideoRedraw w,h[" + w + "," + h + "] s,e[" + s + "," + e + "][" +
				//		mSurfaceView.renderer.mStartLine + "," + mSurfaceView.renderer.mEndLine +
				//		"] diff[" + (mSurfaceView.renderer.mEndLine - mSurfaceView.renderer.mStartLine)  + "," + (e-s) + "]");

				surface.renderer.mDirty = true;
			}
			surface.requestRender();
		} catch (Exception exc) {
			if (Log.DEBUG)
				Log.log("callbackVideoRedraw exception : " + ((exc == null) ? "" : exc.getMessage()));
		}
	}
	
	public Bitmap callbackVideoSetMode( int w, int h) 
	{
		//if (Log.DEBUG) Log.log("callbackVideoSetMode w,h[" + w + "," + h + "]");
		if ((EmuVideo.surface == null) || (EmuVideo.surface.renderer == null))
		{
			if (Log.DEBUG) Log.log("renderer is null 2");
			return null;
		}

		EmuVideo surface = EmuVideo.surface;

		surface.renderer.mSrc_width = w;
		surface.renderer.mSrc_height = h;
		surface.renderer.resetScreen();

		surface.renderer.videoBuffer = null;
		surface.renderer.videoBuffer = ByteBuffer.allocateDirect(w * h * 2);
		surface.renderer.resize = true;

		return null;
	}	

	public void callbackMouseSetVideoMode(int mode, int width, int height)
	{
		//Log.log(String.format(Locale.getDefault(), "callbackMouseSetVideoMode %d,%d,%d ", mode, width, height));
		EmuSignal.calibrateAbsoluteMouse(100);
	}

	public Buffer callbackVideoGetBuffer() 
	{		
		if ((EmuVideo.surface == null) || (EmuVideo.surface.renderer == null))
		{
			if (Log.DEBUG) Log.log("renderer is null 3");
			return null;
		}
		
		if (EmuVideo.surface != null)
		{
			return EmuVideo.surface.renderer.videoBuffer;
		}
		else
		{
			return null;
		}
	}
	
	//audio
	public int callbackAudioInit(int rate, int channels, int encoding, int bufSize) 
	{
		if (mAudioDevice != null)
			return mAudioDevice.initAudio(rate, channels, encoding, bufSize);
		else
			return 0;
	}
	
	public void callbackAudioWriteBuffer(int size) {
		if (mAudioDevice != null){			
			mAudioDevice.AudioWriteBuffer(size);
		}
	}

	public short[] callbackAudioGetBuffer() {
		if (mAudioDevice != null)
			return mAudioDevice.mAudioBuffer;
		else
			return null;
	}

	//dosbox
	class DosBoxThread extends Thread {
		MagicLauncher mParent;
		public boolean	mDosBoxRunning = false;

		DosBoxThread(MagicLauncher parent) {
			mParent =  parent;
		}
		
		public void run() 
		{
			mDosBoxRunning = true;
			
			int w = 640;
			int h = 400;

			int ver = 0;
			try {
				String version = AppGlobal.context.getPackageManager().getPackageInfo(AppGlobal.context.getPackageName(), 0).versionName;
				ver = Integer.parseInt(version.split("1.0.")[1]);
			} catch(Exception e) {
				if (Log.DEBUG) Log.log("Start failed parse application version");
			}
			nativeStart(EmuVideo.surface.renderer.videoBuffer, w, h, ver);
			//nativeStart(mSurfaceView.renderer.videoBuffer, w, h, null);
			//will never return to here;
		}
	}
}
