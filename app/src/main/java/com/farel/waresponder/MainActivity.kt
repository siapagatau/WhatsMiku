package com.farel.waresponder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tv = TextView(this)
        tv.text = "WAResponder aktif 👍\nJangan tutup aplikasi."
        tv.textSize = 18f

        setContentView(tv)
    }
}