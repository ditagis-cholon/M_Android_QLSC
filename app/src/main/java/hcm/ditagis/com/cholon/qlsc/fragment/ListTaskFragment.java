package hcm.ditagis.com.cholon.qlsc.fragment;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import hcm.ditagis.com.cholon.qlsc.ListTaskActivity;
import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.adapter.TraCuuAdapter;
import hcm.ditagis.com.cholon.qlsc.async.QueryServiceFeatureTableGetListAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;

@SuppressLint("ValidFragment")
public class ListTaskFragment extends Fragment {
    private View mRootView;
    ListView mLstChuaXuLy;
    ListView mLstDangXuLy;
    ListView mLstHoanThanh;
    TextView mTxtChuaXuLy;
    TextView mTxtDangXuLy;
    TextView mTxtHoanThanh;
    private ListTaskActivity mActivity;
    private DApplication mApplication;
    TraCuuAdapter mAdapterChuaXuLy, mAdapterDangXuLy, mAdapterHoanThanh;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ValidFragment")
    public ListTaskFragment(ListTaskActivity activity, LayoutInflater inflater) {
        this.mActivity = activity;
        mApplication = (DApplication) activity.getApplication();
        mRootView = inflater.inflate(R.layout.fragment_list_task_list, null);

        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {
        mLstChuaXuLy = mRootView.findViewById(R.id.lst_list_task_chua_xu_ly);
        mLstDangXuLy = mRootView.findViewById(R.id.lst_list_task_dang_xu_ly);
        mLstHoanThanh = mRootView.findViewById(R.id.lst_list_task_da_hoan_thanh);

        mTxtChuaXuLy = mRootView.findViewById(R.id.txt_list_task_chua_xu_ly);
        mTxtDangXuLy = mRootView.findViewById(R.id.txt_list_task_dang_xu_ly);
        mTxtHoanThanh = mRootView.findViewById(R.id.txt_list_task_hoan_thanh);
        mTxtChuaXuLy.setOnClickListener(this::onClick);
        mTxtDangXuLy.setOnClickListener(this::onClick);
        mTxtHoanThanh.setOnClickListener(this::onClick);

        mAdapterChuaXuLy = new TraCuuAdapter(mActivity.getApplicationContext(), new ArrayList<>());
        mAdapterDangXuLy = new TraCuuAdapter(mActivity.getApplicationContext(), new ArrayList<>());
        mAdapterHoanThanh = new TraCuuAdapter(mActivity.getApplicationContext(), new ArrayList<>());

        mLstChuaXuLy.setAdapter(mAdapterChuaXuLy);
        mLstDangXuLy.setAdapter(mAdapterDangXuLy);
        mLstHoanThanh.setAdapter(mAdapterHoanThanh);

        mLstChuaXuLy.setOnItemClickListener((adapterView, view, i, l) -> {
            mActivity.itemClick(adapterView, i);
        });
        mLstDangXuLy.setOnItemClickListener((adapterView, view, i, l) -> {
            mActivity.itemClick(adapterView, i);
        });
        mLstHoanThanh.setOnItemClickListener((adapterView, view, i, l) -> {
//            Toast.makeText(mActivity.getApplicationContext(), R.string.message_click_feature_complete,
//                    Toast.LENGTH_SHORT).show();
            mActivity.itemClick(adapterView, i);
        });
        new QueryServiceFeatureTableGetListAsync(mActivity, mApplication.getServiceFeatureTable(), (List<Feature> output) -> {
            if (output != null && output.size() > 0) {
                handlingQuerySuccess(output);
            }
            mAdapterChuaXuLy.notifyDataSetChanged();
            mAdapterDangXuLy.notifyDataSetChanged();
            mAdapterHoanThanh.notifyDataSetChanged();

            mTxtChuaXuLy.setText(mActivity.getResources().getString(R.string.txt_list_task_chua_xu_ly, mAdapterChuaXuLy.getCount()));
            mTxtDangXuLy.setText(mActivity.getResources().getString(R.string.txt_list_task_dang_xu_ly, mAdapterDangXuLy.getCount()));
            mTxtHoanThanh.setText(mActivity.getResources().getString(R.string.txt_list_task_hoan_thanh, mAdapterHoanThanh.getCount()));
        }).execute();
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    private void handlingQuerySuccess(List<Feature> output) {
        try {
            List<TraCuuAdapter.Item> chuaXuLyList = new ArrayList<>();
            List<TraCuuAdapter.Item> dangXuLyList = new ArrayList<>();
            List<TraCuuAdapter.Item> hoanThanhList = new ArrayList<>();
            for (Feature feature : output) {
                Map<String, Object> attributes = feature.getAttributes();
                Constant.DateFormat.DATE_FORMAT_VIEW.setTimeZone(TimeZone.getTimeZone("UTC"));
                TraCuuAdapter.Item item = new TraCuuAdapter.Item(Integer.parseInt(attributes.get(Constant.FIELD_SUCO.OBJECT_ID).toString()),
                        attributes.get(Constant.FIELD_SUCO.ID_SUCO).toString(),
                        Integer.parseInt(attributes.get(Constant.FIELD_SUCO.TRANG_THAI).toString()),
                        Constant.DateFormat.DATE_FORMAT_VIEW.format(((Calendar) attributes.get(Constant.FIELD_SUCO.NGAY_XAY_RA)).getTime()),
                        attributes.get(Constant.FIELD_SUCO.VI_TRI) == null ? "" : attributes.get(Constant.FIELD_SUCO.VI_TRI).toString());
                Object value = feature.getAttributes().get(Constant.FIELD_SUCO.TRANG_THAI);
                if (value == null) {
                    chuaXuLyList.add(item);
                } else {
                    short trangThai = Short.parseShort(value.toString());
                    switch (trangThai) {
                        case Constant.TrangThaiSuCo.CHUA_XU_LY:
                            chuaXuLyList.add(item);
                            break;
                        case Constant.TrangThaiSuCo.DANG_XU_LY:
                            dangXuLyList.add(item);
                            break;
                        case Constant.TrangThaiSuCo.HOAN_THANH:
                            hoanThanhList.add(item);
                            break;
                    }
                }
            }
            Comparator<TraCuuAdapter.Item> comparator = (TraCuuAdapter.Item o1, TraCuuAdapter.Item o2) -> {
                try {
                    Constant.DateFormat.DATE_FORMAT_VIEW.setTimeZone(TimeZone.getTimeZone("UTC"));
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
            chuaXuLyList.sort(comparator);
            dangXuLyList.sort(comparator);
            hoanThanhList.sort(comparator);
            mAdapterChuaXuLy.addAll(chuaXuLyList);
            mAdapterDangXuLy.addAll(dangXuLyList);
            mAdapterHoanThanh.addAll(hoanThanhList);
        } catch (Exception e) {
            Log.e("Lỗi lấy ds công việc", e.toString());
        }
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_list_task_chua_xu_ly:
                if (mLstChuaXuLy.getVisibility() == View.VISIBLE)
                    mLstChuaXuLy.setVisibility(View.GONE);
                else mLstChuaXuLy.setVisibility(View.VISIBLE);
                break;
            case R.id.txt_list_task_dang_xu_ly:
                if (mLstDangXuLy.getVisibility() == View.VISIBLE)
                    mLstDangXuLy.setVisibility(View.GONE);
                else mLstDangXuLy.setVisibility(View.VISIBLE);
                break;
            case R.id.txt_list_task_hoan_thanh:
                if (mLstHoanThanh.getVisibility() == View.VISIBLE)
                    mLstHoanThanh.setVisibility(View.GONE);
                else mLstHoanThanh.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mRootView;
    }
}
