package magiclib.core;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import magiclib.Global;

public class Screen
{
    /** current screen width*/
    public static int screenWidth;

    /** current screen height*/
    public static int screenHeight;

    /** current screen orientation*/
    public static volatile int orientation;

    public static Display display;

    public static void init() {
        WindowManager wm = (WindowManager) Global.context.getSystemService(Context.WINDOW_SERVICE);
        display = wm.getDefaultDisplay();

        update();
    }

    public static void dispose() {
        display = null;
    }

    public static void update() {
        screenWidth = getDisplayWidth();
        screenHeight = getDisplayHeight();
        orientation = getOrientation();
    }

    public static String getScreenCategory()
    {
        int screenLayout = Global.context.getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        switch (screenLayout) {
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                return "small";
            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
                return "normal";
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                return "large";
            case 4: // Configuration.SCREENLAYOUT_SIZE_XLARGE is API >= 9
                return "xlarge";
            default:
                return "undefined";
        }
    }

    public static int getOrientation()
    {
        return Global.context.getResources().getConfiguration().orientation;
    }

    public static boolean isLandscape()
    {
        return orientation == 2;
    }

    public static String getDpiName(int value) {
        switch (value) {
            case 36: {
                return "ldpi";
            }
            case 48: {
                return "mdpi";
            }
            case 72: {
                return "hdpi";
            }
            case 96: {
                return "xhdpi";
            }
            case 144: {
                return "xxhdpi";
            }
            case 192: {
                return "xxxdpi";
            }
            case 64: {
                return "tvdpi";
            }
        }

        return "unknown dpi";
    }

    private static Point displaySize;

    public static int getDisplayWidth()
    {
        return getDisplayWidth(display);
    }

    public static int getDisplayHeight()
    {
        return getDisplayHeight(display);
    }

    @SuppressWarnings("deprecation")
    public static int getDisplayWidth(Display display)
    {
        if(Build.VERSION.SDK_INT > 12)
        {
            if (displaySize == null)
                displaySize = new Point();

            display.getSize(displaySize);
            return displaySize.x;
        }

        return display.getWidth();
    }

    @SuppressWarnings("deprecation")
    public static int getDisplayHeight(Display display)
    {
        if(Build.VERSION.SDK_INT > 12)
        {
            if (displaySize == null)
                displaySize = new Point();

            display.getSize(displaySize);
            return displaySize.y;
        }

        return display.getHeight();
    }
}