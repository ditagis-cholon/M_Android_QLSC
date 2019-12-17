package hcm.ditagis.com.cholon.qlsc.fragment.task

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.esri.arcgisruntime.data.CodedValue
import com.esri.arcgisruntime.data.CodedValueDomain
import com.esri.arcgisruntime.data.Feature
import hcm.ditagis.com.cholon.qlsc.ListTaskActivity
import hcm.ditagis.com.cholon.qlsc.R
import hcm.ditagis.com.cholon.qlsc.async.QueryFeatureAsync
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import java.util.*


@SuppressLint("ValidFragment")
class SearchFragment @SuppressLint("ValidFragment")
constructor(private val mActivity: ListTaskActivity, inflater: LayoutInflater) : Fragment() {
    private val mRootView: View = inflater.inflate(R.layout.fragment_list_task_search, null)
    private var mEtxtAddress: EditText? = null
    private var mSpinTrangThai: Spinner? = null
    private var mTxtThoiGian: TextView? = null
    private var mBtnSearch: Button? = null
    private var mTxtKetQua: TextView? = null
    private var mLLayoutKetQua: LinearLayout? = null
    private val mApplication: DApplication
    private var mCodeValues: List<CodedValue>? = null
    private var mFeaturesResult: List<Feature>? = null

    init {
        mApplication = mActivity.application as DApplication
        init()
    }

    private fun init() {
        mEtxtAddress = mRootView.findViewById(R.id.etxt_list_task_search_address)
        mSpinTrangThai = mRootView.findViewById(R.id.spin_list_task_search_trang_thai)
        mTxtThoiGian = mRootView.findViewById(R.id.txt_list_task_search_thoi_gian)
        mBtnSearch = mRootView.findViewById(R.id.btn_list_task_search)
        mLLayoutKetQua = mRootView.findViewById(R.id.llayout_list_task_search_ket_qua)
        mTxtKetQua = mRootView.findViewById(R.id.txt_list_task_ket_qua)

        mBtnSearch!!.setOnClickListener{traCuu()}
        mTxtThoiGian!!.setOnClickListener {showDateTimePicker() }
        initSpinTrangThai()
    }

    private fun initSpinTrangThai() {
        val domain = mApplication.dFeatureLayer!!.layer.featureTable.getField(Constant.FieldSuCo.TRANG_THAI).domain
        if (domain != null) {
            mCodeValues = (domain as CodedValueDomain).codedValues
            if (mCodeValues != null) {
                val codes = ArrayList<String>()
                codes.add("Tất cả")
                for (codedValue in mCodeValues!!)
                    if (!Constant.DEFINITION_HIDE_COMPLETE.contains(codedValue.code.toString()))
                        codes.add(codedValue.name)
                val adapter = ArrayAdapter(mRootView.context, android.R.layout.simple_list_item_1, codes)
                mSpinTrangThai!!.adapter = adapter
            }
        }
    }

    private fun showDateTimePicker() {
        val dialogView = View.inflate(mRootView.context, R.layout.date_time_picker, null)
        val alertDialog = android.app.AlertDialog.Builder(mRootView.context).create()
        dialogView.findViewById<View>(R.id.date_time_set).setOnClickListener { view ->
            val datePicker = dialogView.findViewById<DatePicker>(R.id.date_picker)
            val calendar = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            val displaytime = DateFormat.format(Constant.DateFormat.DATE_FORMAT_STRING, calendar.time) as String
            @SuppressLint("SimpleDateFormat") val dateFormatGmt = Constant.DateFormat.DATE_FORMAT_YEAR_FIRST
            //            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            mTxtThoiGian!!.text = displaytime
            alertDialog.dismiss()
        }
        alertDialog.setView(dialogView)
        alertDialog.show()

    }


    private fun traCuu() {
        mLLayoutKetQua!!.removeAllViews()
        var trangThai: Short = -1
        if (mCodeValues != null)
            for (codedValue in mCodeValues!!) {
                if (codedValue.name == mSpinTrangThai!!.selectedItem.toString()) {
                    trangThai = java.lang.Short.parseShort(codedValue.code.toString())
                }
            }
        QueryFeatureAsync(mActivity, trangThai.toInt(),
                mEtxtAddress!!.text.toString(),
                mTxtThoiGian!!.text.toString(), object: QueryFeatureAsync.AsyncResponse {
            override fun processFinish(output: List<Feature>?) {
                if (output != null && output.isNotEmpty()) {
                    mFeaturesResult = output
                    val views = HandlingSearchHasDone.handleFromFeatures(mActivity, mRootView.context, output)
                    for (view in views) {
                        val txtID = view.findViewById<TextView>(R.id.txt_top)
                        view.setOnClickListener { v -> mActivity.itemClick(txtID.text.toString()) }
                        mLLayoutKetQua!!.addView(view)
                    }
                    mTxtKetQua!!.visibility = View.VISIBLE
                    mTxtKetQua!!.text = String.format("Kết quả tra cứu: %d sự cố", mLLayoutKetQua!!.childCount)
                } else {
                    mTxtKetQua!!.visibility = View.INVISIBLE
                    Toast.makeText(mRootView.context, "Không có kết quả", Toast.LENGTH_SHORT).show()
                }
            }
        }).execute()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return mRootView
    }


}
