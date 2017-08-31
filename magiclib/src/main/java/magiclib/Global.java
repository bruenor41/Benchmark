
package magiclib;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Vibrator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

import magiclib.core.EmuConfig;
import magiclib.core.EmuSignal;

public class Global
{
    /**Application name*/
    public static String appName;

    /**Application path. Mostly "/sdcard/MagicBox/"*/
    public static String appPath;

    /**Application export path. Mostly "/sdcard/MagicBox/Exports/"*/
    public static String appExportPath;

    /**Application temp path. Mostly "/sdcard/MagicBox/Temp/"*/
    public static String appTempPath;

    /**Path to games configuration path. Mostly "/sdcard/MagicBox/Games/"*/
    public static String gamesRootPath;

    /**Path to games data path. Mostly "/sdcard/MagicBox/Games/Data/"*/
    public static String gamesDataPath;

    /**Root path to currently isLoaded game*/
    public static String currentGameRootPath;

    /**Current game fonts directory*/
    public static String currentGameFontsPath;

    public static String currentGameDOSROOTPath;
    public static String currentGameDOSSHAREDPath;

    /**detects manifest debuggable value*/
    public static boolean isDebuggable;

    /**detects opengl es 2.0*/
    public static boolean isOpenGL2Present;

    /** Current GLES Version (null for emulator)*/
    public static String glesVersion;

    /** Current GLSL Version (null for emulator)*/
    public static String glslVersion;

    /** Global context based on current Activity*/
    public static Context context;

    /**Display metrics density*/
    public static float densityScale;

    /**Options for decoding image file header only without allocating memory for pixels*/
    public static BitmapFactory.Options imageHeaderOptions;

    /** project resources*/
    public static int logo, empty_image;

    public static int textColor1;

    public static void init()
    {
        empty_image = 0;

        appPath = context.getFilesDir().getAbsolutePath();

        if (appPath == null)
            appPath = "";

        if (appPath != null && !appPath.equals("")) {
            if (!appPath.endsWith("/")) {
                appPath += "/";
            }

            appExportPath = appPath + "Exports/";
            appTempPath = appPath + "Temp/";

            File dir = new File(appTempPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            gamesRootPath = appPath + "Games/";
            gamesDataPath = gamesRootPath + "Data/";
            currentGameDOSROOTPath = appPath + "DOSROOT/";
            dir = new File(currentGameDOSROOTPath );
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }

        densityScale = context.getResources().getDisplayMetrics().density;

        textColor1 = context.getResources().getColor(R.color.textColor1);

        imageHeaderOptions = new BitmapFactory.Options();
        imageHeaderOptions.inJustDecodeBounds = true;

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        if (!isDebuggable)
        {
            ConfigurationInfo info = am.getDeviceConfigurationInfo();
            isOpenGL2Present = (info.reqGlEsVersion >= 0x20000);
        }
        else
            isOpenGL2Present = true;
    }

    public static String getStringFromInputStream(InputStream stream) throws IOException
    {
        int n = 0;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = new InputStreamReader(stream, "UTF8");
        StringWriter writer = new StringWriter();
        while (-1 != (n = reader.read(buffer))) writer.write(buffer, 0, n);
        return writer.toString();
    }

    public static int DensityToPixels(int dps)
    {
        return (int) (dps * densityScale + 0.5f);
    }

    public static Bitmap decodeFile(File f, int max) throws IOException
    {
        Bitmap b = null;

        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;

        FileInputStream fis = new FileInputStream(f);
        BitmapFactory.decodeStream(fis, null, o);
        fis.close();

        int scale = 1;
        if (o.outHeight > max || o.outWidth > max)
        {
            scale = (int)Math.pow(2, (int) Math.ceil(Math.log(max / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
        }

        //Decode with inSampleSize
        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        fis = new FileInputStream(f);
        b = BitmapFactory.decodeStream(fis, null, o2);
        fis.close();

        return b;
    }

    public static String getSharedString(String preference)
    {
        return getSharedString(context, preference);
    }

    public static String getSharedString(Context context, String preference)
    {
        SharedPreferences settings = context.getSharedPreferences(appName + "Configuration", 0);
        return settings.getString(preference, "");
    }

    public static void saveSharedPreferences(String key, String data)
    {
        SharedPreferences settings = context.getSharedPreferences(appName + "Configuration", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, data);
        editor.commit();
    }

    public static void dimNavigationBar()
    {
        if (Build.VERSION.SDK_INT < 14 || !EmuConfig.dimNavigationBar)
        {
            return;
        }

        EmuSignal.sendDimBarMessage(1000);
    }
}
