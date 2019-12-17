package hcm.ditagis.com.cholon.qlsc

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.layers.FeatureLayer
import hcm.ditagis.com.cholon.qlsc.async.AddFeatureAsync
import hcm.ditagis.com.cholon.qlsc.async.LoadingDataFeatureAsync
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import hcm.ditagis.com.cholon.qlsc.utities.ImageFile
import kotlinx.android.synthetic.main.activity_add_feature.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

class AddFeatureActivity : AppCompatActivity(), View.OnClickListener {

    private var mApplication: DApplication? = null

    private var mImages: MutableList<ByteArray>? = null
    private var mUri: Uri? = null
    private val mAdapterLayer: ArrayAdapter<String>? = null
    private var mFeatureLayer: FeatureLayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_feature)
        mApplication = application as DApplication
        initViews()
    }

    private fun initViews() {
        mImages = ArrayList()
        btn_add_feature_capture.setOnClickListener { this.onClick(it) }
        btn_add_feature_add!!.setOnClickListener { this.onClick(it) }
        btn_add_feature_pick_photo.setOnClickListener { this.onClick(it) }
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayShowHomeEnabled(true)
        txt_add_feature_progress!!.text = "Đang khởi tạo thuộc tính..."
        llayout_add_feature_progress!!.visibility = View.VISIBLE
        llayout_add_feature_main!!.visibility = View.GONE

        mFeatureLayer = mApplication!!.dFeatureLayer!!.layer
        LoadingDataFeatureAsync(this@AddFeatureActivity,
                this@AddFeatureActivity, mFeatureLayer!!.featureTable.fields, object : LoadingDataFeatureAsync.AsyncResponse {
            override fun processFinish(views: List<View>) {
                for (view1 in views) {

                    llayout_add_feature_field!!.addView(view1)
                }
                llayout_add_feature_progress!!.visibility = View.GONE
                llayout_add_feature_main!!.visibility = View.VISIBLE
            }
        }).execute(true)
    }


    private fun hadPoint(): Boolean {
        return mApplication!!.addFeaturePoint != null
    }

    fun capture() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.path)

        val photo = ImageFile.getFile(this@AddFeatureActivity)
        //        this.mUri= FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".my.package.name.provider", photo);
        mUri = Uri.fromFile(photo)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
        //        this.mUri = Uri.fromFile(photo);
        try {
            this.startActivityForResult(cameraIntent, Constant.RequestCode.ADD_FEATURE_ATTACHMENT)
        } catch (e: Exception) {
            Log.e("Lỗi chụp ảnh", e.toString())
        }

    }

    private fun pickPhoto() {
        val pickPhoto = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, Constant.RequestCode.PICK_PHOTO)
    }

    override fun onClick(view: View) {
        when (view.id) {

            R.id.btn_add_feature_add -> if (!hadPoint()) {
                Toast.makeText(this@AddFeatureActivity, R.string.message_add_feature_had_not_point, Toast.LENGTH_LONG).show()
            } else if (mFeatureLayer == null) {
                Toast.makeText(this@AddFeatureActivity, R.string.message_add_feature_had_not_feature, Toast.LENGTH_LONG).show()
            } else {
                llayout_add_feature_progress!!.visibility = View.VISIBLE
                llayout_add_feature_main!!.visibility = View.GONE
                txt_add_feature_progress!!.text = "Đang lưu..."
                AddFeatureAsync(this@AddFeatureActivity, mApplication!!.dFeatureLayer!!.serviceFeatureTable, llayout_add_feature_field!!,
                        object : AddFeatureAsync.AsyncResponse {
                            override fun processFinish(output: Feature?) {
                                if (output != null) {
                                    mApplication!!.diemSuCo.objectID = java.lang.Long.parseLong(output.attributes[Constant.Field.OBJECTID].toString())

                                    goHome()
                                }
                                llayout_add_feature_progress!!.visibility = View.GONE
                                llayout_add_feature_main!!.visibility = View.VISIBLE

                            }
                        }).execute()


            }
            R.id.btn_add_feature_capture -> capture()
            R.id.btn_add_feature_pick_photo -> pickPhoto()
        }

    }

    private fun getBitmap(path: String?): Bitmap? {

        val uri = Uri.fromFile(File(path!!))
        var `in`: InputStream?
        try {
            val IMAGE_MAX_SIZE = 1000000 // 1.0MP
            `in` = contentResolver.openInputStream(uri)

            // Decode image size
            var o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`in`, null, o)
            assert(`in` != null)
            `in`!!.close()


            var scale = 1
            while (o.outWidth * o.outHeight * (1 / Math.pow(scale.toDouble(), 2.0)) > IMAGE_MAX_SIZE) {
                scale++
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight)

            var b: Bitmap?
            `in` = contentResolver.openInputStream(uri)
            if (scale > 1) {
                scale--
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = BitmapFactory.Options()
                o.inSampleSize = scale
                b = BitmapFactory.decodeStream(`in`, null, o)

                // resize to desired dimensions
                val height = b!!.height
                val width = b.width
                Log.d("", "1th scale operation dimenions - width: $width, height: $height")

                val y = Math.sqrt(IMAGE_MAX_SIZE / (width.toDouble() / height))
                val x = y / height * width

                val scaledBitmap = Bitmap.createScaledBitmap(b, x.toInt(), y.toInt(), true)
                b.recycle()
                b = scaledBitmap

                System.gc()
            } else {
                b = BitmapFactory.decodeStream(`in`)
            }
            assert(`in` != null)
            `in`!!.close()

            Log.d("", "bitmap size - width: " + b!!.width + ", height: " + b.height)
            return b
        } catch (e: IOException) {
            Log.e("", e.message, e)
            return null
        }

    }

    private fun handlingImage(bitmap: Bitmap?, isFromCamera: Boolean) {
        try {
            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                val imageView = ImageView(llayout_add_feature_image!!.context)
                imageView.setPadding(0, 0, 0, 10)
                if (isFromCamera) {
                    val matrix = Matrix()
                    matrix.postRotate(90f)
                    val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                    rotatedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    imageView.setImageBitmap(rotatedBitmap)
                } else {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    imageView.setImageBitmap(bitmap)
                }
                val image = outputStream.toByteArray()
                Toast.makeText(this, "Đã lưu ảnh", Toast.LENGTH_SHORT).show()
                llayout_add_feature_image!!.addView(imageView)

                mImages!!.add(image)
                mApplication!!.images = mImages
            }
        } catch (ignored: Exception) {
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constant.RequestCode.ADD_FEATURE_ATTACHMENT -> if (resultCode == Activity.RESULT_OK) {
                if (this.mUri != null) {
                    val bitmap = getBitmap(this.mUri!!.path)
                    handlingImage(bitmap, true)
                }

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Hủy chụp ảnh", Toast.LENGTH_SHORT)
            } else {
                Toast.makeText(this, "Lỗi khi chụp ảnh", Toast.LENGTH_SHORT)
            }
            Constant.RequestCode.PICK_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val contentURI = data.data
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
                        handlingImage(bitmap, false)

                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@AddFeatureActivity, "Failed!", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    override fun onBackPressed() {
        goHomeCancel()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun goHome() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun goHomeCancel() {
        val intent = Intent()
        setResult(Activity.RESULT_CANCELED, intent)
        finish()
    }
}
