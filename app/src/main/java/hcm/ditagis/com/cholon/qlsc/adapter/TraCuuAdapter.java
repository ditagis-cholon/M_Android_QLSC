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

        LinearLayout layout =  convertView.findViewById(R.id.layout_tracuu);
        switch (item.getTrangThai()) {
            //chưa sửa chữa
            case 0:
                layout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_chua_sua_chua));
                break;
            //đã sửa chữa
            case 1:
                layout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_da_sua_chua));
                break;
            //đang sửa chữa
            case 2:
                layout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_dang_sua_chua));
                break;
            case 3:
                layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
                break;
            case 4:
                layout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));
                break;
        }

        TextView txtID =  convertView.findViewById(R.id.txt_top);
        if (item.getId() == null || item.getId().isEmpty())
            txtID.setVisibility(View.GONE);
        else
            txtID.setText(item.getId());

        TextView txtDiaChi =  convertView.findViewById(R.id.txt_bottom);
        if (item.getDiaChi() == null || item.getDiaChi().isEmpty())
            txtDiaChi.setVisibility(View.GONE);
        else
            txtDiaChi.setText(item.getDiaChi());

        TextView txtNgayCapNhat =  convertView.findViewById(R.id.txt_right);
        if (item.getNgayCapNhat() == null || item.getNgayCapNhat().isEmpty())
            txtNgayCapNhat.setVisibility(View.GONE);
        else
            txtNgayCapNhat.setText(item.getNgayCapNhat());



        return convertView;
    }


    public static class Item {


        int objectID;
        String id;
        int trangThai;
        String ngayCapNhat;
        String diaChi;
        double latitude;
        double longtitude;

        public Item(int objectID, String id, int trangThai, String ngayCapNhat, String diaChi) {
            this.objectID = objectID;
            this.id = id;
            this.trangThai = trangThai;
            this.ngayCapNhat = ngayCapNhat;
            this.diaChi = diaChi;
        }

        public Item(int objectID, String id, String ngayCapNhat, String diaChi) {
            this.objectID = objectID;
            this.id = id;
            this.ngayCapNhat = ngayCapNhat;
            this.diaChi = diaChi;
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

        int getTrangThai() {
            return trangThai;
        }


        String getNgayCapNhat() {
            return ngayCapNhat;
        }


        public String getDiaChi() {
            return diaChi;
        }


        @Override
        public String toString() {
            return "Item{" + "objectID=" + objectID + ", id='" + id + '\'' + ", trangThai=" + trangThai + ", ngayCapNhat='" + ngayCapNhat + '\'' + ", diaChi='" + diaChi + '\'' + '}';
        }
    }
}