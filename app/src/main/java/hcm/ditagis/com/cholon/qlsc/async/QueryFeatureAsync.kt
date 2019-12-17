package hcm.ditagis.com.cholon.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import java.text.ParseException
import java.util.*
import java.util.concurrent.ExecutionException


/**
 * Created by ThanLe on 4/16/2018.
 */

class QueryFeatureAsync(activity: Activity, private val mTrangThai: Int, private val mDiaChi: String, thoiGianPhanAnh: String, @field:SuppressLint("StaticFieldLeak")
private val mDelegate: AsyncResponse) : AsyncTask<Void, List<Feature>, Void>() {
    private val mApplication: DApplication = activity.application as DApplication
    private val mServiceFeatureTable: ServiceFeatureTable
    private var mThoiGian: String? = null
    private var mHasTime: Boolean = false

    interface AsyncResponse {
        fun processFinish(output: List<Feature>?)
    }

    init {
        this.mServiceFeatureTable = mApplication.dFeatureLayer!!.serviceFeatureTable
        this.mThoiGian = thoiGianPhanAnh
        try {
            val date = Constant.DateFormat.DATE_FORMAT.parse(thoiGianPhanAnh)
            this.mThoiGian = formatTimeToGMT(date)
            this.mHasTime = true
        } catch (e: ParseException) {
            this.mHasTime = false
            e.printStackTrace()
        }

    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    private fun formatTimeToGMT(date: Date): String {
        val dateFormatGmt = Constant.DateFormat.DATE_FORMAT_YEAR_FIRST
        dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
        return dateFormatGmt.format(date)
    }

    @SuppressLint("DefaultLocale")
    override fun doInBackground(vararg aVoids: Void): Void? {
        try {

            val queryParameters = QueryParameters()
            @SuppressLint("DefaultLocale") val queryClause = StringBuilder(String.format(" %s like N'%%%s%%'",
                    Constant.FieldSuCo.DIA_CHI, mDiaChi))
            if (mHasTime)
                queryClause.append(String.format(" and %s > date '%s'", Constant.FieldSuCo.TG_PHAN_ANH, mThoiGian))
            if (mTrangThai != -1) {
                queryClause.append(String.format(" and %s = %d",
                        Constant.FieldSuCo.TRANG_THAI, mTrangThai))
            }
            queryParameters.whereClause = queryClause.toString()

            val featureQueryResultListenableFuture = mServiceFeatureTable.queryFeaturesAsync(queryParameters,
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
        if (values.isNotEmpty()) mDelegate.processFinish(values[0])
    }

}