package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.loadable.LoadStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.HoSoVatTuSuCo;
import hcm.ditagis.com.cholon.qlsc.entities.VatTu;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.LayerInfoDTG;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.ListObjectDB;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;

public class HoSoVatTuSuCoAsync extends AsyncTask<Object, Object, Void> {
    @SuppressLint("StaticFieldLeak")
    private Context mContext;
    private ServiceFeatureTable mServiceFeatureTable;
    private AsyncResponse mDelegate;

    public interface AsyncResponse {
        void processFinish(Object object);
    }

    public HoSoVatTuSuCoAsync(Context context, AsyncResponse response) {
        this.mContext = context;
        this.mDelegate = response;
        for (final LayerInfoDTG layerInfoDTG : ListObjectDB.getInstance().getLstFeatureLayerDTG()) {
            if (layerInfoDTG.getId().equals(mContext.getString(R.string.IDLayer_DiemSuCo))) {
                String url = layerInfoDTG.getUrl();
                if (!layerInfoDTG.getUrl().startsWith("http"))
                    url = "http:" + layerInfoDTG.getUrl();
                url = url.replace("/0", "/1"); // hồ sơ vật tư sự cố
                mServiceFeatureTable = new ServiceFeatureTable(url);
            }
        }
    }

    private void find(String idSuCo) {
        QueryParameters queryParameters = new QueryParameters();
        final List<HoSoVatTuSuCo> list = new ArrayList<>();
        String queryClause = String.format("%s = '%s'", mContext.getString(R.string.Field_HoSoVatTuSuCo_IDSuCo), idSuCo);
//        String queryClause = "1 = 1";
        queryParameters.setWhereClause(queryClause);
        final ListenableFuture<FeatureQueryResult> queryResultListenableFuture =
                this.mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
        queryResultListenableFuture.addDoneListener(() -> {
            try {
                FeatureQueryResult result = queryResultListenableFuture.get();
                for (Iterator it = result.iterator(); it.hasNext(); ) {
                    Feature feature = (Feature) it.next();
                    Map<String, Object> attributes = feature.getAttributes();
                    String maVatTu = attributes.get(mContext.getString(R.string.Field_HoSoVatTuSuCo_MaVatTu)).toString();
                    boolean isFound = false;

                    //Lấy tên vật tư và mã vật tư
                    for (VatTu vatTu : ListObjectDB.getInstance().getVatTuOngChinhs())
                        if (vatTu.getMaVatTu().equals(maVatTu)) {
                            list.add(new HoSoVatTuSuCo(attributes.get(mContext.getString(R.string.Field_HoSoVatTuSuCo_IDSuCo)).toString(),
                                    Double.parseDouble(attributes.get(mContext.getString(R.string.Field_HoSoVatTuSuCo_SoLuong)).toString()),
                                    attributes.get(mContext.getString(R.string.Field_HoSoVatTuSuCo_MaVatTu)).toString(),
                                    vatTu.getTenVatTu(), vatTu.getDonViTinh()));
                            isFound = true;
                            break;
                        }
                    if (!isFound)
                        for (VatTu vatTu : ListObjectDB.getInstance().getVatTuOngNganhs())
                            if (vatTu.getMaVatTu().equals(maVatTu)) {
                                list.add(new HoSoVatTuSuCo(attributes.get(mContext.getString(R.string.Field_HoSoVatTuSuCo_IDSuCo)).toString(),
                                        Double.parseDouble(attributes.get(mContext.getString(R.string.Field_HoSoVatTuSuCo_SoLuong)).toString()),
                                        attributes.get(mContext.getString(R.string.Field_HoSoVatTuSuCo_MaVatTu)).toString(),
                                        vatTu.getTenVatTu(), vatTu.getDonViTinh()));
                                break;
                            }
                }
                ListObjectDB.getInstance().setHoSoVatTuSuCos(list);
                publishProgress(list);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                publishProgress();
            }
        });
    }

