package hcm.ditagis.com.cholon.qlsc.utities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.location.Geocoder
import android.view.MotionEvent
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.MapView
import hcm.ditagis.com.cholon.qlsc.MainActivity
import hcm.ditagis.com.cholon.qlsc.async.QueryServiceFeatureTableAsync
import hcm.ditagis.com.cholon.qlsc.async.SingleTapMapViewAsync
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.DFeatureLayer
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 2/2/2018.
 */

@SuppressLint("Registered")
class MapViewHandler(private val mActivity: MainActivity, DFeatureLayer: DFeatureLayer, private val mCallout: Callout, private val mMapView: MapView,
                     private val mPopUp: Popup, private val mContext: Context, private val mGeocoder: Geocoder) : Activity() {
    private val suCoTanHoaLayer: FeatureLayer?
    private var mClickPoint: android.graphics.Point? = null
    private val mSelectedArcGISFeature: ArcGISFeature? = null
    private var isClickBtnAdd = false
    private val mServiceFeatureTable: ServiceFeatureTable
    private val mApplication: DApplication

    private var mDFeatureLayers: List<DFeatureLayer>? = null

    fun setFeatureLayerDTGs(mDFeatureLayers: List<DFeatureLayer>) {
        this.mDFeatureLayers = mDFeatureLayers
    }

    init {
        mApplication = mActivity.application as DApplication
        this.mServiceFeatureTable = DFeatureLayer.layer.featureTable as ServiceFeatureTable
        this.suCoTanHoaLayer = DFeatureLayer.layer
    }

    fun setClickBtnAdd(clickBtnAdd: Boolean) {
        isClickBtnAdd = clickBtnAdd
    }


    fun onScroll(e2: MotionEvent): DoubleArray {
        val center = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
        val project = GeometryEngine.project(center, SpatialReferences.getWgs84())
        val location = doubleArrayOf(project.extent.center.x, project.extent.center.y)
        mClickPoint = android.graphics.Point(e2.x.toInt(), e2.y.toInt())
        //        Geometry geometry = GeometryEngine.project(project, SpatialReferences.getWebMercator());
        return location
    }

    fun onSingleTapMapView(e: MotionEvent) {
        val clickPoint = mMapView.screenToLocation(android.graphics.Point(Math.round(e.x), Math.round(e.y)))
        mClickPoint = android.graphics.Point(e.x.toInt(), e.y.toInt())
        if (isClickBtnAdd) {
            mMapView.setViewpointCenterAsync(clickPoint, 10.0)
        } else {

            val singleTapMapViewAsync = mDFeatureLayers?.let { SingleTapMapViewAsync(mActivity, it, mPopUp, mClickPoint!!, mMapView) }
            singleTapMapViewAsync?.execute(clickPoint)
        }
    }


    fun query(query: String) {

        val queryParameters = QueryParameters()
        queryParameters.whereClause = query
        val feature: ListenableFuture<FeatureQueryResult>
        feature = mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        feature.addDoneListener {
            try {
                val result = feature.get()
                if (result.iterator().hasNext()) {
                    val item = result.iterator().next()
                    if (item != null) {
                        if (item.geometry != null) {
                            val extent = item.geometry.extent
                            mApplication.geometry = item.geometry
                            mMapView.setViewpointGeometryAsync(extent)
                        }
                        if (suCoTanHoaLayer != null) {
                            suCoTanHoaLayer.selectFeature(item)
                            val queryClause = String.format("%s = '%s' ",
                                    Constant.FieldSuCo.ID_SUCO, item.attributes[Constant.FieldSuCo.ID_SUCO].toString())
                            val queryParameters1 = QueryParameters()
                            queryParameters1.whereClause = queryClause
                            QueryServiceFeatureTableAsync(mActivity,object: QueryServiceFeatureTableAsync.AsyncResponse{
                                override fun processFinish(output: Feature?) {
                                    if (output != null) {
                                        mApplication.selectedArcGISFeature = output as ArcGISFeature
                                        mPopUp.showPopup(false)
                                    }
                                }
                            }).execute(queryParameters1)

                        }
                    }
                }

            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
    }
}