package eu.opentransportnet.cityreport.network;

import android.content.Context;

import eu.opentransportnet.cityreport.utils.Const;
import eu.opentransportnet.cityreport.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author Kristaps Krumins
 */
public class UploadTask {
    private static UploadTask mInstance;
    private static Context sAppCtx;

    private boolean mUploadStarted = false;

    private UploadTask(Context ctx) {
        sAppCtx = ctx.getApplicationContext();
    }

    public static synchronized UploadTask getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new UploadTask(context);
        }
        return mInstance;
    }

    /**
     * Scheduled upload every minute
     */
    public void startScheduledUpload() {
        if (!mUploadStarted) {
            // Sets a new Timer
            Timer timer = new Timer();
            // Initializes the TimerTask's job
            TimerTask timerTask = initializeScheduledUpload();
            // Schedules the timer, after the first 1000ms theTimerTask will run every minute
            timer.schedule(timerTask, 1000, 60000);
            mUploadStarted = true;
        }
    }

    /**
     * Initialize scheduled upload
     */
    private TimerTask initializeScheduledUpload() {
        TimerTask timerTask = new TimerTask() {
            public void run() {
                if (Utils.isConnected(sAppCtx)) {
                    uploadData();
                }

            }
        };
        return timerTask;
    }

    /**
     * Upload data to server
     */
    private void uploadData() {
        String filePath;
        filePath = Const.STORAGE_PATH_REPORT;

        File dir = new File(sAppCtx.getFilesDir() + "/" + filePath);
        File[] directoryListing = dir.listFiles();
        if (directoryListing != null) {
            JSONParser parser = new JSONParser();
            for (File file : directoryListing) {
                Object obj;
                try {
                    obj = parser.parse(new FileReader(file));
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                } catch (ParseException e) {
                    e.printStackTrace();
                    continue;
                }

                if (obj == null) {
                    continue;
                }

                String jsonBodyString = ((org.json.simple.JSONObject) obj).toJSONString();
                JSONObject jsonBody;
                try {
                    jsonBody = new JSONObject(jsonBodyString);
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }

                String fileName = file.getName();
                int pos = fileName.lastIndexOf(".");
                if (pos > 0) {
                    fileName = fileName.substring(0, pos);
                }

                Requests.reportIssue(sAppCtx, jsonBody, false, file.getAbsolutePath());
            }
        }
    }
}

