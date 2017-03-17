package eu.opentransportnet.cityreport.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import eu.opentransportnet.cityreport.R;
import eu.opentransportnet.cityreport.models.BaseActivity;
import eu.opentransportnet.cityreport.models.User;
import eu.opentransportnet.cityreport.utils.ObservableWebView;
import eu.opentransportnet.cityreport.utils.SessionManager;

/**
 * @author Ilmārs Svilsts
 */
public class DisclaimerActivity extends BaseActivity implements View.OnClickListener {

    private static Context sContext;
    private boolean mAgree = false;
    private SessionManager mSessionManager;
    private ObservableWebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);
        findViewById(R.id.scroll_button).setOnClickListener(this);
        sContext = this;
        setToolbarTitle(R.string.title_activity_disclaimer);
        initToolbarBackBtn();
        mSessionManager = new SessionManager(this);

        User user = mSessionManager.getUser();

        int vers = 0;
        try {

            if (getString(R.string.svn_version).equals("null")) {
            } else {
                vers = Integer.parseInt(getString(R.string.svn_version));
            }
        } catch (Exception e) {
        }

        mWebView = (ObservableWebView) findViewById(R.id.web_view);
        mWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        if (user.getDisclaimer() == true && user.getDisclaimerV() >= vers) {
            FrameLayout dis = (FrameLayout) findViewById(R.id.dis_frame);
            dis.setVisibility(View.GONE);

            //Remove bottom margin
            ViewGroup.MarginLayoutParams webViewParms = (ViewGroup.MarginLayoutParams) mWebView
                    .getLayoutParams();
            webViewParms.bottomMargin = 0;
            mWebView.setLayoutParams(webViewParms);
        }

        mWebView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
            @Override
            public void onScroll(int l, int t) {
                int tek = (int) Math.floor(mWebView.getContentHeight() * mWebView.getScale());

                if (tek - mWebView.getScrollY() == mWebView.getHeight()) {
                    //Web view scrolled to end
                    ImageView image = (ImageView) findViewById(R.id.scroll_button);
                    image.setImageDrawable(ContextCompat.getDrawable(sContext, R.drawable.u7));
                    TextView text = (TextView) findViewById(R.id.scroll_button_text);
                    text.setText(getString(R.string.agree));
                    mAgree = true;
                }
            }
        });
        mWebView.loadUrl("file:///android_asset/disclaimerText.html");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {}
        else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {}
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                finish();
                break;
            case R.id.scroll_button:
                if (mAgree == false) {
                    int tek = (int) Math.floor(mWebView.getContentHeight() * mWebView.getScale());
                    mWebView.scrollTo(0, tek - mWebView.getHeight());
                } else {
                    int vers = 0;
                    try {
                        if (getString(R.string.svn_version).equals("null")) {
                        } else {
                            vers = Integer.parseInt(getString(R.string.svn_version));
                        }
                    } catch (Exception e) {
                    }
                    mSessionManager.saveDisclaimer(true, vers);
                    finish();
                }
                break;
        }
    }
}