package hcm.ditagis.com.cholon.qlsc.entities.entitiesDB;

import java.util.ArrayList;
import java.util.List;

import hcm.ditagis.com.cholon.qlsc.entities.HoSoVatTuSuCo;
import hcm.ditagis.com.cholon.qlsc.entities.VatTu;

public class ListObjectDB {

    private static ListObjectDB instance = null;
    private List<VatTu> vatTuOngNganhs;
    private List<VatTu> vatTuOngChinhs;
    private List<String> dmas;
    private List<LayerInfoDTG> lstFeatureLayerDTG;
    private List<String> lstHoSoVatTuSuCoDelete;
    private List<HoSoVatTuSuCo> lstHoSoVatTuSuCoInsert;

    public List<HoSoVatTuSuCo> getHoSoVatTuSuCos() {
        return hoSoVatTuSuCos;
    }

    public void setHoSoVatTuSuCos(List<HoSoVatTuSuCo> hoSoVatTuSuCos) {
        this.hoSoVatTuSuCos = hoSoVatTuSuCos;
    }

    public List<String> getLstHoSoVatTuSuCoDelete() {
        return lstHoSoVatTuSuCoDelete;
    }

    public void setLstHoSoVatTuSuCoDelete(List<String> lstHoSoVatTuSuCoDelete) {
        this.lstHoSoVatTuSuCoDelete = lstHoSoVatTuSuCoDelete;
    }

    public List<HoSoVatTuSuCo> getLstHoSoVatTuSuCoInsert() {
        return lstHoSoVatTuSuCoInsert;
    }

    public void setLstHoSoVatTuSuCoInsert(List<HoSoVatTuSuCo> lstHoSoVatTuSuCoInsert) {
        this.lstHoSoVatTuSuCoInsert = lstHoSoVatTuSuCoInsert;
    }

    public void removeHoSoVatTuSuCoDelete(String maVatTu) {
        for (int i = 0; i < lstHoSoVatTuSuCoDelete.size(); i++) {
            String iMaVatTu = lstHoSoVatTuSuCoDelete.get(i);
            if (maVatTu.equals(iMaVatTu)) {
                lstHoSoVatTuSuCoDelete.remove(i);
                break;
            }
        }
    }
    public void removeHoSoVatTuSuCoInsert(String maVatTu) {
        for (int i = 0; i < lstHoSoVatTuSuCoInsert.size(); i++) {
            HoSoVatTuSuCo hoSoVatTuSuCo = lstHoSoVatTuSuCoInsert.get(i);
            if (maVatTu.equals(hoSoVatTuSuCo.getMaVatTu())) {
                lstHoSoVatTuSuCoInsert.remove(i);
                break;
            }
        }
    }
    public void clearListHoSoVatTuSuCoChange() {
        lstHoSoVatTuSuCoInsert.clear();
        lstHoSoVatTuSuCoDelete.clear();
    }

    private List<HoSoVatTuSuCo> hoSoVatTuSuCos;

    private ListObjectDB() {
        lstHoSoVatTuSuCoInsert = new ArrayList<>();
        lstHoSoVatTuSuCoDelete = new ArrayList<>();
        hoSoVatTuSuCos = new ArrayList<>();
    }

    public static ListObjectDB getInstance() {
        if (instance == null)
            instance = new ListObjectDB();
        return instance;
    }

    public List<VatTu> getVatTuOngNganhs() {
        return vatTuOngNganhs;
    }

    public void setVatTuOngNganhs(List<VatTu> vatTuOngNganhs) {
        this.vatTuOngNganhs = vatTuOngNganhs;
    }

    public List<VatTu> getVatTuOngChinhs() {
        return vatTuOngChinhs;
    }

    public void setVatTuOngChinhs(List<VatTu> vatTuOngChinhs) {
        this.vatTuOngChinhs = vatTuOngChinhs;
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
