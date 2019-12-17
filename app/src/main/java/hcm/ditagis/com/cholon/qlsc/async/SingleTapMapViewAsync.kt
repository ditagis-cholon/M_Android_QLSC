package hcm.ditagis.com.cholon.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.DialogInterface
import android.os.AsyncTask
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.ArcGISFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import hcm.ditagis.com.cholon.qlsc.MainActivity
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.DFeatureLayer
import hcm.ditagis.com.cholon.qlsc.utities.Popup
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */

class SingleTapMapViewAsync(activity: MainActivity, private val mDFeatureLayers: List<DFeatureLayer>, @field:SuppressLint("StaticFieldLeak")
private val mPopUp: Popup,
                            private val mClickPoint: android.graphics.Point, @field:SuppressLint("StaticFieldLeak")
                            private val mMapView: MapView) : AsyncTask<Point, DFeatureLayer, Void>() {
    private val mDialog: ProgressDialog?
    @SuppressLint("StaticFieldLeak")
    private val mActivity: Activity
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var isFound = false
    private val mApplication: DApplication

    init {
        this.mActivity = activity
        this.mApplication = activity.application as DApplication
        this.mDialog = ProgressDialog(activity, android.R.style.Theme_Material_Dialog_Alert)
    }

    @SuppressLint("WrongThread")
    override fun doInBackground(vararg points: Point): Void? {
        val listListenableFuture = mMapView
                .identifyLayersAsync(mClickPoint, 5.0, false)
        listListenableFuture.addDoneListener {
            val identifyLayerResults: List<IdentifyLayerResult>
            try {
                identifyLayerResults = listListenableFuture.get()
                for (identifyLayerResult in identifyLayerResults) {
                    run {
                        val elements = identifyLayerResult.elements
                        if (elements.size > 0 && elements[0] is ArcGISFeature && !isFound) {
                            isFound = true
                            mSelectedArcGISFeature = elements[0] as ArcGISFeature
                            val serviceLayerId = mSelectedArcGISFeature!!.featureTable.serviceLayerId
                            val DFeatureLayer = getmFeatureLayerDTG(serviceLayerId)
                            publishProgress(DFeatureLayer)
                        }
                    }
                }
                publishProgress()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun getmFeatureLayerDTG(serviceLayerId: Long): DFeatureLayer? {
        for (DFeatureLayer in mDFeatureLayers) {
            val serviceLayerDTGId = (DFeatureLayer.layer.featureTable as ArcGISFeatureTable).serviceLayerId
            if (serviceLayerDTGId == serviceLayerId) return DFeatureLayer
        }
        return null
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog!!.setMessage("Đang xử lý...")
        mDialog.setCancelable(false)
        mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Hủy") { dialogInterface, i -> publishProgress() }
        mDialog.show()
    }

    override fun onProgressUpdate(vararg values: DFeatureLayer) {
        super.onProgressUpdate(*values)
        if (values.isNotEmpty() && mSelectedArcGISFeature != null) {
            //            HoSoVatTuSuCoAsync hoSoVatTuSuCoAsync = new HoSoVatTuSuCoAsync(mActivity, object -> {
            //                if (object != null) {
            mApplication.selectedArcGISFeature = mSelectedArcGISFeature
            mPopUp.showPopup(false)
        }
        if (mDialog != null && mDialog.isShowing) {
            mDialog.dismiss()
        }
        //            });
        //            hoSoVatTuSuCoAsync.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Constant.HOSOVATTUSUCO_METHOD.FIND, mSelectedArcGISFeature.getAttributes()
        //                    .get(mActivity.getString(R.string.Field_SuCo_IDSuCo)));

    }



}