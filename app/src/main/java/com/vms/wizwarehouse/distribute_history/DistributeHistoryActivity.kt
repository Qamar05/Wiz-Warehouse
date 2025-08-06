package com.vms.wizwarehouse.distribute_history;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vms.wizwarehouse.R;
import com.vms.wizwarehouse.distribute_inventory.DistributeInventoryActivity;
import com.vms.wizwarehouse.inventory_history.InventoryHistoryActivity;

import com.vms.wizwarehouse.login.LoginActivity;
import com.vms.wizwarehouse.ota.UpdateChecker;
import com.vms.wizwarehouse.reports.ReportsActivity;
import com.vms.wizwarehouse.steps_to_update.StepsToUpdateActivity;
import com.vms.wizwarehouse.utils.SharedPreferenceUtils;
import com.vms.wizwarehouse.utils.Utility;


class DistributeHistoryActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var fullName: TextView
    private lateinit var appVersion: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var layoutNavigation: LinearLayout
    private lateinit var version: TextView
    private lateinit var add: TextView

    private val distributeList = mutableListOf<DistributeHistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_distribute_history)

        name = findViewById(R.id.txt_name_dashboard)
        appVersion = findViewById(R.id.txtVersionName)
        email = findViewById(R.id.nav_txt_email_dashboard)
        fullName = findViewById(R.id.nav_txt_name_dashboard)
        drawerLayout = findViewById(R.id.drawer_layout_survey)
        layoutNavigation = findViewById(R.id.layout_navigation)
        recyclerView = findViewById(R.id.recyclerview_distribute_history)
        version = findViewById(R.id.txt_version)
        add = findViewById(R.id.txt_add)

        add.setOnClickListener {
            startActivity(Intent(this, DistributeInventoryActivity::class.java))
            finish()
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
            startActivity(Intent(this, InventoryHistoryActivity::class.java))
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
            SharedPreferenceUtils.saveBoolean(this, SharedPreferenceUtils.IS_LOGGED_IN_WAREHOUSE, false)
            SharedPreferenceUtils.saveBoolean(this, SharedPreferenceUtils.IS_CHECKED_IN_WAREHOUSE, false)
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
                            DistributeHistoryItem("ID123", "Laptop", "Noida", "Rohit", "10:00 AM", "25 units", date),
                            DistributeHistoryItem("ID124", "Monitor", "Noida", "Amit", "11:00 AM", "10 units", date),
                            DistributeHistoryItem("ID125", "Mouse", "Noida", "Sumit", "01:00 PM", "50 units", date),
                            DistributeHistoryItem("ID123", "Laptop", "Noida", "Rohit", "10:00 AM", "25 units", date),
                            DistributeHistoryItem("ID124", "Monitor", "Noida", "Amit", "11:00 AM", "10 units", date),
                            DistributeHistoryItem("ID125", "Mouse", "Noida", "Sumit", "01:00 PM", "50 units", date)
                    )
            )
        }

        val adapter = DistributeAdapter(distributeList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val headerDecoration = StickyHeaderDecoration(this, object : StickyHeaderDecoration.StickyHeaderInterface {
            override fun getHeaderForPosition(position: Int): String {
                return adapter.getDateForPosition(position)
            }

            override fun isHeader(position: Int): Boolean {
                return false
            }

            override fun getHeaderView(headerText: String, parent: RecyclerView): View {
                val headerView = LayoutInflater.from(parent.context).inflate(R.layout.item_sticky_header, parent, false)
                val textView = headerView.findViewById<TextView>(R.id.headerText)
                        textView.text = headerText
                return headerView
            }
        })

        recyclerView.addItemDecoration(headerDecoration)
    }
}
