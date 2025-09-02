package com.vms.wizwarehouse.inventory_in.fragment_inventory_in_general_details

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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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
import com.vms.wizwarehouse.databinding.FragmentInventoryInGeneralDetailsBinding
import com.vms.wizwarehouse.inventory_in.fragment_inventory_in_product_details.InventoryInProductDetailsFragment
import com.vms.wizwarehouse.inventory_out.distributer_details.ImageItem
import com.vms.wizwarehouse.inventory_out.distributer_details.ImagePreviewAdapter
import com.vms.wizwarehouse.utils.Utility
import java.io.IOException
import java.util.Locale

class InventoryInGeneralDetailsFragment : Fragment() {
    private var _binding: FragmentInventoryInGeneralDetailsBinding? = null
    private lateinit var version: TextView
    private lateinit var deliveryAgent: EditText
    private lateinit var cameraInvoice: ImageView
    private lateinit var cameraReceiver: ImageView
    private lateinit var cameraAgent: ImageView
    private lateinit var receiverImageUri: Uri
    private lateinit var agentImageUri: Uri
    private lateinit var invoiceImageUri: Uri
    private lateinit var next: Button


    private val LOCATION_PERMISSION_REQUEST_CODE = 1001
    private val CAMERA_REQUEST_CODE_RECEIVER = 101
    private val CAMERA_REQUEST_CODE_AGENT = 102
    private val CAMERA_REQUEST_CODE_INVOICE = 103

    private var currentAddress: String = ""
    private var lastTypeRequested: String = ""

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var receiverSignatureBitmap: Bitmap? = null
    private lateinit var receiverSignature: ImageView
    private lateinit var receiverAddSignature: TextView

    private var agentSignatureBitmap: Bitmap? = null
    private lateinit var agentSignature: ImageView
    private lateinit var agentAddSignature: TextView

    private lateinit var recyclerReceiverImages: RecyclerView
    private val receiverImages = mutableListOf<ImageItem>()
    private lateinit var receiverAdapter: ImagePreviewAdapter

    private lateinit var recyclerAgentImages: RecyclerView
    private val agentImages = mutableListOf<ImageItem>()
    private lateinit var agentAdapter: ImagePreviewAdapter

    private lateinit var recyclerInvoiceImages: RecyclerView
    private val invoiceImages = mutableListOf<ImageItem>()
    private lateinit var invoiceAdapter: ImagePreviewAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryInGeneralDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        cameraReceiver = binding.cameraReceiverImg
        receiverSignature = binding.imgSignature
        receiverAddSignature = binding.txtAdd
        cameraAgent = binding.cameraDeliveryImage
        agentSignature = binding.imgDeliveryAgentSignature
        agentAddSignature = binding.txtAdd2
        cameraInvoice = binding.cameraInvoice
        deliveryAgent = binding.editDeliveryAgent
        version = binding.txtVersion
        next = binding.btnNext

        version.setText(Utility.setVersionName(requireContext()))

        recyclerReceiverImages = binding.recyclerReceiverImages
        receiverAdapter = ImagePreviewAdapter(
            requireContext(),
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
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerReceiverImages.adapter = receiverAdapter

        recyclerAgentImages = binding.recyclerAgentImages
        agentAdapter = ImagePreviewAdapter(
            requireContext(),
            agentImages,
            object : ImagePreviewAdapter.OnImageRemoveListener {
                override fun onRemove(position: Int) {
                    if (agentImages.isEmpty()) {
                        // show empty placeholder
                    }
                }
            })

        recyclerAgentImages.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerAgentImages.adapter = agentAdapter

        recyclerInvoiceImages = binding.recyclerInvoiceImages
        invoiceAdapter = ImagePreviewAdapter(
            requireContext(),
            invoiceImages,
            object : ImagePreviewAdapter.OnImageRemoveListener {
                override fun onRemove(position: Int) {
                    if (invoiceImages.isEmpty()) {
                        // show empty placeholder
                    }
                }
            })

        recyclerInvoiceImages.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerInvoiceImages.adapter = invoiceAdapter

        receiverAddSignature.setOnClickListener { v: View? -> openSignatureDialog() }
        agentAddSignature.setOnClickListener { v: View? -> openSignatureDialogAgent() }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        cameraReceiver.setOnClickListener { v: View? ->
            lastTypeRequested = "receiver"
            fetchLocationAndOpenCamera("receiver")
        }

        cameraAgent.setOnClickListener { v: View? ->
            lastTypeRequested = "agent"
            fetchLocationAndOpenCamera("agent")
        }

        cameraInvoice.setOnClickListener { v: View? ->
            lastTypeRequested = "invoice"
            fetchLocationAndOpenCamera("invoice")
        }

        next.setOnClickListener {
//            if (receiverImages.isNullOrEmpty()) {
//                Toast.makeText(context, "Please upload at least one Receiver image", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (receiverSignature.drawable == null) {
//                Toast.makeText(context, "Please add receiver's signature", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (deliveryAgent.text.toString().trim().isEmpty()) {
//                Toast.makeText(context, "Please add delivery agent's name", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (agentImages.isNullOrEmpty()) {
//                Toast.makeText(context, "Please upload at least one Agent image", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (agentSignature.drawable == null) {
//                Toast.makeText(context, "Please add delivery agent's signature", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (invoiceImages.isNullOrEmpty()) {
//                Toast.makeText(context, "Please upload at least one Invoice image", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }

            parentFragmentManager.beginTransaction()
                .replace(R.id.frame_layout, InventoryInProductDetailsFragment())
                .addToBackStack(null)  // 👈 This is important!
                .commit()
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                receiverSignature.visibility = View.VISIBLE
                receiverSignature.setImageBitmap(receiverSignatureBitmap)
                receiverAddSignature.text = update
            }
            signatureDialog.dismiss()
        }

