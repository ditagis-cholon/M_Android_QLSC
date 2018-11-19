package hcm.ditagis.com.cholon.qlsc;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Domain;
import com.esri.arcgisruntime.data.FeatureType;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewMoreInfoAdapter;
import hcm.ditagis.com.cholon.qlsc.async.EditAsync;
import hcm.ditagis.com.cholon.qlsc.async.NotifyDataSetChangeAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;
import hcm.ditagis.com.cholon.qlsc.utities.ImageFile;

public class UpdateActivity extends AppCompatActivity {
    private DApplication mApplication;
    private FeatureViewMoreInfoAdapter mFeatureViewMoreInfoAdapter;
    private ServiceFeatureTable mServiceFeatureTable;
    private List<String> lstFeatureType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        mApplication = (DApplication) getApplication();
        mServiceFeatureTable = mApplication.getFeatureLayerDTG().getServiceFeatureTable();
        lstFeatureType = new ArrayList<>();
        for (int i = 0; i < mApplication.getSelectedArcGISFeature().getFeatureTable().getFeatureTypes().size(); i++) {
            lstFeatureType.add(mApplication.getSelectedArcGISFeature().getFeatureTable().getFeatureTypes().get(i).getName());
        }
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowHomeEnabled(true);
        edit(mApplication.getSelectedArcGISFeature());
    }

    public void edit(ArcGISFeature feature) {
        mFeatureViewMoreInfoAdapter = new FeatureViewMoreInfoAdapter(UpdateActivity.this, new ArrayList<>());
        final ListView lstViewInfo = findViewById(R.id.lstView_alertdialog_info);
        Button mBtnLeft = findViewById(R.id.btn_updateinfo_left);
        Button btnRight = findViewById(R.id.btn_update_right);


        lstViewInfo.setAdapter(mFeatureViewMoreInfoAdapter);
        lstViewInfo.setOnItemClickListener((parent, view, position, id) -> listViewMoreInfoItemClick(parent, position, feature));
        loadDataViewMoreInfo(feature);

        mBtnLeft.setText(UpdateActivity.this.getResources().getString(R.string.btnLeftUpdateFeature));
        btnRight.setText(UpdateActivity.this.getResources().getString(R.string.btnRightUpdateFeature));
        mBtnLeft.setOnClickListener(view -> {
            if (feature != null) {

                EditAsync editAsync;
                editAsync = new EditAsync(UpdateActivity.this, mServiceFeatureTable,
                        feature, true, null,
                        arcGISFeature -> {
                            if (arcGISFeature != null) {
                                Toast.makeText(mBtnLeft.getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                                goHomeSuccess();
                            } else
                                Toast.makeText(mBtnLeft.getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        });
                editAsync.execute(mFeatureViewMoreInfoAdapter);
            }

        });

        btnRight.setOnClickListener(view -> {
            capture();
        });

    }


    private void loadDataViewMoreInfo(ArcGISFeature arcGISFeatureSuCoThongTin) {
        Map<String, Object> attr = arcGISFeatureSuCoThongTin.getAttributes();
        String[] updateFields =getResources().getStringArray(R.array.update_fields_arrays);
//        String[] pgnFields = UpdateActivity.this.getResources().getStringArray(R.array.pgn_fields_arrays);
        String[] pgnFields = new String[]{};
        String typeIdField = arcGISFeatureSuCoThongTin.getFeatureTable().getTypeIdField();
        boolean isFoundContinue = false;
        for (Field field : arcGISFeatureSuCoThongTin.getFeatureTable().getFields()) {
            Object value = attr.get(field.getName());

            FeatureViewMoreInfoAdapter.Item item = new FeatureViewMoreInfoAdapter.Item();
            item.setAlias(field.getAlias());
            item.setFieldName(field.getName());
            item.setEdit(false);

            if (value != null) {
                if (item.getFieldName().equals(typeIdField)) {
                    List<FeatureType> featureTypes = arcGISFeatureSuCoThongTin.getFeatureTable().getFeatureTypes();
                    Object valueFeatureType = getValueFeatureType(featureTypes, value.toString());
                    if (valueFeatureType != null) {
                        item.setValue(valueFeatureType.toString());
                    }

                } else if (field.getDomain() != null) {
                    List<CodedValue> codedValues = ((CodedValueDomain) arcGISFeatureSuCoThongTin.getFeatureTable().getField(item.getFieldName()).getDomain()).getCodedValues();
                    Object valueDomain = getValueDomain(codedValues, value.toString());
                    if (valueDomain != null) item.setValue(valueDomain.toString());
                } else switch (field.getFieldType()) {
                    case DATE:
                        item.setValue(Constant.DATE_FORMAT_VIEW.format(((Calendar) value).getTime()));
                        break;
                    case OID:
                    case TEXT:
                        item.setValue(value.toString());
                        break;
                    case DOUBLE:
                    case SHORT:
                        item.setValue(value.toString());
                        break;
                }
            }

            for (String updateField : updateFields) {
                if (item.getFieldName().equals(updateField)) {
                    item.setEdit(true);
                    break;
                }
            }
            item.setFieldType(field.getFieldType());
            mFeatureViewMoreInfoAdapter.add(item);
            mFeatureViewMoreInfoAdapter.notifyDataSetChanged();
        }
    }

    private Object getValueDomain(List<CodedValue> codedValues, String code) {
        Object value = null;
        for (CodedValue codedValue : codedValues) {
            if (codedValue.getCode().toString().equals(code)) {
                value = codedValue.getName();
                break;
            }
        }
        return value;
    }

    private Object getValueFeatureType(List<FeatureType> featureTypes, String code) {
        Object value = null;
        for (FeatureType featureType : featureTypes) {
            if (featureType.getId().toString().equals(code)) {
                value = featureType.getName();
                break;
            }
        }
        return value;
    }

    private void listViewMoreInfoItemClick(final AdapterView<?> parent, int position, ArcGISFeature arcGISFeature) {
        if (parent.getItemAtPosition(position) instanceof FeatureViewMoreInfoAdapter.Item) {
            final FeatureViewMoreInfoAdapter.Item item = (FeatureViewMoreInfoAdapter.Item) parent.getItemAtPosition(position);
            if (item.isEdit()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(UpdateActivity.this, android.R.style.Theme_Material_Light_Dialog_Alert);
                builder.setTitle("Cập nhật thuộc tính");
                builder.setMessage(item.getAlias());

                @SuppressLint("InflateParams") final LinearLayout layout = (LinearLayout) UpdateActivity.this.getLayoutInflater().
                        inflate(R.layout.layout_dialog_update_feature_listview, null);
                Button btnLeft = layout.findViewById(R.id.btn_updateinfo_left);
                Button btnRight = layout.findViewById(R.id.btn_update_right);

                btnLeft.setText(UpdateActivity.this.getResources().getString(R.string.btnLeft_editItemViewMoreInfo));
                btnRight.setText(UpdateActivity.this.getResources().getString(R.string.btnRight_editItemViewMoreInfo));


                builder.setView(layout);

                loadDataEdit(item, layout, arcGISFeature);

                final AlertDialog dialog = builder.create();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                btnLeft.setOnClickListener(view -> dialog.dismiss());
                btnRight.setOnClickListener(view -> updateEdit(item, layout, parent, dialog, arcGISFeature));
                dialog.show();
            }
        }
    }

    private void loadDataEdit(FeatureViewMoreInfoAdapter.Item item, LinearLayout layout, ArcGISFeature arcGISFeature) {
        loadDataEdit_Another(item, layout, arcGISFeature);
    }


    private void loadDataEdit_Another(FeatureViewMoreInfoAdapter.Item item, LinearLayout layout, ArcGISFeature arcGISFeature) {
        final FrameLayout layoutTextView = layout.findViewById(R.id.layout_edit_viewmoreinfo_TextView);
        final TextView textView = layout.findViewById(R.id.txt_edit_viewmoreinfo);
        final Button button = layout.findViewById(R.id.btn_edit_viewmoreinfo);
        final LinearLayout layoutEditText = layout.findViewById(R.id.layout_edit_viewmoreinfo_Editext);
        final EditText editText = layout.findViewById(R.id.etxt_edit_viewmoreinfo);
        final LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
        final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);

        final Domain domain = arcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain();
        if (item.getFieldName().equals(arcGISFeature.getFeatureTable().getTypeIdField())) {
            layoutSpin.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(layout.getContext(), android.R.layout.simple_list_item_1, lstFeatureType);
            spin.setAdapter(adapter);
            if (item.getValue() != null) {
                spin.setSelection(lstFeatureType.indexOf(item.getValue()));
            }
        } else if (domain != null) {
            layoutSpin.setVisibility(View.VISIBLE);
            List<CodedValue> codedValues = ((CodedValueDomain) domain).getCodedValues();

            if (codedValues != null) {
                List<String> codes = new ArrayList<>();
                for (CodedValue codedValue : codedValues)
                    codes.add(codedValue.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(layout.getContext(), android.R.layout.simple_list_item_1, codes);
                spin.setAdapter(adapter);
                if (item.getValue() != null)
                    spin.setSelection(codes.indexOf(item.getValue()));
            }
        } else switch (item.getFieldType()) {
            case DATE:
                layoutTextView.setVisibility(View.VISIBLE);
                textView.setText(item.getValue());
                button.setOnClickListener(v -> {
                    final View dialogView = View.inflate(UpdateActivity.this, R.layout.date_time_picker, null);
                    final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(UpdateActivity.this).create();
                    dialogView.findViewById(R.id.date_time_set).setOnClickListener(view -> {
                        DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
                        String s = String.format(UpdateActivity.this.getResources().getString(R.string.format_date_month_year), datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear());

                        textView.setText(s);
                        alertDialog.dismiss();
                    });
                    alertDialog.setView(dialogView);
                    alertDialog.show();
                });
                break;
            case TEXT:
                layoutEditText.setVisibility(View.VISIBLE);
                editText.setText(item.getValue());
                break;
            case SHORT:
                layoutEditText.setVisibility(View.VISIBLE);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setText(item.getValue());
                break;
            case DOUBLE:
                layoutEditText.setVisibility(View.VISIBLE);
                editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                editText.setText(item.getValue());
                break;
        }
    }

    private void updateEdit(FeatureViewMoreInfoAdapter.Item item, LinearLayout
            layout, AdapterView<?> parent, DialogInterface dialog, ArcGISFeature arcGISFeatureSuCoThongTin) {
        boolean isCanUpdate = true;
        final TextView textView = layout.findViewById(R.id.txt_edit_viewmoreinfo);
        final EditText editText = layout.findViewById(R.id.etxt_edit_viewmoreinfo);
        final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);

        final Domain domain = arcGISFeatureSuCoThongTin.getFeatureTable().getField(item.getFieldName()).getDomain();
        if (item.getFieldName().equals(arcGISFeatureSuCoThongTin.getFeatureTable().getTypeIdField()) || (domain != null)) {
            if ((item.getValue() == null || !item.getValue().equals(spin.getSelectedItem().toString())) && item.getFieldName().equals(arcGISFeatureSuCoThongTin.getFeatureTable().getTypeIdField())) {
                String[] field_subtypeArr = new String[]{};
                for (int i = 0; i < parent.getCount(); i++) {
                    FeatureViewMoreInfoAdapter.Item item1 = (FeatureViewMoreInfoAdapter.Item) parent.getAdapter().getItem(i);
                    for (String field_subtype : field_subtypeArr) {
                        if (item1.getFieldName().equals(field_subtype)) {
                            item1.setValue("");
                            item1.setEdited(true);
                            ((FeatureViewMoreInfoAdapter) parent.getAdapter()).notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }
            item.setValue(spin.getSelectedItem().toString());
        } else if (domain == null) {
            switch (item.getFieldType()) {
                case DATE:
                    item.setValue(textView.getText().toString());
                    break;
                case DOUBLE:
                    try {
                        double x = Double.parseDouble(editText.getText().toString());
                        item.setValue(String.format("%s", x));
                    } catch (Exception e) {
                        Toast.makeText(UpdateActivity.this, "Số liệu nhập vào không đúng định dạng!!!", Toast.LENGTH_LONG).show();
                    }
                    break;
                case TEXT:
                    item.setValue(editText.getText().toString());
                    break;
                case SHORT:
                    try {
                        short x = Short.parseShort(editText.getText().toString());
                        item.setValue(String.format("%s", x));
                    } catch (Exception e) {
                        Toast.makeText(UpdateActivity.this, "Số liệu nhập vào không đúng định dạng!!!", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
        if (isCanUpdate) {
            dialog.dismiss();
            item.setEdited(true);
            FeatureViewMoreInfoAdapter adapter = (FeatureViewMoreInfoAdapter) parent.getAdapter();
            new NotifyDataSetChangeAsync(UpdateActivity.this).execute(adapter);
        }
    }

    public void capture() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());

        File photo = ImageFile.getFile(UpdateActivity.this);
        Uri uri = Uri.fromFile(photo);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(cameraIntent, UpdateActivity.this.getResources().getInteger(R.integer.REQUEST_ID_IMAGE_CAPTURE_POPUP));
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {

            switch (requestCode) {
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void onBackPressed() {
        goHomeCancel();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void goHomeCancel() {

        Intent intent = new Intent();
        setResult(RESULT_CANCELED, intent);
        finish();
    }

    private void goHomeSuccess() {

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
