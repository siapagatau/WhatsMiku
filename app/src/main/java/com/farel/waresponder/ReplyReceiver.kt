package com.farel.waresponder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.app.RemoteInput

class ReplyReceiver : BroadcastReceiver() {

    private val TAG = "WAResponder"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != "WA_REPLY") return

        val replyText = intent.getStringExtra("reply") ?: return
        val key = intent.getStringExtra("key") ?: "dummy"

        Log.d(TAG, "📤 WA_REPLY diterima: $replyText (key=$key)")

        // Kirim balasan ke WA via last replyAction
        LastReplyAction.action?.let { action ->
            try {
                val bundle = android.os.Bundle()
                action.remoteInputs?.forEach { ri -> bundle.putCharSequence(ri.resultKey, replyText) }
                RemoteInput.addResultsToIntent(action.remoteInputs, Intent(), bundle)
                action.actionIntent.send(context, 0, Intent())
                Log.d(TAG, "✅ Balasan dikirim ke WA: $replyText")
            } catch (e: Exception) {
                Log.d(TAG, "❌ Gagal kirim balasan: ${e.message}")
            }
        }
    }
}
