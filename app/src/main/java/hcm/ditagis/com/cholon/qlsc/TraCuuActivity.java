package hcm.ditagis.com.cholon.qlsc;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.util.ArrayList;
import java.util.List;

import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewMoreInfoAdapter;
import hcm.ditagis.com.cholon.qlsc.async.NotifyDataSetChangeAsync;

public class TraCuuActivity extends AppCompatActivity {
    private ServiceFeatureTable mServiceFeatureTable;
    private List<String> mLstFeatureType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tra_cuu);

        mServiceFeatureTable = new ServiceFeatureTable(getResources().getString(R.string.service_feature_table));

        setContentView(R.layout.activity_tra_cuu);
        mLstFeatureType = new ArrayList<>();
        for (int i = 0; i < mServiceFeatureTable.getFeatureTypes().size(); i++) {
            mLstFeatureType.add(mServiceFeatureTable.getFeatureTypes().get(i).getName());
        }
        View layout = findViewById(R.id.layout_tracuu_include);
        ListView lstViewInfo = layout.findViewById(R.id.lstView_alertdialog_info);
        lstViewInfo.setOnItemClickListener((parent, view, position, id) -> edit(parent, position));
    }

    private void edit(final AdapterView<?> parent, int position) {
        if (parent.getItemAtPosition(position) instanceof FeatureViewMoreInfoAdapter.Item) {
            final FeatureViewMoreInfoAdapter.Item item = (FeatureViewMoreInfoAdapter.Item) parent.getItemAtPosition(position);
            if (item.isEdit()) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(this,
                        android.R.style.Theme_Material_Light_Dialog_Alert);
                builder.setTitle("Cập nhật thuộc tính");
                builder.setMessage(item.getAlias());
                builder.setCancelable(false).setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss());
                @SuppressLint("InflateParams") final android.widget.LinearLayout layout = (android.widget.LinearLayout) this.getLayoutInflater().
                        inflate(R.layout.layout_dialog_update_feature_listview, null);
                builder.setView(layout);
                final android.widget.FrameLayout layoutTextView = layout.findViewById(R.id.layout_edit_viewmoreinfo_TextView);
                final android.widget.TextView textView = layout.findViewById(R.id.txt_edit_viewmoreinfo);
                final Button button = layout.findViewById(R.id.btn_edit_viewmoreinfo);
                final android.widget.LinearLayout layoutEditText = layout.findViewById(R.id.layout_edit_viewmoreinfo_Editext);
                final EditText editText = layout.findViewById(R.id.etxt_edit_viewmoreinfo);
                final android.widget.LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
                final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);

                final com.esri.arcgisruntime.data.Domain domain = mServiceFeatureTable.getField(item.getFieldName()).getDomain();
                if (item.getFieldName().equals(mServiceFeatureTable.getTypeIdField())) {
                    layoutSpin.setVisibility(View.VISIBLE);
                    android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(layout.getContext(),
                            android.R.layout.simple_list_item_1, mLstFeatureType);
                    spin.setAdapter(adapter);
                    if (item.getValue() != null)
                        spin.setSelection(mLstFeatureType.indexOf(item.getValue()));
                } else if (domain != null) {
                    layoutSpin.setVisibility(View.VISIBLE);
                    List<com.esri.arcgisruntime.data.CodedValue> codedValues = ((com.esri.arcgisruntime.data.CodedValueDomain) domain).getCodedValues();
                    if (codedValues != null) {
                        List<String> codes = new ArrayList<>();
                        for (com.esri.arcgisruntime.data.CodedValue codedValue : codedValues)
                            codes.add(codedValue.getName());
                        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(layout.getContext(), android.R.layout.simple_list_item_1, codes);
                        spin.setAdapter(adapter);
                        if (item.getValue() != null)
                            spin.setSelection(codes.indexOf(item.getValue()));

                    }
                } else switch (item.getFieldType()) {
                    case DATE:
                        layoutTextView.setVisibility(View.VISIBLE);
                        textView.setText(item.getValue());
                        button.setOnClickListener(v -> {
                            final View dialogView = View.inflate(TraCuuActivity.this, R.layout.date_time_picker, null);
                            final AlertDialog alertDialog = new AlertDialog.Builder(TraCuuActivity.this).create();
                            dialogView.findViewById(R.id.date_time_set).setOnClickListener(view -> {
                                DatePicker datePicker = dialogView.findViewById(R.id.date_picker);

                                @SuppressLint("DefaultLocale") String s = String.format("%02d_%02d_%d",
                                        datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear());

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
                        editText.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                        editText.setText(item.getValue());


                        break;
                    case DOUBLE:
                        layoutEditText.setVisibility(View.VISIBLE);
                        editText.setInputType(android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                        editText.setText(item.getValue());
                        break;
                }
                builder.setPositiveButton("Cập nhật", (dialog, which) -> {
                    if (item.getFieldName().equals(mServiceFeatureTable.getTypeIdField()) || (domain != null)) {
                        item.setValue(spin.getSelectedItem().toString());
                    } else {
                        switch (item.getFieldType()) {
                            case DATE:
                                item.setValue(textView.getText().toString());
                                break;
                            case DOUBLE:
                                try {
                                    double x = Double.parseDouble(editText.getText().toString());
                                    item.setValue(editText.getText().toString());
                                } catch (Exception e) {
                                    android.widget.Toast.makeText(TraCuuActivity.this,
                                            "Số liệu nhập vào không đúng định dạng!!!", android.widget.Toast.LENGTH_LONG).show();
                                }
                                break;
                            case TEXT:
                                item.setValue(editText.getText().toString());
                                break;
                            case SHORT:
                                try {
                                    short x = Short.parseShort(editText.getText().toString());
                                    item.setValue(editText.getText().toString());
                                } catch (Exception e) {
                                    android.widget.Toast.makeText(TraCuuActivity.this,
                                            "Số liệu nhập vào không đúng định dạng!!!", android.widget.Toast.LENGTH_LONG).show();
                                }
                                break;
                        }
                    }


                    dialog.dismiss();
                    FeatureViewMoreInfoAdapter adapter = (FeatureViewMoreInfoAdapter) parent.getAdapter();
                    new NotifyDataSetChangeAsync(TraCuuActivity.this).execute(adapter);
                });

                AlertDialog dialog = builder.create();
                dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
                dialog.show();

            }
        }

    }

}