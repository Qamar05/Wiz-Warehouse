package com.vms.wizwarehouse.inventory_in.fragment_inventory_in_product_details

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.imageview.ShapeableImageView
import com.vms.wizwarehouse.R

class ImageAdapterIn(private val images: List<ProductImage>) :
    RecyclerView.Adapter<ImageAdapterIn.ImageViewHolder>() {

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ShapeableImageView = itemView.findViewById(R.id.image_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view_product_img, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val productImage = images[position]
        holder.imageView.setImageURI(productImage.uri)
        // If using Glide/Picasso for URL:
        // Glide.with(holder.itemView).load(productImage.url).into(holder.imageView)
    }

    override fun getItemCount(): Int = images.size
}
