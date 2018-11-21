package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Attachment;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;


/**
 * Created by ThanLe on 4/16/2018.
 */

public class AddFeatureAsync extends AsyncTask<Void, Feature, Void> {
    @SuppressLint("StaticFieldLeak")
    private Activity mActivity;
    private ServiceFeatureTable mServiceFeatureTable;
    @SuppressLint("StaticFieldLeak")
    private AsyncResponse mDelegate;
    private DApplication mApplication;
    private LinearLayout mLLayoutField;

    private HashMap<String, Object> mAttributes;

    private Object mThongTinPhanAnh;
    private String mGhiChu;

    public interface AsyncResponse {
        void processFinish(Feature output);
    }

    public AddFeatureAsync(Activity activity,
                           ServiceFeatureTable serviceFeatureTable, LinearLayout layout, AsyncResponse delegate) {
        this.mServiceFeatureTable = serviceFeatureTable;
        this.mActivity = activity;
        this.mApplication = (DApplication) activity.getApplication();
        this.mDelegate = delegate;
        this.mLLayoutField = layout;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mAttributes = getAttributes();
    }

    @Override
    protected Void doInBackground(Void... params) {
        final Feature feature;
        try {
            feature = mServiceFeatureTable.createFeature();
            feature.setGeometry(mApplication.getAddFeaturePoint());
            for (String alias : mAttributes.keySet()) {
                for (Field field : mServiceFeatureTable.getFields()) {
                    if (field.getAlias().equals(alias)) {
                        try {
                            String value = mAttributes.get(alias).toString().trim();
                            if (value.isEmpty())
                                continue;
                            switch (field.getFieldType()) {
                                case TEXT:
                                    feature.getAttributes().put(field.getName(), value);
                                    break;
                                case DOUBLE:
                                    feature.getAttributes().put(field.getName(), Double.parseDouble(value));
                                    break;
                                case FLOAT:
                                    feature.getAttributes().put(field.getName(), Float.parseFloat(value));
                                    break;
                                case INTEGER:
                                    feature.getAttributes().put(field.getName(), Integer.parseInt(value));
                                    break;
                                case SHORT:
                                    feature.getAttributes().put(field.getName(), Short.parseShort(value));
                                    break;
                            }

                        } catch (Exception e) {
                            Log.e("Lỗi thêm điểm", e.toString());
                        }
                        break;
                    }
                }
            }
            addFeatureAsync(feature);

        } catch (Exception e) {
            publishProgress();
        }
        return null;
    }

    private Object getCodeDomain(List<CodedValue> codedValues, String value) {
        Object code = null;
        for (CodedValue codedValue : codedValues) {
            if (codedValue.getName().equals(value)) {
                code = codedValue.getCode();
                break;
            }
        }
        return code;
    }

