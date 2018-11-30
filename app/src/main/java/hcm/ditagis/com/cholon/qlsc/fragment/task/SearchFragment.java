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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Domain;
import com.esri.arcgisruntime.data.Feature;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import hcm.ditagis.com.cholon.qlsc.ListTaskActivity;
import hcm.ditagis.com.cholon.qlsc.R;
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
    private TextView mTxtKetQua;
    private LinearLayout mLLayoutKetQua;
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
        mLLayoutKetQua = mRootView.findViewById(R.id.llayout_list_task_search_ket_qua);
        mTxtKetQua = mRootView.findViewById(R.id.txt_list_task_ket_qua);

        mBtnSearch.setOnClickListener(this::onClick);
        mTxtThoiGian.setOnClickListener(this::onClick);
        initSpinTrangThai();
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
        mLLayoutKetQua.removeAllViews();
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
                List<View> views = HandlingSearchHasDone.handleFromFeatures(mActivity, mRootView.getContext(), output);
                for (View view : views) {
                    TextView txtID = view.findViewById(R.id.txt_top);
                    view.setOnClickListener(v -> {
                        mActivity.itemClick(txtID.getText().toString());
                    });
                    mLLayoutKetQua.addView(view);
                }
                mTxtKetQua.setVisibility(View.VISIBLE);
                mTxtKetQua.setText(String.format("Kết quả tra cứu: %d sự cố", mLLayoutKetQua.getChildCount()));
            } else {
                mTxtKetQua.setVisibility(View.INVISIBLE);
                Toast.makeText(mRootView.getContext(), "Không có kết quả", Toast.LENGTH_SHORT).show();
            }
        }).execute();
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
