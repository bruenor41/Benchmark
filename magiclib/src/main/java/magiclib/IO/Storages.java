
package magiclib.IO;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import magiclib.R;
import magiclib.Global;
import magiclib.core.NativeCore;
import magiclib.core.RandomStringGenerator;

public class Storages
{
    /**list of all detected storages*/
    private static List<StorageInfo> storages = null;

    /**list of detected storages with read-write access*/
    private static List<StorageInfo> rwStorages = null;

    /**Storage 1. User can set own storage or shortcut to path*/
    public static UserStorage userStorage1;

    /**Storage 2. User can set own storage or shortcut to path*/
    public static UserStorage userStorage2;

    private static String hashFile;

    private static String kitkatRWDir = "/";

    public interface onDrivePickListener
    {
        public void onPick(String drive);
    }

    private static void setHashFile()
    {
        hashFile = "mdbx_" + RandomStringGenerator.generateRandomString(5, RandomStringGenerator.Mode.ALPHANUMERIC) + ".temp";
    }

    private static void addKitkatSpecific(List<StorageInfo> list, HashSet<String> paths)
    {
        File[] dirs = Global.context.getExternalFilesDirs(null);

        if (dirs != null)
        {
            //  /storage/sdcard1/Android/data/benchmarkdos.doom/files,
            //  /storage/sdcard0/Android/data/benchmarkdos.doom/files

            for(File dir : dirs)
            {
                if (dir == null)
                    continue;

                String path = dir.getAbsolutePath();

                if (!path.contains("/Android/data/"))
                    continue;

                int index = path.indexOf("/Android/data/");
                String root = path.substring(0, index);

                addStorage(list, paths, root, "", false, true, false);
            }
        }
    }

    private static void getKitkatPrivateFolder()
    {
        File [] dirs = Global.context.getExternalFilesDirs(null);

        if (dirs == null)
            return;

        for(File dir : dirs)
        {
            if (dir == null)
                continue;

            String path = dir.getAbsolutePath();

            if (!path.contains("/Android/data/"))
                continue;

            kitkatRWDir = path.substring(path.indexOf("/Android/data/"));

            if (!kitkatRWDir.endsWith("/"))
            {
                kitkatRWDir += "/";
            }

            return;
        }
    }

    private static void getStorageList()
    {
        setHashFile();

        List<StorageInfo> list = new ArrayList<StorageInfo>();
        String def_path = Environment.getExternalStorageDirectory().getPath();
        BufferedReader buf_reader = null;

        try
        {
            HashSet<String> paths = new HashSet<String>();
            buf_reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;

            if(Build.VERSION.SDK_INT >= 19)
            {
                getKitkatPrivateFolder();
            }

            //SAF support
            if (SAFSupport.isEnabled())
            {
                addStorage(list, paths, SAFSupport.sdcardUriRealPath, null, false, true, true);
            }

            if (Storages.userStorage1 != null)
                addStorage(list, paths, Storages.userStorage1.path, Storages.userStorage1.title, true, true, false);

            if (Storages.userStorage2 != null)
                addStorage(list, paths, Storages.userStorage2.path, Storages.userStorage2.title, true, true, false);

            if(Build.VERSION.SDK_INT >= 19)
            {
                addKitkatSpecific(list, paths);
            }

            addStorage(list, paths, "/mnt/shell/emulated/0", "");
            addStorage(list, paths, "/storage/emulated/0", "");
            addStorage(list, paths, "/HWUserData", "");
            addStorage(list, paths, "/mnt/sdcard0", "");
            addStorage(list, paths, "/mnt/sdcard1", "");
            addStorage(list, paths, "/mnt/sdcard2", "");
            addStorage(list, paths, "/mnt/sdcard/external_sd", "");
            addStorage(list, paths, "/storage/sdcard0", "");
            addStorage(list, paths, "/storage/sdcard1", "");
            addStorage(list, paths, "/storage/sdcard2", "");
            addStorage(list, paths, "/storage/extSdCard", "");

            while ((line = buf_reader.readLine()) != null)
            {
                //if (uiLog.DEBUG) uiLog.log(line);

                if (line.contains("vfat") || line.contains("/mnt"))
                {
                    StringTokenizer tokens = new StringTokenizer(line, " ");
                    String unused = tokens.nextToken(); //device
                    String mount_point = getPathWithoutLastSlah(tokens.nextToken()); //mount point

                    if (mount_point == null || paths.contains(mount_point))
                    {
                        continue;
                    }

                    unused = tokens.nextToken(); //file system
                    List<String> flags = Arrays.asList(tokens.nextToken().split(",")); //flags
                    boolean readonly = flags.contains("ro");

                    if (mount_point.equals(def_path))
                    {
                        if (!readonly)
                        {
                            addStorage(list, paths, def_path, "");
                        }
                    }
                    else if (line.contains("/dev/block/vold") /*|| line.contains("/dev/block/mmc")*/)
                    {
                        if (!line.contains("/mnt/secure")
                                && !line.contains("/mnt/asec")
                                && !line.contains("/mnt/obb")
                                && !line.contains("/dev/mapper")
                                && !line.contains("tmpfs")) {

                            if (!readonly)
                            {
                                addStorage(list, paths, mount_point, "");
                            }
                        }
                    }
                }
            }

            addStorage(list, paths, def_path, "", false, true, false);

            paths.clear();

            deleteTempFiles(list);
            setStorages(list);

            list.clear();
        }
        catch (FileNotFoundException ex)
        {
            ex.printStackTrace();
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (buf_reader != null)
            {
                try
                {
                    buf_reader.close();
                }
                catch (IOException ex) {
                }
            }
        }
    }

