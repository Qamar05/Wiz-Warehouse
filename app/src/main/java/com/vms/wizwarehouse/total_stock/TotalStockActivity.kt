package com.vms.wizwarehouse.total_stock

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.databinding.ActivityTotalStockBinding

class TotalStockActivity : AppCompatActivity() {
    private lateinit var recyclerTotalStock: RecyclerView
    private lateinit var totalStockAdapter: TotalStockAdapter
    private lateinit var binding: ActivityTotalStockBinding
    private lateinit var imgBack: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTotalStockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imgBack = binding.imgBack

        imgBack.setOnClickListener {
            finish()
        }

        recyclerTotalStock = binding.recyclerviewTotalStock
        val totalStockList = listOf(
            TotalStockItem("Laptop", "Non-Perishable", "10 units", "25/08/2025"),
            TotalStockItem("Milk", "Perishable", "35 packets", "24/09/2025"),
            TotalStockItem("Rice", "Non-Perishable", "50 kg", "01/10/2025"),
            TotalStockItem("Laptop", "Non-Perishable", "10 units", "25/08/2025"),
            TotalStockItem("Milk", "Perishable", "35 packets", "24/09/2025"),
            TotalStockItem("Rice", "Non-Perishable", "50 kg", "01/10/2025"),
            TotalStockItem("Laptop", "Non-Perishable", "10 units", "25/08/2025"),
            TotalStockItem("Milk", "Perishable", "35 packets", "24/09/2025"),
            TotalStockItem("Rice", "Non-Perishable", "50 kg", "01/10/2025"),
            TotalStockItem("Laptop", "Non-Perishable", "10 units", "25/08/2025"),
            TotalStockItem("Milk", "Perishable", "35 packets", "24/09/2025"),
            TotalStockItem("Rice", "Non-Perishable", "50 kg", "01/10/2025"),
            TotalStockItem("Laptop", "Non-Perishable", "10 units", "25/08/2025"),
            TotalStockItem("Milk", "Perishable", "35 packets", "24/09/2025"),
            TotalStockItem("Rice", "Non-Perishable", "50 kg", "01/10/2025"),
            TotalStockItem("Laptop", "Non-Perishable", "10 units", "25/08/2025"),
            TotalStockItem("Milk", "Perishable", "35 packets", "24/09/2025"),
            TotalStockItem("Rice", "Non-Perishable", "50 kg", "01/10/2025"),
            TotalStockItem("Laptop", "Non-Perishable", "10 units", "25/08/2025"),
            TotalStockItem("Milk", "Perishable", "35 packets", "24/09/2025"),
            TotalStockItem("Rice", "Non-Perishable", "50 kg", "01/10/2025")
        )

        totalStockAdapter = TotalStockAdapter(totalStockList)
        recyclerTotalStock.layoutManager = LinearLayoutManager(this)
        recyclerTotalStock.adapter = totalStockAdapter
    }
}