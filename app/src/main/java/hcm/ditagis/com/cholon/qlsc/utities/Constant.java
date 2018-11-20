package hcm.ditagis.com.cholon.qlsc.utities;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;

import hcm.ditagis.com.cholon.qlsc.adapter.SettingsAdapter;

/**
 * Created by ThanLe on 3/1/2018.
 */

public class Constant {
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd_MM_yyyy");
    @SuppressLint("SimpleDateFormat")
    public static final SimpleDateFormat DATE_FORMAT_VIEW = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
    public static final String DEFINITION_HIDE_COMPLETE = " and TrangThai <> 3";
    //        public static final String SERVER_API = "http://gis.capnuoccholon.com.vn/cholon/api";
    public static final String SERVER_API = "http://sawagis.vn/cholon/api";
    public static final String NULL = "";

    public static class DateFormat {
        public static final String DATE_FORMAT_STRING = "dd/MM/yyyy";
        public static final SimpleDateFormat DATE_FORMAT_YEAR_FIRST = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);
        public static final SimpleDateFormat DATE_FORMAT_VIEW = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");

    }
    public class FileType {

        public static final String VIDEO = "video/quicktime";
        public static final String PNG = "PNG";
        public static final String JPEG = "image/jpeg";
        public static final String PDF = "application/pdf";
        public static final String DOC = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    }
    public class HTTPRequest {
        public static final String GET_METHOD = "GET";
        public static final String POST_METHOD = "POST";
        public static final String AUTHORIZATION = "Authorization";
    }

    public static class RequestCode {
        public static final int LOGIN = 0;
        public static final int ADD = 7;
        public static final int CAPTURE = 1;
        public static final int SHOW_CAPTURE = 2;
        public static final int PERMISSION = 3;
        public static final int SEARCH = 4;
        public static final int BASEMAP = 5;
        public static final int RLAYER = 6;
        public static final int ADD_FEATURE_ATTACHMENT = 8;
        public static final int LIST_TASK = 9;
        public static final int UPDATE= 10;
        public static final int UPDATE_ATTACHMENT = 11;
        public static final int NOTIFICATION = 100;

    }

    public class Field {
        public static final String OBJECTID = "OBJECTID";
    }

    public class FieldSuCo {
        public static final String ID_SUCO = "IDSuCo";
        public static final String NGUOI_PHAN_ANH = "NguoiPhanAnh";
        public static final String SDT_PHAN_ANH = "SDTPhanAnh";
        public static final String TG_PHAN_ANH = "TGPhanAnh";
        public static final String DOI_QUAN_LY = "DoiQuanLy";
        public static final String HINH_THUC_PHAT_HIEN = "HinhThucPhatHien";
        public static final String TRANG_THAI = "TrangThai";
        public static final String THONG_TIN_PHAN_ANH = "ThongTinPhanAnh";
        public static final String TG_KHAC_PHUC = "TGKhacPhuc";
        public static final String NHOM_KHAC_PHUC = "NhomKhacPhuc";
        public static final String PHAN_LOAI_SU_CO = "PhanLoaiSuCo";
        public static final String DIA_CHI = "DiaChi";
        public static final String MA_DUONG = "MaDuong";
        public static final String MA_QUAN = "MaQuan";
        public static final String MA_PHUONG = "MaPhuong";
        public static final String MA_DMA = "MaDMA";
        public static final String LOAI_SU_CO = "LoaiSuCo";
        public static final String VAT_LIEU = "VatLieu";
        public static final String NGUYEN_NHAN = "NguyenNhan";
        public static final String DUONG_KINH_ONG = "DuongKinhOng";
        public static final String AP_LUC = "ApLuc";
        public static final String DO_SAU_LUNG_ONG = "DoSauLungOng";
        public static final String GHI_CHU = "GhiChu";

    }

    public class TrangThaiSuCo {
        public static final short CHUA_XU_LY = 0;
        public static final short DANG_XU_LY = 1;
        public static final short HOAN_THANH = 3;

    }

    public class URL_API {
        public static final String CHECK_VERSION = "http://cholon.sawagis.vn/cholon/versioning/QLSC?version=%s";
        public static final String ADD_FEATURE = SERVER_API + "/QuanLySuCo/TiepNhanSuCo/%s";
        public static final String LOGIN = SERVER_API + "/Login";
        public static final String PROFILE = SERVER_API + "/Account/Profile";
        public static final String GENERATE_ID_SUCO = SERVER_API + "/QuanLySuCo/GenerateIDSuCo";
        public static final String LAYER_INFO = SERVER_API + "/Account/layerinfo";
        public static final String CHANGE_PASSWORD = SERVER_API + "/Account/changepass";
        public static final String COMPLETE = SERVER_API + "/quanlysuco/xacnhanhoanthanhnhanvien?id=%s";
        public static final String IS_ACCESS = SERVER_API + "/Account/IsAccess/m_qlsc";
        public static final String GENERATE_ID_SUCOTHONGTIN = SERVER_API + "/QuanLySuCo/GenerateIDSuCoThongTin/";


    }

    public String API_LOGIN;

    {
        API_LOGIN = SERVER_API + "/Login";
    }

    public String DISPLAY_NAME;

    {
        DISPLAY_NAME = SERVER_API + "/Account/Profile";
    }

    public String LAYER_INFO;

    {
        LAYER_INFO = SERVER_API + "/layerinfo";
    }

    public String GENERATE_ID_SUCO;

    {
        GENERATE_ID_SUCO = SERVER_API + "/quanlysuco/generateidsuco/";
    }

    public String IS_ACCESS;

    {
        IS_ACCESS = SERVER_API + "/Account/IsAccess/m_qlsc";
    }

    public class HOSOVATTUSUCO_METHOD {
        public static final int FIND = 0;
        public static final int INSERT = 2;
    }

    public class ACCOUNT_ROLE {
        public static final String QLCN1 = "qlcn1";
        public static final String QLCN2 = "qlcn2";
    }

    private SettingsAdapter.Item[] mSettingsItems;

    private static Constant mInstance = null;

    public static Constant getInstance() {
        if (mInstance == null)
            mInstance = new Constant();
        return mInstance;
    }

    private Constant() {
        mSettingsItems = new SettingsAdapter.Item[]{
                new SettingsAdapter.Item("Phương thức thêm điểm sự cố", ""),
                new SettingsAdapter.Item("Tùy chọn tìm kiếm", ""),
                new SettingsAdapter.Item("Bố cục giao diện", ""),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
                new SettingsAdapter.Item("Tiêu đề cài đặt", "Tiêu đề con cài đặt"),
        };
    }

    public SettingsAdapter.Item[] getSettingsItems() {
        return mSettingsItems;
    }


}
