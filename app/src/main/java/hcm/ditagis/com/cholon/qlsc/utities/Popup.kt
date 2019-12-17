package hcm.ditagis.com.cholon.qlsc.utities

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.MapView
import hcm.ditagis.com.cholon.qlsc.MainActivity
import hcm.ditagis.com.cholon.qlsc.R
import hcm.ditagis.com.cholon.qlsc.UpdateActivity
import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewInfoAdapter
import hcm.ditagis.com.cholon.qlsc.async.CheckExistFeatureAsync
import hcm.ditagis.com.cholon.qlsc.async.EditGeometryAsync
import hcm.ditagis.com.cholon.qlsc.async.FindLocationAsycn
import hcm.ditagis.com.cholon.qlsc.async.QueryFeatureAsync
import hcm.ditagis.com.cholon.qlsc.entities.DAddress
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.entities.HoSoVatTuSuCo
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.ListObjectDB
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicReference

@SuppressLint("Registered")
class Popup(private val mMainActivity: MainActivity, private val mMapView: MapView, private val mServiceFeatureTable: ServiceFeatureTable,
            val callout: Callout?) : View.OnClickListener {
    private var mListTenVatTu: MutableList<String>? = null
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var lstFeatureType: MutableList<String>? = null
    private var linearLayout: LinearLayout? = null
    private var mListHoSoVatTuSuCo: List<HoSoVatTuSuCo>? = null
    private val mApplication: DApplication = mMainActivity.application as DApplication

    private var comparator = { o1: Long, o2: Long ->
        val i = o1 - o2
        when {
            i > 0 -> 1
            i == 0L -> 0
            else -> -1
        }
    }

    private fun initializeVatTu() {
        if (mListTenVatTu == null) {
            mListTenVatTu = ArrayList()
            for (vatTu in ListObjectDB.getInstance().vatTus!!)
                mListTenVatTu!!.add(vatTu.tenVatTu)
        }

    }


    fun refreshPopup(arcGISFeature: ArcGISFeature) {
        mSelectedArcGISFeature = arcGISFeature
        val attributes = arcGISFeature.attributes
        val listView = linearLayout!!.findViewById<ListView>(R.id.lstview_thongtinsuco)
        val featureViewInfoAdapter = FeatureViewInfoAdapter(mMainActivity, ArrayList())
        listView.adapter = featureViewInfoAdapter
        val outFields = mApplication.dFeatureLayer!!.getdLayerInfo().outFieldsArr
        var isFoundField = false


        for (field in arcGISFeature.featureTable.fields) {
            if (outFields.size > 0 && outFields[0] != "*") {
                for (s in outFields)
                    if (s == field.name) {
                        isFoundField = true
                        break
                    }
                if (isFoundField) {
                    isFoundField = false
                } else
                    continue
            }
            val value = attributes[field.name]
            if (value != null) {
                val item = FeatureViewInfoAdapter.Item()

                item.alias = field.alias
                item.fieldName = field.name
                if (field.domain != null) {
                    var codedValues: List<CodedValue> = ArrayList()
                    try {
                        codedValues = (arcGISFeature.featureTable.getField(item.fieldName!!).domain as CodedValueDomain).codedValues
                    } catch (ignored: Exception) {
                    }

                    val valueDomain = getValueDomain(codedValues, value.toString())
                    if (valueDomain != null) item.value = valueDomain.toString()
                } else if (item.fieldName == mMainActivity.getString(R.string.Field_SuCo_VatTu)) {
                    val builder = StringBuilder()
                    this.mListHoSoVatTuSuCo = ListObjectDB.getInstance().hoSoVatTuSuCos
                    for (hoSoVatTuSuCo in mListHoSoVatTuSuCo!!) {
                        builder.append(hoSoVatTuSuCo.tenVatTu).append(" ").append(hoSoVatTuSuCo.soLuong).append(" ").append(hoSoVatTuSuCo.donViTinh).append("\n")
                    }
                    if (builder.length > 0)
                        builder.replace(builder.length - 2, builder.length, "")
                    item.value = builder.toString()
                } else
                    when (field.fieldType) {
                        Field.Type.DATE -> item.value = Constant.DATE_FORMAT_VIEW.format((value as Calendar).time)
                        Field.Type.OID, Field.Type.TEXT, Field.Type.SHORT, Field.Type.DOUBLE, Field.Type.INTEGER, Field.Type.FLOAT -> item.value = value.toString()
                    }
                featureViewInfoAdapter.add(item)
                featureViewInfoAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun getValueDomain(codedValues: List<CodedValue>, code: String): Any? {
        var value: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.code.toString() == code) {
                value = codedValue.name
                break
            }

        }
        return value
    }

    private fun deleteFeature() {
        val builder = AlertDialog.Builder(mMainActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
        builder.setTitle("Xác nhận")
        builder.setMessage("Bạn có chắc chắn xóa sự cố này?")
        builder.setPositiveButton("Có") { dialog, which ->
            dialog.dismiss()
            mSelectedArcGISFeature!!.loadAsync()

            // update the selected feature
            mSelectedArcGISFeature!!.addDoneLoadingListener {
                if (mSelectedArcGISFeature!!.loadStatus == LoadStatus.FAILED_TO_LOAD) {
                    Log.d(mMainActivity.resources.getString(R.string.app_name), "Error while loading feature")
                }
                try {
                    // update feature in the feature table
                    val mapViewResult = mServiceFeatureTable.deleteFeatureAsync(mSelectedArcGISFeature!!)
                    mapViewResult.addDoneListener {
                        // apply change to the server
                        val serverResult = mServiceFeatureTable.applyEditsAsync()
                        serverResult.addDoneListener {

                            val edits: List<FeatureEditResult>
                            try {
                                //                                            HoSoVatTuSuCoAsync hoSoVatTuSuCoDB = new HoSoVatTuSuCoAsync(mMainActivity);
                                //                                            hoSoVatTuSuCoDB.delete(mIDSuCo);
                                edits = serverResult.get()
                                if (edits.size > 0) {
                                    if (!edits[0].hasCompletedWithErrors()) {

                                        Log.e("", "Feature successfully updated")
                                    }
                                }
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            } catch (e: ExecutionException) {
                                e.printStackTrace()
                            }


                        }
                    }

                } catch (e: Exception) {
                    Log.e(mMainActivity.resources.getString(R.string.app_name), "deteting feature in the feature table failed: " + e.message)
                }
            }
            callout?.dismiss()
        }.setNegativeButton("Không") { dialog, which -> dialog.dismiss() }.setCancelable(false)
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.show()


    }

    fun showPopupAdd(position: Point?, isChangingGeometry: Boolean) {
        try {
            if (position == null)
                return
            val longtitude = AtomicReference(0.0)
            val latitdue = AtomicReference(0.0)
            val address = AtomicReference("")
            linearLayout = mMainActivity.layoutInflater.inflate(R.layout.layout_dialog_search_address, null) as LinearLayout
            val txtTitle = linearLayout!!.findViewById<TextView>(R.id.txt_dialog_search_address_title)
            txtTitle.text = "ĐỊA CHỈ"
            val txtAddress = linearLayout!!.findViewById<TextView>(R.id.txt_dialog_search_address_address)
            val txtUtity = linearLayout!!.findViewById<TextView>(R.id.txt_dialog_search_address_utity)
            if (isChangingGeometry) {
                txtUtity.text = "ĐỔI VỊ TRÍ"
                linearLayout!!.findViewById<View>(R.id.txt_dialog_search_address_utity).setOnClickListener { view ->
                    val pointLongLat = Point(longtitude.get(), latitdue.get())
                    val geometry = GeometryEngine.project(pointLongLat, SpatialReferences.getWgs84())
                    val geometry1 = GeometryEngine.project(geometry, SpatialReferences.getWebMercator())
                    val point = geometry1.extent.center
                    mApplication.diemSuCo.vitri = address.get()
                    mApplication.addFeaturePoint = point
                    //Kiểm tra cùng ngày, cùng vị trí đã có sự cố nào chưa, nếu có thì cảnh báo, chưa thì thêm bình thường
                    mApplication.selectedArcGISFeature?.let {
                        EditGeometryAsync(mMapView.context, mApplication.dFeatureLayer!!
                                .serviceFeatureTable, it, object : EditGeometryAsync.AsyncResponse {
                            override fun processFinish(aBoolean: Boolean?) {
                                mMainActivity.setChangingGeometry(false)
                                if (aBoolean != null && aBoolean)
                                    Toast.makeText(mMapView.context, "Đổi vị trí thành công", Toast.LENGTH_SHORT).show()
                                else {
                                    Toast.makeText(mMapView.context, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }).execute(point)
                    }
                }
            } else {
                txtUtity.text = "PHẢN ÁNH SỰ CỐ"
                linearLayout!!.findViewById<View>(R.id.txt_dialog_search_address_utity).setOnClickListener { view ->
                    val pointLongLat = Point(longtitude.get(), latitdue.get())
                    val geometry = GeometryEngine.project(pointLongLat, SpatialReferences.getWgs84())
                    val geometry1 = GeometryEngine.project(geometry, SpatialReferences.getWebMercator())
                    val point = geometry1.extent.center
                    mApplication.diemSuCo.vitri = address.get()
                    mApplication.addFeaturePoint = point
                    //Kiểm tra cùng ngày, cùng vị trí đã có sự cố nào chưa, nếu có thì cảnh báo, chưa thì thêm bình thường
                    CheckExistFeatureAsync(mMainActivity, mMapView, object : CheckExistFeatureAsync.AsyncResponse {
                        override fun processFinish(idSuCo: String?) {
                            if (idSuCo != null && idSuCo.isNotEmpty())
                                showDialogAddDuplicateGeometry(idSuCo)
                            else {

                                mMainActivity.addFeature()
                            }
                        }
                    }).execute()
                }
            }
            linearLayout!!.findViewById<View>(R.id.imgBtn_dialog_search_address_cancel).setOnClickListener { view -> mMainActivity.handlingCancelAdd() }
            linearLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            @SuppressLint("InflateParams") val findLocationAsycn = FindLocationAsycn(mMainActivity, false
                    , object : FindLocationAsycn.AsyncResponse {
                override fun processFinish(output: List<DAddress>?) {
                    if (output != null && output.size > 0) {
                        //                    clearSelection();
                        //                        dimissCallout();
                        val dAddress = output[0]
                        val addressLine = dAddress.location
                        if (addressLine.toLowerCase().contains(Constant.DiaBan.QUAN_5.toLowerCase()) ||
                                addressLine.toLowerCase().contains(Constant.DiaBan.QUAN_6.toLowerCase()) ||
                                addressLine.toLowerCase().contains(Constant.DiaBan.QUAN_8.toLowerCase()) ||
                                addressLine.toLowerCase().contains(Constant.DiaBan.QUAN_BINH_TAN.toLowerCase())) {
                            txtAddress.text = addressLine
                            address.set(addressLine)
                            longtitude.set(dAddress.longtitude)
                            latitdue.set(dAddress.latitude)
                            callout!!.location = position
                            callout.content = linearLayout!!
                            mMainActivity.runOnUiThread {
                                callout.refresh()
                                callout.show()
                            }
                        } else {
                            Toast.makeText(mMapView.context, String.format("%s không thuộc địa bàn quản lý", addressLine), Toast.LENGTH_LONG).show()
                        }
                        // show CallOutfre
                    }
                }
            })
            val project = GeometryEngine.project(position, SpatialReferences.getWgs84())
            val location = doubleArrayOf(project.extent.center.x, project.extent.center.y)
            findLocationAsycn.setmLongtitude(location[0])
            findLocationAsycn.setmLatitude(location[1])
            findLocationAsycn.execute()


        } catch (e: Exception) {
            Log.e("Popup tìm kiếm", e.toString())
        }

    }

    private fun showDialogAddDuplicateGeometry(idSuCo: String?) {
        val builder = AlertDialog.Builder(mMapView.context, R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        builder.setCancelable(true)
                .setNegativeButton("HỦY") { dialogInterface, i -> dialogInterface.dismiss() }
                .setPositiveButton("TIẾP TỤC") { dialogInterface, i -> mMainActivity.addFeature() }.setTitle("CẢNH BÁO")
                .setMessage(String.format("Hệ thống phát hiện ở khu vực này đã tiếp nhận sự cố với ID là %s trong ngày hôm nay. Bạn có muốn tiếp tục phản ánh sự cố?", idSuCo))
        val dialog = builder.create()
        dialog.show()
    }


    private fun clearSelection() {
        val featureLayer = mApplication.dFeatureLayer!!.layer
        featureLayer.clearSelection()

    }

    private fun dimissCallout() {
        if (callout != null && callout.isShowing) {
            callout.dismiss()
        }
    }

    @SuppressLint("InflateParams")
    fun showPopup(isAddFeature: Boolean) {
        initializeVatTu()
        clearSelection()
        dimissCallout()
        this.mSelectedArcGISFeature = mApplication.selectedArcGISFeature

        val featureLayer = mApplication.dFeatureLayer!!.layer
        featureLayer.selectFeature(mSelectedArcGISFeature!!)
        lstFeatureType = ArrayList()
        for (i in 0 until mSelectedArcGISFeature!!.featureTable.featureTypes.size) {
            lstFeatureType!!.add(mSelectedArcGISFeature!!.featureTable.featureTypes[i].name)
        }
        val inflater = LayoutInflater.from(this.mMainActivity.applicationContext)
        linearLayout = inflater.inflate(R.layout.layout_thongtinsuco, null) as LinearLayout
        refreshPopup(mSelectedArcGISFeature!!)
        (linearLayout!!.findViewById<View>(R.id.txt_thongtin_ten) as TextView).text = featureLayer.name
        linearLayout!!.findViewById<View>(R.id.imgBtn_layout_thongtinsuco).setOnClickListener(this)
        linearLayout!!.findViewById<View>(R.id.txt_thongtinsuco_prev).setOnClickListener({ this.onClick(it) })
        linearLayout!!.findViewById<View>(R.id.txt_thongtinsuco_next).setOnClickListener({ this.onClick(it) })
        val txtNumber = linearLayout!!.findViewById<TextView>(R.id.txt_thongtinsuco_number)
        if (featureLayer.name == mMainActivity.getString(R.string.ALIAS_DIEM_SU_CO)) {
            //user admin mới có quyền xóa
            if (mApplication.dFeatureLayer!!.getdLayerInfo().isDelete) {
                linearLayout!!.findViewById<View>(R.id.imgBtn_delete).setOnClickListener(this)
            } else {
                linearLayout!!.findViewById<View>(R.id.imgBtn_delete).visibility = View.GONE
            }
            linearLayout!!.findViewById<View>(R.id.imgBtn_ViewMoreInfo).setOnClickListener(this)
        } else {
            linearLayout!!.findViewById<View>(R.id.imgBtn_ViewMoreInfo).visibility = View.INVISIBLE
            linearLayout!!.findViewById<View>(R.id.imgBtn_delete).visibility = View.INVISIBLE
        }

        linearLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val envelope = mSelectedArcGISFeature!!.geometry.extent
        mMapView.setViewpointGeometryAsync(envelope, 0.0)
        // show CallOut
        callout!!.location = envelope.center
        callout.content = linearLayout!!
        callout.show()
    }

    @SuppressLint("InflateParams")
    fun showPopupFindLocation(position: Point?, location: String) {
        try {
            if (position == null)
                return
            clearSelection()
            dimissCallout()

            val inflater = LayoutInflater.from(this.mMainActivity.applicationContext)
            linearLayout = inflater.inflate(R.layout.layout_timkiemdiachi, null) as LinearLayout

            (linearLayout!!.findViewById<View>(R.id.txt_timkiemdiachi) as TextView).text = location
            linearLayout!!.findViewById<View>(R.id.imgBtn_timkiemdiachi_themdiemsuco).setOnClickListener(this)
            linearLayout!!.findViewById<View>(R.id.imgBtn_timkiemdiachi).setOnClickListener(this)


            linearLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            // show CallOut
            callout!!.location = position
            callout.content = linearLayout!!
            mMainActivity.runOnUiThread {
                callout.refresh()
                callout.show()
            }
        } catch (e: Exception) {
            Log.e("Popup tìm kiếm", e.toString())
        }

    }

    fun showPopupFindLocation(position: Point?) {
        try {
            if (position == null)
                return

            @SuppressLint("InflateParams") val findLocationAsycn = FindLocationAsycn(mMainActivity, false
                    , object : FindLocationAsycn.AsyncResponse {
                override fun processFinish(output: List<DAddress>?) {
                    if (output != null && output.size > 0) {
                        clearSelection()
                        dimissCallout()
                        val address = output[0]
                        val addressLine = address.location
                        val inflater = LayoutInflater.from(mMainActivity.applicationContext)
                        linearLayout = inflater.inflate(R.layout.layout_timkiemdiachi, null) as LinearLayout
                        (linearLayout!!.findViewById<View>(R.id.txt_timkiemdiachi) as TextView).text = addressLine
                        linearLayout!!.findViewById<View>(R.id.imgBtn_timkiemdiachi_themdiemsuco).setOnClickListener(this@Popup)
                        linearLayout!!.findViewById<View>(R.id.imgBtn_timkiemdiachi).setOnClickListener(this@Popup)
                        linearLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        // show CallOut
                        callout!!.location = position
                        callout.content = linearLayout!!
                        mMainActivity.runOnUiThread {
                            callout.refresh()
                            callout.show()
                        }
                    }
                }
            })
            val project = GeometryEngine.project(position, SpatialReferences.getWgs84())
            val location = doubleArrayOf(project.extent.center.x, project.extent.center.y)
            findLocationAsycn.setmLongtitude(location[0])
            findLocationAsycn.setmLatitude(location[1])
            findLocationAsycn.execute()
        } catch (e: Exception) {
            Log.e("Popup tìm kiếm", e.toString())
        }

    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.imgBtn_layout_thongtinsuco, R.id.imgBtn_timkiemdiachi -> if (callout != null && callout.isShowing)
                callout.dismiss()
            R.id.imgBtn_ViewMoreInfo -> {
                val popup = PopupMenu(mMainActivity, view)
                val inflater = popup.menuInflater
                inflater.inflate(R.menu.menu_feature_popup, popup.menu)
                popup.setOnMenuItemClickListener { item: MenuItem ->
                    when (item.itemId) {
                        R.id.item_popup_find_route -> {
                            mMainActivity.findRoute()
                            true
                        }
                        R.id.item_popup_edit -> {
                            val updateIntent = Intent(mMainActivity, UpdateActivity::class.java)
                            mMainActivity.startActivityForResult(updateIntent, Constant.RequestCode.UPDATE)
                            true
                        }
                        R.id.item_popup_change_geometry -> {
                            mMainActivity.setChangingGeometry(true)
                            if (callout!!.isShowing)
                                callout.dismiss()
                            true
                        }
                        else -> false
                    }
                }
                popup.show()
            }
            R.id.imgBtn_delete -> {
                mSelectedArcGISFeature!!.featureTable.featureLayer.clearSelection()
                deleteFeature()
            }
            R.id.imgBtn_timkiemdiachi_themdiemsuco -> mMainActivity.onClick(view)
            R.id.txt_thongtinsuco_prev -> QueryFeatureAsync(mMainActivity, Constant.TrangThaiSuCo.CHUA_XU_LY.toInt(), "", "",
                    object : QueryFeatureAsync.AsyncResponse {
                        override fun processFinish(output: List<Feature>?) {
                            if (output != null && output.isNotEmpty()) {
                                val objectID = getObjectID(output, true)
                                mMainActivity.mapViewHandler!!.query(String.format(Constant.QUERY_BY_OBJECTID, objectID))
                            }
                        }
                    }).execute()
            R.id.txt_thongtinsuco_next -> QueryFeatureAsync(mMainActivity, Constant.TrangThaiSuCo.CHUA_XU_LY.toInt(), "", "",
                    object : QueryFeatureAsync.AsyncResponse {
                        override fun processFinish(output: List<Feature>?) {
                            if (output != null && output.size > 0) {
                                val objectID = getObjectID(output, false)
                                mMainActivity.mapViewHandler!!.query(String.format(Constant.QUERY_BY_OBJECTID, objectID))
                            }
                        }
                    }).execute()
        }//                viewMoreInfo(false);
    }

    private fun getObjectID(output: List<Feature>, isPrev: Boolean): Long {
        val list = ArrayList<Long>()
        for (feature in output) {
            list.add(java.lang.Long.parseLong(feature.attributes[Constant.Field.OBJECTID].toString()))
        }

        val currentObjectID = java.lang.Long.parseLong(mApplication.selectedArcGISFeature!!.attributes[Constant.Field.OBJECTID].toString())
        var i = 0
        while (i < list.size) {
            if (list[i] >= currentObjectID)
                break
            i++
        }
        return if (isPrev)
            if (i > 0) list[i - 1] else currentObjectID
        else
            if (i < list.size) list[i + 1] else currentObjectID
    }
}
