package com.farel.waresponder

import java.io.*
import java.net.Socket
import org.json.JSONObject

object LocalSocketApi {
    private const val HOST = "127.0.0.1"
    private const val PORT = 8443

    fun sendMessage(jsonMessage: String): String? {
        return try {
            Socket(HOST, PORT).use { socket ->
                socket.soTimeout = 5000 // Tambah timeout jadi 5 detik

                val writer = PrintWriter(
                    BufferedWriter(
                        OutputStreamWriter(socket.getOutputStream())
                    ), 
                    true
                )
                val reader = BufferedReader(
                    InputStreamReader(socket.getInputStream())
                )

                // Kirim dengan newline di akhir (sesuai server)
                writer.println(jsonMessage)
                
                // Baca balasan
                val response = reader.readLine()
                
                if (response != null) {
                    val jsonResponse = JSONObject(response)
                    jsonResponse.optString("reply", null)
                } else {
                    null
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
