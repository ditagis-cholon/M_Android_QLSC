package hcm.ditagis.com.cholon.qlsc;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import hcm.ditagis.com.cholon.qlsc.adapter.FeatureViewMoreInfoAttachmentsAdapter;
import hcm.ditagis.com.cholon.qlsc.async.GetAttachmentsAsync;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;

public class AttachmentActivity extends AppCompatActivity {
    private ListView mLstViewAttachment;
    private LinearLayout mLayoutProgress;
    private DApplication mApplication;
    private TextView mTxtMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attachment);
        mLstViewAttachment = findViewById(R.id.lstView_alertdialog_attachments);
        mLayoutProgress = findViewById(R.id.llayout_attachment_progress);
        mTxtMessage = findViewById(R.id.txt_attachment_message);

        mApplication = (DApplication) getApplication();
        final FeatureViewMoreInfoAttachmentsAdapter adapter =
                new FeatureViewMoreInfoAttachmentsAdapter(this, new ArrayList<>());
        mLstViewAttachment.setAdapter(adapter);

        new GetAttachmentsAsync( mApplication.getSelectedArcGISFeature(), items -> {
            mLayoutProgress.setVisibility(View.GONE);
            if (items != null && items.size() > 0) {
                mLstViewAttachment.setVisibility(View.VISIBLE);
                adapter.addAll(items);
                adapter.notifyDataSetChanged();
            } else {
                mTxtMessage.setVisibility(View.VISIBLE);
            }
        }).execute();
    }
}
