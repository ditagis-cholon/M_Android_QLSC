package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Attachment;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.UserDangNhap;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;
import hcm.ditagis.com.cholon.qlsc.utities.MySnackBar;

/**
 * Created by ThanLe on 4/16/2018.
 */

public class SingleTapAddFeatureAsync extends AsyncTask<Point, Feature, Void> {
    private ProgressDialog mDialog;
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private byte[] mImage;
    private ServiceFeatureTable mServiceFeatureTable;
    private ArcGISFeature mSelectedArcGISFeature;
    @SuppressLint("StaticFieldLeak")
    private MapView mMapView;
    private AsyncResponse mDelegate;
    private android.graphics.Point mClickPoint;
    private Geocoder mGeocoder;

    public interface AsyncResponse {
        void processFinish(Feature output);
    }

    public SingleTapAddFeatureAsync(android.graphics.Point clickPoint, Context context, byte[] image,
                                    ServiceFeatureTable serviceFeatureTable, MapView mapView, Geocoder geocoder, AsyncResponse delegate) {
        this.mServiceFeatureTable = serviceFeatureTable;
        this.mMapView = mapView;
        this.mImage = image;
        this.mContext = context;
        this.mClickPoint = clickPoint;
        this.mDialog = new ProgressDialog(context, android.R.style.Theme_Material_Dialog_Alert);
        this.mDelegate = delegate;
        this.mGeocoder = geocoder;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mDialog.setMessage("Đang xử lý...");
        mDialog.setCancelable(false);
        mDialog.show();
    }

