package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;

public class NotifyServerAddingFeature extends AsyncTask<String, Void, String> {
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private AsyncResponse mDelegate;
    private DApplication mApplication;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public NotifyServerAddingFeature(Context context, DApplication dApplication, AsyncResponse delegate) {
        this.mContext = context;
        this.mDelegate = delegate;
        this.mApplication = dApplication;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            if (params != null && params.length > 0)
                return getLayerInfoAPI(params[0]);
        } catch (Exception e) {
            Log.e("Lỗi lấy danh sách DMA", e.toString());
        }
        return "";
    }


    @Override
    protected void onPostExecute(String value) {
        this.mDelegate.processFinish(value);
//        }
    }


    private String getLayerInfoAPI(String objectID) {
        try {
            String API_URL = String.format(Constant.URL_API.ADD_FEATURE, objectID);

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(true);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod(Constant.HTTPRequest.POST_METHOD);

                conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, mApplication.getUserDangNhap().getToken());
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setUseCaches(false);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write("");
                wr.flush();
                conn.connect();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
                return builder.toString().replace("\"", "");
            } catch (Exception e) {
                Log.e("error", e.toString());
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e("Lỗi lấy LayerInfo", e.toString());
        }
        return "";
    }


}
