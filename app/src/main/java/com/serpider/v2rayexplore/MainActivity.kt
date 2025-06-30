package com.serpider.v2rayexplore

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.serpider.v2rayexplore.databinding.ActivityMainBinding
import com.serpider.v2rayexplore.utils.ConfigType
import com.serpider.v2rayexplore.utils.DetectConfigType
import org.json.JSONException
import org.json.JSONObject

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
        val config = binding.etConfig.text.toString().trim()

        if (config.isEmpty()) {
            Toast.makeText(this, "Paste v2ray config", Toast.LENGTH_SHORT).show()
            return
        }
        val configType = DetectConfigType.checkType(config)

        when (configType) {
            ConfigType.JSON -> {
                Toast.makeText(this, "JSON ${configType}", Toast.LENGTH_SHORT).show()
            }
            ConfigType.VMESS -> {
                Toast.makeText(this, "VMESS ${configType}", Toast.LENGTH_SHORT).show()
            }
            ConfigType.VLESS -> {
                Toast.makeText(this, "VLESS ${configType}", Toast.LENGTH_SHORT).show()
            }
            ConfigType.TROJAN -> {
                Toast.makeText(this, "TROJAN ${configType}", Toast.LENGTH_SHORT).show()
            }
            ConfigType.SHADOWSOCKS -> {
                Toast.makeText(this, "SHADOWSOCKS ${configType}", Toast.LENGTH_SHORT).show()
            }
            ConfigType.UNKNOWN -> {
                Toast.makeText(this, "Invalid config ${configType}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

