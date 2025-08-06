package com.vms.wizwarehouse.ota


import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.vms.wizactivity.retrofit.RetrofitBuilder
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.retrofit.ApiService
import com.vms.wizwarehouse.utils.Const
import com.vms.wizwarehouse.utils.SharedPreferenceUtils
import com.vms.wizwarehouse.utils.Utility
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

object UpdateChecker {
    private const val TAG = "UpdateChecker"
    private var mode: String? = null
    private var formattedDate: String? = null

    fun checkForUpdate(context: Context, userId: String, userName: String, passUpdate: String) {
        mode = SharedPreferenceUtils.getString(context, SharedPreferenceUtils.SELECTED_MODE)
        val retrofit: Retrofit = RetrofitBuilder.getRetrofitInstance(context, userId, userName)
        val updateApi: ApiService = retrofit.create(ApiService::class.java)
        val accessToken: String =
            SharedPreferenceUtils.getString(context, SharedPreferenceUtils.ACCESS_TOKEN).toString()

        val call: Call<UpdateResponse> = updateApi.getLatestVersion()
        call.enqueue(object : Callback<UpdateResponse> {
            override fun onResponse(call: Call<UpdateResponse>, response: Response<UpdateResponse>) {
                if (response.isSuccessful && response.body() != null) {
                    val updateData = response.body()!!.data ?: return
                    val latestVersion = updateData.version?.trim() ?: return
                    val apkUrl = updateData.apkUrl ?: return
                    formattedDate = updateData.getFormattedDate()

                    Log.d(TAG, "Latest Version from API: $latestVersion")

                    if (isUpdateAvailable(context, latestVersion)) {
                        showUpdateDialog(context, apkUrl, latestVersion)
                    } else if (passUpdate == "drawer") {
                        showUpToDateDialog(context)
                        Log.d(TAG, "No update available")
                    }
                } else {
                    Log.e(TAG, "Update API response unsuccessful: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<UpdateResponse>, t: Throwable) {
                Log.e(TAG, "Failed to check for updates: ${t.message}")
            }
        })
    }

    private fun isUpdateAvailable(context: Context, latestVersion: String): Boolean {
        return try {
            val pInfo: PackageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            val currentVersion = pInfo.versionName?.trim()
            Log.d(TAG, "Current Version: $currentVersion")
            compareVersions(latestVersion, currentVersion!!) > 0
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Failed to get current app version", e)
            false
        }
    }

    private fun compareVersions(version1: String, version2: String): Int {
        val v1 = version1.split(".").map { it.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0 }
        val v2 = version2.split(".").map { it.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0 }
        val length = maxOf(v1.size, v2.size)

        for (i in 0 until length) {
            val num1 = v1.getOrElse(i) { 0 }
            val num2 = v2.getOrElse(i) { 0 }
            if (num1 != num2) return num1.compareTo(num2)
        }
        return 0
    }

    private fun showUpdateDialog(context: Context, apkUrl: String, latestVersion: String) {
        val alertDialog = createDialog(context, R.layout.dialog_update_app)
        val btnUpdate = alertDialog.findViewById<Button>(R.id.btn_update)
        val imgTick = alertDialog.findViewById<ImageView>(R.id.imgTick)
        val txtUpdateMessage = alertDialog.findViewById<TextView>(R.id.txtMessage)
        txtUpdateMessage.text = "Latest Version $latestVersion available.\nPlease update to continue."
        imgTick.setImageResource(if (mode == "demo") R.drawable.img_update_orange else R.drawable.img_update)
        btnUpdate?.setOnClickListener {
            alertDialog.dismiss()
            ApkDownloader.downloadAndInstallApk(context, apkUrl)
            SharedPreferenceUtils.saveBoolean(context, SharedPreferenceUtils.IS_LOGGED_IN_WAREHOUSE, false)
            SharedPreferenceUtils.saveBoolean(context, SharedPreferenceUtils.IS_CHECKED_IN_WAREHOUSE, false)
        }
    }

    private fun showUpToDateDialog(context: Context) {
        val alertDialog = createDialog(context, R.layout.item_up_to_date_pop_up)
        val txtVersion = alertDialog.findViewById<TextView>(R.id.txtVersion)
        val btnOk = alertDialog.findViewById<Button>(R.id.btn_ok)
        val cross = alertDialog.findViewById<ImageView>(R.id.imgCancel)
        val date = alertDialog.findViewById<TextView>(R.id.txtDate)
        val imgTick = alertDialog.findViewById<ImageView>(R.id.imgTick)
        cross.setImageResource(if (mode == "demo") R.drawable.img_cross_orange else R.drawable.img_cross)
        imgTick.setImageResource(if (mode == "demo") R.drawable.img_update_orange else R.drawable.img_update)
        date?.text = "Last Update: $formattedDate"
        txtVersion?.text = Utility.setVersionName(context)
        cross?.setOnClickListener { alertDialog.dismiss() }
        btnOk?.setOnClickListener { alertDialog.dismiss() }
    }

    private fun createDialog(context: Context, layoutRes: Int): AlertDialog {
        val dialogView = LayoutInflater.from(context).inflate(layoutRes, null)
        val builder = AlertDialog.Builder(context, R.style.TransparentAlertDialog)
            .setView(dialogView)
            .setCancelable(false)
        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        return alertDialog
    }
}


