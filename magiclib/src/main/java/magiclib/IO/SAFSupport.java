
package magiclib.IO;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.storage.StorageManager;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.text.TextUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;

import magiclib.Global;

public class SAFSupport
{
    /**Enable SAF support*/
    public static boolean enabled = true;//TODO - add to global settings

    /** Real path of the SDCard Uri*/
    public static String sdcardUriRealPath;

    /** Helper - Real path length*/
    private static int realPathSize;

    private static String sdcardUriStringFull = null;
    public static String sdcardUriStringShort = null;

    private static StringBuilder builder1;
    private static StringBuilder builder2;
    public static ContentResolver resolver = null;
    private static HashMap<String, Uri> pathsCache;
    final static String rw ="rw";
    final static String rwt ="rwt";

    public static boolean set(Uri uri)
    {
        clear();

        /*if (!Global.isDebuggable)
            return false;*/

        try {
            if (uri == null || uri.toString().equals("")) {
                clear();
                return false;
            }

            DocumentFile document = DocumentFile.fromTreeUri(Global.context, uri);

            if (!document.canWrite()) {
                //uiMessage.shortInfo("You gave permissions for writing on sdcard, but the system removed it. You must it again.");
                return false;
            }

            sdcardUriRealPath = getFullPathFromTreeUri(uri);

            //toto urobim tak, ze do globalnych nastaveni pridam moznost aby uzivatel opravil cestu. Tym sa vyhnem problemom ked pride
            //novy android a uz nebude fungovat getFullPathFromTreeUri
            if (sdcardUriRealPath == null || sdcardUriRealPath.equals("") || !(new File(sdcardUriRealPath).exists())) {
                //uiMessage.info("Failed to detect real path to sdcard from given permissions.");
                return false;
            }

            realPathSize = sdcardUriRealPath.length();
            builder1 = new StringBuilder();
            builder2 = new StringBuilder();
            pathsCache = new HashMap<String, Uri>();

            sdcardUriStringFull = uri.toString() + "/document" + uri.toString().substring(uri.toString().lastIndexOf("/"));
            sdcardUriStringShort = uri.toString();
            enabled = true;

            return true;
        }
        catch (Exception e) {
            //uiLog.logError("SAFSupport.set : " + ((e == null)?"":e.getMessage()));
        }

        return false;
    }

    public static void clear()
    {
        enabled = false;
        sdcardUriRealPath = null;
        realPathSize = 0;
        builder1 = null;
        builder2 = null;
        sdcardUriStringFull = null;
        sdcardUriStringShort = null;
        resolver = null;
        if (pathsCache!=null){
            pathsCache.clear();
            pathsCache = null;
        }
    }

    private static void buildUri(String path, boolean fullUri)
    {
        //long startT1 = System.nanoTime();

        builder2.setLength(0);
        int i = realPathSize + 1;
        int l = path.length();
        while (i<l) {
            builder2.append(path.charAt(i));
            i++;
        }

        builder1.setLength(0);
        if (fullUri) {
            builder1.append(sdcardUriStringFull);
        } else {
            builder1.append(sdcardUriStringShort);
        }

        if (builder2.length()>0) {
            builder1.append(Uri.encode(builder2.insert(0,'/').toString()));
        }

        //long startT2 = System.nanoTime();
        //double diff = (((double)(startT2 - startT1))/1000000);

        //if (diff > 1)
        //uiLog.log("JavaGetFD fileExists : buildUri time: total : " + diff);
    }

    private static String buildUriWithoutLast(String path, boolean fullUri)
    {
        int index = path.lastIndexOf("/");
        String relativePath = (index<realPathSize+1)?"":path.substring(realPathSize + 1, index);
        String lastName = path.substring(index+1);

        builder1.setLength(0);
        if (fullUri) {
            builder1.append(sdcardUriStringFull);
        } else {
            builder1.append(sdcardUriStringShort);
        }
        //builder.append(Uri.encode(relativePath));

        if (!relativePath.equals("")) {
            builder1.append(Uri.encode("/" + relativePath));
        }

        return lastName;
    }

    public static boolean isEnabled()
    {
        return (enabled) && (Build.VERSION.SDK_INT >= 21);
    }

