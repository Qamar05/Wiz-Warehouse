package com.vms.wizwarehouse.today_activity

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.databinding.ActivityTodayActivityBinding

class TodayActivityActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTodayActivityBinding
    private lateinit var imgBack: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TodayActivityAdapter
    private lateinit var activityList: ArrayList<TodayActivityItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTodayActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imgBack = binding.imgBack
        recyclerView = binding.recyclerviewTodayActivity

        imgBack.setOnClickListener {
            finish()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)

        // Sample data
        activityList = arrayListOf(
            TodayActivityItem("AB28682", "Testing Activity", "Abcdefgh", "Name1, Name2", "10:00 AM - 12:00 PM", "Noida"),
            TodayActivityItem("CD47892", "Site Visit", "Xyz", "Pqr, Lmn", "1:00 PM - 3:00 PM", "Delhi"),
            TodayActivityItem("EF98321", "Maintenance", "John Doe", "Alice, Bob", "4:00 PM - 6:00 PM", "Gurgaon"),
            TodayActivityItem("GH11223", "Training Session", "Ramesh Kumar", "Sita, Mohan", "9:00 AM - 11:00 AM", "Lucknow"),
//            TodayActivityItem("IJ33445", "Product Demo", "Priya Sharma", "Anil, Sunita", "11:30 AM - 1:00 PM", "Chandigarh"),
//            TodayActivityItem("KL55667", "Inventory Audit", "Amit Verma", "Ravi, Kiran", "2:00 PM - 4:00 PM", "Mumbai"),
//            TodayActivityItem("MN77889", "Quality Check", "Neha Gupta", "Vikas, Rohan", "3:30 PM - 5:30 PM", "Jaipur"),
//            TodayActivityItem("OP99001", "Client Visit", "Sanjay Singh", "Alok, Meena", "10:00 AM - 12:30 PM", "Bangalore"),
//            TodayActivityItem("QR11234", "Team Meeting", "Kavita Joshi", "Deepak, Tanya", "12:00 PM - 1:30 PM", "Hyderabad"),
//            TodayActivityItem("ST22345", "Warehouse Inspection", "Rohit Malhotra", "Nikhil, Arjun", "9:30 AM - 11:00 AM", "Pune"),
//            TodayActivityItem("UV33456", "Equipment Repair", "Pooja Mehta", "Raj, Sneha", "2:30 PM - 4:30 PM", "Kolkata"),
//            TodayActivityItem("WX44567", "Field Work", "Harish Chandra", "Manish, Kavya", "8:00 AM - 10:00 AM", "Ahmedabad"),
//            TodayActivityItem("YZ55678", "Stock Replenishment", "Mehul Patel", "Suresh, Divya", "4:30 PM - 6:30 PM", "Chennai")
        )

        adapter = TodayActivityAdapter(activityList)
        recyclerView.adapter = adapter
    }
}