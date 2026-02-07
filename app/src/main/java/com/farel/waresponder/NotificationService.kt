package com.farel.waresponder

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import android.widget.Toast
import android.util.Log

class NotificationService : NotificationListenerService() {

    private val TAG = "WAResponder"

    private fun log(msg: String) {
        Log.d(TAG, msg)
        Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        log("Notification Listener AKTIF ✅")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName
        log("Notif masuk dari package: $pkg")

        // hanya WhatsApp
        if (pkg != "com.whatsapp") {
            log("Bukan WhatsApp, dilewati")
            return
        }

        val notification = sbn.notification
        val extras = notification.extras

        val title = extras.getString("android.title")
        val text = extras.getCharSequence("android.text")?.toString()

        if (title == null || text == null) {
            log("Notif tidak punya title/text ❌")
            return
        }

        log("Pesan WA terdeteksi 📩")
        log("Pengirim: $title")
        log("Isi pesan: $text")

        val actions = notification.actions
        if (actions == null) {
            log("Notif tidak punya actions ❌")
            return
        }

        var hasReply = false
        for (action in actions) {
            if (action.remoteInputs != null) {
                hasReply = true
                break
            }
        }

        if (!hasReply) {
            log("Pesan tidak bisa direply (mungkin grup/missed call) ❌")
            return
        }

        // 🔥 KIRIM KE TERMUX
        log("Mengirim perintah ke Termux...")

        try {
            val intent = Intent()
            intent.setClassName(
                "com.termux",
                "com.termux.app.RunCommandReceiver"
            )

            intent.action = "com.termux.RUN_COMMAND"

            intent.putExtra(
                "com.termux.RUN_COMMAND_PATH",
                "/data/data/com.termux/files/usr/bin/node"
            )

            intent.putExtra(
                "com.termux.RUN_COMMAND_ARGUMENTS",
                arrayOf("/sdcard/botwa/wabot.js", title, text)
            )

            intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)

            sendBroadcast(intent)

            log("BERHASIL kirim ke Termux 🚀")

        } catch (e: Exception) {
            log("GAGAL kirim ke Termux ❌")
            log("Error: ${e.message}")
        }
    }
}