package hcm.ditagis.com.cholon.qlsc.entities.entitiesDB;


import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;

/**
 * Created by NGUYEN HONG on 3/14/2018.
 */

public class FeatureLayerDTG {

    private ServiceFeatureTable serviceFeatureTable;
    private FeatureLayer layer;
    private LayerInfoDTG layerInfoDTG;

    public FeatureLayerDTG(ServiceFeatureTable serviceFeatureTable, FeatureLayer layer, LayerInfoDTG layerInfoDTG) {
        this.serviceFeatureTable = serviceFeatureTable;
        this.layer = layer;
        this.layerInfoDTG = layerInfoDTG;
    }

    public ServiceFeatureTable getServiceFeatureTable() {
        return serviceFeatureTable;
    }

    public FeatureLayer getLayer() {
        return layer;
    }

    public LayerInfoDTG getLayerInfoDTG() {
        return layerInfoDTG;
    }
}
