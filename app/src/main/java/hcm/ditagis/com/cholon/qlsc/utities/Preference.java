package hcm.ditagis.com.cholon.qlsc.utities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by ThanLe on 4/11/2018.
 */

public class Preference {
    private Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static Preference mInstance = null;

    public static Preference getInstance() {
        if (mInstance == null)
            mInstance = new Preference();
        return mInstance;
    }

    public void setContext(Context context) {
        mContext = context;
    }

    private Preference() {

    }

    private SharedPreferences getPreferences() {
        return mContext.getSharedPreferences("LOGGED_IN", MODE_PRIVATE);
    }

    /**
     * Method used to save Preferences
     */
    public void savePreferences(String key, String value) {
        SharedPreferences sharedPreferences = getPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void deletePreferences() {
        Map<String, ?> all = getPreferences().getAll();
        SharedPreferences.Editor editor = getPreferences().edit();
        for (String key : all.keySet())
            editor.remove(key).apply();
    }

    public void deletePreference(String key) {
        SharedPreferences.Editor editor = getPreferences().edit();
        editor.remove(key).apply();
    }


    /**
     * Method used to load Preferences
     */
    public String loadPreference(String key) {
        try {
            SharedPreferences sharedPreferences = getPreferences();
            return sharedPreferences.getString(key, "");
        } catch (NullPointerException nullPointerException) {
            return null;
        }
    }

}
