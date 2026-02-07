package com.farel.waresponder

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.content.Intent
import android.util.Log

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
        log("📩 Notif dari: $pkg")

        // ✅ Support WA & WA Business
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") {
            log("Bukan WhatsApp → skip")
            return
        }

        val extras = sbn.notification.extras

        // 🔎 Ambil isi pesan (multi fallback)
        val title = extras.getString("android.title")

        var text = extras.getCharSequence("android.text")?.toString()

        if (text == null)
            text = extras.getCharSequence("android.bigText")?.toString()

        if (text == null) {
            val lines = extras.getCharSequenceArray("android.textLines")
            if (lines != null && lines.isNotEmpty())
                text = lines.joinToString("\n")
        }

        if (title.isNullOrEmpty() || text.isNullOrEmpty()) {
            log("❌ Gagal ambil title/text")
            return
        }

        log("👤 Pengirim : $title")
        log("💬 Pesan    : $text")

        // 🔎 Cek bisa reply atau tidak
        val actions = sbn.notification.actions
        if (actions == null) {
            log("❌ Tidak ada tombol reply")
            return
        }

        var canReply = false
        for (action in actions) {
            if (action.remoteInputs != null) {
                canReply = true
                break
            }
        }

        if (!canReply) {
            log("❌ Pesan tidak bisa direply")
            return
        }

        // 🚀 KIRIM KE TERMUX
        log("🚀 Kirim ke Termux...")

        try {
            val intent = Intent()
            intent.setClassName(
                "com.termux",
                "com.termux.app.RunCommandReceiver"
            )

            intent.setPackage("com.termux")
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

            log("✅ SUKSES kirim ke Termux")

        } catch (e: Exception) {
            log("❌ GAGAL kirim ke Termux")
            log("Error: ${e.message}")
        }
    }
}
