package hcm.ditagis.com.cholon.qlsc.entities;

import java.util.List;

import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.LayerInfoDTG;

public class ListLayerInfoDTG {
    private List<LayerInfoDTG> lstFeatureLayerDTG;

    private ListLayerInfoDTG() {

    }

    private static ListLayerInfoDTG instance = null;

    public static ListLayerInfoDTG getInstance() {
        if (instance == null)
            instance = new ListLayerInfoDTG();
        return instance;
    }

    public List<LayerInfoDTG> getLstFeatureLayerDTG() {
        return lstFeatureLayerDTG;
    }

    public void setLstFeatureLayerDTG(List<LayerInfoDTG> lstFeatureLayerDTG) {
        this.lstFeatureLayerDTG = lstFeatureLayerDTG;
    }

    public static void setInstance(ListLayerInfoDTG instance) {
        ListLayerInfoDTG.instance = instance;
    }
}
