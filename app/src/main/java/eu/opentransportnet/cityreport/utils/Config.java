package eu.opentransportnet.cityreport.utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Kristaps Krumins
 */
public class Config {
    private static Config mInstance;

    private Properties mProperties;

    private Config(Context ctx) {
        mProperties = new Properties();

        //access to the folder ‘assets’
        AssetManager am = ctx.getAssets();
        InputStream inputStream;

        try {
            inputStream = am.open("config.properties");
            mProperties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static synchronized Config getInstance(Context ctx) {
        if (mInstance == null) {
            mInstance = new Config(ctx);
        }

        return mInstance;
    }

    public String getDebugHostname() {
        return mProperties.getProperty("debug_hostname");
    }

    public String getReleaseHostname() {
        return mProperties.getProperty("release_hostname");
    }

    public boolean isRelease() {
        if (mProperties.getProperty("release").equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean logMessages() {
        if (mProperties.getProperty("log_messages").equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isEncryption() {
        if (mProperties.getProperty("service_encryption").equals("1")) {
            return true;
        } else {
            return false;
        }
    }
}
