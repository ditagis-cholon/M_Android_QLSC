package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;


/**
 * Created by ThanLe on 4/16/2018.
 */

public class CheckExistFeatureAsync extends AsyncTask<Void, String, Void> {
    @SuppressLint("StaticFieldLeak")
    private Activity mActivity;
    private ServiceFeatureTable mServiceFeatureTable;
    @SuppressLint("StaticFieldLeak")
    private AsyncResponse mDelegate;
    private DApplication mApplication;
    private MapView mMapView;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public CheckExistFeatureAsync(Activity activity, MapView mapView,
                                  ServiceFeatureTable serviceFeatureTable, AsyncResponse delegate) {
        this.mServiceFeatureTable = serviceFeatureTable;
        this.mActivity = activity;
        this.mApplication = (DApplication) activity.getApplication();
        this.mDelegate = delegate;
        this.mMapView = mapView;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        //Kiểm tra vị trí hiện tại đã có điểm sự cố hay chưa
        final ListenableFuture<List<IdentifyLayerResult>> listListenableFuture = mMapView
                .identifyLayersAsync(mMapView.locationToScreen(mApplication.getAddFeaturePoint()), 5, false);
        listListenableFuture.addDoneListener(() -> {
            List<IdentifyLayerResult> identifyLayerResults;
            try {
                identifyLayerResults = listListenableFuture.get();
                if (identifyLayerResults.size() > 0)
                    for (IdentifyLayerResult identifyLayerResult : identifyLayerResults) {
                        {
                            List<GeoElement> elements = identifyLayerResult.getElements();
                            if (elements.size() > 0 && elements.get(0) instanceof ArcGISFeature) {
                                //Nếu có điểm sự cố, kiểm tra ngày phản ánh có phải là hôm nay hay không
                                ArcGISFeature feature = (ArcGISFeature) elements.get(0);
                                Object ngayPhanAnh = feature.getAttributes().get(Constant.FieldSuCo.TG_PHAN_ANH);

                                if (ngayPhanAnh != null) {
                                    Calendar c1 = (Calendar) ngayPhanAnh;
                                    Calendar c2 = Calendar.getInstance();
                                    if (c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
                                            c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                                        publishProgress(feature.getAttributes().get(Constant.FieldSuCo.ID_SUCO).toString());
                                    else publishProgress();
                                } else publishProgress();
                            } else publishProgress();
                        }
                    }
                else publishProgress();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                publishProgress();
            }
        });
        return null;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        if (values == null || values.length == 0) {
            this.mDelegate.processFinish(null);
        } else if (values.length > 0) this.mDelegate.processFinish(values[0]);
    }


    @Override
    protected void onPostExecute(Void result) {


    }

}