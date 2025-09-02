package com.vms.wizwarehouse.login

import android.content.Context
import com.vms.wizwarehouse.retrofit.RetrofitBuilder
import com.vms.wizwarehouse.retrofit.ApiResponseListener
import com.vms.wizwarehouse.retrofit.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetworkManagerLogin {
    private lateinit var context: Context
    private lateinit var userCode: String
    private lateinit var userName: String
    private lateinit var apiServiceLogin: ApiService


    fun init(context: Context, userCode: String, userName: String) {
        this.context = context
        this.userCode = userCode
        this.userName = userName

        apiServiceLogin = RetrofitBuilder.getRetrofitInstance(
            context,
            userCode,
            userName
        ).create(ApiService::class.java)
    }

    fun sendOtpRequest(phoneNumber: String, listener: ApiResponseListener<OtpResponse>) {
        val otpRequest = OtpRequest(phoneNumber)
        val call: Call<OtpResponse> = apiServiceLogin.sendOtp(otpRequest)

        call.enqueue(object : Callback<OtpResponse> {
            override fun onResponse(call: Call<OtpResponse>, response: Response<OtpResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    listener.onSuccess(response.body()!!)
                } else {
                    listener.onFailure("Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                listener.onFailure("Request failed: ${t.message}")
            }
        })
    }
}
