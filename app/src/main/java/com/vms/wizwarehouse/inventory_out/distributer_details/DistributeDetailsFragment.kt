package com.vms.wizwarehouse.inventory_out.distributer_details

import android.Manifest
import android.app.Activity
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.gcacace.signaturepad.views.SignaturePad
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.databinding.FragmentInventoryOutGeneralDetailsBinding
import com.vms.wizwarehouse.inventory_in.fragment_inventory_in_product_details.ProductImage
import com.vms.wizwarehouse.inventory_out.fragment_inventory_out_product_details.InventoryOutProductDetailsFragment
import com.vms.wizwarehouse.utils.Utility
import java.io.IOException
import java.util.Locale

class DistributeDetailsFragment : Fragment() {
    private lateinit var _binding: FragmentInventoryOutGeneralDetailsBinding
    var selectActivity: Spinner? = null
    var supervisorName: TextView? = null

    //    TextView receiverImgTxt;
    var cameraReceiverImg: ImageView? = null
    private var receiverSignatureBitmap: Bitmap? = null
    private var receiverSignature: ImageView? = null
    private var receiverAddSignature: TextView? = null
    var next: Button? = null
    var version: TextView? = null

    private var receiverImageUri: Uri? = null
    val CAMERA_REQUEST_CODE_PRODUCT: Int = 105
    var fusedLocationClient: FusedLocationProviderClient? = null
    private var currentAddress = ""

    private var recyclerReceiverImages: RecyclerView? = null
    private val receiverImages = mutableListOf<ImageItem>()
    private var receiverAdapter: ImagePreviewAdapter? = null

    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryOutGeneralDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        selectActivity =binding.spinActivity
        supervisorName = binding.editSupervisor
        cameraReceiverImg = binding.cameraSupervisor
        receiverSignature = binding.imgSignature
        receiverAddSignature = binding.txtAdd
        next = binding.btnNext
        version = binding.txtVersion
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        supervisorName!!.text = (resources.getText(R.string.supervisor))
        version!!.text = Utility.setVersionName(requireContext())


        val activities: List<String> = mutableListOf("Noida", "Jaipur", "Indore", "Mumbai")
        val activityAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, activities)
        activityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        selectActivity!!.adapter = activityAdapter

        recyclerReceiverImages = binding.recyclerReceiverImages
        receiverAdapter = ImagePreviewAdapter(requireContext(), receiverImages, object : ImagePreviewAdapter.OnImageRemoveListener {
            override fun onRemove(position: Int) {
                if (receiverImages.isEmpty()) {
                    // show empty placeholder
                }
            }
        })

        recyclerReceiverImages!!.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerReceiverImages!!.adapter = receiverAdapter


        cameraReceiverImg!!.setOnClickListener {
            if (receiverImages.size >= 5) {
                Toast.makeText(
                    requireContext(),
                    "You can only upload up to 5 images.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            fetchLocationAndOpenCamera("supervisor")
        }
        receiverAddSignature!!.setOnClickListener { openSignatureDialog() }

        next!!.setOnClickListener { v: View? ->
            //validations
//            if (selectActivity!!.selectedItem.toString().isEmpty()) {
//                Toast.makeText(context, "Please select an activity", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (TextUtils.isEmpty(supervisorName!!.text.toString().trim { it <= ' ' })) {
//                Toast.makeText(context, "Please enter supervisor name", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (receiverImages == null || receiverImages.isEmpty()) {
//                Toast.makeText(
//                    context,
//                    "Please upload at least one Receiver image",
//                    Toast.LENGTH_SHORT
//                )
//                    .show()
//                return@setOnClickListener
//            }
//
//            if (receiverSignature!!.drawable == null) {
//                Toast.makeText(context, "Please add supervisor signature", Toast.LENGTH_SHORT)
//                    .show()
//                return@setOnClickListener
//            }


            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, InventoryOutProductDetailsFragment())
                .addToBackStack(null)  // 👈 This is important!
                .commit()
        }

        return root
    }

    private fun fetchLocationAndOpenCamera(type: String) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationClient!!.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val geocoder = Geocoder(requireContext(), Locale.getDefault())
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
                "supervisor" -> if (receiverImages.size >= 5) {
                    Toast.makeText(
                        requireContext(),
                        "Maximum 5 images allowed for Receiver.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    openCameraForProduct()
                }
            }
        }
    }

    private fun openCameraForProduct() {
        val values = ContentValues()
        values.put(
            MediaStore.Images.Media.TITLE,
            "SUPERVISOR_IMG_" + System.currentTimeMillis() + ".jpg"
        )
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        receiverImageUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, receiverImageUri)
        startActivityForResult(intent, CAMERA_REQUEST_CODE_PRODUCT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            var uri: Uri? = null

            if (requestCode == CAMERA_REQUEST_CODE_PRODUCT) {
                uri = receiverImageUri
            }

            if (uri != null) {
                try {
                    val bitmap =
                        MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                    val watermarked: Bitmap = drawTextOnBitmap(bitmap, currentAddress)

                    val outputStream = requireActivity().contentResolver.openOutputStream(uri)
                    if (outputStream != null) {
                        watermarked.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                        outputStream.close()
                    }

                    // ADD URI to RecyclerView
                    receiverImages.add(ImageItem(uri))
                    receiverAdapter!!.notifyItemInserted(receiverImages.size - 1)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
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

    private fun openSignatureDialog() {
        val layoutRes = R.layout.dialog_signature

        val signatureDialog = Dialog(requireContext())
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
                receiverSignature!!.visibility = View.VISIBLE
                receiverSignature!!.setImageBitmap(receiverSignatureBitmap)
                receiverAddSignature!!.text = update
            }
            signatureDialog.dismiss()
        }

        signatureDialog.show()
    }

}