    private void addFeatureAsync(final Feature feature) {
        ListenableFuture<Void> mapViewResult = mServiceFeatureTable.addFeatureAsync(feature);
        mapViewResult.addDoneListener(() -> {
            final ListenableFuture<List<FeatureEditResult>> listListenableEditAsync = mServiceFeatureTable.applyEditsAsync();
            listListenableEditAsync.addDoneListener(() -> {
                try {
                    List<FeatureEditResult> edits = listListenableEditAsync.get();
                    if (edits != null && edits.size() > 0) {
                        if (!edits.get(0).hasCompletedWithErrors()) {
                            long objectId = edits.get(0).getObjectId();
                            new NotifyServerAddingFeature(mActivity.getApplicationContext(), output -> {
                                if (output != null && output.length() > 0) {
                                    final QueryParameters queryParameters = new QueryParameters();
                                    final String query = String.format("%s = '%s'", Constant.FieldSuCo.ID_SUCO, output);
                                    queryParameters.setWhereClause(query);
                                    final ListenableFuture<FeatureQueryResult> featuresAsync = mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                                    featuresAsync.addDoneListener(() -> {
                                        try {
                                            FeatureQueryResult result = featuresAsync.get();
                                            if (result.iterator().hasNext()) {
                                                Feature item = result.iterator().next();
                                                if (mApplication.getImages() != null && mApplication.getImages().size() > 0)
                                                    addAttachment((ArcGISFeature) item, item);
                                                else publishProgress(item);
                                            } else publishProgress();
                                        } catch (InterruptedException | ExecutionException e) {
                                            e.printStackTrace();
                                            publishProgress();
                                        }

                                    });
                                }
                            }).execute(objectId + "");

                        } else {
                            publishProgress();

                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    publishProgress();
                }

            });
        });
    }


    private HashMap<String, Object> getAttributes() {
        HashMap<String, Object> attributes = new HashMap<>();
//        try {
        String currentAlias = "";
        int countEmpty = 0;
        for (int i = 0; i < mLLayoutField.getChildCount(); i++) {
            LinearLayout itemAddFeature = (LinearLayout) mLLayoutField.getChildAt(i);
            for (int j = 0; j < itemAddFeature.getChildCount(); j++) {
                LinearLayout typeInput_itemAddFeature = (LinearLayout) itemAddFeature.getChildAt(j);
                for (int k = 0; k < typeInput_itemAddFeature.getChildCount(); k++) {
                    View view = typeInput_itemAddFeature.getChildAt(k);
                    if (view.getVisibility() == View.VISIBLE) {
                        if (view instanceof EditText && !currentAlias.isEmpty()) {
                            String value = ((EditText) view).getText().toString();
                            if (value.length() == 0)
                                countEmpty++;
                            else

                                for (Field field : mServiceFeatureTable.getFields()) {
                                    if (field.getAlias().equals(currentAlias)) {
                                        if (field.getName().equals(Constant.FieldSuCo.GHI_CHU)) {
                                            mGhiChu = value;
                                        }
                                        if (field.getDomain() != null) {
                                            List<CodedValue> codedValues = ((CodedValueDomain) field.getDomain()).getCodedValues();

                                            Object valueDomain = getCodeDomain(codedValues, ((EditText) view).getText().toString());
                                            if (valueDomain != null)
                                                attributes.put(currentAlias, valueDomain.toString());
                                            else countEmpty++;
                                        } else {
                                            attributes.put(currentAlias, ((EditText) view).getText().toString());
                                        }
                                        break;
                                    }
                                }
                        } else if (view instanceof Spinner && !currentAlias.isEmpty()) {
                            if (((Spinner) view).getSelectedItemPosition() == 0)
                                countEmpty++;
                            else
                                for (Field field : mServiceFeatureTable.getFields()) {
                                    if (field.getAlias().equals(currentAlias)) {
                                        if (field.getDomain() != null) {
                                            List<CodedValue> codedValues = ((CodedValueDomain) field.getDomain()).getCodedValues();

                                            Object codeDomain = getCodeDomain(codedValues, ((Spinner) view).getSelectedItem().toString());
                                            if (field.getName().equals(Constant.FieldSuCo.THONG_TIN_PHAN_ANH))
                                                mThongTinPhanAnh = codeDomain;
                                            if (codeDomain != null)
                                                attributes.put(currentAlias, codeDomain.toString());
                                            else countEmpty++;
                                        } else {
                                        }
                                        break;
                                    }
                                }
                        } else if (view instanceof TextView) {
                            currentAlias = ((TextView) view).getText().toString();
                            attributes.put(currentAlias, null);
                        }
                    }
                }
            }
        }
        if (countEmpty == 5 || (mGhiChu == null || mGhiChu.length() == 0) && (mThongTinPhanAnh != null ? Short.parseShort(mThongTinPhanAnh.toString()) == 0 : false))
            publishProgress();
//        } catch (Exception e) {
//            Log.e("Lỗi lấy attributes", e.toString());
//        }
        return attributes;
    }

    private void addAttachment(ArcGISFeature arcGISFeature, final Feature feature) {
        for (byte[] image : mApplication.getImages()) {
            @SuppressLint("StringFormatMatches") String attachmentName = mActivity.getApplicationContext()
                    .getString(R.string.attachment_name, System.currentTimeMillis() + "");
            final ListenableFuture<Attachment> addResult = arcGISFeature.addAttachmentAsync(
                    image, Constant.FileType.PNG, attachmentName);
        }
        final ListenableFuture<Void> tableResult = mServiceFeatureTable.updateFeatureAsync(arcGISFeature);
//            tableResult.addDoneListener(() -> {
        final ListenableFuture<List<FeatureEditResult>> updatedServerResult = mServiceFeatureTable.applyEditsAsync();
        updatedServerResult.addDoneListener(() -> {
            try {
                List<FeatureEditResult> edits = updatedServerResult.get();
                if (edits.size() > 0) {
                    if (!edits.get(0).hasCompletedWithErrors()) {
                        publishProgress(feature);
                    } else publishProgress();
                } else publishProgress();
            } catch (InterruptedException | ExecutionException e) {
                publishProgress();
                e.printStackTrace();
            }

        });
    }


    @Override
    protected void onProgressUpdate(Feature... values) {
        if (values == null || values.length == 0) {
            Toast.makeText(mActivity.getApplicationContext(), "Nhập thiếu dữ liệu hoặc có lỗi xảy ra", Toast.LENGTH_SHORT).show();
            this.mDelegate.processFinish(null);
        } else if (values.length > 0) this.mDelegate.processFinish(values[0]);
    }


    @Override
    protected void onPostExecute(Void result) {


    }

}