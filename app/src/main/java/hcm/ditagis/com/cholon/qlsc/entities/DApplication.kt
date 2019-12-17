package hcm.ditagis.com.cholon.qlsc.entities

import android.app.Application

import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.geometry.Geometry
import com.esri.arcgisruntime.geometry.Point

import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.DFeatureLayer
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.User

class DApplication : Application() {
    var dFeatureLayer: DFeatureLayer? = null
     var diemSuCo: DiemSuCo = DiemSuCo()
    var geometry: Geometry? = null

    var selectedArcGISFeature: ArcGISFeature? = null

    var userDangNhap: User? = null

    var addFeaturePoint: Point? = null

    private var dFeatureLayers: List<DFeatureLayer>? = null

    var layerInfos: List<DLayerInfo>? = null

    var images: MutableList<ByteArray>? = null

    var isCheckedVersion: Boolean = false



    fun getdFeatureLayers(): List<DFeatureLayer>? {
        return dFeatureLayers
    }

    fun setdFeatureLayers(dFeatureLayers: List<DFeatureLayer>) {
        this.dFeatureLayers = dFeatureLayers
    }
}
