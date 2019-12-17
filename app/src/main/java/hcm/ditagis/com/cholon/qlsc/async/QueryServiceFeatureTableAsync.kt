package hcm.ditagis.com.cholon.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import java.util.concurrent.ExecutionException


/**
 * Created by ThanLe on 4/16/2018.
 */

class QueryServiceFeatureTableAsync(@field:SuppressLint("StaticFieldLeak")
                                    private val mActivity: Activity, @field:SuppressLint("StaticFieldLeak")
                                    private val mDelegate: AsyncResponse) : AsyncTask<QueryParameters, Feature, Void>() {
    private val mApplication: DApplication = mActivity.application as DApplication
    private val mServiceFeatureTable: ServiceFeatureTable

    interface AsyncResponse {
        fun processFinish(output: Feature?)
    }

    init {
        this.mServiceFeatureTable = mApplication.dFeatureLayer!!.layer.featureTable as ServiceFeatureTable
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun doInBackground(vararg params: QueryParameters): Void? {
        try {
            if (params != null && params.size > 0) {


                val featureQueryResultListenableFuture = mServiceFeatureTable.queryFeaturesAsync(params[0], ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                featureQueryResultListenableFuture.addDoneListener {
                    try {
                        val result = featureQueryResultListenableFuture.get()
                        val iterator = result.iterator()

                        if (iterator.hasNext()) {
                            val feature = iterator.next() as Feature
                            publishProgress(feature)
                        } else {
                            publishProgress()
                        }

                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        publishProgress()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                        publishProgress()
                    }
                }
            } else
                publishProgress()
        } catch (e: Exception) {
            publishProgress()
        }

        return null
    }

    override fun onProgressUpdate(vararg values: Feature) {
        if (values.isNotEmpty())
            mDelegate.processFinish(values[0])
        else
            mDelegate.processFinish(null)
    }




}