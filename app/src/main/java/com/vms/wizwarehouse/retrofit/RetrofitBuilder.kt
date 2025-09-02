// Kotlin version of RetrofitBuilder.java
package com.vms.wizwarehouse.retrofit

import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import com.vms.wizwarehouse.utils.SharedPreferenceUtils
import com.vms.wizwarehouse.utils.SlowNetworkManager
import com.vms.wizwarehouse.utils.Utility
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import okio.Buffer
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object RetrofitBuilder {
    private var retrofit: Retrofit? = null
    private var okHttpClient: OkHttpClient? = null
    private const val TAG = "API_LOGS"
    private const val TIMEOUT = 10L
    private const val BASE_URL_UAT = "https://uat-pmi.wizsuite.com/api/v1/"
    private const val BASE_URL_PROD: String = "https://pmi.wizsuite.com/api/v1/"
    private const val LOG_API_ENDPOINT = "user-logs"
    private var logFile: File? = null
    private var accessToken: String? = null

    fun initLogging(context: Context) {
        logFile = File(context.getExternalFilesDir(null), "api_logs.txt")
    }

    fun getRetrofitInstance(context: Context, userId: String, userName: String): Retrofit {
        return retrofit ?: synchronized(this) {
            retrofit ?: Retrofit.Builder()
                .baseUrl(BASE_URL_UAT)  // Change to your dynamic base URL if needed
                .addConverterFactory(GsonConverterFactory.create()) // Gson converter for parsing JSON
                .client(
                    getOkHttpClient(
                        context,
                        userId,
                        userName
                    )
                ) // OkHttpClient setup with interceptors
                .build()
                .also { retrofit = it }
        }
    }


    private fun getOkHttpClient(context: Context, userId: String, userName: String): OkHttpClient {
        if (okHttpClient == null) {
            synchronized(this) {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }

                okHttpClient = OkHttpClient.Builder()
                    .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                    .addInterceptor(loggingInterceptor)
                    .addInterceptor(ApiLoggerInterceptor(context, userId, userName))
                    .addInterceptor { chain ->
                        val start = System.currentTimeMillis()
                        val response = chain.proceed(chain.request())
                        val duration = System.currentTimeMillis() - start
                        if (duration > 2500) {
                            Handler(Looper.getMainLooper()).post { SlowNetworkManager.notifySlowNetwork() }
                        }
                        response
                    }
                    .build()
            }
        }
        return okHttpClient!!
    }

    private class ApiLoggerInterceptor(
        val context: Context,
        val userId: String,
        val userName: String
    ) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            if (request.url.toString().contains(LOG_API_ENDPOINT)) {
                return chain.proceed(request)
            }

            return try {
                val response = chain.proceed(request)
                val dateTime =
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                val deviceInfo = JSONObject().apply {
                    put("device", "${Build.MANUFACTURER} ${Build.MODEL}")
                    put("os_version", "${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                    put("brand", Build.BRAND)
                    put("hardware", Build.HARDWARE)
                    put("product", Build.PRODUCT)
                    put(
                        "android_id",
                        Settings.Secure.getString(
                            context.contentResolver,
                            Settings.Secure.ANDROID_ID
                        )
                    )
                    put("screen_resolution", getScreenResolution(context))
                    put("locale", "${Locale.getDefault().displayLanguage} (${Locale.getDefault()})")
                }

                val userInfo = JSONObject().apply {
                    put("user_id", userId)
                    put("user_name", userName)
                }

                val versionInfo = JSONObject().apply {
                    put("version", Utility.setVersionName(context))
                }

                val requestInfo = JSONObject().apply {
                    put("url", request.url.toString())
                    put("method", request.method)
                    put("headers", request.headers.toString())
                    put("body", getRequestBody(request))
                }

                val responseInfo = JSONObject().apply {
                    put("code", response.code)
                    put("message", response.message)
                    put("body", getResponseBody(response))
                }

                val logJson = JSONObject().apply {
                    put("api_call_time", dateTime)
                    put("device_info", deviceInfo)
                    put("user_info", userInfo)
                    put("version_info", versionInfo)
                    put("request", requestInfo)
                    put("response", responseInfo)
                }

                val finalLogJson = JSONObject().apply {
                    put("log", logJson)
                }

                Log.d(TAG, finalLogJson.toString())
                saveLogToFile(finalLogJson.toString())

                accessToken =
                    SharedPreferenceUtils.getString(context, SharedPreferenceUtils.ACCESS_TOKEN)
                sendNewLogsToAPI(finalLogJson, accessToken ?: "")

                response
            } catch (e: Exception) {
                Log.e(TAG, "Error logging API request/response", e)
                chain.proceed(request)
            }
        }

        private fun getRequestBody(request: Request): String {
            return try {
                request.body?.let { body ->
                    if (body.contentType()?.toString()?.contains("multipart/form-data") == true) {
                        return "Ignored Multipart Image"
                    }
                    val buffer = Buffer()
                    body.writeTo(buffer)
                    buffer.readUtf8()
                } ?: "No Body"
            } catch (e: IOException) {
                "Error reading request body"
            }
        }

        private fun getResponseBody(response: Response): String {
            return try {
                response.peekBody(1024 * 1024).string()
            } catch (e: IOException) {
                "Error reading response body"
            }
        }

        private fun saveLogToFile(log: String) {
            try {
                val logFile = File(context.filesDir, "api_logs.txt")
                BufferedWriter(FileWriter(logFile, true)).use { writer ->
                    writer.write(log)
                    writer.newLine()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error writing log to file", e)
            }
        }

        private fun sendNewLogsToAPI(logJson: JSONObject, authToken: String) {
            val apiService =
                getRetrofitInstance(context, userId, userName).create(ApiService::class.java)
            val requestBody =
                RequestBody.create("application/json".toMediaTypeOrNull(), logJson.toString())
            val call = apiService.sendLogs("Bearer $authToken", requestBody)

            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: retrofit2.Response<Void>) {
                    if (response.isSuccessful) {
                        Log.d(TAG, "Log sent successfully")
                    } else {
                        Log.e(TAG, "Failed to send log: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Log.e(TAG, "Error sending log", t)
                }
            })
        }
    }

    private fun getScreenResolution(context: Context): String {
        val displayMetrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return "${displayMetrics.widthPixels} x ${displayMetrics.heightPixels} px"
    }
}
