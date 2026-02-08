package com.farel.waresponder

import android.content.Intent
import android.app.RemoteInput
import android.app.Notification
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

        // Jalankan Node langsung
        askBotAndReply(title, text)
    }

private fun askBotAndReply(sender:String, message:String){
    Thread {
        val reply = LocalApi.sendMessage(sender, message) ?: return@Thread
        sendReplyToWhatsapp(reply)
    }.start()
}

private fun sendReplyToWhatsapp(replyText:String){
    LastReplyAction.action?.let { action ->
        try {
            val bundle = android.os.Bundle()
            action.remoteInputs?.forEach { ri ->
                bundle.putCharSequence(ri.resultKey, replyText)
            }
            RemoteInput.addResultsToIntent(action.remoteInputs, Intent(), bundle)
            action.actionIntent.send(this, 0, Intent())
            LocalApi.notifyReplySent(replyText)
        } catch (_: Exception) {}
    }
}
}
