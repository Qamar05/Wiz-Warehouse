package com.vms.wizwarehouse.reports;

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle;
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity;

import com.vms.wizwarehouse.R;
import com.vms.wizwarehouse.databinding.ActivityReportsBinding
import com.vms.wizwarehouse.utils.SharedPreferenceUtils
import com.vms.wizwarehouse.utils.ShowToastUtils
import com.vms.wizwarehouse.utils.Utility

class ReportsActivity : AppCompatActivity() {
    private lateinit var binding : ActivityReportsBinding
    private lateinit var title : EditText
    private lateinit var description : EditText
    private lateinit var steps : EditText
    private lateinit var expectedBehavior : EditText
    private lateinit var actualBehavior : EditText
    private lateinit var frequency : Spinner
    private lateinit var network : Spinner
    private lateinit var upload : TextView
    private lateinit var cancel : TextView
    private lateinit var version : TextView
    private lateinit var submit : Button
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var fileList: LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileList = binding.fileList
        title = binding.editTitle
        description =binding.editDescription
        steps = binding.editSteps
        expectedBehavior = binding.editExpectedBehavior
        actualBehavior = binding.editActualBehavior
        frequency = binding.spinFrequency
        network = binding.spinNetwork
        upload = binding.txtUpload
        cancel = binding.txtCancel
        submit = binding.btnSubmit
        version = binding.txtVersion

        version.text = Utility.setVersionName(this)

        cancel.setOnClickListener {
            finish()
        }

        setupSpinners()
        upload.setOnClickListener {
            openGallery()
        }
        submit.setOnClickListener {
            when {
                title.text.toString().isEmpty() -> {
                    ShowToastUtils.showToast(this, "Please enter your title")
                }
                description.text.toString().isEmpty() -> {
                    ShowToastUtils.showToast(this, "Please enter your description")
                }
                steps.text.toString().isEmpty() -> {
                    ShowToastUtils.showToast(this, "Please enter your steps to reproduce")
                }
                expectedBehavior.text.toString().isEmpty() -> {
                    ShowToastUtils.showToast(this, "Please enter your expected behavior")
                }
                actualBehavior.text.toString().isEmpty() -> {
                    ShowToastUtils.showToast(this, "Please enter your actual behavior")
                }
                frequency.selectedItemPosition == 0 -> {
                    ShowToastUtils.showToast(this, "Please select your frequency")
                }
                network.selectedItemPosition == 0 -> {
                    ShowToastUtils.showToast(this, "Please select your network")
                }
                selectedImages.isEmpty() -> {
                    ShowToastUtils.showToast(this, "Please upload at least one screenshot")
                }
                else -> {
                    showSubmitDialog()
                }
            }
        }

    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Allow multiple selection
        }
        galleryLauncher.launch(intent)
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            handleImageSelection(result.data!!)
        }
    }

    private fun handleImageSelection(data: Intent) {
        selectedImages.clear()
        fileList.removeAllViews()

        when {
            data.clipData != null -> { // Multiple images selected
                val count = data.clipData!!.itemCount
                if (count > 3) {
                    Toast.makeText(this, "You can select up to 3 images only.", Toast.LENGTH_SHORT).show()
                    return
                }

                for (i in 0 until count) {
                    val imageUri = data.clipData!!.getItemAt(i).uri
                    selectedImages.add(imageUri)
                    addFileNameView(imageUri)
                }
            }
            data.data != null -> { // Single image selected
                if (selectedImages.size >= 3) {
                    Toast.makeText(this, "You can select up to 3 images only.", Toast.LENGTH_SHORT).show()
                    return
                }
                val imageUri = data.data!!
                        selectedImages.add(imageUri)
                addFileNameView(imageUri)
            }
        }

        if (selectedImages.isEmpty()) {
            Toast.makeText(this, "Please select at least 1 image.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addFileNameView(imageUri: Uri) {
        val fileRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(10, 10, 10, 10)
        }

        // File name TextView
        val fileNameView = TextView(this).apply {
            text = getFileName(imageUri)
            setTextColor(resources.getColor(R.color.black_2, theme)) // Use theme for compatibility
            textSize = 16f
            setPadding(10, 10, 10, 10)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Delete button (Cross icon)
        val deleteButton = ImageView(this).apply {
            setImageResource(R.drawable.img_delete)
            setPadding(10, 10, 10, 10)
            layoutParams = LinearLayout.LayoutParams(
                    (35 * resources.displayMetrics.density).toInt(),
                    (35 * resources.displayMetrics.density).toInt()
            )
            setOnClickListener {
                selectedImages.remove(imageUri)
                fileList.removeView(fileRow)
            }
        }

        fileRow.addView(fileNameView)
        fileRow.addView(deleteButton)
        fileList.addView(fileRow)
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "Unknown"

        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            }
        } else {
            uri.path?.let { path ->
                    val lastSlashIndex = path.lastIndexOf("/")
                if (lastSlashIndex != -1) {
                    fileName = path.substring(lastSlashIndex + 1)
                }
            }
        }

        return fileName
    }

    private fun setupSpinners() {
        setupFrequencySpinner()
        setupNetworkSpinner()

        // Handle Spinner Selection
        frequency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Handle frequency item selection here
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optional: handle nothing selected
            }
        }

        network.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Handle network item selection here
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Optional: handle nothing selected
            }
        }
    }

    private fun setupFrequencySpinner() {
        val frequencyOptions = listOf("Select", "Always", "More Frequent", "Less Frequent", "Few Times")
        val layoutRes = R.layout.item_spinner

        val frequencyAdapter = ArrayAdapter(this, layoutRes, R.id.txtItem, frequencyOptions)
        frequencyAdapter.setDropDownViewResource(layoutRes)
        frequency.adapter = frequencyAdapter
    }

    private fun setupNetworkSpinner() {
        val networkOptions = listOf("Select", "Mobile Data", "WiFi")
        val layoutRes = R.layout.item_spinner

        val networkAdapter = ArrayAdapter(this, layoutRes, R.id.txtItem, networkOptions)
        networkAdapter.setDropDownViewResource(layoutRes)
        network.adapter = networkAdapter
    }


    private fun showSubmitDialog() {
        val dialogView = layoutInflater.inflate(R.layout.item_report_submitted_pop_up, null)
        val builder = AlertDialog.Builder(this, R.style.TransparentAlertDialog)
                .setView(dialogView)
                .setCancelable(false)

        val alertDialog = builder.create()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog.show()

        val btnOk: Button = dialogView.findViewById(R.id.btn_ok)
        val imgCancel: ImageView = dialogView.findViewById(R.id.imgCancel)
        val imgTick: ImageView = dialogView.findViewById(R.id.imgTick)

        val cancelIcon = R.drawable.img_cross
        imgCancel.setImageResource(cancelIcon)

        val tickIcon = R.drawable.img_report_submitted
        imgTick.setImageResource(tickIcon)

        btnOk.setOnClickListener {
            updateSurvey()
            alertDialog.dismiss()
            finish()
        }

        imgCancel.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun updateSurvey() {
        TODO("Not yet implemented")
    }


}
