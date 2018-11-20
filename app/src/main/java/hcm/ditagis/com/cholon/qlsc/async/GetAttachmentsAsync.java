package hcm.ditagis.com.cholon.qlsc.async;

import android.os.AsyncTask;
import android.util.Log;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Attachment;

import java.util.List;


/**
 * Created by ThanLe on 4/16/2018.
 */

public class GetAttachmentsAsync extends AsyncTask<ArcGISFeature, List<Attachment>, Void> {
    private AsyncResponse mDelegate;

    public interface AsyncResponse {
        void processFinish(List<Attachment> attachments);
    }

    public GetAttachmentsAsync(AsyncResponse delegate) {
        this.mDelegate = delegate;
    }


    @Override
    protected Void doInBackground(ArcGISFeature... params) {
        if (params != null && params.length > 0) {
            final ListenableFuture<List<Attachment>> attachmentResults = params[0].fetchAttachmentsAsync();
            attachmentResults.addDoneListener(() -> {
                try {
                    final List<Attachment> attachments = attachmentResults.get();
                    publishProgress(attachments);
                } catch (Exception e) {
                    Log.e("Lỗi attachment", e.getMessage());
                    publishProgress();
                }
            });
        }
        else publishProgress();
        return null;
    }


    @Override
    protected void onProgressUpdate(List<Attachment>... values) {
        if (values != null && values.length > 0) {
            this.mDelegate.processFinish(values[0]);
        } else this.mDelegate.processFinish(null);
    }
}