    private void delete() {
        QueryParameters queryParameters = new QueryParameters();
        String queryClause = "1 = 1";
        queryParameters.setWhereClause(queryClause);
        final ListenableFuture<FeatureQueryResult> queryResultListenableFuture = this.mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
        queryResultListenableFuture.addDoneListener(() -> {
            try {
                FeatureQueryResult result = queryResultListenableFuture.get();

                mServiceFeatureTable.deleteFeaturesAsync(result).addDoneListener(() -> {
                    ListenableFuture<List<FeatureEditResult>> listListenableFuture = mServiceFeatureTable.applyEditsAsync();
                    listListenableFuture.addDoneListener(() -> {
                        try {
                            if (listListenableFuture.get().size() > 0) {
                                //xóa thành công
                                insert(ListObjectDB.getInstance().getLstHoSoVatTuSuCoInsert());
                            } else {
                                publishProgress(false);
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                            publishProgress(false);
                        }
                    });
                });
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                publishProgress(false);
            }
        });
    }


    private void insert(List<HoSoVatTuSuCo> hoSoVatTuSuCos) {
        List<Feature> features = new ArrayList<>();
        for (HoSoVatTuSuCo hoSoVatTuSuCo : hoSoVatTuSuCos) {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put(mContext.getString(R.string.Field_HoSoVatTuSuCo_IDSuCo), hoSoVatTuSuCo.getIdSuCo());
            attributes.put(mContext.getString(R.string.Field_HoSoVatTuSuCo_MaVatTu), hoSoVatTuSuCo.getMaVatTu());
            attributes.put(mContext.getString(R.string.Field_HoSoVatTuSuCo_SoLuong), hoSoVatTuSuCo.getSoLuong());

            Feature feature = mServiceFeatureTable.createFeature();
            feature.getAttributes().put(mContext.getString(R.string.Field_HoSoVatTuSuCo_IDSuCo), hoSoVatTuSuCo.getIdSuCo());
            feature.getAttributes().put(mContext.getString(R.string.Field_HoSoVatTuSuCo_MaVatTu), hoSoVatTuSuCo.getMaVatTu());
            feature.getAttributes().put(mContext.getString(R.string.Field_HoSoVatTuSuCo_SoLuong), hoSoVatTuSuCo.getSoLuong());

            features.add(feature);
        }
        mServiceFeatureTable.addFeaturesAsync(features).addDoneListener(() -> {
            ListenableFuture<List<FeatureEditResult>> listListenableFuture = mServiceFeatureTable.applyEditsAsync();
            listListenableFuture.addDoneListener(() -> {
                try {
                    List<FeatureEditResult> featureEditResults = listListenableFuture.get();
                    if (featureEditResults.size() > 0) {
                        ListObjectDB.getInstance().clearListHoSoVatTuSuCoChange();
                        publishProgress(true);
                    } else {
                        publishProgress(false);
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    publishProgress(false);
                }
            });

        });


    }

    @Override
    protected Void doInBackground(Object... objects) {
        mServiceFeatureTable.loadAsync();
        mServiceFeatureTable.addLoadStatusChangedListener(loadStatusChangedEvent -> {
            if (loadStatusChangedEvent.getNewLoadStatus().equals(LoadStatus.LOADED)) {
                if (objects != null && objects.length > 0) {
                    switch (Integer.parseInt(objects[0].toString())) {
                        case Constant.HOSOVATTUSUCO_METHOD.FIND:
                            if (objects.length > 1 && objects[1] instanceof String) {
                                find(objects[1].toString());
                            }
                            break;
                        case Constant.HOSOVATTUSUCO_METHOD.INSERT:
                            delete();
                            break;

                    }
                }
            } else {
                publishProgress();
                Log.e("Load table", "không loaded");
            }
        });

        return null;
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        this.mDelegate.processFinish(values);
    }
}
