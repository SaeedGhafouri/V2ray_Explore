package com.serpider.v2rayexplore.ui

// In MainActivity.kt
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Bundle
import android.os.Build
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.serpider.v2rayexplore.R
import com.serpider.v2rayexplore.data.service.V2RayVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

// Add this import for ContextCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var etConfig: EditText
    private lateinit var btnToggleVpn: Button
    private lateinit var tvStatus: TextView
    private lateinit var btnPing: Button
    private lateinit var tvPingResult: TextView

    private var isVpnConnected = false

    // BroadcastReceiver for VPN status updates
    private val vpnStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "com.serpider.v2rayexplore.VPN_STATUS_UPDATE") {
                val isConnected = intent.getBooleanExtra("is_connected", false)
                updateVpnStatus(isConnected)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etConfig = findViewById(R.id.et_config)
        btnToggleVpn = findViewById(R.id.btn_toggle_vpn)
        tvStatus = findViewById(R.id.tv_status)
        btnPing = findViewById(R.id.btn_ping)
        tvPingResult = findViewById(R.id.tv_ping_result)

        etConfig.setText("vless://f73e9865-80c3-45ba-8391-597139c37bdc@91.99.129.82:443?security=&encryption=none&host=exo.ir&headerType=http&type=tcp# https://t.me/ConfigV2box")

        // بازیابی کانفیگ ذخیره شده
        val savedConfig = getSharedPreferences("vpn_prefs", Context.MODE_PRIVATE)
            .getString("v2ray_config", "")
        //etConfig.setText(savedConfig)

        btnToggleVpn.setOnClickListener {
            if (isVpnConnected) {
                stopVpnService()
            } else {
                startVpnService()
            }
        }

        btnPing.setOnClickListener {
            executePing("8.8.8.8", 4)
        }

        // Register the BroadcastReceiver with compatibility flag
        val filter = IntentFilter("com.serpider.v2rayexplore.VPN_STATUS_UPDATE")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33, Tiramisu
            // For API 33+, explicit receiver exported state is often required even for ContextCompat.registerReceiver
            ContextCompat.registerReceiver(this, vpnStatusReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // API 31, S (Snow Cone)
            // For API 31 and 32, Context.RECEIVER_NOT_EXPORTED is available
            registerReceiver(vpnStatusReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            // For older Android versions, no flag needed
            registerReceiver(vpnStatusReceiver, filter)
        }


        // بررسی وضعیت فعلی VPN (اختیاری: نیاز به بررسی دقیق‌تر سرویس)
        updateVpnStatus(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(vpnStatusReceiver) // Unregister to prevent memory leaks
    }

    private fun startVpnService() {
        val config = etConfig.text.toString().trim()
        if (config.isEmpty()) {
            Toast.makeText(this, "Please paste V2Ray config", Toast.LENGTH_SHORT).show()
            return
        }

        // ذخیره کانفیگ
        getSharedPreferences("vpn_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("v2ray_config", config)
            .apply()

        // درخواست مجوز VPN
        val vpnIntent = VpnService.prepare(this)
        if (vpnIntent != null) {
            startActivityForResult(vpnIntent, VPN_PERMISSION_REQUEST_CODE)
        } else {
            // مجوز قبلاً داده شده یا نیازی نیست
            onActivityResult(VPN_PERMISSION_REQUEST_CODE, RESULT_OK, null)
        }
    }

    private fun stopVpnService() {
        val intent = Intent(this, V2RayVpnService::class.java)
        stopService(intent)
        updateVpnStatus(false) // Optimistically update UI to disconnected
        Toast.makeText(this, "VPN Disconnected", Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VPN_PERMISSION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                val config = etConfig.text.toString().trim()
                val intent = Intent(this, V2RayVpnService::class.java).apply {
                    putExtra("config_data", config)
                }
                startService(intent)
                // Do NOT update UI to connected here. V2RayVpnService will send a broadcast.
                Toast.makeText(this, "VPN Connecting...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "VPN permission denied", Toast.LENGTH_SHORT).show()
                updateVpnStatus(false)
            }
        }
    }

    private fun updateVpnStatus(isConnected: Boolean) {
        isVpnConnected = isConnected
        if (isConnected) {
            tvStatus.text = "Status: Connected"
            btnToggleVpn.text = "Disconnect"
        } else {
            tvStatus.text = "Status: Disconnected"
            btnToggleVpn.text = "Connect"
        }
    }

    private fun executePing(host: String, count: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                withContext(Dispatchers.Main) {
                    tvPingResult.text = "Pinging $host..."
                }

                val command = "ping -c $count $host"
                val process = Runtime.getRuntime().exec(command)
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
                process.waitFor()

                withContext(Dispatchers.Main) {
                    tvPingResult.text = output.toString()
                    Toast.makeText(applicationContext, "Ping complete!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvPingResult.text = "Error during ping: ${e.message}"
                    Toast.makeText(applicationContext, "Ping failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
                Log.e("PingTest", "Error during ping: ${e.message}", e)
            }
        }
    }

    companion object {
        private const val VPN_PERMISSION_REQUEST_CODE = 1001
    }
}