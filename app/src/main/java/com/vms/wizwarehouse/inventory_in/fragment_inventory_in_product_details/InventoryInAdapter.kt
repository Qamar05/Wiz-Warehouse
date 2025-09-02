package com.vms.wizwarehouse.inventory_in.fragment_inventory_in_product_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.databinding.ItemInventoryInBinding

class InventoryInAdapter(
    val items: MutableList<InventoryItem>
) : RecyclerView.Adapter<InventoryInAdapter.InventoryViewHolder>() {

    fun addItem(item: InventoryItem) {
        items.add(item)
        notifyItemInserted(items.size - 1)
    }

    fun removeItem(position: Int) {
        if (position in items.indices) {
            items.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = ItemInventoryInBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class InventoryViewHolder(private val binding: ItemInventoryInBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InventoryItem) {
            binding.txtCategory.text = item.category
            binding.txtProductName.text = item.productName
            binding.txtDate.text = item.date
            binding.txtRemarks.text = item.remarks
            binding.txtSubCategory.text = item.subCategory
            binding.txtQuantityUnits.text = item.quantityUnits
            binding.txtModeOfTransport.text = item.modeOfTransport

            val imageAdapter = ImageAdapterIn(item.images)
            binding.recyclerProductImages.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)

            binding.recyclerProductImages.adapter = imageAdapter
        }
    }
}