        signatureDialog.show()
    }

    private fun openSignatureDialogAgent() {
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
            agentSignatureBitmap = signaturePad.signatureBitmap
            if (agentSignatureBitmap != null) {
                val update = "Update"
                agentSignature.visibility = View.VISIBLE
                agentSignature.setImageBitmap(agentSignatureBitmap)
                agentAddSignature.text = update
            }
            signatureDialog.dismiss()
        }

        signatureDialog.show()
    }

    private fun fetchLocationAndOpenCamera(type: String) {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
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
                "receiver" -> if (receiverImages.size >= 5) {
                    Toast.makeText(
                        requireContext(),
                        "Maximum 5 images allowed for Receiver.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    openCameraForReceiver()
                }

                "agent" -> if (agentImages.size >= 5) {
                    Toast.makeText(
                        requireContext(),
                        "Maximum 5 images allowed for Agent.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    openCameraForAgent()
                }

                "invoice" -> if (invoiceImages.size >= 5) {
                    Toast.makeText(
                        requireContext(),
                        "Maximum 5 images allowed for Invoice.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    openCameraForInvoice()
                }
            }
        }
    }

    private fun openCameraForReceiver() {
        receiverImageUri = insertImageUri("RECEIVER_IMG_")!!
        startCameraIntent(
            receiverImageUri,
            CAMERA_REQUEST_CODE_RECEIVER
        )
    }

    private fun openCameraForAgent() {
        agentImageUri = insertImageUri("AGENT_IMG_")!!
        startCameraIntent(
            agentImageUri,
            CAMERA_REQUEST_CODE_AGENT
        )
    }

    private fun openCameraForInvoice() {
        invoiceImageUri = insertImageUri("INVOICE_IMG_")!!
        startCameraIntent(
            invoiceImageUri,
            CAMERA_REQUEST_CODE_INVOICE
        )
    }

    private fun insertImageUri(titlePrefix: String): Uri? {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, titlePrefix + System.currentTimeMillis() + ".jpg")
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        return requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
    }

    private fun startCameraIntent(uri: Uri, requestCode: Int) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, requestCode)
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
            } else if (requestCode == CAMERA_REQUEST_CODE_AGENT) {
                uri = agentImageUri
                if (uri != null && agentImages.size < 5) {
                    addImageToList(uri, agentImages, agentAdapter)
                }
            } else if (requestCode == CAMERA_REQUEST_CODE_INVOICE) {
                uri = invoiceImageUri
                if (uri != null && invoiceImages.size < 5) {
                    addImageToList(uri, invoiceImages, invoiceAdapter)
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
            val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            val watermarked: Bitmap = drawTextOnBitmap(bitmap, currentAddress)

            val outputStream = requireActivity().contentResolver.openOutputStream(uri)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_REQUEST_CODE_RECEIVER) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndOpenCamera("receiver") // or "agent" based on flow
            } else {
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndOpenCamera(lastTypeRequested) // keep a flag: "receiver" or "agent"
            } else {
                Toast.makeText(context, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


}
