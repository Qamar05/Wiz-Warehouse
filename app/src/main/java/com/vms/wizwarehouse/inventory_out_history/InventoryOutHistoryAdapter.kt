package com.vms.wizwarehouse.inventory_out_history

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.inventory_out_view_details.ViewInventoryOutDetailsActivity
import com.vms.wizwarehouse.return_inventory.ReturnInventoryActivity


class InventoryOutHistoryAdapter(private var itemList: List<InventoryOutHistoryItem>) :
    RecyclerView.Adapter<InventoryOutHistoryAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val view: TextView = itemView.findViewById(R.id.txt_view)
        private val returnInventory: TextView = itemView.findViewById(R.id.txt_return)

        fun bind(item: InventoryOutHistoryItem) {
            view.setOnClickListener {
                val intent = Intent(view.context, ViewInventoryOutDetailsActivity::class.java)
                view.context.startActivity(intent)
            }

            returnInventory.setOnClickListener {
                val intent = Intent(returnInventory.context, ReturnInventoryActivity::class.java)
                returnInventory.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_distribute_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size

    fun getDateForPosition(position: Int): String {
        return itemList[position].date
    }

    fun updateData(filtered: List<InventoryOutHistoryItem>) {
        itemList = filtered
        notifyDataSetChanged()
    }

}
