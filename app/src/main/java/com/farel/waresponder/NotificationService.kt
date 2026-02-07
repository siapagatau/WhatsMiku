package com.farel.waresponder

import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.app.Notification
import android.app.RemoteInput
import java.io.File

class NotificationService : NotificationListenerService() {

    private val TAG = "WAResponder"

    // ========================
    // LOG RELIABLE
    // ========================
    private fun log(msg: String) {
        Log.d(TAG, msg)        // optional, kadang ga muncul di Android 12+
        logToTermux(msg)       // realtime di Termux
        logToFile(msg)         // persistent
    }

    private fun logToTermux(msg: String) {
        try {
            val intent = Intent("com.farel.waresponder.LOG")
            intent.putExtra("log", msg)
            sendBroadcast(intent)
        } catch (_: Exception) {}
    }

    private fun logToFile(msg: String) {
        try {
            val file = File(getExternalFilesDir(null), "waresponder.log")
            file.appendText("${System.currentTimeMillis()} $msg\n")
        } catch (_: Exception) {}
    }

    // ========================
    // NotificationListener
    // ========================
    override fun onListenerConnected() {
        super.onListenerConnected()
        log("✅ Notification Listener CONNECTED")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {

        val pkg = sbn.packageName
        log("📩 Notif dari: $pkg")

        // ✅ Support WhatsApp & WA Business
        if (pkg != "com.whatsapp" && pkg != "com.whatsapp.w4b") {
            log("Bukan WhatsApp → skip")
            return
        }

        val extras = sbn.notification.extras

        // 🔎 Ambil isi pesan (multi fallback)
        val title = extras.getString("android.title")
        var text = extras.getCharSequence("android.text")?.toString()
            ?: extras.getCharSequence("android.bigText")?.toString()
            ?: extras.getCharSequenceArray("android.textLines")?.joinToString("\n")

        if (title.isNullOrEmpty() || text.isNullOrEmpty()) {
            log("❌ Gagal ambil title/text")
            return
        }

        log("👤 Pengirim : $title")
        log("💬 Pesan    : $text")

        // =====================================================
        // 🚀 KIRIM DATA KE TERMUX (NODE BOT)
        // =====================================================
        try {
            val intent = Intent().apply {
                setClassName(
                    "com.termux",
                    "com.termux.app.RunCommandReceiver"
                )
                setPackage("com.termux")
                action = "com.termux.RUN_COMMAND"
                putExtra(
                    "com.termux.RUN_COMMAND_PATH",
                    "/data/data/com.termux/files/usr/bin/node"
                )
                putExtra(
                    "com.termux.RUN_COMMAND_ARGUMENTS",
                    arrayOf("/sdcard/botwa/wabot.js", title, text)
                )
                putExtra("com.termux.RUN_COMMAND_BACKGROUND", true)
            }

            sendBroadcast(intent)
            log("🚀 Data dikirim ke Termux")

        } catch (e: Exception) {
            log("❌ Gagal kirim ke Termux")
            log("Error: ${e.message}")
        }
    }
}
