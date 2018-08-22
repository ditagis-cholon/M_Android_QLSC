package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.User;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.UserDangNhap;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;
import hcm.ditagis.com.cholon.qlsc.utities.Preference;

public class LoginAsycn extends AsyncTask<String, Void, User> {
    private ProgressDialog mDialog;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private LoginAsycn.AsyncResponse mDelegate;

    public interface AsyncResponse {
        void processFinish(User output);
    }

    public LoginAsycn(Context context, LoginAsycn.AsyncResponse delegate) {
        this.mContext = context;
        this.mDelegate = delegate;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        this.mDialog = new ProgressDialog(this.mContext, android.R.style.Theme_Material_Dialog_Alert);
        this.mDialog.setMessage(mContext.getString(R.string.connect_message));
        this.mDialog.setCancelable(false);
        this.mDialog.show();
    }

    @Override
    protected User doInBackground(String... params) {
        String userName = params[0];
        String pin = params[1];
        UserDangNhap.getInstance().setUser(null);
//        String passEncoded = (new EncodeMD5()).encode(pin + "_DITAGIS");
        // Do some validation here
        String urlParameters = String.format("Username=%s&Password=%s", userName, pin);
        String urlWithParam = String.format("%s?%s", Constant.getInstance().API_LOGIN, urlParameters);
        try {
//            + "&apiKey=" + API_KEY
            URL url = new URL(urlWithParam);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setRequestMethod("GET");
                conn.connect();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = bufferedReader.readLine();
                if (line != null) {
                    Preference.getInstance().savePreferences(mContext.getString(R.string.preference_login_api), line.replace("\"", ""));
                    bufferedReader.close();

                    if (checkAccess()) {
                        UserDangNhap.getInstance().setUser(new User());
                        UserDangNhap.getInstance().getUser().setDisplayName(getDisplayName());
                    }
                }
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }
        return UserDangNhap.getInstance().getUser();

    }

    @Override
    protected void onPostExecute(User user) {
//        if (khachHang != null) {
        mDialog.dismiss();
        this.mDelegate.processFinish(user);
//        }
    }

    private Boolean checkAccess() {
        boolean isAccess = false;
        try {
            URL url = new URL(Constant.getInstance().IS_ACCESS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", Preference.getInstance().loadPreference(mContext.getString(R.string.preference_login_api)));
                conn.connect();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = bufferedReader.readLine();
                if (line.equals("true"))
                    isAccess = true;

            } catch (Exception e) {
                Log.e("error", e.toString());
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
        return isAccess;

    }

    private String getDisplayName() {
        String displayName = "";
        try {
            URL url = new URL(Constant.getInstance().DISPLAY_NAME);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", Preference.getInstance().loadPreference(mContext.getString(R.string.preference_login_api)));
                conn.connect();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = bufferedReader.readLine();
                if (line != null)
                    pajsonRouteeJSon(line);
            } catch (Exception e) {
                Log.e("error", e.toString());
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
        return displayName;

    }

    private void pajsonRouteeJSon(String data) throws JSONException {
        if (data != null) {
            String myData = "{ \"account\": [".concat(data).concat("]}");
            JSONObject jsonData = new JSONObject(myData);
            JSONArray jsonRoutes = jsonData.getJSONArray("account");
            for (int i = 0; i < jsonRoutes.length(); i++) {
                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                String displayName = jsonRoute.getString(mContext.getString(R.string.sql_coloumn_login_displayname));
                String username = jsonRoute.getString(mContext.getString(R.string.sql_coloumn_login_username));
                String role = jsonRoute.getString(mContext.getString(R.string.sql_coloumn_login_role));
                UserDangNhap.getInstance().setUser(new User());
                UserDangNhap.getInstance().getUser().setDisplayName(displayName);
                UserDangNhap.getInstance().getUser().setUserName(username);
                UserDangNhap.getInstance().getUser().setRole(role.toUpperCase());
            }
        }
    }
}
