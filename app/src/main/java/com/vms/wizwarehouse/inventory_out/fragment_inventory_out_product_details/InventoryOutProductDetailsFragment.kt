package com.vms.wizwarehouse.inventory_out.fragment_inventory_out_product_details

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.databinding.FragmentInventoryOutProductDetailsBinding
import com.vms.wizwarehouse.inventory_in.fragment_inventory_in_product_details.InventoryInAdapter
import com.vms.wizwarehouse.inventory_in.fragment_inventory_in_product_details.ProductImage
import com.vms.wizwarehouse.inventory_out.distributer_details.ImageItem
import com.vms.wizwarehouse.inventory_out.distributer_details.ImagePreviewAdapter
import com.vms.wizwarehouse.utils.Utility
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class InventoryOutProductDetailsFragment : Fragment() {
    private lateinit var _binding: FragmentInventoryOutProductDetailsBinding
//    var productCategory: Spinner? = null
//    var productSubCategory: Spinner? = null
//    var productName: Spinner? = null
//    var itemNumber: EditText? = null
//    var cameraProduct: ImageView? = null
    var addProduct: ImageView? = null
    var previous: TextView? = null
    var submit: Button? = null
    var version: TextView? = null

    private var productImageUri: Uri? = null
    val CAMERA_REQUEST_CODE_PRODUCT: Int = 106
    var fusedLocationClient: FusedLocationProviderClient? = null
    private var currentAddress = ""

//    private var recyclerProductImages: RecyclerView? = null
    private val productImages = mutableListOf<ImageItem>()
    private var productAdapter: ImagePreviewAdapter? = null

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: InventoryOutAdapter


    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryOutProductDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        productCategory = binding.spinCategory
//        productSubCategory = binding.spinSubCategory
//        productName = binding.spinProductName
//        itemNumber = binding.editItemNumber
//        cameraProduct = binding.cameraProduct
        addProduct = binding.imgAddProduct
        previous = binding.txtPrevious
        submit = binding.btnSubmit
        version = binding.txtVersion
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        recyclerView = binding.recyclerViewProductsList

// start with an empty list
        adapter = InventoryOutAdapter(mutableListOf())

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        addProduct?.setOnClickListener {
            openAddProductDialog()
        }

        val categories: List<String> = mutableListOf("Electronics", "Clothing", "Groceries")
        val subCategories: List<String> = mutableListOf("Mobiles", "Laptops", "Accessories")
        val productNames: List<String> = mutableListOf("iPhone", "MacBook", "Charger")

        val categoryAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val subCategoryAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, subCategories)
        subCategoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val productNameAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, productNames)
        productNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        // 3. Set adapters to spinners
//        productCategory!!.adapter = categoryAdapter
//        productSubCategory!!.adapter = subCategoryAdapter
//        productName!!.adapter = productNameAdapter

//        recyclerProductImages = binding.recyclerProductImages
        productAdapter = ImagePreviewAdapter(requireContext(), productImages, object : ImagePreviewAdapter.OnImageRemoveListener {
            override fun onRemove(pos: Int) {
                if (productImages.isEmpty()) {
                    // show empty placeholder
                }
            }
        })

//        recyclerProductImages!!.layoutManager =
//            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//        recyclerProductImages!!.adapter = productAdapter

