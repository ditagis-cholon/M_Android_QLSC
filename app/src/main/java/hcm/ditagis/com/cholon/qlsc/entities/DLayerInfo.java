package hcm.ditagis.com.cholon.qlsc.entities;


/**
 * Created by NGUYEN HONG on 3/14/2018.
 */

public class DLayerInfo {


    private String id;
    private String titleLayer;
    private String url;
    private boolean isCreate;
    private boolean isDelete;
    private boolean isEdit;
    private boolean isView;
    private String definition;
    private String[] outFieldsArr;
    private String[] addFieldsArr;

    public DLayerInfo(String id, String titleLayer, String url, boolean isCreate, boolean isDelete, boolean isEdit, boolean isView, String definition, String[] outFieldsArr, String[] addFieldsArr) {
        this.id = id;
        this.titleLayer = titleLayer;
        this.url = url;
        this.isCreate = isCreate;
        this.isDelete = isDelete;
        this.isEdit = isEdit;
        this.isView = isView;
        this.definition = definition;
        this.outFieldsArr = outFieldsArr;
        this.addFieldsArr = addFieldsArr;
    }

    public String getId() {
        return id;
    }

    public String getTitleLayer() {
        return titleLayer;
    }

    public String getUrl() {
        return url;
    }

    public boolean isCreate() {
        return isCreate;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public boolean isView() {
        return isView;
    }

    public String getDefinition() {
        return definition;
    }

    public String[] getOutFieldsArr() {
        return outFieldsArr;
    }

    public String[] getAddFieldsArr() {
        return addFieldsArr;
    }
}
