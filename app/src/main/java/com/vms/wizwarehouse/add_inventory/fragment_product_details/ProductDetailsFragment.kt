package com.vms.wizwarehouse.add_inventory.fragment_product_details

import android.Manifest
import android.R
import android.app.Activity
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
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vms.wizwarehouse.databinding.FragmentInventoryProductDetailsBinding
import com.vms.wizwarehouse.databinding.ItemSubmitPopUpBinding
import com.vms.wizwarehouse.distribute_inventory.distributer_details.ImageItem
import com.vms.wizwarehouse.distribute_inventory.distributer_details.ImagePreviewAdapter
import com.vms.wizwarehouse.utils.Utility
import java.io.IOException
import java.util.Locale

class ProductDetailsFragment : Fragment() {
    private var _binding: FragmentInventoryProductDetailsBinding? = null
    var productCategory: Spinner? = null
    var productSubCategory: Spinner? = null
    var productName: Spinner? = null
    var quantity: EditText? = null
    var remarks: EditText? = null
    var cameraProduct: ImageView? = null
    var addProduct: ImageView? = null
    lateinit var previous: TextView
    lateinit var btnSubmit: Button
    lateinit var version: TextView
    private var productImageUri: Uri? = null
    private val productImageNameList: List<String> = ArrayList()
    private val CAMERA_REQUEST_CODE_PRODUCT: Int = 104
    var fusedLocationClient: FusedLocationProviderClient? = null
    private var currentAddress = ""

    private var recyclerProductImages: RecyclerView? = null
    private val productImages = mutableListOf<ImageItem>()
    private var productAdapter: ImagePreviewAdapter? = null


    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryProductDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        productCategory = binding.spinCategory
        productSubCategory = binding.spinSubCategory
        productName = binding.spinProductName
        quantity = binding.editQuantity
        remarks = binding.editRemarks
        cameraProduct = binding.cameraProductImg
        addProduct = binding.imgAddProduct
        previous = binding.txtPrevious
        btnSubmit = binding.btnSubmit
        version = binding.txtVersion
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        recyclerProductImages = binding.recyclerProductImages
        productAdapter = ImagePreviewAdapter(requireContext(), productImages, object : ImagePreviewAdapter.OnImageRemoveListener {
            override fun onRemove(position: Int) {
                productImages.removeAt(position)
                productAdapter!!.notifyItemRemoved(position)
            }
        })

        recyclerProductImages!!.setLayoutManager(
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        recyclerProductImages!!.setAdapter(productAdapter)

        cameraProduct!!.setOnClickListener { v: View? -> fetchLocationAndOpenCamera("product") }

        version.text = (Utility.setVersionName(requireContext()))


        // 1. Demo data
        val categories: List<String> = mutableListOf("Electronics", "Clothing", "Groceries")
        val subCategories: List<String> = mutableListOf("Mobiles", "Laptops", "Accessories")
        val productNames: List<String> = mutableListOf("iPhone", "MacBook", "Charger")

        val categoryAdapter =
            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        val subCategoryAdapter =
            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, subCategories)
        subCategoryAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        val productNameAdapter =
            ArrayAdapter(requireContext(), R.layout.simple_spinner_item, productNames)
        productNameAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)

        // 3. Set adapters to spinners
        productCategory!!.adapter = categoryAdapter
        productSubCategory!!.adapter = subCategoryAdapter
        productName!!.adapter = productNameAdapter

        previous.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnSubmit.setOnClickListener {

//            if (productCategory!!.selectedItem.toString().isEmpty()) {
//                Toast.makeText(requireContext(), "Please select a product category", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (productSubCategory!!.selectedItem.toString().isEmpty()) {
//                Toast.makeText(requireContext(), "Please select a sub-category", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (productName!!.selectedItem.toString().isEmpty()) {
//                Toast.makeText(requireContext(), "Please select a product name", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (quantity!!.text.toString().trim().isEmpty()) {
//                Toast.makeText(requireContext(), "Please enter a quantity", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
////
////            if (remarks!!.text.toString().trim().isEmpty()) {
////                Toast.makeText(requireContext(), "Please enter remarks", Toast.LENGTH_SHORT).show()
////                return@setOnClickListener
////            }
//
//            if (productImages == null || productImages.isEmpty()) {
//                Toast.makeText(requireContext(), "Please upload at least one Product image", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }

            showSubmitDialog()
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
                "product" -> if (productImages.size >= 5) {
                    Toast.makeText(
                        requireContext(),
                        "Maximum 5 images allowed for Product.",
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
            "PRODUCT_IMG_" + System.currentTimeMillis() + ".jpg"
        )
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        productImageUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, productImageUri)
        startActivityForResult(intent, CAMERA_REQUEST_CODE_PRODUCT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            var uri: Uri? = null

            if (requestCode == CAMERA_REQUEST_CODE_PRODUCT) {
                uri = productImageUri
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
                    productImages.add(ImageItem(uri))
                    productAdapter!!.notifyItemInserted(productImages.size - 1)
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

    private fun showSubmitDialog() {
        val dialogViewFragmentOne = layoutInflater.inflate(com.vms.wizwarehouse.R.layout.item_submit_pop_up, null)
        if (dialogViewFragmentOne == null) {
            Toast.makeText(requireContext(), "Error loading dialog", Toast.LENGTH_SHORT).show()
            return
        }

        val builderFragmentOne = android.app.AlertDialog.Builder(requireContext(), com.vms.wizwarehouse.R.style.TransparentAlertDialog)
        builderFragmentOne.setView(dialogViewFragmentOne)
        builderFragmentOne.setCancelable(false)
        val alertDialog = builderFragmentOne.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()

        val btnOkFragmentOne: Button = dialogViewFragmentOne.findViewById(com.vms.wizwarehouse.R.id.btn_ok)
        val imgCancelFragmentOne: ImageView = dialogViewFragmentOne.findViewById(com.vms.wizwarehouse.R.id.imgCancel)
        val imgTick: ImageView = dialogViewFragmentOne.findViewById(com.vms.wizwarehouse.R.id.imgTick)

        imgCancelFragmentOne.setImageResource(com.vms.wizwarehouse.R.drawable.img_cross)
        imgTick.setImageResource(com.vms.wizwarehouse.R.drawable.img_smile)

        btnOkFragmentOne.setOnClickListener {
            alertDialog.dismiss()
            requireActivity().finish()
        }

        imgCancelFragmentOne.setOnClickListener {
            alertDialog.dismiss()
        }
    }

}