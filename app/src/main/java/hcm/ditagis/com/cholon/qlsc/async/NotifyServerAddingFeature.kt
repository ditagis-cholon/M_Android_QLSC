package hcm.ditagis.com.cholon.qlsc.async

import android.os.AsyncTask
import android.util.Log
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class NotifyServerAddingFeature(private val mApplication: DApplication, private val mDelegate: AsyncResponse)
    : AsyncTask<String, Void, String?>() {

    interface AsyncResponse {
        fun processFinish(output: String?)
    }


    override fun doInBackground(vararg params: String): String {
        try {
            if (params.isNotEmpty())
                return getLayerInfoAPI(params[0])
        } catch (e: Exception) {
            Log.e("Lỗi lấy danh sách DMA", e.toString())
        }

        return ""
    }


    override fun onPostExecute(value: String?) {
        this.mDelegate.processFinish(value)
        //        }
    }


    private fun getLayerInfoAPI(objectID: String): String {
        try {
            val API_URL = String.format(Constant.URL_API.ADD_FEATURE, objectID)

            val url = URL(API_URL)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = true
                conn.instanceFollowRedirects = false
                conn.requestMethod = Constant.HTTPRequest.POST_METHOD

                conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, mApplication.userDangNhap!!.token)
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.useCaches = false
                val wr = OutputStreamWriter(conn.outputStream)
                wr.write("")
                wr.flush()
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
                return builder.toString().replace("\"", "")
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("Lỗi lấy LayerInfo", e.toString())
        }

        return ""
    }


}
