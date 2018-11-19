package hcm.ditagis.com.cholon.qlsc.async;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Attachment;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewMoreInfoAttachmentsAdapter;

/**
 * Created by ThanLe on 4/16/2018.
 */

public class GetAttachmentsAsync extends AsyncTask<Void, List<FeatureViewMoreInfoAttachmentsAdapter.Item>, Void> {
    @SuppressLint("StaticFieldLeak")
    private ArcGISFeature mSelectedArcGISFeature;
    @SuppressLint("StaticFieldLeak")
    private View layout;
    private AsyncResponse mDelegate;

    public interface AsyncResponse {
        void processFinish(List<FeatureViewMoreInfoAttachmentsAdapter.Item> feature);
    }

    public GetAttachmentsAsync(ArcGISFeature selectedArcGISFeature, AsyncResponse delete) {
        mDelegate = delete;
        mSelectedArcGISFeature = selectedArcGISFeature;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @SuppressLint("InflateParams")
    @Override
    protected Void doInBackground(Void... params) {
        final ListenableFuture<List<Attachment>> attachmentResults = mSelectedArcGISFeature.fetchAttachmentsAsync();
        attachmentResults.addDoneListener(() -> {
            try {
                List<FeatureViewMoreInfoAttachmentsAdapter.Item> items = new ArrayList<>();
                final List<Attachment> attachments = attachmentResults.get();
                AtomicInteger size = new AtomicInteger(attachments.size());
                // if selected feature has attachments, display them in a list fashion
                if (!attachments.isEmpty()) {
                    //
                    for (final Attachment attachment : attachments) {
                        if (attachment.getContentType().toLowerCase().trim().contains("png")) {
                            final FeatureViewMoreInfoAttachmentsAdapter.Item item = new FeatureViewMoreInfoAttachmentsAdapter.Item();
                            item.setName(attachment.getName());
                            final ListenableFuture<InputStream> inputStreamListenableFuture = attachment.fetchDataAsync();
                            inputStreamListenableFuture.addDoneListener(() -> {
                                try {
                                    InputStream inputStream = inputStreamListenableFuture.get();
                                    item.setImg(IOUtils.toByteArray(inputStream));
                                    items.add(item);
                                    size.decrementAndGet();
                                    //Kiểm tra nếu adapter có phần tử và attachment là phần tử cuối cùng thì show dialog

                                    if (size.get() == 0)
                                        publishProgress(items);

                                } catch (InterruptedException | ExecutionException | IOException e) {
                                    e.printStackTrace();
                                }
                            });

                        }
                    }

                } else {
                    publishProgress();
//                        MySnackBar.make(mCallout, "Không có file hình ảnh đính kèm", true);
                }

            } catch (Exception e) {
                Log.e("ERROR", e.getMessage());
            }
        });
        return null;
    }


    @Override
    protected void onProgressUpdate(List<FeatureViewMoreInfoAttachmentsAdapter.Item>... values) {
        if (values != null && values.length > 0) {
            mDelegate.processFinish(values[0]);

        } else mDelegate.processFinish(null);

    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

    }

}

