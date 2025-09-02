package com.vms.wizwarehouse.pending_return

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.vms.wizwarehouse.databinding.ActivityPendingReturnBinding

class PendingReturnActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPendingReturnBinding
    private lateinit var imgBack: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPendingReturnBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imgBack = binding.imgBack

        imgBack.setOnClickListener {
            finish()
        }
    }
}