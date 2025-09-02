package com.vms.wizwarehouse.otp

import android.content.Context
import com.vms.wizwarehouse.retrofit.RetrofitBuilder
import com.vms.wizwarehouse.retrofit.ApiResponseListener
import com.vms.wizwarehouse.retrofit.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NetworkManagerOtp {
    private lateinit var context: Context
    private lateinit var userCode: String
    private lateinit var userName: String
    private lateinit var apiService: ApiService

    fun init(context: Context, userCode: String, userName: String) {
        this.context = context
        this.userCode = userCode
        this.userName = userName

        apiService = RetrofitBuilder.getRetrofitInstance(
            context,
            userCode,
            userName
        ).create(ApiService::class.java)
    }

    fun verifyOtp(
        phoneNumber: String,
        otpCode: String,
        listener: ApiResponseListener<LoginResponse>
    ) {
        val loginRequest = LoginRequest(phoneNumber, otpCode)
        val call = apiService.verifyOtp(loginRequest)

        call.enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    listener.onSuccess(response.body()!!)
                } else {
                    listener.onFailure("Error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                listener.onFailure("Request failed: ${t.message}")
            }
        })
    }
}
