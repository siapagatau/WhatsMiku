package com.farel.waresponder

import java.io.*
import java.net.Socket

object LocalSocketApi {
    private const val HOST = "127.0.0.1"
    private const val PORT = 8443

    fun sendMessage(jsonMessage: String): String? {
        return try {
            Socket(HOST, PORT).use { socket ->
                socket.soTimeout = 3000 // timeout 3 detik

                val writer = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                writer.println(jsonMessage) // kirim pesan
                val line = reader.readLine() // baca balasan

                if (line != null) {
                    val obj = org.json.JSONObject(line)
                    obj.optString("reply", null)
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }
}
