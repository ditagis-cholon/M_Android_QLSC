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

public class OptionAddImageAdapter extends ArrayAdapter<String> {
    private Context mContext;
    private List<String> items;

    public OptionAddImageAdapter(Context context, List<String> items) {
        super(context, 0, items);
        this.mContext = context;
        this.items = items;
    }

    public List<String> getItems() {
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
            convertView = inflater.inflate(R.layout.item_option, null);
        }
        String item = items.get(position);

        TextView txtTitle = convertView.findViewById(R.id.txt_option_title);
        txtTitle.setText(item);
        return convertView;
    }
}
