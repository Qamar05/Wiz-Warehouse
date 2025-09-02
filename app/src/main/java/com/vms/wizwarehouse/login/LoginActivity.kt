package com.vms.wizwarehouse.login

import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.vms.wizwarehouse.dashboard.DashboardActivity
import com.vms.wizwarehouse.databinding.ActivityLoginBinding
import com.vms.wizwarehouse.otp.OtpActivity
import com.vms.wizwarehouse.retrofit.ApiResponseListener
import com.vms.wizwarehouse.utils.LoaderUtils
import com.vms.wizwarehouse.utils.SharedPreferenceUtils
import com.vms.wizwarehouse.utils.Utility


class LoginActivity : AppCompatActivity() {
    private lateinit var loginBinding: ActivityLoginBinding
    private lateinit var phone: EditText
    private lateinit var submit: Button
    private lateinit var networkManager: NetworkManagerLogin
    private lateinit var version: TextView
    private lateinit var userCode: String
    private lateinit var userName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        loginBinding = ActivityLoginBinding.inflate(layoutInflater)

        userCode = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_CODE) ?: ""
        userName = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_NAME) ?: ""

        val isLoggedIn =
            SharedPreferenceUtils.getBoolean(this, SharedPreferenceUtils.IS_LOGGED_IN_WAREHOUSE)
        val isCheckedIn =
            SharedPreferenceUtils.getBoolean(this, SharedPreferenceUtils.IS_CHECKED_IN_WAREHOUSE)
        val isSupervisor =
            SharedPreferenceUtils.getBoolean(this, SharedPreferenceUtils.IS_SUPERVISOR)
        version = loginBinding.txtVersion

        version.text = Utility.setVersionName(this)

        if (isLoggedIn) {
            val intent = when {
                isCheckedIn && isSupervisor -> Intent(this, DashboardActivity::class.java)
                isCheckedIn -> Intent(this, DashboardActivity::class.java)
                else -> Intent(this, DashboardActivity::class.java)
            }
            startActivity(intent)
            finish()
        } else {
            setContentView(loginBinding.root)
            phone = loginBinding.editPhone
            submit = loginBinding.btnSubmit
            networkManager = NetworkManagerLogin()
            networkManager.init(this, userCode, userName)
            LoaderUtils.initLoader(this)

            submit.setOnClickListener {
                val phoneNumber = phone.text.toString()
                if (phoneNumber.isEmpty() || phoneNumber.length != 10) {
                    Toast.makeText(this, "Please enter a valid phone number", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    LoaderUtils.showLoader()
                    sendOtpRequest(phoneNumber)
                }
            }
        }
    }

    private fun sendOtpRequest(phoneNumber: String) {
        networkManager.sendOtpRequest(phoneNumber, object : ApiResponseListener<OtpResponse> {
            override fun onSuccess(response: OtpResponse) {
                if (response.msg == "Verification code sent successfully") {
                    LoaderUtils.hideLoader()
                    SharedPreferenceUtils.saveString(
                        this@LoginActivity,
                        SharedPreferenceUtils.PHONE_NUMBER,
                        phoneNumber
                    )
                    startActivity(Intent(this@LoginActivity, OtpActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(
                        this@LoginActivity,
                        "Failed to send OTP: ${response.msg}",
                        Toast.LENGTH_SHORT
                    ).show()
                    LoaderUtils.hideLoader()
                }
            }

            override fun onFailure(error: String) {
                Toast.makeText(
                    this@LoginActivity,
                    "Something went wrong, contact support team!",
                    Toast.LENGTH_SHORT
                ).show()
                LoaderUtils.hideLoader()
            }
        })
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration(res.configuration)
        config.fontScale = 1.0f // Force fixed font size for the entire app
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }

}
