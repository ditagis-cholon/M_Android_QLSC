package hcm.ditagis.com.cholon.qlsc.entities;

public class VatTu {
    private String maVatTu;
    private String tenVatTu;
    private String donViTinh;

    public VatTu(String maVatTu, String tenVatTu, String donViTinh) {
        this.maVatTu = maVatTu;
        this.tenVatTu = tenVatTu;
        this.donViTinh = donViTinh;
    }

    public String getMaVatTu() {
        return maVatTu;
    }


    public String getTenVatTu() {
        return tenVatTu;
    }


    public String getDonViTinh() {
        return donViTinh;
    }

}
