package hcm.ditagis.com.cholon.qlsc.async

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import com.esri.arcgisruntime.data.Attachment
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import org.apache.commons.io.IOUtils
import java.io.IOException
import java.util.concurrent.ExecutionException


/**
 * Created by ThanLe on 4/16/2018.
 */

class FetchAttachmentAsync(private val mDelegate: AsyncResponse) : AsyncTask<Attachment, Bitmap, Void>() {

    interface AsyncResponse {
        fun processFinish(attachment: Bitmap?)
    }


    override fun doInBackground(vararg params: Attachment): Void? {
        if (params != null && params.isNotEmpty()) {
            val attachment = params[0]
            when (attachment.contentType) {
                Constant.FileType.VIDEO ->
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
                    publishProgress()
                Constant.FileType.PNG, Constant.FileType.JPEG -> {
                    val inputStreamListenableFuturePNG = attachment.fetchDataAsync()
                    inputStreamListenableFuturePNG.addDoneListener {
                        try {
                            val inputStream = inputStreamListenableFuturePNG.get()
                            val bytes = IOUtils.toByteArray(inputStream)
                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            val scaledBitmap = Bitmap.createScaledBitmap(bmp, bmp.width,
                                    bmp.height, false)
                            publishProgress(scaledBitmap)

                        } catch (e: InterruptedException) {
                            publishProgress()
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            publishProgress()
                            e.printStackTrace()
                        } catch (e: IOException) {
                            publishProgress()
                            e.printStackTrace()
                        }
                    }
                }
            }
        } else
            publishProgress()
        return null
    }


    override fun onProgressUpdate(vararg values: Bitmap) {
        if (values != null && values.isNotEmpty()) {
            this.mDelegate.processFinish(values[0])
        } else
            this.mDelegate.processFinish(null)
        super.onProgressUpdate(*values)

    }


}

