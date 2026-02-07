package com.farel.waresponder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "WAResponder aktif ✅\n\n1. Aktifkan Notification Access\n2. Jalankan Termux bot\n3. Kirim WA untuk test"
        tv.textSize = 18f
        tv.setPadding(40, 80, 40, 40)

        setContentView(tv)
    }
}