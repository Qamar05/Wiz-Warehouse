package com.vms.wizwarehouse.retrofit

import com.vms.wizwarehouse.camera.CheckInResponse
import com.vms.wizwarehouse.dashboard.ProfileResponse
import com.vms.wizwarehouse.dashboard.TodayActivityResponse
import com.vms.wizwarehouse.login.OtpRequest
import com.vms.wizwarehouse.login.OtpResponse
import com.vms.wizwarehouse.ota.UpdateResponse
import com.vms.wizwarehouse.otp.LoginRequest
import com.vms.wizwarehouse.otp.LoginResponse
import com.vms.wizwarehouse.refresh.RefreshTokenRequest
import com.vms.wizwarehouse.refresh.RefreshTokenResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @POST("requestVerificationCode")
    fun sendOtp(@Body otpRequest: OtpRequest): Call<OtpResponse>

    @POST("login")
    fun verifyOtp(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @GET("today-activity")
    fun getUserActivities(@Header("Authorization") token: String): Call<TodayActivityResponse>

    @Multipart
    @POST("checkin")
    fun submitForm(
        @Header("Authorization") authorization: String,
        @PartMap map: HashMap<String, @JvmSuppressWildcards RequestBody>,
        @Part checkInSelfie: MultipartBody.Part,
        @Part checkInOutletSelfie: MultipartBody.Part
    ): Call<CheckInResponse>

    @Multipart
    @PUT("checkout/{id}")
    fun checkOutForm(
        @Path("id") id: Int,
        @Header("Authorization") authorization: String,
        @PartMap map: HashMap<String, @JvmSuppressWildcards RequestBody>,
        @Part checkOutSelfie: MultipartBody.Part,
        @Part checkOutOutletSelfie: MultipartBody.Part
    ): Call<CheckInResponse>

    @GET("profile")
    fun getProfile(@Header("Authorization") token: String): Call<ProfileResponse>

    @GET("get-updated-version")
    fun getLatestVersion(): Call<UpdateResponse>

    @Headers("Content-Type: application/json")
    @POST("user-logs")
    fun sendLogs(
        @Header("Authorization") token: String,
        @Body logData: RequestBody
    ): Call<Void>

    @POST("refresh")
    fun refreshToken(@Body request: RefreshTokenRequest): Call<RefreshTokenResponse>
}
