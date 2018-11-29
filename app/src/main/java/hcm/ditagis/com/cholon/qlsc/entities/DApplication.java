package hcm.ditagis.com.cholon.qlsc.entities;

import android.app.Application;

import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Point;

import java.util.List;

import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.DFeatureLayer;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.User;

public class DApplication extends Application {
    private DFeatureLayer DFeatureLayer;
    private DiemSuCo diemSuCo;
    private Geometry geometry;

    public DFeatureLayer getDFeatureLayer() {
        return DFeatureLayer;
    }

    public void setDFeatureLayer(DFeatureLayer DFeatureLayer) {
        this.DFeatureLayer = DFeatureLayer;
    }

    private ArcGISFeature selectedArcGISFeature;

    public ArcGISFeature getSelectedArcGISFeature() {
        return selectedArcGISFeature;
    }

    public void setSelectedArcGISFeature(ArcGISFeature selectedArcGISFeature) {
        this.selectedArcGISFeature = selectedArcGISFeature;
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

    private User userDangNhap;

    public User getUserDangNhap() {
        return userDangNhap;
    }

    public void setUserDangNhap(User userDangNhap) {
        this.userDangNhap = userDangNhap;
    }

    private Point addFeaturePoint;

    public Point getAddFeaturePoint() {
        return addFeaturePoint;
    }

    public void setAddFeaturePoint(Point addFeaturePoint) {
        this.addFeaturePoint = addFeaturePoint;
    }

    private List<DFeatureLayer> dFeatureLayers;

    public List<DFeatureLayer> getdFeatureLayers() {
        return dFeatureLayers;
    }

    public void setdFeatureLayers(List<DFeatureLayer> dFeatureLayers) {
        this.dFeatureLayers = dFeatureLayers;
    }

    private List<DLayerInfo> layerInfos;

    public List<DLayerInfo> getLayerInfos() {
        return layerInfos;
    }

    public void setLayerInfos(List<DLayerInfo> layerInfos) {
        this.layerInfos = layerInfos;
    }

    private List<byte[]> images;

    public List<byte[]> getImages() {
        return images;
    }

    public void setImages(List<byte[]> images) {
        this.images = images;
    }

    private boolean checkedVersion;

    public boolean isCheckedVersion() {
        return checkedVersion;
    }

    public void setCheckedVersion(boolean checkedVersion) {
        this.checkedVersion = checkedVersion;
    }
}
