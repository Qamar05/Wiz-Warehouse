package com.vms.wizwarehouse.distribute_history;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.distribute_view_details.ViewDistributeDetailsActivity


class DistributeAdapter(private val itemList: List<DistributeHistoryItem>) :
    RecyclerView.Adapter<DistributeAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val view: TextView = itemView.findViewById(R.id.txt_view)

        fun bind(item: DistributeHistoryItem) {
            view.setOnClickListener {
                val intent = Intent(view.context, ViewDistributeDetailsActivity::class.java)
                view.context.startActivity(intent)
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
}
