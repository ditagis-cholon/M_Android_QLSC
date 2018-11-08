package hcm.ditagis.com.cholon.qlsc.entities;

import android.app.Application;

import com.esri.arcgisruntime.geometry.Geometry;

import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.FeatureLayerDTG;

public class DApplication extends Application {
    private FeatureLayerDTG featureLayerDTG;
    private DiemSuCo diemSuCo;
    private Geometry geometry;

    public FeatureLayerDTG getFeatureLayerDTG() {
        return featureLayerDTG;
    }

    public void setFeatureLayerDTG(FeatureLayerDTG featureLayerDTG) {
        this.featureLayerDTG = featureLayerDTG;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public DiemSuCo getDiemSuCo() {
        if (diemSuCo == null)
            diemSuCo = new DiemSuCo();
        return diemSuCo;
    }


}
