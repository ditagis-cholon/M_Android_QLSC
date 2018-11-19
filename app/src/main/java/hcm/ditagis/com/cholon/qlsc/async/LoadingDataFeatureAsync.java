package hcm.ditagis.com.cholon.qlsc.async;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Field;

import java.util.ArrayList;
import java.util.List;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;

public class LoadingDataFeatureAsync extends AsyncTask<Void, Void, Void> {
    private AsyncResponse mDelegate;
    private Activity mActivity;
    private Context mContext;
    private List<Field> mFields;
    private ArcGISFeature mArcGISFeature;
    private DApplication mApplication;

    public interface AsyncResponse {
        void processFinish(List<View> views);
    }

    public LoadingDataFeatureAsync(Activity activity, Context context, List<Field> fields, AsyncResponse delegate, ArcGISFeature... arcGISFeatures) {
        mActivity = activity;
        mApplication = (DApplication) activity.getApplication();
        mContext = context;
        mFields = fields;
        mDelegate = delegate;
        if (arcGISFeatures != null && arcGISFeatures.length > 0)
            mArcGISFeature = arcGISFeatures[0];
    }

    @Override
    protected Void doInBackground(Void... voids) {
        publishProgress();
        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        mDelegate.processFinish(loadDataToAdd());
    }

    private List<View> loadDataToAdd() {
        List<View> views = new ArrayList<>();
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        for (Field field : mFields) {
            String name = field.getName();
            if (name.equals(Constant.FieldSuCo.DIA_CHI)
                    || name.equals(Constant.FieldSuCo.NGUOI_PHAN_ANH)
                    || name.equals(Constant.FieldSuCo.SDT_PHAN_ANH)
                    || name.equals(Constant.FieldSuCo.THONG_TIN_PHAN_ANH)
                    || name.equals(Constant.FieldSuCo.GHI_CHU)
                    )
                views.add(getView(field));

        }
        return views;
    }

    private View getView(Field field) {
        LinearLayout layoutView = (LinearLayout) mActivity.getLayoutInflater().inflate(R.layout.item_add_feature, null);
        LinearLayout layoutEditNumber = layoutView.findViewById(R.id.llayout_add_feature_number);
        LinearLayout layoutEditNumberDecimal = layoutView.findViewById(R.id.llayout_add_feature_number_decimal);
        LinearLayout layoutEditSpinner = layoutView.findViewById(R.id.llayout_add_feature_spinner);
        LinearLayout layoutEditText = layoutView.findViewById(R.id.llayout_add_feature_edittext);


        Spinner spin = layoutEditSpinner.findViewById(R.id.spinner_add_spinner_value);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1, new ArrayList<>());
        spin.setAdapter(adapter);
        Object value = null;
        if (mArcGISFeature != null) {
            value = mArcGISFeature.getAttributes().get(field.getName());
        }
        if (field.getDomain() != null) {
            CodedValueDomain codedValueDomain = (CodedValueDomain) field.getDomain();
            List<String> values = new ArrayList<>();
            values.add(Constant.NULL);
            String selectedValue = null;
            for (CodedValue codedValue : codedValueDomain.getCodedValues()) {
                values.add(codedValue.getName());
                if (value != null && codedValue.getCode().equals(value))
                    selectedValue = codedValue.getName();
            }

            layoutEditNumberDecimal.setVisibility(View.GONE);
            layoutEditSpinner.setVisibility(View.VISIBLE);
            layoutEditText.setVisibility(View.GONE);
            layoutEditNumber.setVisibility(View.GONE);

            TextView textViewSpin = layoutEditSpinner.findViewById(R.id.txt_add_spiner_title);
            textViewSpin.setText(field.getAlias());
            adapter.addAll(values);
            adapter.notifyDataSetChanged();

            for (int i = 0; i < values.size(); i++) {
                if (selectedValue != null && values.get(i).equals(selectedValue)) {
                    spin.setSelection(i);
                    break;
                }
            }
        } else switch (field.getFieldType()) {
            case INTEGER:
            case SHORT:
                layoutEditNumberDecimal.setVisibility(View.GONE);
                layoutEditSpinner.setVisibility(View.GONE);
                layoutEditText.setVisibility(View.GONE);
                layoutEditNumber.setVisibility(View.VISIBLE);

                TextView textViewNumber = layoutEditNumber.findViewById(R.id.txt_add_edit_number_title);
                textViewNumber.setText(field.getAlias());

                if (value != null) {
                    EditText editTextNumber = layoutView.findViewById(R.id.etxt_add_edit_number_value);
                    try {
                        switch (field.getFieldType()) {
                            case INTEGER:
                                editTextNumber.setText(Integer.parseInt(value.toString()));
                                break;
                            case SHORT:
                                editTextNumber.setText(Short.parseShort(value.toString()));
                                break;
                        }
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Có lỗi khi load dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case DOUBLE:
            case FLOAT:
                layoutEditNumberDecimal.setVisibility(View.VISIBLE);
                layoutEditSpinner.setVisibility(View.GONE);
                layoutEditText.setVisibility(View.GONE);
                layoutEditNumber.setVisibility(View.GONE);

                TextView textViewNumberDecimal = layoutEditNumberDecimal.findViewById(R.id.txt_add_edit_number_decimal_title);
                textViewNumberDecimal.setText(field.getAlias());

                if (value != null) {
                    EditText editTextNumberDecimal = layoutView.findViewById(R.id.etxt_add_edit_number_decimal_value);
                    try {
                        switch (field.getFieldType()) {
                            case DOUBLE:
                                editTextNumberDecimal.setText(Double.parseDouble(value.toString()) + "");
                                break;
                            case FLOAT:
                                editTextNumberDecimal.setText(Float.parseFloat(value.toString()) + "");
                                break;
                        }
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Có lỗi khi load dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case TEXT:
                layoutEditNumberDecimal.setVisibility(View.GONE);
                layoutEditSpinner.setVisibility(View.GONE);
                layoutEditNumber.setVisibility(View.GONE);
                layoutEditText.setVisibility(View.VISIBLE);
                TextView textViewEditText = layoutEditText.findViewById(R.id.txt_add_edit_text_title);
                textViewEditText.setText(field.getAlias());
                if (field.getName().equals(Constant.FieldSuCo.DIA_CHI))
                    value = mApplication.getDiemSuCo().getVitri();
                if (value != null) {
                    EditText editText = layoutView.findViewById(R.id.edit_add_edittext_value);
                    try {
                        editText.setText(value.toString());
                    } catch (Exception e) {
                        Toast.makeText(mContext, "Có lỗi khi load dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                layoutEditNumberDecimal.setVisibility(View.GONE);
                layoutEditSpinner.setVisibility(View.GONE);
                layoutEditNumber.setVisibility(View.GONE);
                layoutEditText.setVisibility(View.GONE);
        }
        return layoutView;
    }


}