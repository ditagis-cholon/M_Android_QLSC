package hcm.ditagis.com.cholon.qlsc.fragment.task;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.QueryParameters;

import java.util.List;

import hcm.ditagis.com.cholon.qlsc.ListTaskActivity;
import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.async.QueryServiceFeatureTableGetListAsync;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;

@SuppressLint("ValidFragment")
public class ListTaskFragment extends Fragment {
    LinearLayout mLLayoutChuaXuLy;
    TextView mTxtChuaXuLy;
    private View mRootView;
    private ListTaskActivity mActivity;
    private SwipeRefreshLayout mSwipe;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ValidFragment")
    public ListTaskFragment(ListTaskActivity activity, LayoutInflater inflater) {
        this.mActivity = activity;
        mRootView = inflater.inflate(R.layout.fragment_list_task_list, null);

        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void init() {
        mSwipe = mRootView.findViewById(R.id.swipe_list_task);
        mLLayoutChuaXuLy = mRootView.findViewById(R.id.llayout_list_task_chua_xu_ly);
        mTxtChuaXuLy = mRootView.findViewById(R.id.txt_list_task_chua_xu_ly);
        mTxtChuaXuLy.setOnClickListener(this::onClick);

        mSwipe.setOnRefreshListener(() -> {
            loadTasks();
            mSwipe.setRefreshing(false);
        });
        loadTasks();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void loadTasks() {
        mLLayoutChuaXuLy.removeAllViews();
        QueryParameters queryParameters = new QueryParameters();
        String queryClause = String.format("%s = %d", Constant.FieldSuCo.TRANG_THAI, Constant.TrangThaiSuCo.CHUA_XU_LY);
        queryParameters.setWhereClause(queryClause);
        new QueryServiceFeatureTableGetListAsync(mActivity, (List<Feature> output) -> {
            if (output != null && output.size() > 0) {
                List<View> views = HandlingSearchHasDone.handleFromFeatures(mActivity, mRootView.getContext(), output);
                for (View view : views) {
                    TextView txtID = view.findViewById(R.id.txt_top);
                    view.setOnClickListener(v -> {
                        mActivity.itemClick(txtID.getText().toString());
                    });
                    mLLayoutChuaXuLy.addView(view);
                }
            }
            mTxtChuaXuLy.setText(mActivity.getResources().getString(R.string.txt_list_task_chua_xu_ly, mLLayoutChuaXuLy.getChildCount()));
        }).execute(queryParameters);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_list_task_chua_xu_ly:
                if (mLLayoutChuaXuLy.getVisibility() == View.VISIBLE)
                    mLLayoutChuaXuLy.setVisibility(View.GONE);
                else mLLayoutChuaXuLy.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mRootView;
    }
}
