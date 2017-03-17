package eu.opentransportnet.cityreport.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import eu.opentransportnet.cityreport.R;
import eu.opentransportnet.cityreport.activities.MainActivity;
import eu.opentransportnet.cityreport.utils.Const;
import eu.opentransportnet.cityreport.utils.SessionManager;
import eu.opentransportnet.cityreport.utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author Kristaps Krumins
 */
public class User {
    private String mFirstName = "";
    private String mLastName = "";
    private String mEmail = "";
    private String mHashedEmail = "";
    private String mEncodedPhoto = "";
    private Bitmap mDefaultProfilePhoto = null;
    private String mPhotoUrl = "";
    private boolean mDisclaimer = false;
    private int mDisclaimerV = 0;
    private Context mContext;

    public User(Context ctx) {
        mDefaultProfilePhoto = BitmapFactory.decodeResource(ctx.getResources(),
                R.drawable.ic_no_profile_photo);
        mContext = ctx;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(String firstName) {
        this.mFirstName = firstName;
    }

    public String getLastName() {
        return mLastName;
    }

    public void setLastName(String lastName) {
        this.mLastName = lastName;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getHashedEmail() {
        return mHashedEmail;
    }

    /**
     * Also sets hashed email
     *
     * @param email The email
     */
    public void setEmail(String email) {
        if (email == "null") {
            mEmail = null;
            mHashedEmail = null;
        } else {
            mEmail = email;
            if (mHashedEmail == null || mHashedEmail.isEmpty()) {
                setHashedEmail(Utils.hashMd5(Const.APPLICATION_ID + email));
            }
        }
    }

    public void setHashedEmail(String hashedEmail) {
        mHashedEmail = hashedEmail;
    }

    public String getEncodedPhoto() {
        return mEncodedPhoto;
    }

    public void setEncodedPhoto(String encodedPhoto) {
        this.mEncodedPhoto = encodedPhoto;
    }

    public boolean getDisclaimer() {
        return mDisclaimer;
    }
    public void setDisclaimer(boolean disclaimer) {
        this.mDisclaimer = disclaimer;
    }

    public int getDisclaimerV() {
        return mDisclaimerV;
    }
    public void setDisclaimerv(int disclaimerV) {
        this.mDisclaimerV = disclaimerV;
    }

    /**
     * Encodes given image to base 64 string
     *
     * @param image the image
     * @return base 64 string
     */
    public static String encodeToBase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    /**
     * Decodes given base 64 string to bitmap
     *
     * @param input the base 64 string
     * @return Bitmap
     */
    public static Bitmap decodeToBitmap(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.length);
    }

    public Bitmap getPhoto() {
        if (mEncodedPhoto == null || mEncodedPhoto.isEmpty()) {
            return mDefaultProfilePhoto;
        } else {
            return decodeToBitmap(mEncodedPhoto);
        }
    }

    public Bitmap getRoundedPhoto() {
        return Utils.getRoundedCornerBitmap(getPhoto());
    }

    public boolean hasEmail() {
        if (mEmail != null && !mEmail.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public void downloadPhoto() {
        downloadPhoto(null);
    }

    private void downloadPhoto(final MainActivity mainActivity) {
        if (mPhotoUrl != null && !mPhotoUrl.isEmpty()) {
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    String urlStr = mPhotoUrl.replace("sz=50", "sz=100");
                    try {
                        URL url = new URL(urlStr);
                        URLConnection conn = url.openConnection();
                        InputStream in = (InputStream) conn.getContent();
                        mEncodedPhoto = encodeToBase64(BitmapFactory.decodeStream(in));
                        saveUser();
                        if (mainActivity != null) {
                            mainActivity.setPhotoInDrawer(getRoundedPhoto());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    return null;
                }
            }.execute();
        }
    }

    public void downloadPhotoAndPutInDrawer(MainActivity mainActivity) {
        downloadPhoto(mainActivity);
    }

    public String getPhotoUrl() {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.mPhotoUrl = photoUrl;
    }

    public void saveUser() {
        new SessionManager(mContext).updateLoginSession(this);
    }

    public boolean hasPhoto() {
        if (mEncodedPhoto != null && !mEncodedPhoto.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }
}