    private static String getPathWithoutLastSlah(String path)
    {
        if (path == null)
            return null;

        if (path.endsWith("/"))
        {
            return path.substring(0, path.length() - 1);
        }

        return path;
    }

    private static void deleteTempFiles(List<StorageInfo> list)
    {
        //delete temp hash files
        for (StorageInfo info : list)
        {
            try
            {
                File f = new File(info.path + ((info.readOnly)? kitkatRWDir : "/") + hashFile);
                f.delete();
            }
            catch(Exception exc){
            }
        }
    }

    private static void setStorages(List<StorageInfo> list)
    {
        clearStorages();

        //add writeable drives
        for (StorageInfo info : list)
        {
            if (!info.readOnly)
            {
                storages.add(info);
                rwStorages.add(info);
            }
        }

        if (Build.VERSION.SDK_INT < 19)
        {
            return;
        }

        //we are on API >= KitKat
        //add trusted read-only disks
        int count = 0;

        for (StorageInfo info : list)
        {
            if (info.readOnly && info.trustedReadOnly)
            {
                storages.add(info);
                count++;
            }
        }

        //android API has failed, or we don't have inserted sdcard
        //check for untrusted disk and pick first, hope this will be enaugh...
        if (count == 0)
        {
            for (StorageInfo info : list)
            {
                if (info.readOnly && !info.trustedReadOnly)
                {
                    storages.add(info);
                    break;
                }
            }
        }
    }

    private static void clearStorages()
    {
        if (storages == null)
        {
            storages = new ArrayList<StorageInfo>();
        }
        else
            storages.clear();

        if (rwStorages == null)
        {
            rwStorages = new ArrayList<StorageInfo>();
        }
        else
            storages.clear();
    }

    private static void addStorage(List<StorageInfo> list,  HashSet<String> paths, String path, String title)
    {
        addStorage(list, paths, path, title, false, false, false);
    }

