package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Attachment;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureType;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewMoreInfoAdapter;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;


/**
 * Created by ThanLe on 4/16/2018.
 */

public class EditAsync extends AsyncTask<FeatureViewMoreInfoAdapter, ArcGISFeature, Void> {
    @SuppressLint("StaticFieldLeak")
    private ServiceFeatureTable mServiceFeatureTable;
    private ArcGISFeature mSelectedArcGISFeature;
    private byte[] mImage;
    private AsyncResponse mDelegate;
    private DApplication mApplication;
    private LinearLayout mLLayoutField;
    private HashMap<String, Object> mAttributes;
    private boolean mIsComplete;

    public interface AsyncResponse {
        void processFinish(ArcGISFeature feature);
    }

    public EditAsync(Activity activity, boolean isComplete,
                     ArcGISFeature selectedArcGISFeature, LinearLayout layout, byte[] image,
                     AsyncResponse delegate) {
        mLLayoutField = layout;
        mIsComplete = isComplete;
        mApplication = (DApplication) activity.getApplication();
        mServiceFeatureTable = (ServiceFeatureTable) selectedArcGISFeature.getFeatureTable();
        this.mDelegate = delegate;
        mSelectedArcGISFeature = selectedArcGISFeature;
        this.mImage = image;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (mImage == null) mAttributes = getAttributes();

    }

    private HashMap<String, Object> getAttributes() {
        HashMap<String, Object> attributes = new HashMap<>();
//        try {
        String currentAlias = "";
        for (int i = 0; i < mLLayoutField.getChildCount(); i++) {
            LinearLayout itemAddFeature = (LinearLayout) mLLayoutField.getChildAt(i);
            for (int j = 0; j < itemAddFeature.getChildCount(); j++) {
                LinearLayout typeInput_itemAddFeature = (LinearLayout) itemAddFeature.getChildAt(j);
                for (int k = 0; k < typeInput_itemAddFeature.getChildCount(); k++) {
                    View view = typeInput_itemAddFeature.getChildAt(k);
                    if (view.getVisibility() == View.VISIBLE) {
                        if (view instanceof EditText && !currentAlias.isEmpty()) {
                            for (Field field : mServiceFeatureTable.getFields()) {
                                if (field.getAlias().equals(currentAlias)) {
                                    if (field.getDomain() != null) {
                                        List<CodedValue> codedValues = ((CodedValueDomain) field.getDomain()).getCodedValues();

                                        Object valueDomain = getCodeDomain(codedValues, ((EditText) view).getText().toString());
                                        if (valueDomain != null)
                                            attributes.put(currentAlias, valueDomain.toString());
                                    } else {
                                        attributes.put(currentAlias, ((EditText) view).getText().toString());
                                    }
                                    break;
                                }
                            }
                        } else if (view instanceof Spinner && !currentAlias.isEmpty()) {
                            for (Field field : mServiceFeatureTable.getFields()) {
                                if (field.getAlias().equals(currentAlias)) {
                                    if (field.getDomain() != null) {
                                        List<CodedValue> codedValues = ((CodedValueDomain) field.getDomain()).getCodedValues();

                                        Object valueDomain = getCodeDomain(codedValues, ((Spinner) view).getSelectedItem().toString());
                                        if (valueDomain != null)
                                            attributes.put(currentAlias, valueDomain.toString());
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

//        } catch (Exception e) {
//            Log.e("Lỗi lấy attributes", e.toString());
//        }
        return attributes;
    }

    @Override
    protected Void doInBackground(FeatureViewMoreInfoAdapter... params) {
        if (mImage == null) {
            for (String alias : mAttributes.keySet()) {
                for (Field field : mServiceFeatureTable.getFields()) {
                    if (field.getAlias().equals(alias)) {
                        try {
                            String value = mAttributes.get(alias).toString().trim();
//                            if (value.isEmpty())
//                                continue;
                            switch (field.getFieldType()) {
                                case TEXT:
                                    mSelectedArcGISFeature.getAttributes().put(field.getName(), value);
                                    break;
                                case DOUBLE:
                                    mSelectedArcGISFeature.getAttributes().put(field.getName(), Double.parseDouble(value));
                                    break;
                                case FLOAT:
                                    mSelectedArcGISFeature.getAttributes().put(field.getName(), Float.parseFloat(value));
                                    break;
                                case INTEGER:
                                    mSelectedArcGISFeature.getAttributes().put(field.getName(), Integer.parseInt(value));
                                    break;
                                case SHORT:
                                    mSelectedArcGISFeature.getAttributes().put(field.getName(), Short.parseShort(value));
                                    break;
                            }

                        } catch (Exception e) {
                            mSelectedArcGISFeature.getAttributes().put(field.getName(), null);
                            Log.e("Lỗi thêm điểm", e.toString());
                        }
                        break;
                    }
                }
            }
        }
        if (mIsComplete)
            mSelectedArcGISFeature.getAttributes().put(Constant.FieldSuCo.TRANG_THAI, Constant.TrangThaiSuCo.HOAN_THANH);
        mServiceFeatureTable.loadAsync();
        mServiceFeatureTable.addDoneLoadingListener(() -> {
            // update feature in the feature table
            mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature).addDoneListener(() ->
                    mServiceFeatureTable.applyEditsAsync().addDoneListener(() -> {
                        if (mImage != null) {
                            if (mSelectedArcGISFeature.canEditAttachments())
                                addAttachment();
                            else
                                applyEdit();
                        } else {
                            applyEdit();

                        }
                    }));
        });
        return null;
    }

    private void addAttachment() {
        final String attachmentName = "attachment_" + System.currentTimeMillis() + ".png";
        final ListenableFuture<Attachment> addResult = mSelectedArcGISFeature.addAttachmentAsync(mImage, Bitmap.CompressFormat.PNG.toString(), attachmentName);
        addResult.addDoneListener(() -> {
            try {
                Attachment attachment = addResult.get();
                if (attachment.getSize() > 0) {
                    final ListenableFuture<Void> tableResult = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature);
                    tableResult.addDoneListener(() -> applyEdit());
                }
            } catch (Exception ignored) {
                publishProgress();
            }
        });
    }


    private void applyEdit() {

        final ListenableFuture<List<FeatureEditResult>> updatedServerResult = mServiceFeatureTable.applyEditsAsync();
        updatedServerResult.addDoneListener(() -> {
            try {
                updatedServerResult.get();
                publishProgress(mSelectedArcGISFeature);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
                publishProgress();
            }

        });

    }

    private Object getIdFeatureTypes(List<FeatureType> featureTypes, String value) {
        Object code = null;
        for (FeatureType featureType : featureTypes) {
            if (featureType.getName().equals(value)) {
                code = featureType.getId();
                break;
            }
        }
        return code;
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

    @Override
    protected void onProgressUpdate(ArcGISFeature... values) {
        super.onProgressUpdate(values);
        if (values != null && values.length > 0)
            this.mDelegate.processFinish(values[0]);
        else this.mDelegate.processFinish(null);
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

    }

}

