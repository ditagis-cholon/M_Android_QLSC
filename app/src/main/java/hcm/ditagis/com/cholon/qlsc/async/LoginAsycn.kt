package hcm.ditagis.com.cholon.qlsc.async

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import hcm.ditagis.com.cholon.qlsc.R
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.User
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class LoginAsycn(@field:SuppressLint("StaticFieldLeak")
                 private val mContext: Context, private val mDelegate: LoginAsycn.AsyncResponse) : AsyncTask<String, Void, User?>() {
    private var mDialog: ProgressDialog? = null

    interface AsyncResponse {
        fun processFinish(output: User?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        this.mDialog = ProgressDialog(this.mContext, android.R.style.Theme_Material_Dialog_Alert)
        this.mDialog!!.setMessage(mContext.getString(R.string.connect_message))
        this.mDialog!!.setCancelable(false)
        this.mDialog!!.show()
    }

    override fun doInBackground(vararg params: String): User? {
        val userName = params[0]
        val pin = params[1]
        var user: User? = User()
        //        String passEncoded = (new EncodeMD5()).encode(pin + "_DITAGIS");
        // Do some validation here
        val urlParameters = String.format("Username=%s&Password=%s", userName, pin)
        val urlWithParam = String.format("%s?%s", Constant.instance.API_LOGIN, urlParameters)
        try {
            //            + "&apiKey=" + API_KEY
            val url = URL(urlWithParam)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                conn.connect()
                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val line = bufferedReader.readLine()
                if (line != null) {
                    val token = line.replace("\"", "")
                    bufferedReader.close()

                    if (checkAccess(token)!!) {
                        user = getMoreInfo(token)
                    }
                }
            } catch (e: Exception) {
                Log.e("Lỗi login", e.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.message, e)
        }

        return user

    }

    override fun onPostExecute(result: User?) {
        //        if (khachHang != null) {
        mDialog!!.dismiss()
        this.mDelegate.processFinish(result)
        //        }
    }

    private fun checkAccess(token: String): Boolean? {
        var isAccess = false
        try {
            val url = URL(Constant.instance.IS_ACCESS)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = false
                conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, token)
                conn.connect()

                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val line = bufferedReader.readLine()
                if (line == "true")
                    isAccess = true

            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("error", e.toString())
        }

        return isAccess

    }

    private fun getMoreInfo(token: String): User? {
        var user = User()

        try {
            val url = URL(Constant.instance.DISPLAY_NAME)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = false
                conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, token)
                conn.connect()

                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val line = bufferedReader.readLine()
                if (line != null) {
                    user = pajsonRouteeJSon(line)
                    user.token = token
                }
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("error", e.toString())
        }

        return user

    }

    @Throws(JSONException::class)
    private fun pajsonRouteeJSon(data: String?): User {
        val user = User()
        if (data != null) {
            val myData = "{ \"account\": [$data]}"
            val jsonData = JSONObject(myData)
            val jsonRoutes = jsonData.getJSONArray("account")
            for (i in 0 until jsonRoutes.length()) {
                val jsonRoute = jsonRoutes.getJSONObject(i)
                val displayName = jsonRoute.getString(mContext.getString(R.string.sql_coloumn_login_displayname))
                val username = jsonRoute.getString(mContext.getString(R.string.sql_coloumn_login_username))
                val role = jsonRoute.getString(mContext.getString(R.string.sql_coloumn_login_role))

                user.displayName = displayName
                user.userName = username
                user.role = role.toUpperCase()
            }
        }
        return user
    }
}
