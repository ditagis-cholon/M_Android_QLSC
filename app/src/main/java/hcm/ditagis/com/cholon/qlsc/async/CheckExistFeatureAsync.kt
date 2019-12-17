package hcm.ditagis.com.cholon.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult
import com.esri.arcgisruntime.mapping.view.MapView
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import java.util.*
import java.util.concurrent.ExecutionException


/**
 * Created by ThanLe on 4/16/2018.
 */

class CheckExistFeatureAsync(@field:SuppressLint("StaticFieldLeak")
                             private val mActivity: Activity, private val mMapView: MapView,
                             @field:SuppressLint("StaticFieldLeak")
                             private val mDelegate: AsyncResponse) : AsyncTask<Void, String, Void>() {
    private val mApplication: DApplication

    interface AsyncResponse {
        fun processFinish(output: String?)
    }

    init {
        this.mApplication = mActivity.application as DApplication
    }



    @SuppressLint("WrongThread")
    override fun doInBackground(vararg params: Void): Void? {
        //Kiểm tra vị trí hiện tại đã có điểm sự cố hay chưa
        val listListenableFuture = mMapView
                .identifyLayersAsync(mMapView.locationToScreen(mApplication.addFeaturePoint!!), 5.0, false)
        listListenableFuture.addDoneListener {
            val identifyLayerResults: List<IdentifyLayerResult>
            try {
                identifyLayerResults = listListenableFuture.get()
                if (identifyLayerResults.isNotEmpty())
                    for (identifyLayerResult in identifyLayerResults) {
                        run {
                            val elements = identifyLayerResult.elements
                            if (elements.size > 0 && elements[0] is ArcGISFeature) {
                                //Nếu có điểm sự cố, kiểm tra ngày phản ánh có phải là hôm nay hay không
                                val feature = elements[0] as ArcGISFeature
                                val ngayPhanAnh = feature.attributes[Constant.FieldSuCo.TG_PHAN_ANH]

                                if (ngayPhanAnh != null) {
                                    val c1 = ngayPhanAnh as Calendar?
                                    val c2 = Calendar.getInstance()
                                    if (c1!!.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) && c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR))
                                        publishProgress(feature.attributes[Constant.FieldSuCo.ID_SUCO].toString())
                                    else
                                        publishProgress()
                                } else
                                    publishProgress()
                            } else
                                publishProgress()
                        }
                    }
                else
                    publishProgress()
            } catch (e: InterruptedException) {
                e.printStackTrace()
                publishProgress()
            } catch (e: ExecutionException) {
                e.printStackTrace()
                publishProgress()
            }
        }
        return null
    }


    override fun onProgressUpdate(vararg values: String) {
        if (values.isEmpty()) {
            this.mDelegate.processFinish(null)
        } else if (values.isNotEmpty()) this.mDelegate.processFinish(values[0])
    }


}