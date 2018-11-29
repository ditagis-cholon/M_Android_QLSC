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
import hcm.ditagis.com.cholon.qlsc.utities.Constant;

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
        User user = new User();
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
                    String token = line.replace("\"", "");
                    bufferedReader.close();

                    if (checkAccess(token)) {
                        user = getMoreInfo(token);
                    }
                }
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
        }
        return user;

    }

    @Override
    protected void onPostExecute(User user) {
//        if (khachHang != null) {
        mDialog.dismiss();
        this.mDelegate.processFinish(user);
//        }
    }

    private Boolean checkAccess(String token) {
        boolean isAccess = false;
        try {
            URL url = new URL(Constant.getInstance().IS_ACCESS);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", token);
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

    private User getMoreInfo(String token) {
        User user = new User();

        try {
            URL url = new URL(Constant.getInstance().DISPLAY_NAME);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", token);
                conn.connect();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = bufferedReader.readLine();
                if (line != null) {
                    user = pajsonRouteeJSon(line);
                    user.setToken(token);
                }
            } catch (Exception e) {
                Log.e("error", e.toString());
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e("error", e.toString());
        }
        return user;

    }

    private User pajsonRouteeJSon(String data) throws JSONException {
        User user = new User();
        if (data != null) {
            String myData = "{ \"account\": [".concat(data).concat("]}");
            JSONObject jsonData = new JSONObject(myData);
            JSONArray jsonRoutes = jsonData.getJSONArray("account");
            for (int i = 0; i < jsonRoutes.length(); i++) {
                JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
                String displayName = jsonRoute.getString(mContext.getString(R.string.sql_coloumn_login_displayname));
                String username = jsonRoute.getString(mContext.getString(R.string.sql_coloumn_login_username));
                String role = jsonRoute.getString(mContext.getString(R.string.sql_coloumn_login_role));

                user.setDisplayName(displayName);
                user.setUserName(username);
                user.setRole(role.toUpperCase());
            }
        }
        return user;
    }
}
