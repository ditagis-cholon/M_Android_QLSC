package hcm.ditagis.com.cholon.qlsc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView

import com.elconfidencial.bubbleshowcase.BubbleShowCaseBuilder
import com.elconfidencial.bubbleshowcase.BubbleShowCaseSequence

import hcm.ditagis.com.cholon.qlsc.async.LoginAsycn
import hcm.ditagis.com.cholon.qlsc.entities.DApplication
import hcm.ditagis.com.cholon.qlsc.entities.entitiesDB.User
import hcm.ditagis.com.cholon.qlsc.utities.CheckConnectInternet
import hcm.ditagis.com.cholon.qlsc.utities.Preference

class LogInActivity : AppCompatActivity(), View.OnClickListener {
    private var mTxtUsername: TextView? = null
    private var mTxtPassword: TextView? = null
    private var mTxtValidation: TextView? = null
    private var mApplication: DApplication? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mApplication = application as DApplication
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener(this)

        mTxtUsername = findViewById(R.id.txtUsername)
        mTxtPassword = findViewById(R.id.txtPassword)
        //        mTxtUsername.setText("qlcn1");
        //        mTxtPassword.setText("qlcn1@2018");
        mTxtValidation = findViewById(R.id.txt_login_validation)

        (findViewById<View>(R.id.txt_login_version) as TextView).text = "Phiên bản " + BuildConfig.VERSION_NAME
        create()
    }

    private fun create() {
        Preference.instance.setContext(this)
        val preference_userName = Preference.instance.loadPreference(getString(R.string.preference_username))

        //nếu chưa từng đăng nhập thành công trước đó
        //nhập username và password bình thường
        if (preference_userName == null || preference_userName!!.isEmpty()) {
        } else {
            mTxtUsername!!.setText(Preference.instance.loadPreference(getString(R.string.preference_username)))
        }//ngược lại
        //chỉ nhập pasword
        val builderUsername = BubbleShowCaseBuilder(this@LogInActivity)
                .title("Nhập tên đăng nhập")
                .targetView(findViewById(R.id.layout_login_username))
        val builderPassword = BubbleShowCaseBuilder(this@LogInActivity)
                .title("Nhập mật khẩu")
                .targetView(findViewById(R.id.layout_login_password))
        val builderLogin = BubbleShowCaseBuilder(this@LogInActivity)
                .title("Nhấn nút đăng nhập")
                .targetView(findViewById(R.id.btnLogin))
        val bubbleShowCaseSequence = BubbleShowCaseSequence()
        bubbleShowCaseSequence.addShowCase(builderUsername)
        bubbleShowCaseSequence.addShowCase(builderPassword)
        bubbleShowCaseSequence.addShowCase(builderLogin)

    }



    private fun login() {
        if (!CheckConnectInternet.isOnline(this)) {
            mTxtValidation!!.setText(R.string.validate_no_connect)
            mTxtValidation!!.visibility = View.VISIBLE
            return
        }
        mTxtValidation!!.visibility = View.GONE

        val userName = mTxtUsername!!.text.toString().trim { it <= ' ' }
        val passWord = mTxtPassword!!.text.toString().trim { it <= ' ' }
        if (userName.isEmpty() || passWord.isEmpty()) {
            handleInfoLoginEmpty()
            return
        }
        val loginAsycn = LoginAsycn(this,object: LoginAsycn.AsyncResponse{
            override fun processFinish(output: User?) {
                Preference.instance.deletePreferences()
                if (output != null)
                    handleLoginSuccess(output)
                else
                    handleLoginFail()
            }
        })
        loginAsycn.execute(userName, passWord)
    }

    private fun handleInfoLoginEmpty() {
        mTxtValidation!!.setText(R.string.info_login_empty)
        mTxtValidation!!.visibility = View.VISIBLE
    }

    private fun handleLoginFail() {
        mTxtValidation!!.setText(R.string.validate_login_fail)
        mTxtValidation!!.visibility = View.VISIBLE
    }

    private fun handleLoginSuccess(user: User?) {
        mApplication!!.userDangNhap = user

        Preference.instance.savePreferences(getString(R.string.preference_username), mTxtUsername!!.text.toString())
        mTxtUsername!!.text = ""
        mTxtPassword!!.text = ""

        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onPostResume() {
        super.onPostResume()
        create()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogin -> login()
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                if (mTxtPassword!!.text.toString().trim { it <= ' ' }.length > 0) {
                    login()
                    return true
                }
                return super.onKeyUp(keyCode, event)
            }
            else -> return super.onKeyUp(keyCode, event)
        }
    }
}
