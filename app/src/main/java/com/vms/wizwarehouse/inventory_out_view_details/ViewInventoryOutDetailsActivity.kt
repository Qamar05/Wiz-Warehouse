package com.vms.wizwarehouse.inventory_out_view_details;

import android.content.Intent
import android.os.Bundle;
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView;
import android.widget.LinearLayout
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide

import com.vms.wizwarehouse.R;
import com.vms.wizwarehouse.databinding.ActivityViewInventoryOutDetailsBinding
import com.vms.wizwarehouse.inventory_in_view_details.ViewInventoryInImagesAdapter
import com.vms.wizwarehouse.return_inventory.ReturnInventoryActivity
import com.vms.wizwarehouse.utils.Utility;


class ViewInventoryOutDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewInventoryOutDetailsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ViewInventoryOutImagesAdapter
    private lateinit var back: ImageView
    private lateinit var version: TextView
    private lateinit var returnProduct: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewInventoryOutDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        back = binding.imgBack
        recyclerView = binding.recyclerProductImages
        version = binding.txtVersion
        returnProduct = binding.btnReturn

        returnProduct.setOnClickListener {
            val intent =
                Intent(this@ViewInventoryOutDetailsActivity, ReturnInventoryActivity::class.java)
            startActivity(intent)
        }

        back.setOnClickListener {
            finish()
        }


        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val imageUrls = listOf(
            R.drawable.img_profile_demo_img,
            R.drawable.img_profile_demo_img,
            R.drawable.img_profile_demo_img,
            R.drawable.img_profile_demo_img,
            R.drawable.img_profile_demo_img
        )

        val adapter = ViewInventoryInImagesAdapter(this, imageUrls) { fullList ->
            showImageDialog(fullList) // opens dialog
        }
        recyclerView.adapter = adapter

        version.text = Utility.setVersionName(this)
    }

    private fun showImageDialog(images: List<Int>) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_images, null)
        val container = dialogView.findViewById<LinearLayout>(R.id.imageContainer)

        for (img in images) {
            val iv = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    500
                ).apply { setMargins(0, 20, 0, 20) }
                scaleType = ImageView.ScaleType.CENTER_CROP
            }
            Glide.with(this)
                .load(img)
                .into(iv)
            container.addView(iv)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }
}
