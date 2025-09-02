package com.vms.wizwarehouse.inventory_out_history;


data class InventoryOutHistoryItem(
        val id: String,
        val productName: String,
        val activity: String,
        val time: String,
        val units: String,
        val supervisorName: String,
        val date: String // Used for sticky header grouping
)
