package com.vms.wizwarehouse.inventory_out.distributer_details;

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
        if (position in imageList.indices) {
            holder.thumb.setImageURI(imageList[position].uri)

            holder.remove.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION && pos in imageList.indices) {
                    imageList.removeAt(pos)

                    if (imageList.isEmpty()) {
                        notifyDataSetChanged()
                    } else {
                        notifyItemRemoved(pos)
                    }

                    removeListener.onRemove(pos)
                }
            }
        }
    }

    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val thumb: ImageView = itemView.findViewById(R.id.image_thumb)
        val remove: ImageView = itemView.findViewById(R.id.image_remove)
    }

    override fun getItemCount(): Int = imageList.size.coerceAtLeast(0)

}