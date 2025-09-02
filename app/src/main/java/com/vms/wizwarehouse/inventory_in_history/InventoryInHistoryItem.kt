package com.vms.wizwarehouse.inventory_in_history

data class InventoryInHistoryItem(
        val id: String,
        val productName: String,
        val deliveryAgentName: String,
        val time: String,
        val units: String,
        val date: String // This is for sticky header grouping
)
