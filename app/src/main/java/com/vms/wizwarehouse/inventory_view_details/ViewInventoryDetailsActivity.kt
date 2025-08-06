package com.vms.wizwarehouse.inventory_view_details

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.utils.Utility

class ViewInventoryDetailsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ViewProductImagesAdapter
    private lateinit var back: ImageView
    private lateinit var version: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_inventory_details)

        back = findViewById(R.id.img_back)
        recyclerView = findViewById(R.id.recycler_product_images)
        version = findViewById(R.id.txt_version)

        back.setOnClickListener { finish() }

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val imageUrls = listOf(
                R.drawable.img_profile_demo_img,
                R.drawable.img_profile_demo_img,
                R.drawable.img_profile_demo_img,
                R.drawable.img_profile_demo_img,
                R.drawable.img_profile_demo_img
        )

        imageAdapter = ViewProductImagesAdapter(this, imageUrls)
        recyclerView.adapter = imageAdapter

        version.text = Utility.setVersionName(this)
    }
}
