package com.vms.wizwarehouse.inventory_history

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.inventory_view_details.ViewInventoryDetailsActivity

class InventoryAdapter(
        private var itemList: List<InventoryHistoryItem>
) : RecyclerView.Adapter<InventoryAdapter.ViewHolder>() {

fun updateData(filtered: List<InventoryHistoryItem>) {
    itemList = filtered
    notifyDataSetChanged()
}

inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val viewText: TextView = itemView.findViewById(R.id.txt_view)

    fun bind(item: InventoryHistoryItem) {
        // Bind any data you want to show
        viewText.setOnClickListener {
            val intent = Intent(it.context, ViewInventoryDetailsActivity::class.java)
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
