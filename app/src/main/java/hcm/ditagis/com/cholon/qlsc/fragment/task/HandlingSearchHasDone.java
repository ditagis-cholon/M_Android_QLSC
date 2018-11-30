package hcm.ditagis.com.cholon.qlsc.fragment.task;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Feature;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import hcm.ditagis.com.cholon.qlsc.ListTaskActivity;
import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;

public class HandlingSearchHasDone {
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<View> handleFromFeatures(Activity activity, Context context, List<Feature> featureList) {
        List<View> views = new ArrayList<>();
        try {

            List<Item> chuaXuLyList = new ArrayList<>();
            for (Feature feature : featureList) {
                Map<String, Object> attributes = feature.getAttributes();
                Object idSuCo = attributes.get(Constant.FieldSuCo.ID_SUCO);
                Object ngayXayRa = attributes.get(Constant.FieldSuCo.TG_PHAN_ANH);
                Object thongTinPhanAnhCode = attributes.get(Constant.FieldSuCo.THONG_TIN_PHAN_ANH);
                List<CodedValue> codedValues = ((CodedValueDomain) feature.getFeatureTable().getField(Constant.FieldSuCo.THONG_TIN_PHAN_ANH).getDomain()).getCodedValues();
                Object thongTinPhanAnhValue = thongTinPhanAnhCode == null ? null : getValueDomain(codedValues, thongTinPhanAnhCode);
                Item item = new Item(Integer.parseInt(attributes.get(Constant.Field.OBJECTID).toString()),
                        idSuCo != null ? idSuCo.toString() : "",
                        ngayXayRa != null ? Constant.DateFormat.DATE_FORMAT_VIEW.format(((Calendar) ngayXayRa).getTime()) : "",
                        attributes.get(Constant.FieldSuCo.DIA_CHI) != null ? attributes.get(Constant.FieldSuCo.DIA_CHI).toString() : "",
                        thongTinPhanAnhCode != null ? Short.parseShort(thongTinPhanAnhCode.toString()) : Constant.ThongTinPhanAnh.KHAC,
                        thongTinPhanAnhValue != null ? thongTinPhanAnhValue.toString() : "");
                Object value = feature.getAttributes().get(Constant.FieldSuCo.TRANG_THAI);
                if (value == null) {
                    chuaXuLyList.add(item);
                } else {
                    short trangThai = Short.parseShort(value.toString());
                    switch (trangThai) {
                        case Constant.TrangThaiSuCo.CHUA_XU_LY:
                            chuaXuLyList.add(item);
                            break;
                    }
                }
            }
            views = handleFromItems(activity, context, chuaXuLyList);
        } catch (Exception e) {
            Log.e("Lỗi lấy ds công việc", e.toString());
        }
        return views;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public static List<View> handleFromItems(Activity activity, Context context, List<Item> items) {
        List<View> views = new ArrayList<>();
        try {

            Comparator<Item> comparator = (Item o1, Item o2) -> {
                try {
                    long i = Constant.DateFormat.DATE_FORMAT_VIEW.parse(o2.getNgayThongBao()).getTime() -
                            Constant.DateFormat.DATE_FORMAT_VIEW.parse(o1.getNgayThongBao()).getTime();
                    if (i > 0)
                        return 1;
                    else if (i == 0)
                        return 0;
                    else return -1;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            };
            List<Item> list = items;
            list.sort(comparator);
            for (Item item : list) {
                LinearLayout layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.item_tracuu, null);
                TextView txtThongTinPhanAnh = layout.findViewById(R.id.txt_bottom);
                TextView txtDiaChi = layout.findViewById(R.id.txt_bottom1);
                TextView txtID = layout.findViewById(R.id.txt_top);
                TextView txtNgayCapNhat = layout.findViewById(R.id.txt_right);
                switch (item.getThongTinPhanAnh()) {
                    case Constant.ThongTinPhanAnh.KHONG_NUOC:
                    case Constant.ThongTinPhanAnh.XI_DHN:
                    case Constant.ThongTinPhanAnh.ONG_BE:
                        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_chua_sua_chua));
                        txtDiaChi.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        txtID.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        txtNgayCapNhat.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        txtThongTinPhanAnh.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        break;
                    case Constant.ThongTinPhanAnh.HU_VAN:
                    case Constant.ThongTinPhanAnh.KHAC:
                    case Constant.ThongTinPhanAnh.NUOC_DUC:
                    case Constant.ThongTinPhanAnh.NUOC_YEU:
                        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_dang_sua_chua));
                        txtDiaChi.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        txtID.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        txtNgayCapNhat.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        txtThongTinPhanAnh.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                        break;
                    default:
                        break;
                }


                if (item.getId() == null || item.getId().isEmpty())
                    txtID.setVisibility(View.GONE);
                else
                    txtID.setText(item.getId());

                if (item.getDiaChi() == null || item.getDiaChi().isEmpty())
                    txtDiaChi.setVisibility(View.GONE);
                else
                    txtDiaChi.setText(item.getDiaChi());

                if (item.getNgayThongBao() == null || item.getNgayThongBao().isEmpty())
                    txtNgayCapNhat.setVisibility(View.GONE);
                else
                    txtNgayCapNhat.setText(item.getNgayThongBao());
                if (item.getThongTinPhanAnhString() != null && item.getThongTinPhanAnhString().isEmpty()) {
                    txtThongTinPhanAnh.setVisibility(View.GONE);
                } else txtThongTinPhanAnh.setText(item.getThongTinPhanAnhString());


                views.add(layout);
            }
        } catch (Exception e) {
            Log.e("Lỗi lấy ds công việc", e.toString());
        }
        return views;
    }

    public static Object getValueDomain(List<CodedValue> codedValues, Object code) {
        Object value = null;
        for (CodedValue codedValue : codedValues) {
            if (codedValue.getCode().equals(code)) {
                value = codedValue.getName();
                break;
            }
        }
        return value;
    }

    public static class Item {


        int objectID;
        String id;
        String ngayThongBao;
        String diaChi;
        double latitude;
        double longtitude;
        String thongTinPhanAnhString;
        short thongTinPhanAnh;

        public Item(int objectID, String id, String ngayCapNhat, String diaChi, short thongTinPhanAnh, String thongTinPhanAnhString) {
            this.objectID = objectID;
            this.id = id;
            this.ngayThongBao = ngayCapNhat;
            this.diaChi = diaChi;
            this.thongTinPhanAnh = thongTinPhanAnh;
            this.thongTinPhanAnhString = thongTinPhanAnhString;
        }

        public Item(int objectID, String id, String ngayCapNhat, String diaChi) {
            this.objectID = objectID;
            this.id = id;
            this.ngayThongBao = ngayCapNhat;
            this.diaChi = diaChi;
        }

        public String getThongTinPhanAnhString() {
            return thongTinPhanAnhString;
        }

        public short getThongTinPhanAnh() {
            return thongTinPhanAnh;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongtitude() {
            return longtitude;
        }

        public void setLongtitude(double longtitude) {
            this.longtitude = longtitude;
        }

        public int getObjectID() {
            return objectID;
        }


        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }


        public String getNgayThongBao() {
            return ngayThongBao;
        }


        public String getDiaChi() {
            return diaChi;
        }

    }
}
