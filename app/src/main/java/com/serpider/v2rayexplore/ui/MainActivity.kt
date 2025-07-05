package com.serpider.v2rayexplore.ui


import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.serpider.v2rayexplore.R
import com.serpider.v2rayexplore.data.model.ConnectionState
import com.serpider.v2rayexplore.data.service.V2RayService
import com.serpider.v2rayexplore.databinding.ActivityMainBinding
import com.serpider.v2rayexplore.ui.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

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
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnConnection.setOnClickListener {
            when (viewModel.connectionState.value) {
                is ConnectionState.Disconnected -> {
                    val config = binding.etConfig.text.toString().trim()
                    viewModel.connect(config)
                }
                is ConnectionState.Connected -> {
                    viewModel.disconnect()
                    stopService(Intent(this, V2RayService::class.java))
                }
                else -> {}
            }
        }

        binding.txtPing.setOnClickListener {
            viewModel.ping()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.connectionState.collect { state ->
                updateUI(state)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.btnConnection.text = if (isLoading) "Connecting..." else getButtonText()
                binding.btnConnection.isEnabled = !isLoading
            }
        }

        lifecycleScope.launch {
            viewModel.toastMessage.collect { message ->
                message?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearToastMessage()
                }
            }
        }
    }

    private fun updateUI(state: ConnectionState) {
        binding.btnConnection.text = getButtonText()

        when (state) {
            is ConnectionState.Connected -> {
                startService(Intent(this, V2RayService::class.java))
            }
            else -> {}
        }
    }

    private fun getButtonText(): String {
        return when (viewModel.connectionState.value) {
            is ConnectionState.Disconnected -> "Connect"
            is ConnectionState.Connected -> "Disconnect"
            is ConnectionState.Connecting -> "Connecting..."
            is ConnectionState.Error -> "Connect"
        }
    }
}