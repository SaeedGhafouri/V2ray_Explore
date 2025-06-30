package com.serpider.v2rayexplore

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.serpider.v2rayexplore.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnConnection.setOnClickListener {
            handleConnection()
        }
        binding.txtPing.setOnClickListener {
            Toast.makeText(this, "Ping funtionality", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleConnection() {
        val configValue = binding.etConfig.text.toString().trim()
        when {
            configValue.isEmpty() -> {
                Toast.makeText(this,"Paste your v2ray config here", Toast.LENGTH_SHORT).show()
            }

        }
    }
}