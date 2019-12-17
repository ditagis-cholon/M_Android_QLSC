package hcm.ditagis.com.cholon.qlsc.utities

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

import android.content.Context.MODE_PRIVATE

/**
 * Created by ThanLe on 4/11/2018.
 */

class Preference private constructor() {
    private var mContext: Context? = null

    private val preferences: SharedPreferences
        get() = mContext!!.getSharedPreferences("LOGGED_IN", MODE_PRIVATE)

    fun setContext(context: Context) {
        mContext = context
    }

    /**
     * Method used to save Preferences
     */
    fun savePreferences(key: String, value: String) {
        val sharedPreferences = preferences
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun deletePreferences() {
        val all = preferences.all
        val editor = preferences.edit()
        for (key in all.keys)
            editor.remove(key).apply()
    }

    fun deletePreference(key: String) {
        val editor = preferences.edit()
        editor.remove(key).apply()
    }


    /**
     * Method used to load Preferences
     */
    fun loadPreference(key: String): String? {
        try {
            val sharedPreferences = preferences
            return sharedPreferences.getString(key, "")
        } catch (nullPointerException: NullPointerException) {
            return null
        }

    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var mInstance: Preference? = null

        val instance: Preference
            get() {
                if (mInstance == null)
                    mInstance = Preference()
                return mInstance!!
            }
    }

}
