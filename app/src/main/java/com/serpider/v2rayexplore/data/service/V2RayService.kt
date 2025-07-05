package com.serpider.v2rayexplore.data.service


import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.serpider.v2rayexplore.R

class V2RayService : VpnService() {
    companion object {
        const val CHANNEL_ID = "V2RayServiceChannel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())

        // ساخت تونل VPN
        val builder = Builder()
        builder.addAddress("10.0.0.2", 32)
        builder.addRoute("0.0.0.0", 0)
        builder.setSession("V2Ray VPN")
        val vpnInterface = builder.establish()

        // الان تونل ساخته شد و ترافیک به سمت این اینترفیس هدایت می‌شود
        // شما باید V2Ray core را هم اینجا استارت کنید که دیتا را از این تونل بخواند و به سرور بفرستد.

        return START_STICKY
    }


    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "V2Ray Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("V2Ray Explorer")
            .setContentText("Connected to VPN")
            .setSmallIcon(R.drawable.ic_notification)
            .build()
    }
}