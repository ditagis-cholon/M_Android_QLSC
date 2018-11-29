package hcm.ditagis.com.cholon.qlsc.fragment.task;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Domain;
import com.esri.arcgisruntime.data.Feature;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import hcm.ditagis.com.cholon.qlsc.ListTaskActivity;
import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.adapter.TraCuuAdapter;
import hcm.ditagis.com.cholon.qlsc.async.QueryFeatureAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;


@SuppressLint("ValidFragment")
public class SearchFragment extends Fragment {
    private View mRootView;
    private ListTaskActivity mActivity;
    private EditText mEtxtAddress;
    private Spinner mSpinTrangThai;
    private TextView mTxtThoiGian;
    private Button mBtnSearch;
    private ListView mLstKetQua;
    private LinearLayout mLayoutKetQua;

    private DApplication mApplication;
    private List<CodedValue> mCodeValues;
    private List<Feature> mFeaturesResult;

    @SuppressLint("ValidFragment")
    public SearchFragment(ListTaskActivity activity, final LayoutInflater inflater) {
        mRootView = inflater.inflate(R.layout.fragment_list_task_search, null);
        this.mActivity = activity;
        mApplication = (DApplication) activity.getApplication();
        init();
    }

    private void init() {
        mEtxtAddress = mRootView.findViewById(R.id.etxt_list_task_search_address);
        mSpinTrangThai = mRootView.findViewById(R.id.spin_list_task_search_trang_thai);
        mTxtThoiGian = mRootView.findViewById(R.id.txt_list_task_search_thoi_gian);
        mBtnSearch = mRootView.findViewById(R.id.btn_list_task_search);
        mLstKetQua = mRootView.findViewById(R.id.lst_list_task_search);
        mLayoutKetQua = mRootView.findViewById(R.id.llayout_list_task_search_ket_qua);

        mBtnSearch.setOnClickListener(this::onClick);
        mTxtThoiGian.setOnClickListener(this::onClick);
        initSpinTrangThai();
        initListViewKetQuaTraCuu();
    }

