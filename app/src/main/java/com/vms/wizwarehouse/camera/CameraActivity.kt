package com.vms.wizwarehouse.camera


import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.vms.wizwarehouse.retrofit.RetrofitBuilder
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.dashboard.DashboardActivity
import com.vms.wizwarehouse.retrofit.ApiResponseListener
import com.vms.wizwarehouse.utils.BitmapUtils.imageProxyToBitmap
import com.vms.wizwarehouse.utils.Const
import com.vms.wizwarehouse.utils.LoaderUtils
import com.vms.wizwarehouse.utils.SharedPreferenceUtils
import com.vms.wizwarehouse.utils.Utility
import com.vms.wizwarehouse.utils.Utility.printMultiLineAddressOnImage
import com.vms.wizwarehouse.databinding.ActivityCameraBinding
import com.vms.wizwarehouse.retrofit.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {
    private lateinit var cameraBinding: ActivityCameraBinding

    private lateinit var click: ImageView
    private lateinit var display: ImageView
    private lateinit var display2: ImageView
    private lateinit var date: TextView
    private lateinit var time: TextView
    private lateinit var txtRetake: TextView
    private lateinit var txtSubmit: TextView
    private lateinit var address: TextView
    private lateinit var camera: PreviewView
    private lateinit var imageCapture: ImageCapture
    private lateinit var cameraExecutor: ExecutorService
    private var isFrontCamera = true
    private var frontCapturedImage: Bitmap? = null
    private var backCapturedImage: Bitmap? = null
    private var activityId: Int = 0
    private lateinit var refresh: ImageView
    private lateinit var heading: TextView

    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    private lateinit var apiServiceCheckIn: ApiService

    private var timeoutCount = 0 // Counter for timeouts
    val MAX_TIMEOUT_RETRIES: Int = 3 // Max retries before showing connection check dialog


    private lateinit var dateFormat: SimpleDateFormat
    private lateinit var timeFormat: SimpleDateFormat

    private val TAG = "CheckIn"
    private val TAG_TWO = "CAMERA"
    private val TAG_THREE = "Exception"
    private val TAG_FOUR = "Check-in-Error"

    private lateinit var userCode: String
    private lateinit var userName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        cameraBinding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(cameraBinding.root) // Load production layout

// ✅ Initialize Views using findViewById()
        val imgBack: ImageView = cameraBinding.imgBack
        click = cameraBinding.imgClick
        display = cameraBinding.imgDisplay
        display2 = cameraBinding.imgDisplay2
        date = cameraBinding.txtDate
        time = cameraBinding.txtTime
        txtRetake = cameraBinding.txtRetake
        txtSubmit = cameraBinding.txtSubmit
        address = cameraBinding.txtAddress
        camera = cameraBinding.cameraPreview
        refresh = cameraBinding.imgRefresh
        heading = cameraBinding.txtSelfie


        userCode = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_CODE).toString()
        userName = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_NAME).toString()

        LoaderUtils.initLoader(this)

