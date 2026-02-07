package com.farel.waresponder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TermuxReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val replyText = intent.getStringExtra("reply") ?: return
        val key = intent.getStringExtra("key") ?: return

        val i = Intent(context, ReplyReceiver::class.java)
        i.putExtra("reply", replyText)
        i.putExtra("key", key)
        context.sendBroadcast(i)
    }
}