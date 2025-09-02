package com.vms.wizwarehouse.inventory_in_history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.inventory_in.InventoryInActivity
import com.vms.wizwarehouse.inventory_out_history.InventoryOutHistoryActivity
import com.vms.wizwarehouse.login.LoginActivity
import com.vms.wizwarehouse.ota.UpdateChecker
import com.vms.wizwarehouse.reports.ReportsActivity
import com.vms.wizwarehouse.steps_to_update.StepsToUpdateActivity
import com.vms.wizwarehouse.utils.SharedPreferenceUtils
import com.vms.wizwarehouse.utils.Utility
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryInHistoryActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var fullName: TextView
    private lateinit var appVersion: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutNavigation: LinearLayout
    private lateinit var version: TextView
    private lateinit var spinner: Spinner
    private lateinit var add: TextView

    private lateinit var userCode: String
    private lateinit var userName: String
    private lateinit var imgBack: ImageView
    private val inventoryList = mutableListOf<InventoryInHistoryItem>()
    private lateinit var adapterInventory: InventoryInAdapter

    private lateinit var demoEmptyImg: ImageView
    private lateinit var demoEmptyText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_in_history)

        name = findViewById(R.id.txt_name_dashboard)
        appVersion = findViewById(R.id.txtVersionName)
        email = findViewById(R.id.nav_txt_email_dashboard)
        fullName = findViewById(R.id.nav_txt_name_dashboard)
        drawerLayout = findViewById(R.id.drawer_layout_survey)
        recyclerView = findViewById(R.id.recyclerview_inventory_history)
        layoutNavigation = findViewById(R.id.layout_navigation)
        spinner = findViewById(R.id.spin_filter)
        add = findViewById(R.id.txt_add)
        version = findViewById(R.id.txt_version)
        imgBack = findViewById(R.id.img_back)
        demoEmptyImg = findViewById(R.id.demoEmptyImage)
        demoEmptyText = findViewById(R.id.txt_no_inventory_found)

        imgBack.setOnClickListener {
            finish()
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.filter_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                when (position) {
                    0 -> {
                        showAllData()
                    }

                    1 -> { // Single date
                        val datePicker =
                            MaterialDatePicker.Builder.datePicker()
                                .setTitleText("Select Date")
                                .build()

                        datePicker.addOnPositiveButtonClickListener { selectedDate ->
                            // selectedDate is in UTC millis
                            filterRecyclerViewByDate(selectedDate)
                        }

                        datePicker.show(supportFragmentManager, "DATE_PICKER")
                    }

                    2 -> { // Date range
                        val dateRangePicker =
                            MaterialDatePicker.Builder.dateRangePicker()
                                .setTitleText("Select Date Range")
                                .build()

                        dateRangePicker.addOnPositiveButtonClickListener { dateRange ->
                            val startDate = dateRange.first
                            val endDate = dateRange.second
                            filterRecyclerViewByDateRange(startDate, endDate)
                        }

                        dateRangePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

//        val options = listOf("Select Date", "Select Range")
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, options)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        spinner.adapter = adapter
//
//        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
//                when (position) {
//                    0 -> openSingleDatePicker()
//                    1 -> openRangeDatePicker()
//                }
//            }
//            override fun onNothingSelected(parent: AdapterView<*>?) {}
//        }

        add.setOnClickListener {
            startActivity(Intent(this, InventoryInActivity::class.java))
            finish()
        }

        val userNamePref = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_NAME)
        val userCodePref = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_CODE)

        name.text = userNamePref
        fullName.text = userNamePref
        email.text = userCodePref
        appVersion.text = Utility.setVersionName(this)
        version.text = Utility.setVersionName(this)

        userCode = userCodePref!!
        userName = userNamePref!!

        layoutNavigation.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        findViewById<View>(R.id.nav_inventory).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<View>(R.id.nav_distribute).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, InventoryOutHistoryActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.nav_dashboard).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            finish()
        }

        findViewById<View>(R.id.nav_update).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            UpdateChecker.checkForUpdate(this, userCode, userName, "drawer")
        }

        findViewById<View>(R.id.nav_steps).setOnClickListener {
            startActivity(Intent(this, StepsToUpdateActivity::class.java))
        }

        findViewById<View>(R.id.nav_logout).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            SharedPreferenceUtils.saveBoolean(
                this,
                SharedPreferenceUtils.IS_LOGGED_IN_WAREHOUSE,
                false
            )
            SharedPreferenceUtils.saveBoolean(
                this,
                SharedPreferenceUtils.IS_CHECKED_IN_WAREHOUSE,
                false
            )
            startActivity(Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            })
            finish()
        }

        findViewById<View>(R.id.nav_report).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        populateDummyInventory()

        recyclerView.layoutManager = LinearLayoutManager(this)
        adapterInventory = InventoryInAdapter(inventoryList)
        recyclerView.adapter = adapterInventory

        val headerDecoration =
            HeaderItemDecoration(this, object : HeaderItemDecoration.StickyHeaderInterface {
                override fun getHeaderForPosition(position: Int): String {
                    return adapterInventory.getDateForPosition(position)
                }

                override fun isHeader(position: Int): Boolean {
                    return adapterInventory.itemCount > 0 && position < adapterInventory.itemCount
                }

                override fun getHeaderView(headerText: String, parent: RecyclerView): View {
                    val headerView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_sticky_header, parent, false)
                    headerView.findViewById<TextView>(R.id.headerText).text = headerText
                    return headerView
                }
            })

        recyclerView.addItemDecoration(headerDecoration)
    }

    private fun populateDummyInventory() {
        val items = listOf(
            InventoryInHistoryItem(
                "ID123",
                "Laptop",
                "Rohit",
                "10:00 AM",
                "25 units",
                "28 July 2025"
            ),
            InventoryInHistoryItem(
                "ID124",
                "Monitor",
                "Amit",
                "11:00 AM",
                "10 units",
                "28 July 2025"
            ),
            InventoryInHistoryItem(
                "ID125",
                "Mouse",
                "Sumit",
                "01:00 PM",
                "50 units",
                "28 July 2025"
            ),
            InventoryInHistoryItem(
                "ID123",
                "Laptop",
                "Rohit",
                "10:00 AM",
                "25 units",
                "30 July 2025"
            ),
            InventoryInHistoryItem(
                "ID124",
                "Monitor",
                "Amit",
                "11:00 AM",
                "10 units",
                "30 July 2025"
            ),
            InventoryInHistoryItem(
                "ID125",
                "Mouse",
                "Sumit",
                "01:00 PM",
                "50 units",
                "30 July 2025"
            ),
            InventoryInHistoryItem(
                "ID123",
                "Laptop",
                "Rohit",
                "10:00 AM",
                "25 units",
                "31 July 2025"
            ),
            InventoryInHistoryItem(
                "ID124",
                "Monitor",
                "Amit",
                "11:00 AM",
                "10 units",
                "31 July 2025"
            ),
            InventoryInHistoryItem(
                "ID125",
                "Mouse",
                "Sumit",
                "01:00 PM",
                "50 units",
                "31 July 2025"
            )
        )

        // Add multiple times for demonstration
        repeat(1) {
            inventoryList.addAll(items)
        }
    }

