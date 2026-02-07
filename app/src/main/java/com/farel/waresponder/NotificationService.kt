package com.farel.waresponder

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle

class NotificationService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName

        // hanya WhatsApp (nanti bisa diubah dari Termux)
        if (pkg != "com.whatsapp") return

        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString("android.title") ?: return
        val text = extras.getCharSequence("android.text")?.toString() ?: return

        val actions = notification.actions ?: return
        var hasReply = false

        for (action in actions) {
            if (action.remoteInputs != null) {
                hasReply = true
                break
            }
        }

        if (!hasReply) return

        // 🔥 KIRIM KE TERMUX
        val intent = Intent()
        intent.setClassName(
            "com.termux",
            "com.termux.app.RunCommandReceiver"
        )

        intent.action = "com.termux.RUN_COMMAND"

        intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/node")

        intent.putExtra(
            "com.termux.RUN_COMMAND_ARGUMENTS",
            arrayOf("/sdcard/botwa/wabot.js", title, text)
        )

        intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)

        sendBroadcast(intent)
    }
}
