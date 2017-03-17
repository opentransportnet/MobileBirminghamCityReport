package eu.opentransportnet.cityreport.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import eu.opentransportnet.cityreport.BuildConfig;
import eu.opentransportnet.cityreport.R;
import eu.opentransportnet.cityreport.adapters.DrawerList;
import eu.opentransportnet.cityreport.interfaces.VolleyRequestListener;
import eu.opentransportnet.cityreport.listeners.SlideMenuClickListener;
import eu.opentransportnet.cityreport.models.BaseActivity;
import eu.opentransportnet.cityreport.models.User;
import eu.opentransportnet.cityreport.network.NetworkReceiver;
import eu.opentransportnet.cityreport.network.RequestQueueSingleton;
import eu.opentransportnet.cityreport.network.Requests;
import eu.opentransportnet.cityreport.network.UploadTask;
import eu.opentransportnet.cityreport.utils.Classificators;
import eu.opentransportnet.cityreport.utils.Const;
import eu.opentransportnet.cityreport.utils.SessionManager;
import eu.opentransportnet.cityreport.utils.Utils;

import com.library.routerecorder.RouteRecorder;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * @author Kristaps Krumins
 * @author Ilmars Svilsts
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {
    public final static String EXTRA_MESSAGE = "com.birmingham.app.MESSAGE";

    private static final String LOG_TAG = "MainActivity";
    private static final int REPORT_ACTIVITY = 1000;

    public static String sIssueImageBase64;
    private static Context sContext;
    private static RouteRecorder sRouteRec;

    private DrawerLayout mDrawer;
    private NetworkReceiver mNetworkReceiver;
    private ListView mDrawerList;
    private SessionManager mSessionManager;
    public final String TAG_DELETE_USER = "delete user request";
    public final String TAG_LOAD_ISUE = "load issue request";
    private Boolean mStartSearch=false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /* Initialization */
        sContext = this;
        mSessionManager = new SessionManager(this);
        setContentView(R.layout.activity_home);
        initDrawer();
        setToolbarTitle(R.string.title_activity_home);
        initToolbarBackBtn();

        Button drawer = (Button) findViewById(R.id.back_button);
        drawer.setText(R.string.icon_menu);
        drawer.setTextSize(45);
        drawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawer.openDrawer(Gravity.START);
            }
        });
        Typeface tf = getTypeFace();
        Button closeBtn = (Button) findViewById(R.id.closebtn);
        closeBtn.setText(R.string.icon_cancel);
        closeBtn.setTextSize(45);
        closeBtn.setTypeface(tf);
        closeBtn.setOnClickListener(this);

        initRouteRecorder();
        createDefaultFolders();
        mNetworkReceiver = new NetworkReceiver(this);
        findViewById(R.id.report_button).setOnClickListener(this);

        createFolders();


        ImageButton search = (ImageButton) findViewById(R.id.searchbtn);
        search.setVisibility(View.VISIBLE);
        search.setOnClickListener(this);
        search = (ImageButton) findViewById(R.id.closeButton);
        search.setVisibility(View.GONE);

        TextView version = (TextView) findViewById(R.id.version);
        String versionName = BuildConfig.VERSION_NAME;

        try {
            if (getString(R.string.svn_version).equals("null")) {
                version.setText("v" + versionName);
            } else {
                version.setText("v" + versionName + " (" + getString(R.string.svn_version) + ")");
            }
        } catch (Exception e) {
        }

        findViewById(R.id.close_report_type).setOnClickListener(this);

        findViewById(R.id.road_crash).setOnClickListener(this);
        findViewById(R.id.road_traffic).setOnClickListener(this);
        findViewById(R.id.road_miss).setOnClickListener(this);
        findViewById(R.id.road_other).setOnClickListener(this);
        findViewById(R.id.speeding).setOnClickListener(this);

        UploadTask.getInstance(this).startScheduledUpload();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Registers BroadcastReceiver to track network connection changes.
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mNetworkReceiver, filter);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            unregisterReceiver(mNetworkReceiver);
        } catch (IllegalArgumentException e) {
        }
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.close_report_type:
                FrameLayout report_type = (FrameLayout) findViewById(R.id.report_issue_types);
                report_type.setVisibility(View.GONE);
                break;
            case R.id.report_button:
                closeSearch();
                report_type = (FrameLayout) findViewById(R.id.report_issue_types);
                report_type.setVisibility(View.VISIBLE);
                break;
            case R.id.road_crash:
                openReportDetails(Classificators.ISSUE_ACCIDENT);
                break;
            case R.id.road_traffic:
                openReportDetails(Classificators.ISSUE_DANG_TRAFFIC);
                break;
            case R.id.road_miss:
                openReportDetails(Classificators.ISSUE_NEAR_MISS);
                break;
            case R.id.road_other:
                openReportDetails(Classificators.ISSUE_OTHER);
                break;
            case R.id.speeding:
                openReportDetails(Classificators.ISSUE_SPEEDING);
                break;
            case R.id.searchbtn:
                startSearchFind();
                break;
            case R.id.closebtn:
                closeSearch();
                break;
        }
    }

    @JavascriptInterface
    public void onIssueClick(int issueId) {
        Utils.logD(LOG_TAG, "Issue id:" + issueId);

        if (issueId > 1) {
            showIssue(issueId);
        }
    }

    @JavascriptInterface
    public void openReportDetails(int issueId, double lat, double lng) {
        Intent d = new Intent(this, ReportDetailsActivity.class);
        d.putExtra("lat", lat);
        d.putExtra("lng", lng);
        d.putExtra(EXTRA_MESSAGE, issueId);
        this.startActivityForResult(d, REPORT_ACTIVITY);
        FrameLayout reportIssueTypes = (FrameLayout) findViewById(R.id.report_issue_types);
        reportIssueTypes.setVisibility(View.GONE);
    }

    private void openReportDetails(int issueID) {
        // Get report pin coordinates
        sRouteRec.getWebView().loadUrl("javascript:openReportDetails(" + issueID + ")");
    }

    /**
     * Initialize main navigation drawer
     */
    public void initDrawer() {
        mDrawer = (DrawerLayout) findViewById(R.id.drawer);
        mDrawer.setDrawerShadow(R.drawable.drawer_shadow, Gravity.START);
        mDrawerList = (ListView) findViewById(R.id.drawerlist);
        setDrawerAdapter();
        mDrawerList.setOnItemClickListener(new SlideMenuClickListener(mDrawer, this));

        User user = mSessionManager.getUser();
        // Sets name in drawer
        String fullName = user.getFirstName() + " " + user.getLastName();
        if (fullName == null || fullName == "") {
            fullName = "[no name provided]";
        }
        TextView displayName = (TextView) findViewById(R.id.display_name);
        displayName.setText(fullName);

        Bitmap photo = user.getRoundedPhoto();
        setPhotoInDrawer(photo);

        if (!user.hasPhoto()) {
            user.downloadPhotoAndPutInDrawer(this);
        }
    }

    /**
     * Initialize route recorder
     */
    private void initRouteRecorder() {
        sRouteRec = (RouteRecorder) getFragmentManager().findFragmentById(R.id.route_recorder);
        sRouteRec.setDefaultLocation(Const.DEFAULT_LATITUDE, Const.DEFAULT_LONGITUDE);
        sRouteRec.setTracking(true);
        sRouteRec.addJavascriptInterface(this, "MainActivity");
        sRouteRec.loadWebView();
        sRouteRec.loadUrl("javascript:addWmsLayer(" + 0 + ",'"
                + Const.WMS_URL_ISSUES + "','issues_birmingham')");
    }

    public static Context getContext() {
        return sContext;
    }

    /**
     * Creates folders in internal storage
     */
    private void createFolders() {
        File filesDir = getFilesDir();
        File newFolder = new File(filesDir + "/" + Const.STORAGE_PATH_REPORT);
        newFolder.mkdir();
    }

    /**
     * Setting navigation drawer adapter
     */
    private void setDrawerAdapter() {
        String[] drawerItems = getResources().getStringArray(R.array.drawer_titles_array);

        Integer[] drawerItemImages = {
                R.drawable.dis,
                R.drawable.bin,
                R.drawable.ic_log_out
        };

        DrawerList adapter = new DrawerList(
                MainActivity.this,
                drawerItems,
                drawerItemImages);

        mDrawerList.setAdapter(adapter);
    }

    /**
     * Set persons photo in navigation drawer
     *
     * @param photo photo on user as bitmap
     */
    public void setPhotoInDrawer(Bitmap photo) {
        // Sets profile photo in drawer
        ImageView profilePhoto = (ImageView) findViewById(R.id.profile_photo);
        profilePhoto.setImageBitmap(photo);
    }

    public static RouteRecorder getRouteRecorder() {
        return sRouteRec;
    }

    /**
     * Creates default folders for app
     */
    private void createDefaultFolders() {
        File folder = new File(getFilesDir() + "/" + Const.STORAGE_PATH_REPORT);
        folder.mkdir();
        folder = new File(getFilesDir() + "/photo");
        folder.mkdir();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RequestQueueSingleton.getInstance(sContext).cancelAllPendingRequests(TAG_DELETE_USER);
        RequestQueueSingleton.getInstance(sContext).cancelAllPendingRequests(TAG_LOAD_ISUE);

    }

    /**
     * Delete user content from server if user chooses so
     */
    public void deleteUser() {
        FrameLayout spinner;
        spinner = (FrameLayout) findViewById(R.id.progress);
        spinner.setVisibility(View.VISIBLE);

        AlertDialog.Builder builder = new AlertDialog.Builder(sContext);
        builder.setTitle(sContext.getString(R.string.delete_user_title))
                .setMessage(sContext.getString(R.string.delete_user_content))
                .setPositiveButton(sContext.getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                                org.json.simple.JSONObject objs = new org.json.simple.JSONObject();
                                objs.put("appId", Const.APPLICATION_ID);
                                objs.put("userId", Utils.getHashedUserEmail(sContext));

                                String jsonBodyString = ((org.json.simple.JSONObject) objs).toJSONString();
                                JSONObject jsonBody = null;
                                try {
                                    jsonBody = new JSONObject(jsonBodyString);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                Requests.sendPostRequest(sContext, "http://" + Utils.getHostname() + Utils.getUrlPathStart() + Requests
                                                .PATH_DELETE_USER,
                                        jsonBody, new VolleyRequestListener<JSONObject>() {
                                            @Override
                                            public void onResult(JSONObject mUseritem) {
                                                if (mUseritem != null) {
                                                    String a = "2";
                                                    try {
                                                        a = mUseritem.getString("responseCode");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    String check = "0";
                                                    if (check.equals(a)) {
                                                        FrameLayout spinner;
                                                        spinner = (FrameLayout) findViewById(R.id.progress);
                                                        spinner.setVisibility(View.GONE);

                                                        Utils.showToastAtTop(sContext, sContext.getString(R.string.delete_user));
                                                        Utils.deleteAllLocalFiles(sContext);
                                                        new SessionManager(sContext).forceLogoutUser();
                                                    } else {
                                                        FrameLayout spinner;
                                                        spinner = (FrameLayout) findViewById(R.id.progress);
                                                        spinner.setVisibility(View.GONE);
                                                        Utils.showToastAtTop(sContext, sContext.getString(R.string.server_error));
                                                    }
                                                }
                                            }

                                            @Override
                                            public void onError(JSONObject error) {
                                                FrameLayout spinner;
                                                spinner = (FrameLayout) findViewById(R.id.progress);
                                                spinner.setVisibility(View.GONE);
                                                Utils.showToastAtTop(sContext, sContext.getString(R.string.server_error));
                                            }
                                        }, TAG_DELETE_USER);
                            }
                        })
                .setNegativeButton(sContext.getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                FrameLayout spinner;
                                spinner = (FrameLayout) findViewById(R.id.progress);
                                spinner.setVisibility(View.GONE);
                            }
                        });
        builder.create().show();
    }


    /**
     * Open Issue activity after initial click on reported issue
     *
     * @param id issue id
     */
    public void showIssue(final int id) {
        runOnUiThread(new Runnable() {

            public void run() {

                FrameLayout spinner;
                spinner = (FrameLayout) findViewById(R.id.progress);
                spinner.setVisibility(View.VISIBLE);
                JSONObject objs = new JSONObject();

                try {
                    objs.put("issueId", id);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Requests.sendPostRequest(sContext, "http://" + Utils.getHostname() + Utils.getUrlPathStart() + Requests.PATH_LOAD_ISSUE,
                        objs, new VolleyRequestListener<JSONObject>() {
                            @Override
                            public void onResult(JSONObject mUseritem) {
                                if (mUseritem != null) {
                                    String a = "2";
                                    String check = "0";
                                    try {
                                        a = mUseritem.getString("responseCode");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    if (check.equals(a)) {

                                        FrameLayout spinner;
                                        spinner = (FrameLayout) findViewById(R.id.progress);
                                        spinner.setVisibility(View.GONE);
                                        //ATVERT SKATU
                                        Intent issue = new Intent(sContext, ReportInfoActivity.class);

                                        // Cant pass large data through intent so remove picture
                                        // from JSON
                                        try {
                                            sIssueImageBase64 = mUseritem.getString("picture");
                                            mUseritem.remove("picture");
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }

                                        issue.putExtra("json", mUseritem.toString());
                                        startActivityForResult(issue, REPORT_ACTIVITY);
                                    } else {

                                        FrameLayout spinner;
                                        spinner = (FrameLayout) findViewById(R.id.progress);
                                        spinner.setVisibility(View.GONE);
                                        Utils.showToastAtTop(sContext, sContext.getString(R.string.server_error));
                                    }
                                }
                            }

                            @Override
                            public void onError(JSONObject error) {
                                FrameLayout spinner;
                                spinner = (FrameLayout) findViewById(R.id.progress);
                                spinner.setVisibility(View.GONE);
                                Utils.showToastAtTop(sContext, sContext.getString(R.string.server_error));
                            }
                        }, TAG_LOAD_ISUE);
            }
        });
    }
    private void startSearchFind(){
        TextView title = (TextView) findViewById(R.id.title);
        title.setVisibility(View.GONE);
        EditText editSearch = (EditText) findViewById(R.id.edit_search);
        editSearch.setVisibility(View.VISIBLE);
        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search();
                    return true;
                }
                return false;
            }
        });

        TextView closeBtn = (TextView) findViewById(R.id.closebtn);
        closeBtn.setVisibility(View.VISIBLE);
        closeBtn = (TextView) findViewById(R.id.back_button);
        closeBtn.setVisibility(View.GONE);

        if(mStartSearch){search();}
        else{mStartSearch=true;}
    }
    @JavascriptInterface
    public void closeSearch(){
        runOnUiThread(new Runnable() {

            public void run() {
                mStartSearch = false;
                EditText editSearch = (EditText) findViewById(R.id.edit_search);
                editSearch.setVisibility(View.GONE);
                editSearch.setText("");
                TextView closeBtn = (TextView) findViewById(R.id.closebtn);
                closeBtn.setVisibility(View.GONE);
                TextView closeBtnd = (TextView) findViewById(R.id.back_button);
                closeBtnd.setVisibility(View.VISIBLE);
                TextView title = (TextView) findViewById(R.id.title);
                title.setVisibility(View.VISIBLE);
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });
    }
    private void search() {
        FrameLayout spinner;
        spinner = (FrameLayout) findViewById(R.id.progress);
        spinner.setVisibility(View.VISIBLE);
        EditText zipCode=(EditText) findViewById(R.id.edit_search);
        Requests.centerMapByPostalCode(sContext, String.valueOf(zipCode.getText()), new VolleyRequestListener<JSONObject>() {
                    @Override
                    public void onResult(JSONObject mUseritem) {
                        FrameLayout spinner;
                        spinner = (FrameLayout) findViewById(R.id.progress);

                        if (mUseritem != null) {
                            String a = "2";
                            String check = "200";
                            try {
                                a = mUseritem.getString("status");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            spinner.setVisibility(View.GONE);
                            if (!check.equals(a)) {
                                Utils.showToastAtTop(sContext, sContext.getString(R.string.search_failed));
                            }
                        }
                        else {
                            spinner.setVisibility(View.GONE);
                            Utils.showToastAtTop(sContext, sContext.getString(R.string.search_failed));
                        }
                    }

                    @Override
                    public void onError(JSONObject error) {
                        FrameLayout spinner;
                        spinner = (FrameLayout) findViewById(R.id.progress);
                        spinner.setVisibility(View.GONE);
                        Utils.showToastAtTop(sContext, sContext.getString(R.string.search_failed));
                    }
                }, TAG_LOAD_ISUE);
        try  {
            InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {}
    }
}