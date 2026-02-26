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

        val tv = TextView(this)
        tv.text = "WAResponder aktif ✅\n\n" +
                "1. Aktifkan Notification Access\n" +
                "2. Jalankan Termux bot\n" +
                "3. Kirim WA untuk test"
        tv.textSize = 18f
        tv.setPadding(40, 80, 40, 40)
        setContentView(tv)

        toggleNotificationListenerService()

        // ✅ Cek dulu sebelum buka settings
        if (!isNotificationServiceEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val cn = ComponentName(this, NotificationService::class.java)
        val flat = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_NOTIFICATION_LISTENERS
        ) ?: return false

        return flat.contains(cn.flattenToString())
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
