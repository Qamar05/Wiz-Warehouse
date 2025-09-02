package com.vms.wizwarehouse.today_activity

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.R
import org.w3c.dom.Text

class TodayActivityAdapter(
    private val inventoryList: List<TodayActivityItem>
) : RecyclerView.Adapter<TodayActivityAdapter.InventoryViewHolder>() {

    class InventoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtId: TextView = itemView.findViewById(R.id.txt_id)
        val txtActivityType: TextView = itemView.findViewById(R.id.txt_activity_type)
        val txtSupervisor: TextView = itemView.findViewById(R.id.txt_supervisor)
        val txtFwp: TextView = itemView.findViewById(R.id.txt_fwp)
        val txtTimeRange: TextView = itemView.findViewById(R.id.txt_time_range)
        val txtOutlet: TextView = itemView.findViewById(R.id.txt_outlet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_today_activity, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = inventoryList[position]
        holder.txtId.text = "ID: ${item.id}"
        holder.txtActivityType.text = item.activityType
        holder.txtSupervisor.text = "Supervisor: ${item.supervisor}"
        holder.txtFwp.text = "Fwp: ${item.fwp}"
        holder.txtTimeRange.text = item.timeRange
        holder.txtOutlet.text = item.outlet
    }

    override fun getItemCount(): Int = inventoryList.size
}
