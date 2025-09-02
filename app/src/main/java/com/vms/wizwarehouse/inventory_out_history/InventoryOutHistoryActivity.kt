package com.vms.wizwarehouse.inventory_out_history;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout;
import android.widget.Spinner
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.datepicker.MaterialDatePicker

import com.vms.wizwarehouse.R;
import com.vms.wizwarehouse.inventory_in_history.InventoryInAdapter
import com.vms.wizwarehouse.inventory_out.DistributeInventoryActivity;
import com.vms.wizwarehouse.inventory_in_history.InventoryInHistoryActivity;
import com.vms.wizwarehouse.inventory_in_history.InventoryInHistoryItem

import com.vms.wizwarehouse.login.LoginActivity;
import com.vms.wizwarehouse.ota.UpdateChecker;
import com.vms.wizwarehouse.reports.ReportsActivity;
import com.vms.wizwarehouse.steps_to_update.StepsToUpdateActivity;
import com.vms.wizwarehouse.utils.SharedPreferenceUtils;
import com.vms.wizwarehouse.utils.Utility;
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class InventoryOutHistoryActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var fullName: TextView
    private lateinit var appVersion: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutNavigation: LinearLayout
    private lateinit var version: TextView
    private lateinit var add: TextView
    private val distributeList = mutableListOf<InventoryOutHistoryItem>()
    private lateinit var imgBack: ImageView
    private lateinit var spinner: Spinner
    private lateinit var adapterInventoryHistory: InventoryOutHistoryAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_out_history)

        name = findViewById(R.id.txt_name_dashboard)
        appVersion = findViewById(R.id.txtVersionName)
        email = findViewById(R.id.nav_txt_email_dashboard)
        fullName = findViewById(R.id.nav_txt_name_dashboard)
        drawerLayout = findViewById(R.id.drawer_layout_survey)
        layoutNavigation = findViewById(R.id.layout_navigation)
        recyclerView = findViewById(R.id.recyclerview_distribute_history)
        version = findViewById(R.id.txt_version)
        add = findViewById(R.id.txt_add)
        imgBack = findViewById(R.id.img_back)
        spinner = findViewById(R.id.spin_filter)

        add.setOnClickListener {
            startActivity(Intent(this, DistributeInventoryActivity::class.java))
            finish()
        }

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
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 ->{
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

        val userCode = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_CODE)
        val userName = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_NAME)

        name.text = userName
        fullName.text = userName
        email.text = userCode
        appVersion.text = Utility.setVersionName(this)
        version.text = Utility.setVersionName(this)

        layoutNavigation.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        findViewById<View>(R.id.nav_dashboard).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            finish()
        }

        findViewById<View>(R.id.nav_inventory).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, InventoryInHistoryActivity::class.java))
            finish()
        }

        findViewById<View>(R.id.nav_distribute).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
        }

        findViewById<View>(R.id.nav_update).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            UpdateChecker.checkForUpdate(this, userCode!!, userName!!, "drawer")
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
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        findViewById<View>(R.id.nav_report).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        // Dummy data
        repeat(3) { dateIndex ->
            val date = when (dateIndex) {
                0 -> "28 July 2025"
                1 -> "30 July 2025"
                else -> "31 July 2025"
            }
            distributeList.addAll(
                listOf(
                    InventoryOutHistoryItem(
                        "ID123",
                        "Laptop",
                        "Noida",
                        "Rohit",
                        "10:00 AM",
                        "25 units",
                        date
                    ),
                    InventoryOutHistoryItem(
                        "ID124",
                        "Monitor",
                        "Noida",
                        "Amit",
                        "11:00 AM",
                        "10 units",
                        date
                    ),
                    InventoryOutHistoryItem(
                        "ID125",
                        "Mouse",
                        "Noida",
                        "Sumit",
                        "01:00 PM",
                        "50 units",
                        date
                    ),
                    InventoryOutHistoryItem(
                        "ID123",
                        "Laptop",
                        "Noida",
                        "Rohit",
                        "10:00 AM",
                        "25 units",
                        date
                    ),
                    InventoryOutHistoryItem(
                        "ID124",
                        "Monitor",
                        "Noida",
                        "Amit",
                        "11:00 AM",
                        "10 units",
                        date
                    ),
                    InventoryOutHistoryItem(
                        "ID125",
                        "Mouse",
                        "Noida",
                        "Sumit",
                        "01:00 PM",
                        "50 units",
                        date
                    )
                )
            )
        }

        adapterInventoryHistory = InventoryOutHistoryAdapter(distributeList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapterInventoryHistory

        val headerDecoration =
            StickyHeaderDecoration(this, object : StickyHeaderDecoration.StickyHeaderInterface {
                override fun getHeaderForPosition(position: Int): String {
                    return adapterInventoryHistory.getDateForPosition(position)
                }

                override fun isHeader(position: Int): Boolean {
                    return false
                }

                override fun getHeaderView(headerText: String, parent: RecyclerView): View {
                    val headerView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.item_sticky_header, parent, false)
                    val textView = headerView.findViewById<TextView>(R.id.headerText)
                    textView.text = headerText
                    return headerView
                }
            })

        recyclerView.addItemDecoration(headerDecoration)
    }

    private val allInventoryList: List<InventoryOutHistoryItem> = distributeList // keep original copy

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

    private fun updateRecycler(filteredList: List<InventoryOutHistoryItem>) {
        if (filteredList.isEmpty()) {
            recyclerView.visibility = View.GONE
//            demoImageView.visibility = View.VISIBLE
        } else {
            recyclerView.visibility = View.VISIBLE
//            demoImageView.visibility = View.GONE
            adapterInventoryHistory.updateData(filteredList)
        }
    }

    private fun showAllData() {
        updateRecycler(allInventoryList) // restore original list
    }
}
