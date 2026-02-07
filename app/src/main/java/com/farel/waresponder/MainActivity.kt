package com.farel.waresponder

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.content.pm.PackageManager

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // UI sederhana
        val tv = TextView(this)
        tv.text = "WAResponder aktif ✅\n\n" +
                "1. Aktifkan Notification Access\n" +
                "2. Jalankan Termux bot\n" +
                "3. Kirim WA untuk test"
        tv.textSize = 18f
        tv.setPadding(40, 80, 40, 40)
        setContentView(tv)

        // 🔥 PENTING: paksa reconnect NotificationListener
        toggleNotificationListenerService()

        // 🔥 buka halaman Notification Access otomatis
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    private fun toggleNotificationListenerService() {
        val cn = ComponentName(this, NotificationService::class.java)
        val pm = packageManager

        pm.setComponentEnabledSetting(
            cn,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )

        pm.setComponentEnabledSetting(
            cn,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}
