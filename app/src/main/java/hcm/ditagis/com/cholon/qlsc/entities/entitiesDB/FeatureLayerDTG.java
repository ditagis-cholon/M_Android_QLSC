package hcm.ditagis.com.cholon.qlsc.entities.entitiesDB;


import com.esri.arcgisruntime.layers.FeatureLayer;

/**
 * Created by NGUYEN HONG on 3/14/2018.
 */

public class FeatureLayerDTG {


    private FeatureLayer layer;

    private LayerInfoDTG layerInfoDTG;

    public FeatureLayerDTG(FeatureLayer layer, LayerInfoDTG layerInfoDTG) {
        this.layer = layer;
        this.layerInfoDTG = layerInfoDTG;
    }

    public FeatureLayer getLayer() {
        return layer;
    }

    public void setLayer(FeatureLayer layer) {
        this.layer = layer;
    }

    public LayerInfoDTG getLayerInfoDTG() {
        return layerInfoDTG;
    }

}