//    private fun openSingleDatePicker() {
//        val datePicker = MaterialDatePicker.Builder.datePicker()
//            .setTitleText("Select Date")
//            .build()
//
//        datePicker.show(supportFragmentManager, "DATE_PICKER")
//
//        datePicker.addOnPositiveButtonClickListener { selectedDate ->
//            val calendar = Calendar.getInstance()
//            calendar.timeInMillis = selectedDate
//            val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
//
//            updateRecyclerView(listOf(date), null) // pass single date
//        }
//    }

//    private fun openRangeDatePicker() {
//        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker()
//            .setTitleText("Select Date Range")
//            .build()
//
//        dateRangePicker.show(supportFragmentManager, "DATE_RANGE_PICKER")
//
//        dateRangePicker.addOnPositiveButtonClickListener { range ->
//            val start = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//                .format(Date(range.first!!))
//            val end = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
//                .format(Date(range.second!!))
//
//            updateRecyclerView(null, Pair(start, end)) // pass range
//        }
//    }

//    private fun updateRecyclerView(singleDate: List<String>?, range: Pair<String, String>?) {
//        val filteredList = if (singleDate != null) {
//            inventoryList.filter { it.date == singleDate[0] }
//        } else {
//            inventoryList.filter { it.date in range!!.first..range.second }
//        }
//
//        if (filteredList.isEmpty()) {
//            recyclerView.visibility = View.GONE
////            demoImage.visibility = View.VISIBLE
//        } else {
////            demoImage.visibility = View.GONE
//            recyclerView.visibility = View.VISIBLE
//            adapterInventory.updateData(filteredList)
//        }
//    }

    private val allInventoryList: List<InventoryInHistoryItem> = inventoryList // keep original copy

    private fun filterRecyclerViewByDate(dateMillis: Long) {
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        val selectedDate = sdf.format(Date(dateMillis))

        val filteredList = allInventoryList.filter { item ->
            item.date == selectedDate
        }

        updateRecycler(filteredList)
    }

    private fun filterRecyclerViewByDateRange(startMillis: Long?, endMillis: Long?) {
        if (startMillis == null || endMillis == null) return
        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())

        val filteredList = allInventoryList.filter { item ->
            val itemMillis = try {
                sdf.parse(item.date)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
            itemMillis in startMillis..endMillis
        }

        updateRecycler(filteredList)
    }

    private fun updateRecycler(filteredList: List<InventoryInHistoryItem>) {
        if (filteredList.isEmpty()) {
            recyclerView.visibility = View.GONE
            demoEmptyImg.visibility = View.VISIBLE
            demoEmptyText.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
            demoEmptyImg.visibility = View.INVISIBLE
            demoEmptyText.visibility = View.GONE
            adapterInventory.updateData(filteredList)
        }
    }

    private fun showAllData() {
        updateRecycler(allInventoryList) // restore original list
    }


}
