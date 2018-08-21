package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewMoreInfoAdapter;
import hcm.ditagis.com.cholon.qlsc.R;

/**
 * Created by ThanLe on 4/16/2018.
 */

public class NotifyDataSetChangeAsync extends AsyncTask<FeatureViewMoreInfoAdapter, Void, Void> {
    private ProgressDialog dialog;
    @SuppressLint("StaticFieldLeak")
    private Activity mActivity;

    public NotifyDataSetChangeAsync(Activity activity) {
        mActivity =activity;
        dialog = new ProgressDialog(mActivity, android.R.style.Theme_Material_Dialog_Alert);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage(mActivity.getString(R.string.async_dang_cap_nhat_giao_dien));
        dialog.setCancelable(false);

        dialog.show();

    }

    @Override
    protected Void doInBackground(FeatureViewMoreInfoAdapter... params) {
        final FeatureViewMoreInfoAdapter adapter = params[0];
        try {
            Thread.sleep(500);
            mActivity.runOnUiThread(adapter::notifyDataSetChanged);
        } catch (InterruptedException ignored) {

        }


        return null;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);

    }


    @Override
    protected void onPostExecute(Void result) {
        assert dialog != null;
        if (dialog.isShowing())
            dialog.dismiss();
        super.onPostExecute(result);

    }

}
