package eu.opentransportnet.cityreport.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;

/**
 * Provides information about current network state
 *
 * @author Kristaps Krumins
 */
public class NetworkReceiver extends BroadcastReceiver {
    public static final String WIFI = "Wi-Fi";
    public static final String ANY = "Any";

    // The user's current network preference setting.
    public static String sPref = null;


    public NetworkReceiver(Context context) {
        // Gets the user's network preference settings
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieves a string value for the preferences. The second parameter
        // is the default value to use if a preference value is not found.
        sPref = sharedPrefs.getString("listPref", "Any");
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        ConnectivityManager conn =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = conn.getActiveNetworkInfo();

        // If the userpref is Wi-Fi only, checks to see if the device has a Wi-Fi connection.
        if (WIFI.equals(sPref) && networkInfo != null
                && networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {

            // Checks if the setting is ANY network and there is a network connection
        } else if (ANY.equals(sPref) && networkInfo != null) {

            // There is no network connection (mobile or Wi-Fi), or because the pref setting is
            // WIFI, and there is no Wi-Fi connection.
        } else {
        }

    }
}
