package com.farel.waresponder

import android.app.Notification
import android.app.RemoteInput
import android.content.Intent
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
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

        // ✅ Support WhatsApp & WA Business
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

        // =====================================================
        // 🔥 AMBIL REPLY ACTION (SUPPORT WA TERBARU)
        // =====================================================

        var replyAction: Notification.Action? = null

        // 1️⃣ Cek normal actions (Android lama)
        sbn.notification.actions?.forEach { action ->
            if (action.remoteInputs != null) {
                replyAction = action
            }
        }

        // 2️⃣ Cek WearableExtender (WhatsApp modern)
        if (replyAction == null) {
            val wearableExtender = Notification.WearableExtender(sbn.notification)
            wearableExtender.actions.forEach { action ->
                if (action.remoteInputs != null) {
                    replyAction = action
                }
            }
        }

        if (replyAction == null) {
            log("❌ Tidak ada tombol reply (WA new system)")
            return
        }

        log("✅ Reply action ditemukan!")

        // =====================================================
        // 🧪 TEST AUTO REPLY LANGSUNG (HARDCODE)
        // =====================================================
        try {
            val intent = Intent()
            val bundle = Bundle()

            for (remoteInput in replyAction!!.remoteInputs) {
                bundle.putCharSequence(remoteInput.resultKey, "Halo dari bot 🤖")
            }

            RemoteInput.addResultsToIntent(replyAction!!.remoteInputs, intent, bundle)
            replyAction!!.actionIntent.send(this, 0, intent)

            log("✅ AUTO REPLY BERHASIL DIKIRIM!")

        } catch (e: Exception) {
            log("❌ Gagal kirim auto reply: ${e.message}")
        }

        // =====================================================
        // 🚀 KIRIM DATA KE TERMUX (NODE BOT)
        // =====================================================
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

            log("🚀 Data dikirim ke Termux")

        } catch (e: Exception) {
            log("❌ Gagal kirim ke Termux")
            log("Error: ${e.message}")
        }
    }
}
