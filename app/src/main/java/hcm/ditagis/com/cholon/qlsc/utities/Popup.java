package hcm.ditagis.com.cholon.qlsc.utities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Geocoder;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureEditResult;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import hcm.ditagis.com.cholon.qlsc.MainActivity;
import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.UpdateActivity;
import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewInfoAdapter;
import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewMoreInfoAdapter;
import hcm.ditagis.com.cholon.qlsc.async.CheckExistFeatureAsync;
import hcm.ditagis.com.cholon.qlsc.async.EditGeometryAsync;
import hcm.ditagis.com.cholon.qlsc.async.FindLocationAsycn;
import hcm.ditagis.com.cholon.qlsc.async.QueryFeatureAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DAddress;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.entities.HoSoVatTuSuCo;
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
    private List<HoSoVatTuSuCo> mListHoSoVatTuSuCo;
    private DApplication mApplication;

    public Popup(MainActivity mainActivity, MapView mapView, ServiceFeatureTable serviceFeatureTable,
                 Callout callout, Geocoder geocoder) {
        this.mMainActivity = mainActivity;
        this.mApplication = (DApplication) mainActivity.getApplication();
        this.mMapView = mapView;
        this.mServiceFeatureTable = serviceFeatureTable;
        this.mCallout = callout;


    }

    private void initializeVatTu() {
        if (mListTenVatTu == null) {
            mListTenVatTu = new ArrayList<>();
            for (VatTu vatTu : ListObjectDB.getInstance().getVatTus())
                mListTenVatTu.add(vatTu.getTenVatTu());
        }

    }


    public Callout getCallout() {
        return mCallout;
    }


    public void refreshPopup(ArcGISFeature arcGISFeature) {
        mSelectedArcGISFeature = arcGISFeature;
        Map<String, Object> attributes = arcGISFeature.getAttributes();
        ListView listView = linearLayout.findViewById(R.id.lstview_thongtinsuco);
        FeatureViewInfoAdapter featureViewInfoAdapter = new FeatureViewInfoAdapter(mMainActivity, new ArrayList<>());
        listView.setAdapter(featureViewInfoAdapter);
        String[] outFields = mApplication.getFeatureLayerDTG().getLayerInfoDTG().getOutFields().split(",");
        boolean isFoundField = false;


        for (Field field : arcGISFeature.getFeatureTable().getFields()) {
            if (outFields.length > 0 && !outFields[0].equals("*")) {
                for (String s : outFields)
                    if (s.equals(field.getName())) {
                        isFoundField = true;
                        break;
                    }
                if (isFoundField) {
                    isFoundField = false;
                } else continue;
            }
            Object value = attributes.get(field.getName());
            if (value != null) {
                FeatureViewInfoAdapter.Item item = new FeatureViewInfoAdapter.Item();

                item.setAlias(field.getAlias());
                item.setFieldName(field.getName());
                if (field.getDomain() != null) {
                    List<CodedValue> codedValues = new ArrayList<>();
                    try {
                        codedValues = ((CodedValueDomain) arcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain()).getCodedValues();
                    } catch (Exception ignored) {
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

    public void showPopupAdd(final Point position, boolean isChangingGeometry) {
        try {
            if (position == null)
                return;
            AtomicReference<Double> longtitude = new AtomicReference<>(0.0);
            AtomicReference<Double> latitdue = new AtomicReference<>(0.0);
            AtomicReference<String> address = new AtomicReference<>("");
            linearLayout = (LinearLayout) mMainActivity.getLayoutInflater().inflate(R.layout.layout_dialog_search_address, null);
            TextView txtTitle = linearLayout.findViewById(R.id.txt_dialog_search_address_title);
            txtTitle.setText("ĐỊA CHỈ");
            TextView txtAddress = linearLayout.findViewById(R.id.txt_dialog_search_address_address);
            TextView txtUtity = linearLayout.findViewById(R.id.txt_dialog_search_address_utity);
            if (isChangingGeometry) {
                txtUtity.setText("ĐỔI VỊ TRÍ");
                linearLayout.findViewById(R.id.txt_dialog_search_address_utity).setOnClickListener(view -> {
                    Point pointLongLat = new Point(longtitude.get(), latitdue.get());
                    Geometry geometry = GeometryEngine.project(pointLongLat, SpatialReferences.getWgs84());
                    Geometry geometry1 = GeometryEngine.project(geometry, SpatialReferences.getWebMercator());
                    Point point = geometry1.getExtent().getCenter();
                    mApplication.getDiemSuCo().setVitri(address.get());
                    mApplication.setAddFeaturePoint(point);
                    //Kiểm tra cùng ngày, cùng vị trí đã có sự cố nào chưa, nếu có thì cảnh báo, chưa thì thêm bình thường
                    new EditGeometryAsync(mMapView.getContext(), mApplication.getFeatureLayerDTG()
                            .getServiceFeatureTable(), mApplication.getSelectedArcGISFeature(), aBoolean -> {
                        mMainActivity.setChangingGeometry(false);
                        if (aBoolean != null && aBoolean)
                            Toast.makeText(mMapView.getContext(), "Đổi vị trí thành công", Toast.LENGTH_SHORT).show();
                        else {
                            Toast.makeText(mMapView.getContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                        }
                    }).execute(point);
                });
            } else {
                txtUtity.setText("PHẢN ÁNH SỰ CỐ");
                linearLayout.findViewById(R.id.txt_dialog_search_address_utity).setOnClickListener(view -> {
                    Point pointLongLat = new Point(longtitude.get(), latitdue.get());
                    Geometry geometry = GeometryEngine.project(pointLongLat, SpatialReferences.getWgs84());
                    Geometry geometry1 = GeometryEngine.project(geometry, SpatialReferences.getWebMercator());
                    Point point = geometry1.getExtent().getCenter();
                    mApplication.getDiemSuCo().setVitri(address.get());
                    mApplication.setAddFeaturePoint(point);
                    //Kiểm tra cùng ngày, cùng vị trí đã có sự cố nào chưa, nếu có thì cảnh báo, chưa thì thêm bình thường
                    new CheckExistFeatureAsync(mMainActivity, mMapView, mApplication.getFeatureLayerDTG().getServiceFeatureTable(), idSuCo -> {
                        if (idSuCo != null && idSuCo.length() > 0)
                            showDialogAddDuplicateGeometry(idSuCo);
                        else {

                            mMainActivity.addFeature();
                        }
                    }).execute();
                });
            }
            linearLayout.findViewById(R.id.imgBtn_dialog_search_address_cancel).setOnClickListener(view -> {
                mMainActivity.handlingCancelAdd();
            });
            linearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            @SuppressLint("InflateParams") FindLocationAsycn findLocationAsycn = new FindLocationAsycn(mMainActivity, false,
                    output -> {
                        if (output != null && output.size() > 0) {
//                    clearSelection();
//                        dimissCallout();
                            DAddress dAddress = output.get(0);
                            String addressLine = dAddress.getLocation();
                            if ((addressLine.toLowerCase().contains(Constant.DiaBan.QUAN_5.toLowerCase()) ||
                                    addressLine.toLowerCase().contains(Constant.DiaBan.QUAN_6.toLowerCase()) ||
                                    addressLine.toLowerCase().contains(Constant.DiaBan.QUAN_8.toLowerCase()) ||
                                    addressLine.toLowerCase().contains(Constant.DiaBan.QUAN_BINH_TAN.toLowerCase()))) {
                                txtAddress.setText(addressLine);
                                address.set(addressLine);
                                longtitude.set(dAddress.getLongtitude());
                                latitdue.set(dAddress.getLatitude());
                                mCallout.setLocation(position);
                                mCallout.setContent(linearLayout);
                                Popup.this.runOnUiThread(() -> {
                                    mCallout.refresh();
                                    mCallout.show();
                                });
                            } else {
                                Toast.makeText(mMapView.getContext(), String.format("%s không thuộc địa bàn quản lý", addressLine), Toast.LENGTH_LONG).show();
                            }
                            // show CallOutfre
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

    private void showDialogAddDuplicateGeometry(String idSuCo) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mMapView.getContext(), R.style.Theme_AppCompat_DayNight_Dialog_Alert);
        builder.setCancelable(true)
                .setNegativeButton("HỦY", ((dialogInterface, i) -> {
                    dialogInterface.dismiss();
                }))
                .setPositiveButton("TIẾP TỤC", (dialogInterface, i) -> {
                    mMainActivity.addFeature();
                }).setTitle("CẢNH BÁO")
                .setMessage(String.format("Hệ thống phát hiện ở khu vực này đã tiếp nhận sự cố với ID là %s trong ngày hôm nay. Bạn có muốn tiếp tục phản ánh sự cố?", idSuCo));
        AlertDialog dialog = builder.create();
        dialog.show();
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
        this.mSelectedArcGISFeature = mApplication.getSelectedArcGISFeature();

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
        linearLayout.findViewById(R.id.txt_thongtinsuco_prev).setOnClickListener(this::onClick);
        linearLayout.findViewById(R.id.txt_thongtinsuco_next).setOnClickListener(this::onClick);
        TextView txtNumber = linearLayout.findViewById(R.id.txt_thongtinsuco_number);
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
        mCallout.show();
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
                    output -> {
                        if (output != null && output.size() > 0) {
                            clearSelection();
                            dimissCallout();
                            DAddress address = output.get(0);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgBtn_layout_thongtinsuco:
            case R.id.imgBtn_timkiemdiachi:
                if (mCallout != null && mCallout.isShowing())
                    mCallout.dismiss();
                break;
            case R.id.imgBtn_ViewMoreInfo:
                PopupMenu popup = new PopupMenu(mMainActivity, view);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.menu_feature_popup, popup.getMenu());
                popup.setOnMenuItemClickListener((MenuItem item) -> {
                    switch (item.getItemId()) {
                        case R.id.item_popup_find_route:
                            mMainActivity.findRoute();
                            return true;
                        case R.id.item_popup_edit:
                            Intent updateIntent = new Intent(mMainActivity, UpdateActivity.class);
                            mMainActivity.startActivityForResult(updateIntent, Constant.RequestCode.UPDATE);
                            return true;
                        case R.id.item_popup_change_geometry:
                            mMainActivity.setChangingGeometry(true);
                            if (mCallout.isShowing())
                                mCallout.dismiss();
                            return true;
                        default:
                            return false;
                    }
                });
                popup.show();

//                viewMoreInfo(false);
                break;
            case R.id.imgBtn_delete:
                mSelectedArcGISFeature.getFeatureTable().getFeatureLayer().clearSelection();
                deleteFeature();
                break;
            case R.id.imgBtn_timkiemdiachi_themdiemsuco:
                mMainActivity.onClick(view);
                break;
            case R.id.txt_thongtinsuco_prev:
                new QueryFeatureAsync(mMainActivity, Constant.TrangThaiSuCo.CHUA_XU_LY, "", "", output -> {
                    if (output != null && output.size() > 0) {
                        long objectID = getObjectID(output, comparator, true);
                        mMainActivity.getMapViewHandler().queryByObjectID(objectID);
                    }
                }).execute();
                break;
            case R.id.txt_thongtinsuco_next:
                new QueryFeatureAsync(mMainActivity, Constant.TrangThaiSuCo.CHUA_XU_LY, "", "", output -> {
                    if (output != null && output.size() > 0) {
                        long objectID = getObjectID(output, comparator, false);
                        mMainActivity.getMapViewHandler().queryByObjectID(objectID);
                    }
                }).execute();
                break;
        }
    }

    Comparator<Long> comparator = (Long o1, Long o2) -> {
        long i = o1 - o2;
        if (i > 0)
            return 1;
        else if (i == 0)
            return 0;
        else return -1;
    };

    @RequiresApi(api = Build.VERSION_CODES.N)
    private long getObjectID(List<Feature> output, Comparator<Long> comparator, boolean isPrev) {
        List<Long> list = new ArrayList<>();
        for (Feature feature : output) {
            list.add(Long.parseLong(feature.getAttributes().get(Constant.Field.OBJECTID).toString()));
        }
        list.sort(comparator);
        long currentObjectID = Long.parseLong(mApplication.getSelectedArcGISFeature().getAttributes().get(Constant.Field.OBJECTID).toString());
        int i = 0;
        for (; i < list.size(); i++) {
            if (list.get(i) >= currentObjectID)
                break;
        }
        if (isPrev)
            return i > 0 ? list.get(i - 1) : currentObjectID;
        else return i < list.size() ? list.get(i + 1) : currentObjectID;
    }
}
