package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;
import hcm.ditagis.com.cholon.qlsc.utities.Preference;

public class GenerateIDSuCoAsycn extends AsyncTask<Void, Void, String> {
    //    private ProgressDialog mDialog;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private AsyncResponse mDelegate;
    private DApplication mApplication;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    GenerateIDSuCoAsycn(Context context,DApplication dApplication, AsyncResponse delegate) {
        this.mContext = context;
        this.mDelegate = delegate;
        this.mApplication = dApplication;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//        this.mDialog = new ProgressDialog(this.mContext, android.R.style.Theme_Material_Dialog_Alert);
//        this.mDialog.setMessage(mContext.getString(R.string.preparing));
//        this.mDialog.setCancelable(false);
//        this.mDialog.show();
    }

    @Override
    protected String doInBackground(Void... params) {
        //Tránh gặp lỗi networkOnMainThread nên phải dùng asyncTask
        String id = "";
        try {
            String API_URL = Constant.getInstance().GENERATE_ID_SUCO + mApplication.getUserDangNhap().getRole();

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(false);
                conn.setRequestMethod(Constant.HTTPRequest.GET_METHOD);
                conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION,mApplication.getUserDangNhap().getToken());
                conn.connect();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = bufferedReader.readLine();
                id = line.replace("\"", "");
            } catch (Exception e) {
                Log.e("error", e.toString());
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e("Lỗi lấy IDSuCo", e.toString());
        }
        return id;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);


    }

    @Override
    protected void onPostExecute(String value) {
//        if (khachHang != null) {
//        mDialog.dismiss();
        this.mDelegate.processFinish(value);
//        }
    }


}
