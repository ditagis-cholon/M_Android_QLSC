package hcm.ditagis.com.cholon.qlsc.utities

import android.Manifest
import android.annotation.SuppressLint

import com.esri.arcgisruntime.geometry.SpatialReference

import java.text.SimpleDateFormat
import java.util.ArrayList

/**
 * Created by ThanLe on 3/1/2018.
 */

class Constant private constructor() {

    var API_LOGIN: String

    var DISPLAY_NAME: String

    var LAYER_INFO: String

    var GENERATE_ID_SUCO: String

    var IS_ACCESS: String

    object DateFormat {
        val DATE_FORMAT_STRING = "dd/MM/yyyy"
        val DATE_FORMAT_YEAR_FIRST = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val DATE_FORMAT = SimpleDateFormat(DATE_FORMAT_STRING)
        val DATE_FORMAT_VIEW = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")

    }

    object URLImage {
        val CHUA_SUA_CHUA_BAT_THUONG = "$SERVER/images/map/-1.png"
        val CHUA_SUA_CHUA = "$SERVER/images/map/0.png"
        val HOAN_THANH = "$SERVER/images/map/3.png"
    }

    object FileType {

        val VIDEO = "video/quicktime"
        val PNG = "image/png"
        val JPEG = "image/jpeg"
        val PDF = "application/pdf"
        val DOC = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }

    object AttachmentName {
        val ADD = "img_%s_%d.png"
        val UPDATE = "img_%s_%d.png"
    }

    object HTTPRequest {
        val GET_METHOD = "GET"
        val POST_METHOD = "POST"
        val AUTHORIZATION = "Authorization"
    }

    object RequestCode {
        val LOGIN = 0
        val CAPTURE = 1
        val SHOW_CAPTURE = 2
        val PERMISSION = 3
        val SEARCH = 4
        val BASEMAP = 5
        val RLAYER = 6
        val ADD = 7
        val ADD_FEATURE_ATTACHMENT = 8
        val LIST_TASK = 9
        val UPDATE = 10
        val UPDATE_ATTACHMENT = 11
        val PICK_PHOTO = 12
        val NOTIFICATION = 100

    }

    object Field {
        val OBJECTID = "OBJECTID"
    }

    object OptionAddImage {
        val CAPTURE = "Chụp ảnh"
        val PICK = "Chọn ảnh"
    }

    object FieldSuCo {
        val ID_SUCO = "IDSuCo"
        val NGUOI_PHAN_ANH = "NguoiPhanAnh"
        val SDT_PHAN_ANH = "SDTPhanAnh"
        val TG_PHAN_ANH = "TGPhanAnh"
        val DOI_QUAN_LY = "DoiQuanLy"
        val HINH_THUC_PHAT_HIEN = "HinhThucPhatHien"
        val TRANG_THAI = "TrangThai"
        val THONG_TIN_PHAN_ANH = "ThongTinPhanAnh"
        val TG_KHAC_PHUC = "TGKhacPhuc"
        val NHOM_KHAC_PHUC = "NhomKhacPhuc"
        val PHAN_LOAI_SU_CO = "PhanLoaiSuCo"
        val DIA_CHI = "DiaChi"
        val MA_DUONG = "MaDuong"
        val MA_QUAN = "MaQuan"
        val MA_PHUONG = "MaPhuong"
        val MA_DMA = "MaDMA"
        val LOAI_SU_CO = "LoaiSuCo"
        val VAT_LIEU = "VatLieu"
        val NGUYEN_NHAN = "NguyenNhan"
        val DUONG_KINH_ONG = "DuongKinhOng"
        val AP_LUC = "ApLuc"
        val DO_SAU_LUNG_ONG = "DoSauLungOng"
        val GHI_CHU = "GhiChu"

    }

    object TrangThaiSuCo {
        val CHUA_XU_LY: Short = 0
        val DANG_XU_LY: Short = 1
        val HOAN_THANH: Short = 3

    }

    object DiaBan {
        val QUAN_5 = "Quận 5"
        val QUAN_6 = "Quận 6"
        val QUAN_8 = "Quận 8"
        val QUAN_BINH_TAN = "Bình Tân"

    }

