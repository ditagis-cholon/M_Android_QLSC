package hcm.ditagis.com.cholon.qlsc.fragment.task

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.esri.arcgisruntime.data.CodedValue
import com.esri.arcgisruntime.data.CodedValueDomain
import com.esri.arcgisruntime.data.Feature
import hcm.ditagis.com.cholon.qlsc.R
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import java.util.*

object HandlingSearchHasDone {
    fun handleFromFeatures(activity: Activity, context: Context, featureList: List<Feature>): List<View> {
        var views: List<View> = ArrayList()
        try {

            val chuaXuLyList = ArrayList<Item>()
            for (feature in featureList) {
                val attributes = feature.attributes
                val idSuCo = attributes[Constant.FieldSuCo.ID_SUCO]
                val ngayXayRa = attributes[Constant.FieldSuCo.TG_PHAN_ANH]
                val thongTinPhanAnhCode = attributes[Constant.FieldSuCo.THONG_TIN_PHAN_ANH]
                val codedValues = (feature.featureTable.getField(Constant.FieldSuCo.THONG_TIN_PHAN_ANH).domain as CodedValueDomain).codedValues
                val thongTinPhanAnhValue = if (thongTinPhanAnhCode == null) null else getValueDomain(codedValues, thongTinPhanAnhCode)
                val item = Item(Integer.parseInt(attributes[Constant.Field.OBJECTID].toString()),
                        idSuCo?.toString() ?: "",
                        if (ngayXayRa != null) Constant.DateFormat.DATE_FORMAT_VIEW.format((ngayXayRa as Calendar).time) else "",
                        if (attributes[Constant.FieldSuCo.DIA_CHI] != null) attributes[Constant.FieldSuCo.DIA_CHI].toString() else "",
                        if (thongTinPhanAnhCode != null) java.lang.Short.parseShort(thongTinPhanAnhCode.toString()) else Constant.ThongTinPhanAnh.KHAC,
                        thongTinPhanAnhValue?.toString() ?: "")
                val value = feature.attributes[Constant.FieldSuCo.TRANG_THAI]
                if (value == null) {
                    chuaXuLyList.add(item)
                } else {
                    val trangThai = java.lang.Short.parseShort(value.toString())
                    when (trangThai) {
                        Constant.TrangThaiSuCo.CHUA_XU_LY -> chuaXuLyList.add(item)
                    }
                }
            }
            views = handleFromItems(activity, context, chuaXuLyList)
        } catch (e: Exception) {
            Log.e("Lỗi lấy ds công việc", e.toString())
        }

        return views
    }

    fun handleFromItems(activity: Activity, context: Context, items: List<Item>): List<View> {
        val views = ArrayList<View>()
        try {

            items.sortedByDescending { T -> T.ngayThongBao }
            for (item in items) {
                val layout = activity.layoutInflater.inflate(R.layout.item_tracuu, null) as LinearLayout
                val txtThongTinPhanAnh = layout.findViewById<TextView>(R.id.txt_bottom)
                val txtDiaChi = layout.findViewById<TextView>(R.id.txt_bottom1)
                val txtID = layout.findViewById<TextView>(R.id.txt_top)
                val txtNgayCapNhat = layout.findViewById<TextView>(R.id.txt_right)
                when (item.thongTinPhanAnh) {
                    Constant.ThongTinPhanAnh.KHONG_NUOC, Constant.ThongTinPhanAnh.XI_DHN, Constant.ThongTinPhanAnh.ONG_BE -> {
                        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_chua_sua_chua))
                        txtDiaChi.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                        txtID.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                        txtNgayCapNhat.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                        txtThongTinPhanAnh.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                    }
                    Constant.ThongTinPhanAnh.HU_VAN, Constant.ThongTinPhanAnh.KHAC, Constant.ThongTinPhanAnh.NUOC_DUC, Constant.ThongTinPhanAnh.NUOC_YEU -> {
                        layout.setBackgroundColor(ContextCompat.getColor(context, R.color.color_dang_sua_chua))
                        txtDiaChi.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                        txtID.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                        txtNgayCapNhat.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                        txtThongTinPhanAnh.setTextColor(ContextCompat.getColor(context, R.color.colorWhite))
                    }
                    else -> {
                    }
                }


                if (item.id == null || item.id!!.isEmpty())
                    txtID.visibility = View.GONE
                else
                    txtID.text = item.id

                if (item.diaChi == null || item.diaChi!!.isEmpty())
                    txtDiaChi.visibility = View.GONE
                else
                    txtDiaChi.text = item.diaChi

                if (item.ngayThongBao == null || item.ngayThongBao!!.isEmpty())
                    txtNgayCapNhat.visibility = View.GONE
                else
                    txtNgayCapNhat.text = item.ngayThongBao
                if (item.thongTinPhanAnhString != null && item.thongTinPhanAnhString!!.isEmpty()) {
                    txtThongTinPhanAnh.visibility = View.GONE
                } else
                    txtThongTinPhanAnh.text = item.thongTinPhanAnhString


                views.add(layout)
            }
        } catch (e: Exception) {
            Log.e("Lỗi lấy ds công việc", e.toString())
        }

        return views
    }

    fun getValueDomain(codedValues: List<CodedValue>, code: Any): Any? {
        var value: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.code == code) {
                value = codedValue.name
                break
            }
        }
        return value
    }

    class Item {


        var objectID: Int = 0
            internal set
        var id: String? = null
        var ngayThongBao: String? = null
            internal set
        var diaChi: String? = null
            internal set
        var latitude: Double = 0.toDouble()
        var longtitude: Double = 0.toDouble()
        var thongTinPhanAnhString: String? = null
            internal set
        var thongTinPhanAnh: Short = 0
            internal set

        constructor(objectID: Int, id: String, ngayCapNhat: String, diaChi: String, thongTinPhanAnh: Short, thongTinPhanAnhString: String?) {
            this.objectID = objectID
            this.id = id
            this.ngayThongBao = ngayCapNhat
            this.diaChi = diaChi
            this.thongTinPhanAnh = thongTinPhanAnh
            this.thongTinPhanAnhString = thongTinPhanAnhString
        }

        constructor(objectID: Int, id: String, ngayCapNhat: String, diaChi: String) {
            this.objectID = objectID
            this.id = id
            this.ngayThongBao = ngayCapNhat
            this.diaChi = diaChi
        }

    }
}
