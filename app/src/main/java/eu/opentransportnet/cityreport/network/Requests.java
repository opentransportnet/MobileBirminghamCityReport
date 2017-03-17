package eu.opentransportnet.cityreport.network;

import android.app.DownloadManager;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import eu.opentransportnet.cityreport.R;
import eu.opentransportnet.cityreport.activities.MainActivity;
import eu.opentransportnet.cityreport.activities.ReportDetailsActivity;
import eu.opentransportnet.cityreport.interfaces.VolleyRequestListener;
import eu.opentransportnet.cityreport.utils.Const;
import eu.opentransportnet.cityreport.utils.OtnCrypto;
import eu.opentransportnet.cityreport.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Date;

/**
 * @author Kristaps Krumins
 */
public class Requests {
    public static final String PATH_REGISTER_USER = "/platform/users/addUser";
    public static final String PATH_REPORT_ISSUE = "/platform/issues/issueReport";
    public static final String PATH_DELETE_USER = "/platform/users/deleteUserContent";
    public static final String PATH_LOAD_ISSUE = "/platform/issues/loadIssue";
    public static final String PATH_DELETE_ISSUE = "/platform/issues/deleteIssue";

    private static final String LOG_TAG = "Requests";

    /**
     * Makes a request to given url
     *
     * @param ctx            The context
     * @param showErrorToast If true then error toast will be shown when error is detected
     * @param url            The URL for request
     * @param jsonBody       The JSONObject to be sent
     * @param listener       The listener for request response
     * @param tag            The request tag, used for canceling request
     * @param method         The request method. Method codes are in Request.Method
     * @param useEncryption  If true, uses OTN encryption
     * @return If request has been added to request queue returns {@code true},
     * otherwise{@code false}
     */
    public static boolean sendRequest(final Context ctx,
                                      final boolean showErrorToast,
                                      String url, JSONObject jsonBody,
                                      final VolleyRequestListener<JSONObject> listener,
                                      String tag,
                                      int method,
                                      boolean useEncryption) {

        if (!Utils.isConnected(ctx)) {
            Utils.logD(LOG_TAG, "No Internet connectivity");

            if (showErrorToast) {
                Utils.showToastAtTop(ctx, ctx.getString(R.string.network_unavailable));
            }

            return false;
        } else if (method == Request.Method.POST && jsonBody == null) {
            Utils.logD(LOG_TAG, "JSONObject is 'null'");

            if (showErrorToast) {
                Utils.showToastAtTop(ctx, ctx.getString(R.string.something_went_wrong));
            }

            return false;
        }

        String requestMethod = "unsupported:" + method;

        switch (method){
            case Request.Method.POST:
                requestMethod = "POST";
                break;
            case Request.Method.GET:
                requestMethod = "GET";
                break;
        }

        Utils.logD(LOG_TAG, requestMethod + " request to url:" + url);

        if (jsonBody != null) {
            Utils.logD(LOG_TAG, "JSON data:" + jsonBody.toString());
        }

        Request request;

        if (useEncryption == false) {
            request = new JsonObjectRequest(method, url, jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Utils.logD(LOG_TAG, "JSON response:" + response);
                            listener.onResult(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (showErrorToast) {
                                Utils.showToastAtTop(ctx, ctx.getString(R.string.something_went_wrong));
                            }

                            if (error != null && error.networkResponse != null) {
                                Utils.logD(LOG_TAG, "Error response. Response code " + error
                                        .networkResponse.statusCode);
                            }

                            listener.onError(null);
                        }
                    });
        } else {
            final String encryptedBody = OtnCrypto.encrypt(jsonBody.toString());
            Utils.logD(LOG_TAG, "encrypted JSON data:" + encryptedBody);

            request = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String encryptedResponse) {
                            String responseString = OtnCrypto.decrypt(encryptedResponse);
                            JSONObject response;

                            try {
                                response = new JSONObject(responseString);
                                Utils.logD(LOG_TAG, "JSON response:" + response);
                                listener.onResult(response);
                                return;
                            } catch (JSONException e) {
                                e.printStackTrace();

                                if (showErrorToast) {
                                    Utils.showToastAtTop(ctx, ctx.getString(R.string.something_went_wrong));
                                }

                                Utils.logD(LOG_TAG, "Decrypted message is not in JSON format");
                                listener.onError(null);
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if (showErrorToast) {
                                Utils.showToastAtTop(ctx, ctx.getString(R.string.something_went_wrong));
                            }

                            if (error != null && error.networkResponse != null) {
                                Utils.logD(LOG_TAG, "Error response. Response code " + error
                                        .networkResponse.statusCode);
                            }

                            listener.onError(null);
                        }
                    }) {

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return encryptedBody.getBytes();
                }

                @Override
                public String getBodyContentType() {
                    return "text/plain";
                }
            };
        }

        // Adds request to request queue
        RequestQueueSingleton.getInstance(ctx.getApplicationContext())
                .addToRequestQueue(request, tag);

        return true;
    }

    /**
     * Sends JSONObject to server and does not show error messages (toasts)
     *
     * @param ctx      The context
     * @param url      The URL for request
     * @param jsonBody The JSONObject to be sent
     * @param listener The listener for request response
     * @param tag      The request tag, used for canceling request
     * @return If request has been added to request queue returns {@code true},
     * otherwise{@code false}
     */
    public static boolean sendPostRequest(Context ctx, String url, JSONObject jsonBody,
                                          final VolleyRequestListener<JSONObject> listener,
                                          String tag) {
        return sendRequest(ctx, false, url, jsonBody, listener, tag, Request.Method.POST, Utils.isEncryption());
    }

    /**
     * Registers user
     *
     * @param ctx       context
     * @param userEmail user email
     * @return if everything successful
     */
    public static boolean registerUser(Context ctx, String userEmail,
                                       final VolleyRequestListener<JSONObject> listener) {
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userId", userEmail);
            jsonBody.put("appId", Const.APPLICATION_ID);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        String url = "http://" + Utils.getHostname() + Utils.getUrlPathStart() + PATH_REGISTER_USER;

        return sendPostRequest(ctx, url, jsonBody, listener, null);
    }

    /**
     * Uploads report issue to server
     *
     * @param ctx      The context
     * @param jsonBody The JSON body for request
     * @return If request has been added to request queue returns {@code true},
     * otherwise{@code false}
     */
    public static boolean reportIssue(final Context ctx, final JSONObject jsonBody,
                                      final boolean save, final String filePath) {
        String url = "http://" + Utils.getHostname() + Utils.getUrlPathStart() + PATH_REPORT_ISSUE;

        boolean requestSent = sendPostRequest(ctx, url, jsonBody,
                new VolleyRequestListener<JSONObject>() {
                    @Override
                    public void onResult(JSONObject object) {
                        int rc = Utils.getResponseCode(object);

                        if (rc != 0) {
                            // Error
                            if (save) {
                                Date date = new Date();
                                ReportDetailsActivity.saveReportLocally(ctx, date, jsonBody);
                            }
                        } else {
                            // Report issue saved on server
                            if (save == false) {
                                File file = new File(filePath);
                                file.delete();
                            }
                        }
                    }

                    @Override
                    public void onError(JSONObject object) {
                        if (save) {
                            Date date = new Date();
                            ReportDetailsActivity.saveReportLocally(ctx, date, jsonBody);
                        }
                    }
                }, null);

        if (!requestSent) {
            return false;
        }

        return true;
    }

    public static boolean reportIssue(final Context ctx, final JSONObject jsonBody) {
        return reportIssue(ctx, jsonBody, true, null);
    }

    /**
     * Delete users reported issue. Default request error toasts enabled.
     *
     * @param ctx      The context
     * @param issueId  The POI ID
     * @param listener The listener for request response
     * @param tag      The request tag, used for canceling request
     * @return If request has been added to request queue returns {@code true},
     * otherwise{@code false}
     */
    public static boolean deleteIssue(Context ctx, int issueId,
                                      final VolleyRequestListener<JSONObject> listener,
                                      String tag) {
        String userEmail = Utils.getHashedUserEmail(ctx);

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("userId", userEmail);
            jsonBody.put("issueId", issueId);
            jsonBody.put("appId", Const.APPLICATION_ID);
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

        String url = "http://" + Utils.getHostname() + Utils.getUrlPathStart() + PATH_DELETE_ISSUE;

        return sendRequest(ctx, true, url, jsonBody, listener, tag, Request.Method.POST, Utils.isEncryption());
    }

    /**
     * Centers map by UK postal code. Default request error toasts disabled.
     *
     * @param ctx        The context
     * @param postalCode The UK postal code. For example, OX49 5NU.
     * @param listener   The listener for request response
     * @param tag        The request tag, used for canceling request
     * @return If request has been added to request queue returns {@code true},
     * otherwise{@code false}
     */
    public static boolean centerMapByPostalCode(Context ctx, String postalCode,
                                                final VolleyRequestListener<JSONObject> listener,
                                                String tag) {
        String url = "https://api.postcodes.io/postcodes/" + postalCode;

        VolleyRequestListener<JSONObject> responseListener = new VolleyRequestListener<JSONObject>() {
            @Override
            public void onResult(JSONObject object) {
                try {
                    JSONObject result = object.getJSONObject("result");
                    double lat = result.getDouble("latitude");
                    double lng = result.getDouble("longitude");

                    MainActivity.getRouteRecorder().getWebView().loadUrl("javascript:centerMap(" +
                            lat + ", " + lng + ")");
                    listener.onResult(object);
                } catch (JSONException e) {
                    listener.onError(object);
                }
            }

            @Override
            public void onError(JSONObject object) {
                listener.onError(object);
            }
        };

        return sendRequest(ctx, false, url, null, responseListener, tag, Request.Method.GET, false);
    }
}
