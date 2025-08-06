package com.vms.wizwarehouse.otp


import android.content.Intent
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.dashboard.DashboardActivity
import com.vms.wizwarehouse.databinding.ActivityOtpBinding
import com.vms.wizwarehouse.login.LoginActivity
import com.vms.wizwarehouse.login.NetworkManagerLogin
import com.vms.wizwarehouse.login.OtpResponse
import com.vms.wizwarehouse.retrofit.ApiResponseListener
import com.vms.wizwarehouse.utils.LoaderUtils
import com.vms.wizwarehouse.utils.SharedPreferenceUtils
import com.vms.wizwarehouse.utils.Utility

class OtpActivity : AppCompatActivity() {
    private lateinit var otpBinding: ActivityOtpBinding
    private lateinit var networkManager: NetworkManagerLogin
    private lateinit var otpFields: Array<EditText>
    private lateinit var resend: TextView
    private lateinit var countDown: TextView
    private lateinit var countDownTimer: CountDownTimer
    private lateinit var phoneNumber: String
    private lateinit var submit : Button
    private lateinit var heading : TextView
    private lateinit var otpHidden : EditText
    private lateinit var version : TextView
    private lateinit var userCode : String
    private lateinit var userName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        otpBinding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(otpBinding.root)

        version = otpBinding.txtVersion
        // Initialize views
        otpFields = arrayOf(
            otpBinding.otp1, otpBinding.otp2, otpBinding.otp3,
            otpBinding.otp4, otpBinding.otp5, otpBinding.otp6
        )

        userCode = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_CODE) ?: ""
        userName = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_NAME) ?: ""

        resend = otpBinding.txtResend
        countDown = otpBinding.txtCountDown
        submit = otpBinding.btnSubmit
        heading = otpBinding.txtHeading
        otpHidden = otpBinding.editTextHidden

        version.text = Utility.setVersionName(this)

        otpHidden.setAutofillHints("otp")
        LoaderUtils.initLoader(this)

        phoneNumber = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.PHONE_NUMBER) ?: ""
        heading.text = "A verification code has been sent to your registered Phone Number: $phoneNumber"

        networkManager = NetworkManagerLogin()
        networkManager.init(this, userCode, userName)

        countDownTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                countDown.text = "$minutes:$seconds"
            }

            override fun onFinish() {
                resend.isEnabled = true
                resend.setOnClickListener { sendOtpRequest(phoneNumber) }
            }
        }

        resend.isEnabled = false
        countDownTimer.start()

        otpFields.forEachIndexed { index, editText ->
            val prevField = otpFields.getOrNull(index - 1)
            val nextField = otpFields.getOrNull(index + 1)
            editText.addTextChangedListener(GenericTextWatcher(editText, prevField, nextField, otpFields, otpHidden))
        }

        submit.setOnClickListener {
            if (isOtpComplete()) {
                LoaderUtils.showLoader()
                verifyOtp(phoneNumber, getOtpCode())
            } else {
                Toast.makeText(this, "Please enter all digits of the OTP.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isOtpComplete(): Boolean = otpFields.all { it.text.isNotEmpty() }

    private fun getOtpCode(): String = otpFields.joinToString(separator = "") { it.text.toString() }

    private fun verifyOtp(phoneNumber: String, otpCode: String) {
        val networkManagerOtp = NetworkManagerOtp()
        networkManagerOtp.init(this, userCode, userName)
        networkManagerOtp.verifyOtp(phoneNumber, otpCode, object : ApiResponseListener<LoginResponse> {
            override fun onSuccess(response: LoginResponse) {
                LoaderUtils.hideLoader()
                if (response.msg == "Login Successfully") {
                    SharedPreferenceUtils.saveString(this@OtpActivity, SharedPreferenceUtils.ACCESS_TOKEN, response.accessToken)
                    SharedPreferenceUtils.saveString(this@OtpActivity, SharedPreferenceUtils.USER_ROLE, response.role)
                    SharedPreferenceUtils.saveBoolean(this@OtpActivity, SharedPreferenceUtils.IS_SUPERVISOR, response.role == "supervisor")
                    SharedPreferenceUtils.saveBoolean(this@OtpActivity, SharedPreferenceUtils.IS_LOGGED_IN_WAREHOUSE, true)

                    if (response.role == "warehouseManager" ) {
                        startActivity(Intent(this@OtpActivity, DashboardActivity::class.java))
                        finish()
                    } else {
                        showAccessDialog()
                    }
                } else {
                    Toast.makeText(this@OtpActivity, "OTP verification failed: ${response.msg}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(error: String) {
                Toast.makeText(this@OtpActivity, "Error: Unable to verify OTP", Toast.LENGTH_SHORT).show()
                LoaderUtils.hideLoader()
            }
        })
    }

    private fun sendOtpRequest(phoneNumber: String) {
        networkManager.sendOtpRequest(phoneNumber, object : ApiResponseListener<OtpResponse> {
            override fun onSuccess(response: OtpResponse) {
                if (response.msg == "Verification code sent successfully") {
                    Toast.makeText(this@OtpActivity, "OTP Sent Successfully", Toast.LENGTH_SHORT).show()
                    countDownTimer.start()
                    resend.isEnabled = false
                } else {
                    Toast.makeText(this@OtpActivity, "Failed to send OTP: ${response.msg}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(error: String) {
                Toast.makeText(this@OtpActivity, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        })
    }


    private fun showAccessDialog() {
        SharedPreferenceUtils.saveBoolean(this, SharedPreferenceUtils.IS_LOGGED_IN_WAREHOUSE, false)
        val dialogView = layoutInflater.inflate(R.layout.item_access_denied_pop_up, null)
        val builder = AlertDialog.Builder(this, R.style.TransparentAlertDialog).setView(dialogView)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        dialogView.findViewById<Button>(R.id.btn_ok).setOnClickListener {
            alertDialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        dialogView.findViewById<ImageView>(R.id.imgCancel).setOnClickListener {
            alertDialog.dismiss()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration(res.configuration)
        config.fontScale = 1.0f // Force fixed font size for the entire app
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }

}