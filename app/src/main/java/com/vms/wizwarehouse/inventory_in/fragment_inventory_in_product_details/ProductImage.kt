package com.vms.wizwarehouse.inventory_in.fragment_inventory_in_product_details

import android.net.Uri

// ProductImage.kt
data class ProductImage(
    val uri: Uri
)

// InventoryItem.kt
data class InventoryItem(
    val category: String,
    val productName: String,
    val date: String,
    val remarks: String,
    val subCategory: String,
    val quantityUnits: String,
    val modeOfTransport: String,
    val images: List<ProductImage>
)
