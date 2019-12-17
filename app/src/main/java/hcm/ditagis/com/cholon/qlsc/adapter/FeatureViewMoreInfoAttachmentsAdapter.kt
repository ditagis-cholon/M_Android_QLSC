package hcm.ditagis.com.cholon.qlsc.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import hcm.ditagis.com.cholon.qlsc.R


/**
 * Created by ThanLe on 04/10/2017.
 */

class FeatureViewMoreInfoAttachmentsAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<FeatureViewMoreInfoAttachmentsAdapter.Item>(mContext, 0, items) {

    fun getItems(): List<Item> {
        return items
    }

    override fun clear() {
        items.clear()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_viewmoreinfo_attachment, null)
        }
        val item = items[position]

        val txtValue = convertView!!.findViewById<TextView>(R.id.txt_viewmoreinfo_attachment_name)
        txtValue.text = item.name
        if (item.bitmap != null) {
            val imageView = convertView.findViewById<ImageView>(R.id.img_viewmoreinfo_attachment)

            imageView.setImageBitmap(item.bitmap)
        }

        return convertView
    }


    class Item(val name: String, val bitmap: Bitmap)
}
