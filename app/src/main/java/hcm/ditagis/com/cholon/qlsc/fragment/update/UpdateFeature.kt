package hcm.ditagis.com.cholon.qlsc.fragment.update

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.esri.arcgisruntime.data.ArcGISFeature
import hcm.ditagis.com.cholon.qlsc.R
import hcm.ditagis.com.cholon.qlsc.UpdateActivity
import hcm.ditagis.com.cholon.qlsc.async.EditAsync
import hcm.ditagis.com.cholon.qlsc.async.LoadingDataFeatureAsync
import hcm.ditagis.com.cholon.qlsc.entities.DApplication

@SuppressLint("ValidFragment")
class UpdateFeature @SuppressLint("ValidFragment")
constructor(private val mActivity: UpdateActivity, inflater: LayoutInflater) : Fragment() {
    private val mRootView: View
    private val mApplication: DApplication
    private val mUri: Uri? = null

    internal lateinit var mBtnUpdate: Button
    internal lateinit var mBtnComplete: Button
    internal lateinit var mLLayoutMain: LinearLayout
    internal lateinit var mLLayoutField: LinearLayout
    internal lateinit var mLLayoutProgress: LinearLayout
    internal lateinit var mTxtProgress: TextView
    private var mmSwipe: SwipeRefreshLayout? = null
    private var mArcGISFeature: ArcGISFeature? = null

    init {
        mRootView = inflater.inflate(R.layout.fragment_update_feature, null)
        mApplication = mActivity.application as DApplication
        initViews()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mRootView
    }

    private fun initViews() {
        mBtnComplete = mRootView.findViewById(R.id.btn_update_feature_complete)
        mBtnUpdate = mRootView.findViewById(R.id.btn_update_feature_update)
        mLLayoutMain = mRootView.findViewById(R.id.llayout_update_feature_main)
        mLLayoutField = mRootView.findViewById(R.id.llayout_update_feature_field)
        mLLayoutProgress = mRootView.findViewById(R.id.llayout_update_feature_progress)
        mTxtProgress = mRootView.findViewById(R.id.txt_update_feature_progress)

        mBtnComplete.setOnClickListener { this.onClick(it) }
        mBtnUpdate.setOnClickListener { this.onClick(it) }
        mmSwipe = mRootView.findViewById(R.id.swipe_udpate_feature)
        if (mApplication.images != null) mApplication.images!!.clear()
        mTxtProgress.text = "Đang khởi tạo thuộc tính..."
        mLLayoutProgress.visibility = View.VISIBLE
        mLLayoutMain.visibility = View.GONE
        mArcGISFeature = mApplication.selectedArcGISFeature


        mmSwipe!!.setOnRefreshListener {
            loadData()
            mmSwipe!!.isRefreshing = false
        }
        loadData()
    }

    private fun loadData() {
        mLLayoutField.removeAllViews()
        mLLayoutProgress.visibility = View.VISIBLE
        mLLayoutMain.visibility = View.GONE
        LoadingDataFeatureAsync(mActivity, mRootView.context, mArcGISFeature!!.featureTable.fields,
                object: LoadingDataFeatureAsync.AsyncResponse{
                    override fun processFinish(views: List<View>) {
                        if (views != null)
                            for (view1 in views!!) {
                                mLLayoutField.addView(view1)
                            }
                        mLLayoutProgress.visibility = View.GONE
                        mLLayoutMain.visibility = View.VISIBLE
                    }
                }
               , mArcGISFeature!!).execute(false)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_update_feature_complete -> {
                val layout = mActivity.layoutInflater.inflate(R.layout.layout_dialog, null) as LinearLayout
                val txtTitle = layout.findViewById<TextView>(R.id.txt_dialog_title)
                val txtMessage = layout.findViewById<TextView>(R.id.txt_dialog_message)
                txtTitle.text = getString(R.string.message_title_confirm)
                txtMessage.text = "Bạn có muốn hoàn thành sự cố?"

                val builder = AlertDialog.Builder(mRootView.context)
                builder.setView(layout)
                builder.setCancelable(false)
                        .setPositiveButton(R.string.message_btn_ok) { dialog, i ->
                            mLLayoutProgress.visibility = View.VISIBLE
                            mLLayoutMain.visibility = View.GONE
                            mTxtProgress.text = "Đang lưu..."
                            val completeAsync = EditAsync(mActivity, true,
                                    mArcGISFeature!!, mLLayoutField, null
                            ,object: EditAsync.AsyncResponse{
                                override fun processFinish(arcGISFeature: ArcGISFeature?) {
                                    mLLayoutProgress.visibility = View.GONE
                                    mLLayoutMain.visibility = View.VISIBLE
                                    if (arcGISFeature != null) {
                                        Toast.makeText(mRootView.context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                        //                                mActivity.goHome();
                                    } else
                                        Toast.makeText(mRootView.context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()

                                }
                            })
                            completeAsync.execute()
                        }.setNegativeButton(R.string.message_btn_cancel) { dialog, i -> }

                val dialog = builder.create()
                dialog.show()
            }
            R.id.btn_update_feature_update -> {
                mLLayoutProgress.visibility = View.VISIBLE
                mLLayoutMain.visibility = View.GONE
                mTxtProgress.text = "Đang lưu..."
                val updateAsync = EditAsync(mActivity, false,
                        mArcGISFeature!!, mLLayoutField, null, object: EditAsync.AsyncResponse{
                    override fun processFinish(arcGISFeature: ArcGISFeature?) {
                        mLLayoutProgress.visibility = View.GONE
                        mLLayoutMain.visibility = View.VISIBLE
                        if (arcGISFeature != null) {
                            Toast.makeText(mBtnUpdate.context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                            //                                mActivity.goHome();
                        } else
                            Toast.makeText(mBtnUpdate.context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                    }
                }
                )
                updateAsync.execute()
            }
        }
    }
}
