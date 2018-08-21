package hcm.ditagis.com.cholon.qlsc.async;

import android.content.Context;
import android.os.AsyncTask;

import com.esri.arcgisruntime.data.Feature;
import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.SpatialReference;
import com.esri.core.map.FeatureResult;
import com.esri.core.tasks.query.QueryParameters;
import com.esri.core.tasks.query.QueryTask;

import java.util.Iterator;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.LayerInfoDTG;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.ListObjectDB;

public class AsyncQueryTask extends AsyncTask<Object, Void, String> {
    private Context mContext;
    private AsyncResponse mDelegate;

    public interface AsyncResponse {
        void processFinish(String output);
    }

    public AsyncQueryTask(Context context, AsyncResponse delegate) {
        this.mContext = context;
        this.mDelegate = delegate;
    }

    @Override
    protected String doInBackground(Object... objects) {
        if (objects == null || objects.length < 2)
            return null;
        String url = objects[1].toString();
        String result = "";

        QueryParameters qParameters = new QueryParameters();
        String whereClause = "1 = 1";
        SpatialReference sr = SpatialReference.create(102100);
        qParameters.setGeometry((Geometry) objects[0]);
        qParameters.setOutSpatialReference(sr);
        qParameters.setReturnGeometry(true);
        qParameters.setWhere(whereClause);

        QueryTask qTask = new QueryTask(url);

        try {
            FeatureResult results = qTask.execute(qParameters);
            for (Iterator it = results.iterator(); it.hasNext(); ) {
                Feature feature = (Feature) it.next();
                result = feature.getAttributes().get(mContext.getString(R.string.field_DMA_maDMA)).toString();
                break;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mDelegate.processFinish(s);
    }
}
