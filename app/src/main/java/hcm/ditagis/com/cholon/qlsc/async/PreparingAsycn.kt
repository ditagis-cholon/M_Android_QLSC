package hcm.ditagis.com.cholon.qlsc.async

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.FeatureQueryResult
import hcm.ditagis.com.cholon.qlsc.R
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.entities.DLayerInfo
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class PreparingAsycn(@field:SuppressLint("StaticFieldLeak")
                     private val mContext: Context, private val mApplication: DApplication, private val mDelegate: AsyncResponse) : AsyncTask<Void, ListenableFuture<FeatureQueryResult>, List<DLayerInfo>?>() {
    private var mDialog: ProgressDialog? = null


    private val layerInfoAPI: List<DLayerInfo>?
        get() {
            try {
                val API_URL = Constant.instance.LAYER_INFO

                val url = URL(API_URL)
                val conn = url.openConnection() as HttpURLConnection
                try {
                    conn.doOutput = false

                    conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                    conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, mApplication.userDangNhap!!.token)
                    conn.connect()

                    val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                    val builder = StringBuilder()
                    var line: String?
                    while (true) {
                        line = bufferedReader.readLine()
                        if(line == null)
                            break
                        builder.append(line)
                    }
                    return pajsonRouteeJSon(builder.toString())
                } catch (e: Exception) {
                    Log.e("error", e.toString())
                } finally {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                Log.e("Lỗi lấy LayerInfo", e.toString())
            }

            return null
        }

    interface AsyncResponse {
        fun processFinish(output: List<DLayerInfo>?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        this.mDialog = ProgressDialog(this.mContext, android.R.style.Theme_Material_Dialog_Alert)
        this.mDialog!!.setMessage(mContext.getString(R.string.preparing))
        this.mDialog!!.setCancelable(false)
        this.mDialog!!.show()
    }

    override fun doInBackground(vararg params: Void): List<DLayerInfo>? {
        try {
            return layerInfoAPI
            //            new GetVatTu(mContext).getVatTuFromService();
            //            new GetDMA(mContext).getMaDMAFromService();

        } catch (e: Exception) {
            Log.e("Lỗi lấy danh sách DMA", e.toString())
        }

        return null
    }

    @SafeVarargs
    override fun onProgressUpdate(vararg values: ListenableFuture<FeatureQueryResult>) {
        super.onProgressUpdate(*values)


    }

    override fun onPostExecute(value: List<DLayerInfo>?) {
        //        if (khachHang != null) {
        mDialog!!.dismiss()
        this.mDelegate.processFinish(value)
        //        }
    }

    @Throws(JSONException::class)
    private fun pajsonRouteeJSon(data: String?): List<DLayerInfo>? {
        if (data == null)
            return null
        val myData = "{ \"layerInfo\": $data}"
        val jsonData = JSONObject(myData)
        val jsonRoutes = jsonData.getJSONArray("layerInfo")
        val layerDTGS = ArrayList<DLayerInfo>()
        for (i in 0 until jsonRoutes.length()) {
            val jsonRoute = jsonRoutes.getJSONObject(i)
            layerDTGS.add(DLayerInfo(jsonRoute.getString(mContext.getString(R.string.sql_coloumn_sys_id)),
                    jsonRoute.getString(mContext.getString(R.string.sql_coloumn_sys_title)),
                    jsonRoute.getString(mContext.getString(R.string.sql_coloumn_sys_url)),
                    jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_iscreate)), jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_isdelete)),
                    jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_isedit)), jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_isview)),
                    jsonRoute.getString(mContext.getString(R.string.sql_column_sys_definition)),
                    jsonRoute.getString("OutFields").split(",".toRegex()).dropLastWhile {
                        it.isEmpty() }.toTypedArray(), null))
        }
        return layerDTGS
    }

}