    private static void addStorage(List<StorageInfo> list,  HashSet<String> paths, String path, String title, boolean customPath, boolean trusted, boolean isSAF)
    {
        //seems storage was not checked
        path = getPathWithoutLastSlah(path);
        path = NativeCore.nativeGetRealPath(path);


        if (path == null || paths.contains(path))
            return;

        paths.add(path);

        File emmc = new File(path);

        if (!emmc.exists())
        {
            return;
        }

        File file = new File(path, hashFile);

        //Did we checked this storage before?
        boolean checked;

        if (Build.VERSION.SDK_INT >= 19)
        {
            if (!file.exists())
            {
                if (customPath)
                {
                    checked = false;
                }
                else
                {
                    //non-rooted devices will test specific rw folder
                    File rwAccess = new File(path + kitkatRWDir + hashFile);
                    checked = rwAccess.exists();
                }
            }
            else
            {
                //some devices are rooted and have normal access
                checked = true;
            }
        }
        else
            checked = file.exists();

        if (checked)
            return;

        //has storage normal rw access?
        boolean accessed = false;

        try
        {
            file.createNewFile();
            accessed = true;
        }
        catch (Exception exc) {
        }

        if (accessed)
        {
            list.add(new StorageInfo(emmc.getAbsolutePath(), false, title));
            return;
        }

        if (Build.VERSION.SDK_INT < 19)
        {
            return;
        }

        //Ok, we use SDK >= Kitkat and path has no rw access.
        //custom path is automatically added
        if (customPath)
        {
            list.add(new StorageInfo(emmc.getAbsolutePath(), true, title, true));
            return;
        }

        //finally test specific app folder for rw access
        try
        {
            File rwAccess = new File(path + Storages.kitkatRWDir);

            if (rwAccess.exists())
            {
                //Specific folders exists, lets try to write there something
                rwAccess = new File(rwAccess, Storages.hashFile);

                if (rwAccess.exists())
                    return;

                accessed = false;

                try
                {
                    rwAccess.createNewFile();
                    accessed = true;
                }
                catch (Exception exc){
                }

                list.add(new StorageInfo(emmc.getAbsolutePath(), isSAF?false:true, title, isSAF?false:accessed));
            }
            else
            {
                if (isSAF) {
                    list.add(new StorageInfo(emmc.getAbsolutePath(), false, title, false));
                    return;
                }

                if (trusted)
                {
                    list.add(new StorageInfo(emmc.getAbsolutePath(), true, title, true));
                    return;
                }

                //This corner is for group of devices on which failed Context.getExternalFilesDirs(null) function
                //We are probably in third dimension, because detecting storages this way is crazy
                //I don't know what to say...Damn with google developer team...
                //So we make test manually for some common folders...

                File fld = new File(path, "/Android/data");

                if (fld.exists())
                {
                    list.add(new StorageInfo(emmc.getAbsolutePath(), true, title, false));
                    return;
                }

                fld = new File(path, "LOST.DIR");

                if (fld.exists())
                {
                    list.add(new StorageInfo(emmc.getAbsolutePath(), true, title, false));
                    return;
                }
            }
        }
        catch (Exception exc)
        {
            //if (uiLog.DEBUG) uiLog.log("StorageInfo kitkat : " + exc.getMessage());
        }
    }

    public static List<StorageInfo> getStorages()
    {
        if (storages == null)
        {
            getStorageList();
        }

        return storages;
    }

    public static List<StorageInfo> getWriteableStorages()
    {
        if (storages == null)
        {
            getStorageList();
        }

        return rwStorages;
    }

    /** onDrivePick - Shows all detected drives */
    public static void onDrivePick(Context context, final onDrivePickListener event)
    {
        onDrivePick(context, false, event);
    }

    /** onDrivePick - Shows all detected drives based on rwOnly (read write access) flag*/
    public static void onDrivePick(Context context, final boolean rwOnly, final onDrivePickListener event)
    {
        if (event == null)
            return;
    }

    public static void init()
    {
        userStorage1 = null;
        userStorage2 = null;
        hashFile = null;
        kitkatRWDir = "/";

        reset();
    }

    public static void reset()
    {
        storages = null;
        rwStorages = null;
    }

    /** Tests directory for write permission. Unfortunately in KitKat is not possible to test volume with File.canWrite() due FUSE daemon. Must write temp file.*/
    public static boolean isDirWriteable(File dir)
    {
        return isDirWriteable(dir, null);
    }

    /** Tests directory for write permission. Unfortunately in KitKat is not possible to test volume with File.canWrite() due FUSE daemon. Must write temp file.*/
    public static boolean isDirWriteable(File dir, String testFileName)
    {
        try
        {
            if (Build.VERSION.SDK_INT < 19)
            {
                return (dir.canRead() && dir.canWrite());
            }

            if (testFileName == null)
                testFileName = RandomStringGenerator.generateRandomString(5,RandomStringGenerator.Mode.ALPHANUMERIC);

            File f = new File(dir, testFileName);

            if (f.exists() && !f.delete())
            {
                return false;
            }

            if (f.createNewFile() && f.exists())
            {
                return f.delete();
            }
        }
        catch(Exception exc){
        }

        return false;
    }
}