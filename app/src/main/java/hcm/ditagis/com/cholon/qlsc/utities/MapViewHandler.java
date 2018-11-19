package hcm.ditagis.com.cholon.qlsc.utities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Geocoder;
import android.view.MotionEvent;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
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

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.MainActivity;
import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.adapter.TraCuuAdapter;
import hcm.ditagis.com.cholon.qlsc.async.QueryServiceFeatureTableAsync;
import hcm.ditagis.com.cholon.qlsc.async.SingleTapAddFeatureAsync;
import hcm.ditagis.com.cholon.qlsc.async.SingleTapMapViewAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.FeatureLayerDTG;

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

    public void setFeatureLayerDTGs(List<FeatureLayerDTG> mFeatureLayerDTGs) {
        this.mFeatureLayerDTGs = mFeatureLayerDTGs;
    }

    private List<FeatureLayerDTG> mFeatureLayerDTGs;

    public MapViewHandler(MainActivity activity, FeatureLayerDTG featureLayerDTG, Callout mCallout, MapView mapView,
                          Popup popupInfos, Context mContext, Geocoder geocoder) {
        this.mActivity = activity;
        mApplication = (DApplication) activity.getApplication();
        this.mCallout = mCallout;
        this.mMapView = mapView;
        this.mServiceFeatureTable = (ServiceFeatureTable) featureLayerDTG.getLayer().getFeatureTable();
        this.mPopUp = popupInfos;
        this.mContext = mContext;
        this.suCoTanHoaLayer = featureLayerDTG.getLayer();
        this.mGeocoder = geocoder;
    }

    public void setClickBtnAdd(boolean clickBtnAdd) {
        isClickBtnAdd = clickBtnAdd;
    }

    public void addFeature(byte[] image, Point pointFindLocation) {
        mClickPoint = mMapView.locationToScreen(pointFindLocation);

        SingleTapAddFeatureAsync singleTapAdddFeatureAsync = new SingleTapAddFeatureAsync(mClickPoint, mContext,
                image, mServiceFeatureTable, mMapView, mGeocoder, output -> {
            if (output != null && MainActivity.FeatureLayerDTGDiemSuCo != null) {
                mApplication.setSelectedArcGISFeature((ArcGISFeature) output);
                mPopUp.showPopup(true);
            }
        });
//        Point add_point = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).getTargetGeometry().getExtent().getCenter();
        singleTapAdddFeatureAsync.execute(pointFindLocation);
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

            SingleTapMapViewAsync singleTapMapViewAsync = new SingleTapMapViewAsync(mActivity, mFeatureLayerDTGs, mPopUp, mClickPoint, mMapView);
            singleTapMapViewAsync.execute(clickPoint);
        }
    }

    public void queryByObjectID(long objectID) {
        final QueryParameters queryParameters = new QueryParameters();
        final String query = mActivity.getString(R.string.arcgis_query_by_OBJECTID, objectID);
        queryParameters.setWhereClause(query);
        final ListenableFuture<FeatureQueryResult> feature =
                mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
        feature.addDoneListener(() -> {
            try {
                FeatureQueryResult result = feature.get();
                if (result.iterator().hasNext()) {
                    Feature item = result.iterator().next();
                    Envelope extent = item.getGeometry().getExtent();

                    mMapView.setViewpointGeometryAsync(extent);
                    suCoTanHoaLayer.selectFeature(item);
                    if (MainActivity.FeatureLayerDTGDiemSuCo != null) {
                        mSelectedArcGISFeature = (ArcGISFeature) item;
                        if (mSelectedArcGISFeature != null) {
                            mApplication.setSelectedArcGISFeature(mSelectedArcGISFeature);
                            mPopUp.showPopup(false);
                        }
                    }
                }

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });
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

    public void querySearch(String searchStr, final TraCuuAdapter adapter) {
        adapter.clear();
        adapter.notifyDataSetChanged();
        mCallout.dismiss();

        suCoTanHoaLayer.clearSelection();
        QueryParameters queryParameters = new QueryParameters();
        StringBuilder builder = new StringBuilder();
        for (Field field : mServiceFeatureTable.getFields()) {
            switch (field.getFieldType()) {
                case OID:
                case INTEGER:
                case SHORT:
                    try {
                        int search = Integer.parseInt(searchStr);
                        builder.append(String.format("%s = %s", field.getName(), search));
                        builder.append(" or ");
                    } catch (Exception ignored) {

                    }
                    break;
                case FLOAT:
                case DOUBLE:
                    try {
                        double search = Double.parseDouble(searchStr);
                        builder.append(String.format("%s = %s", field.getName(), search));
                        builder.append(" or ");
                    } catch (Exception ignored) {

                    }
                    break;
                case TEXT:
                    builder.append(field.getName()).append(" like N'%").append(searchStr).append("%'");
                    builder.append(" or ");
                    break;
            }
        }
        builder.append(" 1 = 2 ");
        queryParameters.setWhereClause(builder.toString());
        final ListenableFuture<FeatureQueryResult> feature = mServiceFeatureTable.queryFeaturesAsync(queryParameters);
        feature.addDoneListener(() -> {
            try {
                FeatureQueryResult result = feature.get();
                Iterator iterator = result.iterator();
                while (iterator.hasNext()) {
                    Feature item = (Feature) iterator.next();
                    Map<String, Object> attributes = item.getAttributes();
                    String format_date = "";
                    String[] split = attributes.get(mContext.getString(R.string.Field_SuCo_IDSuCo)).toString().split("_");
                    try {
                        format_date = Constant.DATE_FORMAT.format((new GregorianCalendar(Integer.parseInt(split[3]),
                                Integer.parseInt(split[2]), Integer.parseInt(split[1])).getTime()));
                    } catch (Exception ignored) {

                    }
                    String viTri = "";
                    try {
                        viTri = attributes.get(mContext.getString(R.string.Field_SuCo_DiaChi)).toString();
                    } catch (Exception ignored) {

                    }
                    adapter.add(new TraCuuAdapter.Item(Integer.parseInt(attributes.get(mContext.getString(R.string.Field_OBJECTID)).toString()),
                            attributes.get(mContext.getString(R.string.Field_SuCo_IDSuCo)).toString(),
                            Integer.parseInt(attributes.get(mContext.getString(R.string.Field_SuCo_TrangThai)).toString()), format_date, viTri));
                    adapter.notifyDataSetChanged();

//                        queryByObjectID(Integer.parseInt(attributes.get(Constant.OBJECT_ID).toString()));
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        });

    }

}