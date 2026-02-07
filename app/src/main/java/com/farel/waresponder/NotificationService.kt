package com.farel.waresponder

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

object LastReplyAction {
    var action: Notification.Action? = null
}

class NotificationService : NotificationListenerService() {

    private val TAG = "WAResponder"

    private fun log(msg: String) {
        Log.d(TAG, msg)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        log("✅ Notification Listener CONNECTED")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: return
        val text = extras.getCharSequence("android.text")?.toString()
            ?: extras.getCharSequence("android.bigText")?.toString()
            ?: extras.getCharSequenceArray("android.textLines")?.joinToString("\n") ?: return

        log("📩 Notif dari: $pkg")
        log("👤 Pengirim: $title")
        log("💬 Pesan: $text")

        // Ambil replyAction
        var replyAction: Notification.Action? = null
        sbn.notification.actions?.forEach { if (it.remoteInputs != null) replyAction = it }
        if (replyAction == null) {
            val wearable = Notification.WearableExtender(sbn.notification)
            wearable.actions.forEach { if (it.remoteInputs != null) replyAction = it }
        }
        if (replyAction == null) return

        LastReplyAction.action = replyAction

        // Kirim ke Termux
        try {
            val intent = Intent().apply {
                setClassName("com.termux", "com.termux.app.RunCommandReceiver")
                setPackage("com.termux")
                action = "com.termux.RUN_COMMAND"
                putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/node")
                putExtra(
                    "com.termux.RUN_COMMAND_ARGUMENTS",
                    arrayOf("/sdcard/botwa/wabot.js", title, text)
                )
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
            }
            sendBroadcast(intent)
            log("🚀 Data dikirim ke Termux")
        } catch (e: Exception) {
            log("❌ Gagal kirim ke Termux: ${e.message}")
        }
    }
}
