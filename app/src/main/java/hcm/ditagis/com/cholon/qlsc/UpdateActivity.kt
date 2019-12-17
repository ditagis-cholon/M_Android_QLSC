package hcm.ditagis.com.cholon.qlsc

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import com.google.android.material.tabs.TabLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.esri.arcgisruntime.data.ArcGISFeature
import hcm.ditagis.com.cholon.qlsc.async.EditAsync
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.fragment.update.UpdateAttachment
import hcm.ditagis.com.cholon.qlsc.fragment.update.UpdateFeature
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import hcm.ditagis.com.cholon.qlsc.utities.ImageFile
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.*

class UpdateActivity : AppCompatActivity() {
    private var mApplication: DApplication? = null
    private var mUri: Uri? = null

    private lateinit var mUpdateFeature: UpdateFeature
    private lateinit var mUpdateAttachment: UpdateAttachment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update)
        mApplication = application as DApplication
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayHomeAsUpEnabled(true)
        Objects.requireNonNull<ActionBar>(supportActionBar).setDisplayShowHomeEnabled(true)


        val viewPager = findViewById<ViewPager>(R.id.container_update)
        val sectionsPagerAdapter = SectionsPagerAdapter(supportFragmentManager)

        viewPager.adapter = sectionsPagerAdapter
        val tabLayout = findViewById<TabLayout>(R.id.tabs_update)
        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        tabLayout.addOnTabSelectedListener(TabLayout.ViewPagerOnTabSelectedListener(viewPager))
        mUpdateFeature = UpdateFeature(this, layoutInflater)
        mUpdateAttachment = UpdateAttachment(this, layoutInflater)

        viewPager.setCurrentItem(0, true)
    }

    inner class SectionsPagerAdapter internal constructor(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            when (position) {
                0 -> return mUpdateFeature
                1 -> return mUpdateAttachment
                else -> return mUpdateFeature
            }
        }

        override fun getCount(): Int {
            return 2
        }
    }

    fun capture() {
        val cameraIntent = Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, MediaStore.Images.Media.EXTERNAL_CONTENT_URI.path)

        val photo = ImageFile.getFile(this)
        //        this.mUri= FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".my.package.name.provider", photo);
        mUri = Uri.fromFile(photo)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
        //        this.mUri = Uri.fromFile(photo);
        try {
            this.startActivityForResult(cameraIntent, Constant.RequestCode.UPDATE_ATTACHMENT)
        } catch (e: Exception) {
            Log.e("Lỗi chụp ảnh", e.toString())
        }

    }

    fun pickPhoto() {
        val pickPhoto = Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, Constant.RequestCode.PICK_PHOTO)
    }

    private fun update(bitmap: Bitmap) {
        mUpdateAttachment!!.startProgress()
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val image = outputStream.toByteArray()
        val editAsync: EditAsync
        editAsync = EditAsync(this@UpdateActivity, false,
                mApplication!!.selectedArcGISFeature!!, null, image
        ,object: EditAsync.AsyncResponse{
            override fun processFinish(arcGISFeature: ArcGISFeature?) {
                mUpdateAttachment!!.handlingCaptureDone(arcGISFeature)

            }
        })
        editAsync.execute()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Constant.RequestCode.UPDATE_ATTACHMENT -> if (resultCode == Activity.RESULT_OK) {
                if (this.mUri != null) {
                    val bitmap = getBitmap(mUri!!.path)
                    try {
                        if (bitmap != null) {
                            val matrix = Matrix()
                            matrix.postRotate(90f)
                            val rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                            update(rotatedBitmap)

                        }
                    } catch (ignored: Exception) {
                    }

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
                        update(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                        Toast.makeText(this@UpdateActivity, "Có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                    }

                }
            }
        }
    }

    private fun getBitmap(path: String?): Bitmap? {

        val uri = Uri.fromFile(File(path!!))
        var `in`: InputStream?
        try {
            val IMAGE_MAX_SIZE = 1200000 // 1.2MP
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

    override fun onBackPressed() {
        goHome()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    fun goHome() {
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
