package hcm.ditagis.com.cholon.qlsc.utities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Geocoder;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Attachment;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Domain;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureType;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.geometry.Envelope;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.SpatialReferences;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.view.Callout;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.MainActivity;
import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewInfoAdapter;
import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewMoreInfoAdapter;
import hcm.ditagis.com.cholon.qlsc.adapter.VatTuAdapter;
import hcm.ditagis.com.cholon.qlsc.async.EditAsync;
import hcm.ditagis.com.cholon.qlsc.async.FindLocationAsycn;
import hcm.ditagis.com.cholon.qlsc.async.NotifyDataSetChangeAsync;
import hcm.ditagis.com.cholon.qlsc.async.ViewAttachmentAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.entities.HoSoVatTuSuCo;
import hcm.ditagis.com.cholon.qlsc.entities.MyAddress;
import hcm.ditagis.com.cholon.qlsc.entities.VatTu;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.ListObjectDB;

@SuppressLint("Registered")
public class Popup extends AppCompatActivity implements View.OnClickListener {
    private List<String> mListTenVatTu;
    private MainActivity mMainActivity;
    private ArcGISFeature mSelectedArcGISFeature = null;
    private ServiceFeatureTable mServiceFeatureTable;
    private Callout mCallout;
    private List<String> lstFeatureType;
    private FeatureViewMoreInfoAdapter mFeatureViewMoreInfoAdapter;
    private LinearLayout linearLayout;
    private MapView mMapView;
    private String mLoaiSuCo;
    private short mLoaiSuCoShort;
    private Geocoder mGeocoder;
    private List<HoSoVatTuSuCo> mListHoSoVatTuSuCo;
    private String mIDSuCo;
    private Button mBtnLeft;
    private DApplication mApplication;

    public Popup(MainActivity mainActivity, MapView mapView, ServiceFeatureTable serviceFeatureTable,
                 Callout callout, Geocoder geocoder) {
        this.mMainActivity = mainActivity;
        this.mApplication = (DApplication) mainActivity.getApplication();
        this.mMapView = mapView;
        this.mServiceFeatureTable = serviceFeatureTable;
        this.mCallout = callout;
        this.mGeocoder = geocoder;


    }

    private void initializeVatTu() {
        if (mListTenVatTu == null) {
            mListTenVatTu = new ArrayList<>();
            for (VatTu vatTu : ListObjectDB.getInstance().getVatTus())
                mListTenVatTu.add(vatTu.getTenVatTu());
        }

    }


    public Button getmBtnLeft() {
        return mBtnLeft;
    }

    public Callout getCallout() {
        return mCallout;
    }

    public List<HoSoVatTuSuCo> getListHoSoVatTuSuCo() {
        return mListHoSoVatTuSuCo;
    }


