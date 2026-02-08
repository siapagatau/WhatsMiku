package com.farel.waresponder

import org.json.JSONObject
import java.net.URL
import javax.net.ssl.*
import java.security.SecureRandom
import java.security.cert.X509Certificate

object LocalApi {

    // bypass self-signed certificate (localhost only)
    init {
        val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(a:Array<X509Certificate>,b:String){}
            override fun checkServerTrusted(a:Array<X509Certificate>,b:String){}
            override fun getAcceptedIssuers()=arrayOf<X509Certificate>()
        })
        val ssl = SSLContext.getInstance("SSL")
        ssl.init(null, trustAll, SecureRandom())
        HttpsURLConnection.setDefaultSSLSocketFactory(ssl.socketFactory)
        HttpsURLConnection.setDefaultHostnameVerifier { _, _ -> true }
    }

    fun sendMessage(sender:String, message:String): String? {
        try {
            val url = URL("https://127.0.0.1:8443/message")
            val conn = url.openConnection() as HttpsURLConnection
            conn.requestMethod = "POST"
            conn.doOutput = true
            conn.setRequestProperty("Content-Type","application/json")

            val json = JSONObject()
            json.put("sender", sender)
            json.put("message", message)

            conn.outputStream.write(json.toString().toByteArray())

            val response = conn.inputStream.bufferedReader().readText()
            val obj = JSONObject(response)

            return obj.optString("reply", null)

        } catch (e:Exception){
            return null
        }
    }

    fun notifyReplySent(reply:String){
        try{
            val url = URL("https://127.0.0.1:8443/reply")
            val conn = url.openConnection() as HttpsURLConnection
            conn.requestMethod="POST"
            conn.doOutput=true
            conn.setRequestProperty("Content-Type","application/json")

            val json = JSONObject()
            json.put("reply", reply)
            conn.outputStream.write(json.toString().toByteArray())
            conn.inputStream.close()
        }catch(_:Exception){}
    }
}
