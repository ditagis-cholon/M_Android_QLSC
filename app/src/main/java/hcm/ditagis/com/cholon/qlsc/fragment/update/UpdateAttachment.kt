package hcm.ditagis.com.cholon.qlsc.fragment.update

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.ServiceFeatureTable
import hcm.ditagis.com.cholon.qlsc.R
import hcm.ditagis.com.cholon.qlsc.UpdateActivity
import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewMoreInfoAttachmentsAdapter
import hcm.ditagis.com.cholon.qlsc.adapter.OptionAddImageAdapter
import hcm.ditagis.com.cholon.qlsc.async.FetchAttachmentAsync
import hcm.ditagis.com.cholon.qlsc.async.GetAttachmentsAsync
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.atomic.AtomicInteger

@SuppressLint("ValidFragment")
class UpdateAttachment @SuppressLint("ValidFragment")
constructor(private val mActivity: UpdateActivity, inflater: LayoutInflater) : Fragment() {
    private val mRootView: View
    private val mApplication: DApplication
    private val mUri: Uri? = null

    internal lateinit var mBtnAddImage: Button
    internal lateinit var mLLayoutMain: LinearLayout
    internal lateinit var mListView: ListView
    internal lateinit var mLLayoutProgress: LinearLayout
    internal lateinit var mTxtProgress: TextView
    internal lateinit var mmSwipe: SwipeRefreshLayout
    private var mAttachments: List<Attachment>? = null
    private var mAdapter: FeatureViewMoreInfoAttachmentsAdapter? = null

    init {
        mRootView = inflater.inflate(R.layout.fragment_update_attachment, null)
        mApplication = mActivity.application as DApplication
        initViews()
    }

    private fun initViews() {
        mBtnAddImage = mRootView.findViewById(R.id.btn_update_attachment_capture)
        mLLayoutMain = mRootView.findViewById(R.id.llayout_update_attachment_main)
        mListView = mRootView.findViewById(R.id.list_update_attachment)
        mLLayoutProgress = mRootView.findViewById(R.id.llayout_update_attachment_progress)
        mTxtProgress = mRootView.findViewById(R.id.txt_update_attachment_progress)

        mBtnAddImage.setOnClickListener { this.onClick(it) }
        mmSwipe = mRootView.findViewById(R.id.swipe_udpate_attachment)
        mAdapter = FeatureViewMoreInfoAttachmentsAdapter(mRootView.context, ArrayList())
        mListView.adapter = mAdapter
        mListView.setOnItemLongClickListener { adapterView: AdapterView<*>, view: View, i: Int, l: Long ->
            val item = adapterView.getItemAtPosition(i) as FeatureViewMoreInfoAttachmentsAdapter.Item
            val name = item.name.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (name.size > 1 && name[1] == mApplication.userDangNhap!!.userName) {
                val builder = AlertDialog.Builder(mRootView.context, R.style.DDialogBuilder)
                builder.setTitle("Bạn có chắc muốn xóa ảnh này?")
                        .setPositiveButton("Xoá") { dialogInterface, i1 ->
                            mApplication.selectedArcGISFeature!!.deleteAttachmentAsync(mAttachments!![i]).addDoneListener {
                                val listListenableFuture = (mApplication.selectedArcGISFeature!!.featureTable as ServiceFeatureTable).applyEditsAsync()
                                listListenableFuture.addDoneListener {
                                    try {
                                        val featureEditResults = listListenableFuture.get()
                                        mAdapter!!.remove(item)
                                        mAdapter!!.notifyDataSetChanged()
                                        Toast.makeText(mRootView.context, "Xóa thành công", Toast.LENGTH_SHORT).show()
                                    } catch (e: InterruptedException) {
                                        Toast.makeText(mRootView.context, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                                        e.printStackTrace()
                                    } catch (e: ExecutionException) {
                                        Toast.makeText(mRootView.context, "Xóa thất bại", Toast.LENGTH_SHORT).show()
                                        e.printStackTrace()
                                    }
                                }

                            }
                            dialogInterface.dismiss()
                        }.setNegativeButton("Hủy") { dialogInterface, i12 -> dialogInterface.dismiss() }
                val dialog = builder.create()
                dialog.show()
            } else {
                Toast.makeText(mRootView.context, "Bạn không có quyền xóa ảnh này", Toast.LENGTH_SHORT).show()
            }
            false
        }

        mmSwipe.setOnRefreshListener {
            loadImages()
            mmSwipe.isRefreshing = false
        }
        loadImages()
    }

    fun startProgress() {
        mAdapter!!.clear()
        mLLayoutProgress.visibility = View.VISIBLE
        mLLayoutMain.visibility = View.GONE
    }

    fun stopProgress() {
        Toast.makeText(mRootView.context, "Nhấn và giữ ảnh cần xóa trong 2 giây để xóa", Toast.LENGTH_LONG).show()
        mAdapter!!.notifyDataSetChanged()
        mLLayoutProgress.visibility = View.GONE
        mLLayoutMain.visibility = View.VISIBLE
    }

    @SuppressLint("StaticFieldLeak")
    private fun loadImages() {
        startProgress()
        GetAttachmentsAsync (object: GetAttachmentsAsync.AsyncResponse {
            override fun processFinish(attachments: List<Attachment>?) {
                if (attachments != null) {
                    mAttachments = attachments
                    val size = AtomicInteger(attachments.size)
                    if (size.get() == 0) {
                        stopProgress()

                    } else
                        for (attachment in attachments) {
                            FetchAttachmentAsync (object: FetchAttachmentAsync.AsyncResponse {
                                override fun processFinish(bitmap: Bitmap?) {
                                    size.decrementAndGet()
                                    if (size.get() == 0) {
                                        stopProgress()
                                    }
                                    if (bitmap != null) {
                                        mAdapter!!.add(FeatureViewMoreInfoAttachmentsAdapter.Item(attachment.name, bitmap))

                                    }
                                }
                            }).execute(attachment)
                        }
                } else {

                    Toast.makeText(mRootView.context, "Không có tệp đính kèm", Toast.LENGTH_SHORT).show()
                }
            }
        }).execute(mApplication.selectedArcGISFeature)

    }

    fun handlingCaptureDone(arcGISFeature: ArcGISFeature?) {
        loadImages()
        if (arcGISFeature != null) {
            Toast.makeText(mRootView.context, "Đã lưu ảnh", Toast.LENGTH_SHORT).show()

        } else
            Toast.makeText(mRootView.context, "Thêm ảnh thất bại", Toast.LENGTH_SHORT).show()
    }


    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_update_attachment_capture -> {
                val layout = mActivity.layoutInflater.inflate(R.layout.layout_list_option, null) as LinearLayout
                val listView = layout.findViewById<ListView>(R.id.lst_list_option)
                listView.adapter = OptionAddImageAdapter(mRootView.context, Constant.OPTION_IMAGE_LIST)

                val bottomSheetDialog = BottomSheetDialog(mRootView.context)
                bottomSheetDialog.setContentView(layout)

                listView.setOnItemClickListener { adapterView, view1, i, l ->
                    val item = adapterView.getItemAtPosition(i) as String
                    when (item) {
                        Constant.OptionAddImage.CAPTURE -> {
                            mLLayoutProgress.visibility = View.VISIBLE
                            mLLayoutMain.visibility = View.GONE
                            mActivity.capture()
                        }
                        Constant.OptionAddImage.PICK -> mActivity.pickPhoto()
                    }
                    bottomSheetDialog.dismiss()
                }
                bottomSheetDialog.show()
            }
        }

    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mRootView
    }
}
