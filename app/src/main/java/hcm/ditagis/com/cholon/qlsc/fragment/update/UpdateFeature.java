package hcm.ditagis.com.cholon.qlsc.fragment.update;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.data.ArcGISFeature;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.UpdateActivity;
import hcm.ditagis.com.cholon.qlsc.async.EditAsync;
import hcm.ditagis.com.cholon.qlsc.async.LoadingDataFeatureAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;

@SuppressLint("ValidFragment")
public class UpdateFeature extends Fragment {
    private View mRootView;
    private DApplication mApplication;
    private Uri mUri;

    Button mBtnUpdate;
    Button mBtnComplete;
    LinearLayout mLLayoutMain;
    LinearLayout mLLayoutField;
    LinearLayout mLLayoutProgress;
    TextView mTxtProgress;
    private SwipeRefreshLayout mmSwipe;
    private UpdateActivity mActivity;
    private ArcGISFeature mArcGISFeature;

    @SuppressLint("ValidFragment")
    public UpdateFeature(UpdateActivity activity, final LayoutInflater inflater) {
        mRootView = inflater.inflate(R.layout.fragment_update_feature, null);
        mApplication = (DApplication) activity.getApplication();
        this.mActivity = activity;
        initViews();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mRootView;
    }

    private void initViews() {
        mBtnComplete = mRootView.findViewById(R.id.btn_update_feature_complete);
        mBtnUpdate = mRootView.findViewById(R.id.btn_update_feature_update);
        mLLayoutMain = mRootView.findViewById(R.id.llayout_update_feature_main);
        mLLayoutField = mRootView.findViewById(R.id.llayout_update_feature_field);
        mLLayoutProgress = mRootView.findViewById(R.id.llayout_update_feature_progress);
        mTxtProgress = mRootView.findViewById(R.id.txt_update_feature_progress);

        mBtnComplete.setOnClickListener(this::onClick);
        mBtnUpdate.setOnClickListener(this::onClick);
        mmSwipe = mRootView.findViewById(R.id.swipe_udpate_feature);
        if (mApplication.getImages() != null) mApplication.getImages().clear();
        mTxtProgress.setText("Đang khởi tạo thuộc tính...");
        mLLayoutProgress.setVisibility(View.VISIBLE);
        mLLayoutMain.setVisibility(View.GONE);
        mArcGISFeature = mApplication.getSelectedArcGISFeature();


        mmSwipe.setOnRefreshListener(() -> {
            loadData();
            mmSwipe.setRefreshing(false);
        });
        loadData();
    }

    private void loadData() {
        mLLayoutField.removeAllViews();
        mLLayoutProgress.setVisibility(View.VISIBLE);
        mLLayoutMain.setVisibility(View.GONE);
        new LoadingDataFeatureAsync(mActivity, mRootView.getContext(), mArcGISFeature.getFeatureTable().getFields(), views -> {
            if (views != null)
                for (View view1 : views) {
                    mLLayoutField.addView(view1);
                }
            mLLayoutProgress.setVisibility(View.GONE);
            mLLayoutMain.setVisibility(View.VISIBLE);
        }, mArcGISFeature).execute(false);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_update_feature_complete:
                mLLayoutProgress.setVisibility(View.VISIBLE);
                mLLayoutMain.setVisibility(View.GONE);
                mTxtProgress.setText("Đang lưu...");
                EditAsync completeAsync = new EditAsync(mActivity, true,
                        mArcGISFeature, mLLayoutField, null,
                        arcGISFeature -> {
                            mLLayoutProgress.setVisibility(View.GONE);
                            mLLayoutMain.setVisibility(View.VISIBLE);
                            if (arcGISFeature != null) {
                                Toast.makeText(mRootView.getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
//                                mActivity.goHome();
                            } else
                                Toast.makeText(mRootView.getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        });
                completeAsync.execute();
                break;
            case R.id.btn_update_feature_update:
                mLLayoutProgress.setVisibility(View.VISIBLE);
                mLLayoutMain.setVisibility(View.GONE);
                mTxtProgress.setText("Đang lưu...");
                EditAsync updateAsync = new EditAsync(mActivity, false,
                        mArcGISFeature, mLLayoutField, null,
                        arcGISFeature -> {
                            mLLayoutProgress.setVisibility(View.GONE);
                            mLLayoutMain.setVisibility(View.VISIBLE);
                            if (arcGISFeature != null) {
                                Toast.makeText(mBtnUpdate.getContext(), "Cập nhật thành công", Toast.LENGTH_SHORT).show();
//                                mActivity.goHome();
                            } else
                                Toast.makeText(mBtnUpdate.getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                        });
                updateAsync.execute();
                break;
        }
    }
}
