package hcm.ditagis.com.cholon.qlsc.utities;

import java.text.SimpleDateFormat;

import hcm.ditagis.com.cholon.qlsc.adapter.SettingsAdapter;

/**
 * Created by ThanLe on 3/1/2018.
 */

public class Constant {
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd_MM_yyyy");
    public static final SimpleDateFormat DATE_FORMAT_VIEW = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
//        public static final String SERVER_API = "http://gis.capnuoccholon.com.vn/cholon/api";
    private final String SERVER_API = "http://sawagis.vn/cholon/api";
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
        GENERATE_ID_SUCO = SERVER_API + "/quanlysuco/generateidsuco";
    }

    public String IS_ACCESS;

    {
        IS_ACCESS = SERVER_API + "/Account/IsAccess/m_qlsc";
    }
   public class HOSOVATTUSUCO_METHOD{
        public static final int FIND = 0;
        public static final int INSERT = 2;
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
