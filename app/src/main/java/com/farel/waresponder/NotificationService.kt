package com.farel.waresponder

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
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

    // 🔥 IMPORTANT — filter notif summary WA
    private fun isGroupSummary(sbn: StatusBarNotification): Boolean {
        val extras = sbn.notification.extras
        // pakai string langsung agar kompatibel semua SDK
        return extras.getBoolean("android.isGroupSummary", false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") return

        // ❗ Skip summary notif (tidak punya tombol reply)
        if (isGroupSummary(sbn)) {
            log("⏭ Skip summary notification")
            return
        }

        val extras = sbn.notification.extras

        val title = extras.getString("android.title") ?: return
        val text = extras.getCharSequence("android.text")?.toString()
            ?: extras.getCharSequence("android.bigText")?.toString()
            ?: extras.getCharSequenceArray("android.textLines")?.joinToString("\n") ?: return

        log("📩 Notif dari: $pkg")
        log("👤 Pengirim: $title")
        log("💬 Pesan: $text")

        // 🔍 Cari tombol reply WhatsApp
        var replyAction: Notification.Action? = null

        // Cara normal
        sbn.notification.actions?.forEach { action ->
            if (action.remoteInputs != null) replyAction = action
        }

        // Cara wearable (WA Android 13+ sering taruh disini)
        if (replyAction == null) {
            val wearable = Notification.WearableExtender(sbn.notification)
            wearable.actions.forEach { action ->
                if (action.remoteInputs != null) replyAction = action
            }
        }

        if (replyAction == null) {
            log("❌ Tidak ada tombol reply")
            return
        }

        log("✅ Tombol reply ditemukan")

        LastReplyAction.action = replyAction

        // 🚀 Kirim ke HTTPS bot di Termux
        askBotAndReply(title, text)
    }

    // ===============================
    // HTTPS BOT COMMUNICATION
    // ===============================

    private fun askBotAndReply(sender: String, message: String) {
        Thread {
            try {
                log("🌐 Kirim ke bot HTTPS...")
                val reply = LocalApi.sendMessage(sender, message)

                if (reply.isNullOrEmpty()) {
                    log("🤖 Bot tidak memberi balasan")
                    return@Thread
                }

                log("🤖 Reply dari bot: $reply")
                sendReplyToWhatsapp(reply)

            } catch (e: Exception) {
                log("❌ Error HTTPS: ${e.message}")
            }
        }.start()
    }

    private fun sendReplyToWhatsapp(replyText: String) {
        LastReplyAction.action?.let { action ->
            try {
                val bundle = android.os.Bundle()

                action.remoteInputs?.forEach { ri ->
                    bundle.putCharSequence(ri.resultKey, replyText)
                }

                val intent = Intent()
                RemoteInput.addResultsToIntent(action.remoteInputs, intent, bundle)
                action.actionIntent.send(this, 0, intent)

                log("📤 Balasan terkirim ke WhatsApp")
                LocalApi.notifyReplySent(replyText)

            } catch (e: Exception) {
                log("❌ Gagal kirim reply: ${e.message}")
            }
        }
    }
}
