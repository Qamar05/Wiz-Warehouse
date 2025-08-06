package com.vms.wizwarehouse.distribute_view_details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vms.wizwarehouse.R

class ViewDistributeImagesAdapter(
        private val context: Context,
        private val imageUrls: List<Int> // List of drawable resource IDs
) : RecyclerView.Adapter<ViewDistributeImagesAdapter.ImageViewHolder>() {

class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val imageView: ImageView = itemView.findViewById(R.id.image_view)
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
    val view = LayoutInflater.from(context).inflate(R.layout.item_view_product_img, parent, false)
    return ImageViewHolder(view)
}

override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
    val imageUrl = imageUrls[position]

    Glide.with(context)
            .load(imageUrl)
            .centerCrop()
            .into(holder.imageView)
}

override fun getItemCount(): Int = imageUrls.size
}
