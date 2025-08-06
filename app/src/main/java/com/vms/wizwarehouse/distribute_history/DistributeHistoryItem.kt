package com.vms.wizwarehouse.distribute_history;


data class DistributeHistoryItem(
        val id: String,
        val productName: String,
        val activity: String,
        val time: String,
        val units: String,
        val supervisorName: String,
        val date: String // Used for sticky header grouping
)
