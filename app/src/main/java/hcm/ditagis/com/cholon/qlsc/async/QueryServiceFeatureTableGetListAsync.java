package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.os.AsyncTask;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;


/**
 * Created by ThanLe on 4/16/2018.
 */

public class QueryServiceFeatureTableGetListAsync extends AsyncTask<QueryParameters, List<Feature>, Void> {
    @SuppressLint("StaticFieldLeak")
    private Activity mActivity;
    @SuppressLint("StaticFieldLeak")
    private AsyncResponse mDelegate;
    private DApplication mApplication;
    private ServiceFeatureTable mServiceFeatureTable;
    private AlertDialog mDialog;

    public interface AsyncResponse {
        void processFinish(List<Feature> output);
    }

    public QueryServiceFeatureTableGetListAsync(Activity activity, AsyncResponse delegate) {
        this.mActivity = activity;
        this.mApplication = (DApplication) activity.getApplication();
        this.mServiceFeatureTable = mApplication.getDFeatureLayer().getServiceFeatureTable();
        this.mDelegate = delegate;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        LinearLayout layout = (LinearLayout) mActivity.getLayoutInflater().inflate(R.layout.layout_progress_dialog, null);
        TextView txtTitle = layout.findViewById(R.id.txt_progress_dialog_title);
        TextView txtMessage = layout.findViewById(R.id.txt_progress_dialog_message);
        txtTitle.setText(mActivity.getApplicationContext().getString(R.string.message_list_task_title));
        txtMessage.setText(mActivity.getApplicationContext().getString(R.string.message_list_task_message));
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setCancelable(false);
        builder.setView(layout);

        mDialog = builder.create();
        mDialog.show();
        Window window = mDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(mDialog.getWindow().getAttributes());
            layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
            mDialog.getWindow().setAttributes(layoutParams);
        }
    }

    @Override
    protected Void doInBackground(QueryParameters... params) {
        if (params != null && params.length > 0)
            try {


                ListenableFuture<FeatureQueryResult> featureQueryResultListenableFuture = mServiceFeatureTable.queryFeaturesAsync(params[0],
                        ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
                featureQueryResultListenableFuture.addDoneListener(() -> {
                    try {
                        FeatureQueryResult result = featureQueryResultListenableFuture.get();
                        Iterator<Feature> iterator = result.iterator();
                        Feature item;
                        List<Feature> features = new ArrayList<>();
                        while (iterator.hasNext()) {
                            item = iterator.next();
                            features.add(item);
                        }
                        publishProgress(features);

                    } catch (InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                        publishProgress();
                    }
                });
            } catch (Exception e) {
                publishProgress();
            }
        return null;
    }

    @Override
    protected void onProgressUpdate(List<Feature>... values) {
        if (values != null && values.length > 0) mDelegate.processFinish(values[0]);
        else mDelegate.processFinish(null);

        if (mDialog != null && mDialog.isShowing())
            mDialog.dismiss();
    }

}