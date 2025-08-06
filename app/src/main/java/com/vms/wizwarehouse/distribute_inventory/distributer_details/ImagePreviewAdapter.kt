package com.vms.wizwarehouse.distribute_inventory.distributer_details;

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.R


class ImagePreviewAdapter(
    private val context: Context,
    private val imageList: MutableList<ImageItem>,
    private val removeListener: OnImageRemoveListener
) : RecyclerView.Adapter<ImagePreviewAdapter.ImageViewHolder>() {

    interface OnImageRemoveListener {
        fun onRemove(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_image_preview, parent, false)
        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.thumb.setImageURI(imageList[position].uri)
        holder.remove.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos != RecyclerView.NO_POSITION && pos < imageList.size) {
                imageList.removeAt(pos)
                notifyItemRemoved(pos)
                removeListener.onRemove(pos)
            }
        }
    }

    override fun getItemCount(): Int = imageList.size

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumb: ImageView = itemView.findViewById(R.id.image_thumb)
        val remove: ImageView = itemView.findViewById(R.id.image_remove)
    }
}