    private void initSpinTrangThai() {
        Domain domain = mApplication.getDFeatureLayer().getLayer().getFeatureTable().getField(Constant.FieldSuCo.TRANG_THAI).getDomain();
        if (domain != null) {
            mCodeValues = ((CodedValueDomain) domain).getCodedValues();
            if (mCodeValues != null) {
                List<String> codes = new ArrayList<>();
                codes.add("Tất cả");
                for (CodedValue codedValue : mCodeValues)
                    if (!Constant.DEFINITION_HIDE_COMPLETE.contains(codedValue.getCode().toString()))
                        codes.add(codedValue.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(mRootView.getContext(), android.R.layout.simple_list_item_1, codes);
                mSpinTrangThai.setAdapter(adapter);
            }
        }
    }

    private void initListViewKetQuaTraCuu() {
        mLstKetQua.setOnItemClickListener((adapterView, view, i, l) -> {
            mActivity.itemClick(adapterView, i);
        });
    }

    private void showDateTimePicker() {
        final View dialogView = View.inflate(mRootView.getContext(), R.layout.date_time_picker, null);
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(mRootView.getContext()).create();
        dialogView.findViewById(R.id.date_time_set).setOnClickListener(view -> {
            DatePicker datePicker = dialogView.findViewById(R.id.date_picker);
            Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
            String displaytime = (String) DateFormat.format((Constant.DateFormat.DATE_FORMAT_STRING), calendar.getTime());
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormatGmt = Constant.DateFormat.DATE_FORMAT_YEAR_FIRST;
//            dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
            mTxtThoiGian.setText(displaytime);
            alertDialog.dismiss();
        });
        alertDialog.setView(dialogView);
        alertDialog.show();

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void traCuu() {
//        if (!mTxtThoiGian.getText().toString().equals(mRootView.getContext().getString(R.string.txt_chon_thoi_gian_tracuusuco))) {
        mLayoutKetQua.setVisibility(View.GONE);
        short trangThai = -1;
        if (mCodeValues != null)
            for (CodedValue codedValue : mCodeValues) {
                if (codedValue.getName().equals(mSpinTrangThai.getSelectedItem().toString())) {
                    trangThai = Short.parseShort(codedValue.getCode().toString());
                }
            }
        new QueryFeatureAsync(mActivity, trangThai,
                mEtxtAddress.getText().toString(),
                mTxtThoiGian.getText().toString(), output -> {
            if (output != null && output.size() > 0) {
                mFeaturesResult = output;
                handlingTraCuuHoanTat();
            }
        }).execute();
//        } else
//            Toast.makeText(mRootView.getContext(), "Vui lòng chọn thời gian", Toast.LENGTH_SHORT).show();
    }

    private Object getValueDomain(List<CodedValue> codedValues, Object code) {
        Object value = null;
        for (CodedValue codedValue : codedValues) {
            if (codedValue.getCode().equals(code)) {
                value = codedValue.getName();
                break;
            }
        }
        return value;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handlingTraCuuHoanTat() {
        List<TraCuuAdapter.Item> items = new ArrayList<>();
        for (Feature feature : mFeaturesResult) {
            Map<String, Object> attributes = feature.getAttributes();
            for (CodedValue codedValue : mCodeValues) {
                if (Short.parseShort(codedValue.getCode().toString()) ==
                        Short.parseShort(attributes.get(Constant.FieldSuCo.TRANG_THAI).toString())) {
                    Object idSuCo = attributes.get(Constant.FieldSuCo.ID_SUCO);
                    Object ngayXayRa = attributes.get(Constant.FieldSuCo.TG_PHAN_ANH);
                    Object thongTinPhanAnhCode = attributes.get(Constant.FieldSuCo.THONG_TIN_PHAN_ANH);
                    List<CodedValue> codedValues = ((CodedValueDomain) feature.getFeatureTable().getField(Constant.FieldSuCo.THONG_TIN_PHAN_ANH).getDomain()).getCodedValues();
                    Object thongTinPhanAnhValue = thongTinPhanAnhCode == null ? null : getValueDomain(codedValues, thongTinPhanAnhCode);
                    items.add(new TraCuuAdapter.Item(Integer.parseInt(attributes.get(Constant.Field.OBJECTID).toString()),
                            idSuCo != null ? idSuCo.toString() : "",
                            ngayXayRa != null ? Constant.DateFormat.DATE_FORMAT_VIEW.format(((Calendar) ngayXayRa).getTime()) : "",
                            attributes.get(Constant.FieldSuCo.DIA_CHI)!= null? attributes.get(Constant.FieldSuCo.DIA_CHI).toString():"",
                            thongTinPhanAnhCode != null ? Short.parseShort(thongTinPhanAnhCode.toString()) : Constant.ThongTinPhanAnh.KHAC,
                            thongTinPhanAnhValue != null ? thongTinPhanAnhValue.toString() : ""));
                }
            }


        }
        Comparator<TraCuuAdapter.Item> comparator = (TraCuuAdapter.Item o1, TraCuuAdapter.Item o2) -> {
            try {
//                Constant.DateFormat.DATE_FORMAT_VIEW.setTimeZone(TimeZone.getTimeZone("UTC"));
                long i = Constant.DateFormat.DATE_FORMAT_VIEW.parse(o2.getNgayThongBao()).getTime() -
                        Constant.DateFormat.DATE_FORMAT_VIEW.parse(o1.getNgayThongBao()).getTime();
                if (i > 0)
                    return 1;
                else if (i == 0)
                    return 0;
                else return -1;
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return 0;
        };
        items.sort(comparator);
        TraCuuAdapter adapter = new TraCuuAdapter(mRootView.getContext(), items);
        mLstKetQua.setAdapter(adapter);
        mLayoutKetQua.setVisibility(View.VISIBLE);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mRootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_list_task_search:
                traCuu();
                break;
            case R.id.txt_list_task_search_thoi_gian:
                showDateTimePicker();
                break;
        }
    }
}
