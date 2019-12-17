package hcm.ditagis.com.cholon.qlsc.entities.entitiesDB

import hcm.ditagis.com.cholon.qlsc.entities.HoSoVatTuSuCo
import hcm.ditagis.com.cholon.qlsc.entities.VatTu
import java.util.*

class ListObjectDB private constructor() {
    var vatTus: List<VatTu>? = null
    var dmas: List<String>? = null
    var lstFeatureLayerDTG: List<LayerInfoDTG>? = null
    private var lstHoSoVatTuSuCoInsert: MutableList<HoSoVatTuSuCo>? = null
    var hoSoVatTuSuCos: List<HoSoVatTuSuCo>? = null

    fun getLstHoSoVatTuSuCoInsert(): List<HoSoVatTuSuCo>? {
        return lstHoSoVatTuSuCoInsert
    }

    fun setLstHoSoVatTuSuCoInsert(lstHoSoVatTuSuCoInsert: MutableList<HoSoVatTuSuCo>) {
        this.lstHoSoVatTuSuCoInsert = lstHoSoVatTuSuCoInsert
    }

    fun clearListHoSoVatTuSuCoChange() {
        lstHoSoVatTuSuCoInsert!!.clear()
    }


    init {
        lstHoSoVatTuSuCoInsert = ArrayList()
        hoSoVatTuSuCos = ArrayList()

        vatTus = ArrayList()
        dmas = ArrayList()
        lstFeatureLayerDTG = ArrayList()
    }

    companion object {

        private var instance: ListObjectDB? = null

        fun getInstance(): ListObjectDB {
            if (instance == null)
                instance = ListObjectDB()
            return instance!!
        }
    }
}
