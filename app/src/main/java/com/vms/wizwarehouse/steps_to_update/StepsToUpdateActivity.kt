package com.vms.wizwarehouse.steps_to_update;

import android.app.DownloadManager
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.vms.wizwarehouse.databinding.ActivityStepsToUpdateBinding
import com.vms.wizwarehouse.utils.Utility

class StepsToUpdateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStepsToUpdateBinding
    private lateinit var ok: Button
    private lateinit var version: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStepsToUpdateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ok = binding.btnOk
        version = binding.txtVersion

        version.text = Utility.setVersionName(this)

        ok.setOnClickListener {
            val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS)
            startActivity(intent)
        }
    }
}
