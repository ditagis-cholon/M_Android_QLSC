package hcm.ditagis.com.cholon.qlsc.entities.entitiesDB;

import java.util.ArrayList;
import java.util.List;

import hcm.ditagis.com.cholon.qlsc.entities.HoSoVatTuSuCo;
import hcm.ditagis.com.cholon.qlsc.entities.VatTu;

public class ListObjectDB {

    private static ListObjectDB instance = null;
    private List<VatTu> vatTus;
    private List<String> dmas;
    private List<LayerInfoDTG> lstFeatureLayerDTG;
    private List<HoSoVatTuSuCo> lstHoSoVatTuSuCoInsert;
    private List<HoSoVatTuSuCo> hoSoVatTuSuCos;

    public List<HoSoVatTuSuCo> getHoSoVatTuSuCos() {
        return hoSoVatTuSuCos;
    }

    public void setHoSoVatTuSuCos(List<HoSoVatTuSuCo> hoSoVatTuSuCos) {
        this.hoSoVatTuSuCos = hoSoVatTuSuCos;
    }

    public List<HoSoVatTuSuCo> getLstHoSoVatTuSuCoInsert() {
        return lstHoSoVatTuSuCoInsert;
    }

    public void setLstHoSoVatTuSuCoInsert(List<HoSoVatTuSuCo> lstHoSoVatTuSuCoInsert) {
        this.lstHoSoVatTuSuCoInsert = lstHoSoVatTuSuCoInsert;
    }

    public void clearListHoSoVatTuSuCoChange() {
        lstHoSoVatTuSuCoInsert.clear();
    }


    private ListObjectDB() {
        lstHoSoVatTuSuCoInsert = new ArrayList<>();
        hoSoVatTuSuCos = new ArrayList<>();

        vatTus = new ArrayList<>();
        dmas = new ArrayList<>();
        lstFeatureLayerDTG = new ArrayList<>();
    }

    public static ListObjectDB getInstance() {
        if (instance == null)
            instance = new ListObjectDB();
        return instance;
    }

    public List<VatTu> getVatTus() {
        return vatTus;
    }

    public void setVatTus(List<VatTu> vatTuList) {
        this.vatTus = vatTuList;
    }

    public List<String> getDmas() {
        return dmas;
    }

    public void setDmas(List<String> dmas) {
        this.dmas = dmas;
    }

    public List<LayerInfoDTG> getLstFeatureLayerDTG() {
        return lstFeatureLayerDTG;
    }

    public void setLstFeatureLayerDTG(List<LayerInfoDTG> lstFeatureLayerDTG) {
        this.lstFeatureLayerDTG = lstFeatureLayerDTG;
    }
}
