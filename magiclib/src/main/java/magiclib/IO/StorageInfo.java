
package magiclib.IO;

import android.os.Build;
import android.os.StatFs;

import java.io.File;

public class StorageInfo
{
    public String diskTitle;
    public String path;
    public double diskSize = 0;
    public boolean readOnly;
    public boolean trustedReadOnly;

    public StorageInfo(String path, boolean readOnly, String diskTitle)
    {
        this(path, readOnly, diskTitle, false);
    }

    public StorageInfo(String path, boolean readOnly, String diskTitle, boolean trustedReadOnly)
    {
        this.path = path;
        this.diskTitle = diskTitle;
        this.readOnly = readOnly;
        this.trustedReadOnly = trustedReadOnly;

        calcFreeSpace();
    }

    private void calcFreeSpace()
    {
        try {
            long bytes;
            if (Build.VERSION.SDK_INT > 8) {
                bytes = new File(path).getTotalSpace();
            } else {
                StatFs stat = new StatFs(new File(path).getPath());
                bytes = stat.getBlockSize() * stat.getBlockCount();
            }

            if (bytes > 0)
                diskSize = (double) (bytes / 107374182) / 10;
        } catch(Exception e) {
            diskSize = 0;
        }
    }
}