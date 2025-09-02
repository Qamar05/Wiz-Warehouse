package com.vms.wizwarehouse.inventory_in

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout

import com.vms.wizwarehouse.R
import com.vms.wizwarehouse.databinding.ActivityInventoryInBinding
import com.vms.wizwarehouse.inventory_in.fragment_inventory_in_general_details.InventoryInGeneralDetailsFragment
import com.vms.wizwarehouse.inventory_out_history.InventoryOutHistoryActivity
import com.vms.wizwarehouse.inventory_in_history.InventoryInHistoryActivity
import com.vms.wizwarehouse.login.LoginActivity
import com.vms.wizwarehouse.ota.UpdateChecker
import com.vms.wizwarehouse.reports.ReportsActivity
import com.vms.wizwarehouse.steps_to_update.StepsToUpdateActivity
import com.vms.wizwarehouse.utils.SharedPreferenceUtils
import com.vms.wizwarehouse.utils.Utility


class InventoryInActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInventoryInBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var name: TextView
    private lateinit var email: TextView
    private lateinit var fullName: TextView
    private lateinit var appVersion: TextView
    private var userCode: String? = null
    private var userName: String? = null
    private lateinit var imgBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInventoryInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        name = binding.txtNameDashboard
        appVersion = binding.txtVersionName
        email = binding.navTxtEmailDashboard
        fullName = binding.navTxtNameDashboard
        drawerLayout = binding.drawerLayoutSurvey
        imgBack = binding.imgBack
        val layoutNavigation: LinearLayout = binding.layoutNavigation

        imgBack.setOnClickListener {
            finish()
        }

        name.text = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_NAME)
        fullName.text = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_NAME)
        email.text = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_CODE)
        appVersion.text = Utility.setVersionName(this)

        userCode = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_CODE)
        userName = SharedPreferenceUtils.getString(this, SharedPreferenceUtils.USER_NAME)

        layoutNavigation.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        binding.navDashboard.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            finish()
        }

        binding.navInventory.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, InventoryInHistoryActivity::class.java))
            finish()
        }

        binding.navDistribute.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, InventoryOutHistoryActivity::class.java))
            finish()
        }

        binding.navUpdate.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            UpdateChecker.checkForUpdate(this, userCode!!, userName!!, "drawer")
        }

        binding.navSteps.setOnClickListener {
            startActivity(Intent(this, StepsToUpdateActivity::class.java))
        }

        binding.navLogout.setOnClickListener {
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

        binding.navReport.setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.START)
            startActivity(Intent(this, ReportsActivity::class.java))
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_layout, InventoryInGeneralDetailsFragment())
            .commit()
    }
}