//        activityId = SharedPreferenceUtils.getInt(this, SharedPreferenceUtils.KEY_ACTIVITY_ID)

        SharedPreferenceUtils.getInt(this, SharedPreferenceUtils.KEY_ACTIVITY_ID)


        txtRetake.visibility = View.GONE
        txtSubmit.visibility = View.GONE
        display.visibility = View.GONE

        // Initialize Camera
        cameraExecutor = Executors.newSingleThreadExecutor()
        startCamera()

        updateDateTime()

        getCurrentLocation()

        refresh.setOnClickListener {
            getCurrentLocation()
        }

        click.setOnClickListener {
            if (address.text.toString().isEmpty()) {
                Toast.makeText(this, "Please refresh the location", Toast.LENGTH_SHORT).show()
            } else {
                takePhoto()
            }
        }

        txtRetake.setOnClickListener {
            startCamera()
            display.visibility = View.GONE
            display2.visibility = View.GONE
            txtRetake.visibility = View.GONE
            txtSubmit.visibility = View.GONE
        }

        txtSubmit.setOnClickListener {
            if (isFrontCamera) {
                isFrontCamera = false
                heading.text = "Take Outlet Picture"
                startCamera()
                txtSubmit.visibility = View.GONE
            } else {
                submitImages()
            }
        }

        imgBack.setOnClickListener { finish() }
    }

    private fun takePhoto() {
        LoaderUtils.showLoader()
        imageCapture.takePicture(
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    LoaderUtils.hideLoader()
                    val capturedBitmap = imageProxyToBitmap(image)
                    image.close()

                    if (isFrontCamera) {
                        frontCapturedImage = capturedBitmap
                        display.setImageBitmap(frontCapturedImage)
                    } else {
                        backCapturedImage = capturedBitmap
                        display2.setImageBitmap(backCapturedImage)
                    }

                    camera.visibility = View.GONE
                    click.visibility = View.GONE
                    display.visibility = View.VISIBLE
                    display2.visibility = View.VISIBLE
                    txtRetake.visibility = View.VISIBLE
                    txtSubmit.visibility = View.VISIBLE
                }

                override fun onError(exception: ImageCaptureException) {
                    LoaderUtils.hideLoader()
                    Toast.makeText(
                        this@CameraActivity,
                        "Failed to capture image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(if (isFrontCamera) CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK)
                    .build()

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(camera.surfaceProvider)

                imageCapture = ImageCapture.Builder().build()
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)

                camera.visibility = View.VISIBLE
                click.visibility = View.VISIBLE
                display.visibility = View.GONE
                display2.visibility = View.GONE
                txtRetake.visibility = View.GONE
                txtSubmit.visibility = View.GONE
            } catch (e: Exception) {
                Log.e(TAG, "Camera start failed: $e")
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun submitImages() {
        if (frontCapturedImage == null || backCapturedImage == null) {
            Toast.makeText(applicationContext, "Please capture both images.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        LoaderUtils.showLoader()
        val currentDate = Date()
        val textToPrint =
            "Date: ${dateFormat.format(currentDate)} Location: ${address.text} Latitude: $currentLatitude, Longitude: $currentLongitude"
        val frontBitmap = printMultiLineAddressOnImage(frontCapturedImage!!, textToPrint)
        val backBitmap = printMultiLineAddressOnImage(backCapturedImage!!, textToPrint)

        val checkInSelfie = convertBitmapToFile(this, frontBitmap, "checkIn_selfie.jpg")
        val checkInOutletSelfie = convertBitmapToFile(this, backBitmap, "checkIn_outlet_selfie.jpg")

        submitCheckInForm(
            "production",
            activityId.toString(),
            currentLatitude,
            currentLongitude,
            address.text.toString(),
            checkInSelfie,
            checkInOutletSelfie,
            object : ApiResponseListener<CheckInResponse> {
                override fun onSuccess(response: CheckInResponse) {
                    LoaderUtils.hideLoader()
                    SharedPreferenceUtils.saveBoolean(
                        this@CameraActivity,
                        SharedPreferenceUtils.IS_CHECKED_IN_WAREHOUSE,
                        true
                    )
                    SharedPreferenceUtils.saveInt(
                        this@CameraActivity,
                        SharedPreferenceUtils.CHECK_IN_ID,
                        response.data.id
                    )
                    showCheckInDialog()
                }

                override fun onFailure(error: String) {
                    LoaderUtils.hideLoader()
                    Log.e(TAG, "Check-in failed: $error")

                    if (error.lowercase().contains("timeout") || error.lowercase()
                            .contains("failed to connect")
                    ) {
                        timeoutCount++ // Increment timeout counter

                        if (timeoutCount >= MAX_TIMEOUT_RETRIES) {
                            showCheckInternetDialog() // Show "Check Internet" dialog after 3 failures
                        } else {
                            showTimeoutDialog() // Show normal timeout dialog
                        }
                    } else {
                        Toast.makeText(
                            this@CameraActivity,
                            "Check-in failed: $error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })
    }


    private fun updateDateTime() {
        val handler = Handler(Looper.getMainLooper())
        dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        timeFormat = SimpleDateFormat("hh:mm:ss a", Locale.getDefault())

        handler.postDelayed(object : Runnable {
            override fun run() {
                val currentDate = Date()
                date.text = dateFormat.format(currentDate)
                time.text = timeFormat.format(currentDate)
                handler.postDelayed(this, 1000)
            }
        }, 1000)
    }


    private fun getCurrentLocation() {
        if (!Utility.isGpsEnabled(this)) {
            showGPSError()
            return
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            val LOCATION_PERMISSION_REQUEST_CODE = 100
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Try getting an updated location
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    getAddressFromLocation(location.latitude, location.longitude)
                } else {
                    // If location is null, try getLastLocation() as a fallback
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLocation ->
                            if (lastLocation != null) {
                                getAddressFromLocation(
                                    lastLocation.latitude,
                                    lastLocation.longitude
                                )
                            } else {
                                Toast.makeText(
                                    this,
                                    "Unable to fetch location. Please try again.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(
                                this,
                                "Failed to get last known location",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to refresh location", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addressInfo = addresses[0]

                // Save latitude and longitude for later use
                currentLatitude = latitude
                currentLongitude = longitude

                SharedPreferenceUtils.saveString(
                    this,
                    SharedPreferenceUtils.LATITUDE,
                    latitude.toString()
                )
                SharedPreferenceUtils.saveString(
                    this,
                    SharedPreferenceUtils.LONGITUDE,
                    longitude.toString()
                )

                val localAddress = addressInfo.getAddressLine(0)
                // Set formatted address in TextView
                address.text = localAddress
            } else {
                address.text = ""
            }
        } catch (e: IOException) {
            Log.e(TAG_THREE, e.toString())
            address.text = ""
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }


    private fun showGPSError() {
        // Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.item_location_error_pop_up, null)
        if (dialogView == null) {
            Toast.makeText(this, Const.ERROR_LOADING_DIALOG, Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this, R.style.TransparentAlertDialog)
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Show the dialog
        alertDialog.show()

        // Handle the OK button click
        val btnOk: Button = dialogView.findViewById(R.id.btn_ok)
        val imgCancel = dialogView.findViewById<ImageView>(R.id.imgCancel)

        val cancelIcon = R.drawable.img_cross
        imgCancel.setImageResource(cancelIcon)

        val gpsSettingsIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

        btnOk.setOnClickListener {
            startActivity(gpsSettingsIntent)
            alertDialog.dismiss() // Dismiss dialog
            finish()
        }

        imgCancel.setOnClickListener {
            startActivity(gpsSettingsIntent)
            alertDialog.dismiss()
            finish()
        }
    }


    private fun prepareImagePart(imageFile: File, partName: String): MultipartBody.Part {
        // Create RequestBody from File
        val imageRequestBody = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
        // Create MultipartBody.Part
        return MultipartBody.Part.createFormData(partName, imageFile.name, imageRequestBody)
    }

    private fun convertBitmapToFile(context: Context?, bitmap: Bitmap, fileName: String): File {
        requireNotNull(context) { "Context must not be null" }

        val file = File(context.cacheDir, fileName)
        try {
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, bos)
            val bitmapData = bos.toByteArray()

            FileOutputStream(file).use { fos ->
                fos.write(bitmapData)
                fos.flush()
            }
        } catch (e: IOException) {
            Log.e(TAG_THREE, e.toString())
        }
        return file
    }


    private fun submitCheckInForm(
        appMode: String,
        activityId: String,
        checkInLatitude: Double,
        checkInLongitude: Double,
        checkInAddress: String,
        checkInSelfie: File,
        checkInOutletFile: File,
        listener: ApiResponseListener<CheckInResponse>
    ) {
        val accessToken = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.ACCESS_TOKEN)

        if (accessToken.isNullOrEmpty()) {
            listener.onFailure("Access token is missing")
            return
        }

        Log.d(TAG, "Access token retrieved: $accessToken")

        // Initialize Retrofit and create ApiService instance
        apiServiceCheckIn = RetrofitBuilder.getRetrofitInstance(this, userCode, userName)
            .create(ApiService::class.java)

        // Prepare RequestBody for form data
        val appModeBody = appMode.toRequestBody(Const.MULTIPART_FORM_DATA.toMediaTypeOrNull())
        val activityIdBody = activityId.toRequestBody(Const.MULTIPART_FORM_DATA.toMediaTypeOrNull())
        val latitudeBody =
            checkInLatitude.toString().toRequestBody(Const.MULTIPART_FORM_DATA.toMediaTypeOrNull())
        val longitudeBody =
            checkInLongitude.toString().toRequestBody(Const.MULTIPART_FORM_DATA.toMediaTypeOrNull())
        val addressBody =
            checkInAddress.toRequestBody(Const.MULTIPART_FORM_DATA.toMediaTypeOrNull())

        // Prepare MultipartBody.Part for images
        val selfiePart = prepareImagePart(checkInSelfie, "checkin_selfie")
        val outletSelfiePart = prepareImagePart(checkInOutletFile, "checkin_outlet_selfie")

        val map = hashMapOf(
            "appMode" to appModeBody,
            "activity_id" to activityIdBody,
            "checkin_latitude" to latitudeBody,
            "checkin_longitude" to longitudeBody,
            "checkin_address" to addressBody
        )

        Log.d(
            TAG,
            "Submitting Check-in: activity_id=$activityId, checkIn_latitude=$checkInLatitude, checkIn_longitude=$checkInLongitude, checkIn_address=$checkInAddress"
        )

        val call = apiServiceCheckIn.submitForm(
            "${Const.BEARER} $accessToken", map, selfiePart, outletSelfiePart
        )

        call.enqueue(object : Callback<CheckInResponse> {
            override fun onResponse(
                call: Call<CheckInResponse>,
                response: Response<CheckInResponse>
            ) {
                if (response.isSuccessful) {
                    listener.onSuccess(response.body()!!)
                } else {
                    Log.e(TAG, "API error: ${response.code()} ${response.message()}")
                    response.errorBody()?.let {
                        try {
                            val errorBodyString = it.string()
                            val jsonObject = JSONObject(errorBodyString)
                            val errorMessage = jsonObject.getString("error")

                            showErrorDialog(errorMessage)

                        } catch (e: IOException) {
                            Log.e(TAG_THREE, e.toString())
                        } catch (e: JSONException) {
                            Log.e(TAG_THREE, e.toString())
                        }
                    }
                    listener.onFailure("API error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<CheckInResponse>, t: Throwable) {
                listener.onFailure("Network error: ${t.message}")
                Log.e(TAG, "Network error: ${t.message}", t)
            }
        })
    }


    private fun showCheckInDialog() {
        // Inflate the dialog layout
        val dialogView = layoutInflater.inflate(R.layout.item_check_in_pop_up, null) ?: run {
            Log.e(TAG, "Error loading dialog view")
            Toast.makeText(this, Const.ERROR_LOADING_DIALOG, Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this, R.style.TransparentAlertDialog)
        builder.setView(dialogView)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Show the dialog
        alertDialog.show()

        // Find views inside the dialog
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok_check_in)
        val imgCancel: ImageView = dialogView.findViewById(R.id.imgCancel)
        val imgTick: ImageView = dialogView.findViewById(R.id.imgTick)

        val cancelIcon = R.drawable.img_cross
        imgCancel.setImageResource(cancelIcon)

        val tickIcon = R.drawable.img_tick
        imgTick.setImageResource(tickIcon)

        // Handle the OK button click
        btnOk.setOnClickListener {
            alertDialog.dismiss()
            LoaderUtils.showLoader()

            val userRole = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_ROLE)
            if (userRole == "warehouseManager") {
                // Go to survey activity
                startActivity(Intent(this, DashboardActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                })
                LoaderUtils.hideLoader()
                finish()
            } else {
                startActivity(Intent(this, DashboardActivity::class.java))
                LoaderUtils.hideLoader()
            }
        }

        // Handle Cancel (cross) button click
        imgCancel.setOnClickListener { alertDialog.dismiss() }
    }


    private fun showErrorDialog(error: String) {
        val dialogView = layoutInflater.inflate(R.layout.item_error_pop_up, null) ?: run {
            Toast.makeText(this, Const.ERROR_LOADING_DIALOG, Toast.LENGTH_SHORT).show()
            return
        }

        val txtOk = dialogView.findViewById<TextView>(R.id.txt_description)
        Log.d(TAG_FOUR, "Error: $error")
        txtOk.text = error

        val builder = AlertDialog.Builder(this, R.style.TransparentAlertDialog)
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Show the dialog
        alertDialog.show()

        // Handle the OK button click
        val btnOk = dialogView.findViewById<Button>(R.id.btn_ok)
        val imgCancel = dialogView.findViewById<ImageView>(R.id.imgCancel)

        val cancelIcon = R.drawable.img_cross
        imgCancel.setImageResource(cancelIcon)

        btnOk.setOnClickListener {
            startActivity(Intent(this, DashboardActivity::class.java))
            alertDialog.dismiss()
            finish()
        }

        imgCancel.setOnClickListener { alertDialog.dismiss() }
    }

    override fun getResources(): Resources {
        val res = super.getResources()
        val config = Configuration(res.configuration)
        config.fontScale = 1.0f // Force fixed font size for the entire app
        res.updateConfiguration(config, res.displayMetrics)
        return res
    }

    private fun showTimeoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.item_time_out_pop_up, null)
        if (dialogView == null) {
            Toast.makeText(this, Const.ERROR_LOADING_DIALOG, Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this, R.style.TransparentAlertDialog)
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Show the dialog
        alertDialog.show()

        // Handle the OK button click
        val btnOk: Button = dialogView.findViewById(R.id.btn_ok)
        val imgCancel: ImageView = dialogView.findViewById(R.id.imgCancel)

        val cancelIcon = R.drawable.img_cross
        imgCancel.setImageResource(cancelIcon)

        btnOk.setOnClickListener {
            submitImages()
            alertDialog.dismiss()
        }

        imgCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun showCheckInternetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.item_check_internet_pop_up, null)
        if (dialogView == null) {
            Toast.makeText(this, Const.ERROR_LOADING_DIALOG, Toast.LENGTH_SHORT).show()
            return
        }

        val builder = AlertDialog.Builder(this, R.style.TransparentAlertDialog)
            .setView(dialogView)
            .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Show the dialog
        alertDialog.show()

        // Handle the OK button click
        val btnOk: Button = dialogView.findViewById(R.id.btn_ok)
        val imgCancel: ImageView = dialogView.findViewById(R.id.imgCancel)

        val cancelIcon = R.drawable.img_cross
        imgCancel.setImageResource(cancelIcon)

        btnOk.setOnClickListener {
            alertDialog.dismiss()
        }

        imgCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }


}