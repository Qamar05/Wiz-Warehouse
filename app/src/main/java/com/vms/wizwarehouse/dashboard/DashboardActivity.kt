package com.vms.wizwarehouse.dashboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.vms.wizactivity.retrofit.RetrofitBuilder
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.add_inventory.AddInventoryActivity
import com.vms.wizwarehouse.camera.CameraActivity
import com.vms.wizwarehouse.databinding.ActivityDashboardBinding
import com.vms.wizwarehouse.distribute_history.DistributeHistoryActivity
import com.vms.wizwarehouse.distribute_inventory.DistributeInventoryActivity
import com.vms.wizwarehouse.inventory_history.InventoryHistoryActivity
import com.vms.wizwarehouse.login.LoginActivity
import com.vms.wizwarehouse.ota.UpdateChecker
import com.vms.wizwarehouse.refresh.RefreshTokenRequest
import com.vms.wizwarehouse.refresh.RefreshTokenResponse
import com.vms.wizwarehouse.reports.ReportsActivity
import com.vms.wizwarehouse.retrofit.ApiService
import com.vms.wizwarehouse.steps_to_update.StepsToUpdateActivity
import com.vms.wizwarehouse.utils.Const
import com.vms.wizwarehouse.utils.LoaderUtils
import com.vms.wizwarehouse.utils.SharedPreferenceUtils
import com.vms.wizwarehouse.utils.Utility
import com.vms.wizwarehouse.utils.Utility.logoutUser
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException

class DashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDashboardBinding
    private var drawerLayout: DrawerLayout? = null
    lateinit var userCode: String
    lateinit var userName: String
    private var accessToken: String? = null
    val TAG: String = "Dashboard"
    var name: TextView? = null
    var email: TextView? = null
    var fullName: TextView? = null
    var appVersion: TextView? = null
    private var apiServiceDashboard: ApiService? = null
    lateinit var call: Call<TodayActivityResponse>
    private val activityId = -1 // Default invalid value
    val PERMISSION_REQUEST_CODE: Int = 100

    //_______________________________________________
    var locationRefresh: ImageView? = null
    var locationAddress: TextView? = null
    var checkIn: ImageView? = null
    var totalStock: TextView? = null
    var stockAssigned: TextView? = null
    var pendingReturn: TextView? = null
    var todayActivity: TextView? = null
    var inventory: ConstraintLayout? = null
    var distribute: ConstraintLayout? = null
    var returnStock: ConstraintLayout? = null
    var version: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LoaderUtils.initLoader(this)
        requestPermissions()
        accessToken = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.ACCESS_TOKEN)

        drawerLayout = binding.drawerLayoutSurvey
        val layoutNavigation = binding.layoutNavigation
        name = binding.txtNameDashboard
        appVersion = binding.txtVersionName
        email = binding.navTxtEmailDashboard
        fullName = binding.navTxtNameDashboard

        //__________________________________________________________________
        locationRefresh = binding.imgRefreshLocation
        locationAddress = binding.txtAddress
        checkIn = binding.imgCheckIn
        totalStock = binding.txtTotalStockNumber
        stockAssigned = binding.txtStockAssignedNumber
        pendingReturn = binding.txtPendingReturnNumber
        todayActivity = binding.txtTodayActivityNumber
        inventory = binding.layoutInventory
        distribute = binding.layoutDistribute
        returnStock = binding.layoutReturn
        version = binding.txtVersion

        inventory!!.setOnClickListener { view: View? ->
            val intent = Intent(
                this@DashboardActivity,
                AddInventoryActivity::class.java
            )
            startActivity(intent)
        }

        distribute!!.setOnClickListener { view: View? ->
            val intent = Intent(
                this@DashboardActivity,
                DistributeInventoryActivity::class.java
            )
            startActivity(intent)
        }

        appVersion!!.text = (Utility.setVersionName(this))
        version!!.text = (Utility.setVersionName(this))

        // Ensure the drawer is closed by default
        drawerLayout!!.closeDrawer(GravityCompat.START)

        // Open drawer when clicking the profile section
        layoutNavigation.setOnClickListener { v: View? ->
            if (!drawerLayout!!.isDrawerOpen(GravityCompat.START)) {
                drawerLayout!!.openDrawer(GravityCompat.START)
            }
        }

        binding.navDashboard.setOnClickListener { view: View? ->
            drawerLayout!!.closeDrawer(GravityCompat.START)
        }

        binding.navInventory.setOnClickListener { view: View? ->
            drawerLayout!!.closeDrawer(GravityCompat.START)
            val intent = Intent(
                this@DashboardActivity,
                InventoryHistoryActivity::class.java
            )
            startActivity(intent)
        }

        binding.navDistribute.setOnClickListener { view: View? ->
            drawerLayout!!.closeDrawer(GravityCompat.START)
            val intent = Intent(
                this@DashboardActivity,
                DistributeHistoryActivity::class.java
            )
            startActivity(intent)
        }

        binding.navUpdate.setOnClickListener { view: View? ->
            drawerLayout!!.closeDrawer(GravityCompat.START)
            UpdateChecker.checkForUpdate(
                this@DashboardActivity,
                userCode!!,
                userName!!,
                "drawer"
            )
        }

        binding.navSteps.setOnClickListener { view: View? ->
            drawerLayout!!.closeDrawer(GravityCompat.START)
            val intent = Intent(
                this@DashboardActivity,
                StepsToUpdateActivity::class.java
            )
            startActivity(intent)
        }


        // Logout action
        binding.navLogout.setOnClickListener { v: View? ->
            drawerLayout!!.closeDrawer(GravityCompat.START)
            SharedPreferenceUtils.saveBoolean(
                this@DashboardActivity,
                SharedPreferenceUtils.IS_LOGGED_IN_WAREHOUSE,
                false
            )
            SharedPreferenceUtils.saveBoolean(
                this@DashboardActivity,
                SharedPreferenceUtils.IS_CHECKED_IN_WAREHOUSE,
                false
            )
            startActivity(
                Intent(
                    this@DashboardActivity,
                    LoginActivity::class.java
                )
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            finish()
        }

        binding.navReport.setOnClickListener { view: View? ->
            drawerLayout!!.closeDrawer(GravityCompat.START)
            val intent = Intent(
                this@DashboardActivity,
                ReportsActivity::class.java
            )
            startActivity(intent)
        }

        userCode = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_CODE) ?: ""
        userName = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_NAME) ?: ""
        apiServiceDashboard = RetrofitBuilder.getRetrofitInstance(this, userCode, userName).create(
            ApiService::class.java
        )

        fetchProfile()

    }

    private fun fetchProfile() {
        val profileService = RetrofitBuilder.getRetrofitInstance(this, userCode, userName).create(
            ApiService::class.java
        )
        val call = profileService.getProfile(Const.BEARER + " " + accessToken)

        call.enqueue(object : Callback<ProfileResponse> {
            override fun onResponse(
                call: Call<ProfileResponse>,
                response: Response<ProfileResponse>
            ) {
                if (!response.isSuccessful || response.body() == null) {
                    handleErrorResponse(response)
                    return
                }

                LoaderUtils.hideLoader()

                val profile = response.body()
                Log.d("Dashboard", "API Response: $profile")

                val userName = profile!!.firstname + " " + profile!!.lastname
                val userCode = profile!!.userCode

                saveUserProfile(profile)
                updateUI(userName, userCode)
            }

            override fun onFailure(call: Call<ProfileResponse?>, t: Throwable) {
                LoaderUtils.hideLoader()
                Toast.makeText(
                    this@DashboardActivity,
                    "Network error. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("Dashboard", "API Error: " + t.message, t)
            }
        })
    }

    private fun saveUserProfile(profile: ProfileResponse) {
        SharedPreferenceUtils.saveString(
            this@DashboardActivity,
            SharedPreferenceUtils.USER_NAME,
            profile.firstname + " " + profile.lastname
        )
        SharedPreferenceUtils.saveString(
            this@DashboardActivity,
            SharedPreferenceUtils.USER_EMAIL,
            profile.email
        )
        SharedPreferenceUtils.saveString(
            this@DashboardActivity,
            SharedPreferenceUtils.USER_CODE,
            profile.userCode
        )
    }

    private fun updateUI(userName: String, userCode: String) {
        name!!.text = userName
        fullName!!.text = userName
        email!!.text = userCode
    }

    private fun handleErrorResponse(response: Response<ProfileResponse>) {
        if (response.code() == 401 && response.errorBody() != null) {
            try {
                val errorBodyString = response.errorBody()!!.string()
                val jsonObject = JSONObject(errorBodyString)
                if ("sessionExpired" == jsonObject.optString("message")) {
                    accessTokenExpiredDialogCheckIn()
                    //                    refreshToken(accessToken);
                }
                if ("tokenExpired" == jsonObject.optString("message")) {
                    refreshToken(accessToken)
                }
            } catch (e: IOException) {
                Log.e(
                    TAG,
                    "Error processing error body: ",
                    e
                )
            } catch (e: JSONException) {
                Log.e(TAG,
                    "Error processing error body: ",
                    e
                )
            }
        } else {
            Log.e(
                TAG,
                "Failed to fetch profile. Response code: " + response.code()
            )
        }
    }

    private fun accessTokenExpiredDialogCheckIn() {
        LoaderUtils.showLoader()

        val dialogView = layoutInflater.inflate(R.layout.item_access_token_expired_pop_up, null)
        if (dialogView == null) {
            LoaderUtils.hideLoader()
            Toast.makeText(this, "Error loading dialog", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this, R.style.TransparentAlertDialog)
            .setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.setOnDismissListener {
            LoaderUtils.hideLoader()
        }

        alertDialog.setOnShowListener {
            LoaderUtils.hideLoader()
        }

        alertDialog.show()

        // Access views
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)
        val imgCancel = dialogView.findViewById<ImageView>(R.id.imgCancel)

        imgCancel.setImageResource(R.drawable.img_cross)

        btnOk.setOnClickListener {
            LoaderUtils.hideLoader()
            alertDialog.dismiss()
            logoutUser(this)
            finish()
        }

        imgCancel.setOnClickListener {
            LoaderUtils.hideLoader()
            alertDialog.dismiss()
            logoutUser(this)
            finish()
        }
    }

    fun refreshToken(oldToken: String?) {
        if (oldToken.isNullOrEmpty()) {
            Toast.makeText(this, "Invalid token", Toast.LENGTH_SHORT).show()
            return
        }

        val request = RefreshTokenRequest(oldToken)

        apiServiceDashboard?.refreshToken(request)
            ?.enqueue(object : Callback<RefreshTokenResponse> {
                override fun onResponse(
                    call: Call<RefreshTokenResponse>,
                    response: Response<RefreshTokenResponse>
                ) {
                    val body = response.body()
                    if (response.isSuccessful && body?.accessToken != null) {
                        val newToken = body.accessToken!!
                        Log.d("RefreshToken", "New token: $newToken")

                        // Save token to SharedPreferences
                        SharedPreferenceUtils.saveString(
                            this@DashboardActivity,
                            SharedPreferenceUtils.ACCESS_TOKEN,
                            newToken
                        )

                        // Update token locally
                        accessToken = newToken

                        // Fetch updated data
                        LoaderUtils.showLoader()
//                        fetchProfile()
                        fetchUserActivities(false)
                    } else {
                        Log.e("RefreshToken", "Token refresh failed: ${response.message()}")
                        Toast.makeText(
                            this@DashboardActivity,
                            "Session expired. Please log in again.",
                            Toast.LENGTH_SHORT
                        ).show()
                        logoutUser(this@DashboardActivity)
                    }
                }

                override fun onFailure(call: Call<RefreshTokenResponse>, t: Throwable) {
                    Log.e("RefreshToken", "Network error: ${t.message}")
                    Toast.makeText(
                        this@DashboardActivity,
                        "Unable to refresh token. Please check your internet.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun fetchUserActivities(isFetching: Boolean) {
        // Check if mode is "demo"

        // Proceed with normal API flow

        call = apiServiceDashboard!!.getUserActivities(Const.BEARER + " " + accessToken)

        call.enqueue(object : Callback<TodayActivityResponse?> {
            override fun onResponse(
                call: Call<TodayActivityResponse?>,
                response: Response<TodayActivityResponse?>
            ) {
                LoaderUtils.hideLoader()
                if (!response.isSuccessful || response.body() == null) {
                    handleActivityErrorResponse(response)
                    return
                }

                val activityResponse = response.body()
                Log.d(
                    TAG,
                    "API Response: $activityResponse"
                )

                if (activityResponse!!.message != null) {
                    showNoActivityDialog()
                    return
                }

                val activityId = activityResponse!!.id
                if (activityId != null && activityId > 0) {
                    saveActivityDetails(activityId, activityResponse)
                    if (isFetching) {
                        navigateToCamera()
                    }
                    Log.d(
                        TAG,
                        "Activity ID saved: $activityId"
                    )
                } else {
                    SharedPreferenceUtils.saveInt(
                        this@DashboardActivity,
                        SharedPreferenceUtils.KEY_ACTIVITY_ID,
                        0
                    )
                    showNoActivityDialog()
                }
            }

            override fun onFailure(call: Call<TodayActivityResponse?>, t: Throwable) {
                LoaderUtils.hideLoader()
                Toast.makeText(
                    this@DashboardActivity,
                    "Network error. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(
                    TAG,
                    "API Error: " + t.message,
                    t
                )
            }
        })
    }

    private fun saveActivityDetails(activityId: Int, activityResponse: TodayActivityResponse) {
        SharedPreferenceUtils.saveInt(
            this@DashboardActivity,
            SharedPreferenceUtils.KEY_ACTIVITY_ID,
            activityId
        )
        SharedPreferenceUtils.saveInt(
            this@DashboardActivity,
            SharedPreferenceUtils.CITY_ID,
            activityResponse.outlet.city.id
        )
        Log.d("CITY_ID", "City ID saved: " + activityResponse.outlet.city.id)
    }


    private fun handleActivityErrorResponse(response: Response<TodayActivityResponse?>) {
        try {
            if (response.body() != null && "No activity assigned for today" == response.body()!!.message) {
                showNoActivityDialog()
                return
            }

            if (response.code() == 401 && response.errorBody() != null) {
                val errorBodyString = response.errorBody()!!.string()
                val jsonObject = JSONObject(errorBodyString)
                if ("sessionExpired" == jsonObject.optString("message")) {
                    accessTokenExpiredDialogCheckIn()
                    //                    refreshToken(accessToken);
                    return
                }
            }
        } catch (e: IOException) {
            Log.e(
                TAG,
                "Error processing response: ",
                e
            )
        } catch (e: JSONException) {
            Log.e(
                TAG,
                "Error processing response: ",
                e
            )
        }

        Log.e(
            TAG,
            "Failed to get activity ID. Response code: " + response.code()
        )
    }

    private fun navigateToCamera() {
        val intent = Intent(
            this@DashboardActivity,
            CameraActivity::class.java
        )
        intent.putExtra("dialogType", "checkIn")
        intent.putExtra("activity_id", activityId)
        intent.putExtra("access_token", accessToken) // Send the access token
        startActivity(intent)
        finish()
    }


    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
        )

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions

            ActivityCompat.requestPermissions(
                this,
                permissions,
               PERMISSION_REQUEST_CODE
            )
        } else {
            // Permissions already granted
            Log.d("PERMISSION", "Permissions already granted")
        }
    }

    private fun showNoActivityDialog() {
        LoaderUtils.showLoader()

        // Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.item_no_activity_pop_up, null)
        if (dialogView == null) {
            LoaderUtils.hideLoader()
            Toast.makeText(this, "Error loading dialog", Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this, R.style.TransparentAlertDialog)
        builder.setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Show the dialog
        alertDialog.show()

        alertDialog.setOnDismissListener {
            LoaderUtils.hideLoader()
        }

        alertDialog.setOnShowListener {
            LoaderUtils.hideLoader()
        }

        // Find views inside the dialog
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)
        val imgCancel = dialogView.findViewById<ImageView>(R.id.imgCancel)
        val imgTick = dialogView.findViewById<ImageView>(R.id.imgTick)

        val cancelIcon = R.drawable.img_cross
        imgCancel.setImageResource(cancelIcon)

        val tickIcon = R.drawable.img_tick
        imgTick.setImageResource(tickIcon)

        // Handle the OK button click
        btnOk.setOnClickListener {
            LoaderUtils.hideLoader()
            alertDialog.dismiss()
            checkOut()
        }

        // Handle the Cancel button click
        imgCancel.setOnClickListener {
            LoaderUtils.hideLoader()
            alertDialog.dismiss()
            checkOut()
        }
    }


    fun checkOut() {
        SharedPreferenceUtils.saveBoolean(
            this@DashboardActivity,
            SharedPreferenceUtils.IS_CHECKED_IN_WAREHOUSE,
            false
        )
    }


}