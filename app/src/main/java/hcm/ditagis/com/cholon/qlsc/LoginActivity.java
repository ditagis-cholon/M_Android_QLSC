package hcm.ditagis.com.cholon.qlsc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import hcm.ditagis.com.cholon.qlsc.async.LoginAsycn;
import hcm.ditagis.com.cholon.qlsc.async.NewLoginAsycn;
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.KhachHang;
import hcm.ditagis.com.cholon.qlsc.utities.CheckConnectInternet;
import hcm.ditagis.com.cholon.qlsc.utities.Preference;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private TextView mTxtUsername;
    private TextView mTxtPassword;
    private boolean isLastLogin;
    private TextView mTxtValidation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button btnLogin = (findViewById(R.id.btnLogin));
        btnLogin.setOnClickListener(this);
        findViewById(R.id.txt_login_changeAccount).setOnClickListener(this);

        mTxtUsername = findViewById(R.id.txtUsername);
        mTxtPassword = findViewById(R.id.txtPassword);
        mTxtPassword.setText("ditagis@123");
        mTxtValidation = findViewById(R.id.txt_login_validation);
        create();
    }

    private void create() {
        Preference.getInstance().setContext(this);
        String preference_userName = Preference.getInstance().loadPreference(getString(R.string.preference_username));

        //nếu chưa từng đăng nhập thành công trước đó
        //nhập username và password bình thường
        if (preference_userName == null || preference_userName.isEmpty()) {
            findViewById(R.id.layout_login_tool).setVisibility(View.GONE);
            findViewById(R.id.layout_login_username).setVisibility(View.VISIBLE);
            isLastLogin = false;
        }
        //ngược lại
        //chỉ nhập pasword
        else {
            isLastLogin = true;
            findViewById(R.id.layout_login_tool).setVisibility(View.VISIBLE);
            mTxtUsername.setVisibility(View.GONE);
        }

    }

    private void login() {
        if (!CheckConnectInternet.isOnline(this)) {
            mTxtValidation.setText(R.string.validate_no_connect);
            mTxtValidation.setVisibility(View.VISIBLE);
            return;
        }
        mTxtValidation.setVisibility(View.GONE);

        String userName;
        if (isLastLogin)
            userName = Preference.getInstance().loadPreference(getString(R.string.preference_username));
        else
            userName = mTxtUsername.getText().toString().trim();
        final String passWord = mTxtPassword.getText().toString().trim();
        if (userName.length() == 0 || passWord.length() == 0) {
            handleInfoLoginEmpty();
            return;
        }
//        handleLoginSuccess(userName,passWord);
//        final String finalUserName = userName;

//        LoginAsycn loginAsycn = new LoginAsycn(this, new LoginAsycn.AsyncResponse() {
//
//            @Override
//            public void processFinish(KhachHang output) {
//                if (output != null)
//                    handleLoginSuccess(output);
//                else
//                    handleLoginFail();
//            }
//        });
//        loginAsycn.execute(userName, passWord);
        NewLoginAsycn loginAsycn = new NewLoginAsycn(this, new LoginAsycn.AsyncResponse() {

            @Override
            public void processFinish(KhachHang output) {
                if (output != null)
                    handleLoginSuccess(output);
                else
                    handleLoginFail();
            }
        });
        loginAsycn.execute(userName, passWord);
//        if (userName.equals("cholon") && passWord.equals("123456")) {
//            KhachHang.khachHangDangNhap = new KhachHang(userName,passWord,"Cấp nước Chợ Lớn",true,true,true,true);
//            handleLoginSuccess(KhachHang.khachHangDangNhap);
//        } else
//            handleLoginFail();
    }

    private void handleInfoLoginEmpty() {
        mTxtValidation.setText(R.string.info_login_empty);
        mTxtValidation.setVisibility(View.VISIBLE);
    }

    private void handleLoginFail() {
        mTxtValidation.setText(R.string.validate_login_fail);
        mTxtValidation.setVisibility(View.VISIBLE);
    }

    private void handleLoginSuccess(KhachHang khachHang) {
        mTxtUsername.setText("");
        mTxtPassword.setText("");

        Preference.getInstance().savePreferences(getString(R.string.preference_username), khachHang.getUserName());
        Preference.getInstance().savePreferences(getString(R.string.preference_password), khachHang.getPassWord());
        Preference.getInstance().savePreferences(getString(R.string.preference_displayname), khachHang.getDisplayName());

        Intent intent = new Intent(this, QuanLySuCo.class);

        startActivity(intent);
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
            case R.id.txt_login_changeAccount:
                changeAccount();
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
