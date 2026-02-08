package com.farel.waresponder

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket
import org.json.JSONObject

object LocalSocketApi {

    // ⭐ HOST khusus Android untuk akses server lokal Termux
    private const val HOST = "10.0.2.2"
    private const val PORT = 8443

    fun sendMessage(jsonMessage: String): String? {
        return try {
            val socket = Socket()

            // connect timeout
            socket.connect(InetSocketAddress(HOST, PORT), 5000)
            socket.soTimeout = 5000

            val writer = PrintWriter(
                BufferedWriter(OutputStreamWriter(socket.getOutputStream())),
                true
            )

            val reader = BufferedReader(
                InputStreamReader(socket.getInputStream())
            )

            // kirim JSON ke server Termux
            writer.println(jsonMessage)

            // baca balasan server
            val response = reader.readLine()

            socket.close()

            if (response != null) {
                val json = JSONObject(response)
                json.optString("reply", null)
            } else null

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