//        cameraProduct!!.setOnClickListener { v: View? -> fetchLocationAndOpenCamera("product_two") }
        version!!.text = (Utility.setVersionName(requireContext()))

        previous!!.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        submit!!.setOnClickListener {
//            if (productCategory!!.selectedItem.toString().isEmpty()) {
//                Toast.makeText(context, "Please select a product category", Toast.LENGTH_SHORT)
//                    .show()
//                return@setOnClickListener
//            }
//
//            if (productSubCategory!!.selectedItem.toString().isEmpty()) {
//                Toast.makeText(context, "Please select a sub-category", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (productName!!.selectedItem.toString().isEmpty()) {
//                Toast.makeText(context, "Please select a product name", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (TextUtils.isEmpty(itemNumber!!.text.toString().trim { it <= ' ' })) {
//                Toast.makeText(context, "Please enter item number", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//
//            if (productImages == null || productImages.isEmpty()) {
//                Toast.makeText(
//                    context,
//                    "Please upload at least one Product image",
//                    Toast.LENGTH_SHORT
//                ).show()
//                return@setOnClickListener
//            }


            // All validations passed
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
                "product_two" -> if (productImages.size >= 5) {
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
        startActivityForResult(
            intent,
            CAMERA_REQUEST_CODE_PRODUCT
        )
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
        val dialogViewFragmentOne = layoutInflater.inflate(R.layout.item_submit_pop_up, null)
        if (dialogViewFragmentOne == null) {
            Toast.makeText(requireContext(), "Error loading dialog", Toast.LENGTH_SHORT).show()
            return
        }

        val builderFragmentOne = AlertDialog.Builder(requireContext(), R.style.TransparentAlertDialog)
        builderFragmentOne.setView(dialogViewFragmentOne)
        builderFragmentOne.setCancelable(false)
        val alertDialog = builderFragmentOne.create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        alertDialog.show()

        val btnOkFragmentOne: Button = dialogViewFragmentOne.findViewById(R.id.btn_ok)
        val imgCancelFragmentOne: ImageView = dialogViewFragmentOne.findViewById(R.id.imgCancel)
        val imgTick: ImageView = dialogViewFragmentOne.findViewById(R.id.imgTick)

        imgCancelFragmentOne.setImageResource(R.drawable.img_cross)
        imgTick.setImageResource(R.drawable.img_smile)

        btnOkFragmentOne.setOnClickListener {
            alertDialog.dismiss()
            requireActivity().finish()
        }

        imgCancelFragmentOne.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun openAddProductDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_inventory_out, null)
        val dialog = AlertDialog.Builder(requireContext(), R.style.TransparentAlertDialog)
            .setView(dialogView).setCancelable(false)
        val alertDialog = dialog.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()
        alertDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // find views
        val spinCategory = dialogView.findViewById<Spinner>(R.id.spin_category)
        val spinSubCategory = dialogView.findViewById<Spinner>(R.id.spin_sub_category)
        val spinProductName = dialogView.findViewById<Spinner>(R.id.spin_product_name)
        val editItemNumber = dialogView.findViewById<EditText>(R.id.edit_item_number)
        val editManufacturingDate = dialogView.findViewById<TextView>(R.id.edit_manufacturing_date)
        val btnAdd = dialogView.findViewById<Button>(R.id.btn_add)
        val cameraProduct = dialogView.findViewById<ImageView>(R.id.camera_product_img_out)
        val recyclerProductImages = dialogView.findViewById<RecyclerView>(R.id.recycler_product_images_2)

        // product images list
        productAdapter = ImagePreviewAdapter(
            requireContext(), productImages, object : ImagePreviewAdapter.OnImageRemoveListener {
                override fun onRemove(position: Int) {
                    if (productImages.isEmpty()) {
                        // show empty placeholder
                    }
                }
            })
        recyclerProductImages.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerProductImages.adapter = productAdapter

        // camera click
        cameraProduct.setOnClickListener { fetchLocationAndOpenCamera("product_two") }

        // date picker
        editManufacturingDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(selectedYear, selectedMonth, selectedDay)
                    editManufacturingDate.text = sdf.format(selectedCalendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        // spinners (static for now)
        spinCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("Electronics", "Furniture", "Food"))
        spinSubCategory.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("Laptop", "Chair", "Snacks"))
        spinProductName.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, listOf("Dell XPS", "Office Chair", "Chips"))


        // Add button
        btnAdd.setOnClickListener {
            val newItem = InventoryItemOut(
                category = spinCategory.selectedItem?.toString() ?: "",
                subCategory = spinSubCategory.selectedItem?.toString() ?: "",
                productName = spinProductName.selectedItem?.toString() ?: "",
                date = editManufacturingDate.text.toString(),
                itemNumber = editItemNumber.text.toString(),
                images =  productImages.map { imageItem ->
                    ProductImageOut(imageItem.uri)
                }
            )

            (recyclerView.adapter as? InventoryOutAdapter)?.addItem(newItem)
            productImages.clear()
            productAdapter?.notifyDataSetChanged()
            alertDialog.dismiss()
        }
    }

}