package eu.opentransportnet.cityreport.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import eu.opentransportnet.cityreport.R;
import eu.opentransportnet.cityreport.interfaces.VolleyRequestListener;
import eu.opentransportnet.cityreport.models.BaseActivity;
import eu.opentransportnet.cityreport.network.Requests;
import eu.opentransportnet.cityreport.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Ilmārs Svilsts
 */
public class ReportInfoActivity extends BaseActivity implements View.OnClickListener {
    String mJson = "json";
    String mLat = "lat";
    String mLng = "lng";
    String mLoadUrl = "file:///android_asset/www/report.html";

    private static final String LOG_TAG = "ReportInfoActivity";

    public double mLatitude;
    public double mLongitude;

    private Context mContext;
    private int mIssueId = -1;
    private RelativeLayout mLoadingPanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = this;
        setContentView(R.layout.activity_report_details);
        mLoadingPanel = (RelativeLayout) findViewById(R.id.loading_panel);
        initToolbarBackBtn();
        Typeface tf = getTypeFace();
        pictureinit();

        Button deleteButton = (Button) findViewById(R.id.delete_button);
        deleteButton.setVisibility(View.VISIBLE);
        deleteButton.setTypeface(tf);
        deleteButton.setOnClickListener(this);
        ImageButton closeButton = (ImageButton) findViewById(R.id.closeButton);
        closeButton.setVisibility(View.GONE);

        Intent intent = getIntent();
        String json = intent.getStringExtra(mJson);

        mLatitude = intent.getDoubleExtra(mLat, 200);
        mLongitude = intent.getDoubleExtra(mLng, 200);

        try {
            JSONObject obj = new JSONObject(json);
            infoinit(obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        final WebView webView = (WebView) findViewById(R.id.webView);
        WebSettings mWebSettings = webView.getSettings();
        // Enable Javascript
        mWebSettings.setJavaScriptEnabled(true);
        // Force links and redirects to open in the WebView instead of in a browser
        webView.setWebChromeClient(new WebChromeClient() {
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }

            //Enable console.log() from JavaScript
            public boolean onConsoleMessage(ConsoleMessage cm) {
                Utils.logD(LOG_TAG, cm.message() + " -- From line "
                        + cm.lineNumber() + " of "
                        + cm.sourceId());
                return true;
            }
        });
        webView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        webView.loadUrl(mLoadUrl);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                webView.loadUrl("javascript:showLoc(" + mLatitude + "," + mLongitude + ")");
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        pictureinit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.delete_button:
                deleteIssue();
                break;
        }
    }

    /**
     * Delete issue
     */
    private void deleteIssue() {
        mLoadingPanel.setVisibility(View.VISIBLE);

        boolean requestSent = Requests.deleteIssue(this, mIssueId,
                new VolleyRequestListener<JSONObject>() {
                    @Override
                    public void onResult(JSONObject response) {
                        mLoadingPanel.setVisibility(View.GONE);
                        int rc = Utils.getResponseCode(response);

                        if (rc == 0) {
                            Utils.showToastAtTop(mContext, mContext.getString(R.string.issue_deleted));
                            finish();
                            return;
                        } else if (rc == 502) {
                            Utils.showToastAtTop(mContext, mContext.getString(R.string.issue_delete_only_owner));
                        } else {
                            Utils.showToastAtTop(mContext, mContext.getString(R.string.something_went_wrong));
                        }
                    }

                    @Override
                    public void onError(JSONObject response) {
                        mLoadingPanel.setVisibility(View.GONE);
                    }
                }, null);

        if (!requestSent) {
            mLoadingPanel.setVisibility(View.GONE);
        }
    }

    /**
     * Initialize picture
     */
    void pictureinit() {
        final LinearLayout l = (LinearLayout) findViewById(R.id.linearLayout);
        ViewTreeObserver observer = l.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                init();
                l.getViewTreeObserver().removeGlobalOnLayoutListener(
                        this);
            }
        });
    }

    protected void init() {
        final LinearLayout l = (LinearLayout) findViewById(R.id.linearLayout);
        int a = l.getHeight();
        int b = l.getWidth();
        ImageView layout = (ImageView) findViewById(R.id.imageView);
        // Gets the layout params that will allow you to resize the layout
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layout.getLayoutParams();
        // Changes the height and width to the specified *pixels*
        params.height = a;
        layout.setLayoutParams(params);
    }

    /**
     * Initialize other info
     */
    void infoinit(JSONObject json) {
        //Adding title
        TextView title = (TextView) findViewById(R.id.title);
        String txt = getString(R.string.report) + " - Other";
        try {
            txt = getString(R.string.report) + " - " + json.getString("issueTypeName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        title.setText(txt);

        //Adding description
        try {
            txt = json.getString("description");
            if (txt != "") {
                TextView text = (TextView) findViewById(R.id.description_report_details_activity);
                text.setText(txt);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Adding long and lat to webview
        try {
            mLatitude = json.getDouble("latitude");
            mLongitude = json.getDouble("longitude");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Adding date
        try {
            txt = json.getString("report_date");
            if (txt != "") {
                TextView text = (TextView) findViewById(R.id.date_report_details_activity);
                text.setText(txt);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Adding picture
        txt = MainActivity.sIssueImageBase64;
        if (txt == "" || txt == null || txt.length() == 0) {
            LinearLayout img = (LinearLayout) findViewById(R.id.linearLayout);
            img.setVisibility(View.GONE);
        } else {
            byte[] decodedString = Base64.decode(txt, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            ImageView img = (ImageView) findViewById(R.id.imageView);
            img.setImageBitmap(decodedByte);
        }


        try {
            mIssueId = json.getInt("issueId");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}












