
package magiclib.logging;

import android.content.Context;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

import magiclib.Global;

public class Log
{
    public static boolean DEBUG = false;
    public static boolean enableShow = true;
    public static boolean toFile = false;

    public static void log(String msg)
    {
        if (msg == null)
            return;

        android.util.Log.d("Benchmark", msg);

        if (toFile) {
            writeToFile(msg);
        }
    }

    public static void logError(Context ctx, String msg)
    {
        if (msg == null)
            return;

        log(msg);

        if (enableShow)
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
    }

    public static void logError(String msg)
    {
        if (msg == null)
            return;

        log(msg);

        if (enableShow)
            Toast.makeText(Global.context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void logErrorLong(Context ctx, String msg)
    {
        if (msg == null)
            return;

        log(msg);

        if (enableShow)
            Toast.makeText(ctx, msg, Toast.LENGTH_LONG).show();
    }

    private static void writeToFile(String msg)
    {
        try {
            File log = new File("/storage/emulated/0/magicbox.log");
            FileOutputStream out = new FileOutputStream(log, true);
            out.write	((msg + "\n").getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
