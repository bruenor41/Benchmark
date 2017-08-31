package magiclib.core;

import android.content.res.Configuration;
import android.opengl.GLSurfaceView;
import android.view.KeyEvent;
import android.view.MotionEvent;

import magiclib.Global;
import magiclib.IO.SAFSupport;
import magiclib.graphics.EmuVideo;
import magiclib.mouse.MouseButton;
import magiclib.mouse.MouseType;

public class EmuManager {
    public abstract interface onEmuManagerEventListener
    {
        //base
        void onInit();
        boolean onPause();
        boolean onUnPause();
        void onQuit();
        void onForceQuit();
        void onMainMenuShow();
        void onNativeOption(NativeOption option, int value, String value2);
        void onSplashTimeOut();
    }

    public static boolean isInitialized;
    public static boolean showAllButtons = true;
    public static boolean blockEvents;
    /**I need to know how to generate shell commands*/
    public static EmuConfig emuConfig;
    public static MouseButton mouse_button;
    public static int systemWidgetDialogsCount;

    private static MouseButton temp_mouse_button = MouseButton.disabled;
    private static MouseType temp_mouse_type = MouseType.disabled;
    private static boolean isEmuPaused;

    private static onEmuManagerEventListener managerEvent;

    public static void reset() {
    }

    public static void init(onEmuManagerEventListener event)
    {
        isInitialized = false;

        Screen.init();

        isEmuPaused = false;
        managerEvent = event;
        showAllButtons = true;
        systemWidgetDialogsCount = 0;
        blockEvents = false;

        loadLayouts();

        EmuSignal.init();

        SAFSupport.resolver = Global.context.getContentResolver();

        if (managerEvent!=null) {
            managerEvent.onInit();
        }

        onConfigurationChanged(null);

        isInitialized = true;
    }

    public static void dispose() {
        blockEvents = true;

        SAFSupport.resolver = null;
        Screen.dispose();
    }

    public static void onConfigurationChanged(Configuration newConfig)
    {
        if (!blockEvents) {
            int orientation = Screen.orientation;
            int newOrientation = newConfig==null?orientation:newConfig.orientation;

            Screen.update();
        }
    }

    private static void loadLayouts()
    {

        emuConfig = new EmuConfig();
        mouse_button = EmuConfig.mouse_button;

        setLazyDrawing();
    }

    public static boolean isMouseDisabled()
    {
        return (getMouseType() == MouseType.disabled);
    }

    public static MouseButton getMouseButton()
    {
        return getMouseButton(false);
    }

    public static MouseButton getMouseButton(boolean spen)
    {
        if (temp_mouse_button != MouseButton.disabled)
        {
            return temp_mouse_button;
        }

        if (spen)
        {
            return EmuConfig.spen_primary_button;
        }

        return mouse_button;
    }

    public static MouseType getMouseType()
    {
        if (temp_mouse_type != MouseType.disabled)
        {
            return temp_mouse_type;
        }

        return emuConfig.mouse_type;
    }

    public static void setLazyDrawing()
    {
        EmuVideo.setVideoRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public static boolean onTouch(MotionEvent event){
        return true;
    }

    public static void pause() {
        isEmuPaused  = managerEvent.onPause();
    }

    public static void quit()
    {
        managerEvent.onQuit();
    }

    public static boolean onHandleKey(int keyCode, final KeyEvent event)
    {
        if (blockEvents) {
            return false;
        }

        boolean isDown = event.getAction() == KeyEvent.ACTION_DOWN;

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:{
                NativeControl.nativeKey(45, isDown ? 1 : 0, 0, 0, 0);
                return true;
            }
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            {
                break;
            }
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_HOME:
            case KeyEvent.KEYCODE_SEARCH:
            case KeyEvent.KEYCODE_UNKNOWN:
            {
                break;
            }
            default:
            {
                break;
            }
        }

        return false;
    }

    public static void showMainMenu() {
        managerEvent.onMainMenuShow();
    }

    public static void setNativeOption(NativeOption option, int value, String value2) {
        managerEvent.onNativeOption(option, value, value2);
    }

    public static void forceQuit(){
        managerEvent.onForceQuit();
    }

    public static void finishSplash() {
        managerEvent.onSplashTimeOut();
    }
}