    public void refreshPopup(ArcGISFeature arcGISFeature) {
        mSelectedArcGISFeature = arcGISFeature;
        Map<String, Object> attributes = mSelectedArcGISFeature.getAttributes();
        ListView listView = linearLayout.findViewById(R.id.lstview_thongtinsuco);
        FeatureViewInfoAdapter featureViewInfoAdapter = new FeatureViewInfoAdapter(mMainActivity, new ArrayList<>());
        listView.setAdapter(featureViewInfoAdapter);
        String typeIdField = mSelectedArcGISFeature.getFeatureTable().getTypeIdField();
        String[] noDisplayFields = mMainActivity.getResources().getStringArray(R.array.no_display_fields_arrays);
        boolean isFoundField = false;
        mIDSuCo = attributes.get(mMainActivity.getString(R.string.Field_SuCo_IDSuCo)).toString();


        for (Field field : this.mSelectedArcGISFeature.getFeatureTable().getFields()) {
            for (String noDisplayField : noDisplayFields)
                if (noDisplayField.equals(field.getName())) {
                    isFoundField = true;
                    break;
                }
            if (isFoundField) {
                isFoundField = false;
                continue;
            }
            Object value = attributes.get(field.getName());
            if (value != null) {
                FeatureViewInfoAdapter.Item item = new FeatureViewInfoAdapter.Item();

                item.setAlias(field.getAlias());
                item.setFieldName(field.getName());
                if (item.getFieldName().equals(typeIdField)) {
                    List<FeatureType> featureTypes = mSelectedArcGISFeature.getFeatureTable().getFeatureTypes();
                    Object valueFeatureType = getValueFeatureType(featureTypes, value.toString());

                    if (valueFeatureType != null) {
                        mLoaiSuCo = valueFeatureType.toString();
                        mLoaiSuCoShort = (Short.parseShort(attributes.get(mMainActivity.getString(R.string.Field_SuCo_LoaiSuCo)).toString()));
                        item.setValue(mLoaiSuCo);
                    } else continue;
                } else if (field.getDomain() != null) {
                    List<CodedValue> codedValues = new ArrayList<>();
                    if (field.getName().equals(mMainActivity.getString(R.string.Field_SuCo_NguyenNhan))) {
                        if (mLoaiSuCo != null && (mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngNganh))
                                || mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngChinh)))) {
                            codedValues = ((CodedValueDomain) mSelectedArcGISFeature.getFeatureTable().getFeatureTypes()
                                    .get(mLoaiSuCoShort - 1).getDomains().get(mMainActivity.getString(R.string.Field_SuCo_NguyenNhan))).getCodedValues();

                        }
                    } else if (field.getName().equals(mMainActivity.getString(R.string.Field_SuCo_VatLieu))) {
                        if (mLoaiSuCo != null && (mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngNganh))
                                || mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngChinh)))) {
                            codedValues = ((CodedValueDomain) mSelectedArcGISFeature.getFeatureTable().getFeatureTypes()
                                    .get(mLoaiSuCoShort - 1).getDomains().get(mMainActivity.getString(R.string.Field_SuCo_VatLieu))).getCodedValues();

                        }
                    } else if (field.getName().equals(mMainActivity.getString(R.string.Field_SuCo_DuongKinhOng))) {
                        if (mLoaiSuCo != null && (mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngNganh))
                                || mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngChinh)))) {
                            codedValues = ((CodedValueDomain) mSelectedArcGISFeature.getFeatureTable().getFeatureTypes()
                                    .get(mLoaiSuCoShort - 1).getDomains().get(mMainActivity.getString(R.string.Field_SuCo_DuongKinhOng))).getCodedValues();

                        }
                    } else {
                        codedValues = ((CodedValueDomain) this.mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain()).getCodedValues();

                    }
                    Object valueDomain = getValueDomain(codedValues, value.toString());
                    if (valueDomain != null) item.setValue(valueDomain.toString());
                } else if (item.getFieldName().equals(mMainActivity.getString(R.string.Field_SuCo_VatTu))) {
                    StringBuilder builder = new StringBuilder();
                    this.mListHoSoVatTuSuCo = ListObjectDB.getInstance().getHoSoVatTuSuCos();
                    for (HoSoVatTuSuCo hoSoVatTuSuCo : mListHoSoVatTuSuCo) {
                        builder.append(hoSoVatTuSuCo.getTenVatTu()).append(" ").append(hoSoVatTuSuCo.getSoLuong()).append(" ").append(hoSoVatTuSuCo.getDonViTinh()).append("\n");
                    }
                    if (builder.length() > 0)
                        builder.replace(builder.length() - 2, builder.length(), "");
                    item.setValue(builder.toString());
                } else switch (field.getFieldType()) {
                    case DATE:
                        item.setValue(Constant.DATE_FORMAT_VIEW.format(((Calendar) value).getTime()));
                        break;
                    case OID:
                    case TEXT:
                    case SHORT:
                    case DOUBLE:
                    case INTEGER:
                    case FLOAT:
                        item.setValue(value.toString());
                        break;
                }
                featureViewInfoAdapter.add(item);
                featureViewInfoAdapter.notifyDataSetChanged();
            }
        }
    }

    private void viewMoreInfo(final boolean isAddFeature) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        @SuppressLint("InflateParams") final View layout = mMainActivity.getLayoutInflater().inflate(R.layout.layout_viewmoreinfo_feature, null);
        mFeatureViewMoreInfoAdapter = new FeatureViewMoreInfoAdapter(mMainActivity, new ArrayList<>());
        final ListView lstViewInfo = layout.findViewById(R.id.lstView_alertdialog_info);
        mBtnLeft = layout.findViewById(R.id.btn_updateinfo_left);
        Button btnRight = layout.findViewById(R.id.btn_updateinfo_right);
        layout.findViewById(R.id.layout_viewmoreinfo_id_su_co).setVisibility(View.VISIBLE);

        layout.findViewById(R.id.framelayout_viewmoreinfo_attachment).setOnClickListener(v -> viewAttachment());

        lstViewInfo.setAdapter(mFeatureViewMoreInfoAdapter);
        lstViewInfo.setOnItemClickListener((parent, view, position, id) -> edit(parent, position));
        loadDataViewMoreInfo(isAddFeature, layout);
        builder.setView(layout);
        builder.setCancelable(true);
        final AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (isAddFeature) {
            mBtnLeft.setText(mMainActivity.getString(R.string.btnLeftAddFeature));
            btnRight.setText(mMainActivity.getString(R.string.btnRightAddFeature));
            mBtnLeft.setOnClickListener(view -> {
                EditAsync editAsync = new EditAsync(getListHoSoVatTuSuCo(), mMainActivity,
                        mApplication.getServiceFeatureTable(),
                        mSelectedArcGISFeature, true, null, arcGISFeature -> {
                    mCallout.dismiss();
                    dialog.dismiss();
                });
                editAsync.execute(mFeatureViewMoreInfoAdapter);

            });
            btnRight.setOnClickListener((View view) -> capture(true));
        } else {
            layout.findViewById(R.id.framelayout_viewmoreinfo_attachment).setVisibility(View.VISIBLE);
            mBtnLeft.setText(mMainActivity.getString(R.string.btnLeftUpdateFeature));
            btnRight.setText(mMainActivity.getString(R.string.btnRightUpdateFeature));
            mBtnLeft.setOnClickListener((View view) -> {
                boolean isComplete = false;
                for (FeatureViewMoreInfoAdapter.Item item : mFeatureViewMoreInfoAdapter.getItems())
                    if (item.getFieldName().equals(mMainActivity.getString(R.string.Field_SuCo_TrangThai))
                            && item.getValue().equals(mMainActivity.getResources().getString(R.string.SuCo_TrangThai_HoanThanh))) {
                        isComplete = true;
                    }
                if (isComplete) {
                    final ListenableFuture<List<Attachment>> attachmentResults = mSelectedArcGISFeature.fetchAttachmentsAsync();
                    attachmentResults.addDoneListener(() -> {
                        try {

                            final List<Attachment> attachments = attachmentResults.get();
                            int size = attachments.size();
                            if (size == 0) {
                                MySnackBar.make(mBtnLeft, R.string.message_ChupAnh_HoanThanh, true);
                            } else {
                                EditAsync editAsync = new EditAsync(getListHoSoVatTuSuCo(), mMainActivity,
                                        mApplication.getServiceFeatureTable(),
                                        mSelectedArcGISFeature, true, null, arcGISFeature -> {
                                    mCallout.dismiss();
                                    dialog.dismiss();
                                });
                                editAsync.execute(mFeatureViewMoreInfoAdapter);
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    });

                } else {
                    EditAsync editAsync = new EditAsync(getListHoSoVatTuSuCo(), mMainActivity,
                            mApplication.getServiceFeatureTable(),
                            mSelectedArcGISFeature, true, null, (ArcGISFeature arcGISFeature) -> {
                        mCallout.dismiss();
                        dialog.dismiss();
                    });
                    editAsync.execute(mFeatureViewMoreInfoAdapter);
                }

            });
            btnRight.setOnClickListener(view -> capture(false));
        }
        dialog.show();
    }

    private void loadDataViewMoreInfo(boolean isAddFeature, View layout) {
        Map<String, Object> attr = mSelectedArcGISFeature.getAttributes();

        String[] updateFields = mMainActivity.getResources().getStringArray(R.array.update_fields_arrays);
        String[] addFields = mMainActivity.getResources().getStringArray(R.array.add_fields_arrays);
        String[] no_displayFields = mMainActivity.getResources().getStringArray(R.array.no_display_fields_arrays);
        String typeIdField = mSelectedArcGISFeature.getFeatureTable().getTypeIdField();
        boolean isFoundContinue = false;
        for (Field field : this.mSelectedArcGISFeature.getFeatureTable().getFields()) {
            Object value = attr.get(field.getName());

            //nếu là nodisplay field thì bỏ qua
            for (String noDisplayField : no_displayFields)
                if (field.getName().equals(noDisplayField)) {
                    isFoundContinue = true;
                    break;
                }
            if (isFoundContinue) {
                isFoundContinue = false;
                continue;
            }

            //Nếu đang trong chức năng thêm sự cố
            if (isAddFeature) {
                isFoundContinue = true;
                //Kiểm tra nếu không phải là addField thì bỏ qua
                for (String addField : addFields)
                    if (field.getName().equals(addField)) {
                        isFoundContinue = false;
                        break;
                    }
            }
            if (isFoundContinue) {
                isFoundContinue = false;
                continue;
            }
            if (field.getName().equals(mMainActivity.getString(R.string.Field_SuCo_IDSuCo))) {
                if (value != null) {
                    mIDSuCo = value.toString();
                    ((TextView) layout.findViewById(R.id.txt_alertdialog_id_su_co)).setText(mIDSuCo);
//                    QueryHoSoVatTuSuCoAsync queryHoSoVatTuSuCoAsync = new QueryHoSoVatTuSuCoAsync(mMainActivity);
//                    queryHoSoVatTuSuCoAsync.doInBackground();
                    this.mListHoSoVatTuSuCo = ListObjectDB.getInstance().getHoSoVatTuSuCos();
                }
            } else {
                FeatureViewMoreInfoAdapter.Item item = new FeatureViewMoreInfoAdapter.Item();
                item.setAlias(field.getAlias());
                item.setFieldName(field.getName());
                if (value != null) {
                    if (item.getFieldName().equals(typeIdField)) {
                        List<FeatureType> featureTypes = mSelectedArcGISFeature.getFeatureTable().getFeatureTypes();
                        Object valueFeatureType = getValueFeatureType(featureTypes, value.toString());
                        mLoaiSuCo = "";
                        if (valueFeatureType != null) {
                            item.setValue(valueFeatureType.toString());
                            mLoaiSuCo = valueFeatureType.toString();
                        }

                    } else if (field.getDomain() != null) {
                        List<CodedValue> codedValues = new ArrayList<>();
                        if (field.getName().equals(mMainActivity.getString(R.string.Field_SuCo_NguyenNhan))) {
                            if (mLoaiSuCo != null && (mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngNganh))
                                    || mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngChinh)))) {
                                codedValues = ((CodedValueDomain) mSelectedArcGISFeature.getFeatureTable().getFeatureTypes()
                                        .get(mLoaiSuCoShort - 1).getDomains().get(mMainActivity.getString(R.string.Field_SuCo_NguyenNhan))).getCodedValues();

                            }
                        } else if (field.getName().equals(mMainActivity.getString(R.string.Field_SuCo_VatLieu))) {
                            if (mLoaiSuCo != null && (mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngNganh))
                                    || mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngChinh)))) {
                                codedValues = ((CodedValueDomain) mSelectedArcGISFeature.getFeatureTable().getFeatureTypes()
                                        .get(mLoaiSuCoShort - 1).getDomains().get(mMainActivity.getString(R.string.Field_SuCo_VatLieu))).getCodedValues();

                            }
                        } else if (field.getName().equals(mMainActivity.getString(R.string.Field_SuCo_DuongKinhOng))) {
                            if (mLoaiSuCo != null && (mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngNganh))
                                    || mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngChinh)))) {
                                codedValues = ((CodedValueDomain) mSelectedArcGISFeature.getFeatureTable().getFeatureTypes()
                                        .get(mLoaiSuCoShort - 1).getDomains().get(mMainActivity.getString(R.string.Field_SuCo_DuongKinhOng))).getCodedValues();

                            }
                        } else {
                            codedValues = ((CodedValueDomain) this.mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain()).getCodedValues();

                        }
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
                item.setEdit(false);
                boolean isAddField = false;
                for (String addField : addFields) {
                    if (addField.equals(item.getFieldName())) {
                        isAddField = true;
                        break;
                    }
                }
                for (String updateField : updateFields) {
                    //Nếu là update field
                    if (item.getFieldName().equals(updateField)) {
                        //Nếu đang trong chức năng thêm sự cố thì edit = true
                        if (isAddFeature)
                            item.setEdit(true);
                            //Ngược lại, nếu không phải là addField thì edit = true
                        else if (!isAddField)
                            item.setEdit(true);
                        break;
                    }
                }
                item.setFieldType(field.getFieldType());
                mFeatureViewMoreInfoAdapter.add(item);
                mFeatureViewMoreInfoAdapter.notifyDataSetChanged();
            }
        }
    }

    private void viewAttachment() {
        ViewAttachmentAsync viewAttachmentAsync = new ViewAttachmentAsync(mMainActivity, mSelectedArcGISFeature);
        viewAttachmentAsync.execute();
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

    private void edit(final AdapterView<?> parent, int position) {
        if (parent.getItemAtPosition(position) instanceof FeatureViewMoreInfoAdapter.Item) {
            final FeatureViewMoreInfoAdapter.Item item = (FeatureViewMoreInfoAdapter.Item) parent.getItemAtPosition(position);
            if (item.isEdit()) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_Dialog_Alert);
                builder.setTitle("Cập nhật thuộc tính");
                builder.setMessage(item.getAlias());

                @SuppressLint("InflateParams") final LinearLayout layout = (LinearLayout) mMainActivity.getLayoutInflater().
                        inflate(R.layout.layout_dialog_update_feature_listview, null);
                Button btnLeft = layout.findViewById(R.id.btn_updateinfo_left);
                Button btnRight = layout.findViewById(R.id.btn_updateinfo_right);

                btnLeft.setText(mMainActivity.getString(R.string.btnLeft_editItemViewMoreInfo));
                btnRight.setText(mMainActivity.getString(R.string.btnRight_editItemViewMoreInfo));


                builder.setView(layout);

                loadDataEdit(item, layout);

                final AlertDialog dialog = builder.create();
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                btnLeft.setOnClickListener(view -> {
                    ListObjectDB.getInstance().clearListHoSoVatTuSuCoChange();
                    dialog.dismiss();
                });
                btnRight.setOnClickListener(view -> updateEdit(item, layout, parent, dialog));
                dialog.show();

            }
        }

    }

    private void loadDataEdit(FeatureViewMoreInfoAdapter.Item item, LinearLayout layout) {
        //Load danh sách madma từ csdl
        if (item.getFieldName().equals(mMainActivity.getString(R.string.Field_MADMA))) {
            loadDataEdit_DMA(item, layout);
        }
        //Trường hợp vị trí thì không dùng domain, vì còn có nhập khoảng cách
        else if (item.getFieldName().equals(mMainActivity.getString(R.string.Field_SuCo_ViTri))) {
            loadDataEdit_ViTri(layout);
        }
        //Trường hợp nguyên nhân, không tự động lấy được domain
        else if (item.getFieldName().equals(mMainActivity.getString(R.string.Field_SuCo_NguyenNhan))) {
            loadDataEdit_NguyenNhan(item, layout);
        }
        //Trường hợp vật liệu, không tự động lấy được domain
        else if (item.getFieldName().equals(mMainActivity.getString(R.string.Field_SuCo_VatLieu))) {
            loadDataEdit_VatLieu(item, layout);

        }
        //Trường hợp vật tư, không tự động lấy được domain
        else if (item.getFieldName().equals(mMainActivity.getString(R.string.Field_SuCo_VatTu))) {
            loadDataEdit_VatTu(layout);
        } else if (item.getFieldName().equals(mMainActivity.getString(R.string.Field_SuCo_DuongKinhOng))) {
            loadDataEdit_DuongKinhOng(item, layout);
        } else {
            loadDataEdit_Another(item, layout);
        }
    }

    private void loadDataEdit_DMA(FeatureViewMoreInfoAdapter.Item item, LinearLayout layout) {
        final LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
        final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);
        layoutSpin.setVisibility(View.VISIBLE);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(layout.getContext(), android.R.layout.simple_list_item_1,
                new ArrayList<>());
        if (ListObjectDB.getInstance().getDmas() != null)
            adapter.addAll(ListObjectDB.getInstance().getDmas());
        adapter.notifyDataSetChanged();
        spin.setAdapter(adapter);
        if (item.getValue() != null)
            spin.setSelection(ListObjectDB.getInstance().getDmas().indexOf(item.getValue()));
    }

    private void loadDataEdit_ViTri(LinearLayout layout) {
        final LinearLayout layoutEditText = layout.findViewById(R.id.layout_edit_viewmoreinfo_Editext);
        final LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
        final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);
        layoutSpin.setVisibility(View.VISIBLE);
        if (mLoaiSuCo != null && mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngNganh))) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(layout.getContext(), android.R.layout.simple_list_item_1, mMainActivity.getResources().getStringArray(R.array.vitri_ongnganh_arrays));
            spin.setAdapter(adapter);
        } else if (mLoaiSuCo != null && mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngChinh))) {
            layoutEditText.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(layout.getContext(), android.R.layout.simple_list_item_1, mMainActivity.getResources().getStringArray(R.array.vitri_ongchinh1_arrays));
            spin.setAdapter(adapter);
        }
