package com.farel.waresponder

import java.io.*
import java.net.Socket

object LocalSocketApi {

    private const val HOST = "127.0.0.1" // HP yang sama
    private const val PORT = 8443

    // Kirim pesan ke Node.js socket server dan dapat balasan
    fun sendMessage(jsonMessage: String): String? {
        return try {
            Socket(HOST, PORT).use { socket ->
                val writer = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                // Kirim JSON string
                writer.println(jsonMessage)

                // Baca balasan
                val line = reader.readLine()
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
