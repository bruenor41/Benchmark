package magiclib.IO;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Decompress {
    public abstract interface DecompressEventListener
    {
        public abstract void onUnzip(String name, boolean isDirectory, int index);
        public abstract void onError(String error);
    }

    private static final int BUFFER_SIZE = 1024 * 10;
    private static final String TAG = "Decompress";

    private DecompressEventListener event;

    public void setOnDecompressEventListener(DecompressEventListener event) {
        this.event = event;
    }

    public boolean unzipFromAssets(Context context, String zipFile, String destination) {
        boolean result = false;
        try {
            if (destination == null || destination.length() == 0)
                destination = context.getFilesDir().getAbsolutePath();
            InputStream stream = context.getAssets().open(zipFile);

            if (!destination.endsWith(File.separator)) {
                destination+=File.separator;
            }

            result = unzip(stream, destination);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean unzip(String zipFile, String destination) {
        boolean result = false;
        try {
            FileInputStream fin = new FileInputStream(zipFile);

            if (!destination.endsWith(File.separator)) {
                destination+=File.separator;
            }

            result = unzip(fin, destination);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    public boolean unzip(InputStream stream, String destination) {
        boolean result = false;
        int index = 0;

        dirChecker(destination, "");
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            ZipInputStream zin = new ZipInputStream(stream);
            ZipEntry ze = null;

            while ((ze = zin.getNextEntry()) != null) {
                if (ze.isDirectory()) {
                    dirChecker(destination, ze.getName());

                    if (event != null) {
                        event.onUnzip(ze.getName(), true, index);
                    }
                    index++;
                } else {
                    File f = new File(destination + ze.getName());
                    if (!f.exists()) {
                        FileOutputStream fout = new FileOutputStream(destination + ze.getName());
                        int count;
                        while ((count = zin.read(buffer)) != -1) {
                            fout.write(buffer, 0, count);
                        }
                        zin.closeEntry();
                        fout.close();
                    }
                    if (event != null) {
                        event.onUnzip(ze.getName(), false, index);
                    }
                    index++;
                }

            }
            zin.close();
            result = true;
        } catch (Exception e) {
            if (event != null) {
                event.onError("unzip : " + e.getMessage());
            }
        }
        return result;
    }

    private void dirChecker(String destination, String dir) {
        File f = new File(destination + dir);

        if (!f.isDirectory()) {
            boolean success = f.mkdirs();
            if (!success) {
                if (event != null) {
                    event.onError("Failed to create folder " + f.getName());
                }
            }
        }
    }

    public int getItemsCountFromAssetsZip(Context context, String zipFile)
    {
        int count = 0;

        try {
            InputStream inputStream = context.getAssets().open(zipFile);
            ZipInputStream zipIs = new ZipInputStream(inputStream);

            while ((zipIs.getNextEntry()) != null) {
                count++;
                zipIs.closeEntry();
            }

            zipIs.close();
        } catch (Exception e) {
            if (event != null) {
                event.onError("unzip : " + e.getMessage());
            }
            count = -1;
        }

        return count;
    }
}