//        if (item.getValue() != null)
//            spin.setSelection(mListObjectDB.indexOf(item.getValue()));
    }

    private void loadDataEdit_NguyenNhan(FeatureViewMoreInfoAdapter.Item item, LinearLayout layout) {
        final LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
        final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);

        layoutSpin.setVisibility(View.VISIBLE);
        List<String> codes = new ArrayList<>();
        if (mLoaiSuCo != null && (mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngNganh))
                || mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngChinh)))) {
            List<CodedValue> codedValues = ((CodedValueDomain) this.mSelectedArcGISFeature.getFeatureTable().getFeatureTypes()
                    .get(mLoaiSuCoShort - 1).getDomains().get(mMainActivity.getString(R.string.Field_SuCo_NguyenNhan))).getCodedValues();
            if (codedValues != null) {
                for (CodedValue codedValue : codedValues)
                    codes.add(codedValue.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(layout.getContext(), android.R.layout.simple_list_item_1, codes);
                spin.setAdapter(adapter);
            }
        }
        if (item.getValue() != null)
            spin.setSelection(codes.indexOf(item.getValue()));
    }

    private void loadDataEdit_VatLieu(FeatureViewMoreInfoAdapter.Item item, LinearLayout layout) {
        final LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
        final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);
        final AutoCompleteTextView autoCompleteTextView = layout.findViewById(R.id.autoCompleteTV_edit_viewmoreinfo);
        autoCompleteTextView.setBackgroundResource(R.drawable.layout_border);

        layoutSpin.setVisibility(View.VISIBLE);
        List<String> codes = new ArrayList<>();
        if (mLoaiSuCo != null && (mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngNganh))
                || mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngChinh)))) {
            List<CodedValue> codedValues = ((CodedValueDomain) this.mSelectedArcGISFeature.getFeatureTable().getFeatureTypes()
                    .get(mLoaiSuCoShort - 1).getDomains().get(mMainActivity.getString(R.string.Field_SuCo_VatLieu))).getCodedValues();
            if (codedValues != null) {
                for (CodedValue codedValue : codedValues)
                    codes.add(codedValue.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(layout.getContext(), android.R.layout.simple_list_item_1, codes);
                spin.setAdapter(adapter);
            }
        }
        if (item.getValue() != null)
            spin.setSelection(codes.indexOf(item.getValue()));
    }

    private void loadDataEdit_DuongKinhOng(FeatureViewMoreInfoAdapter.Item item, LinearLayout layout) {
        final LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
        final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);

        layoutSpin.setVisibility(View.VISIBLE);
        List<String> codes = new ArrayList<>();
        if (mLoaiSuCo != null && (mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngNganh))
                || mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngChinh)))) {
            List<CodedValue> codedValues = ((CodedValueDomain) this.mSelectedArcGISFeature.getFeatureTable().getFeatureTypes()
                    .get(mLoaiSuCoShort - 1).getDomains().get(mMainActivity.getString(R.string.Field_SuCo_DuongKinhOng))).getCodedValues();
            if (codedValues != null) {
                for (CodedValue codedValue : codedValues)
                    codes.add(codedValue.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(layout.getContext(), android.R.layout.simple_list_item_1, codes);
                spin.setAdapter(adapter);
            }
        }
        if (item.getValue() != null)
            spin.setSelection(codes.indexOf(item.getValue()));
    }


    private void loadDataEdit_VatTu(LinearLayout layout) {
        final LinearLayout layoutAutoCompleteTV = layout.findViewById(R.id.layout_edit_viewmoreinfo_AutoCompleteTV);
        final AutoCompleteTextView autoCompleteTextView = layout.findViewById(R.id.autoCompleteTV_edit_viewmoreinfo);
        autoCompleteTextView.setBackgroundResource(R.drawable.layout_border);
        final ListView listViewVatTu = layout.findViewById(R.id.lstview_viewmoreinfo_autoCompleteTV);
        final EditText etxtSoLuong = layout.findViewById(R.id.etxt_soLuong);
        final TextView txtDonViTinh = layout.findViewById(R.id.txt_donvitinh);
        final TextView txtThemVatTu = layout.findViewById(R.id.txt_them_vattu);
        ListObjectDB.getInstance().clearListHoSoVatTuSuCoChange();
        if (mLoaiSuCo != null) {
            layoutAutoCompleteTV.setVisibility(View.VISIBLE);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(layout.getContext(), android.R.layout.simple_list_item_1, mListTenVatTu);
            autoCompleteTextView.setAdapter(adapter);
        }
        final VatTuAdapter vatTuAdapter = new VatTuAdapter(layout.getContext(), new ArrayList<>());
        final String[] maVatTu = {""};
        listViewVatTu.setAdapter(vatTuAdapter);

        //Nhấn và giữ một item để xóa
        listViewVatTu.setOnItemLongClickListener((adapterView, view, i, l) -> {
            final VatTuAdapter.Item itemVatTu = (VatTuAdapter.Item) adapterView.getAdapter().getItem(i);
            final AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_Dialog_Alert);
            builder.setTitle("Xóa vật tư");
            builder.setMessage("Bạn có chắc muốn xóa vật tư " + itemVatTu.getTenVatTu());
            builder.setCancelable(false)
                    .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                    .setPositiveButton("Xóa", (dialogInterface, i12) -> {
                        vatTuAdapter.remove(itemVatTu);
                        vatTuAdapter.notifyDataSetChanged();
                        dialogInterface.dismiss();
                    });
            AlertDialog dialog = builder.create();
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.show();
            return false;
        });
        autoCompleteTextView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String tenVatTu = editable.toString();
                if (mLoaiSuCo != null) {
                    for (VatTu vatTu : ListObjectDB.getInstance().getVatTus()) {
                        if (vatTu.getTenVatTu().equals(tenVatTu)) {
                            txtDonViTinh.setText(vatTu.getDonViTinh());
                            maVatTu[0] = vatTu.getMaVatTu();
                            break;
                        }
                    }
                }
            }
        });
        txtThemVatTu.setOnClickListener(view -> {
            if (etxtSoLuong.getText().toString().trim().length() == 0)
                MySnackBar.make(etxtSoLuong, mMainActivity.getString(R.string.message_soluong_themvattu), true);
            else {
                try {
                    double soLuong = Double.parseDouble(etxtSoLuong.getText().toString());
                    HoSoVatTuSuCo hoSoVatTuSuCo = new HoSoVatTuSuCo(mIDSuCo, soLuong, maVatTu[0], autoCompleteTextView.getText().toString(),
                            txtDonViTinh.getText().toString());

                    vatTuAdapter.add(new VatTuAdapter.Item(hoSoVatTuSuCo.getTenVatTu(),
                            hoSoVatTuSuCo.getSoLuong(), hoSoVatTuSuCo.getDonViTinh(), hoSoVatTuSuCo.getMaVatTu()));
                    vatTuAdapter.notifyDataSetChanged();

                    autoCompleteTextView.setText("");
                    etxtSoLuong.setText("");
                    txtDonViTinh.setText("");

                    if (listViewVatTu.getHeight() > 500) {
                        ViewGroup.LayoutParams params = listViewVatTu.getLayoutParams();
                        params.height = 500;
                        listViewVatTu.setLayoutParams(params);
                    }
                } catch (NumberFormatException e) {
                    MySnackBar.make(etxtSoLuong, mMainActivity.getString(R.string.message_number_format_exception), true);
                }

            }
        });

        for (HoSoVatTuSuCo hoSoVatTuSuCo : mListHoSoVatTuSuCo) {
            vatTuAdapter.add(new VatTuAdapter.Item(hoSoVatTuSuCo.getTenVatTu(), hoSoVatTuSuCo.getSoLuong(),
                    hoSoVatTuSuCo.getDonViTinh(), hoSoVatTuSuCo.getMaVatTu()));
        }
        vatTuAdapter.notifyDataSetChanged();

    }

    private void loadDataEdit_Another(FeatureViewMoreInfoAdapter.Item item, LinearLayout layout) {
        final FrameLayout layoutTextView = layout.findViewById(R.id.layout_edit_viewmoreinfo_TextView);
        final TextView textView = layout.findViewById(R.id.txt_edit_viewmoreinfo);
        final Button button = layout.findViewById(R.id.btn_edit_viewmoreinfo);
        final LinearLayout layoutEditText = layout.findViewById(R.id.layout_edit_viewmoreinfo_Editext);
        final EditText editText = layout.findViewById(R.id.etxt_edit_viewmoreinfo);
        final LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
        final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);
        final AutoCompleteTextView autoCompleteTextView = layout.findViewById(R.id.autoCompleteTV_edit_viewmoreinfo);
        autoCompleteTextView.setBackgroundResource(R.drawable.layout_border);

        final Domain domain = mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain();
        if (item.getFieldName().equals(mSelectedArcGISFeature.getFeatureTable().getTypeIdField())) {
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
                    final View dialogView = View.inflate(mMainActivity, R.layout.date_time_picker, null);
                    final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(mMainActivity).create();
                    dialogView.findViewById(R.id.date_time_set).setOnClickListener(view -> {
                        DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
                        String s = String.format(getString(R.string.format_date_month_year), datePicker.getDayOfMonth(), datePicker.getMonth(), datePicker.getYear());

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
            layout, AdapterView<?> parent, DialogInterface dialog) {
        boolean isCanUpdate = true;
        final TextView textView = layout.findViewById(R.id.txt_edit_viewmoreinfo);
        final EditText editText = layout.findViewById(R.id.etxt_edit_viewmoreinfo);
        final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);
        final AutoCompleteTextView autoCompleteTextView = layout.findViewById(R.id.autoCompleteTV_edit_viewmoreinfo);
        autoCompleteTextView.setBackgroundResource(R.drawable.layout_border);
        final ListView listViewVatTu = layout.findViewById(R.id.lstview_viewmoreinfo_autoCompleteTV);

        final Domain domain = mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain();
        if (item.getFieldName().equals(mMainActivity.getString(R.string.Field_MADMA))) {
            item.setValue(spin.getSelectedItem().toString());
        } else if (mLoaiSuCo != null && item.getFieldName().equals(mMainActivity.getString(R.string.Field_SuCo_ViTri))) {
            if (mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngNganh))) {
                item.setValue(spin.getSelectedItem().toString());
            } else if (mLoaiSuCo.equals(mMainActivity.getString(R.string.LoaiSuCo_OngChinh))) {
                item.setValue(spin.getSelectedItem().toString() + editText.getText().toString());
            }
        } else if (item.getFieldName().equals(mMainActivity.getString(R.string.Field_SuCo_VatTu))) {
            mListHoSoVatTuSuCo = new ArrayList<>();

            if (listViewVatTu.getAdapter() != null) {
                if (listViewVatTu.getAdapter().getCount() == 0) {
                    isCanUpdate = false;
                    MySnackBar.make(listViewVatTu, mMainActivity.getResources().getString(R.string.message_CapNhat_VatTu), true);
                } else {
                    VatTuAdapter vatTuAdapter = (VatTuAdapter) listViewVatTu.getAdapter();
                    for (VatTuAdapter.Item itemVatTu : vatTuAdapter.getItems()) {
                        mListHoSoVatTuSuCo.add(new HoSoVatTuSuCo(mIDSuCo, itemVatTu.getSoLuong(), itemVatTu.getMaVatTu(), itemVatTu.getTenVatTu(), itemVatTu.getDonVi()));
                    }
                    if (mListHoSoVatTuSuCo.size() > 0) {
                        VatTuAdapter.Item itemVatTu = vatTuAdapter.getItem(0);
                        assert itemVatTu != null;
                        item.setValue(itemVatTu.getTenVatTu() + "\n" + itemVatTu.getSoLuong() + " " + itemVatTu.getDonVi() + "\n...");
                    }
                }
            }

        } else if (item.getFieldName().equals(mSelectedArcGISFeature.getFeatureTable().getTypeIdField()) || (domain != null)) {
            //Khi đổi subtype
            //Phải set những field liên quan đến subtype = null;
            if ((item.getValue() == null || !item.getValue().equals(spin.getSelectedItem().toString())) && item.getFieldName().equals(mSelectedArcGISFeature.getFeatureTable().getTypeIdField())) {
                String[] field_subtypeArr = mMainActivity.getResources().getStringArray(R.array.field_subtype_array);
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
            if (item.getFieldName().equals(mSelectedArcGISFeature.getFeatureTable().getTypeIdField())) {
                for (int i = 0; i < mSelectedArcGISFeature.getFeatureTable().getFeatureTypes().size(); i++) {
                    FeatureType featureType = mSelectedArcGISFeature.getFeatureTable().getFeatureTypes().get(i);
                    if (featureType.getName().equals(item.getValue())) {
                        mLoaiSuCoShort = (Short.parseShort(featureType.getId().toString()));
                        break;
                    }
                }

                mLoaiSuCo = item.getValue();
            }
        } else {
            switch (item.getFieldType()) {
                case DATE:
                    item.setValue(textView.getText().toString());
                    break;
                case DOUBLE:
                    try {
                        double x = Double.parseDouble(editText.getText().toString());
                        item.setValue(String.format("%s", x));
                    } catch (Exception e) {
                        Toast.makeText(mMainActivity, "Số liệu nhập vào không đúng định dạng!!!", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(mMainActivity, "Số liệu nhập vào không đúng định dạng!!!", Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        }
        if (isCanUpdate) {
            dialog.dismiss();
            item.setEdited(true);
            FeatureViewMoreInfoAdapter adapter = (FeatureViewMoreInfoAdapter) parent.getAdapter();
            new NotifyDataSetChangeAsync(mMainActivity).execute(adapter);
        }
    }


    private void deleteFeature() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_Dialog_Alert);
        builder.setTitle("Xác nhận");
        builder.setMessage("Bạn có chắc chắn xóa sự cố này?");
        builder.setPositiveButton("Có", (dialog, which) -> {
            dialog.dismiss();
            mSelectedArcGISFeature.loadAsync();

            // update the selected feature
            mSelectedArcGISFeature.addDoneLoadingListener(() -> {
                if (mSelectedArcGISFeature.getLoadStatus() == LoadStatus.FAILED_TO_LOAD) {
                    Log.d(mMainActivity.getResources().getString(R.string.app_name), "Error while loading feature");
                }
                try {
                    // update feature in the feature table
                    ListenableFuture<Void> mapViewResult = mServiceFeatureTable.deleteFeatureAsync(mSelectedArcGISFeature);
                    mapViewResult.addDoneListener(() -> {
                        // apply change to the server
                        final ListenableFuture<List<FeatureEditResult>> serverResult = mServiceFeatureTable.applyEditsAsync();
                        serverResult.addDoneListener(() -> {

                            List<FeatureEditResult> edits;
                            try {
//                                            HoSoVatTuSuCoAsync hoSoVatTuSuCoDB = new HoSoVatTuSuCoAsync(mMainActivity);
//                                            hoSoVatTuSuCoDB.delete(mIDSuCo);
                                edits = serverResult.get();
                                if (edits.size() > 0) {
                                    if (!edits.get(0).hasCompletedWithErrors()) {

                                        Log.e("", "Feature successfully updated");
                                    }
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }

                        });
                    });

                } catch (Exception e) {
                    Log.e(mMainActivity.getResources().getString(R.string.app_name), "deteting feature in the feature table failed: " + e.getMessage());
                }
            });
            if (mCallout != null) mCallout.dismiss();
        }).setNegativeButton("Không", (dialog, which) -> dialog.dismiss()).setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();


    }

    public void capture(boolean isAddFeature) {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.getPath());

        File photo = ImageFile.getFile(mMainActivity);
//        this.mUri= FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".my.package.name.provider", photo);
        Uri uri = Uri.fromFile(photo);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        mMainActivity.setSelectedArcGISFeature(mSelectedArcGISFeature);
        mMainActivity.setFeatureViewMoreInfoAdapter(mFeatureViewMoreInfoAdapter);
        mMainActivity.setUri(uri);
//        this.mUri = Uri.fromFile(photo);
        if (isAddFeature)
            mMainActivity.startActivityForResult(cameraIntent, mMainActivity.getResources().getInteger(R.integer.REQUEST_ID_IMAGE_CAPTURE_ADD_FEATURE));
        else
            mMainActivity.startActivityForResult(cameraIntent, mMainActivity.getResources().getInteger(R.integer.REQUEST_ID_IMAGE_CAPTURE_POPUP));
    }

    private void clearSelection() {
        FeatureLayer featureLayer = mApplication.getFeatureLayerDTG().getLayer();
        featureLayer.clearSelection();

    }

    private void dimissCallout() {
        if (mCallout != null && mCallout.isShowing()) {
            mCallout.dismiss();
        }
    }

    @SuppressLint("InflateParams")
    public void showPopup(final boolean isAddFeature) {
        initializeVatTu();
        clearSelection();
        dimissCallout();
        this.mSelectedArcGISFeature = mApplication.getDiemSuCo().getArcGISFeature();

        FeatureLayer featureLayer = mApplication.getFeatureLayerDTG().getLayer();
        featureLayer.selectFeature(mSelectedArcGISFeature);
        lstFeatureType = new ArrayList<>();
        for (int i = 0; i < mSelectedArcGISFeature.getFeatureTable().getFeatureTypes().size(); i++) {
            lstFeatureType.add(mSelectedArcGISFeature.getFeatureTable().getFeatureTypes().get(i).getName());
        }
        LayoutInflater inflater = LayoutInflater.from(this.mMainActivity.getApplicationContext());
        linearLayout = (LinearLayout) inflater.inflate(R.layout.layout_thongtinsuco, null);
        refreshPopup(mSelectedArcGISFeature);
        ((TextView) linearLayout.findViewById(R.id.txt_thongtin_ten)).setText(featureLayer.getName());
        linearLayout.findViewById(R.id.imgBtn_layout_thongtinsuco).setOnClickListener(this);
        if (featureLayer.getName().equals(mMainActivity.getString(R.string.ALIAS_DIEM_SU_CO))) {
            //user admin mới có quyền xóa
            if (mApplication.getFeatureLayerDTG().getLayerInfoDTG().isDelete()) {
                linearLayout.findViewById(R.id.imgBtn_delete).setOnClickListener(this);
            } else {
                linearLayout.findViewById(R.id.imgBtn_delete).setVisibility(View.GONE);
            }
            //khi hoàn thành rồi thì không chỉnh sửa được
            Object o = mSelectedArcGISFeature.getAttributes().get(mMainActivity.getString(R.string.Field_SuCo_TrangThai));
            if (o != null && Integer.parseInt(o.toString())
                    != mMainActivity.getResources().getInteger(R.integer.trang_thai_hoan_thanh) && mApplication.getFeatureLayerDTG().getLayerInfoDTG().isEdit())
                linearLayout.findViewById(R.id.imgBtn_ViewMoreInfo).setOnClickListener(this);
            else
                linearLayout.findViewById(R.id.imgBtn_ViewMoreInfo).setVisibility(View.GONE);
        } else {
            linearLayout.findViewById(R.id.imgBtn_ViewMoreInfo).setVisibility(View.INVISIBLE);
            linearLayout.findViewById(R.id.imgBtn_delete).setVisibility(View.INVISIBLE);
        }

        linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        Envelope envelope = mSelectedArcGISFeature.getGeometry().getExtent();
        mMapView.setViewpointGeometryAsync(envelope, 0);
        // show CallOut
        mCallout.setLocation(envelope.getCenter());
        mCallout.setContent(linearLayout);
        this.runOnUiThread(() -> {
            mCallout.refresh();
            mCallout.show();
            if (isAddFeature) {
                viewMoreInfo(true);
            }
        });
    }

    @SuppressLint("InflateParams")
    public void showPopupFindLocation(Point position, String location) {
        try {
            if (position == null)
                return;
            clearSelection();
            dimissCallout();

            LayoutInflater inflater = LayoutInflater.from(this.mMainActivity.getApplicationContext());
            linearLayout = (LinearLayout) inflater.inflate(R.layout.layout_timkiemdiachi, null);

            ((TextView) linearLayout.findViewById(R.id.txt_timkiemdiachi)).setText(location);
            linearLayout.findViewById(R.id.imgBtn_timkiemdiachi_themdiemsuco).setOnClickListener(this);
            linearLayout.findViewById(R.id.imgBtn_timkiemdiachi).setOnClickListener(this);


            linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            // show CallOut
            mCallout.setLocation(position);
            mCallout.setContent(linearLayout);
            this.runOnUiThread(() -> {
                mCallout.refresh();
                mCallout.show();
            });
        } catch (Exception e) {
            Log.e("Popup tìm kiếm", e.toString());
        }

    }

    public void showPopupFindLocation(final Point position) {
        try {
            if (position == null)
                return;

            @SuppressLint("InflateParams") FindLocationAsycn findLocationAsycn = new FindLocationAsycn(mMainActivity, false,
                    mGeocoder, output -> {
                if (output != null && output.size() > 0) {
                    clearSelection();
                    dimissCallout();
                    MyAddress address = output.get(0);
                    String addressLine = address.getLocation();
                    LayoutInflater inflater = LayoutInflater.from(mMainActivity.getApplicationContext());
                    linearLayout = (LinearLayout) inflater.inflate(R.layout.layout_timkiemdiachi, null);
                    ((TextView) linearLayout.findViewById(R.id.txt_timkiemdiachi)).setText(addressLine);
                    linearLayout.findViewById(R.id.imgBtn_timkiemdiachi_themdiemsuco).setOnClickListener(Popup.this);
                    linearLayout.findViewById(R.id.imgBtn_timkiemdiachi).setOnClickListener(Popup.this);
                    linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    // show CallOut
                    mCallout.setLocation(position);
                    mCallout.setContent(linearLayout);
                    Popup.this.runOnUiThread(() -> {
                        mCallout.refresh();
                        mCallout.show();
                    });
                }
            });
            Geometry project = GeometryEngine.project(position, SpatialReferences.getWgs84());
            double[] location = {project.getExtent().getCenter().getX(), project.getExtent().getCenter().getY()};
            findLocationAsycn.setmLongtitude(location[0]);
            findLocationAsycn.setmLatitude(location[1]);
            findLocationAsycn.execute();
        } catch (Exception e) {
            Log.e("Popup tìm kiếm", e.toString());
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgBtn_layout_thongtinsuco:
            case R.id.imgBtn_timkiemdiachi:
                if (mCallout != null && mCallout.isShowing())
                    mCallout.dismiss();
                break;
            case R.id.imgBtn_ViewMoreInfo:
                viewMoreInfo(false);
                break;
            case R.id.imgBtn_delete:
                mSelectedArcGISFeature.getFeatureTable().getFeatureLayer().clearSelection();
                deleteFeature();
                break;
            case R.id.imgBtn_timkiemdiachi_themdiemsuco:
                mMainActivity.onClick(view);
                break;
        }
    }
}
