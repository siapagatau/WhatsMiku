package com.farel.waresponder

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.content.pm.PackageManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnEnable = findViewById<Button>(R.id.btnEnable)

        btnEnable.setOnClickListener {
            toggleNotificationListenerService()
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }
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
