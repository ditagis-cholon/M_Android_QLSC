package hcm.ditagis.com.cholon.qlsc.entities.entitiesDB;


import com.esri.arcgisruntime.data.ServiceFeatureTable;
import com.esri.arcgisruntime.layers.FeatureLayer;

import hcm.ditagis.com.cholon.qlsc.entities.DLayerInfo;

/**
 * Created by NGUYEN HONG on 3/14/2018.
 */

public class DFeatureLayer {

    private ServiceFeatureTable serviceFeatureTable;
    private FeatureLayer layer;
    private DLayerInfo dLayerInfo;

    public DFeatureLayer(ServiceFeatureTable serviceFeatureTable, FeatureLayer layer, DLayerInfo dLayerInfo) {
        this.serviceFeatureTable = serviceFeatureTable;
        this.layer = layer;
        this.dLayerInfo = dLayerInfo;
    }

    public ServiceFeatureTable getServiceFeatureTable() {
        return serviceFeatureTable;
    }

    public FeatureLayer getLayer() {
        return layer;
    }

    public DLayerInfo getdLayerInfo() {
        return dLayerInfo;
    }
}
