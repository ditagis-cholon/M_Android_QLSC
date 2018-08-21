package hcm.ditagis.com.cholon.qlsc.entities.entitiesDB;


/**
 * Created by NGUYEN HONG on 3/14/2018.
 */

public class LayerInfoDTG {


    private String id;
    private String titleLayer;
    private String url;
    private boolean isCreate;
    private boolean isDelete;
    private boolean isEdit;
    private boolean isView;

    public LayerInfoDTG(String id, String titleLayer, String url, boolean isCreate, boolean isDelete,
                        boolean isEdit, boolean isView) {
        this.id = id;
        this.titleLayer = titleLayer;
        this.url = url;
        this.isCreate = isCreate;
        this.isDelete = isDelete;
        this.isEdit = isEdit;
        this.isView = isView;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitleLayer() {
        return titleLayer;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public boolean isCreate() {
        return isCreate;
    }

    public void setCreate(boolean create) {
        isCreate = create;
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

    public void setView(boolean view) {
        isView = view;
    }



}
