package hcm.ditagis.com.cholon.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.os.AsyncTask
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.cholon.qlsc.R
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import java.util.*
import java.util.concurrent.ExecutionException


/**
 * Created by ThanLe on 4/16/2018.
 */

class QueryServiceFeatureTableGetListAsync(@field:SuppressLint("StaticFieldLeak")
                                           private val mActivity: Activity, @field:SuppressLint("StaticFieldLeak")
                                           private val mDelegate: AsyncResponse) : AsyncTask<QueryParameters, List<Feature>, Void>() {
    private val mApplication: DApplication = mActivity.application as DApplication
    private val mServiceFeatureTable: ServiceFeatureTable
    private var mDialog: AlertDialog? = null

    interface AsyncResponse {
        fun processFinish(output: List<Feature>?)
    }

    init {
        this.mServiceFeatureTable = mApplication.dFeatureLayer!!.serviceFeatureTable
    }

    override fun onPreExecute() {
        super.onPreExecute()
        val layout = mActivity.layoutInflater.inflate(R.layout.layout_progress_dialog, null) as LinearLayout
        val txtTitle = layout.findViewById<TextView>(R.id.txt_progress_dialog_title)
        val txtMessage = layout.findViewById<TextView>(R.id.txt_progress_dialog_message)
        txtTitle.text = mActivity.applicationContext.getString(R.string.message_list_task_title)
        txtMessage.text = mActivity.applicationContext.getString(R.string.message_list_task_message)
        val builder = AlertDialog.Builder(mActivity)
        builder.setCancelable(false)
        builder.setView(layout)

        mDialog = builder.create()
        mDialog!!.show()
        val window = mDialog!!.window
        if (window != null) {
            val layoutParams = WindowManager.LayoutParams()
            layoutParams.copyFrom(mDialog!!.window!!.attributes)
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            mDialog!!.window!!.attributes = layoutParams
        }
    }

    override fun doInBackground(vararg params: QueryParameters): Void? {
        if (params != null && params.size > 0)
            try {


                val featureQueryResultListenableFuture = mServiceFeatureTable.queryFeaturesAsync(params[0],
                        ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                featureQueryResultListenableFuture.addDoneListener {
                    try {
                        val result = featureQueryResultListenableFuture.get()
                        val iterator = result.iterator()
                        var item: Feature
                        val features = ArrayList<Feature>()
                        while (iterator.hasNext()) {
                            item = iterator.next()
                            features.add(item)
                        }
                        publishProgress(features)

                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                        publishProgress()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                        publishProgress()
                    }
                }
            } catch (e: Exception) {
                publishProgress()
            }

        return null
    }

    override fun onProgressUpdate(vararg values: List<Feature>) {
        if (values.isNotEmpty())
            mDelegate.processFinish(values[0])
        else
            mDelegate.processFinish(null)

        if (mDialog != null && mDialog!!.isShowing)
            mDialog!!.dismiss()
    }

}