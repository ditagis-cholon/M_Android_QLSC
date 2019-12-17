package hcm.ditagis.com.cholon.qlsc.async

import android.annotation.SuppressLint
import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.esri.arcgisruntime.data.*
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.utities.Constant
import java.util.*
import java.util.concurrent.ExecutionException


/**
 * Created by ThanLe on 4/16/2018.
 */

class EditAsync(activity: Activity, private val mIsComplete: Boolean,
                private val mSelectedArcGISFeature: ArcGISFeature, private val mLLayoutField: LinearLayout?, private val mImage: ByteArray?,
                private val mDelegate: AsyncResponse) : AsyncTask<Void, ArcGISFeature, Void>() {
    @SuppressLint("StaticFieldLeak")
    private val mServiceFeatureTable: ServiceFeatureTable
    private val mApplication: DApplication
    private var mAttributes: HashMap<String, Any?>? = null

    private//        try {
    //        } catch (Exception e) {
    //            Log.e("Lỗi lấy attributes", e.toString());
    //        }
    val attributes: HashMap<String, Any?>
        get() {
            val attributes = HashMap<String, Any?>()
            var currentAlias = ""
            if (mLLayoutField != null)
                for (i in 0 until mLLayoutField.childCount) {
                    val itemAddFeature = mLLayoutField.getChildAt(i) as LinearLayout
                    for (j in 0 until itemAddFeature.childCount) {
                        val typeInput_itemAddFeature = itemAddFeature.getChildAt(j) as LinearLayout
                        for (k in 0 until typeInput_itemAddFeature.childCount) {
                            val view = typeInput_itemAddFeature.getChildAt(k)
                            if (view.visibility == View.VISIBLE) {
                                if (view is EditText && !currentAlias.isEmpty()) {
                                    for (field in mServiceFeatureTable.fields) {
                                        if (field.alias == currentAlias) {
                                            if (field.domain != null) {
                                                val codedValues = (field.domain as CodedValueDomain).codedValues

                                                val valueDomain = getCodeDomain(codedValues, view.text.toString())
                                                if (valueDomain != null)
                                                    attributes[currentAlias] = valueDomain.toString()
                                            } else {
                                                attributes[currentAlias] = view.text.toString()
                                            }
                                            break
                                        }
                                    }
                                } else if (view is Spinner && !currentAlias.isEmpty()) {
                                    for (field in mServiceFeatureTable.fields) {
                                        if (field.alias == currentAlias) {
                                            if (field.domain != null) {
                                                val codedValues = (field.domain as CodedValueDomain).codedValues

                                                val valueDomain = getCodeDomain(codedValues, view.selectedItem.toString())
                                                if (valueDomain != null)
                                                    attributes[currentAlias] = valueDomain.toString()
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
            return attributes
        }

    interface AsyncResponse {
        fun processFinish(feature: ArcGISFeature?)
    }

    init {
        mApplication = activity.application as DApplication
        mServiceFeatureTable = mSelectedArcGISFeature.featureTable as ServiceFeatureTable
    }

    override fun onPreExecute() {
        super.onPreExecute()
        if (mImage == null) mAttributes = attributes

    }

    override fun doInBackground(vararg params: Void): Void? {
        if (mImage == null) {
            for (alias in mAttributes!!.keys) {
                for (field in mServiceFeatureTable.fields) {
                    if (field.alias == alias) {
                        try {
                            val value = mAttributes!![alias].toString().trim { it <= ' ' }
                            //                            if (value.isEmpty())
                            //                                continue;
                            when (field.fieldType) {
                                Field.Type.TEXT -> mSelectedArcGISFeature.attributes[field.name] = value
                                Field.Type.DOUBLE -> mSelectedArcGISFeature.attributes[field.name] = java.lang.Double.parseDouble(value)
                                Field.Type.FLOAT -> mSelectedArcGISFeature.attributes[field.name] = java.lang.Float.parseFloat(value)
                                Field.Type.INTEGER -> mSelectedArcGISFeature.attributes[field.name] = Integer.parseInt(value)
                                Field.Type.SHORT -> mSelectedArcGISFeature.attributes[field.name] = java.lang.Short.parseShort(value)
                            }

                        } catch (e: Exception) {
                            mSelectedArcGISFeature.attributes[field.name] = null
                            Log.e("Lỗi thêm điểm", e.toString())
                        }

                        break
                    }
                }
            }
        }
        if (mIsComplete)
            mSelectedArcGISFeature.attributes[Constant.FieldSuCo.TRANG_THAI] = Constant.TrangThaiSuCo.HOAN_THANH
        mServiceFeatureTable.loadAsync()
        mServiceFeatureTable.addDoneLoadingListener {
            // update feature in the feature table
            mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature).addDoneListener {
                mServiceFeatureTable.applyEditsAsync().addDoneListener {
                    if (mImage != null) {
                        if (mSelectedArcGISFeature.canEditAttachments())
                            addAttachment()
                        else
                            applyEdit()
                    } else {
                        applyEdit()

                    }
                }
            }
        }
        return null
    }

    private fun addAttachment() {
        val attachmentName = String.format(Constant.AttachmentName.UPDATE,
                mApplication.userDangNhap!!.userName, System.currentTimeMillis())
        val addResult = mSelectedArcGISFeature.addAttachmentAsync(mImage, Constant.FileType.PNG, attachmentName)
        addResult.addDoneListener {
            try {
                val attachment = addResult.get()
                if (attachment.size > 0) {
                    val tableResult = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature)
                    tableResult.addDoneListener { applyEdit() }
                }
            } catch (ignored: Exception) {
                publishProgress()
            }
        }
    }


    private fun applyEdit() {

        val updatedServerResult = mServiceFeatureTable.applyEditsAsync()
        updatedServerResult.addDoneListener {
            try {
                updatedServerResult.get()
                publishProgress(mSelectedArcGISFeature)
            } catch (e: InterruptedException) {
                e.printStackTrace()
                publishProgress()
            } catch (e: ExecutionException) {
                e.printStackTrace()
                publishProgress()
            }


        }

    }

    private fun getIdFeatureTypes(featureTypes: List<FeatureType>, value: String): Any? {
        var code: Any? = null
        for (featureType in featureTypes) {
            if (featureType.name == value) {
                code = featureType.id
                break
            }
        }
        return code
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

    override fun onProgressUpdate(vararg values: ArcGISFeature) {
        super.onProgressUpdate(*values)
        if (values != null && values.size > 0)
            this.mDelegate.processFinish(values[0])
        else
            this.mDelegate.processFinish(null)
    }

}

