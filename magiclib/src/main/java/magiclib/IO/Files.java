
package magiclib.IO;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import magiclib.Global;
import magiclib.logging.Log;

public class Files
{
    public static boolean filesEquals(File a, File b) {
        if(a.length() != b.length()){
            return false;
        }

        try {

            final int BLOCK_SIZE = 128;
            InputStream aStream = new FileInputStream(a);
            InputStream bStream = new FileInputStream(b);
            byte[] aBuffer = new byte[BLOCK_SIZE];
            byte[] bBuffer = new byte[BLOCK_SIZE];
            while (true) {
                int aByteCount = aStream.read(aBuffer, 0, BLOCK_SIZE);
                bStream.read(bBuffer, 0, BLOCK_SIZE);
                if (aByteCount < 0) {
                    return true;
                }
                if (!Arrays.equals(aBuffer, bBuffer)) {
                    return false;
                }
            }
        }
        catch(Exception e)
        {
            if (Log.DEBUG) Log.log("Files.binaryDiff : " + e.getMessage());
        }

        return false;
    }

    public static boolean fileCopy(File src, File dest)
    {
        if (src.getAbsolutePath() == dest.getAbsolutePath())
            return false;

        try
        {
            InputStream in = new FileInputStream( src );
            OutputStream out = new FileOutputStream( dest );

            int bufferSize;
            byte[] bufffer = new byte[512];

            while ((bufferSize = in.read(bufffer)) > 0)
            {
                out.write(bufffer, 0, bufferSize);
            }

            in.close();
            out.close();

            return true;
        }
        catch(Exception e)
        {
            if (Log.DEBUG) Log.log("Files.fileCopy : " + e.getMessage());
        }

        return false;
    }

    public static void fileCopy(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;

        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public static String readTextFile(String fileName) throws IOException
    {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();

        try
        {
            String line = br.readLine();

            while (line != null)
            {
                sb.append(line);
                sb.append("\n");

                line = br.readLine();
            }
        }
        finally
        {
            br.close();
        }

        return sb.toString();
    }

    public static boolean writeTextFile(String fileName, String text) throws IOException
    {
        boolean error = false;

        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));

        try
        {
            bw.write(text);
        }
        catch(Exception e) {
            error = true;
        }
        finally
        {
            bw.flush();
            bw.close();
        }

        return error;
    }

    public static void folderCopy(File src, File dest) throws IOException
    {
        if(src.isDirectory())
        {
            if(!dest.exists())
            {
                dest.mkdir();
            }

            String files[] = src.list();

            for (String file : files)
            {
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);

                folderCopy(srcFile, destFile);
            }

        }
        else
        {
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;

            while ((length = in.read(buffer)) > 0)
            {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }

    public static boolean deleteDirectory(File dir) {
        if(! dir.exists() || !dir.isDirectory())    {
            return false;
        }

        String[] files = dir.list();
        for(int i = 0, len = files.length; i < len; i++)    {
            File f = new File(dir, files[i]);
            if(f.isDirectory()) {
                deleteDirectory(f);
            }else   {
                f.delete();
            }
        }
        return dir.delete();
    }

    public static void createNoMediaFile()
    {
        File nomedia = new File(Global.appPath, ".nomedia");

        try
        {
            if (!nomedia.exists())
                nomedia.createNewFile();
        }
        catch (Exception exc) {
        }
    }

    public static String getFileExtension(String fileName)
    {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i >= 0) {
            extension = fileName.substring(i+1);
        }

        return extension;
    }

    public static String getFileNameWithoutExtension(String fileName)
    {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i==0) {
            return "";
        }

        if (i > 0) {
            return fileName.substring(0, i);
        }

        return fileName;
    }

    public static boolean saveResourceToFile(Context context, int resourceID, File destination)
    {
        boolean result = false;

        try
        {
            InputStream stream = context.getResources().openRawResource(resourceID);
            FileOutputStream fos = new FileOutputStream(destination.getAbsolutePath());

            byte[] buffer = new byte[1024];

            int byteRead = 0;

            while ((byteRead = stream.read(buffer, 0, 1024)) >= 0)
            {
                fos.write(buffer, 0, byteRead);
            }

            fos.flush();
            fos.close();

            result = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return result;
    }

    public static boolean copyScaledFile(File source, File destination, int maxScale) {
        BitmapFactory.decodeFile(source.getAbsolutePath(), Global.imageHeaderOptions);

        if ((Global.imageHeaderOptions.outWidth > maxScale) || (Global.imageHeaderOptions.outHeight > maxScale)) {
            try {
                Bitmap scaled = Global.decodeFile(source, maxScale);

                boolean isPng = Files.getFileExtension(source.getAbsolutePath()).toLowerCase().equals("png");

                OutputStream fOut = new FileOutputStream(destination);
                scaled.compress(isPng ? Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();

                scaled.recycle();
                scaled = null;

                return  true;
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            return false;
        }
        return Files.fileCopy(source, destination);
    }
}
