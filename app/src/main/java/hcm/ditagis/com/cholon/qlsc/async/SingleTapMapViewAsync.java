package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
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

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.FeatureLayerDTG;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;
import hcm.ditagis.com.cholon.qlsc.utities.Popup;

/**
 * Created by ThanLe on 4/16/2018.
 */

public class SingleTapMapViewAsync extends AsyncTask<Point, FeatureLayerDTG, Void> {
    private ProgressDialog mDialog;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private List<FeatureLayerDTG> mFeatureLayerDTGs;
    @SuppressLint("StaticFieldLeak")
    private MapView mMapView;
    private ArcGISFeature mSelectedArcGISFeature;
    @SuppressLint("StaticFieldLeak")
    private Popup mPopUp;
    private android.graphics.Point mClickPoint;
    private boolean isFound = false;

    public SingleTapMapViewAsync(Context context, List<FeatureLayerDTG> featureLayerDTGS, Popup popup,
                                 android.graphics.Point clickPoint, MapView mapview) {
        this.mMapView = mapview;
        this.mFeatureLayerDTGs = featureLayerDTGS;
        this.mPopUp = popup;
        this.mClickPoint = clickPoint;
        this.mContext = context;
        this.mDialog = new ProgressDialog(context, android.R.style.Theme_Material_Dialog_Alert);
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
                            FeatureLayerDTG featureLayerDTG = getmFeatureLayerDTG(serviceLayerId);
                            publishProgress(featureLayerDTG);
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

    private FeatureLayerDTG getmFeatureLayerDTG(long serviceLayerId) {
        for (FeatureLayerDTG featureLayerDTG : mFeatureLayerDTGs) {
            long serviceLayerDTGId = ((ArcGISFeatureTable) featureLayerDTG.getLayer().getFeatureTable()).getServiceLayerId();
            if (serviceLayerDTGId == serviceLayerId) return featureLayerDTG;
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
    protected void onProgressUpdate(FeatureLayerDTG... values) {
        super.onProgressUpdate(values);
        if (values != null && values.length > 0 && mSelectedArcGISFeature != null) {
            HoSoVatTuSuCoAsync hoSoVatTuSuCoAsync = new HoSoVatTuSuCoAsync(mContext, object -> {
                if (object != null) {

                    FeatureLayerDTG featureLayerDTG = values[0];
                    mPopUp.setFeatureLayerDTG(featureLayerDTG);
                    mPopUp.showPopup(mSelectedArcGISFeature, false);
                }
                if (mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            });
            hoSoVatTuSuCoAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Constant.HOSOVATTUSUCO_METHOD.FIND, mSelectedArcGISFeature.getAttributes()
                    .get(mContext.getString(R.string.Field_SuCo_IDSuCo)));
        } else if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

    }

}