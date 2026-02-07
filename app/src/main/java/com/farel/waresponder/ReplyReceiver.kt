package com.farel.waresponder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import android.app.RemoteInput
import android.app.Notification
import android.app.NotificationManager

class ReplyReceiver : BroadcastReceiver() {

    private val TAG = "WAResponder"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "WA_REPLY") return

        val reply = intent.getStringExtra("reply") ?: ""
        val key = intent.getStringExtra("key") ?: "dummy"

        Log.d(TAG, "📤 WA_REPLY diterima: $reply (key=$key)")
        Toast.makeText(context, "WA_REPLY: $reply", Toast.LENGTH_SHORT).show()

        // ===== Kirim balasan ke WhatsApp =====
        // Bisa lewat NotificationListenerService direct reply jika mau
        // Atau lewat PendingIntent WA jika sudah punya reference actionIntent
    }
}
