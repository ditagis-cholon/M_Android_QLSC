package hcm.ditagis.com.cholon.qlsc.fragment.update;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.Attachment;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import hcm.ditagis.com.cholon.qlsc.R;
import hcm.ditagis.com.cholon.qlsc.UpdateActivity;
import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewMoreInfoAttachmentsAdapter;
import hcm.ditagis.com.cholon.qlsc.adapter.OptionAddImageAdapter;
import hcm.ditagis.com.cholon.qlsc.async.FetchAttachmentAsync;
import hcm.ditagis.com.cholon.qlsc.async.GetAttachmentsAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.utities.Constant;

@SuppressLint("ValidFragment")
public class UpdateAttachment extends Fragment {
    private View mRootView;
    private DApplication mApplication;
    private Uri mUri;

    Button mBtnAddImage;
    LinearLayout mLLayoutMain;
    ListView mListView;
    LinearLayout mLLayoutProgress;
    TextView mTxtProgress;
    SwipeRefreshLayout mmSwipe;
    private UpdateActivity mActivity;
    private List<Attachment> mAttachments;
    private FeatureViewMoreInfoAttachmentsAdapter mAdapter;

    @SuppressLint("ValidFragment")
    public UpdateAttachment(UpdateActivity activity, final LayoutInflater inflater) {
        mRootView = inflater.inflate(R.layout.fragment_update_attachment, null);
        mActivity = activity;
        mApplication = (DApplication) activity.getApplication();
        initViews();
    }

    private void initViews() {
        mBtnAddImage = mRootView.findViewById(R.id.btn_update_attachment_capture);
        mLLayoutMain = mRootView.findViewById(R.id.llayout_update_attachment_main);
        mListView = mRootView.findViewById(R.id.list_update_attachment);
        mLLayoutProgress = mRootView.findViewById(R.id.llayout_update_attachment_progress);
        mTxtProgress = mRootView.findViewById(R.id.txt_update_attachment_progress);

        mBtnAddImage.setOnClickListener(this::onClick);
        mmSwipe = mRootView.findViewById(R.id.swipe_udpate_attachment);
        mAdapter = new FeatureViewMoreInfoAttachmentsAdapter(mRootView.getContext(), new ArrayList<>());
        mListView.setAdapter(mAdapter);
        mListView.setOnItemLongClickListener((AdapterView<?> adapterView, View view, int i, long l) -> {
            FeatureViewMoreInfoAttachmentsAdapter.Item item = (FeatureViewMoreInfoAttachmentsAdapter.Item) adapterView.getItemAtPosition(i);
            String[] name = item.getName().split("_");
            if (name.length > 1 && name[1].equals(mApplication.getUserDangNhap().getUserName())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mRootView.getContext(), R.style.DDialogBuilder);
                builder.setTitle("Bạn có chắc muốn xóa ảnh này?")
                        .setPositiveButton("Xoá", (dialogInterface, i1) -> {
                            mApplication.getSelectedArcGISFeature().deleteAttachmentAsync(mAttachments.get(i)).addDoneListener(() -> {
                                ListenableFuture<List<FeatureEditResult>> listListenableFuture =
                                        ((ServiceFeatureTable) mApplication.getSelectedArcGISFeature().getFeatureTable()).applyEditsAsync();
                                listListenableFuture.addDoneListener(() -> {
                                    try {
                                        List<FeatureEditResult> featureEditResults = listListenableFuture.get();
                                        mAdapter.remove(item);
                                        mAdapter.notifyDataSetChanged();
                                        Toast.makeText(mRootView.getContext(), "Xóa thành công", Toast.LENGTH_SHORT).show();
                                    } catch (InterruptedException | ExecutionException e) {
                                        Toast.makeText(mRootView.getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                                        e.printStackTrace();
                                    }
                                });

                            });
                            dialogInterface.dismiss();
                        }).setNegativeButton("Hủy", (dialogInterface, i12) -> {
                    dialogInterface.dismiss();
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                Toast.makeText(mRootView.getContext(), "Bạn không có quyền xóa ảnh này", Toast.LENGTH_SHORT).show();
            }
            return false;
        });

        mmSwipe.setOnRefreshListener(() -> {
            loadImages();
            mmSwipe.setRefreshing(false);
        });
        loadImages();
    }

    public void startProgress() {
        mAdapter.clear();
        mLLayoutProgress.setVisibility(View.VISIBLE);
        mLLayoutMain.setVisibility(View.GONE);
    }

    public void stopProgress() {
        Toast.makeText(mRootView.getContext(), "Nhấn và giữ ảnh cần xóa trong 2 giây để xóa", Toast.LENGTH_LONG).show();
        mAdapter.notifyDataSetChanged();
        mLLayoutProgress.setVisibility(View.GONE);
        mLLayoutMain.setVisibility(View.VISIBLE);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadImages() {
        startProgress();
        new GetAttachmentsAsync(attachments -> {
            if (attachments != null) {
                mAttachments = attachments;
                AtomicInteger size = new AtomicInteger(attachments.size());
                if (size.get() == 0) {
                    stopProgress();

                } else
                    for (Attachment attachment : attachments) {
                        new FetchAttachmentAsync(bitmap -> {
                            size.decrementAndGet();
                            if (size.get() == 0) {
                                stopProgress();
                            }
                            if (bitmap != null) {
                                mAdapter.add(new FeatureViewMoreInfoAttachmentsAdapter.Item(attachment.getName(), bitmap));

                            }
                        }).execute(attachment);
                    }
            } else {

                Toast.makeText(mRootView.getContext(), "Không có tệp đính kèm", Toast.LENGTH_SHORT).show();
            }

        }).execute(mApplication.getSelectedArcGISFeature());

    }

    public void handlingCaptureDone(ArcGISFeature arcGISFeature) {
        loadImages();
        if (arcGISFeature != null) {
            Toast.makeText(mRootView.getContext(), "Đã lưu ảnh", Toast.LENGTH_SHORT).show();

        } else
            Toast.makeText(mRootView.getContext(), "Thêm ảnh thất bại", Toast.LENGTH_SHORT).show();
    }


    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_update_attachment_capture:
                LinearLayout layout = (LinearLayout) mActivity.getLayoutInflater().inflate(R.layout.layout_list_option, null);
                ListView listView = layout.findViewById(R.id.lst_list_option);
                listView.setAdapter(new OptionAddImageAdapter(mRootView.getContext(), Constant.OPTION_IMAGE_LIST));

                BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(mRootView.getContext());
                bottomSheetDialog.setContentView(layout);

                listView.setOnItemClickListener((adapterView, view1, i, l) -> {
                    String item = (String) adapterView.getItemAtPosition(i);
                    switch (item) {
                        case Constant.OptionAddImage.CAPTURE:
                            mLLayoutProgress.setVisibility(View.VISIBLE);
                            mLLayoutMain.setVisibility(View.GONE);
                            mActivity.capture();
                            break;
                        case Constant.OptionAddImage.PICK:
                            mActivity.pickPhoto();
                            break;
                    }
                    bottomSheetDialog.dismiss();
                });
                bottomSheetDialog.show();
                break;
        }

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return mRootView;
    }
}