    public static int getFD(String path, boolean modeW, int fileExists)
    {
        //uiLog.log("JavaGetFD path: " + path + ", fileExists : " + fileExists + ", modeR"+ modeR + ", modeW" + modeW+ ", modePlus" + modePlus);
        try
        {
            switch(fileExists) {
                case 1: {
                    Uri uri = pathsCache.get(path);
                    if (uri == null) {
                        buildUri(path, true);
                        uri=Uri.parse(builder1.toString());
                        pathsCache.put(path, uri);
                    }
                    return resolver.openFileDescriptor(uri, modeW ? rwt : rw).detachFd();
                }
                case 0: {
                    String fileName = buildUriWithoutLast(path, true);
                    Uri uri = Uri.parse(builder1.toString());
                    uri = DocumentsContract.createDocument(resolver, uri, "image", fileName);
                    return resolver.openFileDescriptor(uri, modeW ? rwt : rw).detachFd();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    public static int mkdir(String path, int checkDir)
    {
        //uiLog.log("JavaMkdir : " + path + ", checkDir=" + checkDir);
        try {
            switch (checkDir)
            {
                case -2:
                case 1:{
                    String name = buildUriWithoutLast(path, true);
                    Uri uri = Uri.parse(builder1.toString());

                    if (checkDir == -2 && !dirExists(uri)) {
                        return -1;
                    }

                    if (DocumentsContract.createDocument(Global.context.getContentResolver(),
                            uri,
                            DocumentsContract.Document.MIME_TYPE_DIR,
                            name) != null) {
                        return 0;
                    }
                }
                default: {
                    buildUri(path, true);
                    Uri uri = Uri.parse(builder1.toString());
                    if (dirExists(uri))
                        return -1;

                    String name = buildUriWithoutLast(path, true);
                    uri = Uri.parse(builder1.toString());
                    if (!dirExists(uri))
                        return -1;

                    if (DocumentsContract.createDocument(Global.context.getContentResolver(),
                            uri,
                            DocumentsContract.Document.MIME_TYPE_DIR,
                            name) != null) {
                        return 0;
                    }
                }
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    public static int rmdir(String path)
    {
        try {
			buildUri(path, true);
			Uri uri = Uri.parse(builder1.toString());

			if (!isDirectoryEmpty(uri))
				return -1;

			if (DocumentsContract.deleteDocument(resolver,  uri)) {
				return 0;
			}
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    public static int fileExists(String path, int param)
    {
        Uri uri = pathsCache.get(path);
        if (uri == null) {
            buildUri(path, true);
            uri = Uri.parse(builder1.toString());
            pathsCache.put(path, uri);
        }

        if (param == -1) {
            return exists(uri)?1:0;
        }

        return fileExists(uri)?1:0;
    }

    public static int delete(String path, int fileExists)
    {
        try {
			Uri uri = pathsCache.get(path);
			if (uri == null) {
				buildUri(path, true);
				uri = Uri.parse(builder1.toString());
				pathsCache.put(path, uri);
			}

			if (fileExists==-1 && !fileExists(uri))
				return -1;

			if (DocumentsContract.deleteDocument(resolver, uri)) {
				return 0;
			}
        } catch(Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static int rename(String oldName, String newName)
    {
        //uiLog.log("rename from : " + oldName + ", to : " + newName);
        try {
            File o = new File(oldName);
            File n = new File(newName);

            String oDir = o.getParent();
            String nDir = n.getParent();

            //rename file or folder
            if (oDir.equals(nDir)) {
                buildUri(oldName, true);
                Uri uri = Uri.parse(builder1.toString());
                return (DocumentsContract.renameDocument(resolver, uri, n.getName())==null?-1:0);
            }

            //move file
            if (!o.isFile()) {
                return -1;
            }

            String fileName = buildUriWithoutLast(newName, true);
            Uri uri = DocumentsContract.createDocument(resolver, Uri.parse(builder1.toString()), "image", fileName);

            if (copyFile(o, uri) == 0)
            {
                return delete(oldName, 1);
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return -1;
    }

    private static int copyFile(File source, Uri uri)
    {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        int result;

        try {
            inStream = new FileInputStream(source);
            outStream = resolver.openOutputStream(uri);

            if (outStream != null) {
                byte[] buffer = new byte[4096]; // MAGIC_NUMBER
                int bytesRead;
                while ((bytesRead = inStream.read(buffer)) != -1) {
                    outStream.write(buffer, 0, bytesRead);
                }
            }

            result = 0;
        }
        catch(Exception e)
        {
            result = -1;
        }
        finally {
            try {
                inStream.close();
            }
            catch (Exception e) {
                // ignore exception
            }
            try {
                outStream.close();
            }
            catch (Exception e) {
                // ignore exception
            }
        }

        return result;
    }

    public static String getFullPathFromTreeUri(final Uri treeUri)
    {
        try {
            if (treeUri == null) {
                return null;
            }
            String volumePath = getVolumePath(getVolumeIdFromTreeUri(treeUri));
            if (volumePath == null) {
                return File.separator;
            }
            if (volumePath.endsWith(File.separator)) {
                volumePath = volumePath.substring(0, volumePath.length() - 1);
            }

            String documentPath = getDocumentPathFromTreeUri(treeUri);
            if (documentPath.endsWith(File.separator)) {
                documentPath = documentPath.substring(0, documentPath.length() - 1);
            }

            if (documentPath != null && documentPath.length() > 0) {
                if (documentPath.startsWith(File.separator)) {
                    return volumePath + documentPath;
                } else {
                    return volumePath + File.separator + documentPath;
                }
            } else {
                return volumePath;
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) {
            return split[1];
        }
        else {
            return File.separator;
        }
    }

    private static String getVolumePath(final String volumeId) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return null;
        }

        try {
            StorageManager mStorageManager =
                    (StorageManager) Global.context.getSystemService(Context.STORAGE_SERVICE);

            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");

            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                // primary volume?
                if (primary.booleanValue() && "primary".equals(volumeId)) {
                    return (String) getPath.invoke(storageVolumeElement);
                }

                // other volumes?
                if (uuid != null) {
                    if (uuid.equals(volumeId)) {
                        return (String) getPath.invoke(storageVolumeElement);
                    }
                }
            }

            // not found.
            return null;
        }
        catch (Exception ex) {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");

        if (split.length > 0) {
            return split[0];
        }
        else {
            return null;
        }
    }

    private static boolean exists(Uri uri) {
        Cursor c = null;
        try {
            c = resolver.query(uri, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID}, null, null, null);
            return c.getCount() > 0;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        } finally {
            closeQuietly(c);
        }
    }

    private static boolean fileExists(Uri uri) {
        Cursor c = null;
        boolean result = false;
        try {
            c = resolver.query(uri, new String[]{DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                String mime = c.getString(0);

                if (!TextUtils.isEmpty(mime) && !mime.equals(DocumentsContract.Document.MIME_TYPE_DIR))
                    result = true;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        } finally {
            closeQuietly(c);
        }

        return result;
    }

    private static boolean dirExists(Uri uri) {
        Cursor c = null;
        boolean result = false;
        try {
            c = resolver.query(uri, new String[]{DocumentsContract.Document.COLUMN_MIME_TYPE}, null, null, null);
            if (c.moveToFirst() && !c.isNull(0)) {
                String mime = c.getString(0);

                if (!TextUtils.isEmpty(mime) && mime.equals(DocumentsContract.Document.MIME_TYPE_DIR))
                    result = true;
            }
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        } finally {
            closeQuietly(c);
        }

        return result;
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception ignored) {
            }
        }
    }

    private static boolean isDirectoryEmpty(Uri uri)
    {
        final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri,
                DocumentsContract.getDocumentId(uri));

        Cursor c = null;
        boolean result = false;
        try {
            c = resolver.query(childrenUri, new String[]{
                    DocumentsContract.Document.COLUMN_DOCUMENT_ID}, null, null, null);
            result = c.getCount()==0;
        } catch (Exception e) {
            //e.printStackTrace();
            return false;
        } finally {
            closeQuietly(c);
        }

        return  result;
    }
}
