package hcm.ditagis.com.cholon.qlsc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import androidx.annotation.RequiresApi
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import android.util.Log
import android.view.*
import android.widget.*
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISMapImageSublayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.*
import com.esri.arcgisruntime.symbology.PictureMarkerSymbol
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import com.esri.arcgisruntime.symbology.UniqueValueRenderer
import hcm.ditagis.com.cholon.qlsc.async.FindLocationAsycn
import hcm.ditagis.com.cholon.qlsc.async.PreparingAsycn
import hcm.ditagis.com.cholon.qlsc.async.QueryServiceFeatureTableGetListAsync
import hcm.ditagis.com.cholon.qlsc.entities.DAddress
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.entities.DLayerInfo
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.DFeatureLayer
import hcm.ditagis.com.cholon.qlsc.fragment.task.HandlingSearchHasDone
import hcm.ditagis.com.cholon.qlsc.utities.*
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*
import java.util.concurrent.ExecutionException

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private var mPopUp: Popup? = null
    private var mMapView: MapView? = null
    private var mDFeatureLayer: DFeatureLayer? = null

    var mapViewHandler: MapViewHandler? = null
        private set
    private var mLocationDisplay: LocationDisplay? = null
    private val requestCode = 2
    private var mGraphicsOverlay: GraphicsOverlay? = null
    private var mIsSearchingFeature = false
    private var mLayoutTimSuCo: LinearLayout? = null
    private var mLayoutTimDiaChi: LinearLayout? = null
    private var mLayoutTimKiem: LinearLayout? = null
    private var mFloatButtonLayer: FloatingActionButton? = null
    private var mFloatButtonLocation: FloatingActionButton? = null
    private var mDFeatureLayers: MutableList<DFeatureLayer>? = null
    private var mPointFindLocation: Point? = null
    private var mGeocoder: Geocoder? = null
    private var mImageOpenStreetMap: ImageView? = null
    private var mImageStreetMap: ImageView? = null
    private var mImageImageWithLabel: ImageView? = null
    private var mTxtOpenStreetMap: TextView? = null
    private var mTxtStreetMap: TextView? = null
    private var mTxtImageWithLabel: TextView? = null
    private var mTxtSearchView: SearchView? = null
    private var states: Array<IntArray>? = null
    private var colors: IntArray? = null
    private var hanhChinhImageLayers: ArcGISMapImageLayer? = null
    private var taiSanImageLayers: ArcGISMapImageLayer? = null
    private var mLayoutDisplayLayerThematic: LinearLayout? = null
    private var mLayoutDisplayLayerAdministration: LinearLayout? = null
    private var mSeekBarAdministrator: SeekBar? = null
    private var mSeekBarThematic: SeekBar? = null
    private var mListLayerID: MutableList<String>? = null
    private lateinit var mApplication: DApplication
    private var mIsFirstLocating = true
    private var isChangingGeometry = false
    private var mFeatureLayer: FeatureLayer? = null
    private var mLLayoutSearch: LinearLayout? = null

    fun setChangingGeometry(changingGeometry: Boolean) {
        isChangingGeometry = changingGeometry
    }

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quan_ly_su_co)
        mListLayerID = ArrayList()
        mApplication = application as DApplication
        startSignIn()
    }

    private fun startSignIn() {
        val intent = Intent(this@MainActivity, LogInActivity::class.java)
        this@MainActivity.startActivityForResult(intent, Constant.RequestCode.LOGIN)
    }

    fun requestPermisson() {
        val permissionCheck1 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[0]) == PackageManager.PERMISSION_GRANTED
        val permissionCheck2 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[1]) == PackageManager.PERMISSION_GRANTED
        val permissionCheck3 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[2]) == PackageManager.PERMISSION_GRANTED
        val permissionCheck4 = ContextCompat.checkSelfPermission(this,
                Constant.REQUEST_PERMISSIONS[3]) == PackageManager.PERMISSION_GRANTED

        if (!(permissionCheck1 && permissionCheck2 && permissionCheck3 && permissionCheck4)) {
            // If permissions are not already granted, request permission from the user.
            ActivityCompat.requestPermissions(this, Constant.REQUEST_PERMISSIONS, Constant.RequestCode.PERMISSION)
        }  // Report other unknown failure types to the user - for example, location services may not // be enabled on the device. //                    String message = String.format("Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent //                            .getSource().getLocationDataSource().getError().getMessage()); //                    Toast.makeText(QuanLySuCo.this, message, Toast.LENGTH_LONG).show();
        else {
            val preparingAsycn = PreparingAsycn(this, mApplication, object : PreparingAsycn.AsyncResponse {
                override fun processFinish(output: List<DLayerInfo>?) {
                    if (output != null && output.isNotEmpty()) {
                        mApplication!!.layerInfos = output
                        startMain()
                    } else {
                        Toast.makeText(this@MainActivity, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                        startSignIn()
                    }
                }
            })
            if (CheckConnectInternet.isOnline(this))
                preparingAsycn.execute()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        val expected = grantResults.size
        var sum = 0
        for (i in grantResults)
            if (i == PackageManager.PERMISSION_GRANTED)
                sum++
        if (sum == expected) {
            val preparingAsycn = PreparingAsycn(this, mApplication, object : PreparingAsycn.AsyncResponse {
                override fun processFinish(output: List<DLayerInfo>?) {
                    if (output != null && output.isNotEmpty()) {
                        mApplication!!.layerInfos = output
                        startMain()
                    } else {
                        Toast.makeText(this@MainActivity, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                        startSignIn()
                    }
                }
            })
            if (CheckConnectInternet.isOnline(this))
                preparingAsycn.execute()
        } else {
            //            Toast.makeText(MainActivity.this, "Vui lòng cho phép ứng dụng truy cập các quyền trên", Toast.LENGTH_LONG).show();
            requestPermisson()
        }
    }

    @SuppressLint("ClickableViewAccessibility", "SetTextI18n")
    private fun startMain() {
        // create an empty map instance
        mListLayerID!!.clear()
        taiSanImageLayers = null
        hanhChinhImageLayers = taiSanImageLayers
        states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        colors = intArrayOf(R.color.colorTextColor_1, R.color.colorTextColor_1)
        findViewById<View>(R.id.layout_layer).visibility = View.INVISIBLE
        setLicense()
        mGeocoder = Geocoder(this.applicationContext, Locale.getDefault())
        mLayoutDisplayLayerThematic = findViewById(R.id.linearDisplayLayerFeature)
        mLayoutDisplayLayerAdministration = findViewById(R.id.linearDisplayLayerAdministration)
        mLayoutDisplayLayerThematic!!.removeAllViews()
        mLayoutDisplayLayerAdministration!!.removeAllViews()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)


        //for camera begin
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        //for camera end
        mLLayoutSearch = findViewById(R.id.llayout_main_search)
        //đưa listview search ra phía sau
        mLLayoutSearch!!.invalidate()
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this@MainActivity,
                drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this@MainActivity)
        mMapView = findViewById(R.id.mapView)
        mMapView!!.map = ArcGISMap(Basemap.Type.OPEN_STREET_MAP, 10.7554041, 106.6546293, 12)

        mMapView!!.map.addDoneLoadingListener { this.handlingMapViewDoneLoading() }
        val edit_latitude_vido = findViewById<EditText>(R.id.edit_latitude_vido)
        val edit_longtitude_kinhdo = findViewById<EditText>(R.id.edit_longtitude_kinhdo)
        mMapView!!.onTouchListener = object : DefaultMapViewOnTouchListener(this, mMapView) {
            override fun onLongPress(e: MotionEvent) {
                addGraphicsAddFeature(e)
                super.onLongPress(e)
            }

            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                try {
                    if (mapViewHandler != null)
                        mapViewHandler!!.onSingleTapMapView(e!!)
                } catch (ex: ArcGISRuntimeException) {
                    Log.d("", ex.toString())
                }

                return super.onSingleTapConfirmed(e)
            }

            @SuppressLint("SetTextI18n")
            override fun onScroll(e1: MotionEvent, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                mGraphicsOverlay!!.graphics.clear()
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                return super.onScale(detector)
            }
        }
        mGraphicsOverlay = GraphicsOverlay()
        mMapView!!.graphicsOverlays.add(mGraphicsOverlay)

        mSeekBarAdministrator = findViewById(R.id.skbr_hanhchinh_app_bar_quan_ly_su_co)
        mSeekBarThematic = findViewById(R.id.skbr_chuyende_app_bar_quan_ly_su_co)
        mSeekBarAdministrator!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                hanhChinhImageLayers!!.opacity = i.toFloat() / 100
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        mSeekBarThematic!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                taiSanImageLayers!!.opacity = i.toFloat() / 100
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        findViewById<View>(R.id.layout_layer_open_street_map).setOnClickListener(this)
        findViewById<View>(R.id.layout_layer_street_map).setOnClickListener(this)
        findViewById<View>(R.id.layout_layer_image).setOnClickListener(this)


        mTxtOpenStreetMap = findViewById(R.id.txt_layer_open_street_map)
        mTxtStreetMap = findViewById(R.id.txt_layer_street_map)
        mTxtImageWithLabel = findViewById(R.id.txt_layer_image)
        mImageOpenStreetMap = findViewById(R.id.img_layer_open_street_map)
        mImageStreetMap = findViewById(R.id.img_layer_street_map)
        mImageImageWithLabel = findViewById(R.id.img_layer_image)

        mFloatButtonLayer = findViewById(R.id.floatBtnLayer)
        mFloatButtonLayer!!.setOnClickListener(this)
        findViewById<View>(R.id.btn_add_feature_close).setOnClickListener(this)
        findViewById<View>(R.id.btn_layer_close).setOnClickListener(this)
        findViewById<View>(R.id.img_chonvitri_themdiemsuco).setOnClickListener(this)
        mFloatButtonLocation = findViewById(R.id.floatBtnLocation)
        mFloatButtonLocation!!.setOnClickListener(this)
        mLayoutTimSuCo = findViewById(R.id.layout_tim_su_co)
        mLayoutTimSuCo!!.setOnClickListener(this)
        mLayoutTimDiaChi = findViewById(R.id.layout_tim_dia_chi)
        mLayoutTimDiaChi!!.setOnClickListener(this)
        mLayoutTimKiem = findViewById(R.id.layout_tim_kiem)
        (findViewById<View>(R.id.txt_nav_header_tenNV) as TextView).text = mApplication!!.userDangNhap!!.userName
        (findViewById<View>(R.id.txt_nav_header_displayname) as TextView).text = mApplication!!.userDangNhap!!.displayName
        optionSearchFeature()

    }

    fun addFeature() {
        val intentAdd = Intent(this@MainActivity, AddFeatureActivity::class.java)
        startActivityForResult(intentAdd, Constant.RequestCode.ADD)
    }

    fun handlingAddFeatureSuccess() {
        handlingCancelAdd()
        mapViewHandler!!.query(String.format(Constant.QUERY_BY_OBJECTID, mApplication!!.diemSuCo.objectID))

        mApplication!!.diemSuCo.clear()
    }

    fun handlingCancelAdd() {
        if (mPopUp!!.callout != null && mPopUp!!.callout!!.isShowing) {
            mPopUp!!.callout!!.dismiss()
        }
        mGraphicsOverlay!!.graphics.clear()
    }


    private fun addGraphicsAddFeature(vararg e: MotionEvent) {
        val center: Point
        if (e.size == 0)
            center = mMapView!!.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
        else {
            center = mMapView!!.screenToLocation(android.graphics.Point(Math.round(e[0].x), Math.round(e[0].y)))
            mMapView!!.setViewpointCenterAsync(center)
        }
        val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.YELLOW, 20f)
        val graphic = Graphic(center, symbol)
        mGraphicsOverlay!!.graphics.clear()
        mGraphicsOverlay!!.graphics.add(graphic)
        mPopUp!!.showPopupAdd(center, isChangingGeometry)
        mPointFindLocation = center
    }

    fun findRoute() {
        val uri = String.format("google.navigation:q=%s", Uri.encode(mApplication!!.selectedArcGISFeature!!.attributes[Constant.FieldSuCo.DIA_CHI].toString()))
        val gmmIntentUri = Uri.parse(uri)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handlingMapViewDoneLoading() {
        mLocationDisplay = mMapView!!.locationDisplay
        mLocationDisplay!!.startAsync()

        //        loginWithPortal1();
        setServices()
    }

    private fun setServices() {
        try {
            // config feature layer service
            mDFeatureLayers = ArrayList()
            for (dLayerInfo in mApplication!!.layerInfos!!) {
                if (dLayerInfo.id.substring(dLayerInfo.id.length - 3) == "TBL" || !dLayerInfo.isView)
                    continue
                var url: String? = dLayerInfo.url
                if (!dLayerInfo.url.startsWith("http"))
                    url = "http:" + dLayerInfo.url
                if (url == null)
                    continue
                if (dLayerInfo.id == getString(R.string.IDLayer_Basemap) && hanhChinhImageLayers == null) {
                    hanhChinhImageLayers = ArcGISMapImageLayer(url)
                    hanhChinhImageLayers!!.id = dLayerInfo.id
                    mMapView!!.map.operationalLayers.add(hanhChinhImageLayers)
                    hanhChinhImageLayers!!.addDoneLoadingListener {
                        if (hanhChinhImageLayers!!.loadStatus == LoadStatus.LOADED) {
                            val sublayerList = hanhChinhImageLayers!!.sublayers
                            for (sublayer in sublayerList) {
                                addCheckBox(sublayer as ArcGISMapImageSublayer, states, colors, true)

                            }

                        }
                    }
                    hanhChinhImageLayers!!.loadAsync()
                } else if (dLayerInfo.id == getString(R.string.IDLayer_DiemSuCo)) {
                    val serviceFeatureTable = ServiceFeatureTable(url)
                    mFeatureLayer = FeatureLayer(serviceFeatureTable)
                    if (dLayerInfo.definition.toLowerCase() == "null") {
                        mFeatureLayer!!.definitionExpression = Constant.DEFINITION_HIDE_COMPLETE
                    } else
                        mFeatureLayer!!.definitionExpression = dLayerInfo.definition + " and " + Constant.DEFINITION_HIDE_COMPLETE
                    mFeatureLayer!!.id = dLayerInfo.id
                    mFeatureLayer!!.name = dLayerInfo.titleLayer
                    mFeatureLayer!!.id = dLayerInfo.id
                    mFeatureLayer!!.isPopupEnabled = true
                    mFeatureLayer!!.minScale = 0.0
                    mFeatureLayer!!.addDoneLoadingListener {
                        setRendererSuCoFeatureLayer(mFeatureLayer!!)
                        mDFeatureLayer = DFeatureLayer(serviceFeatureTable, mFeatureLayer!!, dLayerInfo)
                        mApplication!!.dFeatureLayer = mDFeatureLayer
                        mDFeatureLayers!!.add(mDFeatureLayer!!)
                        val callout = mMapView!!.callout
                        mPopUp = mGeocoder?.let { Popup(this@MainActivity, mMapView!!, serviceFeatureTable, callout) }


                        DFeatureLayerDiemSuCo = mDFeatureLayer as DFeatureLayer

                        mapViewHandler = mGeocoder?.let {
                            MapViewHandler(this, mDFeatureLayer!!, callout, mMapView!!, mPopUp!!,
                                    this@MainActivity, it)
                        }
                        mapViewHandler!!.setFeatureLayerDTGs(mDFeatureLayers as ArrayList<DFeatureLayer>)

                    }
                    mMapView!!.map.operationalLayers.add(mFeatureLayer)

                } else if (dLayerInfo.id != "diemdanhgiaLYR" && taiSanImageLayers == null) {

                    taiSanImageLayers = ArcGISMapImageLayer(url.replaceFirst("FeatureServer(.*)".toRegex(), "MapServer"))
                    taiSanImageLayers!!.name = dLayerInfo.titleLayer
                    taiSanImageLayers!!.id = dLayerInfo.id
                    mMapView!!.map.operationalLayers.add(taiSanImageLayers)
                    taiSanImageLayers!!.addDoneLoadingListener {
                        if (taiSanImageLayers!!.loadStatus == LoadStatus.LOADED) {

                            val sublayerList = taiSanImageLayers!!.sublayers
                            for (sublayer in sublayerList) {
                                if (sublayer.id == 13L) {
                                    sublayer.isVisible = false
                                } else
                                    addCheckBox(sublayer as ArcGISMapImageSublayer, states, colors, false)
                            }

                        }
                    }
                    taiSanImageLayers!!.loadAsync()
                }

            }
        } catch (e: Exception) {
            Log.e("error", e.toString())
        }

        //        mMapViewHandler.setFeatureLayerDTGs(mDFeatureLayers);
    }

    private fun addCheckBox(layer: ArcGISMapImageSublayer, states: Array<IntArray>?, colors: IntArray?, isAdministrator: Boolean) {
        @SuppressLint("InflateParams") val layoutFeature = layoutInflater.inflate(R.layout.layout_feature, null) as LinearLayout
        val checkBox = layoutFeature.findViewById<CheckBox>(R.id.ckb_layout_feature)
        val textView = layoutFeature.findViewById<TextView>(R.id.txt_layout_feature)
        textView.setTextColor(resources.getColor(android.R.color.black))
        textView.text = layer.name
        checkBox.isChecked = false
        layer.isVisible = false
        CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList(states, colors))
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->

            if (buttonView.isChecked) {
                if (textView.text == layer.name)
                    layer.isVisible = true


            } else {
                if (textView.text == layer.name)
                    layer.isVisible = false
            }
        }
        if (!mListLayerID!!.contains(layer.name)) {
            if (isAdministrator)
                mLayoutDisplayLayerAdministration!!.addView(layoutFeature)
            else
                mLayoutDisplayLayerThematic!!.addView(layoutFeature)
            mListLayerID!!.add(layer.name)
        }
    }


    private fun setLicense() {
        //way 1
        ArcGISRuntimeEnvironment.setLicense(getString(R.string.license))
        //way 2
        //        UserCredential credential = new UserCredential("thanle95", "Gemini111");
        //
        //// replace the URL with either the ArcGIS Online URL or your portal URL
        //        final Portal portal = new Portal("https://than-le.maps.arcgis.com");
        //        portal.setCredential(credential);
        //
        //// load portal and listen to done loading event
        //        portal.loadAsync();
        //        portal.addDoneLoadingListener(new Runnable() {
        //            @Override
        //            public void run() {
        //                LicenseInfo licenseInfo = portal.getPortalInfo().getLicenseInfo();
        //                // Apply the license at Standard level
        //                ArcGISRuntimeEnvironment.setLicense(licenseInfo);
        //            }
        //        });
    }

    private fun setRendererSuCoFeatureLayer(mSuCoTanHoaLayer: FeatureLayer) {
        val uniqueValueRenderer = UniqueValueRenderer()
        uniqueValueRenderer.fieldNames.add(Constant.FieldSuCo.TRANG_THAI)
        uniqueValueRenderer.fieldNames.add(Constant.FieldSuCo.THONG_TIN_PHAN_ANH)
        val chuaXuLyDacBiet = PictureMarkerSymbol(Constant.URLImage.CHUA_SUA_CHUA_BAT_THUONG)
        chuaXuLyDacBiet.height = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        chuaXuLyDacBiet.width = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        val chuaXuLyBinhThuong = PictureMarkerSymbol(Constant.URLImage.CHUA_SUA_CHUA)
        chuaXuLyBinhThuong.height = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        chuaXuLyBinhThuong.width = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        val hoanThanh = PictureMarkerSymbol(Constant.URLImage.HOAN_THANH)
        chuaXuLyBinhThuong.height = resources.getInteger(R.integer.size_feature_renderer).toFloat()
        chuaXuLyBinhThuong.width = resources.getInteger(R.integer.size_feature_renderer).toFloat()

        uniqueValueRenderer.defaultSymbol = chuaXuLyBinhThuong
        uniqueValueRenderer.defaultLabel = "Chưa xác định"

        val dacBietValue1 = ArrayList<Any>()
        dacBietValue1.add(Constant.TrangThaiSuCo.CHUA_XU_LY)
        dacBietValue1.add(Constant.ThongTinPhanAnh.KHONG_NUOC)
        val dacBietValue2 = ArrayList<Any>()
        dacBietValue2.add(Constant.TrangThaiSuCo.CHUA_XU_LY)
        dacBietValue2.add(Constant.ThongTinPhanAnh.XI_DHN)
        val dacBietValue3 = ArrayList<Any>()
        dacBietValue3.add(Constant.TrangThaiSuCo.CHUA_XU_LY)
        dacBietValue3.add(Constant.ThongTinPhanAnh.ONG_BE)

        val binhThuongValue = ArrayList<Any>()
        binhThuongValue.add(Constant.TrangThaiSuCo.CHUA_XU_LY)

        val hoanThanhValueNull = ArrayList<Any?>()
        hoanThanhValueNull.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValueNull.add(null)
        val hoanThanhValue0 = ArrayList<Any>()
        hoanThanhValue0.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue0.add(Constant.ThongTinPhanAnh.KHAC)
        val hoanThanhValue1 = ArrayList<Any>()
        hoanThanhValue1.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue1.add(Constant.ThongTinPhanAnh.KHONG_NUOC)
        val hoanThanhValue2 = ArrayList<Any>()
        hoanThanhValue2.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue2.add(Constant.ThongTinPhanAnh.NUOC_DUC)
        val hoanThanhValue3 = ArrayList<Any>()
        hoanThanhValue3.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue3.add(Constant.ThongTinPhanAnh.NUOC_YEU)
        val hoanThanhValue4 = ArrayList<Any>()
        hoanThanhValue4.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue4.add(Constant.ThongTinPhanAnh.XI_DHN)
        val hoanThanhValue5 = ArrayList<Any>()
        hoanThanhValue5.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue5.add(Constant.ThongTinPhanAnh.HU_VAN)
        val hoanThanhValue6 = ArrayList<Any>()
        hoanThanhValue6.add(Constant.TrangThaiSuCo.HOAN_THANH)
        hoanThanhValue6.add(Constant.ThongTinPhanAnh.ONG_BE)

        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Chưa xử lý", "Chưa xử lý", chuaXuLyDacBiet, dacBietValue1))
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Chưa xử lý", "Chưa xử lý", chuaXuLyDacBiet, dacBietValue2))
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Chưa xử lý", "Chưa xử lý", chuaXuLyDacBiet, dacBietValue3))

        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Chưa xử lý", "Chưa xử lý", chuaXuLyBinhThuong, binhThuongValue))

        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValueNull))
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue0))
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue1))
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue2))
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue3))
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue4))
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue5))
        uniqueValueRenderer.uniqueValues.add(UniqueValueRenderer.UniqueValue(
                "Hoàn thành", "Hoàn thành", hoanThanh, hoanThanhValue6))
        mSuCoTanHoaLayer.renderer = uniqueValueRenderer
        mSuCoTanHoaLayer.loadAsync()
    }

    private fun setViewPointCenter(position: Point) {
        if (mPopUp == null) {
            mMapView?.let { MySnackBar.make(it, getString(R.string.message_unloaded_map), true) }
        } else {
            val geometry = GeometryEngine.project(position, SpatialReferences.getWebMercator())
            val booleanListenableFuture = mMapView!!.setViewpointCenterAsync(geometry.extent.center)
            booleanListenableFuture.addDoneListener {
                try {
                    if (booleanListenableFuture.get()) {
                        this@MainActivity.mPointFindLocation = position
                    }
                    //                    mPopUp.showPopupFindLocation(position);
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }


            }
        }

    }

    private fun setViewPointCenterLongLat(position: Point, location: String?) {
        if (mPopUp == null) {
            mMapView?.let { MySnackBar.make(it, getString(R.string.message_unloaded_map), true) }
        } else {
            val geometry = GeometryEngine.project(position, SpatialReferences.getWgs84())
            val geometry1 = GeometryEngine.project(geometry, SpatialReferences.getWebMercator())
            val point = geometry1.extent.center

            val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.RED, 20f)
            val graphic = Graphic(point, symbol)
            mGraphicsOverlay!!.graphics.add(graphic)

            mMapView!!.setViewpointCenterAsync(point, resources.getInteger(R.integer.SCALE_IMAGE_WITH_LABLES).toDouble())
            //            mPopUp.showPopupFindLocation(point, location);
            this.mPointFindLocation = point
        }

    }


    private fun optionSearchFeature() {
        this.mIsSearchingFeature = true
        mLayoutTimSuCo!!.setBackgroundResource(R.drawable.layout_border_bottom)
        mLayoutTimDiaChi!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
    }

    private fun optionFindRoute() {
        this.mIsSearchingFeature = false
        mLayoutTimDiaChi!!.setBackgroundResource(R.drawable.layout_border_bottom)
        mLayoutTimSuCo!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
    }

    private fun deleteSearching() {
        mGraphicsOverlay!!.graphics.clear()
        mLLayoutSearch!!.removeAllViews()
    }


    private fun visibleFloatActionButton() {
        if (mFloatButtonLayer!!.visibility == View.VISIBLE) {
            mFloatButtonLayer!!.hide()
            mFloatButtonLocation!!.hide()
        } else {
            mFloatButtonLayer!!.show()
            mFloatButtonLocation!!.show()
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        }  //            super.onBackPressed();

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.quan_ly_su_co, menu)
        mTxtSearchView = menu.findItem(R.id.action_search).actionView as SearchView
        mTxtSearchView!!.queryHint = getString(R.string.title_search)
        mTxtSearchView!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            @RequiresApi(api = Build.VERSION_CODES.N)
            override fun onQueryTextSubmit(query: String): Boolean {
                try {
                    mLLayoutSearch!!.removeAllViews()
                    if (mIsSearchingFeature && mapViewHandler != null) {
                        mPopUp!!.callout!!.dismiss()

                        mFeatureLayer!!.clearSelection()
                        val queryParameters = QueryParameters()
                        val builder = StringBuilder()
                        for (field in mFeatureLayer!!.featureTable.fields) {
                            when (field.fieldType) {
                                Field.Type.OID, Field.Type.INTEGER, Field.Type.SHORT -> try {
                                    val search = Integer.parseInt(query)
                                    builder.append(String.format("%s = %s", field.name, search))
                                    builder.append(" or ")
                                } catch (ignored: Exception) {

                                }

                                Field.Type.FLOAT, Field.Type.DOUBLE -> try {
                                    val search = java.lang.Double.parseDouble(query)
                                    builder.append(String.format("%s = %s", field.name, search))
                                    builder.append(" or ")
                                } catch (ignored: Exception) {

                                }

                                Field.Type.TEXT -> {
                                    builder.append(field.name).append(" like N'%").append(query).append("%'")
                                    builder.append(" or ")
                                }
                            }
                        }
                        builder.append(" 1 = 2 ")
                        queryParameters.whereClause = builder.toString()

                        QueryServiceFeatureTableGetListAsync(this@MainActivity,object: QueryServiceFeatureTableGetListAsync.AsyncResponse{
                            override fun processFinish(output: List<Feature>?) {
                                if (output != null && output.size > 0) {
                                    val items = ArrayList<HandlingSearchHasDone.Item>()
                                    for (feature in output) {
                                        val attributes = feature.attributes
                                        val idSuCo = attributes[Constant.FieldSuCo.ID_SUCO]
                                        val ngayXayRa = attributes[Constant.FieldSuCo.TG_PHAN_ANH]
                                        val thongTinPhanAnhCode = attributes[Constant.FieldSuCo.THONG_TIN_PHAN_ANH]
                                        val codedValues = (feature.featureTable.getField(Constant.FieldSuCo.THONG_TIN_PHAN_ANH).domain as CodedValueDomain).codedValues
                                        val thongTinPhanAnhValue = if (thongTinPhanAnhCode == null) null else HandlingSearchHasDone.getValueDomain(codedValues, thongTinPhanAnhCode)
                                        items.add(HandlingSearchHasDone.Item(Integer.parseInt(attributes[Constant.Field.OBJECTID].toString()),
                                                idSuCo?.toString() ?: "",
                                                if (ngayXayRa != null) Constant.DateFormat.DATE_FORMAT_VIEW.format((ngayXayRa as Calendar).time) else "",
                                                if (attributes[Constant.FieldSuCo.DIA_CHI] != null) attributes[Constant.FieldSuCo.DIA_CHI].toString() else "",
                                                if (thongTinPhanAnhCode != null) java.lang.Short.parseShort(thongTinPhanAnhCode.toString()) else Constant.ThongTinPhanAnh.KHAC,
                                                thongTinPhanAnhValue?.toString() ?: ""))
                                    }
                                    val views = HandlingSearchHasDone.handleFromItems(this@MainActivity, this@MainActivity, items)
                                    for (view in views) {
                                        val txtID = view.findViewById<TextView>(R.id.txt_top)
                                        view.setOnClickListener { v ->
                                            if (mapViewHandler != null) {
                                                mapViewHandler!!.query(String.format(Constant.QUERY_BY_SUCOID, txtID.text.toString()))
                                                mLLayoutSearch!!.removeAllViews()
                                            }
                                        }
                                        mLLayoutSearch!!.addView(view)
                                    }
                                } else {

                                }
                            }
                        }).execute(queryParameters)
                    } else if (query.isNotEmpty()) {
                        deleteSearching()
                        val findLocationAsycn = FindLocationAsycn(this@MainActivity,
                                true, object: FindLocationAsycn.AsyncResponse{
                            override fun processFinish(output: List<DAddress>?) {
                                if (output != null) {
                                    if (output.size > 0) {
                                        for (address in output) {
                                            val item = HandlingSearchHasDone.Item(-1, "", "", address.location, Constant.ThongTinPhanAnh.KHAC, null)
                                            item.latitude = address.latitude
                                            item.longtitude = address.longtitude

                                            val layout = this@MainActivity.layoutInflater.inflate(R.layout.item_tracuu, null) as LinearLayout
                                            val txtThongTinPhanAnh = layout.findViewById<TextView>(R.id.txt_bottom)
                                            val txtDiaChi = layout.findViewById<TextView>(R.id.txt_bottom1)
                                            val txtID = layout.findViewById<TextView>(R.id.txt_top)
                                            val txtNgayCapNhat = layout.findViewById<TextView>(R.id.txt_right)


                                            txtID.visibility = View.GONE
                                            txtDiaChi.text = item.diaChi

                                            txtNgayCapNhat.visibility = View.GONE
                                            txtThongTinPhanAnh.visibility = View.GONE
                                            layout.setOnClickListener { v ->

                                                setViewPointCenterLongLat(Point(item.longtitude, item.latitude), item.diaChi)
                                                Log.d("Tọa độ tìm kiếm", String.format("[% ,.9f;% ,.9f]", item.longtitude, item.latitude))
                                            }
                                            mLLayoutSearch!!.addView(layout)
                                        }

                                        //                                    }
                                    }
                                }
                            }
                        })
                        findLocationAsycn.execute(query)

                    }
                } catch (e: Exception) {
                    Log.e("Lỗi tìm kiếm", e.toString())
                }

                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.trim { it <= ' ' }.length > 0 && !mIsSearchingFeature) {
                } else {
                    mLLayoutSearch!!.removeAllViews()
                    mGraphicsOverlay!!.graphics.clear()
                }
                return false
            }
        })
        menu.findItem(R.id.action_search).setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                visibleFloatActionButton()
                mLayoutTimKiem!!.visibility = View.VISIBLE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                mLayoutTimKiem!!.visibility = View.INVISIBLE
                visibleFloatActionButton()
                return true
            }
        })
        return true
    }

    private fun showHideComplete() {
        if (mApplication!!.dFeatureLayer!!.getdLayerInfo().definition.toLowerCase() == "null") {
            if (mApplication!!.dFeatureLayer!!.layer.definitionExpression.contains(Constant.DEFINITION_HIDE_COMPLETE)) {
                mApplication!!.dFeatureLayer!!.layer.definitionExpression = null
            } else {
                mApplication!!.dFeatureLayer!!.layer.definitionExpression = mApplication!!.dFeatureLayer!!.getdLayerInfo().definition + " and " + Constant.DEFINITION_HIDE_COMPLETE
            }
        } else {
            if (mApplication!!.dFeatureLayer!!.layer.definitionExpression.contains(Constant.DEFINITION_HIDE_COMPLETE)) {
                mApplication!!.dFeatureLayer!!.layer.definitionExpression = mApplication!!.dFeatureLayer!!.getdLayerInfo().definition
            } else {
                mApplication!!.dFeatureLayer!!.layer.definitionExpression = mApplication!!.dFeatureLayer!!.getdLayerInfo().definition + Constant.DEFINITION_HIDE_COMPLETE
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handleFromFeatures clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_search -> {
            }
            else -> return super.onOptionsItemSelected(item)
        }

        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_thongke -> {
                val intent = Intent(this, ListTaskActivity::class.java)
                this.startActivityForResult(intent, Constant.RequestCode.LIST_TASK)
            }
            //            case R.id.nav_tracuu:
            //                intent = new Intent(this, TraCuuActivity.class);
            //                this.startActivityForResult(intent, 1);
            //                break;

            R.id.nav_reload -> if (CheckConnectInternet.isOnline(this))
                startMain()
            R.id.nav_reload_layer -> if (CheckConnectInternet.isOnline(this)) {
                if (mPopUp != null && mPopUp!!.callout != null && mPopUp!!.callout!!.isShowing)
                    mPopUp!!.callout!!.dismiss()
                mFeatureLayer!!.loadAsync()
                if (mApplication!!.dFeatureLayer!!.getdLayerInfo().definition.toLowerCase() == "null") {
                    mFeatureLayer!!.definitionExpression = Constant.DEFINITION_HIDE_COMPLETE
                } else
                    mFeatureLayer!!.definitionExpression = mApplication!!.dFeatureLayer!!.getdLayerInfo().definition + " and " + Constant.DEFINITION_HIDE_COMPLETE
            }
            R.id.nav_show_hide_complete -> showHideComplete()
            R.id.nav_logOut -> startSignIn()
            R.id.nav_delete_searching -> deleteSearching()
            R.id.nav_visible_float_button -> visibleFloatActionButton()
            else -> {
            }
        }


        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    fun onClickTextView(v: View) {
        when (v.id) {
            R.id.txt_quanlysuco_hanhchinh ->

                if (mLayoutDisplayLayerAdministration!!.visibility == View.VISIBLE) {
                    mSeekBarAdministrator!!.visibility = View.GONE
                    mLayoutDisplayLayerAdministration!!.visibility = View.GONE
                } else {
                    mSeekBarAdministrator!!.visibility = View.VISIBLE
                    mLayoutDisplayLayerAdministration!!.visibility = View.VISIBLE
                }
            R.id.txt_quanlysuco_dulieu -> if (mLayoutDisplayLayerThematic!!.visibility == View.VISIBLE) {
                mLayoutDisplayLayerThematic!!.visibility = View.GONE
                mSeekBarThematic!!.visibility = View.GONE
            } else {
                mLayoutDisplayLayerThematic!!.visibility = View.VISIBLE
                mSeekBarThematic!!.visibility = View.VISIBLE
            }
        }
    }

    fun onClickCheckBox(v: View) {
        if (v is CheckBox) {
            when (v.getId()) {
                R.id.ckb_quanlysuco_hanhchinh ->

                    for (i in 0 until mLayoutDisplayLayerAdministration!!.childCount) {
                        val view = mLayoutDisplayLayerAdministration!!.getChildAt(i)
                        if (view is LinearLayout) {
                            for (j in 0 until view.childCount) {
                                val view1 = view.getChildAt(j)
                                if (view1 is LinearLayout) {
                                    for (k in 0 until view1.childCount) {
                                        val view2 = view1.getChildAt(k)
                                        if (view2 is CheckBox) {
                                            if (v.isChecked)
                                                view2.isChecked = true
                                            else
                                                view2.isChecked = false
                                        }
                                    }
                                }
                            }
                        }
                    }
                R.id.ckb_quanlysuco_dulieu -> for (i in 0 until mLayoutDisplayLayerThematic!!.childCount) {
                    val view = mLayoutDisplayLayerThematic!!.getChildAt(i)
                    if (view is LinearLayout) {
                        for (j in 0 until view.childCount) {
                            val view1 = view.getChildAt(j)
                            if (view1 is LinearLayout) {
                                for (k in 0 until view1.childCount) {
                                    val view2 = view1.getChildAt(k)
                                    if (view2 is CheckBox) {
                                        if (v.isChecked)
                                            view2.isChecked = true
                                        else
                                            view2.isChecked = false
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handlingLocation() {
        if (mIsFirstLocating) {
            mIsFirstLocating = false
            mLocationDisplay!!.stop()
            enableLocation()
        } else {
            if (mLocationDisplay!!.isStarted) {
                disableLocation()
            } else if (!mLocationDisplay!!.isStarted) {
                enableLocation()
            }
        }
    }

    private fun disableLocation() {
        mLocationDisplay!!.stop()
    }

    private fun enableLocation() {
        mLocationDisplay!!.autoPanMode = LocationDisplay.AutoPanMode.RECENTER
        mLocationDisplay!!.startAsync()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.layout_tim_su_co -> optionSearchFeature()
            R.id.layout_tim_dia_chi -> optionFindRoute()
            R.id.floatBtnLayer -> {
                v.visibility = View.INVISIBLE
                findViewById<View>(R.id.layout_layer).visibility = View.VISIBLE
            }
            R.id.layout_layer_open_street_map -> {
                mMapView!!.map.maxScale = 1128.497175
                mMapView!!.map.basemap = Basemap.createOpenStreetMap()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_open_street_map)
                mMapView!!.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)
            }
            R.id.layout_layer_street_map -> {
                mMapView!!.map.maxScale = 1128.497176
                mMapView!!.map.basemap = Basemap.createStreets()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_street_map)
            }
            R.id.layout_layer_image -> {
                mMapView!!.map.maxScale = resources.getInteger(R.integer.MAX_SCALE_IMAGE_WITH_LABLES).toDouble()
                mMapView!!.map.basemap = Basemap.createImageryWithLabels()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_image)
            }
            R.id.btn_layer_close -> {
                findViewById<View>(R.id.layout_layer).visibility = View.INVISIBLE
                findViewById<View>(R.id.floatBtnLayer).visibility = View.VISIBLE
            }
            R.id.img_chonvitri_themdiemsuco -> {
            }
            R.id.btn_add_feature_close -> if (mapViewHandler != null) {
                findViewById<View>(R.id.linear_addfeature).visibility = View.GONE
                findViewById<View>(R.id.img_map_pin).visibility = View.GONE
                mapViewHandler!!.setClickBtnAdd(false)
            }
            R.id.floatBtnLocation -> handlingLocation()
            R.id.imgBtn_timkiemdiachi_themdiemsuco -> {
            }
        }//                mCurrentPoint = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).getTargetGeometry().getExtent().getCenter();
        //                themDiemSuCo();
        //                themDiemSuCo();
    }

    private fun getBitmap(path: String): Bitmap? {

        val uri = Uri.fromFile(File(path))
        var `in`: InputStream?
        try {
            val IMAGE_MAX_SIZE = 1200000 // 1.2MP
            `in` = contentResolver.openInputStream(uri)

            // Decode image size
            var o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`in`, null, o)
            assert(`in` != null)
            `in`!!.close()


            var scale = 1
            while (o.outWidth * o.outHeight * (1 / Math.pow(scale.toDouble(), 2.0)) > IMAGE_MAX_SIZE) {
                scale++
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight)

            var b: Bitmap?
            `in` = contentResolver.openInputStream(uri)
            if (scale > 1) {
                scale--
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = BitmapFactory.Options()
                o.inSampleSize = scale
                b = BitmapFactory.decodeStream(`in`, null, o)

                // resize to desired dimensions
                val height = b!!.height
                val width = b.width
                Log.d("", "1th scale operation dimenions - width: $width, height: $height")

                val y = Math.sqrt(IMAGE_MAX_SIZE / (width.toDouble() / height))
                val x = y / height * width

                val scaledBitmap = Bitmap.createScaledBitmap(b, x.toInt(), y.toInt(), true)
                b.recycle()
                b = scaledBitmap

                System.gc()
            } else {
                b = BitmapFactory.decodeStream(`in`)
            }
            assert(`in` != null)
            `in`!!.close()

            Log.d("", "bitmap size - width: " + b!!.width + ", height: " + b.height)
            return b
        } catch (e: IOException) {
            Log.e("", e.message, e)
            return null
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun handlingListTaskActivityResult() {
        //query sự cố theo idsuco, lấy objectid
        val selectedIDSuCo = mApplication!!.diemSuCo.idSuCo
        mapViewHandler!!.query(String.format("%s = '%s'", Constant.FieldSuCo.ID_SUCO, selectedIDSuCo))
    }

    @SuppressLint("ResourceAsColor")
    private fun handlingColorBackgroundLayerSelected(id: Int) {
        when (id) {
            R.id.layout_layer_open_street_map -> {
                mImageOpenStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap)
                mTxtOpenStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                mImageStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
                mImageImageWithLabel!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtImageWithLabel!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
            }
            R.id.layout_layer_street_map -> {
                mImageStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap)
                mTxtStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                mImageOpenStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtOpenStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
                mImageImageWithLabel!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtImageWithLabel!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
            }
            R.id.layout_layer_image -> {
                mImageImageWithLabel!!.setBackgroundResource(R.drawable.layout_shape_basemap)
                mTxtImageWithLabel!!.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                mImageOpenStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtOpenStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
                mImageStreetMap!!.setBackgroundResource(R.drawable.layout_shape_basemap_none)
                mTxtStreetMap!!.setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            val objectid = data!!.getIntExtra(getString(R.string.ket_qua_objectid), 1)
            when (requestCode) {
                1 -> if (resultCode == Activity.RESULT_OK && mapViewHandler != null) {
                    mapViewHandler!!.query(String.format(Constant.QUERY_BY_OBJECTID, objectid))
                }
                Constant.RequestCode.LOGIN -> if (resultCode == Activity.RESULT_OK)
                    requestPermisson()
                Constant.RequestCode.LIST_TASK -> if (resultCode == Activity.RESULT_OK)
                    handlingListTaskActivityResult()
                Constant.RequestCode.ADD -> if (resultCode == Activity.RESULT_OK) {
                    handlingAddFeatureSuccess()
                } else {
                    handlingCancelAdd()
                }
                Constant.RequestCode.UPDATE -> mPopUp!!.refreshPopup(mApplication!!.selectedArcGISFeature!!)
                else -> {
                }
            }
        } catch (ignored: Exception) {
        }

    }

    companion object {
        lateinit var DFeatureLayerDiemSuCo: DFeatureLayer
    }
}