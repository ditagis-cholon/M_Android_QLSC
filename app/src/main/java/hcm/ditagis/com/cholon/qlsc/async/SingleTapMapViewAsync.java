package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.ArcGISFeatureTable;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.List;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.MainActivity;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.DFeatureLayer;
import hcm.ditagis.com.cholon.qlsc.utities.Popup;

/**
 * Created by ThanLe on 4/16/2018.
 */

public class SingleTapMapViewAsync extends AsyncTask<Point, DFeatureLayer, Void> {
    private ProgressDialog mDialog;
    @SuppressLint("StaticFieldLeak")
    private Activity mActivity;
    private List<DFeatureLayer> mDFeatureLayers;
    @SuppressLint("StaticFieldLeak")
    private MapView mMapView;
    private ArcGISFeature mSelectedArcGISFeature;
    @SuppressLint("StaticFieldLeak")
    private Popup mPopUp;
    private android.graphics.Point mClickPoint;
    private boolean isFound = false;
    private DApplication mApplication;

    public SingleTapMapViewAsync(MainActivity activity, List<DFeatureLayer> DFeatureLayers, Popup popup,
                                 android.graphics.Point clickPoint, MapView mapview) {
        this.mMapView = mapview;
        this.mDFeatureLayers = DFeatureLayers;
        this.mPopUp = popup;
        this.mClickPoint = clickPoint;
        this.mActivity = activity;
        this.mApplication = (DApplication) activity.getApplication();
        this.mDialog = new ProgressDialog(activity, android.R.style.Theme_Material_Dialog_Alert);
    }

    @Override
    protected Void doInBackground(Point... points) {
        final ListenableFuture<List<IdentifyLayerResult>> listListenableFuture = mMapView
                .identifyLayersAsync(mClickPoint, 5, false);
        listListenableFuture.addDoneListener(() -> {
            List<IdentifyLayerResult> identifyLayerResults;
            try {
                identifyLayerResults = listListenableFuture.get();
                for (IdentifyLayerResult identifyLayerResult : identifyLayerResults) {
                    {
                        List<GeoElement> elements = identifyLayerResult.getElements();
                        if (elements.size() > 0 && elements.get(0) instanceof ArcGISFeature && !isFound) {
                            isFound = true;
                            mSelectedArcGISFeature = (ArcGISFeature) elements.get(0);
                            long serviceLayerId = mSelectedArcGISFeature.getFeatureTable().
                                    getServiceLayerId();
                            DFeatureLayer DFeatureLayer = getmFeatureLayerDTG(serviceLayerId);
                            publishProgress(DFeatureLayer);
                        }
                    }
                }
                publishProgress();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
        return null;
    }

    private DFeatureLayer getmFeatureLayerDTG(long serviceLayerId) {
        for (DFeatureLayer DFeatureLayer : mDFeatureLayers) {
            long serviceLayerDTGId = ((ArcGISFeatureTable) DFeatureLayer.getLayer().getFeatureTable()).getServiceLayerId();
            if (serviceLayerDTGId == serviceLayerId) return DFeatureLayer;
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog.setMessage("Đang xử lý...");
        mDialog.setCancelable(false);
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hủy", (dialogInterface, i) -> publishProgress());
        mDialog.show();
    }

    @Override
    protected void onProgressUpdate(DFeatureLayer... values) {
        super.onProgressUpdate(values);
        if (values != null && values.length > 0 && mSelectedArcGISFeature != null) {
//            HoSoVatTuSuCoAsync hoSoVatTuSuCoAsync = new HoSoVatTuSuCoAsync(mActivity, object -> {
//                if (object != null) {
            mApplication.setSelectedArcGISFeature(mSelectedArcGISFeature);
            mPopUp.showPopup(false);
        }
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
//            });
//            hoSoVatTuSuCoAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Constant.HOSOVATTUSUCO_METHOD.FIND, mSelectedArcGISFeature.getAttributes()
//                    .get(mActivity.getString(R.string.Field_SuCo_IDSuCo)));

    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

    }

}