    object ThongTinPhanAnh {
        val KHAC: Short = 0
        val KHONG_NUOC: Short = 1
        val NUOC_DUC: Short = 2
        val NUOC_YEU: Short = 3
        val XI_DHN: Short = 4
        val HU_VAN: Short = 5
        val ONG_BE: Short = 6

    }

    object URL_API {
        val CHECK_VERSION = "$SERVER_FOR_API/versioning/QLSC?version=%s"
        val ADD_FEATURE = "$SERVER_API/QuanLySuCo/TiepNhanSuCo/%s"
        val LOGIN = "$SERVER_API/Login"
        val PROFILE = "$SERVER_API/Account/Profile"
        val GENERATE_ID_SUCO = "$SERVER_API/QuanLySuCo/GenerateIDSuCo"
        val LAYER_INFO = "$SERVER_API/Account/layerinfo"
        val CHANGE_PASSWORD = "$SERVER_API/Account/changepass"
        val COMPLETE = "$SERVER_API/quanlysuco/xacnhanhoanthanhnhanvien?id=%s"
        val IS_ACCESS = "$SERVER_API/Account/IsAccess/m_qlsc"
        val GENERATE_ID_SUCOTHONGTIN = "$SERVER_API/QuanLySuCo/GenerateIDSuCoThongTin/"
        val QUERY_HANH_CHINH = "$SERVER_FOR_API/hanhchinh/getbypoint"


    }

    init {
        API_LOGIN = "$SERVER_API/Login"
    }

    init {
        DISPLAY_NAME = "$SERVER_API/Account/Profile"
    }

    init {
        LAYER_INFO = "$SERVER_API/layerinfo"
    }

    init {
        GENERATE_ID_SUCO = "$SERVER_API/quanlysuco/generateidsuco/"
    }

    init {
        IS_ACCESS = "$SERVER_API/Account/IsAccess/m_qlsc"
    }

    object HOSOVATTUSUCO_METHOD {
        val FIND = 0
        val INSERT = 2
    }

    object ACCOUNT_ROLE {
        val QLCN1 = "qlcn1"
        val QLCN2 = "qlcn2"
    }

    companion object {
        @SuppressLint("SimpleDateFormat")
        val DATE_FORMAT = SimpleDateFormat("dd_MM_yyyy")
        @SuppressLint("SimpleDateFormat")
        val DATE_FORMAT_VIEW = SimpleDateFormat("HH:mm:ss dd/MM/yyyy")
        val DEFINITION_HIDE_COMPLETE = " TrangThai <> 3"
        val SERVER = "https://gis.capnuoccholon.com.vn"
        //    public static final String SERVER_FOR_API = SERVER + "/cholon";
        val SERVER_FOR_API = "http://cholon.sawagis.vn/api"
        val SERVER_API = "$SERVER_FOR_API/api"
        val NULL = ""

        val OPTION_IMAGE_LIST: List<String> = object : ArrayList<String>() {
            init {
                add(OptionAddImage.CAPTURE)
                add(OptionAddImage.PICK)
            }
        }
        val QUERY_BY_OBJECTID = Field.OBJECTID + " = %d"
        val QUERY_BY_SUCOID = FieldSuCo.ID_SUCO + " = '%s'"

        val SPATIAL_REFERENCE_VN2000 = SpatialReference.create("PROJCS[\"TPHCM_VN2000\",GEOGCS[\"GCS_VN_2000\",DATUM[\"D_Vietnam_2000\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Transverse_Mercator\"],PARAMETER[\"False_Easting\",500000.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",105.75],PARAMETER[\"Scale_Factor\",0.9999],PARAMETER[\"Latitude_Of_Origin\",0.0],UNIT[\"Meter\",1.0]]")
        val REQUEST_PERMISSIONS = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val MAX_SCALE_IMAGE_WITH_LABLES = 18000.0


        private var mInstance: Constant? = null

        val instance: Constant
            get() {
                if (mInstance == null)
                    mInstance = Constant()
                return mInstance!!
            }
    }


}
