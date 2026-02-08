package com.farel.waresponder

import java.io.*
import java.net.Socket

object LocalSocketApi {

    private const val HOST = "127.0.0.1" // HP yang sama
    private const val PORT = 8443

    fun sendMessage(message: String): String? {
        return try {
            Socket(HOST, PORT).use { socket ->
                val writer = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())), true)
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

                // Kirim pesan
                writer.println(message)

                // Baca reply
                reader.readLine()
            }
        } catch (e: Exception) {
            null
        }
    }
}
