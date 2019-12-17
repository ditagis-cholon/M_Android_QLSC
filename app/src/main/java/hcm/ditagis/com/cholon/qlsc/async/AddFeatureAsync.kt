package hcm.ditagis.com.cholon.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.*
import com.esri.arcgisruntime.data.*
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import java.util.*
import java.util.concurrent.ExecutionException


/**
 * Created by ThanLe on 4/16/2018.
 */

class AddFeatureAsync(@field:SuppressLint("StaticFieldLeak")
                      private val mActivity: Activity,
                      private val mServiceFeatureTable: ServiceFeatureTable, private val mLLayoutField: LinearLayout, @field:SuppressLint("StaticFieldLeak")
                      private val mDelegate: AsyncResponse) : AsyncTask<Void, Feature, Void>() {
    private val mApplication: DApplication

    private var mAttributes: HashMap<String, Any?>? = null

    private var mThongTinPhanAnh: Any? = null
    private var mGhiChu: String? = null


    private//        try {
    //        } catch (Exception e) {
    //            Log.e("Lỗi lấy attributes", e.toString());
    //        }
    val attributes: HashMap<String, Any?>
        get() {
            val attributes = HashMap<String, Any?>()
            var currentAlias = ""
            var countEmpty = 0
            for (i in 0 until mLLayoutField.childCount) {
                val itemAddFeature = mLLayoutField.getChildAt(i) as LinearLayout
                for (j in 0 until itemAddFeature.childCount) {
                    val typeInput_itemAddFeature = itemAddFeature.getChildAt(j) as LinearLayout
                    for (k in 0 until typeInput_itemAddFeature.childCount) {
                        val view = typeInput_itemAddFeature.getChildAt(k)
                        if (view.visibility == View.VISIBLE) {
                            if (view is EditText && !currentAlias.isEmpty()) {
                                val value = view.text.toString()
                                if (value.length == 0)
                                    countEmpty++
                                else

                                    for (field in mServiceFeatureTable.fields) {
                                        if (field.alias == currentAlias) {
                                            if (field.name == Constant.FieldSuCo.GHI_CHU) {
                                                mGhiChu = value
                                            }
                                            if (field.domain != null) {
                                                val codedValues = (field.domain as CodedValueDomain).codedValues

                                                val valueDomain = getCodeDomain(codedValues, view.text.toString())
                                                if (valueDomain != null)
                                                    attributes[currentAlias] = valueDomain.toString()
                                                else
                                                    countEmpty++
                                            } else {
                                                attributes[currentAlias] = view.text.toString()
                                            }
                                            break
                                        }
                                    }
                            } else if (view is Spinner && !currentAlias.isEmpty()) {
                                if (view.selectedItemPosition == 0)
                                    countEmpty++
                                else
                                    for (field in mServiceFeatureTable.fields) {
                                        if (field.alias == currentAlias) {
                                            if (field.domain != null) {
                                                val codedValues = (field.domain as CodedValueDomain).codedValues

                                                val codeDomain = getCodeDomain(codedValues, view.selectedItem.toString())
                                                if (field.name == Constant.FieldSuCo.THONG_TIN_PHAN_ANH)
                                                    mThongTinPhanAnh = codeDomain
                                                if (codeDomain != null)
                                                    attributes[currentAlias] = codeDomain.toString()
                                                else
                                                    countEmpty++
                                            } else {
                                            }
                                            break
                                        }
                                    }
                            } else if (view is TextView) {
                                currentAlias = view.text.toString()
                                attributes[currentAlias] = null
                            }
                        }
                    }
                }
            }
            if (countEmpty == 5 || (mGhiChu == null || mGhiChu!!.length == 0) && (if (mThongTinPhanAnh != null) java.lang.Short.parseShort(mThongTinPhanAnh!!.toString()).toInt() == 0 else false))
                publishProgress()
            return attributes
        }

    interface AsyncResponse {
        fun processFinish(output: Feature?)
    }

    init {
        this.mApplication = mActivity.application as DApplication
    }

    override fun onPreExecute() {
        super.onPreExecute()
        mAttributes = attributes
    }

    override fun doInBackground(vararg params: Void): Void? {
        val feature: Feature
        try {
            feature = mServiceFeatureTable.createFeature()
            feature.geometry = mApplication.addFeaturePoint
            for (alias in mAttributes!!.keys) {
                for (field in mServiceFeatureTable.fields) {
                    if (field.alias == alias) {
                        try {
                            val value = mAttributes!![alias].toString().trim { it <= ' ' }
                            if (value.isEmpty())
                                continue
                            when (field.fieldType) {
                                Field.Type.TEXT -> feature.attributes[field.name] = value
                                Field.Type.DOUBLE -> feature.attributes[field.name] = java.lang.Double.parseDouble(value)
                                Field.Type.FLOAT -> feature.attributes[field.name] = java.lang.Float.parseFloat(value)
                                Field.Type.INTEGER -> feature.attributes[field.name] = Integer.parseInt(value)
                                Field.Type.SHORT -> feature.attributes[field.name] = java.lang.Short.parseShort(value)
                            }

                        } catch (e: Exception) {
                            Log.e("Lỗi thêm điểm", e.toString())
                        }

                        break
                    }
                }
            }
            addFeatureAsync(feature)

        } catch (e: Exception) {
            publishProgress()
        }

        return null
    }

    private fun getCodeDomain(codedValues: List<CodedValue>, value: String): Any? {
        var code: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.name == value) {
                code = codedValue.code
                break
            }
        }
        return code
    }

    private fun addFeatureAsync(feature: Feature) {
        val mapViewResult = mServiceFeatureTable.addFeatureAsync(feature)
        mapViewResult.addDoneListener {
            val listListenableEditAsync = mServiceFeatureTable.applyEditsAsync()
            listListenableEditAsync.addDoneListener {
                try {
                    val edits = listListenableEditAsync.get()
                    if (edits != null && edits.size > 0) {
                        if (!edits[0].hasCompletedWithErrors()) {
                            val objectId = edits[0].objectId
                            NotifyServerAddingFeature(mApplication, object :
                                    NotifyServerAddingFeature.AsyncResponse {
                                override fun processFinish(output: String?) {
                                    if (output != null && output.isNotEmpty()) {
                                        val queryParameters = QueryParameters()
                                        val query = String.format("%s = '%s'", Constant.FieldSuCo.ID_SUCO, output)
                                        queryParameters.whereClause = query
                                        val featuresAsync = mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                                        featuresAsync.addDoneListener {
                                            try {
                                                val result = featuresAsync.get()
                                                if (result.iterator().hasNext()) {
                                                    val item = result.iterator().next()
                                                    if (mApplication.images != null && mApplication.images!!.size > 0)
                                                        addAttachment(item as ArcGISFeature, item)
                                                    else
                                                        publishProgress(item)
                                                } else
                                                    publishProgress()
                                            } catch (e: InterruptedException) {
                                                e.printStackTrace()
                                                publishProgress()
                                            } catch (e: ExecutionException) {
                                                e.printStackTrace()
                                                publishProgress()
                                            }
                                        }
                                    }
                                }
                            }).execute(objectId.toString() + "")

                        } else {
                            publishProgress()

                        }
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                    publishProgress()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                    publishProgress()
                }


            }
        }
    }

    private fun addAttachment(arcGISFeature: ArcGISFeature, feature: Feature) {
        for (image in mApplication.images!!) {
            @SuppressLint("StringFormatMatches") val attachmentName = String.format(Constant.AttachmentName.ADD,
                    mApplication.userDangNhap!!.userName, System.currentTimeMillis())
            val addResult = arcGISFeature.addAttachmentAsync(
                    image, Constant.FileType.PNG, attachmentName)
        }
        val tableResult = mServiceFeatureTable.updateFeatureAsync(arcGISFeature)
        //            tableResult.addDoneListener(() -> {
        val updatedServerResult = mServiceFeatureTable.applyEditsAsync()
        updatedServerResult.addDoneListener {
            try {
                val edits = updatedServerResult.get()
                if (edits.size > 0) {
                    if (!edits[0].hasCompletedWithErrors()) {
                        publishProgress(feature)
                    } else
                        publishProgress()
                } else
                    publishProgress()
            } catch (e: InterruptedException) {
                publishProgress()
                e.printStackTrace()
            } catch (e: ExecutionException) {
                publishProgress()
                e.printStackTrace()
            }


        }
    }


    override fun onProgressUpdate(vararg values: Feature) {
        if (values.isEmpty()) {
            Toast.makeText(mActivity.applicationContext, "Nhập thiếu dữ liệu hoặc có lỗi xảy ra", Toast.LENGTH_SHORT).show()
            this.mDelegate.processFinish(null)
        } else if (values.isNotEmpty()) this.mDelegate.processFinish(values[0])
    }


}