package hcm.ditagis.com.cholon.qlsc.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;

/**
 * Created by ThanLe on 04/10/2017.
 */

public class TraCuuAdapter extends ArrayAdapter<TraCuuAdapter.Item> {
    private Context context;
    private List<Item> items;

    public TraCuuAdapter(Context context, List<Item> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    public List<Item> getItems() {
        return items;
    }

    public void clear() {
        items.clear();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("InflateParams")
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.item_tracuu, null);
        }
        Item item = items.get(position);
        TextView txtThongTinPhanAnh = convertView.findViewById(R.id.txt_bottom);
        TextView txtDiaChi = convertView.findViewById(R.id.txt_bottom1);
        TextView txtID = convertView.findViewById(R.id.txt_top);
        TextView txtNgayCapNhat = convertView.findViewById(R.id.txt_right);
        LinearLayout layout = convertView.findViewById(R.id.layout_tracuu);
        switch (item.getThongTinPhanAnh()) {
            //chưa sửa chữa
            case Constant.ThongTinPhanAnh.KHONG_NUOC:
            case Constant.ThongTinPhanAnh.XI_DHN:
            case Constant.ThongTinPhanAnh.ONG_BE:
                layout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_chua_sua_chua));
                txtDiaChi.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                txtID.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                txtNgayCapNhat.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                txtThongTinPhanAnh.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                break;
            //đã sửa chữa
            //đang sửa chữa
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

        return convertView;
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