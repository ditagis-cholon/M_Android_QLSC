package hcm.ditagis.com.cholon.qlsc.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.KhachHang;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.KhachHangDangNhap;
import hcm.ditagis.com.cholon.qlsc.utities.Preference;

public class NewLoginAsycn extends AsyncTask<String, Void, KhachHang> {
    private Exception exception;
    private ProgressDialog mDialog;
    private Context mContext;
    private LoginAsycn.AsyncResponse mDelegate;
    String API_URL = "http://sawagis.vn/cholon/api/Login";

    public interface AsyncResponse {
        void processFinish(KhachHang output);
    }

    public NewLoginAsycn(Context context, LoginAsycn.AsyncResponse delegate) {
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
    protected KhachHang doInBackground(String... params) {
        String userName = params[0];
        String pin = params[1];
//        String passEncoded = (new EncodeMD5()).encode(pin + "_DITAGIS");
        // Do some validation here
        String urlParameters = String.format("Username=%s&Password=%s", userName, pin);
        String urlWithParam = String.format("%s?%s", API_URL, urlParameters);
        try {
//            + "&apiKey=" + API_KEY
            URL url = new URL(urlWithParam);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setRequestMethod("GET");
                conn.connect();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                    break;
                }
                Preference.getInstance().savePreferences(mContext.getString(R.string.preference_login_api), stringBuilder.toString());
                bufferedReader.close();

                KhachHangDangNhap.getInstance().setKhachHang(new KhachHang());
                return KhachHangDangNhap.getInstance().getKhachHang();
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e("ERROR", e.getMessage(), e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(KhachHang khachHang) {
//        if (khachHang != null) {
        mDialog.dismiss();
        this.mDelegate.processFinish(khachHang);
//        }
    }
}
