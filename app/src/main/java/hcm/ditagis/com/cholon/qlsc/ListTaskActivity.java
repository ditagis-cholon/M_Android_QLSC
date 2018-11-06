package hcm.ditagis.com.cholon.qlsc;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Objects;

import hcm.ditagis.com.cholon.qlsc.adapter.TraCuuAdapter;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.fragment.ListTaskFragment;
import hcm.ditagis.com.cholon.qlsc.fragment.SearchFragment;

public class ListTaskActivity extends AppCompatActivity {

    private ListTaskFragment mListTaskFragment;
    private SearchFragment mSearchFragment;

    private DApplication mApplication;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_task);
        mApplication = (DApplication) getApplication();
        (Objects.requireNonNull(getSupportActionBar())).setDisplayHomeAsUpEnabled(true);
        (Objects.requireNonNull(getSupportActionBar())).setDisplayShowHomeEnabled(true);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.container_basemap);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tabs_basemap);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(viewPager));
        mListTaskFragment = new ListTaskFragment(ListTaskActivity.this, getLayoutInflater());
        mSearchFragment = new SearchFragment(ListTaskActivity.this, getLayoutInflater());

        viewPager.setCurrentItem(0, true);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return mSearchFragment;
                case 1:
                    return mListTaskFragment;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
    public void itemClick(AdapterView<?> adapter, int position) {
        TraCuuAdapter.Item item = (TraCuuAdapter.Item) adapter.getItemAtPosition(position);
        LinearLayout layout = (LinearLayout) getLayoutInflater().inflate(R.layout.layout_dialog, null);
        TextView txtTitle = layout.findViewById(R.id.txt_dialog_title);
        TextView txtMessage = layout.findViewById(R.id.txt_dialog_message);
        txtTitle.setText(getString(R.string.message_title_confirm));
        txtMessage.setText(getString(R.string.message_click_list_task, item.getId()));

        AlertDialog.Builder builder = new AlertDialog.Builder(ListTaskActivity.this);
        builder.setView(layout);
        builder.setCancelable(false)
                .setPositiveButton(R.string.message_btn_ok, (dialog, i) -> {
                    mApplication.getDiemSuCo().setIdSuCo(item.getId());
                    mApplication.getDiemSuCo().setTrangThai((short) item.getTrangThai());
                    goHome();
                }).setNegativeButton(R.string.message_btn_cancel, (dialog, i) -> {
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        goHome();
    }


    public void goHome() {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }
}
