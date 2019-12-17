package hcm.ditagis.com.cholon.qlsc.fragment.task

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters

import hcm.ditagis.com.cholon.qlsc.ListTaskActivity
import hcm.ditagis.com.cholon.qlsc.R
import hcm.ditagis.com.cholon.qlsc.async.QueryServiceFeatureTableGetListAsync
import hcm.ditagis.com.cholon.qlsc.utities.Constant

@SuppressLint("ValidFragment")
class ListTaskFragment @RequiresApi(api = Build.VERSION_CODES.N)
@SuppressLint("ValidFragment")
constructor(private val mActivity: ListTaskActivity, inflater: LayoutInflater) : Fragment() {
    internal lateinit var mLLayoutChuaXuLy: LinearLayout
    internal lateinit var mTxtChuaXuLy: TextView
    private val mRootView: View = inflater.inflate(R.layout.fragment_list_task_list, null)
    private var mSwipe: SwipeRefreshLayout? = null

    init {

        init()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun init() {
        mSwipe = mRootView.findViewById(R.id.swipe_list_task)
        mLLayoutChuaXuLy = mRootView.findViewById(R.id.llayout_list_task_chua_xu_ly)
        mTxtChuaXuLy = mRootView.findViewById(R.id.txt_list_task_chua_xu_ly)
        mTxtChuaXuLy.setOnClickListener(View.OnClickListener { this.onClick(it) })

        mSwipe!!.setOnRefreshListener {
            loadTasks()
            mSwipe!!.isRefreshing = false
        }
        loadTasks()
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private fun loadTasks() {
        mLLayoutChuaXuLy.removeAllViews()
        val queryParameters = QueryParameters()
        val queryClause = String.format("%s = %d", Constant.FieldSuCo.TRANG_THAI, Constant.TrangThaiSuCo.CHUA_XU_LY)
        queryParameters.whereClause = queryClause
        QueryServiceFeatureTableGetListAsync(mActivity, object: QueryServiceFeatureTableGetListAsync.AsyncResponse {
            override fun processFinish(output: List<Feature>?) {
                if (output != null && output.size > 0) {
                    val views = HandlingSearchHasDone.handleFromFeatures(mActivity, mRootView.context, output)
                    for (view in views) {
                        val txtID = view.findViewById<TextView>(R.id.txt_top)
                        view.setOnClickListener { v -> mActivity.itemClick(txtID.text.toString()) }
                        mLLayoutChuaXuLy.addView(view)
                    }
                }
                mTxtChuaXuLy.text = mActivity.resources.getString(R.string.txt_list_task_chua_xu_ly, mLLayoutChuaXuLy.childCount)
            }
        }).execute(queryParameters)
    }

    fun onClick(view: View) {
        when (view.id) {
            R.id.txt_list_task_chua_xu_ly -> if (mLLayoutChuaXuLy.visibility == View.VISIBLE)
                mLLayoutChuaXuLy.visibility = View.GONE
            else
                mLLayoutChuaXuLy.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mRootView
    }
}
