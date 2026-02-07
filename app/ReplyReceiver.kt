package com.farel.waresponder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.RemoteInput

class ReplyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {

        val reply = intent.getStringExtra("reply") ?: return

        val remoteInput = RemoteInput.Builder("key_text_reply")
            .setLabel("Reply")
            .build()

        val bundle = android.os.Bundle()
        bundle.putCharSequence("key_text_reply", reply)

        val replyIntent = Intent()
        RemoteInput.addResultsToIntent(arrayOf(remoteInput), replyIntent, bundle)

        context.sendBroadcast(replyIntent)
    }
}