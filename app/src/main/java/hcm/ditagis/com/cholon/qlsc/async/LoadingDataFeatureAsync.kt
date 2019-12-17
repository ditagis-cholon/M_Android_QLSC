package hcm.ditagis.com.cholon.qlsc.async

import android.app.Activity
import android.content.Context
import android.os.AsyncTask
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.View
import android.widget.*
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.CodedValueDomain
import com.esri.arcgisruntime.data.Field
import hcm.ditagis.com.cholon.qlsc.R
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import java.util.*

open class LoadingDataFeatureAsync(private val mActivity: Activity, private val mContext: Context, private val mFields: List<Field>, private val mDelegate: AsyncResponse, vararg arcGISFeatures: ArcGISFeature)
    : AsyncTask<Boolean, Boolean, Void>() {
    private var mArcGISFeature: ArcGISFeature? = null
    private val mApplication: DApplication = mActivity.application as DApplication

    interface AsyncResponse {
        fun processFinish(views: List<View>)
    }

    init {
        if (arcGISFeatures != null && arcGISFeatures.size > 0)
            mArcGISFeature = arcGISFeatures[0]
    }

 override fun doInBackground(vararg params: Boolean?): Void? {
        if (params != null && params.size > 0)
            publishProgress(params[0])
        return null
    }

    override fun onProgressUpdate(vararg values: Boolean?) {
        mDelegate.processFinish(loadDataToAdd(values[0]))
    }

    private fun loadDataToAdd(isAdd: Boolean?): List<View> {
        val views = ArrayList<View>()
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        val outFields = mApplication.dFeatureLayer!!.getdLayerInfo().outFieldsArr
        var isFoundField = false
        for (field in mFields) {
            val name = field.name
            if (isAdd!!) {
                if (name == Constant.FieldSuCo.DIA_CHI
                        || name == Constant.FieldSuCo.NGUOI_PHAN_ANH
                        || name == Constant.FieldSuCo.SDT_PHAN_ANH
                        || name == Constant.FieldSuCo.THONG_TIN_PHAN_ANH
                        || name == Constant.FieldSuCo.GHI_CHU)
                    views.add(getView(field))

            } else {
                if (outFields.size > 0 && outFields[0] != "*") {
                    for (s in outFields)
                        if (s == field.name) {
                            isFoundField = true
                            break
                        }
                    if (isFoundField) {
                        isFoundField = false
                    } else
                        continue
                }
                if (name == Constant.FieldSuCo.ID_SUCO
                        || name == Constant.Field.OBJECTID
                        || name == Constant.FieldSuCo.MA_DMA
                        || name == Constant.FieldSuCo.MA_DUONG
                        || name == Constant.FieldSuCo.MA_PHUONG
                        || name == Constant.FieldSuCo.MA_QUAN
                        || name == Constant.FieldSuCo.SDT_PHAN_ANH
                        || name == Constant.FieldSuCo.NGUOI_PHAN_ANH)
                    continue
                views.add(getView(field))
            }
        }
        return views
    }

    private fun getView(field: Field): View {
        val layoutView = mActivity.layoutInflater.inflate(R.layout.item_add_feature, null) as LinearLayout
        val layoutEditNumber = layoutView.findViewById<LinearLayout>(R.id.llayout_add_feature_number)
        val layoutEditNumberDecimal = layoutView.findViewById<LinearLayout>(R.id.llayout_add_feature_number_decimal)
        val layoutEditSpinner = layoutView.findViewById<LinearLayout>(R.id.llayout_add_feature_spinner)
        val layoutEditText = layoutView.findViewById<LinearLayout>(R.id.llayout_add_feature_edittext)


        val spin = layoutEditSpinner.findViewById<Spinner>(R.id.spinner_add_spinner_value)
        val adapter = ArrayAdapter(mContext, android.R.layout.simple_list_item_1, ArrayList<String>())
        spin.adapter = adapter
        var value: Any? = null
        if (mArcGISFeature != null) {
            value = mArcGISFeature!!.attributes[field.name]
        }
        if (field.domain != null) {
            val codedValueDomain = field.domain as CodedValueDomain
            val values = ArrayList<String>()
            values.add(Constant.NULL)
            var selectedValue: String? = null
            for (codedValue in codedValueDomain.codedValues) {
                values.add(codedValue.name)
                if (value != null && codedValue.code == value)
                    selectedValue = codedValue.name
            }

            layoutEditNumberDecimal.visibility = View.GONE
            layoutEditSpinner.visibility = View.VISIBLE
            layoutEditText.visibility = View.GONE
            layoutEditNumber.visibility = View.GONE

            val textViewSpin = layoutEditSpinner.findViewById<TextView>(R.id.txt_add_spiner_title)
            textViewSpin.text = field.alias
            adapter.addAll(values)
            adapter.notifyDataSetChanged()

            for (i in values.indices) {
                if (selectedValue != null && values[i] == selectedValue) {
                    spin.setSelection(i)
                    break
                }
            }
        } else
            when (field.fieldType) {
                Field.Type.INTEGER, Field.Type.SHORT -> {
                    layoutEditNumberDecimal.visibility = View.GONE
                    layoutEditSpinner.visibility = View.GONE
                    layoutEditText.visibility = View.GONE
                    layoutEditNumber.visibility = View.VISIBLE

                    val textViewNumber = layoutEditNumber.findViewById<TextView>(R.id.txt_add_edit_number_title)
                    textViewNumber.text = field.alias

                    if (value != null) {
                        val editTextNumber = layoutView.findViewById<EditText>(R.id.etxt_add_edit_number_value)
                        try {
                            when (field.fieldType) {
                                Field.Type.INTEGER -> editTextNumber.setText(Integer.parseInt(value.toString()))
                                Field.Type.SHORT -> editTextNumber.setText(java.lang.Short.parseShort(value.toString()).toInt())
                            }
                        } catch (e: Exception) {
                            Toast.makeText(mContext, "Có lỗi khi load dữ liệu", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
                Field.Type.DOUBLE, Field.Type.FLOAT -> {
                    layoutEditNumberDecimal.visibility = View.VISIBLE
                    layoutEditSpinner.visibility = View.GONE
                    layoutEditText.visibility = View.GONE
                    layoutEditNumber.visibility = View.GONE

                    val textViewNumberDecimal = layoutEditNumberDecimal.findViewById<TextView>(R.id.txt_add_edit_number_decimal_title)
                    textViewNumberDecimal.text = field.alias

                    if (value != null) {
                        val editTextNumberDecimal = layoutView.findViewById<EditText>(R.id.etxt_add_edit_number_decimal_value)
                        try {
                            when (field.fieldType) {
                                Field.Type.DOUBLE -> editTextNumberDecimal.setText(java.lang.Double.parseDouble(value.toString()).toString() + "")
                                Field.Type.FLOAT -> editTextNumberDecimal.setText(java.lang.Float.parseFloat(value.toString()).toString() + "")
                            }
                        } catch (e: Exception) {
                            Toast.makeText(mContext, "Có lỗi khi load dữ liệu", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
                Field.Type.TEXT -> {
                    layoutEditNumberDecimal.visibility = View.GONE
                    layoutEditSpinner.visibility = View.GONE
                    layoutEditNumber.visibility = View.GONE
                    layoutEditText.visibility = View.VISIBLE
                    val textViewEditText = layoutEditText.findViewById<TextView>(R.id.txt_add_edit_text_title)
                    textViewEditText.text = field.alias
                    if (field.name == Constant.FieldSuCo.DIA_CHI)
                        value = mApplication.diemSuCo.vitri
                    if (value != null) {
                        val editText = layoutView.findViewById<EditText>(R.id.edit_add_edittext_value)
                        try {
                            editText.setText(value.toString())
                        } catch (e: Exception) {
                            Toast.makeText(mContext, "Có lỗi khi load dữ liệu", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
                else -> {
                    layoutEditNumberDecimal.visibility = View.GONE
                    layoutEditSpinner.visibility = View.GONE
                    layoutEditNumber.visibility = View.GONE
                    layoutEditText.visibility = View.GONE
                }
            }
        return layoutView
    }


}