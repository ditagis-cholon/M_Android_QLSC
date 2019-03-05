package hcm.ditagis.com.cholon.qlsc;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder;
import com.elconfidencial.bubbleshowcase.BubbleShowCaseSequence;

import hcm.ditagis.com.cholon.qlsc.async.CheckVersionAsycn;
import hcm.ditagis.com.cholon.qlsc.async.LoginAsycn;
import hcm.ditagis.com.cholon.qlsc.entities.DApplication;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.User;
import hcm.ditagis.com.cholon.qlsc.utities.CheckConnectInternet;
import hcm.ditagis.com.cholon.qlsc.utities.Preference;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTxtUsername;
    private TextView mTxtPassword;
    private TextView mTxtValidation;
    private DApplication mApplication;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mApplication = (DApplication) getApplication();
        Button btnLogin = (findViewById(R.id.btnLogin));
        btnLogin.setOnClickListener(this);

        mTxtUsername = findViewById(R.id.txtUsername);
        mTxtPassword = findViewById(R.id.txtPassword);
//        mTxtUsername.setText("qlcn1");
//        mTxtPassword.setText("qlcn1@2018");
        mTxtValidation = findViewById(R.id.txt_login_validation);

        ((TextView) findViewById(R.id.txt_login_version)).setText("Phiên bản " + BuildConfig.VERSION_NAME);
        create();
    }

    private void create() {
        Preference.getInstance().setContext(this);
        String preference_userName = Preference.getInstance().loadPreference(getString(R.string.preference_username));

        //nếu chưa từng đăng nhập thành công trước đó
        //nhập username và password bình thường
        if (preference_userName == null || preference_userName.isEmpty()) {
        }
        //ngược lại
        //chỉ nhập pasword
        else {
            mTxtUsername.setText(Preference.getInstance().loadPreference(getString(R.string.preference_username)));
        }
        BubbleShowCaseBuilder builderUsername = new BubbleShowCaseBuilder(LogInActivity.this)
                .title("Nhập tên đăng nhập")
                .targetView(findViewById(R.id.layout_login_username));
        BubbleShowCaseBuilder builderPassword = new BubbleShowCaseBuilder(LogInActivity.this)
                .title("Nhập mật khẩu")
                .targetView(findViewById(R.id.layout_login_password));
        BubbleShowCaseBuilder builderLogin = new BubbleShowCaseBuilder(LogInActivity.this)
                .title("Nhấn nút đăng nhập")
                .targetView(findViewById(R.id.btnLogin));
        BubbleShowCaseSequence bubbleShowCaseSequence = new BubbleShowCaseSequence();
        bubbleShowCaseSequence.addShowCase(builderUsername);
        bubbleShowCaseSequence.addShowCase(builderPassword);
        bubbleShowCaseSequence.addShowCase(builderLogin);
        try {
            if (!mApplication.isCheckedVersion()) {
                mApplication.setCheckedVersion(true);
                new CheckVersionAsycn(this, output -> {
                    if (output != null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(LogInActivity.this, R.style.Theme_AppCompat_DayNight_Dialog_Alert);
                        builder.setCancelable(true)
                                .setPositiveButton("CẬP NHẬT", (dialogInterface, i) -> {
                                    goURLBrowser(output.getLink());
                                }).setTitle("Có phiên bản mới");
                        boolean isDeveloper = false;
                        if (!output.getType().equals("RELEASE")) {
                            int anInt = Settings.Secure.getInt(this.getContentResolver(),
                                    Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0);
                            if (anInt != 0)
                                isDeveloper = true;

                        }
                        if (isDeveloper)
                            builder.setMessage("Bạn là người phát triển ứng dụng! Bạn có muốn cập nhật lên phiên bản ".concat(output.getVersionCode()).concat("?"));
                        else
                            builder.setMessage("Bạn có muốn cập nhật lên phiên bản ".concat(output.getVersionCode().concat("?")));
                        AlertDialog dialog = builder.create();
                        dialog.show();

                    } else {
                        Toast.makeText(LogInActivity.this, "Phiên bản hiện tại là mới nhất", Toast.LENGTH_LONG).show();
                    }

//                    bubbleShowCaseSequence.show();
                }).execute(getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Toast.makeText(this, "Có lỗi xảy ra khi kiểm tra phiên bản", Toast.LENGTH_LONG).show();
        }


    }

    private void goURLBrowser(String url) {
        boolean result = false;
        if (!url.startsWith("http://") && !url.startsWith("https://"))
            url = "http://" + url;

        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

        try {
            startActivity(intent);
            result = true;
        } catch (Exception ignored) {
        }
    }

    private void login() {
        if (!CheckConnectInternet.isOnline(this)) {
            mTxtValidation.setText(R.string.validate_no_connect);
            mTxtValidation.setVisibility(View.VISIBLE);
            return;
        }
        mTxtValidation.setVisibility(View.GONE);

        String userName = mTxtUsername.getText().toString().trim();
        final String passWord = mTxtPassword.getText().toString().trim();
        if (userName.length() == 0 || passWord.length() == 0) {
            handleInfoLoginEmpty();
            return;
        }
        LoginAsycn loginAsycn = new LoginAsycn(this, output -> {
            Preference.getInstance().deletePreferences();
            if (output != null)
                handleLoginSuccess(output);
            else
                handleLoginFail();
        });
        loginAsycn.execute(userName, passWord);
    }

    private void handleInfoLoginEmpty() {
        mTxtValidation.setText(R.string.info_login_empty);
        mTxtValidation.setVisibility(View.VISIBLE);
    }

    private void handleLoginFail() {
        mTxtValidation.setText(R.string.validate_login_fail);
        mTxtValidation.setVisibility(View.VISIBLE);
    }

    private void handleLoginSuccess(User user) {
        mApplication.setUserDangNhap(user);

        Preference.getInstance().savePreferences(getString(R.string.preference_username), mTxtUsername.getText().toString());
        mTxtUsername.setText("");
        mTxtPassword.setText("");

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void changeAccount() {
        mTxtUsername.setText("");
        mTxtPassword.setText("");

        Preference.getInstance().savePreferences(getString(R.string.preference_username), "");
        create();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        create();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                login();
                break;
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_ENTER:
                if (mTxtPassword.getText().toString().trim().length() > 0) {
                    login();
                    return true;
                }
            default:
                return super.onKeyUp(keyCode, event);
        }
    }
}
