package com.vms.wizwarehouse.inventory_in_history

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.inventory_in_view_details.ViewInventoryInDetailsActivity

class InventoryInAdapter(
    private var itemList: List<InventoryInHistoryItem>
) : RecyclerView.Adapter<InventoryInAdapter.ViewHolder>() {

    fun updateData(filtered: List<InventoryInHistoryItem>) {
        itemList = filtered
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val viewText: TextView = itemView.findViewById(R.id.txt_view)

        fun bind(item: InventoryInHistoryItem) {
            // Bind any data you want to show
            viewText.setOnClickListener {
                val intent = Intent(it.context, ViewInventoryInDetailsActivity::class.java)
                it.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inventory_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    fun getDateForPosition(position: Int): String {
        return itemList[position].date
    }
}
