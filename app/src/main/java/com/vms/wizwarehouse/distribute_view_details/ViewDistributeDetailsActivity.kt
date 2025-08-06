package com.vms.wizwarehouse.distribute_view_details;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vms.wizwarehouse.R;
import com.vms.wizwarehouse.databinding.ActivityViewDistributeDetailsBinding
import com.vms.wizwarehouse.utils.Utility;


class ViewDistributeDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewDistributeDetailsBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var imageAdapter: ViewDistributeImagesAdapter
    private lateinit var back: ImageView
    private lateinit var version: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewDistributeDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        back = binding.imgBack
        recyclerView = binding.recyclerProductImages
        version = binding.txtVersion

        back.setOnClickListener {
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val imageUrls = listOf(
                R.drawable.img_profile_demo_img,
                R.drawable.img_profile_demo_img,
                R.drawable.img_profile_demo_img,
                R.drawable.img_profile_demo_img,
                R.drawable.img_profile_demo_img
        )

        imageAdapter = ViewDistributeImagesAdapter(this, imageUrls)
        recyclerView.adapter = imageAdapter

        version.text = Utility.setVersionName(this)
    }
}
