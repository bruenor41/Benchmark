package benchmarkdos.doom;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import magiclib.IO.Decompress;
import magiclib.IO.Files;
import magiclib.IO.SAFSupport;
import magiclib.IO.StorageInfo;
import magiclib.IO.Storages;
import magiclib.logging.Log;

public class DOSProgramUpdater {
    private final static int ERROR_UNKNOWN = 0;
    private final static int ERROR_DELETE_OLD_DIR = 0;
    private final static int ERROR_RENAME_EXTRACTION_DIR = 1;
    private final static int ERROR_DECOMPRESS = 2;
    private final static int ERROR_CREATE_EXPORT_DIR = 3;
    private final static int ERROR_CREATE_SHARED_DIR = 4;
    private final static int ERROR_CREATE_DOSROOT_DIR = 5;
    private final static int ERROR_COPY_SAVEPADS = 6;

    public abstract interface DOSProgramUpdaterEventListener
    {
        public abstract void onFinish();
    }

    class UpdateProcessAsync extends AsyncTask<String, Integer, String>
    {
        private int newVersion;

        @Override
        protected String doInBackground(String... params) {
            LicenseType licenseType = LicenseType.get(Integer.parseInt(params[2]));

            File dosRoot = new File(params[0]);

            String updateName;
            if (dosRoot.exists()) {
                updateName = getUpdateName(true, licenseType);
            } else {
                if (!dosRoot.mkdirs()) {
                    return ERROR_CREATE_DOSROOT_DIR + "";
                }
                updateName = getUpdateName(false, licenseType);
            }

            publishProgress(updateName!=null?new Decompress().getItemsCountFromAssetsZip(context, updateName) + 1: 1);

            if (updateName != null) {
                 String result = updateVersion(updateName, params[0]);
                 if (!result.equals("OK")) {
                     return result;
                 }
                AppGlobal.saveSharedPreferences("dosversion" + licenseType.getLicenseName(), "" + newVersion);
            }

            return "OK";
        }

        @Override
        protected void onPostExecute(String result)
        {
            progressBar.setProgress(progressBar.getMax());

            if (result.equals("OK")) {
                //view.removeAllViews();
                if (event!=null) {
                    event.onFinish();
                }
            } else {
                progress_message.setTextColor(Color.RED);
                progress_message.setText("Initialization error (" + result + ")");
            }
        }

        protected void onProgressUpdate(Integer... progress)
        {
            if (firstReport) {
                progressBar.setMax(progress[0]);
                firstReport = false;
            } else {
                progressBar.setProgress(progress[0]);
            }
        }

        private String updateVersion(String updateFile, String fld) {
            File extractFolder = new File(fld);

            File extractFolderExtended = null;
            if (extractFolder.exists()) {
                String extension = "_" + (new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
                extractFolderExtended = new File(extractFolder.getAbsolutePath() + extension);
            }

            final int unzipIndex = 0;

            Decompress decompress = new Decompress();
            decompress.setOnDecompressEventListener(new Decompress.DecompressEventListener() {
                @Override
                public void onUnzip(String name, boolean isDirectory, int index) {
                    publishProgress(unzipIndex + 2);
                }

                @Override
                public void onError(String error) {
                    Log.log("Updater decompress error : " + error);
                }
            });

            if (decompress.unzipFromAssets(context, updateFile, extractFolderExtended.getAbsolutePath())) {
                if (extractFolderExtended != null) {
                    if (!Files.deleteDirectory(extractFolder)) {
                        return ERROR_DELETE_OLD_DIR + "";
                    }
                    if (!extractFolderExtended.renameTo(extractFolder)) {
                        return ERROR_RENAME_EXTRACTION_DIR + "";
                    }
                }

                return "OK";
            }

            return ERROR_DECOMPRESS + "";
        }

        private String getUpdateName(boolean checkVersions, LicenseType licenseType) {
            String zipName = getZipFileName(licenseType.index());

            if (zipName == null) {
                return null;
            }

            if (!checkVersions) {
                return zipName;
            }

            newVersion = Integer.parseInt(zipName.split("[_.]")[1]);
            String savedVersion = AppGlobal.getSharedString(context, "dosversion" + licenseType.getLicenseName());

            if (savedVersion == null || savedVersion.trim().equals("")) {
                return zipName;
            }

            int iSavedVersion = Integer.parseInt(savedVersion);

            return (iSavedVersion != newVersion && iSavedVersion < newVersion)?zipName:null;
        }

        private String getZipFileName(int licenseType) {
            String zipName = "t" + licenseType + "_";

            String [] list;
            try {
                list = context.getAssets().list("");
                for(String fileName : list) {
                    if (fileName.startsWith(zipName) && fileName.endsWith(".zip")) {
                        return fileName;
                    }
                }
            } catch (IOException e) {
                return null;
            }

            return null;
        }
    }

    private DOSProgramUpdaterEventListener event;
    private Context context;
    private RelativeLayout view;
    private CircleProgressBar progressBar;
    private TextView progress_message;
    private boolean firstReport;

    public DOSProgramUpdater(Context context, RelativeLayout view) {
        this.context = context;
        this.view = view;
    }

    public void setOnDOSProgramUpdaterEventListener(DOSProgramUpdaterEventListener event) {
        this.event = event;
    }

    public void start() {
        firstReport = true;

        SAFSupport.clear();
        Storages.init();

        LicenseType licenseType = LicenseType.getCurrentLicense();

        Activity activity = (Activity) context;
        AppGlobal.currentGameDOSSHAREDPath = new File(AppGlobal.currentGameDOSROOTPath, "SHARED/").getAbsolutePath();
        File dosRoot = new File(AppGlobal.currentGameDOSROOTPath, licenseType.getLicenseName());
        AppGlobal.currentGameDOSROOTPath = dosRoot.getAbsolutePath();

        activity.getLayoutInflater().inflate(R.layout.progress, view);
        activity.setContentView(view);

        progressBar = (CircleProgressBar) view.findViewById(R.id.custom_progressBar);
        progressBar.setColor(Color.parseColor("#00FF00"));
        progressBar.setMin(0);

        progress_message = (TextView) view.findViewById(R.id.progress_message);
        progress_message.setText("Loading...");
        progress_message.setTextColor(Color.GREEN);

        UpdateProcessAsync updater = new UpdateProcessAsync();
        updater.execute(AppGlobal.currentGameDOSROOTPath, AppGlobal.currentGameDOSSHAREDPath, licenseType.index() + "");
    }

    public void dispose() {

    }
}
