package hcm.ditagis.com.cholon.qlsc.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import hcm.ditagis.com.cholon.qlsc.R;

/**
 * Created by ThanLe on 04/10/2017.
 */

public class VatTuAdapter extends ArrayAdapter<VatTuAdapter.Item> {
    private Context context;
    private List<Item> items;

    public VatTuAdapter(Context context, List<Item> items) {
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

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.item_tracuu, null);
        }
        Item item = items.get(position);

        TextView txtTop = convertView.findViewById(R.id.txt_top);
        if (item.getTenVatTu() == null || item.getTenVatTu().isEmpty())
            txtTop.setVisibility(View.GONE);
        else
            txtTop.setText(item.getTenVatTu());


        TextView txtRight =  convertView.findViewById(R.id.txt_right);
        txtRight.setText(item.getSoLuong() + " " + item.getDonVi());

        TextView txtBottom = convertView.findViewById(R.id.txt_bottom);
        txtBottom.setVisibility(View.GONE);
        return convertView;
    }


    public static class Item {
        private String tenVatTu;
        private double soLuong;
        private String donVi;
        private String maVatTu;

        public String getTenVatTu() {
            return tenVatTu;
        }

        public double getSoLuong() {
            return soLuong;
        }

        public String getDonVi() {
            return donVi;
        }

        public String getMaVatTu() {
            return maVatTu;
        }

        public Item(String tenVatTu, double soLuong, String donVi, String maVatTu) {
            this.tenVatTu = tenVatTu;
            this.soLuong = soLuong;
            this.donVi = donVi;
            this.maVatTu = maVatTu;
        }
    }
}