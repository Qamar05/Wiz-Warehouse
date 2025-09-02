package com.vms.wizwarehouse.inventory_out.fragment_inventory_out_product_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.databinding.ItemInventoryOutBinding


class InventoryOutAdapter(
    val items: MutableList<InventoryItemOut>
) : RecyclerView.Adapter<InventoryOutAdapter.InventoryViewHolder>() {

    fun addItem(item: InventoryItemOut) {
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
        val binding =
            ItemInventoryOutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class InventoryViewHolder(private val binding: ItemInventoryOutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: InventoryItemOut) {
            binding.txtCategory.text = item.category
            binding.txtProductName.text = item.productName
            binding.txtDate.text = item.date
            binding.txtSubCategory.text = item.subCategory
            binding.txtItemNumber.text = item.itemNumber

            val imageAdapter = ImageAdapterOut(item.images)
            binding.recyclerProductImages.layoutManager =
                LinearLayoutManager(binding.root.context, LinearLayoutManager.HORIZONTAL, false)
            binding.recyclerProductImages.adapter = imageAdapter
        }
    }

}
