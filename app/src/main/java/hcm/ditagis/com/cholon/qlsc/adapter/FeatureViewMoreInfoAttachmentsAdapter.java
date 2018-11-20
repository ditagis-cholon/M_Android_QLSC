package hcm.ditagis.com.cholon.qlsc.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import hcm.ditagis.com.cholon.qlsc.R;


/**
 * Created by ThanLe on 04/10/2017.
 */

public class FeatureViewMoreInfoAttachmentsAdapter extends ArrayAdapter<FeatureViewMoreInfoAttachmentsAdapter.Item> {
    private Context mContext;
    private List<Item> items;

    public FeatureViewMoreInfoAttachmentsAdapter(Context context, List<Item> items) {
        super(context, 0, items);
        this.mContext = context;
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
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            assert inflater != null;
            convertView = inflater.inflate(R.layout.item_viewmoreinfo_attachment, null);
        }
        Item item = items.get(position);

        TextView txtValue = convertView.findViewById(R.id.txt_viewmoreinfo_attachment_name);
        txtValue.setText(item.getName());
        if (item.getBitmap() != null) {
            ImageView imageView = convertView.findViewById(R.id.img_viewmoreinfo_attachment);

            imageView.setImageBitmap(item.getBitmap());
        }

        return convertView;
    }


    public static class Item {
        private String name;
        private Bitmap bitmap;

        public Item(String name, Bitmap bitmap) {
            this.name = name;
            this.bitmap = bitmap;
        }

        public String getName() {
            return name;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }
}
