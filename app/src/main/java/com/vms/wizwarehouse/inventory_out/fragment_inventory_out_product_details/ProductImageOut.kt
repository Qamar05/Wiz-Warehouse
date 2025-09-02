package com.vms.wizwarehouse.inventory_out.fragment_inventory_out_product_details

import android.net.Uri

data class ProductImageOut(
    val uri: Uri
)

// InventoryItem.kt
data class InventoryItemOut(
    val category: String,
    val productName: String,
    val date: String,
    val subCategory: String,
    val itemNumber: String,
    val images: List<ProductImageOut>
)