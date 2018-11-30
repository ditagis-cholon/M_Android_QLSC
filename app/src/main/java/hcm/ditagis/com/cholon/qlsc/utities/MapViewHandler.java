package hcm.ditagis.com.cholon.qlsc.utities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Geocoder;
import android.view.MotionEvent;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.MainActivity;
import hcm.ditagis.com.cholon.qlsc.async.QueryServiceFeatureTableAsync;
import hcm.ditagis.com.cholon.qlsc.async.QueryServiceFeatureTableGetListAsync;
import hcm.ditagis.com.cholon.qlsc.async.SingleTapMapViewAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.DFeatureLayer;
import hcm.ditagis.com.cholon.qlsc.fragment.task.HandlingSearchHasDone;

/**
 * Created by ThanLe on 2/2/2018.
 */

@SuppressLint("Registered")
public class MapViewHandler extends Activity {
    private final FeatureLayer suCoTanHoaLayer;
    private Callout mCallout;
    private android.graphics.Point mClickPoint;
    private ArcGISFeature mSelectedArcGISFeature;
    private MapView mMapView;
    private boolean isClickBtnAdd = false;
    private ServiceFeatureTable mServiceFeatureTable;
    private Popup mPopUp;
    private Context mContext;
    private Geocoder mGeocoder;
    private DApplication mApplication;
    private MainActivity mActivity;

    public void setFeatureLayerDTGs(List<DFeatureLayer> mDFeatureLayers) {
        this.mDFeatureLayers = mDFeatureLayers;
    }

    private List<DFeatureLayer> mDFeatureLayers;

    public MapViewHandler(MainActivity activity, DFeatureLayer DFeatureLayer, Callout mCallout, MapView mapView,
                          Popup popupInfos, Context mContext, Geocoder geocoder) {
        this.mActivity = activity;
        mApplication = (DApplication) activity.getApplication();
        this.mCallout = mCallout;
        this.mMapView = mapView;
        this.mServiceFeatureTable = (ServiceFeatureTable) DFeatureLayer.getLayer().getFeatureTable();
        this.mPopUp = popupInfos;
        this.mContext = mContext;
        this.suCoTanHoaLayer = DFeatureLayer.getLayer();
        this.mGeocoder = geocoder;
    }

    public void setClickBtnAdd(boolean clickBtnAdd) {
        isClickBtnAdd = clickBtnAdd;
    }


    public double[] onScroll(MotionEvent e2) {
        Point center = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).getTargetGeometry().getExtent().getCenter();
        Geometry project = GeometryEngine.project(center, SpatialReferences.getWgs84());
        double[] location = {project.getExtent().getCenter().getX(), project.getExtent().getCenter().getY()};
        mClickPoint = new android.graphics.Point((int) e2.getX(), (int) e2.getY());
//        Geometry geometry = GeometryEngine.project(project, SpatialReferences.getWebMercator());
        return location;
    }

    public void onSingleTapMapView(MotionEvent e) {
        final Point clickPoint = mMapView.screenToLocation(new android.graphics.Point(Math.round(e.getX()), Math.round(e.getY())));
        mClickPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());
        if (isClickBtnAdd) {
            mMapView.setViewpointCenterAsync(clickPoint, 10);
        } else {

            SingleTapMapViewAsync singleTapMapViewAsync = new SingleTapMapViewAsync(mActivity, mDFeatureLayers, mPopUp, mClickPoint, mMapView);
            singleTapMapViewAsync.execute(clickPoint);
        }
    }


    public void query(String query) {

        final QueryParameters queryParameters = new QueryParameters();
        queryParameters.setWhereClause(query);
        final ListenableFuture<FeatureQueryResult> feature;
        feature = mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
        feature.addDoneListener(() -> {
            try {
                FeatureQueryResult result = feature.get();
                if (result.iterator().hasNext()) {
                    Feature item = result.iterator().next();
                    if (item != null) {
                        if (item.getGeometry() != null) {
                            Envelope extent = item.getGeometry().getExtent();
                            mApplication.setGeometry(item.getGeometry());
                            mMapView.setViewpointGeometryAsync(extent);
                        }
                        if (suCoTanHoaLayer != null) {
                            suCoTanHoaLayer.selectFeature(item);
                            String queryClause = String.format("%s = '%s' ",
                                    Constant.FieldSuCo.ID_SUCO, item.getAttributes().get(Constant.FieldSuCo.ID_SUCO).toString());
                            QueryParameters queryParameters1 = new QueryParameters();
                            queryParameters1.setWhereClause(queryClause);
                            new QueryServiceFeatureTableAsync(mActivity, output -> {
                                if (output != null) {
                                    mApplication.setSelectedArcGISFeature((ArcGISFeature) output);
                                    mPopUp.showPopup(false);
                                }
                            }).execute(queryParameters1);

                        }
                    }
                }

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
    }
}