    @Override
    protected Void doInBackground(Point... params) {
        final Point clickPoint = params[0];

        final Feature feature;
        try {
            feature = mServiceFeatureTable.createFeature();
            feature.setGeometry(clickPoint);
            FindLocationAsycn findLocationAsycn = new FindLocationAsycn(mContext, false,
                    mGeocoder, output -> {
                if (output != null) {

                    feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_DiaChi), output.get(0).getLocation());
                    String subAdminArea = output.get(0).getSubAdminArea();
                    if (subAdminArea.equals(mContext.getString(R.string.Quan5Name)))
                        feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_MaQuan), mContext.getString(R.string.Quan5Code));
                    else if (subAdminArea.equals(mContext.getString(R.string.Quan8Name)))
                        feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_MaQuan), mContext.getString(R.string.Quan8Code));
                    else if (subAdminArea.equals(mContext.getString(R.string.Quan6Name)))
                        feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_MaQuan), mContext.getString(R.string.Quan6Code));
                    else if (subAdminArea.equals(mContext.getString(R.string.QuanBinhTanName)))
                        feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_MaQuan), mContext.getString(R.string.QuanBinhTanCode));
                    Short intObj = (short) 0;
                    feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_TrangThai), intObj);
                    feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_DoiQuanLy),
                            UserDangNhap.getInstance().getUser().getRole());

                    String searchStr = "";
                    String timeID;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        timeID = getTimeID();
                        searchStr = mContext.getString(R.string.Field_SuCo_IDSuCo) + " like '%" + timeID + "'";
                    }
                    QueryParameters queryParameters = new QueryParameters();
                    queryParameters.setWhereClause(searchStr);
                    mServiceFeatureTable.queryFeaturesAsync(queryParameters).addDoneListener(() -> addFeatureAsync(feature));
                }
            });
            Geometry project = GeometryEngine.project(clickPoint, SpatialReferences.getWgs84());
            double[] location = {project.getExtent().getCenter().getX(), project.getExtent().getCenter().getY()};
            findLocationAsycn.setmLongtitude(location[0]);
            findLocationAsycn.setmLatitude(location[1]);

            findLocationAsycn.execute();

        } catch (Exception e) {
            MySnackBar.make(mMapView, mContext.getString(R.string.message_error_add_feature), true);
            publishProgress();
        }


        return null;
    }

    private String getTimeID() {
        return Constant.DATE_FORMAT.format(Calendar.getInstance().getTime());
    }

    private void addFeatureAsync(final Feature feature) {
        new GenerateIDSuCoAsycn(mContext, output -> {
            if (output.isEmpty()) {
                publishProgress();
                return;
            }
            feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_IDSuCo), output);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Calendar c = Calendar.getInstance();
                feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_NgayXayRa), c);
                feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_NgayThongBao), c);
            }
            feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_NguoiBaoSuCo), UserDangNhap.getInstance().getUser().getUserName());
            feature.getAttributes().put(mContext.getString(R.string.Field_SuCo_LoaiSuCo), (short) 0);
            //---get DMA begin
            final ListenableFuture<List<IdentifyLayerResult>> listListenableFuture = mMapView.identifyLayersAsync(mClickPoint, 5, false, 1);
            listListenableFuture.addDoneListener(() -> {
                List<IdentifyLayerResult> identifyLayerResults;
                try {
                    identifyLayerResults = listListenableFuture.get();
                    for (IdentifyLayerResult identifyLayerResult : identifyLayerResults) {
                        {
                            List<GeoElement> elements = identifyLayerResult.getElements();
                            if (elements.size() > 0) {
                                if (elements.get(0) instanceof ArcGISFeature) {
                                    mSelectedArcGISFeature = (ArcGISFeature) elements.get(0);
                                }
                            }
                        }
                    }
//                        publishProgress(null);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
            //---get DMA end
            ListenableFuture<Void> mapViewResult = mServiceFeatureTable.addFeatureAsync(feature);
            mapViewResult.addDoneListener(() -> {
                final ListenableFuture<List<FeatureEditResult>> listListenableEditAsync = mServiceFeatureTable.applyEditsAsync();
                listListenableEditAsync.addDoneListener(() -> {
                    try {
                        List<FeatureEditResult> featureEditResults = listListenableEditAsync.get();
                        if (featureEditResults.size() > 0) {
                            long objectId = featureEditResults.get(0).getObjectId();
                            final QueryParameters queryParameters = new QueryParameters();
                            final String query = String.format(mContext.getString(R.string.arcgis_query_by_OBJECTID), objectId);
                            queryParameters.setWhereClause(query);
                            final ListenableFuture<FeatureQueryResult> featuresAsync = mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                            featuresAsync.addDoneListener(() -> {
                                try {
                                    FeatureQueryResult result = featuresAsync.get();
                                    if (result.iterator().hasNext()) {
                                        Feature item = result.iterator().next();
                                        if (mImage != null)
                                            addAttachment(featuresAsync, item);
                                        else publishProgress(item);
                                    }
                                } catch (InterruptedException | ExecutionException e) {
                                    e.printStackTrace();
                                }

                            });
                        }
                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }

                });
            });
        }).execute();


    }

    private void addAttachment(ListenableFuture<FeatureQueryResult> listenableFuture, final Feature feature) {
        FeatureQueryResult result;
        try {

            result = listenableFuture.get();
            if (result.iterator().hasNext()) {
                Feature item = result.iterator().next();
                mSelectedArcGISFeature = (ArcGISFeature) item;
                final String attachmentName = mContext.getString(R.string.attachment) + "_" + System.currentTimeMillis() + ".png";
                final ListenableFuture<Attachment> addResult = mSelectedArcGISFeature.addAttachmentAsync(mImage, Bitmap.CompressFormat.PNG.toString(), attachmentName);
                addResult.addDoneListener(() -> {
                    if (mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                    try {
                        Attachment attachment = addResult.get();
                        if (attachment.getSize() > 0) {
                            final ListenableFuture<Void> tableResult = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature);
                            tableResult.addDoneListener(() -> {
                                final ListenableFuture<List<FeatureEditResult>> updatedServerResult = mServiceFeatureTable.applyEditsAsync();
                                updatedServerResult.addDoneListener(() -> {
                                    List<FeatureEditResult> edits;
                                    try {
                                        edits = updatedServerResult.get();
                                        if (edits.size() > 0) {
                                            if (!edits.get(0).hasCompletedWithErrors()) {
                                                publishProgress(feature);
                                            }
                                        }
                                    } catch (InterruptedException | ExecutionException e) {
                                        e.printStackTrace();
                                    }
                                    if (mDialog != null && mDialog.isShowing()) {
                                        mDialog.dismiss();
                                    }

                                });


                            });
                        }

                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
                Envelope extent = item.getGeometry().getExtent();
                mMapView.setViewpointGeometryAsync(extent);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onProgressUpdate(Feature... values) {
        if (values == null)
            this.mDelegate.processFinish(null);
        else if (values.length > 0) this.mDelegate.processFinish(values[0]);
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }


    @Override
    protected void onPostExecute(Void result) {


    }

}