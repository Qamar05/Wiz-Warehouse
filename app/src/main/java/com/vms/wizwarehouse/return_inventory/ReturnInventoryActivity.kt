package com.vms.wizwarehouse.return_inventory

import android.Manifest
import android.app.Activity
import android.app.ComponentCaller
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.databinding.ActivityReturnInventoryBinding
import com.vms.wizwarehouse.inventory_out.distributer_details.ImageItem
import com.vms.wizwarehouse.inventory_out.distributer_details.ImagePreviewAdapter
import java.io.IOException
import java.util.Locale

class ReturnInventoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReturnInventoryBinding
    private lateinit var back: ImageView

    private var receiverSignatureBitmap: Bitmap? = null
    private lateinit var receiverSignature: ImageView
    private lateinit var receiverAddSignature: TextView
    private lateinit var recyclerReceiverImages: RecyclerView
    private val receiverImages = mutableListOf<ImageItem>()
    private lateinit var receiverAdapter: ImagePreviewAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var currentAddress: String = ""
    private lateinit var receiverImageUri: Uri
    private val CAMERA_REQUEST_CODE_RECEIVER = 101
    private lateinit var cameraReceiver: ImageView
    private var lastTypeRequested: String = ""
    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private lateinit var btnReturn: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReturnInventoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        back = binding.imgBack
        receiverSignature = binding.imgSignature
        receiverAddSignature = binding.txtAdd
        cameraReceiver = binding.cameraDeliveryImage
        btnReturn = binding.btnReturn
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        cameraReceiver.setOnClickListener { v: View? ->
            lastTypeRequested = "receiver"
            fetchLocationAndOpenCamera("receiver")
        }

        receiverAddSignature.setOnClickListener { v: View? -> openSignatureDialog() }

        recyclerReceiverImages = binding.recyclerProductImages
        receiverAdapter = ImagePreviewAdapter(
            this,
            receiverImages,
            object : ImagePreviewAdapter.OnImageRemoveListener {
                override fun onRemove(position: Int) {
                    // No need to remove again, adapter already did it
                    // Just handle UI updates or show empty state if needed
                    if (receiverImages.isEmpty()) {
                        // show empty placeholder
                    }
                }
            })

        recyclerReceiverImages.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerReceiverImages.adapter = receiverAdapter


        back.setOnClickListener {
            finish()
        }

        btnReturn.setOnClickListener {
            showSubmitDialog()
        }

    }

    private fun openSignatureDialog() {
        val layoutRes = R.layout.dialog_signature

        val signatureDialog = Dialog(this)
        signatureDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        signatureDialog.setContentView(layoutRes)
        signatureDialog.setCancelable(false)
        val signaturePad = signatureDialog.findViewById<SignaturePad>(R.id.signature_pad)
        val btnClear = signatureDialog.findViewById<TextView>(R.id.txt_clear)
        val btnDone = signatureDialog.findViewById<Button>(R.id.btn_done)
        val cross = signatureDialog.findViewById<ImageView>(R.id.img_cross)

        cross.setOnClickListener { v: View? -> signatureDialog.dismiss() }
        btnClear.setOnClickListener { v: View? -> signaturePad.clear() }

        btnDone.setOnClickListener { v: View? ->
            receiverSignatureBitmap = signaturePad.signatureBitmap
            if (receiverSignatureBitmap != null) {
                val update = "Update"
                receiverSignature.visibility = View.VISIBLE
                receiverSignature.setImageBitmap(receiverSignatureBitmap)
                receiverAddSignature.text = update
            }
            signatureDialog.dismiss()
        }

        signatureDialog.show()
    }

    private fun fetchLocationAndOpenCamera(type: String) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val geocoder = Geocoder(this, Locale.getDefault())
                try {
                    val addresses =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)
                    if (!addresses!!.isEmpty()) {
                        val address = addresses!![0]

                        val sb = StringBuilder()
                        if (address.subThoroughfare != null) sb.append(address.subThoroughfare)
                            .append(", ")
                        if (address.thoroughfare != null) sb.append(address.thoroughfare)
                            .append(", ")
                        if (address.subLocality != null) sb.append(address.subLocality).append(", ")
                        if (address.locality != null) sb.append(address.locality).append(", ")
                        if (address.adminArea != null) sb.append(address.adminArea).append(", ")
                        if (address.postalCode != null) sb.append(address.postalCode).append(", ")

                        if (sb.length > 0) sb.setLength(sb.length - 2)
                        currentAddress = sb.toString()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            when (type) {
                "receiver" -> if (receiverImages.size >= 5) {
                    Toast.makeText(
                        this,
                        "Maximum 5 images allowed for Receiver.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    openCameraForReceiver()
                }


            }
        }
    }

    private fun addImageToList(
        uri: Uri,
        imageList: MutableList<ImageItem>,
        adapter: ImagePreviewAdapter
    ) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
            val watermarked: Bitmap = drawTextOnBitmap(bitmap, currentAddress)

            val outputStream = this.contentResolver.openOutputStream(uri)
            if (outputStream != null) {
                watermarked.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.close()
            }

            imageList.add(ImageItem(uri))
            adapter.notifyItemInserted(imageList.size - 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun drawTextOnBitmap(bitmap: Bitmap, text: String): Bitmap {
        val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)

        val paint = Paint()
        paint.color = Color.WHITE
        paint.textSize = 40f
        paint.setShadowLayer(1f, 0f, 1f, Color.BLACK)
        paint.isAntiAlias = true

        val x = 20
        val y = result.height - 150

        val maxWidth = result.width - 40
        val staticLayout = StaticLayout(
            text,
            TextPaint(paint),
            maxWidth,
            Layout.Alignment.ALIGN_NORMAL,
            1.2f,
            0.0f,
            false
        )
        canvas.save()
        canvas.translate(x.toFloat(), y.toFloat())
        staticLayout.draw(canvas)
        canvas.restore()

        return result
    }

    private fun openCameraForReceiver() {
        receiverImageUri = insertImageUri("RECEIVER_IMG_")!!
        startCameraIntent(
            receiverImageUri,
            CAMERA_REQUEST_CODE_RECEIVER
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            var uri: Uri? = null

            if (requestCode == CAMERA_REQUEST_CODE_RECEIVER) {
                uri = receiverImageUri
                if (uri != null && receiverImages.size < 5) {
                    addImageToList(uri, receiverImages, receiverAdapter)
                }
            }
        }
    }

    private fun insertImageUri(titlePrefix: String): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, titlePrefix + System.currentTimeMillis() + ".jpg")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        return this.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
    }

    private fun startCameraIntent(uri: Uri, requestCode: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, requestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_REQUEST_CODE_RECEIVER) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndOpenCamera("receiver") // or "agent" based on flow
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndOpenCamera(lastTypeRequested) // keep a flag: "receiver" or "agent"
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSubmitDialog() {
        val dialogViewFragmentOne =
            layoutInflater.inflate(com.vms.wizwarehouse.R.layout.item_submit_pop_up, null)
        if (dialogViewFragmentOne == null) {
            Toast.makeText(this, "Error loading dialog", Toast.LENGTH_SHORT).show()
            return
        }

        val builderFragmentOne = android.app.AlertDialog.Builder(
            this, com.vms.wizwarehouse.R.style.TransparentAlertDialog
        )
        builderFragmentOne.setView(dialogViewFragmentOne)
        builderFragmentOne.setCancelable(false)
        val alertDialog = builderFragmentOne.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()

        val btnOkFragmentOne: Button =
            dialogViewFragmentOne.findViewById(com.vms.wizwarehouse.R.id.btn_ok)
        val imgCancelFragmentOne: ImageView =
            dialogViewFragmentOne.findViewById(com.vms.wizwarehouse.R.id.imgCancel)
        val imgTick: ImageView =
            dialogViewFragmentOne.findViewById(com.vms.wizwarehouse.R.id.imgTick)

        imgCancelFragmentOne.setImageResource(com.vms.wizwarehouse.R.drawable.img_cross)
        imgTick.setImageResource(com.vms.wizwarehouse.R.drawable.img_smile)

        btnOkFragmentOne.setOnClickListener {
            alertDialog.dismiss()
            finish()
        }

        imgCancelFragmentOne.setOnClickListener {
            alertDialog.dismiss()
        }
    }

}
