package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.FeatureQueryResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.entities.DLayerInfo;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;

public class PreparingAsycn extends AsyncTask<Void, ListenableFuture<FeatureQueryResult>, List<DLayerInfo>> {
    private ProgressDialog mDialog;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private AsyncResponse mDelegate;
    private DApplication mApplication;

    public interface AsyncResponse {
        void processFinish(List<DLayerInfo> output);
    }

    public PreparingAsycn(Context context, DApplication dApplication, AsyncResponse delegate) {
        this.mContext = context;
        this.mDelegate = delegate;
        this.mApplication = dApplication;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        this.mDialog = new ProgressDialog(this.mContext, android.R.style.Theme_Material_Dialog_Alert);
        this.mDialog.setMessage(mContext.getString(R.string.preparing));
        this.mDialog.setCancelable(false);
        this.mDialog.show();
    }

    @Override
    protected List<DLayerInfo> doInBackground(Void... params) {
        try {
            return getLayerInfoAPI();
//            new GetVatTu(mContext).getVatTuFromService();
//            new GetDMA(mContext).getMaDMAFromService();

        } catch (Exception e) {
            Log.e("Lỗi lấy danh sách DMA", e.toString());
        }
        return null;
    }

    @SafeVarargs
    @Override
    protected final void onProgressUpdate(ListenableFuture<FeatureQueryResult>... values) {
        super.onProgressUpdate(values);


    }

    @Override
    protected void onPostExecute(List<DLayerInfo> value) {
//        if (khachHang != null) {
        mDialog.dismiss();
        this.mDelegate.processFinish(value);
//        }
    }


    private List<DLayerInfo> getLayerInfoAPI() {
        try {
            String API_URL = Constant.getInstance().LAYER_INFO;

            URL url = new URL(API_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setDoOutput(false);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", mApplication.getUserDangNhap().getToken());
                conn.connect();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder builder = new StringBuilder();
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    builder.append(line);
                }
                return pajsonRouteeJSon(builder.toString());
            } catch (Exception e) {
                Log.e("error", e.toString());
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            Log.e("Lỗi lấy LayerInfo", e.toString());
        }
        return null;
    }

    private List<DLayerInfo> pajsonRouteeJSon(String data) throws JSONException {
        if (data == null)
            return null;
        String myData = "{ \"layerInfo\": ".concat(data).concat("}");
        JSONObject jsonData = new JSONObject(myData);
        JSONArray jsonRoutes = jsonData.getJSONArray("layerInfo");
        List<DLayerInfo> layerDTGS = new ArrayList<>();
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.getJSONObject(i);
            layerDTGS.add(new DLayerInfo(jsonRoute.getString(mContext.getString(R.string.sql_coloumn_sys_id)),
                    jsonRoute.getString(mContext.getString(R.string.sql_coloumn_sys_title)),
                    jsonRoute.getString(mContext.getString(R.string.sql_coloumn_sys_url)),
                    jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_iscreate)), jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_isdelete)),
                    jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_isedit)), jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_isview)),
                    jsonRoute.getString(mContext.getString(R.string.sql_column_sys_definition)),
                    jsonRoute.getString("OutFields").split(","), null));
        }
        return layerDTGS;
    }

}
