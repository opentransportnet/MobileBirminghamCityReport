package eu.opentransportnet.cityreport.utils;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AlertDialog;

import eu.opentransportnet.cityreport.R;
import eu.opentransportnet.cityreport.activities.LoginActivity;
import eu.opentransportnet.cityreport.models.User;

import java.io.File;

/**
 * @author Kristaps Krumins
 */
public class SessionManager {
    /**
     * All Shared Preferences Keys
     */
    public static final String KEY_FIRST_NAME = "first_name";
    public static final String KEY_LAST_NAME = "last_name";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_HASHED_EMAIL = "hashed_email";
    public static final String KEY_ENCODED_PHOTO = "encoded_photo";
    public static final String KEY_PHOTO_URL = "photo_url";
    public static final String KEY_DISCLAIMER = "disclaimer";
    public static final String KEY_DISCLAIMER_V = "disclaimer_v";

    private static final String IS_LOGGED_IN = "is_logged_in";

    // Shared pref file name
    private static final String PREF_NAME = "User";

    private SharedPreferences mPref;
    private Editor mEditor;
    private Context mContext;
    // Shared pref mode
    private int PRIVATE_MODE = 0;
    private SimpleCrypto mSimpleCrypto = null;

    public SessionManager(Context context) {
        mContext = context;
        mPref = mContext.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        mEditor = mPref.edit();
        mSimpleCrypto = new SimpleCrypto();
    }

    /**
     * Create login session
     */
    public boolean createLoginSession(User user) {
        mEditor.putBoolean(IS_LOGGED_IN, true);
        mEditor.putString(KEY_FIRST_NAME, mSimpleCrypto.encryptIt(user.getFirstName()));
        mEditor.putString(KEY_LAST_NAME, mSimpleCrypto.encryptIt(user.getLastName()));
        mEditor.putString(KEY_EMAIL, mSimpleCrypto.encryptIt(user.getEmail()));
        mEditor.putString(KEY_HASHED_EMAIL, mSimpleCrypto.encryptIt(user.getHashedEmail()));
        mEditor.putString(KEY_ENCODED_PHOTO, mSimpleCrypto.encryptIt(user.getEncodedPhoto()));
        mEditor.putString(KEY_PHOTO_URL, mSimpleCrypto.encryptIt(user.getPhotoUrl()));
        mEditor.putBoolean(KEY_DISCLAIMER, user.getDisclaimer());
        mEditor.putInt(KEY_DISCLAIMER_V, user.getDisclaimerV());
        return mEditor.commit();
    }

    /**
     * Update login session
     */
    public boolean updateLoginSession(User user){
        mEditor.putString(KEY_FIRST_NAME, mSimpleCrypto.encryptIt(user.getFirstName()));
        mEditor.putString(KEY_LAST_NAME, mSimpleCrypto.encryptIt(user.getLastName()));
        mEditor.putString(KEY_EMAIL, mSimpleCrypto.encryptIt(user.getEmail()));
        mEditor.putString(KEY_HASHED_EMAIL, mSimpleCrypto.encryptIt(user.getHashedEmail()));
        mEditor.putString(KEY_ENCODED_PHOTO, mSimpleCrypto.encryptIt(user.getEncodedPhoto()));
        mEditor.putString(KEY_PHOTO_URL, mSimpleCrypto.encryptIt(user.getPhotoUrl()));
        mEditor.putBoolean(KEY_DISCLAIMER, user.getDisclaimer());
        mEditor.putInt(KEY_DISCLAIMER_V, user.getDisclaimerV());
        return mEditor.commit();
    }

    /**
     * Get stored session data
     */
    public User getUser() {
        User user = new User(mContext);
        user.setFirstName(mSimpleCrypto.decryptIt(mPref.getString(KEY_FIRST_NAME, "")));
        user.setLastName(mSimpleCrypto.decryptIt(mPref.getString(KEY_LAST_NAME, "")));
        user.setEmail(mSimpleCrypto.decryptIt(mPref.getString(KEY_EMAIL, "")));
        user.setHashedEmail(mSimpleCrypto.decryptIt(mPref.getString(KEY_HASHED_EMAIL, "")));
        user.setEncodedPhoto(mSimpleCrypto.decryptIt(mPref.getString(KEY_ENCODED_PHOTO, "")));
        user.setPhotoUrl(mSimpleCrypto.decryptIt(mPref.getString(KEY_PHOTO_URL, "")));
        user.setDisclaimer(mPref.getBoolean(KEY_DISCLAIMER, false));
        user.setDisclaimerv(mPref.getInt(KEY_DISCLAIMER_V, 0));
        return user;
    }

    /**
     * Clear user session
     */
    public void clearUserSession() {
        // Clearing all data from Shared Preferences
        mEditor.clear();
        mEditor.commit();
    }

    /**
     * Logout user
     */
    public void logoutUser() {
        File reportDir = new File(mContext.getFilesDir() + "/" + Const.STORAGE_PATH_REPORT);
        File[] reports = reportDir.listFiles();

        if (reports.length > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(mContext.getString(R.string.delete_local_files))
                    .setMessage(mContext.getString(R.string.delete_local_files_message))
                    .setPositiveButton(mContext.getString(R.string.yes),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Utils.deleteAllLocalFiles(mContext);
                                    forceLogoutUser();
                                }
                            })
                    .setNegativeButton(mContext.getString(R.string.no),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
            builder.create().show();
        } else {
            forceLogoutUser();
        }
    }

    /**
     * Force logout user
     */
    public void forceLogoutUser() {
        clearUserSession();

        ((Activity) mContext).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        Intent loginActivity = new Intent(mContext, LoginActivity.class);
        loginActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mContext.startActivity(loginActivity);
    }

    /**
     * Save disclaimer
     * @param dr if user read disclaimer
     * @param v version disclaimer last read
     * @return if everything was successful
     */
    public boolean saveDisclaimer(boolean dr,int v) {
        mEditor.putBoolean(KEY_DISCLAIMER, dr);
        mEditor.putInt(KEY_DISCLAIMER_V, v);
        return mEditor.commit();
    }


    /**
     * Quick check for login
     * Get Login State
     **/
    public boolean isLoggedIn() {
        return mPref.getBoolean(IS_LOGGED_IN, false);
    }
}
