package com.vms.wizwarehouse.inventory_out_view_details

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.inventory_in_view_details.ViewInventoryInImagesAdapter
import com.vms.wizwarehouse.inventory_in_view_details.ViewInventoryInImagesAdapter.ImageViewHolder

class ViewInventoryOutImagesAdapter(
    private val context: Context,
    private val imageUrls: List<Int>,
    private val onMoreClick: (List<Int>) -> Unit // callback for dialog
) : RecyclerView.Adapter<ViewInventoryOutImagesAdapter.ImageViewHolder>() {

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgItem)
        val overlayText: TextView = itemView.findViewById(R.id.txtOverlay)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewInventoryOutImagesAdapter.ImageViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_inventory_image, parent, false)
        return ImageViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewInventoryOutImagesAdapter.ImageViewHolder, position: Int) {
        val imageUrl = imageUrls[position]

        Glide.with(context)
            .load(imageUrl)
            .centerCrop()
            .into(holder.imageView)

        // If second image and more exist → show overlay
        if (position == 1 && imageUrls.size > 2) {
            holder.overlayText.visibility = View.VISIBLE
            holder.overlayText.text = "+${imageUrls.size - 1}\n more"

            holder.itemView.setOnClickListener {
                onMoreClick(imageUrls) // show all in dialog
            }
        } else {
            holder.overlayText.visibility = View.GONE
            holder.itemView.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int {
        return if (imageUrls.size > 2) 2 else imageUrls.size
    }
}
