package com.farel.waresponder

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONObject
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
            val line = "${System.currentTimeMillis()} $msg"
            logFile.appendText("$line\n")
            LocalSocketApi.sendLog(line)
        } catch (_: Exception) {}
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        log("✅ Notification Listener CONNECTED")
    }

    // ================= FILTER =================

    private fun isGroupSummary(sbn: StatusBarNotification): Boolean {
        return sbn.notification.extras.getBoolean("android.isGroupSummary", false)
    }

    private fun isSystemWhatsappNotification(title: String): Boolean {
        return title == "WhatsApp"
    }

    private fun isFromSelf(sbn: StatusBarNotification): Boolean {
        val extras = sbn.notification.extras

        // 1️⃣ Outgoing flag (paling aman kalau ada)
        if (extras.getBoolean("android.isOutgoing", false)) {
            return true
        }

        // 2️⃣ Prefix teks
        val text = extras.getCharSequence("android.text")?.toString() ?: ""
        if (text.startsWith("You:", true) || text.startsWith("Anda:", true)) {
            return true
        }

        // 3️⃣ Nama diri sendiri == title
        val selfName = extras.getString("android.selfDisplayName")
        val title = extras.getString("android.title")
        if (!selfName.isNullOrEmpty() && selfName == title) {
            return true
        }

        return false
    }

    // ================= DUPLICATE =================

    private var lastMessageHash: Int? = null

    private fun isDuplicate(sender: String, message: String): Boolean {
        val hash = (sender + message).hashCode()
        if (hash == lastMessageHash) return true
        lastMessageHash = hash
        return false
    }

    // ================= MAIN =================

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val pkg = sbn.packageName
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") return

        val extras = sbn.notification.extras
        val title = extras.getString("android.title") ?: return

        // 🚫 Summary
        if (isGroupSummary(sbn)) {
            log("⏭ Skip summary notification")
            return
        }

        // 🚫 Notif sistem WhatsApp
        if (isSystemWhatsappNotification(title)) {
            log("⏭ Skip WhatsApp system notification")
            return
        }

        // 🚫 Pesan dari diri sendiri
        if (isFromSelf(sbn)) {
            log("⏭ Skip pesan dari diri sendiri")
            return
        }

        val text =
            extras.getCharSequence("android.text")?.toString()
                ?: extras.getCharSequence("android.bigText")?.toString()
                ?: extras.getCharSequenceArray("android.textLines")?.joinToString("\n")
                ?: return

        if (isDuplicate(title, text)) {
            log("⏭ Skip duplicate message")
            return
        }

        log("📩 Notif dari: $pkg")
        log("👤 Pengirim: $title")
        log("💬 Pesan: $text")

        // ================= REPLY ACTION =================

        var replyAction: Notification.Action? = null

        sbn.notification.actions?.forEach {
            if (it.remoteInputs != null) replyAction = it
        }

        if (replyAction == null) {
            val wearable = Notification.WearableExtender(sbn.notification)
            wearable.actions.forEach {
                if (it.remoteInputs != null) replyAction = it
            }
        }

        if (replyAction == null) {
            log("❌ Tidak ada tombol reply")
            return
        }

        log("✅ Tombol reply ditemukan")
        LastReplyAction.action = replyAction

        askBotAndReply(title, text)
    }

    // ================= BOT =================

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
                action.remoteInputs?.forEach {
                    bundle.putCharSequence(it.resultKey, replyText)
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
