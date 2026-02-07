package com.farel.waresponder

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Environment
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.io.File

object LastReplyAction {
    var action: Notification.Action? = null
}

class NotificationService : NotificationListenerService() {

    private val logFile by lazy {
        File(getExternalFilesDir(null), "waresponder.log")
    }

    private fun log(msg: String) {
        try {
            val line = "${System.currentTimeMillis()} $msg\n"
            logFile.appendText(line)
        } catch (_: Exception) {}
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
        if (replyAction == null) {
            log("❌ Tidak ada tombol reply")
            return
        }

        LastReplyAction.action = replyAction

        // Kirim data ke Termux
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
