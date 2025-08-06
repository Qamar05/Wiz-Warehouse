package com.vms.wizwarehouse.inventory_history

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.add_inventory.AddInventoryActivity
import com.vms.wizwarehouse.distribute_history.DistributeHistoryActivity
import com.vms.wizwarehouse.login.LoginActivity
import com.vms.wizwarehouse.ota.UpdateChecker
import com.vms.wizwarehouse.reports.ReportsActivity
import com.vms.wizwarehouse.steps_to_update.StepsToUpdateActivity
import com.vms.wizwarehouse.utils.SharedPreferenceUtils
import com.vms.wizwarehouse.utils.Utility

class InventoryHistoryActivity : AppCompatActivity() {

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

    private val inventoryList = mutableListOf<InventoryHistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_history)

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

        add.setOnClickListener {
            startActivity(Intent(this, AddInventoryActivity::class.java))
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
            startActivity(Intent(this, DistributeHistoryActivity::class.java))
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
            SharedPreferenceUtils.saveBoolean(this, SharedPreferenceUtils.IS_LOGGED_IN_WAREHOUSE, false)
            SharedPreferenceUtils.saveBoolean(this, SharedPreferenceUtils.IS_CHECKED_IN_WAREHOUSE, false)
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
        val adapter = InventoryAdapter(inventoryList)
        recyclerView.adapter = adapter

        val headerDecoration = HeaderItemDecoration(this, object : HeaderItemDecoration.StickyHeaderInterface {
            override fun getHeaderForPosition(position: Int): String {
                return adapter.getDateForPosition(position)
            }

            override fun isHeader(position: Int): Boolean {
                return adapter.itemCount > 0 && position < adapter.itemCount
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
                InventoryHistoryItem("ID123", "Laptop", "Rohit", "10:00 AM", "25 units", "28 July 2025"),
                InventoryHistoryItem("ID124", "Monitor", "Amit", "11:00 AM", "10 units", "28 July 2025"),
                InventoryHistoryItem("ID125", "Mouse", "Sumit", "01:00 PM", "50 units", "28 July 2025"),
                InventoryHistoryItem("ID123", "Laptop", "Rohit", "10:00 AM", "25 units", "30 July 2025"),
                InventoryHistoryItem("ID124", "Monitor", "Amit", "11:00 AM", "10 units", "30 July 2025"),
                InventoryHistoryItem("ID125", "Mouse", "Sumit", "01:00 PM", "50 units", "30 July 2025"),
                InventoryHistoryItem("ID123", "Laptop", "Rohit", "10:00 AM", "25 units", "31 July 2025"),
                InventoryHistoryItem("ID124", "Monitor", "Amit", "11:00 AM", "10 units", "31 July 2025"),
                InventoryHistoryItem("ID125", "Mouse", "Sumit", "01:00 PM", "50 units", "31 July 2025")
        )

        // Add multiple times for demonstration
        repeat(1) {
            inventoryList.addAll(items)
        }
    }
}
