package com.vms.wizwarehouse.total_stock

import android.renderscript.Type
import android.text.Layout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.R

class TotalStockAdapter(
    private val itemList: List<TotalStockItem>
) : RecyclerView.Adapter<TotalStockAdapter.InventoryViewHolder>() {

    inner class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvType: TextView = itemView.findViewById(R.id.tvType)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_total_stock, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = itemList[position]
        holder.tvName.text = item.name
        holder.tvType.text = item.type
        holder.tvQuantity.text = item.quantity
        holder.tvDate.text = item.date
    }

    override fun getItemCount(): Int = itemList.size
}
