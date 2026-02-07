package com.farel.waresponder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class TermuxReceiver : BroadcastReceiver() {

    private val TAG = "WAResponder"

    override fun onReceive(context: Context, intent: Intent) {

        val replyText = intent.getStringExtra("reply") ?: return
        val key = intent.getStringExtra("key") ?: "dummy"

        Log.d(TAG, "📥 TermuxReceiver menerima reply: $replyText (key=$key)")

        // Kirim broadcast ke action WA_REPLY → ReplyReceiver akan menangkap
        val i = Intent("WA_REPLY")
        i.putExtra("reply", replyText)
        i.putExtra("key", key)
        context.sendBroadcast(i)
    }
}
