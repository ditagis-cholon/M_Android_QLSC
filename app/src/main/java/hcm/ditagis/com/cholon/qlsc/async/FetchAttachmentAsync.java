package hcm.ditagis.com.cholon.qlsc.async;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Attachment;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

import hcm.ditagis.com.cholon.qlsc.utities.Constant;


/**
 * Created by ThanLe on 4/16/2018.
 */

public class FetchAttachmentAsync extends AsyncTask<Attachment, Bitmap, Void> {
    private AsyncResponse mDelegate;

    public interface AsyncResponse {
        void processFinish(Bitmap attachment);
    }

    public FetchAttachmentAsync( AsyncResponse delegate) {
        this.mDelegate = delegate;
    }


    @Override
    protected Void doInBackground(Attachment... params) {
        if (params != null && params.length > 0) {
            Attachment attachment = params[0];
            switch (attachment.getContentType()) {
                case Constant.FileType.VIDEO:
//                    final ListenableFuture<InputStream> inputStreamListenableFuture = attachment.fetchDataAsync();
//                    inputStreamListenableFuture.addDoneListener(() -> {
//                        try {
//                            InputStream inputStream = inputStreamListenableFuture.get();
//                            final FeatureViewMoreInfoAttachmentsAdapter.Item item =
//                                    new FeatureViewMoreInfoAttachmentsAdapter.Item(
//                                            attachment.getName(), inputStream, attachment.getContentType()
//                                    );
//                            File temp = DFile.getFileVideo(mContext, attachment.getName());
////                                        temp.deleteOnExit();
//
//                            FileOutputStream out = new FileOutputStream(temp);
//                            byte buf[] = new byte[128];
//                            do {
//                                int numread = inputStream.read(buf);
//                                if (numread <= 0)
//                                    break;
//                                out.write(buf, 0, numread);
//                            } while (true);
//
//                            inputStream.close();
//                            out.close();
//                            item.setURL(temp.getPath());
//                        } catch (InterruptedException | ExecutionException | IOException e) {
//                            publishProgress();
//                            e.printStackTrace();
//                        }
//                    });
                    publishProgress();
                    break;
                case Constant.FileType.PNG:
                case Constant.FileType.JPEG:
                    final ListenableFuture<InputStream> inputStreamListenableFuturePNG = attachment.fetchDataAsync();
                    inputStreamListenableFuturePNG.addDoneListener(() -> {
                        try {
                            InputStream inputStream = inputStreamListenableFuturePNG.get();
                            byte[] bytes = IOUtils.toByteArray(inputStream);
                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bmp, bmp.getWidth(),
                                    bmp.getHeight(), false);
                            publishProgress(scaledBitmap);

                        } catch (InterruptedException | ExecutionException | IOException e) {
                            publishProgress();
                            e.printStackTrace();
                        }
                    });
                    break;
            }
        }else publishProgress();
        return null;
    }


    @Override
    protected void onProgressUpdate(Bitmap... values) {
        if (values != null && values.length > 0) {
            this.mDelegate.processFinish(values[0]);
        } else this.mDelegate.processFinish(null);
        super.onProgressUpdate(values);

    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

    }

}

