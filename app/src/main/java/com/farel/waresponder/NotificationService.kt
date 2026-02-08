package com.farel.waresponder

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONObject
import java.io.File
import java.net.Socket
import java.io.*

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

    private fun isGroupSummary(sbn: StatusBarNotification): Boolean {
        val extras = sbn.notification.extras
        return extras.getBoolean("android.isGroupSummary", false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") return
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

        log("✅ Tombol reply ditemukan")
        LastReplyAction.action = replyAction

        askBotAndReply(title, text)
    }

    private fun askBotAndReply(sender: String, message: String) {
        Thread {
            try {
                log("🌐 Kirim ke Local Socket Bot...")

                val json = JSONObject().apply {
                    put("sender", sender)
                    put("message", message)
                }.toString()

                val reply = LocalSocketApi.sendMessage(json)

                if (reply.isNullOrEmpty()) {
                    log("🤖 Bot tidak memberi balasan")
                    return@Thread
                }

                log("🤖 Reply dari bot: $reply")
                sendReplyToWhatsapp(reply)

            } catch (e: Exception) {
                log("❌ Error socket: ${e.message}")
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
            } catch (e: Exception) {
                log("❌ Gagal kirim reply: ${e.message}")
            }
        }
    }
}
