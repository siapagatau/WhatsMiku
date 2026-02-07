package com.farel.waresponder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.RemoteInput
import java.io.File

class ReplyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "WA_REPLY") return

        val replyText = intent.getStringExtra("reply") ?: return
        val key = intent.getStringExtra("key") ?: "dummy"

        val logFile = File(context.getExternalFilesDir(null), "waresponder.log")
        try {
            logFile.appendText("${System.currentTimeMillis()} 📤 WA_REPLY diterima: $replyText (key=$key)\n")
        } catch (_: Exception) {}

        // Kirim balasan ke WA via last replyAction
        LastReplyAction.action?.let { action ->
            try {
                val bundle = android.os.Bundle()
                action.remoteInputs?.forEach { ri ->
                    bundle.putCharSequence(ri.resultKey, replyText)
                }
                RemoteInput.addResultsToIntent(action.remoteInputs, Intent(), bundle)
                action.actionIntent.send(context, 0, Intent())
                logFile.appendText("${System.currentTimeMillis()} ✅ Balasan dikirim ke WA: $replyText\n")
            } catch (e: Exception) {
                logFile.appendText("${System.currentTimeMillis()} ❌ Gagal kirim balasan: ${e.message}\n")
            }
        }

        // Jalankan Node langsung di Termux (sinkron dengan NotificationService)
        try {
            val safeReply = replyText.replace("\"", "\\\"")
            val nodePath = "/data/data/com.termux/files/usr/bin/node"
            val scriptPath = "/sdcard/botwa/wabot.js"

            val process = ProcessBuilder(nodePath, scriptPath, safeReply)
                .redirectErrorStream(true)
                .start()

            val output = process.inputStream.bufferedReader().readText()
            logFile.appendText("${System.currentTimeMillis()} 🚀 Node output: $output\n")
        } catch (e: Exception) {
            logFile.appendText("${System.currentTimeMillis()} ❌ Gagal jalankan Node: ${e.message}\n")
        }
    }
}
